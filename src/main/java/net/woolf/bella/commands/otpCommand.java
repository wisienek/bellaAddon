package net.woolf.bella.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.meta.ItemMeta;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.effect.*;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;
import net.woolf.bella.Utils;

@SuppressWarnings("unused")
public class otpCommand implements CommandExecutor {

	private Main plugin;

	public otpCommand(
			Main plugin
	) {
		this.plugin = plugin;
		plugin.getCommand( "otp" ).setExecutor( this );
	}

	public String getUsage() {
		return Main.prefixInfo + "Użycie komendy: /otp" + "\n/otp info - info o aktualnym lvl"
				+ "\n/otp set/del/tp/ws/os/list/seteffect <nazwa> - set: ustawia, del: usuwa, tp: teleportuje, ws: teleportacja wspólna, list: lista, os: teleportuje wyznaczoną osobę"
				+ "\n/otp set tp1" + "\n/otp tp tp1" + "\n/otp list" + "\n/otp ws tp1"
				+ "\n/otp os tp1 Zbyszek" + "\n/otp seteffect ignis"
				+ "\n/otp enchant <punkt tp> [ilość użyć] - ustawia teleport na przedmiot z ewentualną ilością użyć"
				+ "\n/otp inspect - robi inspekcję przedmiotu trzymanego w dłoni";
	}

