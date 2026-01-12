# Installation

1. Download the latest release JAR (shadow) from GitHub Releases.
2. Place the JAR in your server `plugins` folder.
3. Start the server once to generate configuration files in `plugins/BellaAddon/`.
4. Configure:
   - `config.yml` (OTP/ATP behavior)
   - `tpInfo.yml`, `tpLevels.yml`
   - `money.yml`, `emoji.yml`, `playerConfig.yml`
   - `database.yml` (MySQL connection)
   - `pwd.txt` (Discord bot token, one line)
5. Restart the server and verify the log contains no DB or config errors.

Upgrade
-------
1. Backup your config files and database.
2. Replace the old JAR with the new release.
3. Restart the server.
