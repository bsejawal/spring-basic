package com.bsejawal.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    private String accountNo;
    private String palAccount;
    private String name;
    private List<PositionRequest> positions;
    private String address;
    private String city;
    private String state;
}
