package net.woolf.bella.events;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import net.woolf.bella.Main;
import net.woolf.bella.commands.ItemEnchanter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

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


  private void handleEquippedEvent (Player player, ItemStack item) { }

  private void handleUnEquippedEvent (Player player, ItemStack item) { }
}
