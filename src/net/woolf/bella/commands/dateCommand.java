package net.woolf.bella.commands;

import java.util.Date;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;

public class dateCommand implements CommandExecutor {
	
	private Main plugin;

	public dateCommand(Main main) {
		this.plugin = main;
		plugin.getCommand("date").setExecutor(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( sender instanceof Player ) {
			Player player = (Player) sender;

			Date today = new Date();
			int day = today.getDate();
			int month = today.getMonth()+1;
			
			ChatColor[] colors = ChatColor.values();
			int size = colors.length;
			
			ChatColor day_color = colors[ day % size ];
			ChatColor month_color = colors[ month % size ];
			
			player.sendMessage( Main.prefixInfo + "Dzisiejsza data to: " + day_color + (day<10? "0" : "") + String.valueOf(day) + ChatColor.GRAY + "/" + month_color + (month<10? "0" : "") + String.valueOf(month)  );

			return true;
		} else {
			sender.sendMessage( "Komenda tylko dla graczy!" );
			return true;
		}
	}

}
