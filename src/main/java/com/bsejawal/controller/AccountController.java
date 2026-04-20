package com.bsejawal.controller;

import com.bsejawal.dto.AccountRequest;
import com.bsejawal.dto.BulkIngestResponse;
import com.bsejawal.service.AccountIngestionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountIngestionService accountIngestionService;

    @PostMapping("/bulk")
    public ResponseEntity<BulkIngestResponse> bulkInsert(
            @RequestBody(required = false) List<AccountRequest> requests) {
        return ResponseEntity.ok(accountIngestionService.ingestAccounts(requests));
    }
}
