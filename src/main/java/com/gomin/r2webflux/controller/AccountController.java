package com.gomin.r2webflux.controller;

import com.gomin.r2webflux.domain.Account;
import com.gomin.r2webflux.service.AccountService;
import com.gomin.r2webflux.state.AccountState;
import com.gomin.r2webflux.model.AccountCreationResponse; // 추가
import com.gomin.r2webflux.model.AccountUpdateResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus; // 추가
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent; // 추가
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account management APIs")
@SecurityRequirements // 필요한 경우 보안 설정 추가
public class AccountController {
    private final AccountService accountService;

    @Operation(summary = "Get all accounts", description = "Retrieves a list of all active accounts")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts")
    @GetMapping
    public Flux<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @Operation(summary = "Get account by ID", description = "Retrieves an account by its ID")
    @ApiResponse(responseCode = "200", description = "Account found")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @GetMapping("/{id}")
    public Mono<Account> getAccountById(
            @Parameter(description = "Account ID", required = true) @PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/sfid/{sfid}")
    @Operation(summary = "Get account by Salesforce ID")
    public Mono<Account> getAccountBySfid(
            @Parameter(description = "Salesforce Account ID", required = true) @PathVariable String sfid) {
        return accountService.getAccountBySfid(sfid);
    }

    @PutMapping(value = "/sfid/{sfid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Update account Field", description = """
            Update account with Server-Sent Events stream for status updates.

            Frontend Implementation Guide:
            ```javascript
            const sfid = '001xx000003DGb0AAG';
            const eventSource = new EventSource('/api/v1/accounts/sfid/' + sfid);  // 템플릿 리터럴 대신 문자열 연결 사용
            
            eventSource.addEventListener('STARTED', (event) => {
                const data = JSON.parse(event.data);
                console.log('Update started:', data);
            });
            // ...existing code...
            ```
            // ...existing code...
            """, responses = {
            @ApiResponse(responseCode = "200", description = "SSE stream started", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = ServerSentEvent.class), examples = @ExampleObject(value = """
                    event:STARTED
                    data:{"id":null,"status":"PROCESSING","message":"Account Updating started"}

                    event:PENDING
                    data:{"id":12345,"status":"PENDING","message":"Waiting for Update"}

                    event:SYNCED
                    data:{"id":12345,"sfid":"001xx000003DGb0AAG","status":"SYNCED"}
                    """)))
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Flux<ServerSentEvent<AccountUpdateResponse>> updateAccount(
        @PathVariable String sfid, @RequestBody Account account) {
        AccountUpdateResponse initialResponse = new AccountUpdateResponse(sfid);

        initialResponse.setStatus("PROCESSING");
        initialResponse.setMessage("Account updating started");
        
        account.setSfid(sfid);
        return accountService.updateAccount(account)
       .flatMapMany(updateAccount -> {
            ServerSentEvent<AccountUpdateResponse> startEvent = ServerSentEvent
                .<AccountUpdateResponse>builder()
                .event("STARTED")
                .data(initialResponse)
                .build();

            Flux<ServerSentEvent<AccountUpdateResponse>> statusStream = Flux.interval(Duration.ofSeconds(1))
                .take(60)
                .flatMap(i -> accountService.checkAccountUpdateStatus(sfid))
                .map(status -> ServerSentEvent.<AccountUpdateResponse>builder()
                    .event(status.getStatus())
                    .data(status)
                    .build())
                .takeUntil(sse -> "SYNCED".equals(sse.data().getStatus()) ||
                    "FAILED".equals(sse.data().getStatus()));


            return Flux.concat(Flux.just(startEvent), statusStream
                );
       });
    }

    @GetMapping("/industry/{industry}")
    public Flux<Account> getAccountsByIndustry(@PathVariable String industry) {
        return accountService.getAccountsByIndustry(industry);
    }

    @GetMapping("/type/{type}")
    public Flux<Account> getAccountsByType(@PathVariable String type) {
        return accountService.getAccountsByType(type);
    }

    @GetMapping("/priority/{priority}")
    public Flux<Account> getAccountsByCustomerPriority(@PathVariable String priority) {
        return accountService.getAccountsByCustomerPriority(priority);
    }

    @Operation(summary = "Create new account", description = """
            Creates a new account with Server-Sent Events stream for status updates.

            Implementation Options:

            1. EventSource (Standard):
            ```javascript
            const eventSource = new EventSource('/api/v1/accounts');
            // ...existing EventSource implementation...
            ```

            2. Fetch API:
            ```javascript
            const response = await fetch('/api/v1/accounts', {
                headers: { 'Accept': 'text/event-stream' }
            });
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            
            while (true) {
                const {value, done} = await reader.read();
                if (done) break;
                
                const chunks = decoder.decode(value).split('\\n\\n');
                chunks.forEach(chunk => {
                    if (chunk) {
                        const [eventLine, dataLine] = chunk.split('\\n');
                        const event = eventLine.replace('event:', '');
                        const data = JSON.parse(dataLine.replace('data:', ''));
                        
                        switch(event) {
                            case 'STARTED':
                                console.log('Creation started:', data);
                                break;
                            case 'SYNCED':
                                console.log('Synced:', data);
                                break;
                            // ... handle other events
                        }
                    }
                });
            }
            ```

            3. RxJS (Observable):
            ```typescript
            import { fromEvent } from 'rxjs';
            import { map, takeUntil, filter } from 'rxjs/operators';

            const sse$ = new EventSource('/api/v1/accounts');
            
            // Handle specific events
            const started$ = fromEvent(sse$, 'STARTED').pipe(
                map((event: MessageEvent) => JSON.parse(event.data))
            );
            
            const synced$ = fromEvent(sse$, 'SYNCED').pipe(
                map((event: MessageEvent) => JSON.parse(event.data))
            );
            
            // Subscribe to events
            started$.subscribe(data => console.log('Started:', data));
            synced$.subscribe(data => {
                console.log('Synced:', data);
                sse$.close();
            });
            
            // Error handling
            fromEvent(sse$, 'error').subscribe(error => {
                console.error('SSE Error:', error);
                sse$.close();
            });
            ```

            4. Axios SSE (with event-source-polyfill):
            ```javascript
            import EventSourcePolyfill from 'event-source-polyfill';
            import axios from 'axios';

            const eventSource = new EventSourcePolyfill('/api/v1/accounts', {
                headers: {
                    'Authorization': 'Bearer ' + token // if needed
                }
            });

            // Event handlers
            const handlers = {
                STARTED: (data) => console.log('Started:', data),
                SYNCED: (data) => {
                    console.log('Synced:', data);
                    eventSource.close();
                }
            };

            Object.entries(handlers).forEach(([event, handler]) => {
                eventSource.addEventListener(event, (e) => handler(JSON.parse(e.data)));
            });
            ```

            Example curl command:
            ```bash
            curl -N -H "Accept:text/event-stream" \\
                 -H "Content-Type:application/json" \\
                 -d '{"name":"Test Account","type":"Customer"}' \\
                 http://localhost:8080/api/v1/accounts
            ```

            Stream events:
            - STARTED: Initial creation started
            - PENDING: Waiting for SFID
            - SYNCED: Successfully synced with Salesforce
            - FAILED: Sync failed
            """, responses = {
            @ApiResponse(responseCode = "200", description = "SSE stream started", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = ServerSentEvent.class), examples = @ExampleObject(value = """
                    event:STARTED
                    data:{"id":null,"status":"PROCESSING","message":"Account creation started"}

                    event:PENDING
                    data:{"id":12345,"status":"PENDING","message":"Waiting for SFID"}

                    event:SYNCED
                    data:{"id":12345,"sfid":"001xx000003DGb0AAG","status":"SYNCED"}
                    """)))
    })
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Flux<ServerSentEvent<AccountCreationResponse>> createAccount(@RequestBody Account account) {
        // 초기 응답 생성
        AccountCreationResponse initialResponse = new AccountCreationResponse(null);
        initialResponse.setStatus("PROCESSING");
        initialResponse.setMessage("Account creation started");

        return accountService.createAccount(account)
                .flatMapMany(createdAccount -> {
                    // 초기 이벤트
                    ServerSentEvent<AccountCreationResponse> startEvent = ServerSentEvent
                            .<AccountCreationResponse>builder()
                            .event("STARTED")
                            .data(initialResponse)
                            .build();

                    // 상태 체크 스트림
                    Flux<ServerSentEvent<AccountCreationResponse>> statusStream = Flux.interval(Duration.ofSeconds(1))
                            .take(60) // 최대 60초
                            .flatMap(i -> accountService.checkAccountStatus(createdAccount.getId()))
                            .map(status -> ServerSentEvent.<AccountCreationResponse>builder()
                                    .event(status.getStatus())
                                    .data(status)
                                    .build())
                            .takeUntil(sse -> "SYNCED".equals(sse.data().getStatus()) ||
                                    "FAILED".equals(sse.data().getStatus()));

                    return Flux.concat(
                            Flux.just(startEvent),
                            statusStream);
                })
                .doOnNext(sse -> log.debug("Sending SSE: {}", sse.data()))
                .doOnComplete(() -> log.info("SSE stream completed"))
                .doOnError(error -> log.error("Error in SSE stream: {}", error.getMessage()));
    }

    @DeleteMapping("/sfid/{sfid}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> deleteAccount(@PathVariable String sfid) {
        return accountService.deleteAccount(sfid);
    }

    @GetMapping("/sfid/{sfid}/state")
    public Mono<AccountState> getState(@PathVariable String sfid) {
        return accountService.getState(sfid);
    }
}
