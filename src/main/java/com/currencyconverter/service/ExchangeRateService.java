package com.currencyconverter.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.currencyconverter.model.ExchangeRateResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExchangeRateService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final String PRIMARY_API_URL = "https://api.fxratesapi.com/latest?base=";
    private static final String FALLBACK_API_URL = "https://api.exchangerate-api.com/v4/latest/";
    private static final int CACHE_DURATION_MINUTES = 30;
    
    private final Gson gson;
    private final Map<String, CacheEntry> rateCache;
    
    public ExchangeRateService() {
        this.gson = new GsonBuilder().create();
        this.rateCache = new ConcurrentHashMap<>();
    }
    
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) throws IOException {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        ExchangeRateResponse rates = getExchangeRates(fromCurrency);
        BigDecimal rate = rates.getRates().get(toCurrency);
        
        if (rate == null) {
            throw new IllegalArgumentException("Taux de change non disponible pour: " + toCurrency);
        }
        
        return rate;
    }
    
    public ExchangeRateResponse getExchangeRates(String baseCurrency) throws IOException {
        CacheEntry cacheEntry = rateCache.get(baseCurrency);
        
        if (cacheEntry != null && !isCacheExpired(cacheEntry.getTimestamp())) {
            logger.info("Utilisation du cache pour la devise: {}", baseCurrency);
            return cacheEntry.getRates();
        }
        
        logger.info("Récupération des taux de change depuis l'API pour: {}", baseCurrency);
        
        // Essayer d'abord l'API principale
        try {
            ExchangeRateResponse rates = fetchFromApi(PRIMARY_API_URL + baseCurrency);
            if (rates != null) {
                rateCache.put(baseCurrency, new CacheEntry(rates, LocalDateTime.now()));
                return rates;
            }
        } catch (Exception e) {
            logger.warn("Échec de l'API principale: {}", e.getMessage());
        }
        
        // Essayer l'API de fallback
        try {
            ExchangeRateResponse rates = fetchFromApi(FALLBACK_API_URL + baseCurrency);
            if (rates != null) {
                rateCache.put(baseCurrency, new CacheEntry(rates, LocalDateTime.now()));
                return rates;
            }
        } catch (Exception e) {
            logger.warn("Échec de l'API de fallback: {}", e.getMessage());
        }
        
        // Si aucune API ne fonctionne, utiliser des taux de change fixes pour le développement
        logger.warn("Utilisation des taux de change fixes (mode dégradé)");
        return getFallbackExchangeRates(baseCurrency);
    }
    
    private ExchangeRateResponse fetchFromApi(String apiUrl) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            request.setHeader("Accept", "application/json");
            request.setHeader("User-Agent", "CurrencyConverter/1.0");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("Erreur API: " + response.getStatusLine().getStatusCode() + " - " + responseBody);
                }
                
                ExchangeRateResponse exchangeRates = gson.fromJson(responseBody, ExchangeRateResponse.class);
                
                // Certaines APIs n'utilisent pas le champ "success"
                if (exchangeRates.getRates() == null || exchangeRates.getRates().isEmpty()) {
                    throw new IOException("Réponse API invalide: aucun taux de change trouvé");
                }
                
                // Marquer comme succès si les taux sont présents
                exchangeRates.setSuccess(true);
                if (exchangeRates.getBase() == null) {
                    exchangeRates.setBase(apiUrl.contains("base=") ? 
                        apiUrl.substring(apiUrl.indexOf("base=") + 5) : "USD");
                }
                
                return exchangeRates;
            }
        }
    }
    
    private ExchangeRateResponse getFallbackExchangeRates(String baseCurrency) {
        ExchangeRateResponse fallbackResponse = new ExchangeRateResponse();
        fallbackResponse.setSuccess(true);
        fallbackResponse.setBase(baseCurrency);
        fallbackResponse.setTimestamp(System.currentTimeMillis() / 1000);
        
        // Taux de change fixes approximatifs pour le développement
        Map<String, BigDecimal> rates = new ConcurrentHashMap<>();
        
        switch (baseCurrency.toUpperCase()) {
            case "USD":
                rates.put("EUR", new BigDecimal("0.85"));
                rates.put("GBP", new BigDecimal("0.73"));
                rates.put("JPY", new BigDecimal("110.0"));
                rates.put("CAD", new BigDecimal("1.25"));
                rates.put("AUD", new BigDecimal("1.35"));
                rates.put("CHF", new BigDecimal("0.92"));
                rates.put("CNY", new BigDecimal("6.45"));
                rates.put("INR", new BigDecimal("74.50"));
                rates.put("SEK", new BigDecimal("8.60"));
                rates.put("NOK", new BigDecimal("8.80"));
                rates.put("MXN", new BigDecimal("20.0"));
                rates.put("SGD", new BigDecimal("1.35"));
                rates.put("HKD", new BigDecimal("7.80"));
                rates.put("NZD", new BigDecimal("1.40"));
                break;
            case "EUR":
                rates.put("USD", new BigDecimal("1.18"));
                rates.put("GBP", new BigDecimal("0.86"));
                rates.put("JPY", new BigDecimal("129.0"));
                break;
            default:
                // Pour les autres devises, calculer par rapport à USD
                rates.put("USD", BigDecimal.ONE);
                rates.put("EUR", new BigDecimal("0.85"));
        }
        
        fallbackResponse.setRates(rates);
        return fallbackResponse;
    }
    
    private boolean isCacheExpired(LocalDateTime cacheTime) {
        return ChronoUnit.MINUTES.between(cacheTime, LocalDateTime.now()) > CACHE_DURATION_MINUTES;
    }
    
    public void clearCache() {
        rateCache.clear();
        logger.info("Cache des taux de change vidé");
    }
    
    private static class CacheEntry {
        private final ExchangeRateResponse rates;
        private final LocalDateTime timestamp;
        
        public CacheEntry(ExchangeRateResponse rates, LocalDateTime timestamp) {
            this.rates = rates;
            this.timestamp = timestamp;
        }
        
        public ExchangeRateResponse getRates() {
            return rates;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}