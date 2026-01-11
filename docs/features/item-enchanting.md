# Item Enchanting

Command: `/zaczaruj <efekt/lista>` and `/przebadaj`.

Flow:
- `ItemEnchanter` registers both commands.
- Available effects are keys from `ItemEffects.stringToEnum`.
- Enchant writes NBT booleans on the held item; hides attributes and makes it unbreakable.
- Item-based teleport uses stored NBT payload (`teleportEnchantment` compound) read by `TeleportUtils.itemTP`.

Permissions:
- `bella.enchanter` to enchant.
- `bella.enchanter.check` to inspect.

Usage tips:
- Use `/zaczaruj lista` to list available effects.
- Items must be in main hand.
- Teleport items enforce cooldowns and max uses stored in NBT.
