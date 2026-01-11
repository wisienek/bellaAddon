package net.woolf.bella.commands;

import Types.BotChannels;
import Types.Permissions;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;
import net.woolf.bella.utils.LocationUtils;
import net.woolf.bella.utils.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class OtpCommand implements CommandExecutor {

  private final Main plugin;

  public OtpCommand(
      Main plugin
  ) {
    this.plugin = plugin;
    plugin.getCommand( "otp" ).setExecutor( this );
  }

  public String getUsage() {
    return Main.prefixInfo + "Użycie komendy: /otp" + "\n/otp info - info o aktualnym lvl"
        + "\n/otp set/del/tp/ws/os"
        + "/list/seteffect <nazwa> - set: ustawia, del: usuwa, tp: teleportuje, ws: teleportacja wspólna, list: "
        + "lista, " + "os: teleportuje wyznaczoną osobę" + "\n/otp set tp1" + "\n/otp tp tp1"
        + "\n/otp list" + "\n/otp" + " ws tp1" + "\n/otp os tp1 Zbyszek" + "\n/otp seteffect ignis"
        + "\n/otp enchant <punkt tp> [ilość użyć] - " + "ustawia "
        + "teleport na przedmiot z ewentualną ilością użyć" + "\n/otp inspect - robi inspekcję "
        + "przedmiotu " + "trzymanego w " + "dłoni";
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command cmd,
      String label,
      String[] args
  ) {
    if ( !( sender instanceof Player ) ) {
      sender.sendMessage( "Tylko dla graczy!" );
      return true;
    }

    Player player = (Player) sender;

    if ( player.hasPermission( Permissions.OTP_USE.toString() ) ) {

      if ( args.length < 1 ) {
        player.sendMessage( getUsage() );
        return true;
      }

      String otp = "";
      String levelS = plugin.teleportUtils.getLevel( player );
      int level = Integer.parseInt( levelS );

      if ( args.length > 1 )
        otp = args[1];

      if ( level == 0 ) {
        player.sendMessage( Main.prefixError + "Nie posiadasz odpowiednich umiejętności!" );
        return true;
      }

      int cld = Integer
          .parseInt( (String) plugin.configManager.config.get( "tp-level-" + levelS + "-cld" ) );
      int radius = Integer
          .parseInt( (String) plugin.configManager.config.get( "tp-level-" + levelS + "-radius" ) );
      int maxp = Integer
          .parseInt( (String) plugin.configManager.config.get( "tp-level-" + levelS + "-maxp" ) );
      int maxpts = Integer.parseInt( (String) plugin.configManager.config
          .get( "tp-level-" + levelS + "-maxpoints" ) );
      int setmaxuse = Integer.parseInt( (String) plugin.configManager.config
          .get( "tp-level-" + levelS + "-setmaxuse" ) );

      switch ( args[0] ) {
        case "info": {
          Map<String, Object> list = plugin.teleportUtils.getOTP( player );
          int len = list.size();
          String type = plugin.teleportUtils.getType( player );

          String os = Main.prefixInfo + "Twój level wynosi: " + ChatColor.YELLOW + levelS + "\n"
              + ChatColor.GRAY + "Max pkt tp : " + ChatColor.YELLOW + len + " / " + maxpts + "\n"
              + ChatColor.GRAY + "Odległość  : " + ChatColor.YELLOW + radius + "m" + "\n"
              + ChatColor.GRAY + "Cooldown   : " + ChatColor.YELLOW + cld + "s" + "\n"
              + ChatColor.GRAY + "Max Graczy : " + ChatColor.YELLOW + maxp + " graczy" + "\n"
              + ChatColor.GRAY + "Efekt tp   : " + ChatColor.YELLOW
              + ( ( type == null || type.isEmpty() ) ? "ignis (default)" : type );

          player.sendMessage( os );
          return true;
        }

        case "set": {
          if ( otp == null || otp.isEmpty() ) {
            player.sendMessage( Main.prefixError + "Niepoprawna nazwa: " + ChatColor.YELLOW + otp );
            return true;
          }

          // sprawdmax
          Map<String, Object> list = plugin.teleportUtils.getOTP( player );
          int len = list.size();
          if ( len >= maxpts ) {
            player.sendMessage( Main.prefixError + "Nie możesz mieć więcej punktów tp (" + maxpts
                + ")!" );
            return true;
          }

          // ustaw
          if ( plugin.configManager.config.getBoolean( "setOTP-command-delay" ) ) {
            if ( plugin.cooldownUtils.hasSetTpCooldown( player ) ) {
              player.sendMessage( Main.prefixError + "Musisz poczekać " + ChatColor.RED
                  + plugin.cooldownUtils.getSetCooldownTime( player ) + ChatColor.GRAY
                  + " sekund." );
            } else {
              setPlayerOTP( player, otp );
              plugin.cooldownUtils.setCoolDownTimeOTP( player, cld, true );
              player.sendMessage( Main.prefixInfo + "Ustawiono TP : " + ChatColor.AQUA + otp
                  + ChatColor.WHITE + ". Zajęte tp: " + ( len + 1 ) + " / " + maxpts );
            }
          } else {
            setPlayerOTP( player, otp );
            player.sendMessage( Main.prefixInfo + "Ustawiono TP : " + ChatColor.AQUA + otp
                + ChatColor.WHITE + ". Zajęte tp: " + ( len + 1 ) + " / " + maxpts );
          }

          return true;
        }

        case "del": {
          if ( otp == null || otp.isEmpty() ) {
            player.sendMessage( Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
            return true;
          }

          if ( plugin.teleportUtils.tpsIsNull( player, otp ) ) {
            player
                .sendMessage( Main.prefixError + "Nie masz takiego TP: " + ChatColor.YELLOW + otp );
          } else {
            Map<String, Object> list = plugin.teleportUtils.getOTP( player );
            int len = list.size();

            plugin.teleportUtils.deleteOTP( player, otp );
            player.sendMessage( Main.prefixInfo + "Usunięto teleport: " + ChatColor.AQUA + otp
                + ChatColor.WHITE + ". Wolne: " + ( len - 1 ) + " / " + maxpts );
          }

          return true;
        }

        case "effect": {
          if (
            player.hasPermission( Permissions.ATP_ADMIN.toString() )
                || player.hasPermission( Permissions.TEST.toString() )
          ) {
            String type = plugin.teleportUtils.getType( player );
            plugin.teleportUtils.tpEffect( player, null, null );

            player.sendMessage( Main.prefixInfo + "Typ: " + ChatColor.BLUE + type );
          }

          return true;
        }

        case "seteffect": {
          if ( otp.isEmpty() ) {
            player.sendMessage( getUsage() );
            return true;
          }

          String ef = plugin.teleportUtils.getType( player );

          if ( !ef.isEmpty() && !player.hasPermission( Permissions.ATP_ADMIN.toString() ) ) {
            player.sendMessage( Main.prefixError
                + "Masz już ustawiony efekt! Jedynie admin może Ci zmienić." );
            return true;
          }

          net.woolf.bella.types.EffectType effectType = net.woolf.bella.types.EffectType
              .fromString( otp );
          if (
            effectType == net.woolf.bella.types.EffectType.IGNIS && !otp.equalsIgnoreCase( "ignis" )
          ) {
            player.sendMessage( Main.prefixError + "Twój efekt nie znajduje się na liście: "
                + ChatColor.YELLOW + "ignis, aqua, geo, electro, aeter, caligo, lux" );
            return true;
          }

          Boolean ok = plugin.teleportUtils.setType( player, otp );

          player.sendMessage( ok ? Main.prefixInfo + "Ustawiono efekt na: " + ChatColor.BLUE + otp
              : Main.prefixError + "Nie powiodło się ustawianie efektu!" );
          return true;
        }

        case "tp": {
          if ( otp == null || otp.isEmpty() ) {
            player.sendMessage( Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
            return true;
          }

          if ( plugin.teleportUtils.tpsIsNull( player, otp ) ) {
            player
                .sendMessage( Main.prefixError + "Nie masz takiego TP: " + ChatColor.YELLOW + otp );
          } else {

            // przelicz odległość
            Location loc = plugin.teleportUtils.getOTPLocation( player, otp );
            Location playerLoc = player.getLocation();
            double distance = playerLoc.distance( loc );

            if ( distance > radius ) {
              player.sendMessage( Main.prefixError
                  + "Za długi dystans pomiędzy lokacją, a punktem teleportacji! ( " + (int) distance
                  + " / " + radius + " )" );
              return true;
            }

            List<Player> sendTo = LocationUtils.getNearbyPlayers( player, 20 );
            for ( Player sending : sendTo )
              sending.sendMessage( ChatColor.WHITE + "[L] " + ChatColor.YELLOW
                  + "[Niedaleko słychać trzask teleportacji]" );

            if ( plugin.configManager.config.getBoolean( "OTP-command-delay" ) ) {
              if ( plugin.cooldownUtils.hasTpCooldown( player ) ) {
                player.sendMessage( Main.prefixError + "Musisz odpocząć " + ChatColor.RED
                    + plugin.cooldownUtils.getCooldownTime( player ) + ChatColor.GRAY
                    + " sekund." );
              } else {
                plugin.teleportUtils.tpEffect( player, otp, null );

                plugin.cooldownUtils.setCoolDownTimeOTP( player, cld, false );
                player.sendMessage( Main.prefixInfo + "Teleportowano do punktu " + ChatColor.YELLOW
                    + otp );

                this.plugin.bot.sendLog( String
                    .format( "[%s] teleportował {%o %o %o} -> {%o %o %o} (%s)", player
                        .getName(), playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc
                            .getBlockZ(), loc.getBlockX(), loc
                                .getBlockY(), loc.getBlockZ(), otp ), BotChannels.VariousLogId
                                    .toString() );
              }
            } else {
              plugin.teleportUtils.tpEffect( player, otp, null );

              player.sendMessage( Main.prefixInfo + "Teleportowano do punktu " + ChatColor.YELLOW
                  + otp );

              this.plugin.bot
                  .sendLog( String.format( "[%s] teleportował {%o %o %o} -> {%o %o %o} (%s)", player
                      .getName(), playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc
                          .getBlockZ(), loc.getBlockX(), loc
                              .getBlockY(), loc.getBlockZ(), otp ), BotChannels.VariousLogId
                                  .toString() );
            }
          }

          return true;
        }

        case "list": {
          Map<String, Object> list = plugin.teleportUtils.getOTP( player );
          int len = list.size();

          if ( len == 0 ) {
            player.sendMessage( Main.prefixInfo + "Nie posiadasz żadnych tp. Wolne: " + maxpts );
            return true;
          }
          Location playerLoc = player.getLocation();

          StringBuilder os = new StringBuilder();
          os.append( Main.prefixInfo )
              .append( "Twoja lista TP ( " )
              .append( len )
              .append( " / " )
              .append( maxpts )
              .append( " ) : " );
          for ( String key : list.keySet() ) {
            Location loc = plugin.teleportUtils.getOTPLocation( player, key );
            os.append( "\n- " )
                .append( key )
                .append( " (" )
                .append( (int) playerLoc.distance( loc ) )
                .append( "m)" );
            // plugin.logger.info("OTP: " + key);
          }

          player.sendMessage( os.toString() );
          return true;
        }

        case "enchant": {
          if ( !player.hasPermission( Permissions.OTP_ENCHANT.toString() ) ) {
            player.sendMessage( Main.prefixError
                + "Musisz posiadać umiejętność enchatnowania aby tego użyć! (bella.otp.enchant)" );
            return true;
          }

          if ( args.length < 3 ) {
            player.sendMessage( Main.prefixError + "Argument 3 musi być liczbą!" );
            return true;
          }

          ItemStack item = player.getInventory().getItemInMainHand();

          if ( item == null || item.getType() == Material.AIR ) {
            player.sendMessage( Main.prefixError
                + "Musisz trzymać przedmiot, który chcesz enchantować!" );
            return true;
          }

          if ( otp == null || otp.isEmpty() ) {
            player.sendMessage( Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
            return true;
          }

          Location loc = plugin.teleportUtils.getOTPLocation( player, otp );
          @SuppressWarnings("deprecation")
          NBTItem nbti = new NBTItem( item, true );

          if ( nbti.hasKey( "teleportEnchantment" ) ) {
            player.sendMessage( Main.prefixError + "Przedmiot jest już enchantowany!" );
            return true;
          }

          int maxUses = Integer.parseInt( args[2] );

          // otp enchant <name> <maxuse> <????minlvl>
          NBTCompound comp = nbti.addCompound( "teleportEnchantment" );
          // core
          comp.setString( "enchanter", player.getName() );
          comp.setInteger( "maxLength", radius );
          comp.setInteger( "cld", cld );
          comp.setInteger( "maxUse", maxUses );
          // location
          comp.setDouble( "x", loc.getX() );
          comp.setDouble( "y", loc.getY() );
          comp.setDouble( "z", loc.getZ() );
          comp.setFloat( "yaw", loc.getYaw() );
          comp.setFloat( "pitch", loc.getPitch() );

          nbti.mergeNBT( item );

          player.sendMessage( Main.prefixInfo + "Enchantowano item na teleport do punktu "
              + ChatColor.GOLD + otp + ChatColor.AQUA + " (MaxU: " + maxUses + ")" + "!" );

          return true;
        }

        case "inspect": {
          if ( !player.hasPermission( Permissions.OTP_ENCHANT.toString() ) ) {
            player.sendMessage( Main.prefixError
                + "Musisz posiadać umiejętność enchatnowania aby tego użyć! ("
                + Permissions.OTP_ENCHANT + ")" );
            return true;
          }
          ItemStack item = player.getInventory().getItemInMainHand();

          if ( item == null || item.getType() == Material.AIR ) {
            player.sendMessage( Main.prefixError
                + "Musisz trzymać przedmiot, który chcesz enchantować!" );
            return true;
          }

          NBTItem nbti = new NBTItem( item );

          if ( !nbti.hasKey( "teleportEnchantment" ) ) {
            player.sendMessage( Main.prefixError + "Przedmiot nie jest enchantowany!" );
            return true;
          }

          NBTCompound comp = nbti.getCompound( "teleportEnchantment" );
          String enchanter = comp.getString( "enchanter" );
          String maxLen = String.valueOf( comp.getInteger( "maxLength" ) );
          String enchCld = String.valueOf( comp.getInteger( "cld" ) );
          String maxUse = comp.hasKey( "maxUse" )
              ? comp.getInteger( "maxUse" ) == Integer.MAX_VALUE ? "Nieskończoności"
                  : String.valueOf( comp.getInteger( "maxUse" ) )
              : "Nieskończoności";
          String used = comp.hasKey( "used" ) ? String.valueOf( comp.getInteger( "used" ) )
              : "niewiadomo";
          // location
          int x = (int) Math.round( comp.getDouble( "x" ) );
          int y = (int) Math.round( comp.getDouble( "y" ) );
          int z = (int) Math.round( comp.getDouble( "z" ) );

          player.sendMessage( Main.prefixInfo + "Informacje o Zaczarowanym przedmiocie:\n"
              + ChatColor.GREEN + "Enchanter: " + ChatColor.AQUA + enchanter + "\n"
              + ChatColor.GREEN + "Odległość: " + ChatColor.AQUA + maxLen + "\n" + ChatColor.GREEN
              + "Odpoczynek: " + ChatColor.AQUA + enchCld + "s.\n" + ChatColor.GREEN
              + "Użytkowanie: " + ChatColor.AQUA + used + " z " + maxUse + "\n" + ChatColor.GREEN
              + "Lokacja: " + ChatColor.AQUA + x + " " + y + " " + z + "\n" );

          return true;
        }

        case "os": {
          if ( args.length < 3 ) {
            player.sendMessage( getUsage() );
            return true;
          }

          if ( otp == null || otp.isEmpty() ) {
            player.sendMessage( Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
            return true;
          }

          Location loc = plugin.teleportUtils.getOTPLocation( player, otp );
          double distance = player.getLocation().distance( loc );

          if ( distance > radius ) {
            player.sendMessage( Main.prefixError
                + "Za długi dystans pomiędzy lokacją, a punktem teleportacji! ( " + (int) distance
                + " / " + radius + " )" );
            return true;
          }

          String pname = args[2];

          List<Player> list = PlayerUtils.getPlayersWithinRange( player.getLocation(), 5L, player );
          Player target = null;
          for ( Player p : list )
            if ( p.getName().equals( pname ) )
              target = p;

          if ( target == null || target.getName().equals( player.getName() ) ) {
            player.sendMessage( Main.prefixError + "Nie znaleziono gracza: " + ChatColor.YELLOW
                + pname );
            return true;
          }

          Location playerLoc = target.getLocation();

          if ( plugin.configManager.config.getBoolean( "OTP-command-delay" ) ) {
            if ( plugin.cooldownUtils.hasTpCooldown( player ) ) {
              player.sendMessage( Main.prefixError + "Musisz odpocząć " + ChatColor.RED
                  + plugin.cooldownUtils.getCooldownTime( player ) + ChatColor.GRAY + " sekund." );
            } else {
              plugin.teleportUtils.tpEffect( player, otp, target );
              target.sendMessage( Main.prefixInfo + "Zostałeś teleportowany przez "
                  + player.getName() + " do punktu " + otp );
              player
                  .sendMessage( Main.prefixInfo + "Teleportowałeś " + pname + "do punktu " + otp );

              plugin.cooldownUtils.setCoolDownTimeOTP( player, cld, false );

              this.plugin.bot.sendLog( String
                  .format( "[%s] teleportował [%s] {%o %o %o} -> {%o %o %o} (%s)", player
                      .getName(), target.getName(), playerLoc.getBlockX(), playerLoc
                          .getBlockY(), playerLoc.getBlockZ(), loc.getBlockX(), loc
                              .getBlockY(), loc.getBlockZ(), otp ), BotChannels.VariousLogId
                                  .toString() );
            }
          } else {
            plugin.teleportUtils.tpEffect( player, otp, target );
            target.sendMessage( Main.prefixInfo + "Zostałeś teleportowany przez " + player.getName()
                + " do punktu " + otp );
            player.sendMessage( Main.prefixInfo + "Teleportowałeś " + pname + "do punktu " + otp );

            this.plugin.bot.sendLog( String
                .format( "[%s] teleportował [%s] {%o %o %o} -> {%o %o %o} (%s)", player
                    .getName(), target.getName(), playerLoc.getBlockX(), playerLoc
                        .getBlockY(), playerLoc.getBlockZ(), loc.getBlockX(), loc
                            .getBlockY(), loc.getBlockZ(), otp ), BotChannels.VariousLogId
                                .toString() );
          }

          return true;
        }

        case "ws": {
          if ( !player.hasPermission( Permissions.OTP_WS.toString() ) ) {
            player.sendMessage( Main.prefixError + "Nie potrafisz jeszcze wspólnej teleportacji!" );
            return true;
          }

          Location playerLoc = player.getLocation();
          List<Player> nearbyPlayers = PlayerUtils.getPlayersWithinRange( playerLoc, 3L, player );

          if ( nearbyPlayers.size() > maxp ) {
            player.sendMessage( Main.prefixError + "Za dużo osób do teleportacji! ( "
                + nearbyPlayers.size() + " / " + maxp + " )" );
            return true;
          }

          if ( otp == null || otp.isEmpty() ) {
            player.sendMessage( Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
            return true;
          }

          if ( plugin.teleportUtils.tpsIsNull( player, otp ) ) {
            player
                .sendMessage( Main.prefixError + "Nie masz takiego TP: " + ChatColor.YELLOW + otp );
          } else {
            // przelicz odległość
            Location loc = plugin.teleportUtils.getOTPLocation( player, otp );
            double distance = playerLoc.distance( loc );

            if ( distance > radius ) {
              player.sendMessage( Main.prefixError
                  + "Za długi dystans pomiędzy lokacją, a punktem teleportacji ( " + (int) distance
                  + " / " + radius + " )" );
              return true;
            }

            List<Player> sendTo = LocationUtils.getNearbyPlayers( player, 20 );
            for ( Player sending : sendTo )
              sending.sendMessage( ChatColor.WHITE + "[L] " + ChatColor.YELLOW
                  + "[Niedaleko słychać trzask teleportacji łącznej]" );

            String playerNames = String.format( "[%s]", nearbyPlayers.stream()
                .map( (p) -> String.format( "`%s`", p.getName() ) )
                .collect( Collectors.joining( ", " ) ) );

            StringBuilder os = new StringBuilder();
            if ( plugin.configManager.config.getBoolean( "OTP-command-delay" ) ) {
              if ( plugin.cooldownUtils.hasTpCooldown( player ) ) {
                player.sendMessage( Main.prefixError + "Musisz odpocząć " + ChatColor.RED
                    + plugin.cooldownUtils.getCooldownTime( player ) + ChatColor.GRAY
                    + " sekund." );
              } else {
                for ( Player target : nearbyPlayers ) {
                  plugin.teleportUtils.tpEffect( player, otp, target );

                  os.append( target.getName() ).append( " " );
                  target.sendMessage( Main.prefixInfo + player.getDisplayName()
                      + " Teleportował się z tobą do punktu: " + ChatColor.YELLOW + otp );
                }
                plugin.teleportUtils.tpEffect( player, otp, null );

                plugin.cooldownUtils.setCoolDownTimeOTP( player, cld, false );
                player.sendMessage( Main.prefixInfo + "Teleportowano wspólnie z ( " + os
                    + " ) do punktu " + ChatColor.YELLOW + otp );

                this.plugin.bot.sendLog( String
                    .format( "[%s] teleportował {%o %o %o} -> {%o %o %o} (%s) z %s", player
                        .getName(), playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc
                            .getBlockZ(), loc.getBlockX(), loc.getBlockY(), loc
                                .getBlockZ(), otp, playerNames ), BotChannels.VariousLogId
                                    .toString() );
              }
            } else {
              for ( Player target : nearbyPlayers ) {
                plugin.teleportUtils.tpEffect( player, otp, target );

                os.append( target.getName() ).append( " " );
                target.sendMessage( Main.prefixInfo + player.getDisplayName()
                    + " Teleportował się z tobą do punktu: " + ChatColor.YELLOW + otp );
              }
              plugin.teleportUtils.tpEffect( player, otp, null );

              player.sendMessage( Main.prefixInfo + "Teleportowano wspólnie z ( " + os
                  + " ) do punktu " + ChatColor.YELLOW + otp );

              this.plugin.bot.sendLog( String
                  .format( "[%s] teleportował {%o %o %o} -> {%o %o %o} (%s) z %s", player
                      .getName(), playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc
                          .getBlockZ(), loc.getBlockX(), loc.getBlockY(), loc
                              .getBlockZ(), otp, playerNames ), BotChannels.VariousLogId
                                  .toString() );
            }
          }

          return true;
        }
      }

      return true;
    } else {
      sender.sendMessage( Main.prefixError
          + "Potrzebujesz permissi bella.otp.use aby użyć tej komendy!" );
      return true;
    }
  }

  void setPlayerOTP(
      Player player,
      String name
  ) {
    plugin.teleportUtils.setOTP( player, name );
    if ( plugin.configManager.config.getBoolean( "show-setOTP-message" ) ) {
      String strFormatted = plugin.configManager.config.getString( "setOTP-message" )
          .replace( "%player%", player.getDisplayName() );
      player.sendMessage( ChatColor.translateAlternateColorCodes( '&', strFormatted ) );
    }
  }

}