	@Override
	public boolean onCommand(
			CommandSender sender, Command cmd, String label, String[] args
	) {
		if ( !( sender instanceof Player ) ) {
			sender.sendMessage( "Tylko dla graczy!" );
			return true;
		}

		Player player = (Player) sender;

		if ( player.hasPermission( "bella.otp.use" ) ) {

			if ( args.length < 1 ) {
				player.sendMessage( getUsage() );
				return true;
			}

			String otp = "";
			String levelS = plugin.utils.getLevel( player );
			int level = Integer.parseInt( levelS );

			if ( args.length > 1 )
				otp = args[1];

			if ( level == 0 ) {
				player.sendMessage( Main.prefixError + "Nie posiadasz odpowiednich umiejętności!" );
				return true;
			}

			int cld = Integer
					.valueOf( (String) plugin.config.get( "tp-level-" + levelS + "-cld" ) );
			int radius = Integer
					.valueOf( (String) plugin.config.get( "tp-level-" + levelS + "-radius" ) );
			int maxp = Integer
					.valueOf( (String) plugin.config.get( "tp-level-" + levelS + "-maxp" ) );
			int maxpts = Integer
					.valueOf( (String) plugin.config.get( "tp-level-" + levelS + "-maxpoints" ) );
			int setmaxuse = Integer
					.valueOf( (String) plugin.config.get( "tp-level-" + levelS + "-setmaxuse" ) );

			switch ( args[0] ) {
				case "info": {
					Map<String, Object> list = plugin.utils.getOTP( player );
					int len = list.size();
					String type = plugin.utils.getType( player );

					StringBuilder os = new StringBuilder();
					os.append(
							Main.prefixInfo + "Twój level wynosi: " + ChatColor.YELLOW + levelS );
					os.append( "\n" + ChatColor.GRAY + "Max pkt tp : " + ChatColor.YELLOW
							+ String.valueOf( len ) + " / " + String.valueOf( maxpts ) );
					os.append( "\n" + ChatColor.GRAY + "Odległość  : " + ChatColor.YELLOW
							+ String.valueOf( radius ) + "m" );
					os.append( "\n" + ChatColor.GRAY + "Cooldown   : " + ChatColor.YELLOW
							+ String.valueOf( cld ) + "s" );
					os.append( "\n" + ChatColor.GRAY + "Max Graczy : " + ChatColor.YELLOW
							+ String.valueOf( maxp ) + " graczy" );
					os.append( "\n" + ChatColor.GRAY + "Efekt tp   : " + ChatColor.YELLOW
							+ ( ( type == null || type.isEmpty() ) ? "ignis (default)" : type ) );

					player.sendMessage( os.toString() );
					return true;
				}

				case "set": {
					if ( otp == null || otp.isEmpty() ) {
						player.sendMessage(
								Main.prefixError + "Niepoprawna nazwa: " + ChatColor.YELLOW + otp );
						return true;
					}

					// sprawdmax
					Map<String, Object> list = plugin.utils.getOTP( player );
					int len = list.size();
					if ( len >= maxpts ) {
						player.sendMessage( Main.prefixError + "Nie możesz mieć więcej punktów tp ("
								+ String.valueOf( maxpts ) + ")!" );
						return true;
					}

					// ustaw
					if ( plugin.config.getBoolean( "setOTP-command-delay" ) ) {
						if ( plugin.utils.cooldownTimeSetOTP.containsKey( player ) ) {
							player.sendMessage( Main.prefixError + "Musisz poczekać "
									+ ChatColor.RED + plugin.utils.cooldownTimeSetOTP.get( player )
									+ ChatColor.GRAY + " sekund." );
						} else {
							setPlayerOTP( player, otp );
							plugin.utils.setCoolDownTimeSetOTP( player, cld );
							player.sendMessage( Main.prefixInfo + "Ustawiono TP : " + ChatColor.AQUA
									+ otp + ChatColor.WHITE + ". Zajęte tp: "
									+ String.valueOf( len + 1 ) + " / "
									+ String.valueOf( maxpts ) );
						}
					} else {
						setPlayerOTP( player, otp );
						player.sendMessage( Main.prefixInfo + "Ustawiono TP : " + ChatColor.AQUA
								+ otp + ChatColor.WHITE + ". Zajęte tp: "
								+ String.valueOf( len + 1 ) + " / " + String.valueOf( maxpts ) );
					}

					return true;
				}

				case "del": {
					if ( otp == null || otp.isEmpty() ) {
						player.sendMessage(
								Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
						return true;
					}

					if ( plugin.utils.tpsIsNull( player, otp ) ) {
						player.sendMessage( Main.prefixError + "Nie masz takiego TP: "
								+ ChatColor.YELLOW + otp );
					} else {
						Map<String, Object> list = plugin.utils.getOTP( player );
						int len = list.size();

						plugin.utils.deleteOTP( player, otp );
						player.sendMessage( Main.prefixInfo + "Usunięto teleport: " + ChatColor.AQUA
								+ otp + ChatColor.WHITE + ". Wolne: " + String.valueOf( len - 1 )
								+ " / " + String.valueOf( maxpts ) );
					}

					return true;
				}

				case "effect": {
					if (
						player.hasPermission( "atp.admin" ) || player.hasPermission( "otp.test" )
					) {
						String type = plugin.utils.getType( player );
						plugin.utils.tpEffect( player, null, null );

						player.sendMessage( Main.prefixInfo + "Typ: " + ChatColor.BLUE + type );
					}

					return true;
				}

				case "seteffect": {
					if ( otp.isEmpty() || otp == null ) {
						player.sendMessage( getUsage() );
						return true;
					}

					String ef = plugin.utils.getType( player );

					if ( ef.isEmpty() == false && player.hasPermission( "atp.admin" ) == false ) {
						player.sendMessage( Main.prefixError
								+ "Masz już ustawiony efekt! Jedynie admin może Ci zmienić." );
						return true;
					}

					if ( Utils.types.contains( otp ) == false ) {
						StringBuilder os = new StringBuilder();
						for ( String type : Utils.types )
							os.append( ChatColor.GRAY + ", " + ChatColor.YELLOW + type );

						player.sendMessage(
								Main.prefixError + "Twój efekt nie znajduje się na liście: "
										+ ChatColor.YELLOW + String.join( ", ", Utils.types ) );
						return true;
					}

					Boolean ok = plugin.utils.setType( player, otp );

					player.sendMessage(
							ok ? Main.prefixInfo + "Ustawiono efekt na: " + ChatColor.BLUE + otp
									: Main.prefixError + "Nie powiodło się ustawianie efektu!" );
					return true;
				}

				case "tp": {
					if ( otp == null || otp.isEmpty() ) {
						player.sendMessage(
								Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
						return true;
					}

					if ( plugin.utils.tpsIsNull( player, otp ) ) {
						player.sendMessage( Main.prefixError + "Nie masz takiego TP: "
								+ ChatColor.YELLOW + otp );
					} else {

						// przelicz odległość
						Location loc = plugin.utils.getOTPLocation( player, otp );
						double distance = player.getLocation().distance( loc );

						if ( distance > radius ) {
							player.sendMessage( Main.prefixError
									+ "Za długi dystans pomiędzy lokacją, a punktem teleportacji! ( "
									+ String.valueOf( (int) distance ) + " / "
									+ String.valueOf( (int) radius ) + " )" );
							return true;
						}

						List<Player> sendTo = plugin.utils.getNearbyPlayers( player, 20 );
						for ( Player sending : sendTo ) {
							sending.sendMessage( ChatColor.WHITE + "[L] " + ChatColor.YELLOW
									+ "[Niedaleko słychać trzask teleportacji]" );
						}

						if ( plugin.config.getBoolean( "OTP-command-delay" ) ) {
							if ( plugin.utils.cooldownTimeOTP.containsKey( player ) ) {
								player.sendMessage( Main.prefixError + "Musisz odpocząć "
										+ ChatColor.RED + plugin.utils.cooldownTimeOTP.get( player )
										+ ChatColor.GRAY + " sekund." );
							} else {
								plugin.utils.tpEffect( player, otp, null );

								plugin.utils.setCoolDownTimeOTP( player, cld );
								player.sendMessage( Main.prefixInfo + "Teleportowano do punktu "
										+ ChatColor.YELLOW + otp );
							}
						} else {
							plugin.utils.tpEffect( player, otp, null );

							player.sendMessage( Main.prefixInfo + "Teleportowano do punktu "
									+ ChatColor.YELLOW + otp );
						}
					}

					return true;
				}

				case "list": {
					Map<String, Object> list = plugin.utils.getOTP( player );
					int len = list.size();

					if ( len == 0 ) {
						player.sendMessage( Main.prefixInfo + "Nie posiadasz żadnych tp. Wolne: "
								+ String.valueOf( maxpts ) );
						return true;
					}
					Location playerLoc = player.getLocation();

					StringBuilder os = new StringBuilder();
					os.append( Main.prefixInfo + "Twoja lista TP ( " + len + " / " + maxpts
							+ " ) : " );
					for ( String key : list.keySet() ) {
						Location loc = plugin.utils.getOTPLocation( player, key );
						os.append( "\n- " + key + " (" + (int) playerLoc.distance( loc ) + "m)" );
						// plugin.logger.info("OTP: " + key);
					}

					player.sendMessage( os.toString() );
					return true;
				}

				case "enchant": {
					if ( !player.hasPermission( "bella.otp.enchant" ) ) {
						player.sendMessage( Main.prefixError
								+ "Musisz posiadać umiejętność enchatnowania aby tego użyć! (bella.otp.enchant)" );
						return true;
					}

					if ( args.length >= 3 && Integer.valueOf( args[2] ) == null ) {
						player.sendMessage( Main.prefixError + "Argument 3 musi być liczbą!" );
						return true;
					}

					ItemStack item = player.getInventory().getItemInMainHand();

					if ( item == null || item.getType() == Material.AIR ) {
						player.sendMessage( Main.prefixError
								+ "Musisz trzymać przedmiot, który chcesz enchantować!" );
						return true;
					}

					if ( otp == null || otp.isEmpty() ) {
						player.sendMessage(
								Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
						return true;
					}

					Location loc = plugin.utils.getOTPLocation( player, otp );
					NBTItem nbti = new NBTItem( item, true );

					if ( nbti.hasKey( "teleportEnchantment" ) ) {
						player.sendMessage( Main.prefixError + "Przedmiot jest już enchantowany!" );
						return true;
					}

					String maxu = args.length >= 3 ? args[2]
							: player.hasPermission( "atp.admin" )
									? String.valueOf( Integer.MAX_VALUE )
									: String.valueOf( setmaxuse );

					// otp enchant <name> <maxuse> <????minlvl>
					NBTCompound comp = nbti.addCompound( "teleportEnchantment" );
					// core
					comp.setString( "enchanter", player.getName() );
					comp.setInteger( "maxLength", radius );
					comp.setInteger( "cld", cld );
					if ( args.length >= 3 )
						comp.setInteger( "maxUse", Integer.valueOf( maxu ) );
					// location
					comp.setDouble( "x", loc.getX() );
					comp.setDouble( "y", loc.getY() );
					comp.setDouble( "z", loc.getZ() );
					comp.setFloat( "yaw", loc.getYaw() );
					comp.setFloat( "pitch", loc.getPitch() );

					nbti.mergeNBT( item );

					player.sendMessage( Main.prefixInfo + "Enchantowano item na teleport do punktu "
							+ ChatColor.GOLD + otp + ChatColor.AQUA + " (MaxU: " + maxu + ")"
							+ "!" );

					return true;
				}

				case "inspect": {
					if ( !player.hasPermission( "bella.otp.enchant" ) ) {
						player.sendMessage( Main.prefixError
								+ "Musisz posiadać umiejętność enchatnowania aby tego użyć! (bella.otp.enchant)" );
						return true;
					}
					ItemStack item = player.getInventory().getItemInMainHand();

					if ( item == null || item.getType() == Material.AIR ) {
						player.sendMessage( Main.prefixError
								+ "Musisz trzymać przedmiot, który chcesz enchantować!" );
						return true;
					}

					NBTItem nbti = new NBTItem( item );

					if ( nbti.hasKey( "teleportEnchantment" ) == false ) {
						player.sendMessage( Main.prefixError + "Przedmiot nie jest enchantowany!" );
						return true;
					}

					NBTCompound comp = nbti.getCompound( "teleportEnchantment" );
					String enchanter = comp.getString( "enchanter" );
					String maxLen = String.valueOf( comp.getInteger( "maxLength" ) );
					String enchCld = String.valueOf( comp.getInteger( "cld" ) );
					String maxUse = comp.hasKey( "maxUse" )
							? comp.getInteger( "maxUse" ) == Integer.MAX_VALUE ? "Nieskończoności"
									: String.valueOf( comp.getInteger( "maxUse" ) )
							: "Nieskończoności";
					String used = comp.hasKey( "used" )
							? String.valueOf( comp.getInteger( "used" ) )
							: "niewiadomo";
					// location
					int x = (int) Math.round( comp.getDouble( "x" ) );
					int y = (int) Math.round( comp.getDouble( "y" ) );
					int z = (int) Math.round( comp.getDouble( "z" ) );

					player.sendMessage( Main.prefixInfo + "Informacje o Zaczarowanym przedmiocie:\n"
							+ ChatColor.GREEN + "Enchanter: " + ChatColor.AQUA + enchanter + "\n"
							+ ChatColor.GREEN + "Odległość: " + ChatColor.AQUA + maxLen + "\n"
							+ ChatColor.GREEN + "Odpoczynek: " + ChatColor.AQUA + enchCld + "s.\n"
							+ ChatColor.GREEN + "Użytkowanie: " + ChatColor.AQUA + used + " z "
							+ maxUse + "\n" + ChatColor.GREEN + "Lokacja: " + ChatColor.AQUA + x
							+ " " + y + " " + z + "\n" );

					return true;
				}

				case "os": {
					if ( args.length < 3 ) {
						player.sendMessage( getUsage() );
						return true;
					}

					if ( otp == null || otp.isEmpty() ) {
						player.sendMessage(
								Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
						return true;
					}

					String pname = args[2];

					List<Player> list = player.getWorld().getPlayers();
					Player target = null;
					for ( Player p : list )
						if ( p.getName().equals( pname ) )
							target = p;

					if ( target == null || target.getName().equals( player.getName() ) ) {
						player.sendMessage( Main.prefixError + "Nie znaleziono gracza: "
								+ ChatColor.YELLOW + pname );
						return true;
					}

					if ( player.getLocation().distance( target.getLocation() ) > 4 ) {
						player.sendMessage(
								Main.prefixError + "Gracz jest za daleko aby go teleportować!" );
						return true;
					}

					if ( plugin.config.getBoolean( "OTP-command-delay" ) ) {
						if ( plugin.utils.cooldownTimeOTP.containsKey( player ) ) {
							player.sendMessage( Main.prefixError + "Musisz odpocząć "
									+ ChatColor.RED + plugin.utils.cooldownTimeOTP.get( player )
									+ ChatColor.GRAY + " sekund." );
						} else {
							plugin.utils.tpEffect( player, otp, target );
							target.sendMessage( Main.prefixInfo + "Zostałeś teleportowany przez "
									+ player.getName() + " do punktu " + otp );
							player.sendMessage( Main.prefixInfo + "Teleportowałeś " + pname
									+ "do punktu " + otp );

							plugin.utils.setCoolDownTimeOTP( player, cld );
						}
					} else {
						plugin.utils.tpEffect( player, otp, target );
						target.sendMessage( Main.prefixInfo + "Zostałeś teleportowany przez "
								+ player.getName() + " do punktu " + otp );
						player.sendMessage(
								Main.prefixInfo + "Teleportowałeś " + pname + "do punktu " + otp );
					}

					return true;
				}

				case "ws": {
					if ( !player.hasPermission( "bella.otp.ws" ) ) {
						player.sendMessage(
								Main.prefixError + "Nie potrafisz jeszcze wspólnej teleportacji!" );
						return true;
					}

					List<Player> nearbyPlayers = plugin.utils.getNearbyPlayers( player, 3 );

					if ( nearbyPlayers.size() > maxp ) {
						player.sendMessage( Main.prefixError + "Za dużo osób do teleportacji! ( "
								+ String.valueOf( nearbyPlayers.size() ) + " / "
								+ String.valueOf( maxp ) + " )" );
						return true;
					}

					if ( otp == null || otp.isEmpty() ) {
						player.sendMessage(
								Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp );
						return true;
					}

					if ( plugin.utils.tpsIsNull( player, otp ) ) {
						player.sendMessage( Main.prefixError + "Nie masz takiego TP: "
								+ ChatColor.YELLOW + otp );
					} else {
						// przelicz odległość
						Location loc = plugin.utils.getOTPLocation( player, otp );
						double distance = player.getLocation().distance( loc );

						if ( distance > radius ) {
							player.sendMessage( Main.prefixError
									+ "Za długi dystans pomiędzy lokacją, a punktem teleportacji ( "
									+ String.valueOf( (int) distance ) + " / "
									+ String.valueOf( (int) radius ) + " )" );
							return true;
						}

						List<Player> sendTo = plugin.utils.getNearbyPlayers( player, 20 );
						for ( Player sending : sendTo ) {
							sending.sendMessage( ChatColor.WHITE + "[L] " + ChatColor.YELLOW
									+ "[Niedaleko słychać trzask teleportacji łącznej]" );
						}

						StringBuilder os = new StringBuilder();
						if ( plugin.config.getBoolean( "OTP-command-delay" ) ) {
							if ( plugin.utils.cooldownTimeOTP.containsKey( player ) ) {
								player.sendMessage( Main.prefixError + "Musisz odpocząć "
										+ ChatColor.RED + plugin.utils.cooldownTimeOTP.get( player )
										+ ChatColor.GRAY + " sekund." );
							} else {
								for ( Player target : nearbyPlayers ) {
									plugin.utils.tpEffect( player, otp, target );

									os.append( target.getName() + " " );
									target.sendMessage( Main.prefixInfo + player.getDisplayName()
											+ " Teleportował się z tobą do punktu: "
											+ ChatColor.YELLOW + otp );
								}
								plugin.utils.tpEffect( player, otp, null );

								plugin.utils.setCoolDownTimeOTP( player, cld );
								player.sendMessage( Main.prefixInfo + "Teleportowano wspólnie z ( "
										+ os.toString() + " ) do punktu " + ChatColor.YELLOW
										+ otp );
							}
						} else {
							for ( Player target : nearbyPlayers ) {
								plugin.utils.tpEffect( player, otp, target );

								os.append( target.getName() + " " );
								target.sendMessage( Main.prefixInfo + player.getDisplayName()
										+ " Teleportował się z tobą do punktu: " + ChatColor.YELLOW
										+ otp );
							}
							plugin.utils.tpEffect( player, otp, null );

							player.sendMessage( Main.prefixInfo + "Teleportowano wspólnie z ( "
									+ os.toString() + " ) do punktu " + ChatColor.YELLOW + otp );
						}
					}

					return true;
				}
			}

			return true;
		} else {
			sender.sendMessage( Main.prefixError
					+ "Potrzebujesz permissi bella.otp.use aby użyć tej komendy!" );
			return true;
		}
	}

	void setPlayerOTP(
			Player player, String name
	) {
		plugin.utils.setOTP( player, name );
		if ( plugin.config.getBoolean( "show-setOTP-message" ) ) {
			String strFormatted = plugin.config.getString( "setOTP-message" ).replace( "%player%",
					player.getDisplayName() );
			player.sendMessage( ChatColor.translateAlternateColorCodes( '&', strFormatted ) );
		}
	}

}
