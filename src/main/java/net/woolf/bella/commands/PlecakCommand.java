package net.woolf.bella.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import Types.BackpackNBTKeys;
import Types.Permissions;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.PlayerUtils;

public class PlecakCommand implements CommandExecutor {

	private final Main plugin = Main.getInstance();

	public PlecakCommand() {
		plugin.getCommand( "plecak" ).setExecutor( this );
	}

	@Override
	public boolean onCommand(
			CommandSender sender,
			Command cmd,
			String label,
			String[] args
	) {
		if ( sender.hasPermission( Permissions.ADMIN.toString() ) == false )
			return true;

		if ( sender instanceof Player ) {
			Player player = (Player) sender;

			ItemStack heldItem = player.getInventory().getItemInMainHand();

			if (
				heldItem == null || heldItem.getType().isItem() == false
						|| heldItem.getType() == Material.AIR
			) {
				player.sendMessage( Main.prefixError
						+ "Musisz trzymać item aby zrobić w nim ekwipunek!" );
				return true;
			}

			if ( args.length < 1 ) {
				player.sendMessage( Main.prefixError + this.getUsage() );
				return true;
			}

			String action = args[0].toLowerCase();

			switch ( action ) {
				case "stworz": {
					if ( args.length < 3 ) {
						player.sendMessage( Main.prefixError + this.getUsage() );
						return true;
					}

					int rows = Integer.parseInt( args[1] );
					if ( rows < 1 || rows > 6 ) {
						player.sendMessage( Main.prefixError
								+ "Liczba rzędów musi być z przedziału <1;6>!" );
						return true;
					}

					String name = Arrays.asList( Arrays.copyOfRange( args, 2, args.length ) )
							.stream()
							.collect( Collectors.joining( " " ) );

					if ( name.length() < 3 ) {
						player.sendMessage( Main.prefixError
								+ "Nazwa plecaka musi mieć przynajmniej 3 znaki!" );
						return true;
					}

					try {
						String uuid = DbUtils.getInstance().createBackpack( name );

						setBackPackUUID( heldItem, uuid );

						player.sendMessage( Main.prefixInfo + "Stworzono plecak: " + uuid );

						addInventoryMeta( heldItem, rows );

						plugin.bot.sendLog( String
								.format( "**%s** Utworzył plecak o nazwie `%s` i uuid `%s`", player
										.getName(), name, uuid ), Bot.VariousLogId );

					} catch ( SQLException | IOException e ) {
						player.sendMessage( Main.prefixError
								+ "Coś poszło nie tak przy twożeniu plecaka!" );
						e.printStackTrace();
					}

					return true;
				}

				case "ustaw": {
					if ( args.length < 2 ) {
						player.sendMessage( Main.prefixError + this.getUsage() );
						return true;
					}

					String playerName = args[1];

					OfflinePlayer target = PlayerUtils.getOfflinePlayer( playerName );

					if ( target == null ) {
						player.sendMessage( Main.prefixError + "Nie znaleziono gracza " + playerName
								+ ", sprawdz dokładną pisownię!" );
						return true;
					}

					if ( target.isOnline() ) {
						target.getPlayer().getInventory().addItem( heldItem );

						player.sendMessage( Main.prefixInfo
								+ String.format( "Dałeś %s plecak, który trzymasz w ręce", target
										.getName() ) );
					}

					return true;
				}
			}

			return true;
		} else {
			sender.sendMessage( "Komenda tylko dla graczy!" );
			return true;
		}
	}

	public static void addInventoryMeta(
			@NotNull ItemStack item,
			@NotNull int rows
	) {
		NBTItem nbti = new NBTItem( item );
		nbti.setBoolean( BackpackNBTKeys.ISBACKPACK.toString(), true );
		nbti.setInteger( BackpackNBTKeys.ROWS.toString(), rows );

		nbti.applyNBT( item );
	}
	
	public static void setBackPackUUID(
			@NotNull ItemStack item,
			@NotNull String uuid
	) {
		NBTItem nbti = new NBTItem( item );
		nbti.setString( BackpackNBTKeys.UUID.toString(), uuid );
		nbti.applyNBT( item );
	}

	public String getUsage() {
		return "/plecak stworz <1-6> <nazwa> - tworzy z przedmiotu w ręku plecak"
				+ "\n/plecak ustaw <nick> - ustawia komuś właściciela";
	}

}
