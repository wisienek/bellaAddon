package net.woolf.bella.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.woolf.bella.Main;

@SuppressWarnings("unused")
public class otpCommand implements CommandExecutor {
	
	private Main plugin;

    private HashMap<Player, Integer> cooldownTimeOTP;
    private HashMap<Player, BukkitRunnable> cooldownTaskOTP;

    private HashMap<Player, Integer> cooldownTimeSetOTP;
	private HashMap<Player, BukkitRunnable> cooldownTaskSetOTP;
	
	public otpCommand(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("otp").setExecutor(this);
		
        cooldownTimeOTP = new HashMap<>();
        cooldownTaskOTP = new HashMap<>();

        cooldownTimeSetOTP = new HashMap<>();
        cooldownTaskSetOTP = new HashMap<>();
	}
	
	public String getUsage() {
		return Main.prefixInfo + "Użycie komendy: /otp\n/otp info - info o aktualnym lvl\n/otp set/del/tp/tpws/list <nazwa> - set: ustawia, del: usuwa, tp: teleportuje, tpws: teleportacja wspólna, list: lista\n/otp set tp1\n/otp tp tp1\n/otp list\n/otp tpws tp1";
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if( !(sender instanceof Player) ) {
			sender.sendMessage("Tylko dla graczy!");
			return true;
		}
		
		Player player = (Player) sender;
		
		if( player.hasPermission("otp.use") ) {
			
			if( args.length < 1  ) {
				player.sendMessage( getUsage() );
				return true;
			}
			
			
			String otp = "";
			String levelS = plugin.utils.getLevel(player);
			int level = Integer.parseInt( levelS );
			
			if( args.length > 1 )
				otp = args[1];
			
			if( level == 0 ) {
				player.sendMessage( Main.prefixError + "Nie posiadasz odpowiednich umiejętności!" );
				return true;
			}
			
			int cld    = Integer.valueOf( (String) plugin.config.get("tp-level-"+ levelS +"-cld") );
			int radius = Integer.valueOf( (String) plugin.config.get("tp-level-"+ levelS +"-radius") );
			int maxp   = Integer.valueOf( (String) plugin.config.get("tp-level-"+ levelS +"-maxp") );
			int maxpts = Integer.valueOf( (String) plugin.config.get("tp-level-"+ levelS +"-maxpoints") );
			
			// plugin.logger.info( "level: " + levelS + ", cld: "+ cld + ", rad: " + radius +", maxp: "+ maxp + ", maxpts: " + maxpts );
			
			switch( args[0] ) {
				case "info": {
					Map<String, Object> list = plugin.utils.getOTP(player);
			    	int len = list.size();
					StringBuilder os = new StringBuilder();
					os.append( Main.prefixInfo + "Twój level wynosi: " + ChatColor.YELLOW + levelS );
					os.append( "\n" + ChatColor.GRAY + "Max pkt tp : " + ChatColor.YELLOW + String.valueOf(len) + " / " + String.valueOf(maxpts) );
					os.append( "\n" + ChatColor.GRAY + "Odległość  : " + ChatColor.YELLOW + String.valueOf(radius) + "m" );
					os.append( "\n" + ChatColor.GRAY + "Cooldown   : " + ChatColor.YELLOW + String.valueOf(cld) + "s");
					os.append( "\n" + ChatColor.GRAY + "Max Graczy : " + ChatColor.YELLOW + String.valueOf(maxp) + " graczy" );
					
					player.sendMessage( os.toString() );
					return true;
				}
				case "set": {
					if( otp == null || otp.isEmpty() ) {
						player.sendMessage( Main.prefixError + "Niepoprawna nazwa: " + ChatColor.YELLOW + otp);
						return true;
					}
					
					// sprawdmax
					Map<String, Object> list = plugin.utils.getOTP(player);
			    	int len = list.size();
			    	if( len >= maxpts ) {
			    		player.sendMessage( Main.prefixError + "Nie możesz mieć więcej punktów tp ("+ String.valueOf(maxpts) +")!");
			    		return true;
			    	}
					
					// ustaw
	                if ( plugin.config.getBoolean("setOTP-command-delay") ) {
	                    if ( cooldownTimeSetOTP.containsKey(player) ) {
	                        player.sendMessage( Main.prefixError + "Musisz poczekać " + ChatColor.RED + cooldownTimeSetOTP.get(player) + ChatColor.GRAY + " sekund.");
	                    } else {
	                        setPlayerOTP(player, otp);
	                        setCoolDownTimeSetOTP(player, cld);
	                        player.sendMessage( Main.prefixInfo + "Ustawiono TP : " + ChatColor.AQUA + otp + ChatColor.WHITE +". Zajęte tp: " + String.valueOf(len + 1) +" / " + String.valueOf(maxpts) );
	                    }
	                } else {
	                    setPlayerOTP(player, otp);
	                    player.sendMessage( Main.prefixInfo + "Ustawiono TP : " + ChatColor.AQUA + otp + ChatColor.WHITE +". Zajęte tp: " + String.valueOf(len + 1) +" / " + String.valueOf(maxpts) );
	                }
	                
					return true;
				}
				case "del": {
					if( otp == null || otp.isEmpty() ) {
						player.sendMessage( Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp);
						return true;
					}
					
					if( plugin.utils.tpsIsNull( player, otp ) ) {
						player.sendMessage( Main.prefixError + "Nie masz takiego TP: " + ChatColor.YELLOW + otp);
					} else {
						Map<String, Object> list = plugin.utils.getOTP(player);
				    	int len = list.size();
				    	
						plugin.utils.deleteOTP(player, otp);
						player.sendMessage( Main.prefixInfo + "Usunięto teleport: " + ChatColor.AQUA + otp + ChatColor.WHITE +". Wolne: " + String.valueOf( len - 1 ) + " / " + String.valueOf(maxpts) );
					}
					
					return true;
				}
				case "effect": {
					tpEffect(player);
					
					return true;
				}
				case "tp": {
					if( otp == null || otp.isEmpty() ) {
						player.sendMessage( Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp);
						return true;
					}
					
					if( plugin.utils.tpsIsNull( player, otp ) ) {
						player.sendMessage( Main.prefixError + "Nie masz takiego TP: " + ChatColor.YELLOW + otp);
					} else {
						
						// przelicz odległość
						Location loc = plugin.utils.getOTPLocation(player, otp);
						double distance = player.getLocation().distance(loc);
						
						if( distance > radius ) {
							player.sendMessage( Main.prefixError + "Za długi dystans pomiędzy lokacją, a punktem teleportacji! ( " + String.valueOf( (int) distance ) + " / " + String.valueOf( (int) radius ) +" )" );
							return true;
						}
						
						List<Player> sendTo = plugin.utils.getNearbyPlayers(player, 20).collect( Collectors.toList() );
						for( Player sending : sendTo ) {
							sending.sendMessage( ChatColor.WHITE + "[L] " + ChatColor.YELLOW +"[Niedaleko słychać trzask teleportacji]" );
						}
						
						
						if ( plugin.config.getBoolean("OTP-command-delay") ) {
							if ( cooldownTimeOTP.containsKey(player) ) {
								player.sendMessage( Main.prefixError + "Musisz odpocząć " + ChatColor.RED + cooldownTimeOTP.get(player) + ChatColor.GRAY + " sekund.");
							} else {
								tpEffect(player);
								
								plugin.utils.sendOTP(player, otp);
								setCoolDownTimeOTP(player, cld);
								player.sendMessage( Main.prefixInfo + "Teleportowano do punktu " + ChatColor.YELLOW + otp );
							}
	                    } else {
	                    	tpEffect(player);
	                    	
	                    	plugin.utils.sendOTP(player, otp);
							player.sendMessage( Main.prefixInfo + "Teleportowano do punktu " + ChatColor.YELLOW + otp );
	                    }
					}
					
					return true;
				}
				case "list": {
					Map<String, Object> list = plugin.utils.getOTP(player);
			    	int len = list.size();
			    	
			    	if( len == 0 ) {
			    		player.sendMessage( Main.prefixInfo + "Nie posiadasz żadnych tp. Wolne: " + String.valueOf( maxpts ) );
			    		return true;
			    	}
			    	Location playerLoc = player.getLocation();
			    	
			    	StringBuilder os = new StringBuilder();
			    	os.append( Main.prefixInfo + "Twoja lista TP ( "+ len +" / "+ maxpts +" ) : " );
			    	for ( String key : list.keySet() ) {
			    		Location loc = plugin.utils.getOTPLocation(player, key);
			    		os.append("\n- "+ key +" (" + (int) playerLoc.distance(loc) +"m)");
			    		// plugin.logger.info("OTP: " + key);
			    	}
			    	
			    	player.sendMessage( os.toString() );
					return true;
				}
				case "tpws": {
					if( !player.hasPermission("otp.tpws") ) {
						player.sendMessage( Main.prefixError + "Nie potrafisz jeszcze wspólnej teleportacji!" );
						return true;
					}
					
					List<Player> nearbyPlayers = plugin.utils.getNearbyPlayers( player, 2 ).collect( Collectors.toList() );
					
					if( nearbyPlayers.size() > maxp ) {
						player.sendMessage( Main.prefixError + "Za dużo osób do teleportacji! ( " + String.valueOf( nearbyPlayers.size() ) + " / " + String.valueOf( maxp ) +" )" );
						return true;
					}
					
					if( otp == null || otp.isEmpty() ) {
						player.sendMessage( Main.prefixError + "Niepoprawny TP: " + ChatColor.YELLOW + otp);
						return true;
					}
					
					if( plugin.utils.tpsIsNull( player, otp ) ) {
						player.sendMessage( Main.prefixError + "Nie masz takiego TP: " + ChatColor.YELLOW + otp);
					} else {
						// przelicz odległość
						Location loc = plugin.utils.getOTPLocation(player, otp);
						double distance = player.getLocation().distance(loc);
						
						if( distance > radius ) {
							player.sendMessage( Main.prefixError + "Za długi dystans pomiędzy lokacją, a punktem teleportacji ( " + String.valueOf( (int) distance ) + " / " + String.valueOf( (int) radius ) +" )"  );
							return true;
						}
						
						
						List<Player> sendTo = plugin.utils.getNearbyPlayers(player, 20).collect( Collectors.toList() );
						for( Player sending : sendTo ) {
							sending.sendMessage( ChatColor.WHITE + "[L] " + ChatColor.YELLOW +"[Niedaleko słychać trzask teleportacji łącznej]" );
						}
						
						
						StringBuilder os = new StringBuilder();
						if ( plugin.config.getBoolean("OTP-command-delay") ) {
							if ( cooldownTimeOTP.containsKey(player) ) {
								player.sendMessage( Main.prefixError + "Musisz odpocząć " + ChatColor.RED + cooldownTimeOTP.get(player) + ChatColor.GRAY + " sekund.");
							} else {
								for( Player target : nearbyPlayers ) {
									tpEffect(target);
									
									plugin.utils.sendOTP( player, otp, target );
									
									os.append( target.getName() + " " );
									target.sendMessage( Main.prefixInfo + player.getDisplayName()+" Teleportował się z tobą do punktu: " + ChatColor.YELLOW + otp );
								}
								plugin.utils.sendOTP(player, otp);
								
								setCoolDownTimeOTP(player, cld);
								player.sendMessage( Main.prefixInfo + "Teleportowano wspólnie z ( "+ os.toString() +" ) do punktu " + ChatColor.YELLOW + otp );
							}
	                    } else {
							for( Player target : nearbyPlayers ) {
								tpEffect(target);
								plugin.utils.sendOTP(player, otp, target);
								
								os.append( target.getName() + " " );
								target.sendMessage( Main.prefixInfo + player.getDisplayName()+" Teleportował się z tobą do punktu: " + ChatColor.YELLOW + otp );
							}
							plugin.utils.sendOTP(player, otp);
	                    	
							player.sendMessage( Main.prefixInfo + "Teleportowano wspólnie z ( "+ os.toString() +" ) do punktu " + ChatColor.YELLOW + otp );
	                    }
					}
					
					return true;
				}
			}
			
			return true;
		} else {
			sender.sendMessage( Main.prefixError + "Potrzebujesz permissi otp.use aby użyć tej komendy!" );
			return true;
		}
	}
	
