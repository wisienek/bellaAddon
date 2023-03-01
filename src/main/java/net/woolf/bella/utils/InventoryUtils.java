package net.woolf.bella.utils;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import de.tr7zw.nbtapi.NBTEntity;

public class InventoryUtils {

  public static void dropInventory (
      final @NotNull Inventory inv, final @NotNull Location location
  ) {
    ItemStack[] items = inv.getContents();

    for ( ItemStack item : items ) {
      location.getWorld().dropItemNaturally(location, item);
      inv.removeItem(item);
    }
  }

  public static String getOpenedBackpack (
      @NotNull OfflinePlayer player
  ) {
    NBTEntity nbti = new NBTEntity((Entity) player);

    if ( nbti.hasKey("backpack-opened") ) return nbti.getString("backpack-opened");

    return null;
  }

}
