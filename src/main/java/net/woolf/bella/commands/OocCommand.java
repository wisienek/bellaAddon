package net.woolf.bella.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;
import net.woolf.bella.utils.ChatUtils;

public class OocCommand implements CommandExecutor {

	private Main plugin;

	public OocCommand(
			Main main
	) {
		this.plugin = main;
		plugin.getCommand( "o" ).setExecutor( this );
	}

	@Override
	public boolean onCommand(
			CommandSender sender,
			Command cmd,
			String label,
			String[] args
	) {
		if ( sender instanceof Player ) {
			if ( args.length == 0 )
				return true;

			Player player = (Player) sender;

			List<Player> nearbyPlayers = plugin.utils.getNearbyPlayers( player, 15 );
			String msg = ChatColor.WHITE + "[" + ChatColor.RED + "OOC" + ChatColor.WHITE + "] "
					+ ChatColor.GRAY + player.getName() + ": (" + String.join( " ", args ) + ")";

			String logMsg = ChatUtils.OOCPrefix + " " + player.getName() + ": `("
					+ String.join( " ", args ).replaceAll( "`", "" ) + ")`";

			player.sendMessage( msg );
			for ( Player target : nearbyPlayers )
				target.sendMessage( msg );

			ChatUtils.cacheMessageForChatLog( logMsg );
			return true;
		} else {
			sender.sendMessage( "Komenda tylko dla graczy!" );
			return true;
		}
	}

}
