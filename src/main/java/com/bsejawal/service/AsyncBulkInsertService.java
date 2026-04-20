package com.bsejawal.service;

import com.bsejawal.entity.AccountEntity;
import com.bsejawal.entity.PositionEntity;
import com.bsejawal.repository.batch.AccountBatchRepository;
import com.bsejawal.repository.batch.PositionBatchRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsyncBulkInsertService {

    private final AccountBatchRepository accountBatchRepository;
    private final PositionBatchRepository positionBatchRepository;

    @Async("bulkInsertExecutor")
    @Transactional
    public CompletableFuture<Integer> insertAccountsAsync(List<AccountEntity> accounts) {
        int insertedCount = accountBatchRepository.batchInsert(accounts);
        return CompletableFuture.completedFuture(insertedCount);
    }

    @Async("bulkInsertExecutor")
    @Transactional
    public CompletableFuture<Integer> insertPositionsAsync(List<PositionEntity> positions) {
        int insertedCount = positionBatchRepository.batchInsert(positions);
        return CompletableFuture.completedFuture(insertedCount);
    }
}
