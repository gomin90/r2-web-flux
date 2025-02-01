package com.gomin.r2webflux.event;

import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;
import com.gomin.r2webflux.state.AccountState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountEventListener extends StateMachineListenerAdapter<AccountState, AccountEvent> {
    
    @Override
    public void stateChanged(State<AccountState, AccountEvent> from, State<AccountState, AccountEvent> to) {
        log.info("Account state changed from {} to {}", 
                from != null ? from.getId() : "NONE", 
                to != null ? to.getId() : "NONE");
    }
}
