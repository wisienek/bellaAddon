# config.yml

Defaults are created on first run. Key options:

- `OTP-command-delay` / `setOTP-command-delay` (boolean): enforce cooldown checks.
- `OTP-time-delay` / `setOTP-time-delay` (seconds): cooldown duration.
- `show-setOTP-message` (boolean) and `setOTP-message` (string): feedback after OTP set.
- `tp-level-<n>-cld` (seconds): cooldown per teleport level.
- `tp-level-<n>-radius`: max teleport radius for level.
- `tp-level-<n>-maxp`: max saved points.
- `tp-level-<n>-maxpoints`: score threshold per level.
- `tp-level-<n>-setmaxuse`: max uses of set command per level.

Tips:
- Keep cooldowns > 0 to avoid spam.
- Level keys are generated for 0–5 by default; you can extend the range manually.
