package net.bella.bridge.api;

import java.util.List;
import java.util.UUID;

public interface IPermissionBridge {

    default boolean hasPermission(UUID playerUuid, String permission) {
        throw new UnsupportedOperationException("Permission bridge method hasPermission is not implemented.");
    }

    default String getPrimaryGroup(UUID playerUuid) {
        throw new UnsupportedOperationException("Permission bridge method getPrimaryGroup is not implemented.");
    }

    default List<String> getGroups(UUID playerUuid) {
        throw new UnsupportedOperationException("Permission bridge method getGroups is not implemented.");
    }
}
