package net.woolf.bella.events;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.woolf.bella.Main;

public class BellaEvents implements Listener {
	
	private Main plugin;
	
	public BellaEvents(Main main) {
		this.plugin = main;
	}
	
	
    @EventHandler
    public void onPlayerJoin( PlayerJoinEvent event ) {
    	List<Player> online = plugin.utils.getPlayers();
    	plugin.bot.updatePresence("Graczy online: " + ( online.size() + 1 ));
    }
    
    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event ) {
    	List<Player> online = plugin.utils.getPlayers();
    	plugin.bot.updatePresence("Graczy online: " +  ( online.size() - 1 ));
    }
    
}
