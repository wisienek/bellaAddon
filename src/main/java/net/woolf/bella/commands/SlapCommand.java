package net.woolf.bella.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import Types.Permissions;
import net.woolf.bella.Main;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class SlapCommand implements CommandExecutor {

  Main plugin;

  public SlapCommand() {
    plugin = Main.getInstance();

    plugin.getCommand( "slap" ).setExecutor( this );
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command cmd,
      String alias,
      String[] args
  ) {
    if ( !sender.hasPermission( Permissions.ADMIN.toString() ) ) {
      sender.sendMessage( Main.prefixError + "Nie masz permissi " + Permissions.ADMIN
          + " aby to wykonać!" );
      return true;
    }
    if ( args.length != 1 ) {
      sender.sendMessage( this.getUsage() );
      return true;
    }

    String playerName = args[0];
    Player target = this.plugin.server.getPlayer( playerName );
    if ( !target.isOnline() ) {
      sender.sendMessage( Main.prefixError + "Gracz jest nieaktywny!" );
      return true;
    }

    Vector loc = target.getLocation().getDirection();
    Vector randomV = Vector.getRandom();

    double randomX = ThreadLocalRandom.current().nextDouble( -1.5, 1.5 );
    double randomY = ThreadLocalRandom.current().nextDouble( 0.15, 1 );
    double randomZ = ThreadLocalRandom.current().nextDouble( -1.5, 1.5 );

    target.setVelocity( loc.multiply( randomV ).setY( randomY ).setZ( randomZ ).setX( randomX ) );

    target.sendMessage( Main.prefixInfo + "Dostałeś slapa!" );
    sender.sendMessage( Main.prefixInfo + target.getDisplayName() + " Dostał slapa!" );

    return true;
  }

  private String getUsage() {
    return "/slap <player> - slappuje gracza";
  }
}
