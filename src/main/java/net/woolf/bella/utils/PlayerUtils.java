package net.woolf.bella.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.model.user.User;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;

public class PlayerUtils {

  private final Main plugin;

  public PlayerUtils (
      Main main
  ) {
    this.plugin = main;
  }

  @SuppressWarnings( "deprecation" )
  public String resolvePlayerToUUID (
      @Nonnull final String player
  ) {
    String uuid = null;

    Player target = plugin.server.getPlayer(player);
    if ( target != null ) {
      uuid = target.getUniqueId().toString();
    }
    else {
      OfflinePlayer target1 = plugin.server.getOfflinePlayer(player);
      if ( target1 != null ) uuid = target1.getUniqueId().toString();
    }

    return uuid;
  }

  public Player resolveUUIDToOnlinePlayer (
      @Nonnull final String UUID
  ) {
    Player player = plugin.server.getPlayer(UUID);
    if ( player != null ) return player;

    List<Player> players = PlayerUtils.getOnlinePlayers();
    for ( Player sp : players )
      if ( sp.getUniqueId().toString().equals(UUID) ) return sp;

    return null;
  }

  @SuppressWarnings( "deprecation" )
  public static OfflinePlayer getOfflinePlayer (
      @NotNull String uuid
  ) {
    return Main.getInstance().server.getOfflinePlayer(uuid);
  }

  public void toggleJazda (
      @Nonnull final Player player, @Nonnull final Boolean change
  ) {
    String uuid = player.getUniqueId().toString();

    boolean check = plugin.playerConfig.getBoolean(uuid + ".canBeRidden");
    if ( check != change ) {
      plugin.playerConfig.set(uuid + ".canBeRidden", change);
      plugin.savePlayerConfig();
    }

  }

  public static List<Player> getOnlinePlayers () {
    return new ArrayList<>(Main.getInstance().server.getOnlinePlayers());
  }

  public static List<Player> getPlayersWithinRange (
      @Nonnull Location location, @Nonnull Long range, @Nullable OfflinePlayer excludePlayer
  ) {
    List<Player> players = PlayerUtils.getOnlinePlayers();

    return players.stream().filter(player -> {
      if ( !( player.getLocation().distance(location) <= range ) ) return false;
      assert excludePlayer != null;
      return !player.getName().equals(excludePlayer.getName());
    }).collect(Collectors.toList());

  }

  public static Boolean playerArmourHasEffect (
      @Nonnull Player player, @Nonnull String effect
  ) {
    PlayerInventory inv = player.getInventory();
    ItemStack[] armour = inv.getArmorContents();

    for ( ItemStack singleArmour : armour ) {
      if ( singleArmour == null ) continue;

      NBTItem nbti = new NBTItem(singleArmour);
      if ( nbti.hasKey(effect) && nbti.getBoolean(effect) ) return true;
    }

    return false;
  }

  public static String getPlayerPrefix (Player player) {
    String userPrefix = "[-]";

    User user = Main.getInstance().lpApi.getUserManager().getUser(player.getUniqueId());
    if ( user != null ) {
      CachedDataManager cachedData = user.getCachedData();
      String lpPrefix = cachedData.getMetaData().getPrefix();

      if ( lpPrefix != null ) {
        userPrefix = lpPrefix;
      }
    }
    return userPrefix;
  }

}
