package net.bella.bridge;

import java.util.concurrent.atomic.AtomicReference;

import net.bella.bridge.api.IEconomyBridge;
import net.bella.bridge.api.IPermissionBridge;

public final class BridgeRegistry {

    private static final AtomicReference<IEconomyBridge> economy = new AtomicReference<>();
    private static final AtomicReference<IPermissionBridge> permissions = new AtomicReference<>();

    private BridgeRegistry() {
    }

    public static void register(IEconomyBridge impl) {
        if (impl == null) {
            throw new IllegalArgumentException("IEconomyBridge implementation cannot be null");
        }
        economy.set(impl);
    }

    public static void register(IPermissionBridge impl) {
        if (impl == null) {
            throw new IllegalArgumentException("IPermissionBridge implementation cannot be null");
        }
        permissions.set(impl);
    }

    public static IEconomyBridge getEconomy() {
        IEconomyBridge impl = economy.get();
        if (impl == null) {
            throw new IllegalStateException("Economy bridge not registered. Is the plugin enabled?");
        }
        return impl;
    }

    public static IPermissionBridge getPermissions() {
        IPermissionBridge impl = permissions.get();
        if (impl == null) {
            throw new IllegalStateException("Permission bridge not registered. Is the plugin enabled?");
        }
        return impl;
    }

    public static boolean isReady() {
        return economy.get() != null && permissions.get() != null;
    }
}
