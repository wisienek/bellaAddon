# Extending BellaAddon

BellaAddon is a Spigot/Magma plugin with clear entry points for new commands, events, and Discord integrations. This guide shows how to add features safely.

## Prerequisites

- Java 17
- Gradle wrapper (included)
- Minecraft 1.20.1 server for testing
- MySQL access for data-backed features

## Build and run locally

```bash
./gradlew clean shadowJar
# Output: build/libs/belladdon-<version>.jar
```

Copy the shaded JAR into your server `plugins/` folder and restart.

## Adding a Bukkit command

1. Create a command class under `net.woolf.bella.commands`.
2. Register the executor in `Main#onEnable`.
3. Add command metadata to `src/main/resources/plugin.yml`.

```java
// Example skeleton
public class HelloCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Hello from BellaAddon!");
        return true;
    }
}
```

## Adding an event listener

1. Create a listener under `net.woolf.bella.events`.
2. Annotate methods with `@EventHandler`.
3. Register the listener in `Main#onEnable`.

```java
public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // TODO: add your feature
    }
}
```

## Working with the database

- Use repositories in `net.woolf.bella.repositories` as a reference.
- Keep queries parameterized and reuse the existing connection helpers.
- Add integration tests where possible (see `development/testing.md`).

## Configuration

- Add new config keys to the relevant YAML in the plugin data folder.
- Document new keys in `configuration/config-files.md` and related pages.
- Provide sensible defaults to avoid startup errors.

## Discord bot features

- Commands live under `net.woolf.bella.bot`.
- Reuse existing permission and rate-limiting helpers.
- Keep secrets in environment variables or external config (never in code).

## Pull requests

- Update JavaDoc for public APIs.
- Add or update tests.
- Include documentation updates in the same PR.
- Follow the Gradle shadow build; ensure the JAR still runs on Java 17.
