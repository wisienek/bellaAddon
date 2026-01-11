package net.woolf.bella.commands;

import Types.BotChannels;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.utils.MoneyUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.woolf.bella.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WymienCommand implements CommandExecutor {

  public static final String MoneyNbtTag = "console";
  private final Main plugin;

  public WymienCommand() {
    plugin = Main.getInstance();

    plugin.getCommand( "wymien" ).setExecutor( this );
  }

  public String getUsage() {
    return Main.prefixInfo + "Użycie komendy: /wymien <typ> <ilość>\n"
        + "Typ - miedziak | złotnik | srebrnik\n" + "Ilość - Liczba naturalna";
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command cmd,
      String alias,
      String[] args
  ) {
    if ( !( sender instanceof Player ) ) {
      sender.sendMessage( "Tylko dla graczy!" );
      return true;
    }

    Player player = (Player) sender;

    if ( args.length < 2 ) {
      sender.sendMessage( this.getUsage() );
      return true;
    }

    String type = args[0];
    int amount = Integer.parseInt( args[1] );
    if ( !MoneyUtils.moneyTypes.contains( type ) ) {
      player
          .sendMessage( Main.prefixError + "Wpisano zły typ pieniążka: " + MoneyUtils.moneyTypes );
      return true;
    }

    if ( amount <= 0 ) {
      player.sendMessage( Main.prefixError + "Wpisano złą ilość" );
      return true;
    }

    if ( !this.plugin.mutils.hasAmount( player.getUniqueId().toString(), type, amount ) ) {
      player.sendMessage( Main.prefixError + "Masz za mało pieniążków aby wymienić!" );
      return true;
    }

    this.plugin.mutils.takeMoney( player.getUniqueId().toString(), type, amount );

    ItemStack item = this.createMoneyItem( type, amount );
    player.getInventory().addItem( item );

    player.sendMessage( Main.prefixInfo + "Wyciągnięto " + amount + " " + type + " z portfela" );

    ChatUtils.cacheMessageForBotLog( BotChannels.MoneyLogId.toString(), String
        .format( "**%s** Wyciągnął z portfela %d " + "%sów", player
            .getDisplayName(), amount, type ) );

    return true;
  }

  @SuppressWarnings("deprecation")
  private ItemStack createMoneyItem(
      String type,
      int amount
  ) {
    Material material = Material.matchMaterial( String.valueOf( this.getMoneyId( type ) ) );
    if ( material == null ) {
      material = Material.GOLD_NUGGET;
    }
    ItemStack item = new ItemStack( material, amount );
    ItemMeta meta = item.getItemMeta();

    meta.setDisplayName( this.getMoneyDisplayName( type ) );
    meta.setLore( this.getMoneyLore( type ) );

    item.setItemMeta( meta );

    NBTItem nbt = new NBTItem( item );
    nbt.setBoolean( WymienCommand.MoneyNbtTag, true );
    nbt.setString( "type", type );
    nbt.applyNBT( item );

    return item;
  }

  private int getMoneyId(
      String type
  ) {
    switch ( type ) {
      case "złotnik":
        return 6571;

      case "srebrnik":
        return 6570;

      case "miedziak":
        return 6569;
    }

    return 0;
  }

  private String getMoneyDisplayName(
      String type
  ) {
    switch ( type ) {
      case "złotnik":
        return ChatColor.YELLOW + "Złotnik";

      case "srebrnik":
        return ChatColor.BLUE + "Srebrnik";

      case "miedziak":
        return ChatColor.GOLD + "Miedziak";
    }

    return "NIEPOPRAWNY TYP";
  }

  private List<String> getMoneyLore(
      String type
  ) {
    switch ( type ) {
      case "złotnik":
        return Arrays.asList( ChatColor.YELLOW + "[Wizualia:]", ChatColor.DARK_GRAY
            + "Moneta będąca aktualną walutą najwyższej wartości na Koris. Wykonana została z "
            + "nieznanego złotawego stopu metali o kolorze blado-złotym. Moneta nie ulega korozji. Jej średnica to "
            + "23 mm i grubość 1,7 mm. Na monecie został wybity symbol przedstawiający głowę smoka.", ChatColor.YELLOW
                + "[Waga:] 5 gram", ChatColor.YELLOW
                    + "[Jakość:] Bardzo dobra [5/5]", ChatColor.YELLOW + "[Narrator wydający:] "
                        + ChatColor.RED + "Konsola" );

      case "srebrnik":
        return Arrays.asList( ChatColor.BLUE + "[Wizualia:]", ChatColor.DARK_GRAY
            + "Moneta będąca aktualną walutą średniej wartości na Koris. Wykonana została z "
            + "nieznanego srebrzystego stopu metali o kolorze blado-srebrnym. Moneta nie ulega korozji. Jej "
            + "średnica to 20,5 mm i grubość 1,7 mm. Na monecie został wybity symbol dwóch skrzyżowanych mieczy.", ChatColor.BLUE
                + "[Waga:] 3,94 grama", ChatColor.BLUE
                    + "[Jakość:] Bardzo dobra [5/5]", ChatColor.BLUE + "[Narrator wydający:] "
                        + ChatColor.RED + "Konsola" );

      case "miedziak":
        return Arrays.asList( ChatColor.GOLD + "[Wizualia:]", ChatColor.DARK_GRAY
            + "Moneta będąca aktualną walutą najniższej wartości na Koris. Wykonana została z "
            + "nieznanego stopu o kolorze miedzianym, który z czasem pokrywa się patyną. Jej średnica to 16,5 mm i "
            + "grubość 1,7 mm. Na monecie został wybity symbol liścia winorośli.", ChatColor.GOLD
                + "[Waga:] 2,51 grama", ChatColor.GOLD
                    + "[Jakość:] Bardzo dobra [5/5]", ChatColor.GOLD + "[Narrator wydający:] "
                        + ChatColor.RED + "Konsola" );
    }

    return Collections.emptyList();
  }

  public static void moveMoneyToPurse(
      Player player,
      NBTItem nbti,
      ItemStack item,
      boolean all
  ) {
    String type = nbti.getString( "type" );
    int amount = all ? item.getAmount() : 1;

    Main.getInstance().mutils.giveMoney( player.getUniqueId().toString(), type, amount );

    item.setAmount( item.getAmount() - amount );

    player.sendMessage( Main.prefixInfo + "Wrzucono " + amount + " " + type + " do portfela" );

    ChatUtils.cacheMessageForBotLog( BotChannels.MoneyLogId.toString(), String
        .format( "**%s** Wrzucił do portfela %d " + "%sów", player
            .getDisplayName(), amount, type ) );
  }
}
