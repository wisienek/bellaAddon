# Backpack System

- Backpack items are stored in MySQL (`BackpackRepository`).
- Backpacks use NBT flag `BackpackNBTKeys.ISBACKPACK` and persist inventory contents.
- Safety: cannot move backpacks while open; bans items from risky mods to prevent loss.
- Events handled in `BackpackEvents` to save on close and on player quit.

Usage
-----
- Use `/plecak` to open your backpack.
- Backpacks are cached; DB persists contents on close.
