# API Overview

Entry point:
- `Main.getInstance()` returns the running plugin.

Utilities (constructed in `Main`):
- `TeleportUtils` — manage OTP points, teleport effects, item-based teleports.
- `MoneyUtils` — balances, transfers, conversions, nearby bank checks.
- `PlayerUtils`, `EffectUtils`, `CooldownUtils`, `ConfigManager` — helper utilities for chat, effects, cooldowns, and configs.

Data access:
- `BackpackRepository` — load/save backpacks via MySQL.
- `AccountLinkRepository` — Discord ↔ player linking.
- `DbUtils.getInstance()` — singleton holding JDBC connection and repositories.

Events:
- Custom: `ArmorEquipEvent` (cancellable).
- Listeners: see [Events](../events/listened-events.md).

Usage example (inside another plugin):
```java
Main bella = Main.getInstance();
if (bella != null) {
    bella.mutils.setMoney(player.getUniqueId().toString(), "miedziak", 100L);
}
```

Threading:
- Database calls in repositories use JDBC; avoid heavy work on the main thread in your extensions.
