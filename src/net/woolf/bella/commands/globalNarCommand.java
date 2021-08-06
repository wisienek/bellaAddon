package net.woolf.bella.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;

public class globalNarCommand implements CommandExecutor {

	private Main plugin;
	
	public globalNarCommand(Main main) {
		this.plugin = main;

		plugin.getCommand("globalnar").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( !(sender instanceof Player) || sender.hasPermission("nar.global") ) {

			List<Player> players = sender.getServer().getWorlds().get(0).getPlayers();
			
			String msg = ChatColor.WHITE + "["+ ChatColor.LIGHT_PURPLE +"GlobalNar"+ ChatColor.WHITE +"] " + ChatColor.GREEN + String.join(" ", args);
			for( Player target : players ) {
				target.sendMessage( msg );
			}
			
			if( !(sender instanceof Player) )
				sender.sendMessage( msg );
			
			return true;
		} else {
			sender.sendMessage( Main.prefixError + "Musisz mieć permissie nar.global aby użyć tej komendy!");
			return true;
		}
	}
}
