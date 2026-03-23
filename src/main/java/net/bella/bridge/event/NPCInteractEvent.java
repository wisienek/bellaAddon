package net.bella.bridge.event;

import java.util.Map;
import java.util.UUID;

public class NPCInteractEvent {

    private final UUID playerUuid;
    private final String npcId;
    private final String npcType;
    private final Map<String, Object> metadata;

    public NPCInteractEvent(UUID playerUuid, String npcId, String npcType, Map<String, Object> metadata) {
        this.playerUuid = playerUuid;
        this.npcId = npcId;
        this.npcType = npcType;
        this.metadata = metadata;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getNpcId() {
        return npcId;
    }

    public String getNpcType() {
        return npcType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
