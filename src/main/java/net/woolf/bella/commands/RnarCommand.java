package net.woolf.bella.commands;

import Types.BotChannels;
import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RnarCommand implements CommandExecutor {

  private final Main plugin;

  public RnarCommand(
      Main instance
  ) {
    this.plugin = instance;

    plugin.getCommand( "rnar" ).setExecutor( this );
  }

  public static String getUsage() {
    return "/rnar <int range> <...text> - wyświetla narracje w promieniu <range> (minimum 2)";
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command cmd,
      String label,
      String[] args
  ) {
    if ( sender instanceof Player ) {
      Player player = (Player) sender;

      if ( !player.hasPermission( "beloris.nar" ) ) {
        player.sendMessage( Main.prefixError
            + "Musisz mieć permissie beloris.nar aby użyć tej komendy!" );
        return true;
      }

      if ( args.length == 0 ) {
        player.sendMessage( Main.prefixInfo + RnarCommand.getUsage() );
        return true;
      }

      try {
        int radius = Integer.parseInt( args[0] );

        if ( radius < 2 ) {
          player.sendMessage( Main.prefixError + RnarCommand.getUsage() );
          return true;
        }

        List<String> _msg = new LinkedList<>( Arrays.asList( args ) );
        _msg.remove( 0 );

        List<Player> nearbyPlayers = LocationUtils.getNearbyPlayers( player, radius );

        String msg = String.join( " ", _msg ).replaceAll( "`", "" );
        String formatedMsg = ChatColor.DARK_RED + "[R] " + ChatColor.YELLOW + "[" + msg + "]";

        player.sendMessage( formatedMsg + " {" + args[0] + "}" );
        for ( Player target : nearbyPlayers )
          target.sendMessage( formatedMsg );

        Location loc = player.getLocation();

        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.RangePrefix
            + " {" + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "} " + " ("
            + radius + ")" + "[" + player.getDisplayName() + "] " + "`" + msg + "`" );
      } catch ( NumberFormatException nfe ) {
        player.sendMessage( Main.prefixError + "Błąd przy formatowaniu liczby!" );
        player.sendMessage( Main.prefixError + RnarCommand.getUsage() );
        return true;
      }

    } else {
      sender.sendMessage( "Komenda tylko dla graczy!" );
    }
    return true;
  }
}
