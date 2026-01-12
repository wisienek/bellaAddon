BellaAddon
==========

Helper plugin for BelorisRP (Spigot/Magma 1.20.1). Provides teleportation, backpacks, money/bank, Discord bot integration, item enchanting, and QoL commands.

Requirements
------------
- Java 17
- Spigot/Magma 1.20.1 server
- MySQL database
- LuckPerms API (optional, recommended)
- Discord bot token (for bot features)

Quick Start
-----------
1) Download the latest release JAR (shadow) from GitHub Releases.  
2) Drop the JAR into the server `plugins` folder.  
3) Start the server to generate config files, then edit `config.yml`, `tpInfo.yml`, `tpLevels.yml`, `money.yml`, `emoji.yml`, `playerConfig.yml`, and `database.yml` (in the plugin data folder).  
4) Restart the server.

Building From Source
--------------------
```powershell
# from repo root
./gradlew.bat clean build shadowJar
# JAR will be in build/libs/belladdon-<version>.jar
```

Docs
----
Full documentation (setup, commands, API, config, features, JavaDoc): https://wisienek.github.io/bellaAddon/

To build docs locally (Docusaurus):
```bash
cd docs
npm install
npm run start
```

Contributing
------------
- Use Java 17, run `./gradlew build shadowJar`.  
- Keep public APIs documented and add tests where feasible.  
- Open PRs against `master`.
