package com.gomin.r2webflux.repository;

import com.gomin.r2webflux.domain.Account;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends R2dbcRepository<Account, Long> {
    
    @Query("SELECT * FROM salesforce.account WHERE id = :id")
    Mono<Account> findById(Long id);
    
    @Query("SELECT * FROM salesforce.account WHERE sfid = :sfid")
    Mono<Account> findBySfid(String sfid);
    
    @Query("SELECT * FROM salesforce.account WHERE is_deleted = false ORDER BY last_modified_date DESC")
    Flux<Account> findAllActive();
    
    @Query("SELECT * FROM salesforce.account WHERE industry = :industry AND is_deleted = false")
    Flux<Account> findByIndustry(String industry);
    
    @Query("SELECT * FROM salesforce.account WHERE type = :type AND is_deleted = false")
    Flux<Account> findByType(String type);
    
    @Query("SELECT * FROM salesforce.account WHERE customer_priority__c = :priority AND is_deleted = false")
    Flux<Account> findByCustomerPriority(String priority);

    @Query("SELECT salesforce.hc_capture_insert_from_row(hstore(salesforce.account.*), 'account')::text " +
           "FROM salesforce.account WHERE id = :id")
    Mono<String> captureInsert(Long id);

    @Query("SELECT salesforce.hc_capture_update_from_row(hstore(salesforce.account.*), 'account', ARRAY['parent__external_id__c'])::text " +
           "FROM salesforce.account WHERE sfid = :sfid")
    Mono<String> captureUpdate(String sfid);

    @Query("SELECT sfid FROM salesforce.account WHERE id = :id")
    Mono<String> findSfidById(Long id);

    @Query("SELECT _hc_err FROM salesforce.account WHERE id = :id")
    Mono<String> checkHerokuConnectError(Long id);
}
