# Testing

- Build check: `./gradlew build shadowJar`
- JavaDoc check: `./gradlew javadoc`
- Smoke test on a local Spigot/Magma 1.20.1 server:
  - Verify commands register (`/help bellaaddon`).
  - Create OTP point, teleport, and ensure cooldown messages appear.
  - Create/open backpack, add/remove items, relog to confirm persistence.
  - Use `/wymien`, `/portfel`, `/bank` near a bank location.
- For Discord bot, use a test guild/token and verify slash commands respond.
- Watch server logs for DB connectivity and permission issues.
