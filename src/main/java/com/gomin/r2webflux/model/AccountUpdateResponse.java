package com.gomin.r2webflux.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateResponse {
    private String sfid;
    private String status;
    private String message;

    public AccountUpdateResponse(String sfid) {
        this.sfid = sfid;
        this.status = "PENDING";
        this.message = "Update in progress";
    }

    public void setCompleted() {
        this.status = "SYNCED";
        this.message = "Account successfully updated in Salesforce";
    }

    public void setError(String error) {
        this.status = "FAILED";
        this.message = "Update failed: " + error;
    }
}
