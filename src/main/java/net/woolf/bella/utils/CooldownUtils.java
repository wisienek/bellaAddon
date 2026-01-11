package net.woolf.bella.utils;

import java.util.HashMap;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.woolf.bella.Main;

public class CooldownUtils {

  private final Main plugin;

  private final HashMap<Player, Integer> cooldownTimeOTP;
  private final HashMap<Player, BukkitRunnable> cooldownTaskOTP;

  private final HashMap<Player, Integer> cooldownTimeSetOTP;
  private final HashMap<Player, BukkitRunnable> cooldownTaskSetOTP;

  public CooldownUtils(
      Main plugin
  ) {
    this.plugin = plugin;
    this.cooldownTimeOTP = new HashMap<>();
    this.cooldownTaskOTP = new HashMap<>();
    this.cooldownTimeSetOTP = new HashMap<>();
    this.cooldownTaskSetOTP = new HashMap<>();
  }

  public boolean hasTpCooldown(
      @Nonnull Player player
  ) {
    return cooldownTimeOTP.containsKey( player );
  }

  public boolean hasSetTpCooldown(
      @Nonnull Player player
  ) {
    return cooldownTimeSetOTP.containsKey( player );
  }

  public Integer getCooldownTime(
      @Nonnull Player player
  ) {
    return cooldownTimeOTP.get( player );
  }

  public Integer getSetCooldownTime(
      @Nonnull Player player
  ) {
    return cooldownTimeSetOTP.get( player );
  }

  public void setCoolDownTimeOTP(
      @Nonnull Player player,
      int coolDown,
      Boolean setter
  ) {
    HashMap<Player, Integer> time = ( setter ? cooldownTimeSetOTP : cooldownTimeOTP );
    HashMap<Player, BukkitRunnable> task = ( setter ? cooldownTaskSetOTP : cooldownTaskOTP );

    time.put( player, coolDown );
    task.put( player, new BukkitRunnable() {

      public void run() {
        if ( !time.containsKey( player ) && !task.containsKey( player ) ) {
          cancel();
          return;
        }

        time.put( player, time.get( player ) - 1 );
        if ( time.get( player ) == 0 ) {
          time.remove( player );
          task.remove( player );
          cancel();
        }
      }
    } );

    task.get( player ).runTaskTimer( plugin, 20, 20 );
  }

  public void clearCooldown(
      @Nonnull Player player
  ) {
    if ( cooldownTaskOTP.containsKey( player ) ) {
      cooldownTaskOTP.get( player ).cancel();
      cooldownTaskOTP.remove( player );
    }
    cooldownTimeOTP.remove( player );

    if ( cooldownTaskSetOTP.containsKey( player ) ) {
      cooldownTaskSetOTP.get( player ).cancel();
      cooldownTaskSetOTP.remove( player );
    }
    cooldownTimeSetOTP.remove( player );
  }
}
