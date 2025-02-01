package com.gomin.r2webflux.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Transient;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.gomin.r2webflux.state.AccountState;

@Data
@Table(schema = "salesforce", name = "account")
public class Account {
    @Id
    private Long id;

    @Column("sfid")
    private String sfid;

    private String name;
    private String type;
    private String accountNumber;
    private Integer numberOfEmployees;
    private String site;
    private Double annualRevenue;
    private String phone;
    private String fax;
    private String website;
    private String photoUrl;
    private String industry;
    private String rating;
    private String ownership;
    private String description;

    // Address fields
    private String billingStreet;
    private String billingCity;
    private String billingState;
    private String billingPostalCode;
    private String billingCountry;
    private Double billingLatitude;
    private Double billingLongitude;
    private String billingGeocodeAccuracy;

    // Person fields
    private String personContactId;
    private String firstName;
    private String lastName;
    private Boolean isPersonAccount;
    private String personEmail;
    private String personTitle;
    private String personMobilePhone;
    private LocalDate personBirthdate;

    // Custom fields
    @Column("active__c")
    private String active;
    
    @Column("customerpriority__c")
    private String customerPriority;
    
    @Column("numberoflocations__c")
    private Double numberOfLocations;
    
    @Column("sla__c")
    private String sla;
    
    @Column("slaexpirationdate__c")
    private LocalDate slaExpirationDate;
    
    @Column("slaserialnumber__c")
    private String slaSerialNumber;
    
    @Column("upsellopportunity__c")
    private String upsellOpportunity;

    // System fields
    private String ownerId;
    private LocalDateTime createdDate;
    private String createdById;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedById;
    private LocalDateTime systemModstamp;
    private Boolean isDeleted;
    private LocalDateTime lastViewedDate;
    private LocalDateTime lastReferencedDate;

    // Heroku Connect fields
    @Column("_hc_lastop")
    private String hcLastop;
    
    @Column("_hc_err")
    private String hcErr;

    // State field
    @Transient  // DB 컬럼과 매핑하지 않음
    private AccountState state;


    public AccountState getState() {
        if (this.getSfid() != null) {
            return AccountState.SYNCED;
        } else if (this.getHcErr() != null) {
            return AccountState.ERROR;
        }
        return AccountState.PENDING;
    }
}
