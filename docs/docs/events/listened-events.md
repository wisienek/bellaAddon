# Listened Bukkit Events

Main listeners:
- `BellaEvents` — chat, commands, interactions, item damage, join/quit, move, toggle flight, interact entity.
- `BackpackEvents` — inventory close/click for backpacks, quit handling.
- `ArmorListener` / `ArmourEquipEventListener` — armor equip/unequip safety.

Purposes:
- Teleport chat flow and message caching (`AsyncPlayerChatEvent`, `PlayerCommandPreprocessEvent`).
- Teleport item use (`PlayerInteractEvent`, `PlayerInteractEntityEvent`).
- Backpack save/validation (`InventoryCloseEvent`, `InventoryClickEvent`, `PlayerQuitEvent`).
- Movement/flight gating for teleport effects (`PlayerMoveEvent`, `PlayerToggleFlightEvent`).
- Item durability handling (`PlayerItemDamageEvent`).
