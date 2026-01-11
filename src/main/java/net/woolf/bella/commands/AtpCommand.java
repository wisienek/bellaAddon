package net.woolf.bella.commands;

import java.io.File;
import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import Types.Permissions;

import org.bukkit.ChatColor;

import net.woolf.bella.Main;
import net.woolf.bella.Utils;

public class AtpCommand implements CommandExecutor {

  private final Main plugin;

  public AtpCommand(
      Main plugin
  ) {
    this.plugin = plugin;
    PluginCommand command = plugin.getCommand( "atp" );
    if ( command != null ) {
      command.setExecutor( this );
    } else {
      plugin.getLogger().severe( "Could not register /atp command! Is it defined in plugin.yml?" );
    }
  }

  public String getUsage() {
    return Main.prefixInfo + "Użycie komendy: /atp"
        + "\n/atp edit < level [1-5] > < cld / radius / maxp / maxpts / "
        + "maxuse > < int >  (cld - czas odczekiwania, radius - średnica koła tp, maxpts - ile max punktów tp, maxp - "
        + "ile ludzi na wspólną)" + "\n/atp edit 1 cld 15" + "\n/atp edit 3 radius 5000"
        + "\n/atp set <lvl> <gracz> - " + "ustawia level dla gracza"
        + "\n/atp info <lvl> - wyświetla info o levelu" + "\n/atp player <nick> - "
        + "wyświetla info o graczu"
        + "\n/atp seteffect <player> <effect> - ustawia efekt dla gracza online" + "\n/atp "
        + "edit <level> maxuse <0 - infinity> - ustawia max użyć do enchantera na item (infinity słownie)";
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command cmd,
      String label,
      String[] args
  ) {
    if (
      !( sender instanceof Player ) || sender.hasPermission( Permissions.ATP_ADMIN.toString() )
    ) {

      if ( args.length < 1 ) {
        sender.sendMessage( getUsage() );
        return true;
      }

      switch ( args[0] ) {
        case "edit": {
          if ( args.length < 4 ) {
            sender.sendMessage( getUsage() );
            return true;
          }
          String level = args[1];
          String opt = args[2];
          String val = args[3];

          if ( val.equals( "infinity" ) )
            val = String.valueOf( Integer.MAX_VALUE );

          switch ( opt ) {
            case "cld": {
              plugin.configManager.config.set( "tp-level-" + level + "-cld", val );
              break;
            }

            case "radius": {
              plugin.configManager.config.set( "tp-level-" + level + "-radius", val );
              break;
            }

            case "maxp": {
              plugin.configManager.config.set( "tp-level-" + level + "-maxp", val );
              break;
            }

            case "maxpts": {
              plugin.configManager.config.set( "tp-level-" + level + "-maxpoints", val );
            }

            case "maxuse": {
              plugin.configManager.config.set( "tp-level-" + level + "-setmaxuse", val );
            }
          }

          try {
            plugin.configManager.config
                .save( plugin.getDataFolder() + File.separator + "config.yml" );
          } catch ( IOException e ) {
            e.printStackTrace();
          }

          plugin.logger
              .info( Main.prefixInfo + "Zmieniono " + opt + " dla lvl " + level + " na " + val );
          sender.sendMessage( Main.prefixInfo + "Zmieniono " + opt + " dla lvl " + level + " na "
              + val );
          return true;
        }

        case "set": {
          if ( args.length < 3 ) {
            sender.sendMessage( getUsage() );
            return true;
          }
          String level = args[1];
          String pname = args[2];
          int ilvl = Integer.parseInt( level );

          if ( ilvl < 0 || ilvl > 5 ) {
            sender
                .sendMessage( Main.prefixError + "Level musi się znajdować w przedziale: <0; 5>" );
            return true;
          }
          Player target = sender.getServer().getPlayer( pname );

          plugin.teleportUtils.setTPL( target, level );

          sender.sendMessage( Main.prefixInfo + "Zmieniono level dla " + pname + " na " + level );

          return true;
        }

        case "info": {
          if ( args.length < 2 ) {
            sender.sendMessage( getUsage() );
            return true;
          }
          String level = args[1];
          String cld = (String) plugin.configManager.config.get( "tp-level-" + level + "-cld" );
          String radius = (String) plugin.configManager.config
              .get( "tp-level-" + level + "-radius" );
          String maxp = (String) plugin.configManager.config.get( "tp-level-" + level + "-maxp" );
          String maxpts = (String) plugin.configManager.config
              .get( "tp-level-" + level + "-maxpoints" );

          // plugin.logger.info(" cld " + cld + " r " + radius + " mxp " + maxp + ",
          // maxpts: "+ maxpts);

          String os = Main.prefixInfo + "Informacje o TP level " + level + "\n" + ChatColor.GRAY
              + "Punkty tp  : " + ChatColor.YELLOW + maxpts + "\n" + ChatColor.GRAY
              + "Odległość  : " + ChatColor.YELLOW + radius + "m" + "\n" + ChatColor.GRAY
              + "Cooldown   : " + ChatColor.YELLOW + cld + "s" + "\n" + ChatColor.GRAY
              + "Max Graczy : " + ChatColor.YELLOW + maxp + " graczy";

          sender.sendMessage( os );
          return true;
        }

        case "player": {
          if ( args.length < 2 ) {
            sender.sendMessage( getUsage() );
            return true;
          }
          String pname = args[1];
          Player target = sender.getServer().getPlayer( pname );
          if ( target != null ) {
            String lvl = plugin.teleportUtils.getLevel( target );

            sender.sendMessage( Main.prefixInfo + "Level gracza " + pname + " to: " + lvl );
            return true;
          } else {
            sender.sendMessage( Main.prefixError + "Gracz " + pname + " nie jest online!" );
            return true;
          }
        }

        case "seteffect": {
          if ( args.length < 3 ) {
            sender.sendMessage( getUsage() );
            return true;
          }

          String pname = args[1];
          String effect = args[2];
          Player target = sender.getServer().getPlayer( pname );

          if ( target == null ) {
            sender.sendMessage( Main.prefixError + "Gracz " + pname + " nie jest online!" );
            return true;
          }

          net.woolf.bella.types.EffectType effectType = net.woolf.bella.types.EffectType
              .fromString( effect );
          if (
            effectType == net.woolf.bella.types.EffectType.IGNIS
                && !effect.equalsIgnoreCase( "ignis" )
          ) {
            sender.sendMessage( Main.prefixError + "Efekt nie znajduje się na liście: "
                + ChatColor.YELLOW + "ignis, aqua, geo, electro, aeter, caligo, lux" );
            return true;
          }

          plugin.teleportUtils.setType( target, effect );

          sender.sendMessage( Main.prefixInfo + "Ustawiono efekt gracza " + ChatColor.GREEN + pname
              + ChatColor.WHITE + " na: " + ChatColor.AQUA + effect );
          return true;
        }

        default:
          sender.sendMessage( getUsage() );
          return true;
      }
    } else {
      sender.sendMessage( Main.prefixError
          + "Potrzebujesz permissi atp.admin aby używać tej komendy" );
      return true;
    }
  }

}
