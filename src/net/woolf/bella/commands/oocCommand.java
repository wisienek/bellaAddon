package net.woolf.bella.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;

public class oocCommand implements CommandExecutor {
	
	private Main plugin;

	public oocCommand(Main main) {
		this.plugin = main;
		plugin.getCommand("o").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( sender instanceof Player ) {
			if( args.length == 0 ) return true;
			
			Player player = (Player) sender;
			
			List<Player> nearbyPlayers = plugin.utils.getNearbyPlayers( player, 15 ).collect( Collectors.toList() );
			String msg = ChatColor.WHITE +"["+ ChatColor.RED +"OOC"+ ChatColor.WHITE +"] "+ player.getName() + ChatColor.GRAY + ": ("+String.join(" ", args) +")";
			
			player.sendMessage(msg);
			for( Player target : nearbyPlayers ) {
				target.sendMessage(msg);
			}
		} else {
			sender.sendMessage( " Komenda tylko dla graczy!" );
		}
		return false;
	}

}
