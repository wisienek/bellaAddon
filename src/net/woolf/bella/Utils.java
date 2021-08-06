package net.woolf.bella;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Utils {
	private Main plugin;

	public Utils(Main main) {
		this.plugin = main;
	}
	
	public Stream<Player> getNearbyPlayers( Player player, int len ) {
		Collection<Entity> nearbyPlayers = player.getWorld().getNearbyEntities( player.getLocation(), len, len, len );
		
		return nearbyPlayers.stream()
				.filter(e -> e instanceof Player && !e.getName().equals( player.getName() ) )
				.map(e-> (Player) e);
	}
	
	public void setTPL(Player player, String level) {
		plugin.tpl.set(player.getUniqueId().toString() + ".level", level);
		plugin.saveTPLFile();
	}
	public String getLevel(Player player) {
		String level = "0";
		
		if( plugin.tpl.contains(player.getUniqueId().toString() + ".level") ) {
			level = (String) plugin.tpl.get(player.getUniqueId().toString() + ".level");			
		} else {
			setTPL(player, level);
		}
		
		return level;
	}
	
    public void setOTP(Player player, String name) {
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".X", player.getLocation().getX());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Y", player.getLocation().getY());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Z", player.getLocation().getZ());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Yaw", player.getLocation().getYaw());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Pitch", player.getLocation().getPitch());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".World", player.getLocation().getWorld().getName());
        plugin.saveOTPFile();
    }
    
	public Map<String, Object> getOTP(Player player) {
		if( plugin.tps.getConfigurationSection( "tps." + player.getUniqueId().toString() ) == null )
			return new HashMap<String, Object>();
		
    	Map<String, Object> list = plugin.tps.getConfigurationSection("tps." + player.getUniqueId().toString() ).getValues( false ); 
    	return list;
    }
	
    public void sendOTP(Player player, String name) {
        player.teleport(getOTPLocation(player, name));
    }
    public void sendOTP(Player player, String name, Player target) {
        target.teleport(getOTPLocation(player, name));
    }
	
    public Location getOTPLocation(Player player, String name) {
        return new Location(
                Bukkit.getWorld(plugin.tps.getString("tps." + player.getUniqueId().toString() + "." + name + ".World")),
                plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".X"),
                plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".Y"),
                plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".Z"),
                plugin.tps.getLong("tps." + player.getUniqueId().toString() + "." + name + ".Yaw"),
                plugin.tps.getLong("tps." + player.getUniqueId().toString() + "." + name + ".Pitch")
        );
    }
    
    public void deleteOTP(Player player, String name) {
    	plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name, null);
        plugin.saveOTPFile();
    	return;
    }

    public boolean tpsIsNull(Player player, String name) {
        return plugin.tps.getString( "tps." + player.getUniqueId() + "." + name ) == null;
    }
	
}
