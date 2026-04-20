package com.bsejawal.mapper;

import com.bsejawal.dto.AccountRequest;
import com.bsejawal.dto.PositionRequest;
import com.bsejawal.entity.AccountEntity;
import com.bsejawal.entity.PositionEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public List<AccountEntity> toAccountEntities(List<AccountRequest> requests) {
        List<AccountEntity> accounts = new ArrayList<>();
        if (requests == null) {
            return accounts;
        }

        for (AccountRequest request : requests) {
            String normalizedAccountNo = normalizeAccountNo(request.getAccountNo());
            if (normalizedAccountNo == null) {
                continue;
            }

            accounts.add(AccountEntity.builder()
                    .accountNo(normalizedAccountNo)
                    .palAccount(normalizeText(request.getPalAccount()))
                    .name(normalizeText(request.getName()))
                    .address(normalizeText(request.getAddress()))
                    .city(normalizeText(request.getCity()))
                    .state(normalizeText(request.getState()))
                    .build());
        }

        return accounts;
    }

    public List<PositionEntity> toPositionEntities(List<AccountRequest> requests) {
        List<PositionEntity> positions = new ArrayList<>();
        if (requests == null) {
            return positions;
        }

        for (AccountRequest request : requests) {
            String normalizedAccountNo = normalizeAccountNo(request.getAccountNo());
            if (normalizedAccountNo == null || request.getPositions() == null) {
                continue;
            }

            for (PositionRequest positionRequest : request.getPositions()) {
                PositionEntity position = toPositionEntity(normalizedAccountNo, positionRequest);
                if (position != null) {
                    positions.add(position);
                }
            }
        }

        return positions;
    }

    public String normalizeAccountNo(String accountNo) {
        return normalizeText(accountNo);
    }

    private PositionEntity toPositionEntity(String accountNo, PositionRequest positionRequest) {
        if (positionRequest == null) {
            return null;
        }

        String normalizedStock = normalizeStock(positionRequest.getStock());
        if (normalizedStock == null || positionRequest.getValue() == null) {
            return null;
        }

        return PositionEntity.builder()
                .accountNo(accountNo)
                .stock(normalizedStock)
                .value(positionRequest.getValue())
                .build();
    }

    private String normalizeStock(String stock) {
        String normalized = normalizeText(stock);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
