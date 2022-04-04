package net.woolf.bella.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.woolf.bella.Main;

public class linkCommand implements CommandExecutor {

	private Main plugin;
	
	public linkCommand(Main main) {
		this.plugin = main;
		plugin.getCommand("jazda").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( sender instanceof Player ) {
		
			
			
			sender.sendMessage( Main.prefixInfo + "Tutaj będzie link do skopiowania!" );
			return true;
		}
		
		sender.sendMessage( Main.prefixInfo + this.getUsage() );
		return true; 
	}
	
	
	
	private String getUsage() {
		return "/link";
	}
	
}