	void tpEffect(Player player) {
		player.playEffect(player.getLocation(), Effect.ENDER_SIGNAL, null);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, (float) 0.08 , (float) 1);
	
        new BukkitRunnable() {
            double phi = 0.0;
            
            public void run() {
                this.phi += 0.39269908169872414;
                final Location location1 = player.getLocation();
                final World world = player.getWorld();
                for (double t = 0.0; t <= 5.283185307179586; t += 0.19634954084936207) {
                    for (double i = 0.0; i <= 1.0; ++i) {
                        final double x = 0.4 * (6.283185307179586 - t) * 0.5 * Math.cos(t + this.phi + i * 3.141592653589793);
                        final double y = 0.5 * t;
                        final double z = 0.4 * (6.283185307179586 - t) * 0.5 * Math.sin(t + this.phi + i * 3.141592653589793);
                        location1.add(x, y, z);
                        world.spawnParticle(Particle.CRIT_MAGIC, location1, 1);
                        location1.subtract(x, y, z);
                    }
                }
                if (this.phi > 2 * 3.141592653589793) {
                    this.cancel();
                }
            }
        }.runTaskTimer( (Plugin) plugin, 0L, 3L );
        try {
			Thread.sleep(10L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	void setCoolDownTimeOTP(Player player, int coolDown) {
        cooldownTimeOTP.put(player, coolDown);
        cooldownTaskOTP.put(player, new BukkitRunnable() {
            public void run() {
                cooldownTimeOTP.put(player, cooldownTimeOTP.get(player) - 1);
                if (cooldownTimeOTP.get(player) == 0) {
                    cooldownTimeOTP.remove(player);
                    cooldownTaskOTP.remove(player);
                    cancel();
                }
            }
        });
        cooldownTaskOTP.get(player).runTaskTimer(plugin, 20, 20);
    }
    void setCoolDownTimeSetOTP(Player player, int coolDown) {
        cooldownTimeSetOTP.put(player, coolDown);
        cooldownTaskSetOTP.put(player, new BukkitRunnable() {
            public void run() {
                cooldownTimeSetOTP.put(player, cooldownTimeSetOTP.get(player) - 1);
                if (cooldownTimeSetOTP.get(player) == 0) {
                    cooldownTimeSetOTP.remove(player);
                    cooldownTaskSetOTP.remove(player);
                    cancel();
                }
            }
        });
        cooldownTaskSetOTP.get(player).runTaskTimer(plugin, 20, 20);
    }
    
    void setPlayerOTP(Player player, String name) {
        plugin.utils.setOTP(player, name);
        if ( plugin.config.getBoolean("show-setOTP-message") ) {
            String strFormatted = plugin.config.getString("setOTP-message").replace("%player%", player.getDisplayName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', strFormatted));
        }
    }

}
