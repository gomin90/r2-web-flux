package com.gomin.r2webflux.config;

import java.util.EnumSet;
import com.gomin.r2webflux.state.AccountState;
import com.gomin.r2webflux.event.AccountEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<AccountState, AccountEvent> {
    
    @Override
    public void configure(StateMachineStateConfigurer<AccountState, AccountEvent> states) throws Exception {
        states.withStates()
                .initial(AccountState.CREATED)
                .states(EnumSet.allOf(AccountState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AccountState, AccountEvent> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(AccountState.CREATED).target(AccountState.ACTIVE)
                    .event(AccountEvent.ACTIVATE)
                    .and()
                .withExternal()
                    .source(AccountState.ACTIVE).target(AccountState.SUSPENDED)
                    .event(AccountEvent.SUSPEND)
                    .and()
                .withExternal()
                    .source(AccountState.ACTIVE).target(AccountState.CLOSED)
                    .event(AccountEvent.CLOSE);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<AccountState, AccountEvent> config) throws Exception {
        config.withConfiguration()
                .autoStartup(true);
    }
}
