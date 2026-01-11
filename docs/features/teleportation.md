# Teleportation (OTP/ATP)

Components:
- `TeleportUtils` — OTP storage, teleport execution, effects.
- Config files: `tpInfo.yml`, `tpLevels.yml`, and `config.yml` for defaults.
- Commands: `/otp` (user), `/atp` (admin).
- Cooldowns handled by `CooldownUtils`; effects by `EffectUtils`.

Behavior:
- OTP points saved per player under `tps.<uuid>.<name>`.
- Levels (0–5 default) adjust cooldown and radius; type controls visual effect.
- Item-based teleport: enchanted item contains coordinates and cooldown, enforced in `itemTP`.

Admin tips:
- Adjust `tp-level-*` keys for balance.
- Keep cooldowns > 0 to avoid spam/lag.
- Ensure worlds referenced in OTP entries exist before teleporting.
