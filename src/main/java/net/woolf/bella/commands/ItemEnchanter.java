package net.woolf.bella.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import de.tr7zw.nbtapi.NBTCompound;
import net.woolf.bella.bot.Bot;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import Types.Permissions;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;

public class ItemEnchanter implements CommandExecutor {

  public static final Set<String> AvailableEnchantEffects = new HashSet<>(
      Arrays.asList("test", "lot", "doublejump", "niewidzialnosc", "predkosc"));

  public ItemEnchanter (
      Main main
  ) {
    main.getCommand("zaczaruj").setExecutor(this);
    main.getCommand("przebadaj").setExecutor(this);
  }

  @Override
  public boolean onCommand (
      CommandSender sender, Command cmd, String label, String[] args
  ) {
    if ( !( sender instanceof Player ) ) {
      sender.sendMessage(Main.prefixError + "Tylko gracze!");
      return true;
    }

    Player player = (Player) sender;

    if ( label.equals("zaczaruj") ) {
      if ( !player.hasPermission(Permissions.ENCHANTER.toString()) ) {
        player.sendMessage(
            Main.prefixError + "Nie masz permissi " + ChatColor.DARK_RED + Permissions.ENCHANTER + ChatColor.GRAY +
                " aby użyć tej komendy");
        return true;
      }

      if ( args.length < 1 ) {
        player.sendMessage(Main.prefixError + this.getUsage());
        return true;
      }

      String action = args[0];

      switch ( action ) {
        case "efekt": {
          if ( args.length < 2 ) {
            player.sendMessage(Main.prefixError + "Musisz podać efekt aby zaczarować przedmiot w ręce!");
            return true;
          }

          String effect = args[1];

          if ( !AvailableEnchantEffects.contains(effect) ) {
            player.sendMessage(Main.prefixError + "Podany efekt nie widnieje na liście dostępnych!");
            player.sendMessage(Main.prefixInfo + ItemEnchanter.getEffects());
            return true;
          }

          ItemStack heldItem = player.getInventory().getItemInMainHand();

          if ( heldItem == null || !heldItem.getType().isItem() ) {
            player.sendMessage(Main.prefixError + "Musisz trzymać item aby go zaczarować!");
            return true;
          }

          List<String> enchants = this.checkEnchants(heldItem);

          if ( enchants != null && enchants.contains(effect) ) {
            player.sendMessage(
                Main.prefixError + "Przedmiot ma już dany efekt: " + ChatColor.GREEN + String.join(", ", enchants));
            return true;
          }

          player.sendMessage(Main.prefixInfo + "Czaruję przedmiot...");

          this.enchantItem(heldItem, effect);

          break;
        }

        case "lista": {
          player.sendMessage(Main.prefixInfo + ItemEnchanter.getEffects());
          return true;
        }

        default: {
          player.sendMessage(Main.prefixError + this.getUsage());
          return true;
        }
      }
    }
    else {
      if ( !player.hasPermission(Permissions.CHECK_ENCHANT.toString()) ) {
        player.sendMessage(
            Main.prefixError + "Nie masz permissi " + ChatColor.DARK_RED + Permissions.CHECK_ENCHANT + ChatColor.GRAY + " aby użyć tej komendy");
        return true;
      }

      ItemStack heldItem = player.getInventory().getItemInMainHand();

      if ( heldItem == null || !heldItem.getType().isItem() ) {
        player.sendMessage(Main.prefixError + "Musisz trzymać item aby go sprawdzić!");
        return true;
      }

      List<String> enchants = this.checkEnchants(heldItem);

      if ( enchants != null ) player.sendMessage(
          Main.prefixInfo + "Przedmiot jest zaczarowany na: " + ChatColor.GREEN + String.join(", ", enchants));
      else player.sendMessage(Main.prefixInfo + "Przedmiot nie jest zaczarowany!");

      return true;
    }

    return true;
  }

  private List<String> checkEnchants (
      @Nonnull ItemStack item
  ) {
    NBTItem nbti = new NBTItem(item);

    List<String> enchants = new ArrayList<>();

    for ( String enchant : ItemEnchanter.AvailableEnchantEffects )
      if ( nbti.hasKey(enchant) && nbti.getBoolean(enchant) ) enchants.add(enchant);

    return enchants.size() > 0 ? enchants : null;
  }

  private void enchantItem (
      @Nonnull ItemStack item, @Nonnull String effect
  ) {
    NBTItem nbti = new NBTItem(item);
    nbti.setBoolean(effect, true);

    nbti.applyNBT(item);

    ItemMeta meta = item.getItemMeta();
    meta.setUnbreakable(true);
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DESTROYS,
        ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);

    item.setItemMeta(meta);
  }

  public static String getEffects () {
    StringBuilder sb = new StringBuilder();

    sb.append(ChatColor.GRAY).append("Lista efektów możliwych do zaczarowania przedmiotu:");
    for ( String effect : ItemEnchanter.AvailableEnchantEffects )
      sb.append("\n" + ChatColor.DARK_PURPLE + "- " + ChatColor.BLUE).append(effect);

    return sb.toString();
  }

  public String getUsage () {
    return "Użycie komend: \n/zaczaruj <efekt/lista> [...efekt] \n/przebadaj";
  }

  public static void teleportPlayerWithItem (Player player, NBTItem nbti) {
    Location playerLoc = player.getLocation();

    NBTCompound comp = nbti.getCompound("teleportEnchantment");
    Main.getInstance().utils.itemTP(player, comp);

    double x = comp.getDouble("x");
    double y = comp.getDouble("y");
    double z = comp.getDouble("z");

    Main.getInstance().bot.sendLog(
        String.format("[%s] teleportował {%d %d %d} -> {%f %f %f} (item)", player.getName(), playerLoc.getBlockX(),
            playerLoc.getBlockY(), playerLoc.getBlockZ(), x, y, z), Bot.VariousLogId);
  }
}
