# Discord Bot

Implemented with JDA in `Bot`:
- Reads token from `plugins/BellaAddon/pwd.txt` (one line).
- Registers slash commands: `/who`, `/link`, `/portfel`, `/bank`, `/narracja` with subcommands.
- Listens via `MessageListener`.

Setup
-----
1. Create a Discord bot, invite with needed intents (members, messages, reactions).
2. Save the token in `pwd.txt`.
3. Restart the server to log in the bot.

Usage
-----
- `/link code` — links MC account.
- `/who` — list online users (optionally staff).
- `/portfel` / `/bank` — admin wallet/bank operations.
- `/narracja` — send narration globally, locally (warp/user/range), or privately.

Notes
-----
- Presence can be changed via `Bot.updatePresence`.
- Logs can be sent to channel IDs via `Bot.sendLog`.
