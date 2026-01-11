package net.woolf.bella.utils;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.tr7zw.nbtapi.NBTCompound;
import net.woolf.bella.Main;
import net.woolf.bella.types.EffectType;

public class TeleportUtils {

  private final Main plugin;
  private final ConfigManager configManager;
  private final EffectUtils effectUtils;
  private final CooldownUtils cooldownUtils;

  public TeleportUtils(
      Main plugin,
      ConfigManager configManager,
      EffectUtils effectUtils,
      CooldownUtils cooldownUtils
  ) {
    this.plugin = plugin;
    this.configManager = configManager;
    this.effectUtils = effectUtils;
    this.cooldownUtils = cooldownUtils;
  }

  public void setTPL(
      @Nonnull Player player,
      @Nonnull String level
  ) {
    configManager.tpl.set( player.getUniqueId().toString() + ".level", level );
    configManager.saveTPLFile();
  }

  public String getLevel(
      @Nonnull Player player
  ) {
    String level = "0";

    if ( configManager.tpl.contains( player.getUniqueId().toString() + ".level" ) ) {
      level = configManager.tpl.getString( player.getUniqueId().toString() + ".level" );
    } else {
      setTPL( player, level );
    }

    return level;
  }

  public void setOTP(
      @Nonnull Player player,
      @Nonnull String name
  ) {
    Location loc = player.getLocation();
    String uuid = player.getUniqueId().toString();
    String basePath = "tps." + uuid + "." + name;

    configManager.tps.set( basePath + ".X", loc.getX() );
    configManager.tps.set( basePath + ".Y", loc.getY() );
    configManager.tps.set( basePath + ".Z", loc.getZ() );
    configManager.tps.set( basePath + ".Yaw", loc.getYaw() );
    configManager.tps.set( basePath + ".Pitch", loc.getPitch() );
    if ( loc.getWorld() != null ) {
      configManager.tps.set( basePath + ".World", loc.getWorld().getName() );
    }
    configManager.saveOTPFile();
  }

  public Map<String, Object> getOTP(
      @Nonnull Player player
  ) {
    String path = "tps." + player.getUniqueId().toString();
    if ( configManager.tps.getConfigurationSection( path ) == null )
      return new HashMap<>();

    return configManager.tps.getConfigurationSection( path ).getValues( false );
  }

  public Boolean setType(
      @Nonnull Player player,
      @Nonnull String type
  ) {
    EffectType effectType = EffectType.fromString( type );
    if ( effectType == EffectType.IGNIS && !type.equalsIgnoreCase( "ignis" ) )
      return false;

    configManager.tpl.set( player.getUniqueId().toString() + ".type", type );
    configManager.saveTPLFile();

    return true;
  }

  public String getType(
      @Nonnull Player player
  ) {
    String type = "";
    if ( configManager.tpl.contains( player.getUniqueId().toString() + ".type" ) )
      type = configManager.tpl.getString( player.getUniqueId().toString() + ".type" );
    return type;
  }

  public void sendOTP(
      @Nonnull Player player,
      @Nonnull String name
  ) {
    Location tpLoc = getOTPLocation( player, name );
    if ( tpLoc == null || tpLoc.getWorld() == null )
      return;

    World world = tpLoc.getWorld();
    world.playSound( tpLoc, Sound.BLOCK_PORTAL_TRAVEL, 1, 1 );

    var sendTo = LocationUtils.getNearbyPlayers( tpLoc, 15 );
    for ( Player sender : sendTo )
      sender.sendMessage( ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD
          + " [Niedaleko słychać odgłos teleportacji]" );

    player.teleport( tpLoc );
  }

  public void sendOTP(
      @Nonnull Player player,
      @Nonnull String name,
      @Nonnull Player target
  ) {
    Location tpLoc = getOTPLocation( player, name );
    if ( tpLoc == null || tpLoc.getWorld() == null )
      return;

    World world = tpLoc.getWorld();
    world.playSound( tpLoc, Sound.BLOCK_PORTAL_TRAVEL, 1, 1 );
    var sendTo = LocationUtils.getNearbyPlayers( tpLoc, 15 );
    for ( Player sender : sendTo )
      sender.sendMessage( ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD
          + " [Niedaleko słychać odgłos teleportacji]" );

    target.teleport( tpLoc );
  }

  public void sendOTP(
      @Nonnull Player player,
      @Nonnull Location loc
  ) {
    if ( loc.getWorld() == null )
      return;

    World world = loc.getWorld();
    world.playSound( loc, Sound.BLOCK_PORTAL_TRAVEL, 1, 1 );

    var sendTo = LocationUtils.getNearbyPlayers( loc, 15 );
    for ( Player sender : sendTo )
      sender.sendMessage( ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD
          + " [Niedaleko słychać odgłos teleportacji]" );

    player.teleport( loc );
  }

