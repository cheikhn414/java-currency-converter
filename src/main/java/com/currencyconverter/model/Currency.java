package com.currencyconverter.model;

public enum Currency {
    USD("USD", "US Dollar", "$"),
    EUR("EUR", "Euro", "€"),
    GBP("GBP", "British Pound", "£"),
    JPY("JPY", "Japanese Yen", "¥"),
    INR("INR", "Indian Rupee", "₹"),
    CAD("CAD", "Canadian Dollar", "C$"),
    AUD("AUD", "Australian Dollar", "A$"),
    CHF("CHF", "Swiss Franc", "Fr"),
    CNY("CNY", "Chinese Yuan", "¥"),
    SEK("SEK", "Swedish Krona", "kr"),
    NOK("NOK", "Norwegian Krone", "kr"),
    MXN("MXN", "Mexican Peso", "$"),
    SGD("SGD", "Singapore Dollar", "S$"),
    HKD("HKD", "Hong Kong Dollar", "HK$"),
    NZD("NZD", "New Zealand Dollar", "NZ$"),
    XOF("XOF", "Francs CFA", "CFA");

    private final String code;
    private final String name;
    private final String symbol;

    Currency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Currency fromCode(String code) {
        for (Currency currency : values()) {
            if (currency.code.equalsIgnoreCase(code)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Devise non supportée: " + code);
    }
}