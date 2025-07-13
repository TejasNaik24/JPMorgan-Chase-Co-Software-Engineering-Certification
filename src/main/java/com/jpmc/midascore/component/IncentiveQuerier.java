package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncentiveQuerier {
    private final RestTemplate restTemplate;
    private final String incentiveApiUrl;

    public IncentiveQuerier(RestTemplateBuilder builder, @Value("${general.incentive-api-url}") String incentiveApiUrl) {
        this.restTemplate = builder.build();
        this.incentiveApiUrl = incentiveApiUrl;
    }

    public Incentive query(Transaction transaction) {
        return restTemplate.postForObject(incentiveApiUrl, transaction, Incentive.class);
    }
}