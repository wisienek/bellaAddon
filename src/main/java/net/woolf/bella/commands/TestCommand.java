package net.woolf.bella.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import Types.Permissions;
import net.woolf.bella.Main;
import net.woolf.bella.utils.DbUtils;

public class TestCommand implements CommandExecutor {

	public static final String Permission = "bella.test";

	private Main plugin;
	private DbUtils dbInstance;

	public TestCommand() {
		this.plugin = Main.getInstance();

		this.plugin.getCommand( "test" ).setExecutor( this );
	}

	@Override
	public boolean onCommand(
			CommandSender sender,
			Command cmd,
			String alias,
			String[] args
	) {
		if ( !sender.hasPermission( Permissions.TEST.toString() ) )
			return true;

		Player player = (Player) sender;

		String opt = args[0] != null ? args[0] : null;
		if ( opt == null )
			return true;

		switch ( opt ) {
			case "db": {
				try {
					if ( this.dbInstance == null )
						this.dbInstance = DbUtils.getInstance();

					Map<String, String[]> accounts = this.dbInstance
							.getConnectedAccounts( player.getUniqueId().toString() );

					if ( accounts != null )
						player.sendMessage( Main.prefixInfo + accounts.toString() );
					else
						player.sendMessage( Main.prefixInfo + "Null accounts!" );

				} catch ( SQLException | IOException e ) {
					player.sendMessage( Main.prefixError + "Wyjebało coś w kosmos!" );
					e.printStackTrace();
				}
				return true;
			}

			case "player": {
				final String uuid = "936d0ee1-a793-34e9-876c-c9ae4b87e740";
				Player searched = Main.getInstance().putils.resolveUUIDToOnlinePlayer( uuid );
				if ( searched == null ) {
					player.sendMessage( Main.prefixInfo + "Nie znaleziono gracza!" );
					return true;
				}

				player.sendMessage( Main.prefixInfo + "Znaleziono gracza: "
						+ searched.getPlayerListName() );

				return true;
			}

			default:
				sender.sendMessage( "nah, fam" );
		}

		return true;
	}
}
