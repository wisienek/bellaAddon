package net.bella.bridge.api;

import java.util.UUID;

public interface IEconomyBridge {

    default double getBalance(UUID playerUuid, String currencyType) {
        throw new UnsupportedOperationException("Economy bridge method getBalance is not implemented.");
    }

    default boolean withdraw(UUID playerUuid, String currencyType, double amount) {
        throw new UnsupportedOperationException("Economy bridge method withdraw is not implemented.");
    }

    default boolean deposit(UUID playerUuid, String currencyType, double amount) {
        throw new UnsupportedOperationException("Economy bridge method deposit is not implemented.");
    }

    default boolean has(UUID playerUuid, String currencyType, double amount) {
        throw new UnsupportedOperationException("Economy bridge method has is not implemented.");
    }

    default double getConversion(String fromCurrency, String toCurrency) {
        throw new UnsupportedOperationException("Economy conversion bridge is not implemented.");
    }

    default String getCurrencyName(String currencyType) {
        throw new UnsupportedOperationException("Economy bridge method getCurrencyName is not implemented.");
    }
}
