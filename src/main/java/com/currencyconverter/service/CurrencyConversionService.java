package com.currencyconverter.service;

import com.currencyconverter.model.ConversionResult;
import com.currencyconverter.model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyConversionService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyConversionService.class);
    private static final int DECIMAL_PLACES = 4;
    
    private final ExchangeRateService exchangeRateService;
    
    public CurrencyConversionService() {
        this.exchangeRateService = new ExchangeRateService();
    }
    
    public ConversionResult convertCurrency(String fromCurrency, String toCurrency, BigDecimal amount) 
            throws IOException, IllegalArgumentException {
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        
        Currency.fromCode(fromCurrency);
        Currency.fromCode(toCurrency);
        
        logger.info("Conversion: {} {} vers {}", amount, fromCurrency, toCurrency);
        
        BigDecimal exchangeRate = exchangeRateService.getExchangeRate(fromCurrency, toCurrency);
        BigDecimal convertedAmount = amount.multiply(exchangeRate).setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        
        logger.info("Taux de change: 1 {} = {} {}", fromCurrency, exchangeRate, toCurrency);
        logger.info("Résultat: {} {} = {} {}", amount, fromCurrency, convertedAmount, toCurrency);
        
        return new ConversionResult(fromCurrency, toCurrency, amount, convertedAmount, exchangeRate);
    }
    
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) throws IOException {
        return exchangeRateService.getExchangeRate(fromCurrency, toCurrency);
    }
    
    public void clearCache() {
        exchangeRateService.clearCache();
    }
}