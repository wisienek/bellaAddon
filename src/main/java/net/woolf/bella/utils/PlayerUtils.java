package net.woolf.bella.utils;

import javax.annotation.Nonnull;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.woolf.bella.Main;


public class PlayerUtils {
	
	private Main plugin;
	
	public PlayerUtils(Main main) {
		this.plugin = main;
	}

	@SuppressWarnings("deprecation")
	public String resolveUUID( @Nonnull final String player ) {
		String uuid = null;
		
		Player target = plugin.server.getPlayer(player);
		if( target != null ) {
			uuid = target.getUniqueId().toString();
		} else {
			OfflinePlayer target1 = plugin.server.getOfflinePlayer(player);
			if( target1 != null ) uuid = target1.getUniqueId().toString();
		}
		
		return uuid;
	}
	
	public void toggleJazda( @Nonnull final Player player, @Nonnull final Boolean change ) { 
		String uuid = player.getUniqueId().toString();

		Boolean check = plugin.playerConfig.getBoolean( uuid + ".canBeRidden" );
		if( check != change ) {
			plugin.playerConfig.set( uuid + ".canBeRidden", change);
			plugin.savePlayerConfig();
		}
		
	}
	
}
