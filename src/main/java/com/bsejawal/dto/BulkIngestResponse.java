package com.bsejawal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkIngestResponse {

    private int totalReceived;
    private int duplicatesInPayload;
    private int duplicatesInDatabase;
    private int insertedAccounts;
    private int insertedPositions;
    private String message;
}
