package net.woolf.bella.events;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
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
    
    @EventHandler(priority=EventPriority.HIGH)
    public void onPlayerUse(PlayerInteractEvent event){
        Player player = event.getPlayer();
     
        ItemStack item = player.getInventory().getItemInMainHand();
        if( item != null && item.getType() != Material.AIR ) {
			NBTItem nbti = new NBTItem(item, true);

			if( nbti.hasKey("teleportEnchantment") ) {
				event.setCancelled(true);
				
				NBTCompound comp = nbti.getCompound("teleportEnchantment");
				plugin.utils.itemTP(player, comp);
				
			}
        }
    }

}
