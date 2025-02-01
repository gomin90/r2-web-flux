package com.gomin.r2webflux.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationResponse {
    private Long id;
    private String sfid;
    private String status;
    private String message;

    public AccountCreationResponse(Long id) {
        this.id = id;
        this.status = "PENDING";
        this.message = "Account creation in progress";
    }

    public void setSfid(String sfid) {
        this.sfid = sfid;
        this.status = "SYNCED";
        this.message = "Account successfully synchronized with Salesforce";
    }

    public void setError(String error) {
        this.status = "FAILED";
        this.message = error;
    }
}
