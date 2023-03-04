package net.woolf.bella;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.effect.DonutEffect;
import de.slikey.effectlib.effect.EarthEffect;
import de.slikey.effectlib.effect.FountainEffect;
import de.slikey.effectlib.effect.StarEffect;
import de.slikey.effectlib.effect.TornadoEffect;
import de.slikey.effectlib.effect.VortexEffect;
import de.tr7zw.nbtapi.NBTCompound;
import net.md_5.bungee.api.ChatColor;

public class Utils {

  public HashMap<Player, Integer> cooldownTimeOTP;
  public HashMap<Player, BukkitRunnable> cooldownTaskOTP;

  public HashMap<Player, Integer> cooldownTimeSetOTP;
  public HashMap<Player, BukkitRunnable> cooldownTaskSetOTP;

  private final Main plugin;

  public static final Set<String> types = new HashSet<>(
      Arrays.asList("ignis", "aqua", "geo", "electro", "aeter", "caligo", "lux"));

  public Utils (
      Main main
  ) {
    this.plugin = main;

    cooldownTimeOTP = new HashMap<>();
    cooldownTaskOTP = new HashMap<>();

    cooldownTimeSetOTP = new HashMap<>();
    cooldownTaskSetOTP = new HashMap<>();
  }

  public List<Player> getNearbyPlayers (
      Player player, int len
  ) {
    Collection<Entity> nearbyPlayers = player.getWorld().getNearbyEntities(player.getLocation(), len, len, len);

    return nearbyPlayers.stream()
                        .filter(e -> e instanceof Player && !e.getName().equals(player.getName()))
                        .map(e -> (Player) e)
                        .collect(Collectors.toList());
  }

  public List<Player> getNearbyPlayers (
      Location loc, int len
  ) {
    Collection<Entity> nearbyPlayers = loc.getWorld().getNearbyEntities(loc, len, len, len);

    return nearbyPlayers.stream().filter(e -> e instanceof Player).map(e -> (Player) e).collect(Collectors.toList());
  }

  public Boolean isWithinReach (
      Player player, Player target, Long len
  ) {
    return player.getLocation().distance(target.getLocation()) < len;
  }

  public Boolean isWithinReach (
      Player player, String target, Long len
  ) {
    List<Player> playerList = player.getWorld().getPlayers();
    Player ptarget = null;

    for ( Player p : playerList )
      if ( p.getName().equals(target) ) ptarget = p;

    return ptarget != null && player.getLocation().distance(ptarget.getLocation()) < len;
  }

  public void setTPL (
      Player player, String level
  ) {
    plugin.tpl.set(player.getUniqueId().toString() + ".level", level);
    plugin.saveTPLFile();
  }

  public String getLevel (
      Player player
  ) {
    String level = "0";

    if ( plugin.tpl.contains(player.getUniqueId().toString() + ".level") ) {
      level = (String) plugin.tpl.get(player.getUniqueId().toString() + ".level");
    }
    else {
      setTPL(player, level);
    }

    return level;
  }

