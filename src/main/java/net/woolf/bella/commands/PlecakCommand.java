package net.woolf.bella.commands;

import Types.BackpackNBTKeys;
import Types.BotChannels;
import Types.Permissions;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;

public class PlecakCommand implements CommandExecutor {

  private final Main plugin = Main.getInstance();

  public PlecakCommand() {
    plugin.getCommand( "plecak" ).setExecutor( this );
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command cmd,
      String label,
      String[] args
  ) {
    if ( !( sender instanceof Player ) ) {
      sender.sendMessage( Main.prefixError + "Komenda tylko dla graczy!" );
      return true;
    }

    if ( !sender.hasPermission( Permissions.ADMIN.toString() ) ) {
      sender.sendMessage( Main.prefixError + "Brak uprawnień do tej komendy." );
      return true;
    }

    Player player = (Player) sender;
    ItemStack heldItem = player.getInventory().getItemInMainHand();

    if ( heldItem == null || !heldItem.getType().isItem() || heldItem.getType() == Material.AIR ) {
      player.sendMessage( Main.prefixError + "Musisz trzymać item aby zrobić w nim ekwipunek!" );
      return true;
    }

    if ( args.length == 0 ) {
      player.sendMessage( Main.prefixError + this.getUsage() );
      return true;
    }

    String action = args[0].toLowerCase();
    switch ( action ) {
      case "stworz":
        handleCreate( player, heldItem, args );
        return true;

      case "ustaw":
        handleAssign( player, heldItem, args );
        return true;

      default:
        player.sendMessage( Main.prefixError + this.getUsage() );
        return true;
    }
  }

  private void handleCreate(
      @NotNull Player player,
      @NotNull ItemStack heldItem,
      String[] args
  ) {
    if ( args.length < 3 ) {
      player.sendMessage( Main.prefixError + this.getUsage() );
      return;
    }

    int rows;
    try {
      rows = Integer.parseInt( args[1] );
    } catch ( NumberFormatException ex ) {
      player.sendMessage( Main.prefixError + "Liczba rzędów musi być liczbą 1-6." );
      return;
    }

    if ( rows < 1 || rows > 6 ) {
      player.sendMessage( Main.prefixError + "Liczba rzędów musi być z przedziału <1;6>!" );
      return;
    }

    String name = String.join( " ", Arrays.copyOfRange( args, 2, args.length ) ).trim();
    if ( name.length() < 3 ) {
      player.sendMessage( Main.prefixError + "Nazwa plecaka musi mieć przynajmniej 3 znaki!" );
      return;
    }

    try {
      String uuid = DbUtils.getInstance().createBackpack( name );
      setBackPackUUID( heldItem, uuid );
      addInventoryMeta( heldItem, rows );

      player.sendMessage( Main.prefixInfo + "Stworzono plecak: " + uuid );

      if ( plugin.bot != null && plugin.bot.api != null ) {
        plugin.bot
            .sendLog( String.format( "**%s** Utworzył plecak o nazwie `%s` i uuid `%s`", player
                .getName(), name, uuid ), BotChannels.VariousLogId.toString() );
      }
    } catch ( SQLException | IOException e ) {
      String reason = e.getMessage() != null ? e.getMessage() : "Brak szczegółów";
      if ( reason.contains( "No suitable driver" ) ) {
        player.sendMessage( Main.prefixError
            + "Brak sterownika bazy MySQL. Sprawdź konfigurację i zależności." );
      } else {
        player.sendMessage( Main.prefixError + "Coś poszło nie tak przy tworzeniu plecaka!" );
      }
      plugin.getLogger().log( Level.WARNING, "Błąd podczas tworzenia plecaka: " + reason, e );
    }
  }

  private void handleAssign(
      @NotNull Player player,
      @NotNull ItemStack heldItem,
      String[] args
  ) {
    if ( args.length < 2 ) {
      player.sendMessage( Main.prefixError + this.getUsage() );
      return;
    }

    String playerName = args[1];
    OfflinePlayer target = PlayerUtils.getOfflinePlayer( playerName );

    if ( target == null ) {
      player.sendMessage( Main.prefixError + "Nie znaleziono gracza " + playerName
          + ", sprawdź pisownię!" );
      return;
    }

    if ( !target.isOnline() ) {
      player.sendMessage( Main.prefixError + "Gracz " + playerName + " nie jest online." );
      return;
    }

    Player targetPlayer = target.getPlayer();
    if ( targetPlayer == null ) {
      player.sendMessage( Main.prefixError + "Nie można nadać plecaka — gracz jest offline." );
      return;
    }

    targetPlayer.getInventory().addItem( heldItem );
    player.sendMessage( Main.prefixInfo
        + String.format( "Dałeś %s plecak, który trzymasz w ręce", target.getName() ) );
  }

  public static void addInventoryMeta(
      @NotNull ItemStack item,
      int rows
  ) {
    NBTItem nbti = new NBTItem( item );
    nbti.setBoolean( BackpackNBTKeys.ISBACKPACK.toString(), true );
    nbti.setInteger( BackpackNBTKeys.ROWS.toString(), rows );

    nbti.applyNBT( item );
  }

  public static void setBackPackUUID(
      @NotNull ItemStack item,
      @NotNull String uuid
  ) {
    NBTItem nbti = new NBTItem( item );
    nbti.setString( BackpackNBTKeys.UUID.toString(), uuid );
    nbti.applyNBT( item );
  }

  public String getUsage() {
    return "/plecak stworz <1-6> <nazwa> - tworzy z przedmiotu w ręku plecak"
        + "\n/plecak ustaw <nick> - ustawia " + "komuś właściciela";
  }

}
