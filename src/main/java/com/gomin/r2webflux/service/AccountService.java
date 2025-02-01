package com.gomin.r2webflux.service;

import com.gomin.r2webflux.domain.Account;
import com.gomin.r2webflux.state.AccountState;
import com.gomin.r2webflux.repository.AccountRepository;
import com.gomin.r2webflux.model.AccountCreationResponse; // 추가된 import
import com.gomin.r2webflux.model.AccountUpdateResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j  // 추가
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
   
    public Flux<Account> getAllAccounts() {
        return accountRepository.findAllActive();
    }
    
    public Mono<Account> getAccountById(Long id) {
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Account not found with id: " + id)));
    }
    
    public Mono<Account> getAccountBySfid(String sfid) {
        return accountRepository.findBySfid(sfid)
            .switchIfEmpty(Mono.error(new RuntimeException("Account not found with sfid: " + sfid)));
    }
    
    private void updateAccountFields(Account existing, Account update) {
        existing.setName(update.getName());
        existing.setType(update.getType());
        existing.setIndustry(update.getIndustry());
        existing.setDescription(update.getDescription());
        existing.setPhone(update.getPhone());
        existing.setWebsite(update.getWebsite());
        existing.setActive(update.getActive());
        existing.setLastModifiedDate(LocalDateTime.now());
        existing.setSystemModstamp(LocalDateTime.now());
    }

    public Mono<Void> deleteAccount(String sfid) {
        return accountRepository.findBySfid(sfid)
            .switchIfEmpty(Mono.error(new RuntimeException("Account not found with SFID: " + sfid)))
            .flatMap(account -> {
                account.setIsDeleted(true);
                account.setHcLastop("DELETE");
                return accountRepository.save(account);
            })
            .flatMap(this::waitForSalesforceSync)
            .then();
    }

    private Mono<Account> waitForSalesforceSync(Account account) {
        return accountRepository.findById(account.getId())
            .filter(acc -> acc.getHcErr() == null)
            .switchIfEmpty(Mono.error(new RuntimeException("Salesforce synchronization failed")))
            .retryWhen(Retry.fixedDelay(30, Duration.ofSeconds(1))
                .doBeforeRetry(retrySignal -> 
                    log.debug("Waiting for Salesforce sync, attempt: {}", retrySignal.totalRetries()))
            )
            .timeout(Duration.ofSeconds(31));
    }

    public Flux<Account> getAccountsByIndustry(String industry) {
        return accountRepository.findByIndustry(industry);
    }
    
    public Flux<Account> getAccountsByType(String type) {
        return accountRepository.findByType(type);
    }
    
    public Flux<Account> getAccountsByCustomerPriority(String priority) {
        return accountRepository.findByCustomerPriority(priority);
    }

    public Mono<AccountState> getState(String sfid) {
        return accountRepository.findBySfid(sfid)
            .map(Account::getState);
    }

    public Mono<Account> handleHerokuConnectUpdate(Account account) {
        return Mono.just(account)
            .filter(acc -> "UPDATE".equals(acc.getHcLastop()))
            .flatMap(acc -> {
                log.info("Processing Heroku Connect update for account: {}", acc.getSfid());
                return accountRepository.save(acc);
            })
            .defaultIfEmpty(account);
    }

    public Mono<AccountCreationResponse> createAccount(Account account) {
        account.setCreatedDate(LocalDateTime.now());
        account.setLastModifiedDate(LocalDateTime.now());
        account.setSystemModstamp(LocalDateTime.now());
        account.setHcLastop("INSERT");
        account.setIsDeleted(false);

        return accountRepository.save(account)
            .flatMap(savedAccount -> {
                log.info("Account created with ID: {}", savedAccount.getId());
                AccountCreationResponse response = new AccountCreationResponse(savedAccount.getId());
                
                return accountRepository.captureInsert(savedAccount.getId())
                    .doOnNext(result -> log.info("Capture insert result: {}", result))
                    .thenReturn(response);
            })
            .doOnError(error -> log.error("Error in account creation: {}", error.getMessage()));
    }

    public Mono<AccountUpdateResponse> updateAccount(Account account) {
        
        String sfid = account.getSfid();
       
        Mono<Account> existingAccountMono = accountRepository.findBySfid(sfid)
            .switchIfEmpty(Mono.error(new RuntimeException("Account not found with SFID: " + sfid)));

        return existingAccountMono
            .map(existingAccount -> {
            updateAccountFields(existingAccount, account);
            existingAccount.setHcLastop("PENDING");
            return existingAccount;
            })
            .flatMap(accountRepository::save)
            .flatMap(updatedAccount -> {
            log.info("Account updated with ID: {}", updatedAccount.getId());
            AccountUpdateResponse response = new AccountUpdateResponse(updatedAccount.getSfid());
            return accountRepository.captureUpdate(updatedAccount.getSfid())
                .doOnNext(result -> log.info("Capture update result: {}", result))
                .thenReturn(response);
            });

    }

    public Mono<AccountCreationResponse> checkAccountStatus(Long id) {
        return accountRepository.findById(id)
            .map(account -> {
                AccountCreationResponse response = new AccountCreationResponse(id);
                if (account.getSfid() != null && !account.getSfid().isEmpty()) {
                    response.setSfid(account.getSfid());
                    response.setStatus("SYNCED");
                    response.setMessage("Account synchronized with Salesforce");
                } else if (account.getHcErr() != null) {
                    response.setStatus("FAILED");
                    response.setMessage("Sync failed: " + account.getHcErr());
                } else {
                    response.setStatus("PENDING");
                    response.setMessage("Waiting for Salesforce synchronization");
                }
                return response;
            })
            .doOnNext(response -> log.debug("Status check for ID {}: {}", id, response.getStatus()));
    }

    public Mono<AccountUpdateResponse> checkAccountUpdateStatus(String sfid) {
        return accountRepository.findBySfid(sfid)
            .map(account -> {
                AccountUpdateResponse response = new AccountUpdateResponse(sfid);
                if (account.getHcLastop() != null && !account.getHcLastop().isEmpty() && "UPDATED".equals(account.getHcLastop())) {
                    response.setSfid(account.getSfid());
                    response.setStatus("SYNCED");
                    response.setMessage("Account synchronized with Salesforce");
                } else if (account.getHcErr() != null) {
                    response.setStatus("FAILED");
                    response.setMessage("Sync failed: " + account.getHcErr());
                } else {
                    response.setStatus("PENDING");
                    response.setMessage("Waiting for Salesforce synchronization");
                }
                return response;
            })
            .doOnNext(response -> log.debug("Status check for ID {}: {}", sfid, response.getStatus()));
    }
}
