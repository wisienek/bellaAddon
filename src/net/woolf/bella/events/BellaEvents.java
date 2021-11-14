package net.woolf.bella.events;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;
import net.woolf.bella.utils.ChatUtils;

public class BellaEvents implements Listener {
		
	private Main plugin;
	
	public BellaEvents(Main main) {
		this.plugin = main;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerChat( AsyncPlayerChatEvent event ) {
		String msg = event.getMessage();
		// add more
		
		//String newMsg = ChatUtils.formatDOaction(msg);
		//newMsg = ChatUtils.formatMEaction(newMsg);
		String newMsg = ChatUtils.formatOOC(msg);
		newMsg = ChatUtils.formatEmojis(newMsg);
		
		event.setMessage( newMsg );
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
    public void onPlayerInteract(PlayerInteractEvent event) {
    	Player player = event.getPlayer();
    	
        if( player.isSneaking() ) {
        	List<Entity> passangers = player.getPassengers();
        	
        	if( passangers.size() > 0 ) 
        		for( Entity Passanger : passangers )
        			player.removePassenger(Passanger);
        }
        
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
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    	if(event.getHand().equals(EquipmentSlot.HAND) == false ) return;
    	
    	Entity clicked = event.getRightClicked();
    	Player player = event.getPlayer();
    	
    	if( clicked instanceof Player) {
    		Player target = (Player) clicked;
    		
    		Boolean check = plugin.playerConfig.getBoolean( target.getUniqueId().toString() + ".canBeRidden" );
    		
    		if( check == true ) {
    			List<Entity> passangers = target.getPassengers();
    			
    			if( passangers.size() == 0 )
    				target.addPassenger(player);
    		}
    	}
    }
}
