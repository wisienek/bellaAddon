package net.woolf.bella.commands;

import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;

public class moneyCommand implements CommandExecutor {

	private Main plugin;

	public moneyCommand(
			Main main
	) {
		this.plugin = main;
		plugin.getCommand( "portfel" ).setExecutor( this );
	}

	public String getUsage(
			Player player
	) {
		String add = ( player.hasPermission( "portfel.admin" ) == true )
				? "\n/portfel ustaw <komu> <typ> <ile> - ustawia graczu ileś kasy"
						+ "\n/portfel dodaj <komu> <typ> <ile> - dodaje komuś ileś kasy"
						+ "\n/portfel zabierz <gracz> <typ> <ile> - zabiera komuś ileś kasy"
						+ "\n/portfel sprawdz <gracz> - sprawdza stan portfela gracza"
				: "";

		return Main.prefixInfo + "Użycie komendy: /porfel"
				+ "\n/portfel pokaz <gracz> - pokazuje zawartość porfela graczowi"
				+ "\n/portfel daj <komu> <typ> <ile> - przekazuje graczu Ileś monet pewnego typu"
				+ add;
	}

	@Override
	public boolean onCommand(
			CommandSender sender, Command cmd, String label, String[] args
	) {
		if ( sender instanceof Player ) {
			Player player = (Player) sender;

			Map<String, Long> money = plugin.mutils.getMoney( player );

			if ( args.length == 0 ) {
				player.sendMessage( Main.prefixInfo + "Twoje pieniążki w portfelu: \n"
						+ ChatColor.RED + "Miedziaki : " + String.valueOf( money.get( "miedziak" ) )
						+ "\n" + ChatColor.GRAY + "Srebrniki : "
						+ String.valueOf( money.get( "srebrnik" ) ) + "\n" + ChatColor.YELLOW
						+ "Złotniki : " + String.valueOf( money.get( "złotnik" ) ) );
				return true;
			}

			if ( args.length < 2 ) {
				player.sendMessage( getUsage( player ) );
				return true;
			}

			String make = args[0].toLowerCase();
			String to = args[1];
			String type = args.length >= 3 ? args[2].toLowerCase() : null;
			int ammount = args.length > 3 ? Integer.valueOf( args[3] ) : 0;

			if (
				to.equals( player.getName() ) && player.getName().equals( "Przesladowca" ) == false
			) {
				player.sendMessage( Main.prefixError + "Pierwszy argument jest twoim nickiem." );
				return true;
			}

			List<Player> nearbyPlayers = plugin.utils.getNearbyPlayers( player, 4 );
			Player target = null;

			for ( Player p : nearbyPlayers )
				if ( p.getName().equals( to ) )
					target = p;

			if ( target == null ) {
				if ( player.hasPermission( "portfel.admin" ) == false ) {
					player.sendMessage( Main.prefixError + "Nie znaleziono gracza " + ChatColor.RED
							+ to + ChatColor.GRAY + " obok ciebie!" );
					return true;
				}

				target = plugin.server.getPlayer( to );
				if ( target == null || target instanceof Player == false ) {
					player.sendMessage( Main.prefixError + "Nie znaleziono gracza " + ChatColor.RED
							+ to + ChatColor.GRAY + ", aby pokazać mu portfel!" );
					return true;
				}
			}

			switch ( make ) {
				case "sprawdz": {
					if ( player.hasPermission( "portfel.admin" ) == false )
						return true;

					String uuid = plugin.putils.resolveUUID( to );
					Map<String, Long> targetMoney = plugin.mutils.getMoney( uuid );

					player.sendMessage( Main.prefixInfo + "Pieniążki gracza" + to + ": \n"
							+ ChatColor.RED + "Miedziaki : "
							+ String.valueOf( targetMoney.get( "miedziak" ) ) + "\n"
							+ ChatColor.GRAY + "Srebrniki : "
							+ String.valueOf( targetMoney.get( "srebrnik" ) ) + "\n"
							+ ChatColor.YELLOW + "Złotniki : "
							+ String.valueOf( targetMoney.get( "złotnik" ) ) );
					return true;
				}

				case "pokaz": {
					target.sendMessage( Main.prefixInfo + "Pieniążki " + ChatColor.GREEN
							+ player.getName() + " : \n" + ChatColor.WHITE + "Miedziaki : "
							+ String.valueOf( money.get( "miedziak" ) ) + "\n" + ChatColor.GRAY
							+ "Srebrniki : " + String.valueOf( money.get( "srebrnik" ) ) + "\n"
							+ ChatColor.YELLOW + "Złotniki : "
							+ String.valueOf( money.get( "złotnik" ) ) );

					player.sendMessage( Main.prefixInfo + "Pokazano portfel dla gracza "
							+ ChatColor.GREEN + target.getName() );
					player.performCommand(
							"me Pokazał zawartość portfela [" + target.getName() + "]" );

					return true;
				}

				case "daj": {
					boolean transfered = plugin.mutils.transferMoney( player, target, type,
							Long.valueOf( ammount ) );

					if ( transfered == false ) {
						player.sendMessage( Main.prefixError
								+ "Nie udało się wykonać przelewu! (sprawdz komendę i czy jest gracz obok ciebie)" );
						player.sendMessage( getUsage( player ) );
						return true;
					}

					target.sendMessage( Main.prefixInfo + "Otrzymałeś " + ChatColor.YELLOW + ammount
							+ " " + type + "ów" + ChatColor.GRAY + " od " + player.getName() );
					player.performCommand(
							"me Przekazał parę pieniążków [" + target.getName() + "]" );

					money = plugin.mutils.getMoney( player );
					Map<String, Long> toMoney = plugin.mutils.getMoney( target );

					plugin.bot.moneyLog( "**" + player.getName() + "** przekazał *" + ammount + " "
							+ type + "ów* dla gracza **" + to + "**" + "\n" + player.getName()
							+ " ma teraz: " + money.get( type ) + " " + type + "\n"
							+ target.getName() + " ma teraz: " + toMoney.get( type ) + " " + type );

					return true;
				}

				case "ustaw": {
					if ( player.hasPermission( "portfel.admin" ) == false ) {
						player.sendMessage( getUsage( player ) );
						return true;
					}

					String uuid = plugin.putils.resolveUUID( to );

					Boolean done = plugin.mutils.setMoney( uuid, type, Long.valueOf( ammount ) );
					if ( done == false ) {
						player.sendMessage( Main.prefixError
								+ "Błąd podczas setowania, sprawdz pisownie i czy gracz jest online." );
						return true;
					}

					player.sendMessage( Main.prefixInfo + "Ustawiono stan portfela "
							+ ChatColor.GREEN + to + ChatColor.WHITE + " na " + ChatColor.YELLOW
							+ ammount + " " + type + "ów" );

					plugin.bot.moneyLog( "**" + player.getName() + "** Ustawił *" + type
							+ "i* w portfelu gracza **" + to + "** na " + ammount );

					return true;
				}

				case "dodaj": {
					if ( player.hasPermission( "portfel.admin" ) == false ) {
						player.sendMessage( getUsage( player ) );
						return true;
					}

					String uuid = plugin.putils.resolveUUID( to );
					Map<String, Long> pm = plugin.mutils.getMoney( uuid );

					Boolean done = plugin.mutils.setMoney( uuid, type,
							Long.valueOf( ammount ) + pm.get( type ) );
					if ( done == false ) {
						player.sendMessage( Main.prefixError
								+ "Błąd podczas dodawania, sprawdz pisownie i czy gracz jest online." );
						return true;
					}

					player.sendMessage( Main.prefixInfo + "Dodano " + ChatColor.YELLOW + ammount
							+ " " + type + "ów" + ChatColor.WHITE + " do konta " + ChatColor.GREEN
							+ to + ChatColor.WHITE + ", teraz ma: " + ChatColor.YELLOW
							+ Long.valueOf( ammount ) + pm.get( type ) );

					plugin.bot.moneyLog( "**" + player.getName() + "** Dodał *" + ammount + " "
							+ type + "ów* do portfela gracza **" + to + "**" + "\n" + "Teraz ma: "
							+ String.valueOf( Long.valueOf( ammount ) + pm.get( type ) ) + type
							+ "ów" );

					return true;
				}

				case "zabierz": {
					if ( player.hasPermission( "portfel.admin" ) == false ) {
						player.sendMessage( getUsage( player ) );
						return true;
					}

					String uuid = plugin.putils.resolveUUID( to );
					Map<String, Long> pm = plugin.mutils.getMoney( uuid );

					if ( pm.get( type ) - ammount < 0 ) {
						player.sendMessage( Main.prefixError
								+ "Nie można zabrać tyle kasy, gracz będzie na minusie! "
								+ ChatColor.RED + pm.get( type ) + " -> "
								+ ( pm.get( type ) - ammount ) );
						return true;
					}

					Boolean done = plugin.mutils.setMoney( uuid, type,
							pm.get( type ) - Long.valueOf( ammount ) );
					if ( done == false ) {
						player.sendMessage( Main.prefixError
								+ "Błąd podczas zabierania, sprawdz pisownie i czy gracz jest online." );
						return true;
					}

					player.sendMessage( Main.prefixInfo + "Zabrano " + ChatColor.YELLOW + ammount
							+ " " + type + "ów" + ChatColor.WHITE + " z konta gracza "
							+ ChatColor.GREEN + to + ChatColor.WHITE + ", Teraz ma: "
							+ ChatColor.YELLOW + ( pm.get( type ) - Long.valueOf( ammount ) ) );

					plugin.bot.moneyLog( "**" + player.getName() + "** Zabrał *" + ammount + " "
							+ type + "ów* z portfela gracza **" + to + "**" + "\n" + "Teraz ma: "
							+ String.valueOf( pm.get( type ) - Long.valueOf( ammount ) ) + type
							+ "ów" );

					return true;
				}

				default: {
					player.sendMessage( getUsage( player ) );
					return true;
				}
			}
		} else {
			sender.sendMessage( "Komenda tylko dla graczy!" );
			return true;
		}
	}

}
