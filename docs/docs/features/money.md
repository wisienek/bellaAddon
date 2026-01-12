# Money & Bank

Components:
- `MoneyUtils` — balances, bank proximity, transfers, conversions.
- Config: `money.yml` stores banks list, personal/bank balances, conversions.
- Commands: `/portfel` (wallet), `/bank` (bank), `/wymien` (exchange).
- Discord admin commands mirror in-game operations.

Banks:
- Stored as world/XYZ in `money.yml` under `banks`.
- `isNearBank` checks 20-block radius; required for some actions.

Currency:
- Types: `miedziak`, `srebrnik`, `złotnik`.
- Conversions stored under `conversion.<from>.<to>`; use `/wymien`.

Admin tips:
- Set sensible conversion rates and keep them symmetric (from/to, to/from).
- Use `/bank` admin subcommands for audits; require proximity to a bank.