  public void setOTP (
      Player player, String name
  ) {
    plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".X", player.getLocation().getX());
    plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Y", player.getLocation().getY());
    plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Z", player.getLocation().getZ());
    plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Yaw", player.getLocation().getYaw());
    plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Pitch", player.getLocation().getPitch());
    plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".World",
        player.getLocation().getWorld().getName());
    plugin.saveOTPFile();
  }

  public Map<String, Object> getOTP (
      Player player
  ) {
    if ( plugin.tps.getConfigurationSection("tps." + player.getUniqueId().toString()) == null ) return new HashMap<>();

    return plugin.tps.getConfigurationSection("tps." + player.getUniqueId().toString()).getValues(false);
  }

  public Boolean setType (
      Player player, String type
  ) {
    if ( !types.contains(type) ) return false;

    plugin.tpl.set(player.getUniqueId().toString() + ".type", type);
    plugin.saveTPLFile();

    return true;
  }

  public String getType (
      Player player
  ) {
    String type = "";
    if ( plugin.tpl.contains(player.getUniqueId().toString() + ".type") )
      type = plugin.tpl.getString(player.getUniqueId().toString() + ".type");
    return type;
  }

  public Boolean sendOTP (
      Player player, String name
  ) {
    World world = player.getWorld();
    Location tpLoc = getOTPLocation(player, name);

    world.playSound(tpLoc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);

    List<Player> sendTo = getNearbyPlayers(tpLoc, 15);
    for ( Player sender : sendTo )
      sender.sendMessage(ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD + " [Niedaleko słychać odgłos teleportacji]");

    return player.teleport(tpLoc);
  }

  public Boolean sendOTP (
      Player player, String name, Player target
  ) {
    World world = player.getWorld();
    Location tpLoc = getOTPLocation(player, name);

    world.playSound(tpLoc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
    List<Player> sendTo = getNearbyPlayers(tpLoc, 15);
    for ( Player sender : sendTo )
      sender.sendMessage(ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD + " [Niedaleko słychać odgłos teleportacji]");

    return target.teleport(tpLoc);
  }

  public Boolean sendOTP (
      Player player, Location loc
  ) {
    World world = player.getWorld();

    world.playSound(loc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);

    List<Player> sendTo = getNearbyPlayers(loc, 15);
    for ( Player sender : sendTo )
      sender.sendMessage(ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD + " [Niedaleko słychać odgłos teleportacji]");

    return player.teleport(loc);
  }

  public Location getOTPLocation (
      Player player, String name
  ) {
    return new Location(
        Bukkit.getWorld(plugin.tps.getString("tps." + player.getUniqueId().toString() + "." + name + ".World")),
        plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".X"),
        plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".Y"),
        plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".Z"),
        plugin.tps.getLong("tps." + player.getUniqueId().toString() + "." + name + ".Yaw"),
        plugin.tps.getLong("tps." + player.getUniqueId().toString() + "." + name + ".Pitch"));
  }

  public void deleteOTP (
      Player player, String name
  ) {
    plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name, null);
    plugin.saveOTPFile();
  }

  public boolean tpsIsNull (
      Player player, String name
  ) {
    return plugin.tps.getString("tps." + player.getUniqueId() + "." + name) == null;
  }

  public List<Player> getPlayers () {
    return plugin.server.getWorlds().get(0).getPlayers();
  }

  public void tpEffect (
      Player player, String locName, Player target
  ) {
    executeTP(player, locName, target);
  }

  public void tpEffect (
      Player player, String locName
  ) {
    executeTP(player, locName, null);
  }

  public void tpEffect (
      Player player, Location loc
  ) {
    executeTP(player, loc);
  }

  void executeTP (
      Player player, String locName, Player target
  ) {
    Player tpd = target != null ? target : player;

    Effect tpParticles = getPlayerEffect(tpd);
    tpParticles.duration = 4 * 20;
    tpParticles.callback = () -> {
      if ( locName != null && !locName.isEmpty() ) {
        if ( target != null ) {
          sendOTP(player, locName, target);
          tpEffect(player, null, target);
        }
        else {
          sendOTP(player, locName);
          tpEffect(player, null, null);
        }
      }
    };
    player.getWorld().playSound(tpd.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);

    tpParticles.start();
  }

  void executeTP (
      Player player, Location loc
  ) {
    Effect tpParticles = getPlayerEffect(player);
    tpParticles.duration = 4 * 20;
    tpParticles.callback = () -> {
      sendOTP(player, loc);
      tpEffect(player, null, null);
    };
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);

    tpParticles.start();
  }

  public boolean hasTpCooldown (
      Player player
  ) {
    return cooldownTimeOTP.containsKey(player);
  }

  public void itemTP (
      Player player, NBTCompound comp
  ) {
    // String enchanter = comp.getString("enchanter");
    int maxLen = comp.getInteger("maxLength");
    int enchCld = comp.getInteger("cld");
    int maxUse = comp.hasKey("maxUse") ? comp.getInteger("maxUse") : Integer.MAX_VALUE;
    int used = comp.hasKey("used") ? comp.getInteger("used") : 0;
    // location
    double x = comp.getDouble("x");
    double y = comp.getDouble("y");
    double z = comp.getDouble("z");
    float yaw = comp.getFloat("yaw");
    float pitch = comp.getFloat("pitch");

    Location loc = new Location(player.getWorld(), x, y, z, yaw, pitch);

    if ( used >= maxUse ) {
      player.sendMessage(Main.prefixError + "Nie można już użyć przedmiotu!");
      return;
    }

    if ( player.getLocation().distance(loc) > maxLen ) {
      player.sendMessage(Main.prefixError + "Nie można użyć przedmiotu, za daleko!");
      return;
    }

    if ( hasTpCooldown(player) ) {
      player.sendMessage(Main.prefixError + "Musisz odpocząć " + ChatColor.RED + cooldownTimeOTP.get(
          player) + ChatColor.GRAY + " sekund.");
    }
    else {
      if ( comp.hasKey("maxUse") ) {
        used++;
        comp.setInteger("used", used);
      }

      tpEffect(player, loc);
      setCoolDownTimeOTP(player, enchCld, false);
      player.sendMessage(Main.prefixInfo + "Teleportowano do punktu z przedmiotu!");
    }
  }

  public Effect getPlayerEffect (
      Player player
  ) {
    Effect ef;
    String type = getType(player);

    switch ( type ) {
      case "ignis": {
        TornadoEffect tpParticles = new TornadoEffect(plugin.effectManager);
        tpParticles.setEntity(player);
        tpParticles.tornadoHeight = (float) 2.4;
        tpParticles.maxTornadoRadius = (float) 1.5;
        tpParticles.yOffset = -2;
        tpParticles.showCloud = false;

        return tpParticles;
      }
      case "caligo": {
        VortexEffect tpParticles = new VortexEffect(plugin.effectManager);
        tpParticles.setEntity(player);
        tpParticles.particle = Particle.SMOKE_LARGE;
        tpParticles.radius = (float) 1.5;
        tpParticles.radials = 30;
        tpParticles.circles = 30;
        tpParticles.helixes = 30;

        return tpParticles;
      }
      case "lux": {
        DonutEffect tpParticles = new DonutEffect(plugin.effectManager);
        tpParticles.setEntity(player);
        tpParticles.radiusDonut = (float) 1.8;
        tpParticles.particle = Particle.TOTEM;

        return tpParticles;
      }
      case "aeter": {
        TornadoEffect tpParticles = new TornadoEffect(plugin.effectManager);
        tpParticles.setEntity(player);
        tpParticles.tornadoHeight = (float) 2.4;
        tpParticles.maxTornadoRadius = (float) 1.5;
        tpParticles.yOffset = -1;
        tpParticles.showTornado = false;

        return tpParticles;
      }
      case "aqua": {
        FountainEffect tpParticles = new FountainEffect(plugin.effectManager);
        tpParticles.setEntity(player);
        tpParticles.radius = 2;
        tpParticles.height = 2;
        tpParticles.radiusSpout = 1;
        tpParticles.heightSpout = 2;

        return tpParticles;
      }
      case "geo": {
        EarthEffect tpParticles = new EarthEffect(plugin.effectManager);
        tpParticles.setEntity(player);
        tpParticles.radius = (float) 1.5;

        return tpParticles;
      }
      case "electro": {
        StarEffect tpParticles = new StarEffect(plugin.effectManager);
        tpParticles.setEntity(player);
        tpParticles.innerRadius = (float) 0.5;
        tpParticles.spikeHeight = (float) 2;
        tpParticles.particle = Particle.SPELL_WITCH;
        tpParticles.color = Color.PURPLE;

        return tpParticles;
      }
      default: {
        TornadoEffect tpParticles = new TornadoEffect(plugin.effectManager);
        tpParticles.setEntity(player);
        tpParticles.tornadoHeight = (float) 2.4;
        tpParticles.maxTornadoRadius = (float) 1.5;
        tpParticles.yOffset = -2;
        tpParticles.showCloud = false;

        ef = tpParticles;
        break;
      }
    }

    return ef;
  }

  public void setCoolDownTimeOTP (
      Player player, int coolDown, Boolean setter
  ) {
    HashMap<Player, Integer> time = ( setter ? cooldownTimeSetOTP : cooldownTimeOTP );
    HashMap<Player, BukkitRunnable> task = ( setter ? cooldownTaskSetOTP : cooldownTaskOTP );

    time.put(player, coolDown);
    task.put(player, new BukkitRunnable() {
      public void run () {
        if ( !time.containsKey(player) && !task.containsKey(player) ) {
          cancel();
        }

        time.put(player, time.get(player) - 1);
        if ( time.get(player) == 0 ) {
          time.remove(player);
          task.remove(player);
          cancel();
        }
      }
    });

    task.get(player).runTaskTimer(plugin, 20, 20);
  }
}
