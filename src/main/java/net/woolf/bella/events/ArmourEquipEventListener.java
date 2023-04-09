package net.woolf.bella.events;

import Types.ItemEffects;
import com.codingforcookies.armorequip.ArmorEquipEvent;
import net.woolf.bella.Main;
import net.woolf.bella.commands.ItemEnchanter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.logging.Logger;

public class ArmourEquipEventListener implements Listener {
  private final Main plugin = Main.getInstance();
  private final Logger logger = plugin.getLogger();

  public ArmourEquipEventListener () { }

  @EventHandler
  public void onArmourEquipEvent (ArmorEquipEvent event) {
    Player player = event.getPlayer();

    ItemStack newItem = event.getNewArmorPiece();
    ItemStack oldItem = event.getOldArmorPiece();

    boolean equipped = newItem != null;
    boolean isEnchanted = ItemEnchanter.isEnchanted(equipped ? newItem : oldItem);

    if ( !isEnchanted ) return;

    // handle each event
    if ( equipped ) this.handleEquippedEvent(player, newItem);
    else this.handleUnEquippedEvent(player, oldItem);
  }


  private void handleEquippedEvent (Player player, ItemStack item) {
    List<String> enchantments = ItemEnchanter.getItemEffects(item);

    for ( String enchant : enchantments ) {
      ItemEffects effect = ItemEffects.fromString(enchant);

      switch ( effect ) {
        case TEST:
          this.logger.info(String.format("%s test enchantment", player.getName()));
          break;
        case FLIGHT:
          player.setFlying(true);
          break;
        case INVISIBILITY:
          for ( Player online : this.plugin.server.getOnlinePlayers() ) {
            if ( online.getGameMode() == GameMode.SURVIVAL ) {
              online.hidePlayer(this.plugin, player);
            }
          }
          break;
        case SPEED:
          player.addPotionEffect(( new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, true, false) ));
          break;
        case GLOW:
          player.addPotionEffect(( new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 4, true, false) ));
          break;
        default:
          this.logger.info(String.format("%s put on item with unknown effect: %s", player.getName(), effect));
          break;
      }
    }
  }

  private void handleUnEquippedEvent (Player player, ItemStack item) {
    List<String> enchantments = ItemEnchanter.getItemEffects(item);

    for ( String enchant : enchantments ) {
      ItemEffects effect = ItemEffects.fromString(enchant);

      switch ( effect ) {
        case TEST:
          this.logger.info(String.format("%s test enchantment", player.getName()));
          break;
        case FLIGHT:
          player.setFlying(false);
          break;
        case INVISIBILITY:
          for ( Player online : this.plugin.server.getOnlinePlayers() ) {
            online.showPlayer(this.plugin, player);
          }
          break;
        case SPEED:
          player.removePotionEffect(PotionEffectType.SPEED);
          break;
        case GLOW:
          player.removePotionEffect(PotionEffectType.GLOWING);
          break;
        default:
          this.logger.info(String.format("%s put off item with unknown effect: %s", player.getName(), effect));
          break;
      }
    }
  }
}
