# Project Structure

- `src/main/java/net/woolf/bella` — core plugin code
  - `commands` — Bukkit command executors
  - `events` — listeners and `ArmorEquipEvent`
  - `utils` — utilities (teleport, money, cooldowns, effects, config, DB)
  - `bot` — Discord bot (JDA) and commands
  - `repositories` — DB access for backpacks and account links
  - `models` — data holders
  - `types` — enums/constants
- `src/main/resources/plugin.yml` — Bukkit plugin descriptor and command map
- `build.gradle` — Gradle build and shading
- `docs/` — Docusaurus site (configuration, build, deployment)
- `docs/docs/` — documentation pages
- `.github/workflows/` — CI/CD pipelines
