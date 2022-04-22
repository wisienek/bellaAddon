package net.woolf.bella.events;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
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
		
//		String newMsg = ChatUtils.formatDOaction(msg);
//		newMsg = ChatUtils.formatMEaction(newMsg);
//		String newMsg = ChatUtils.formatOOC(msg);
		String newMsg = ChatUtils.formatEmojis(msg);
		
		// Send chat
		event.setMessage( newMsg );
		
		ChatUtils.cacheMessageForChatLog(
			ChatUtils.LocalPrefix + 
			" [" + event.getPlayer().getDisplayName() + "] " + 
			event.getPlayer().getName() + 
			": `" + newMsg.replaceAll("(§.)|(`)", "") + "`" 
		);
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
    public void onPlayerInteract( PlayerInteractEvent event ) {
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
    public void onPlayerInteractEntity( PlayerInteractEntityEvent event ) {
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

    @EventHandler
    public void onPlayerItemDamageEvent( PlayerItemDamageEvent event ) {
    	event.setCancelled(true);
    }

    @EventHandler
    public boolean onPlayerCommandPreprocessEvent( PlayerCommandPreprocessEvent event ) {
    	Player player = event.getPlayer();
    	
    	List<String> args = new LinkedList<String>();
    	Collections.addAll(args, event.getMessage().split(" "));
    	
    	String cmd = args.get(0).replace("/", "");
    	
    	args.remove(0);
    	
    	switch (cmd) { 
	    	case "ooc": {
				ChatUtils.cacheMessageForChatLog( 
						ChatUtils.OOCPrefix + " " +
						player.getName() + ": `(" + 
						String.join(" ", args).replaceAll("`", "") + ")`"
					);
				break;
	    	}
	    	case "me": case "k": {
	    		ChatUtils.cacheMessageForChatLog( 
						ChatUtils.LocalPrefix + 
						" [" + player.getDisplayName() + "] " +
						player.getName() + ": `*" + 
						String.join(" ", args).replaceAll("`", "") + "*`"
					);
	    		break;
	    	}
	    	case "do": {
	    		ChatUtils.cacheMessageForChatLog( 
	    				ChatUtils.LocalPrefix + 
						" [" + player.getDisplayName() + "] " +
						player.getName() + ": `**" + 
						String.join(" ", args).replaceAll("`", "") + "**`"
					);
	    		break;
	    	}
	    	case "s": {
	    		ChatUtils.cacheMessageForChatLog( 
	    				ChatUtils.WhisperPrefix + 
						" [" + player.getDisplayName() + "] " +
						player.getName() + ": `" + 
						String.join(" ", args).replaceAll("`", "") + "`"
					);
	    		break;
	    	}
	    	case "globalnar": {
	    		ChatUtils.cacheMessageForChatLog( 
	    				ChatUtils.GlobalPrefix + 
						" [" + player.getName() + "] `" +
						String.join(" ", args).replaceAll("`", "") + "`"
					);
	    		break;
	    	}
	    	case "midnar": case "localnar": {
	    		Location loc = player.getLocation();
	    		ChatUtils.cacheMessageForChatLog( 
	    				ChatUtils.LocalPrefix + " {" +
	    				loc.getBlockX() + " " +
	    				loc.getBlockY() + " " + 
	    				loc.getBlockZ() + "} " +
						" [" + player.getName() + "] `" +
						String.join(" ", args).replaceAll("`", "") + "`"
					);
	    		break;
	    	}
	    	case "privnar": {
	    		String narrated = args.get(0);
	    		args.remove(0);
	    		
	    		ChatUtils.cacheMessageForChatLog(
	    				"**[PRIVNAR]** " +
						"[" + player.getName() + " -> " + narrated + "] `" +
						String.join(" ", args).replaceAll("`", "") + "`"
					);
	    		break;
	    	}
    	}
    	
    	return true;
    }
}
