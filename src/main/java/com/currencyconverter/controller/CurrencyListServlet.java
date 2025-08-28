package com.currencyconverter.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.currencyconverter.model.Currency;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/currencies")
public class CurrencyListServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyListServlet.class);
    
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.gson = new Gson();
        logger.info("CurrencyListServlet initialisé");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        try {
            List<CurrencyInfo> currencies = Arrays.stream(Currency.values())
                    .map(c -> new CurrencyInfo(c.getCode(), c.getName(), c.getSymbol()))
                    .collect(Collectors.toList());
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(currencies));
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la liste des devises", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new ErrorResponse("Erreur interne du serveur")));
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    private static class CurrencyInfo {
        private final String code;
        private final String name;
        private final String symbol;
        
        public CurrencyInfo(String code, String name, String symbol) {
            this.code = code;
            this.name = name;
            this.symbol = symbol;
        }
        
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getSymbol() { return symbol; }
    }
    
    private static class ErrorResponse {
        private final String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
    }
}