  @Nullable
  public Location getOTPLocation(
      @Nonnull Player player,
      @Nonnull String name
  ) {
    String uuid = player.getUniqueId().toString();
    String basePath = "tps." + uuid + "." + name;

    String worldName = configManager.tps.getString( basePath + ".World" );
    if ( worldName == null )
      return null;

    World world = Bukkit.getWorld( worldName );
    if ( world == null )
      return null;

    double x = configManager.tps.getDouble( basePath + ".X" );
    double y = configManager.tps.getDouble( basePath + ".Y" );
    double z = configManager.tps.getDouble( basePath + ".Z" );
    float yaw = (float) configManager.tps.getDouble( basePath + ".Yaw" );
    float pitch = (float) configManager.tps.getDouble( basePath + ".Pitch" );

    return new Location( world, x, y, z, yaw, pitch );
  }

  public void deleteOTP(
      @Nonnull Player player,
      @Nonnull String name
  ) {
    configManager.tps.set( "tps." + player.getUniqueId().toString() + "." + name, null );
    configManager.saveOTPFile();
  }

  public boolean tpsIsNull(
      @Nonnull Player player,
      @Nonnull String name
  ) {
    return configManager.tps.getString( "tps." + player.getUniqueId() + "." + name ) == null;
  }

  public void tpEffect(
      @Nonnull Player player,
      @Nullable String locName,
      @Nullable Player target
  ) {
    executeTP( player, locName, target );
  }

  public void tpEffect(
      @Nonnull Player player,
      @Nullable String locName
  ) {
    executeTP( player, locName, null );
  }

  public void tpEffect(
      @Nonnull Player player,
      @Nonnull Location loc
  ) {
    executeTP( player, loc );
  }

  private void executeTP(
      @Nonnull Player player,
      @Nullable String locName,
      @Nullable Player target
  ) {
    Player tpd = target != null ? target : player;

    String typeName = getType( player );
    var tpParticles = effectUtils.getPlayerEffect( tpd, typeName );
    tpParticles.duration = 4 * 20;
    tpParticles.callback = () -> {
      if ( locName != null && !locName.isEmpty() ) {
        if ( target != null ) {
          sendOTP( player, locName, target );
          tpEffect( player, null, target );
        } else {
          sendOTP( player, locName );
          tpEffect( player, null, null );
        }
      }
    };
    if ( tpd.getWorld() != null ) {
      tpd.getWorld().playSound( tpd.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1 );
    }

    tpParticles.start();
  }

  private void executeTP(
      @Nonnull Player player,
      @Nonnull Location loc
  ) {
    String typeName = getType( player );
    var tpParticles = effectUtils.getPlayerEffect( player, typeName );
    tpParticles.duration = 4 * 20;
    tpParticles.callback = () -> {
      sendOTP( player, loc );
      tpEffect( player, null, null );
    };
    if ( player.getWorld() != null ) {
      player.getWorld().playSound( player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1 );
    }

    tpParticles.start();
  }

  public Boolean itemTP(
      @Nonnull Player player,
      @Nonnull NBTCompound comp
  ) {
    int maxLen = comp.getInteger( "maxLength" );
    int enchCld = comp.getInteger( "cld" );
    int maxUse = comp.hasKey( "maxUse" ) ? comp.getInteger( "maxUse" ) : Integer.MAX_VALUE;
    int used = comp.hasKey( "used" ) ? comp.getInteger( "used" ) : 0;

    double x = comp.getDouble( "x" );
    double y = comp.getDouble( "y" );
    double z = comp.getDouble( "z" );
    float yaw = comp.getFloat( "yaw" );
    float pitch = comp.getFloat( "pitch" );

    if ( player.getWorld() == null )
      return false;

    Location loc = new Location( player.getWorld(), x, y, z, yaw, pitch );

    if ( used >= maxUse ) {
      player.sendMessage( Main.prefixError + "Nie można już użyć przedmiotu!" );
      return false;
    }

    if ( player.getLocation().distance( loc ) > maxLen ) {
      player.sendMessage( Main.prefixError + "Nie można użyć przedmiotu, za daleko!" );
      return false;
    }

    if ( cooldownUtils.hasTpCooldown( player ) ) {
      player.sendMessage( Main.prefixError + "Musisz odpocząć " + ChatColor.RED
          + cooldownUtils.getCooldownTime( player ) + ChatColor.GRAY + " sekund." );
      return false;
    }

    if ( comp.hasKey( "maxUse" ) ) {
      used++;
      comp.setInteger( "used", used );
    }

    tpEffect( player, loc );
    cooldownUtils.setCoolDownTimeOTP( player, enchCld, false );
    player.sendMessage( Main.prefixInfo + "Teleportowano do punktu z przedmiotu!" );

    return true;
  }
}
