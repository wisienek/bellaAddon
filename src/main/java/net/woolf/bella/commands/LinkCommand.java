package net.woolf.bella.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nonnull;

import Types.BotChannels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import net.woolf.bella.Main;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.utils.DbUtils;

public class LinkCommand implements CommandExecutor {

  public static final Integer KeyLengh = 7;
  public static Map<String, String> CachedKeys = new HashMap<>();

  private final Main plugin;

  public LinkCommand(
      Main main
  ) {
    this.plugin = main;
    plugin.getCommand( "link" ).setExecutor( this );
  }

  public static String getUsage() {
    return "/link - Generuje kod do podłączenia konta (przepisz w dc)\n/link lista - Pokazuje połączone konta\n/link "
        + "remove - usuwa połączenie";
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command cmd,
      String label,
      String[] args
  ) {
    if ( !( sender instanceof Player ) ) {
      sender.sendMessage( Main.prefixError + "Tylko dla graczy!" );
      return true;
    }

    Player player = (Player) sender;

    String action = args.length > 0 ? args[0] : "generate";

    switch ( action ) {
      case "lista": {
        try {
          Map<String, String[]> accounts = DbUtils.getInstance()
              .getConnectedAccounts( player.getUniqueId().toString() );

          StringBuilder sb = new StringBuilder( Main.prefixInfo + "Połączone konta z dc:" );

          for ( Entry<String, String[]> ent : accounts.entrySet() )
            sb.append( "\n- " + ChatColor.AQUA )
                .append( ent.getValue()[0] )
                .append( ChatColor.GRAY )
                .append( " (" )
                .append( ent.getKey() )
                .append( ")" );

          player.sendMessage( sb.toString() );
          return true;

        } catch ( SQLException | IOException e ) {
          player.sendMessage( Main.prefixError + "Błąd z DB!" );
          e.printStackTrace();
        }

        return true;
      }

      case "remove": {
        try {
          boolean deleted = DbUtils.getInstance().removeAccount( player.getUniqueId().toString() );

          if ( deleted ) {
            player.sendMessage( Main.prefixInfo + "Usunięto połączenie konta!" );
            this.plugin.bot.sendLog( "Gracz **" + player.getName()
                + "** Odlinkował konto z dc!", BotChannels.VariousLogId.toString() );
          } else {
            player.sendMessage( Main.prefixError + "Nie udało się usunąć konta!" );
            this.plugin.bot.sendLog( "Gracz **" + player.getName()
                + "** Odlinkował konto z dc!", BotChannels.VariousLogId.toString() );
          }

          return true;
        } catch ( SQLException | IOException e ) {
          player.sendMessage( Main.prefixError + "Błąd z DB!" );
          e.printStackTrace();
        }

        return true;
      }

      case "generate": {
        try {
          Map<String, String[]> accounts = DbUtils.getInstance()
              .getConnectedAccounts( player.getUniqueId().toString() );

          if ( accounts != null ) {
            player.sendMessage( Main.prefixError + "To konto jest już połączone z discordem!" );
            return true;
          }

          String cachedKey = this.getKeyFromCacheByPlayer( player );
          String key = cachedKey != null ? cachedKey : this.genKey();

          sender.sendMessage( Main.prefixInfo + "Twój kod na discorda to: " + ChatColor.BOLD
              + ChatColor.AQUA + key );

          if ( cachedKey == null )
            this.addKeyToCache( key, player.getUniqueId().toString() );

        } catch ( SQLException | IOException e ) {
          player.sendMessage( Main.prefixError + "Błąd przy DB!" );
          e.printStackTrace();
        }
      }

      default: {
        player.sendMessage( Main.prefixInfo + LinkCommand.getUsage() );
      }
    }

    return true;
  }

  private String genKey() {
    StringBuilder randomStr = new StringBuilder( UUID.randomUUID().toString() );
    while ( randomStr.length() < LinkCommand.KeyLengh )
      randomStr.append( UUID.randomUUID().toString().replace( "-", "" ) );

    return randomStr.substring( 0, LinkCommand.KeyLengh );
  }

  public String getKeyFromCache(
      @Nonnull String key
  ) {
    return LinkCommand.CachedKeys.get( key );
  }

  public String getKeyFromCacheByPlayer(
      @Nonnull Player player
  ) {
    String playerUUID = player.getUniqueId().toString();

    for ( Entry<String, String> ent : LinkCommand.CachedKeys.entrySet() )
      if ( ent.getValue().equals( playerUUID ) )
        return ent.getKey();

    return null;
  }

  private void addKeyToCache(
      @Nonnull String key,
      @Nonnull String uuid
  ) {
    LinkCommand.CachedKeys.put( key, uuid );

    Main.getInstance().logger.info( "Dodano do cache nowy klucz: " + key + ", dla: " + uuid );
  }

}
