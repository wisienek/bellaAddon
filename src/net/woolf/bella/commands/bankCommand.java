package net.woolf.bella.commands;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;

public class bankCommand implements CommandExecutor {
	
	public static int tax = 1;
	
	private Main plugin;

	public bankCommand(Main main) {
		this.plugin = main;
		plugin.getCommand("bank").setExecutor(this);
	}
	
	public String getUsage(Player player) {
		String add = (player.hasPermission("bank.admin") == true) ? 
				"\n/bank ustaw <typ> <ile> <komu> - ustawia graczu ileś kasy"+
				"\n/bank dodaj <typ> <ile> <komu> - dodaje komuś ileś kasy"+
				"\n/bank zabierz <typ> <ile> <komu> - zabiera komuś ileś kasy"+
				"\n/bank stwórz miejsce - tworzy bank w miejscu gdzie stoisz"+
				"\n/bank usuń - usuwa bank obok którego stoisz"+
				"\n/bank konwersje <z czego> <na co> <kwota> - ile potrzebujesz 1 waluty aby zrobić konwersję na drugą"
			: "";
		
		return Main.prefixInfo + "Użycie komendy: /bank"+
					"\n/bank - pokazuje stan banku"+
					"\n/bank wpłać <typ> <ile> - Wpłaca ilość monet do banku"+
					"\n/bank wypłać <typ> <ile> - Wypłaca ilość monet z banku"+
					"\n/bank przelej <typ> <ile> <komu> - Przelewa ileś kaski dla innego gracza"+
					"\n/bank wymień <z czego> <na co> <ile chcesz wymienić> - wymienia jedną walutę na drugą"
					+ add;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( sender instanceof Player ) {
			Player player = (Player) sender;	
			
			Map<String, Long> money = plugin.utils.getMoney( player );
			Map<String, Long> bankMoney = plugin.utils.getBankMoney( player );
			
			if( player.hasPermission("bank.admin") == false && plugin.utils.isNearBank( player.getLocation() ) == false ) {
				player.sendMessage( Main.prefixError + "Musisz być w pewnej lokacji aby używać banku!" );
				return true;
			}

			if( args.length == 0 ) {
				player.sendMessage( 
						Main.prefixInfo + "Twoje pieniążki w banku: \n" 
						+ ChatColor.WHITE + "Miedziaki : " + String.valueOf(bankMoney.get("miedziak")) + "\n" 
						+ ChatColor.GRAY + "Srebrniki : " + String.valueOf(bankMoney.get("srebrnik")) + "\n" 
						+ ChatColor.YELLOW + "Złotniki : " + String.valueOf(bankMoney.get("złotnik")) 
					);
				return true;
			}
			
			if( args.length == 0 ) { player.sendMessage( getUsage(player) ); return true; };
			
			String make = args[0];
			String type = args.length > 1 ? args[1] : "null";
			
			switch (make) {
				case "wpłać": {
					Long ammount = args.length >= 3 ? Long.valueOf(args[2]) : 0l;
					
					Long rest = money.get(type) - ammount;
					if( rest < 0 ) {
						player.sendMessage( Main.prefixError + "Nie możesz wpłacić " + ChatColor.YELLOW + ammount + ChatColor.GRAY + " bo twój balans portfela będzie na minusie: " + ChatColor.RED + rest );
						return true;
					}
					
					Boolean check = plugin.utils.transferToBank(player, type, ammount, money, bankMoney);
					if( check == false ) {
						player.sendMessage( Main.prefixError + "Nie udało się wpłacić kasy!" );
						player.sendMessage( getUsage(player) ); 
						return true;
					}
					
					bankMoney.put(type, bankMoney.get(type) + ammount);
					money.put(type, rest);
					player.sendMessage( Main.prefixInfo + "Wpłacono " + ChatColor.YELLOW + ammount + " " + type + "ów" + ChatColor.WHITE + ", Aktualny stan konta: " + ChatColor.YELLOW + bankMoney.get(type) + " " + type + "ów" );
					plugin.bot.moneyLog( "**" + player.getName() + "** Wpłacił *" + ammount + " " + type + "ów* do banku!" );
					
					return true;
				}
				case "wypłać" : {
					Long ammount = args.length >= 3 ? Long.valueOf(args[2]) : 0l;
					
					Long rest = bankMoney.get(type) - ammount;
					if( rest < 0 ) {
						player.sendMessage( Main.prefixError + "Nie możesz wypłacić " + ChatColor.YELLOW + ammount + ChatColor.GRAY + " bo twój balans banku będzie na minusie: " + ChatColor.RED + rest );
						return true;
					}
					
					Boolean check = plugin.utils.transferFromBank(player, type, ammount, money, bankMoney);
					if( check == false ) {
						player.sendMessage( Main.prefixError + "Nie udało się wypłacić kasy!" );
						player.sendMessage( getUsage(player) ); 
						return true;
					}
					
					bankMoney.put(type, rest);
					money.put(type, money.get(type) + ammount);
					player.sendMessage( Main.prefixInfo + "Wypłacono " + ChatColor.YELLOW + ammount + " " + type + "ów" + ChatColor.WHITE + ", Aktualny stan konta: " + ChatColor.YELLOW + bankMoney.get(type) + " " + type + "ów" );
					plugin.bot.moneyLog( "**" + player.getName() + "** Wypłacił *" + ammount + " " + type + "ów* z banku!" );
					
					return true;
				}
				case "przelej" : {
					if( args.length < 4 ) { player.sendMessage( getUsage(player) ); return true; };

					Long ammount = args.length >= 3 ? Long.valueOf(args[2]) : 0l;
					
					String uuid = plugin.utils.resolveUUID( args[3] );
					
					Boolean done = plugin.utils.transferBankMoney( player.getUniqueId().toString(), uuid, type, ammount);
					if( done == false ) {
						player.sendMessage( Main.prefixError + "Błąd podczas dodawania, sprawdz pisownie i czy gracz jest online." );
						return true;
					}
					
					player.sendMessage( Main.prefixInfo + "Przelano " + ChatColor.YELLOW + ammount + " " + type + "ów" + ChatColor.WHITE + " na konto gracza " + ChatColor.GREEN + args[3] );
					plugin.bot.moneyLog( "**" + player.getName() + "** Przelał *" + ammount + " " + type + "ów* na konto gracza **" + args[3] + "**!" );
					
					return true;
				}
				case "wymień": {
					if( args.length < 4 ) { player.sendMessage( getUsage(player) ); return true; }
					
					String from = args[1];
					String to = args[2];
					Long ammount = Long.valueOf(args[3]);
					
					Double conversion = plugin.utils.getConversion( from, to );
					if( conversion == null ) {
						player.sendMessage( Main.prefixError + "Brak konwersji!" );
						return true;
					}
					if( ammount - tax < conversion ) {
						player.sendMessage( Main.prefixError + "Podana kwota jest za niska na wymianę. Pamiętaj o wliczeniu stałej opłaty w wysokości " + ChatColor.RED + " 4 " + from + "i!" );
						return true;
					}
					
					Long bfrom = bankMoney.get(from);
					Long mfrom = money.get(from);
					
					if( bfrom - tax < ammount && mfrom - tax < ammount ) {
						player.sendMessage( Main.prefixError + "Nie posiadasz tyle kasy aby ją wymienić!" );
						return true;
					}
					
					Long get = (long) ( (ammount - tax) / conversion );
					Long take = (long) ( ( get * conversion ) + tax );

					if( bfrom < take && mfrom < take ) { 
						player.sendMessage( Main.prefixError + "Nie masz odpowiedniej sumy: " + ChatColor.YELLOW + take + from + "ów" );
						return true;
					}
					
					Boolean taken = false;
					if( bfrom >= take ) {
						Boolean check = plugin.utils.setBankMoney(player.getUniqueId().toString(), from, bfrom - take);
						if( check == false ) {
							player.sendMessage( Main.prefixError + "Nie udało się pobrać kasy z banku!" );
							return true;
						}
						bankMoney.put( from, bfrom - take );
						taken = true;
					}
					if( taken == false && mfrom >= take ) {
						Boolean check = plugin.utils.setMoney( player.getUniqueId().toString(), from, mfrom - take );
						if( check == false ) {
							player.sendMessage( Main.prefixError + "Nie udało się pobrać kasy z banku!" );
							return true;
						}
						money.put( from, mfrom - take );
						taken = true;
					}
					
					if( taken == false ) {
						player.sendMessage( Main.prefixError + "Nie udało się sfinalizować transakcji!" );
						return true;
					} else {
						Boolean check = plugin.utils.setBankMoney( player.getUniqueId().toString(), to, bankMoney.get(to) + get );
						if( check == false ) {
							player.sendMessage( Main.prefixError + "Nie udało się dodać kasy do banku!" );
							return true;
						}
						
						player.sendMessage( Main.prefixInfo + "Prawidłowo wymieniono " + ChatColor.YELLOW + take + " " + from + "ów" + ChatColor.WHITE + " na " + ChatColor.YELLOW + get + " " + to + "ów" );
						plugin.bot.moneyLog( "**" + player.getName() + "** Wymienił *" + take + " " + from + "ów* na *" + get + " " + to + "ów*!" );
					
					}
					
					return true; 
				}
				case "ustaw" : {
					if( player.hasPermission("bank.admin") == false ) { player.sendMessage( getUsage(player) ); return true; };
					if( args.length < 4 ) { player.sendMessage( getUsage(player) ); return true; };
					Long ammount = args.length >= 3 ? Long.valueOf(args[2]) : 0l;
					
					String uuid = plugin.utils.resolveUUID( args[3] );
					
					Boolean done = plugin.utils.setBankMoney( uuid, type, Long.valueOf(ammount) );
					if( done == false ) {
						player.sendMessage( Main.prefixError + "Błąd podczas setowania, sprawdz pisownie i czy gracz jest online." );
						return true;
					}
					
					player.sendMessage( Main.prefixInfo + "Ustawiono stan konta gracza " + ChatColor.GREEN + args[3] + ChatColor.WHITE + " na " + ChatColor.YELLOW + ammount + " " + type + "ów" );
					plugin.bot.moneyLog( "**" + player.getName() + "** Ustawił *" + type + "i* gracza **" + args[3] + "** na " + ammount );
					
					return true;
				}
				case "dodaj" : {
					if( player.hasPermission("bank.admin") == false ) { player.sendMessage( getUsage(player) ); return true; };
					if( args.length < 4 ) { player.sendMessage( getUsage(player) ); return true; };
					Long ammount = args.length >= 3 ? Long.valueOf(args[2]) : 0l;
					
					String uuid = plugin.utils.resolveUUID( args[3] );
					
					Map<String, Long> bm = plugin.utils.getBankMoney(uuid);
					
					Boolean done = plugin.utils.setBankMoney( uuid, type, bm.get(type) + ammount );
					if( done == false ) {
						player.sendMessage( Main.prefixError + "Błąd podczas dodawania, sprawdz pisownie i czy gracz jest online." );
						return true;
					}
					
					player.sendMessage( Main.prefixInfo + "Dodano " + ChatColor.YELLOW + ammount + " " + type + "ów" + ChatColor.WHITE + " do konta gracza " + ChatColor.GREEN + args[3] + ChatColor.WHITE + ", teraz ma: " + ChatColor.YELLOW + (bm.get(type) + ammount) );
					plugin.bot.moneyLog( "**" + player.getName() + "** Dodał *" + ammount + " " + type + "ów* do banku gracza **" + args[3] + "**!" );
					
					return true;
				}
				case "zabierz" : {
					if( player.hasPermission("bank.admin") == false ) { player.sendMessage( getUsage(player) ); return true; };
					if( args.length < 4 ) { player.sendMessage( getUsage(player) ); return true; };
					Long ammount = args.length >= 3 ? Long.valueOf(args[2]) : 0l;
					
					String uuid = plugin.utils.resolveUUID( args[3] );
					Map<String, Long> bm = plugin.utils.getBankMoney(uuid);
					
					if( bm.get(type) - ammount < 0 ) {
						player.sendMessage( Main.prefixError + "Nie można zabrać tyle kasy, gracz będzie na minusie! " + ChatColor.RED + bm.get(type) + " -> " + (bm.get(type) - ammount) );
						return true;
					}
					
					Boolean done = plugin.utils.setBankMoney( uuid, type, bm.get(type) - ammount );
					if( done == false ) {
						player.sendMessage( Main.prefixError + "Błąd podczas zabierania, sprawdz pisownie i czy gracz jest online." );
						return true;
					}
					
					player.sendMessage( Main.prefixInfo + "Zabrano " + ChatColor.YELLOW + ammount + " " + type + "ów" + ChatColor.WHITE + " z konta gracza " + ChatColor.GREEN + args[3] + ChatColor.WHITE + ", teraz ma: " + ChatColor.YELLOW + (bm.get(type) - ammount) );
					plugin.bot.moneyLog( "**" + player.getName() + "** Zabrał *" + ammount + " " + type + "ów* z banku gracza **" + args[3] + "**!" );
					
					return true;
				}
				case "stwórz": {
					if( player.hasPermission("bank.admin") == false ) { player.sendMessage( getUsage(player) ); return true; };
					Location loc = player.getLocation();
					
					if( type.equals("miejsce") ) {
						Boolean check = plugin.utils.createBank( loc );
						if( check == false ) {
							player.sendMessage( Main.prefixError + "Coś poszło nie tak! Banki można ustawiać co 50m.");
							return true;
						}
						
						player.sendMessage( Main.prefixInfo + "Stworzono bank w twojej lokalizacji!" );
						plugin.bot.moneyLog( "**" + player.getName() + "** Ustawił nowy bank w lokalizacji (" + loc.getX() + " " + loc.getY() + " " + loc.getZ() +")" );
					}
					
					return true;
				}
				case "usuń": {
					if( player.hasPermission("bank.admin") == false ) { player.sendMessage( getUsage(player) ); return true; };
					Location loc = player.getLocation();
					
					Boolean check = plugin.utils.deleteBank(player);
					if( check == false ) {
						player.sendMessage( Main.prefixError + "Coś poszło nie tak przy usuwaniu banku! Upewnij się, że jesteś w obszarze 20m od centrum." );
						return true;
					}
					
					player.sendMessage( Main.prefixInfo + "Usunięto bank najbliższy twojej lokalizacji!" );
					plugin.bot.moneyLog( "**" + player.getName() + "** Usunął bank w lokalizacji (" + loc.getX() + " " + loc.getY() + " " + loc.getZ() +")" );
					
					return true;
				}
				case "konwersje" : {
					if( player.hasPermission("bank.admin") == false ) { player.sendMessage( getUsage(player) ); return true; };
					if( args.length < 4 ) { player.sendMessage( getUsage(player) ); return true; };
					
					String from = args[1];
					String to = args[2];
					Long conv = Long.valueOf(args[3]);
					
					Boolean check = plugin.utils.setConversion(from, to, conv);
					if( check == false ) {
						player.sendMessage( Main.prefixError + "Nie udało się ustawić konwersji: " + from +" * " + conv + " = 1 * " + to );
						return true;
					}
					
					player.sendMessage( Main.prefixInfo + "Ustawiono konwersję: " + from +" * " + conv + " = 1 * " + to );
					plugin.bot.moneyLog( "**" + player.getName() + "** Ustawił konwersję: `" + from + " -> " + to + "` **" + conv + " : 1**" );
					
					return true;
				}
				case "list" : {
					if( player.hasPermission("bank.admin") == false ) { player.sendMessage( getUsage(player) ); return true; };
					List<Location> banks = plugin.utils.getBanks();
					
					StringBuilder os = new StringBuilder( Main.prefixInfo + "Banki: " );
					for( Location bank : banks ) 
						os.append( ChatColor.GREEN + "\nX: " + ChatColor.RED + bank.getBlockX() + ChatColor.GREEN + ", Y: " + ChatColor.RED + bank.getBlockY() + ChatColor.GREEN +", Z: " + ChatColor.RED + bank.getBlockZ() );
					 
					player.sendMessage( os.toString() );
					
					return true;
				}
				
				default: {
					player.sendMessage( getUsage(player) ); 
					return true;
				}
			}
		} else {
			sender.sendMessage( "Komenda tylko dla graczy!" );
			return true;
		}
	}

}

