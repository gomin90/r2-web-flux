package com.gomin.r2webflux.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Duration;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.gomin.r2webflux.repository")
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String url;

    // @Value("${spring.r2dbc.username}")
    // private String username;

    // @Value("${spring.r2dbc.password}")
    // private String password;

    @Value("${spring.r2dbc.pool.initial-size}")
    private int initialSize;

    @Value("${spring.r2dbc.pool.max-size}")
    private int maxSize;

    @Value("${spring.r2dbc.pool.max-idle-time}")
    private Duration maxIdleTime;

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {

        ConnectionFactory factory = ConnectionFactoryBuilder.withUrl(
            url)
                // .username(username)
                // .password(password)
                .build();

        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder()
                .connectionFactory(factory)
                .initialSize(initialSize)
                .maxSize(maxSize)
                .maxIdleTime(maxIdleTime)
                .build();

        return new ConnectionPool(poolConfig);
    }

    @Bean
    ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
