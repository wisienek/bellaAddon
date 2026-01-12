# Teleport Configs

`tpInfo.yml`
- Stores player OTP points under `tps.<uuid>.<name>` with world, coordinates, yaw/pitch.
- Managed by commands; edit with care.

`tpLevels.yml`
- Stores per-player teleport level and type:
  - `<uuid>.level` — string level, default `"0"`.
  - `<uuid>.type` — effect type name (see `EffectType`).
- System also seeds defaults for level cooldowns/radius in `config.yml`.

Operational guidance:
- Avoid manual edits unless the server is stopped.
- Higher levels reduce cooldown and expand allowed radius.
