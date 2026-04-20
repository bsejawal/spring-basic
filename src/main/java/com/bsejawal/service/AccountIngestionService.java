package com.bsejawal.service;

import com.bsejawal.dto.AccountRequest;
import com.bsejawal.dto.BulkIngestResponse;
import com.bsejawal.entity.AccountEntity;
import com.bsejawal.entity.PositionEntity;
import com.bsejawal.mapper.AccountMapper;
import com.bsejawal.repository.AccountRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountIngestionService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AsyncBulkInsertService asyncBulkInsertService;

    public BulkIngestResponse ingestAccounts(List<AccountRequest> requests) {
        int totalReceived = requests == null ? 0 : requests.size();
        if (requests == null || requests.isEmpty()) {
            return buildResponse(0, 0, 0, 0, 0, "Request payload is empty.");
        }

        DeduplicationResult deduplicationResult = deduplicatePayload(requests);
        List<AccountRequest> uniqueRequests = deduplicationResult.uniqueRequests();

        if (uniqueRequests.isEmpty()) {
            return buildResponse(
                    totalReceived,
                    deduplicationResult.duplicatesInPayload(),
                    0,
                    0,
                    0,
                    buildMessage(0, 0, deduplicationResult.invalidAccountNumbers()));
        }

        Set<String> accountNos = new HashSet<>();
        for (AccountRequest request : uniqueRequests) {
            accountNos.add(accountMapper.normalizeAccountNo(request.getAccountNo()));
        }

        Set<String> existingAccountNos = accountNos.isEmpty()
                ? Set.of()
                : new HashSet<>(accountRepository.findExistingAccountNos(accountNos));

        List<AccountRequest> accountsToInsert = uniqueRequests.stream()
                .filter(request -> !existingAccountNos.contains(accountMapper.normalizeAccountNo(request.getAccountNo())))
                .toList();

        List<AccountEntity> accountEntities = accountMapper.toAccountEntities(accountsToInsert);
        List<PositionEntity> positionEntities = accountMapper.toPositionEntities(accountsToInsert);

        int insertedAccounts = 0;
        int insertedPositions = 0;

        try {
            CompletableFuture<Integer> accountInsertFuture = asyncBulkInsertService.insertAccountsAsync(accountEntities);
            CompletableFuture<Integer> positionInsertFuture = accountInsertFuture.thenCompose(insertedAccountCount -> {
                if (insertedAccountCount == 0 || positionEntities.isEmpty()) {
                    return CompletableFuture.completedFuture(0);
                }
                return asyncBulkInsertService.insertPositionsAsync(positionEntities);
            });

            insertedAccounts = accountInsertFuture.join();
            insertedPositions = positionInsertFuture.join();
        } catch (CompletionException ex) {
            throw new IllegalStateException("Bulk insert failed.", ex.getCause());
        }

        return buildResponse(
                totalReceived,
                deduplicationResult.duplicatesInPayload(),
                existingAccountNos.size(),
                insertedAccounts,
                insertedPositions,
                buildMessage(insertedAccounts, insertedPositions, deduplicationResult.invalidAccountNumbers()));
    }

    private DeduplicationResult deduplicatePayload(List<AccountRequest> requests) {
        LinkedHashMap<String, AccountRequest> uniqueAccounts = new LinkedHashMap<>();
        int duplicatesInPayload = 0;
        int invalidAccountNumbers = 0;

        for (AccountRequest request : requests) {
            if (request == null) {
                invalidAccountNumbers++;
                continue;
            }

            String normalizedAccountNo = accountMapper.normalizeAccountNo(request.getAccountNo());
            if (normalizedAccountNo == null) {
                invalidAccountNumbers++;
                continue;
            }

            AccountRequest existing = uniqueAccounts.putIfAbsent(normalizedAccountNo, request);
            if (existing != null) {
                duplicatesInPayload++;
            }
        }

        return new DeduplicationResult(new ArrayList<>(uniqueAccounts.values()), duplicatesInPayload, invalidAccountNumbers);
    }

    private BulkIngestResponse buildResponse(
            int totalReceived,
            int duplicatesInPayload,
            int duplicatesInDatabase,
            int insertedAccounts,
            int insertedPositions,
            String message) {
        return BulkIngestResponse.builder()
                .totalReceived(totalReceived)
                .duplicatesInPayload(duplicatesInPayload)
                .duplicatesInDatabase(duplicatesInDatabase)
                .insertedAccounts(insertedAccounts)
                .insertedPositions(insertedPositions)
                .message(message)
                .build();
    }

    private String buildMessage(int insertedAccounts, int insertedPositions, int invalidAccountNumbers) {
        List<String> parts = new ArrayList<>();
        if (insertedAccounts > 0) {
            parts.add("Bulk ingest completed successfully.");
        } else {
            parts.add("No new accounts were inserted.");
        }

        if (invalidAccountNumbers > 0) {
            parts.add("Ignored " + invalidAccountNumbers + " record(s) with blank accountNo.");
        }

        parts.add("Inserted " + insertedAccounts + " account(s) and " + insertedPositions + " position(s).");
        return String.join(" ", parts);
    }

    private record DeduplicationResult(
            List<AccountRequest> uniqueRequests,
            int duplicatesInPayload,
            int invalidAccountNumbers) {
    }
}
