package com.currencyconverter.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConversionResult {
    private final String fromCurrency;
    private final String toCurrency;
    private final BigDecimal amount;
    private final BigDecimal convertedAmount;
    private final BigDecimal exchangeRate;
    private final LocalDateTime timestamp;

    public ConversionResult(String fromCurrency, String toCurrency, BigDecimal amount, 
                           BigDecimal convertedAmount, BigDecimal exchangeRate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.exchangeRate = exchangeRate;
        this.timestamp = LocalDateTime.now();
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}