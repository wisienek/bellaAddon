package net.woolf.bella.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LocationUtils {

  public static List<Player> getNearbyPlayers(
      @Nonnull Player player,
      int len
  ) {
    Collection<Entity> nearbyPlayers = player.getWorld()
        .getNearbyEntities( player.getLocation(), len, len, len );

    return nearbyPlayers.stream()
        .filter( e -> e instanceof Player && !e.getName().equals( player.getName() ) )
        .map( Player.class::cast )
        .collect( Collectors.toList() );
  }

  public static List<Player> getNearbyPlayers(
      @Nonnull Location loc,
      int len
  ) {
    if ( loc.getWorld() == null )
      return List.of();

    Collection<Entity> nearbyPlayers = loc.getWorld().getNearbyEntities( loc, len, len, len );

    return nearbyPlayers.stream()
        .filter( Player.class::isInstance )
        .map( Player.class::cast )
        .collect( Collectors.toList() );
  }

  public static Boolean isWithinReach(
      @Nonnull Player player,
      @Nonnull Player target,
      @Nonnull Long len
  ) {
    return player.getLocation().distance( target.getLocation() ) < len;
  }

  public static Boolean isWithinReach(
      @Nonnull Player player,
      @Nonnull String target,
      @Nonnull Long len
  ) {
    List<Player> playerList = player.getWorld().getPlayers();
    Player ptarget = null;

    for ( Player p : playerList )
      if ( p.getName().equals( target ) )
        ptarget = p;

    return ptarget != null && player.getLocation().distance( ptarget.getLocation() ) < len;
  }

  public static String formatLocation(
      @Nonnull Location loc
  ) {
    return String.format( "%s %s %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() );
  }
}
