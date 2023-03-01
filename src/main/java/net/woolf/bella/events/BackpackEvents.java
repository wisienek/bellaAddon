package net.woolf.bella.events;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import Types.BackpackNBTKeys;
import classes.Backpack;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;
import net.woolf.bella.utils.InventoryUtils;

public class BackpackEvents implements Listener {

  public static List<String> bannedModsForBackpacks = Arrays.asList("bewitchment", "heroicarmory", "ebwizardry");

  private final Logger logger = Main.getInstance().logger;
  private final Boolean isTest = Main.getInstance().isTest();

  public BackpackEvents () {
  }

  @EventHandler
  public void onClose (
      InventoryCloseEvent event
  ) {
    if ( event.getInventory() != null && event.getInventory()
                                              .getHolder() instanceof Backpack && event.getPlayer() instanceof Player ) {
      Backpack backpack = (Backpack) event.getInventory().getHolder();
      Player closer = (Player) event.getPlayer();

      backpack.close(closer);

      if ( this.isTest ) this.logger.info(String.format("Saved and closed backpack %s", backpack.getBagID()));
    }
  }

  @EventHandler( priority = EventPriority.HIGHEST, ignoreCancelled = true )
  public void onInventoryClick (
      InventoryClickEvent event
  ) {
    if ( event.getInventory() != null && event.getInventory()
                                              .getHolder() instanceof Backpack && event.getWhoClicked() instanceof Player ) {
      Backpack backpack = (Backpack) event.getInventory().getHolder();
      ItemStack bagItem = backpack.getBagItem();

      ItemStack ci = event.getCurrentItem();
      if ( ci == null || ci.getType() == Material.AIR ) return;

      NBTItem ciNBTI = new NBTItem(ci);

      if ( ciNBTI.hasKey(BackpackNBTKeys.ISBACKPACK.toString()) && ciNBTI.getBoolean(
          BackpackNBTKeys.ISBACKPACK.toString()) ) {
        event.setCancelled(true);
        event.getWhoClicked().sendMessage(Main.prefixError + "Nie można przenosić plecaków w otwartym ekwipunku!");
        return;
      }
      String material = ci.getType().name();
      String modOrigin = material.split(":")[0];

      if ( BackpackEvents.bannedModsForBackpacks.contains(modOrigin) ) {
        event.setCancelled(true);
        event.getWhoClicked()
             .sendMessage(
                 Main.prefixError + modOrigin + " Znajduje się na liście potencjalnie znikających przedmiotów, " +
                     "zatrzymano event.");

        return;
      }

      if ( bagItem == null ) {
        this.logger.info("NonNullish bag item!");

        event.setCancelled(true);
      }

      backpack.setChanged();
    }
  }

  @EventHandler( priority = EventPriority.MONITOR )
  public void onPlayerLeaveEvent (
      PlayerQuitEvent event
  ) {
    String bagUUID = InventoryUtils.getOpenedBackpack(event.getPlayer());

    if ( bagUUID == null ) return;

    Backpack backpack = Backpack.getCachedInfo(bagUUID);
    if ( backpack != null ) backpack.close(event.getPlayer());
  }

}
