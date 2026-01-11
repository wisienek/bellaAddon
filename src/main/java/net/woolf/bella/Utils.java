package net.woolf.bella;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.slikey.effectlib.Effect;
import de.tr7zw.nbtapi.NBTCompound;
import net.woolf.bella.utils.LocationUtils;

@Deprecated
public class Utils {

  public HashMap<Player, Integer> cooldownTimeOTP;
  public HashMap<Player, BukkitRunnable> cooldownTaskOTP;

  public HashMap<Player, Integer> cooldownTimeSetOTP;
  public HashMap<Player, BukkitRunnable> cooldownTaskSetOTP;

  private final Main plugin;

  @Deprecated
  public static final Set<String> types = new HashSet<>(
      Arrays.asList( "ignis", "aqua", "geo", "electro", "aeter", "caligo", "lux" ) );

  public Utils(
      Main main
  ) {
    this.plugin = main;

    cooldownTimeOTP = new HashMap<>();
    cooldownTaskOTP = new HashMap<>();

    cooldownTimeSetOTP = new HashMap<>();
    cooldownTaskSetOTP = new HashMap<>();
  }

  @Deprecated
  public List<Player> getNearbyPlayers(
      Player player,
      int len
  ) {
    return LocationUtils.getNearbyPlayers( player, len );
  }

  @Deprecated
  public List<Player> getNearbyPlayers(
      Location loc,
      int len
  ) {
    return LocationUtils.getNearbyPlayers( loc, len );
  }

  public Boolean isWithinReach(
      Player player,
      Player target,
      Long len
  ) {
    return player.getLocation().distance( target.getLocation() ) < len;
  }

  public Boolean isWithinReach(
      Player player,
      String target,
      Long len
  ) {
    List<Player> playerList = player.getWorld().getPlayers();
    Player ptarget = null;

    for ( Player p : playerList )
      if ( p.getName().equals( target ) )
        ptarget = p;

    return ptarget != null && player.getLocation().distance( ptarget.getLocation() ) < len;
  }

  @Deprecated
  public void setTPL(
      Player player,
      String level
  ) {
    plugin.teleportUtils.setTPL( player, level );
  }

  @Deprecated
  public String getLevel(
      Player player
  ) {
    return plugin.teleportUtils.getLevel( player );
  }

  @Deprecated
  public void setOTP(
      Player player,
      String name
  ) {
    plugin.teleportUtils.setOTP( player, name );
  }

  @Deprecated
  public Map<String, Object> getOTP(
      Player player
  ) {
    return plugin.teleportUtils.getOTP( player );
  }

  @Deprecated
  public Boolean setType(
      Player player,
      String type
  ) {
    return plugin.teleportUtils.setType( player, type );
  }

  @Deprecated
  public String getType(
      Player player
  ) {
    return plugin.teleportUtils.getType( player );
  }

  @Deprecated
  public void sendOTP(
      Player player,
      String name
  ) {
    plugin.teleportUtils.sendOTP( player, name );
  }

  @Deprecated
  public void sendOTP(
      Player player,
      String name,
      Player target
  ) {
    plugin.teleportUtils.sendOTP( player, name, target );
  }

  @Deprecated
  public void sendOTP(
      Player player,
      Location loc
  ) {
    plugin.teleportUtils.sendOTP( player, loc );
  }

  @Deprecated
  public Location getOTPLocation(
      Player player,
      String name
  ) {
    return plugin.teleportUtils.getOTPLocation( player, name );
  }

  @Deprecated
  public void deleteOTP(
      Player player,
      String name
  ) {
    plugin.teleportUtils.deleteOTP( player, name );
  }

  @Deprecated
  public boolean tpsIsNull(
      Player player,
      String name
  ) {
    return plugin.teleportUtils.tpsIsNull( player, name );
  }

  @Deprecated
  public List<Player> getPlayers() {
    return new ArrayList<>( plugin.server.getOnlinePlayers() );
  }

  @Deprecated
  public void tpEffect(
      Player player,
      String locName,
      Player target
  ) {
    plugin.teleportUtils.tpEffect( player, locName, target );
  }

  @Deprecated
  public void tpEffect(
      Player player,
      String locName
  ) {
    plugin.teleportUtils.tpEffect( player, locName );
  }

  @Deprecated
  public void tpEffect(
      Player player,
      Location loc
  ) {
    plugin.teleportUtils.tpEffect( player, loc );
  }

  @Deprecated
  public boolean hasTpCooldown(
      Player player
  ) {
    return plugin.cooldownUtils.hasTpCooldown( player );
  }

  @Deprecated
  public Boolean itemTP(
      Player player,
      NBTCompound comp
  ) {
    return plugin.teleportUtils.itemTP( player, comp );
  }

  @Deprecated
  public Effect getPlayerEffect(
      Player player
  ) {
    String type = getType( player );
    return plugin.effectUtils.getPlayerEffect( player, type );
  }

  @Deprecated
  public void setCoolDownTimeOTP(
      Player player,
      int coolDown,
      Boolean setter
  ) {
    plugin.cooldownUtils.setCoolDownTimeOTP( player, coolDown, setter );
  }
}
