package com.currencyconverter.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.currencyconverter.model.ConversionResult;
import com.currencyconverter.service.CurrencyConversionService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/api/convert")
public class CurrencyConverterServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyConverterServlet.class);
    
    private CurrencyConversionService conversionService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.conversionService = new CurrencyConversionService();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        logger.info("CurrencyConverterServlet initialisé");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        setResponseHeaders(response);
        
        String fromCurrency = request.getParameter("from");
        String toCurrency = request.getParameter("to");
        String amountStr = request.getParameter("amount");
        
        if (fromCurrency == null || toCurrency == null || amountStr == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                            "Paramètres manquants: from, to, amount requis");
            return;
        }
        
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            ConversionResult result = conversionService.convertCurrency(fromCurrency, toCurrency, amount);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));
            
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                            "Format de montant invalide: " + amountStr);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            logger.error("Erreur lors de la récupération des taux de change", e);
            sendErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, 
                            "Service de taux de change temporairement indisponible");
        } catch (Exception e) {
            logger.error("Erreur interne", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                            "Erreur interne du serveur");
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setResponseHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    private void setResponseHeaders(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
    
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        response.setStatus(statusCode);
        ErrorResponse errorResponse = new ErrorResponse(statusCode, message);
        response.getWriter().write(gson.toJson(errorResponse));
    }
    
    private static class ErrorResponse {
        private final int status;
        private final String message;
        private final long timestamp;
        
        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}