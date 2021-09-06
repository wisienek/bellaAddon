package net.woolf.bella.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;

public class moneyCommand implements CommandExecutor {
	
	private Main plugin;

	public moneyCommand(Main main) {
		this.plugin = main;
		plugin.getCommand("portfel").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( sender instanceof Player ) {
			if( args.length == 0 ) {
				// wyświetl status
				
				return true;
			}
			
			Player player = (Player) sender;			
			// List<Player> nearbyPlayers = plugin.utils.getNearbyPlayers( player, 15 );

			
			
			
			
			
			return true;
		} else {
			sender.sendMessage( "Komenda tylko dla graczy!" );
			return true;
		}
	}

}
