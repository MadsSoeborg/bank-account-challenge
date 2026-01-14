package com.bank.service;

import com.bank.event.TransactionEvents.*;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetricsListener {

    @Inject
    MeterRegistry registry;

    public void onDeposit(@Observes DepositEvent event) {
        registry.counter("bank.deposits.count").increment();
        registry.summary("bank.deposits.amount").record(event.amount().doubleValue());
    }

    public void onWithdraw(@Observes WithdrawEvent event) {
        registry.counter("bank.withdrawals.count").increment();
    }
}