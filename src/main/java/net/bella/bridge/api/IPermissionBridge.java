package net.bella.bridge.api;

import java.util.List;
import java.util.UUID;

public interface IPermissionBridge {

    boolean hasPermission(UUID playerUuid, String permission);

    String getPrimaryGroup(UUID playerUuid);

    List<String> getGroups(UUID playerUuid);
}
