package net.bella.bridge.api;

import java.util.UUID;

public interface IEconomyBridge {

    double getBalance(UUID playerUuid, String currencyType);

    boolean withdraw(UUID playerUuid, String currencyType, double amount);

    boolean deposit(UUID playerUuid, String currencyType, double amount);

    boolean has(UUID playerUuid, String currencyType, double amount);

    double getConversion(String fromCurrency, String toCurrency);

    String getCurrencyName(String currencyType);
}
