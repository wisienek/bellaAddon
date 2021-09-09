package net.woolf.bella;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.effect.DonutEffect;
import de.slikey.effectlib.effect.EarthEffect;
import de.slikey.effectlib.effect.FountainEffect;
import de.slikey.effectlib.effect.StarEffect;
import de.slikey.effectlib.effect.TornadoEffect;
import de.slikey.effectlib.effect.VortexEffect;
import de.tr7zw.nbtapi.NBTCompound;
import net.md_5.bungee.api.ChatColor;

public class Utils {
	
    public HashMap<Player, Integer> cooldownTimeOTP;
    public HashMap<Player, BukkitRunnable> cooldownTaskOTP;

    public HashMap<Player, Integer> cooldownTimeSetOTP;
    public HashMap<Player, BukkitRunnable> cooldownTaskSetOTP;
	
	private Main plugin;

	public static Set<String> types = new HashSet<String>( Arrays.asList( "ignis", "aqua", "geo", "electro", "aeter", "caligo", "lux" ) );
	public static Set<String> moneyTypes = new HashSet<String>( Arrays.asList( "miedziak", "srebrnik", "złotnik" ) );
	
	
	public Utils(Main main) {
		this.plugin = main;
		
        cooldownTimeOTP = new HashMap<>();
        cooldownTaskOTP = new HashMap<>();

        cooldownTimeSetOTP = new HashMap<>();
        cooldownTaskSetOTP = new HashMap<>();
	}
	
	@SuppressWarnings("deprecation")
	public String resolveUUID( String player ) {
		String uuid = null;
		
		Player target = plugin.server.getPlayer(player);
		if( target != null ) {
			uuid = target.getUniqueId().toString();
		} else {
			OfflinePlayer target1 = plugin.server.getOfflinePlayer(player);
			if( target1 != null ) uuid = target1.getUniqueId().toString();
		}
		
		return uuid;
	}
	
	// get money
//	@SuppressWarnings("deprecation")
	public void updateMoneyScore( Object player ) {
//		String uuid = (player instanceof String)? (String) player: ((Player)player).getUniqueId().toString();
//		
//		OfflinePlayer target = plugin.server.getOfflinePlayer(uuid);
//		
//		Set<Score> scores = plugin.server.getScoreboardManager().getMainScoreboard().getScores(target);		
	}
	

	public Boolean isNearBank( Location loc ) {
		if( loc == null || loc instanceof Location == false ) return false;
		List<Location> banks = getBanks();
		
		for ( Location bank : banks ) {
			if( bank == null ) { 
				plugin.logger.info("Bank null!");
				continue;
			} else {
				if( loc.distance(bank) <= 20 ) return true;
			}
		}

		
		return false;
	}
	
	public Location getNearestBank( Location loc ) {
		if( loc == null || loc instanceof Location == false ) return null;
		
		List<Location> banks = getBanks();
		
		for ( Location bank : banks ) {
			if( bank == null ) { 
				plugin.logger.info("Bank null!");
				continue;
			} else {
				if( loc.distance(bank) <= 20 ) return bank;
			}
		}

		
		return null;
	}
	
	public Boolean createBank( Location loc ) {
		List<Location> banks = getBanks();
		
		for( Location bank : banks )
			if( bank != null )
				if( loc.distance(bank) < 50 )
					return false;

		
		List<String> locs = new ArrayList<String>();
		
		banks.add(loc);
		
		for( Location bank : banks ) 
			locs.add( bank.getWorld().getName() + " " + bank.getX() + " " + bank.getY() + " " + bank.getZ() );
		
		plugin.moneyConfig.set( "banks", locs );
		
		plugin.saveMoneyConfig();
		return true;
	}
	
	public Boolean deleteBank( Player player ) {
		if( player == null ) return false;
		
		Location nearestBank = getNearestBank( player.getLocation() );
		if( nearestBank == null ) return false;
		
		List<Location> banks = getBanks();
		List<String> locs = new ArrayList<String>();
		Boolean deleted = false;
		
		for( Location bank : banks ) {
			if( bank != null ) {
				if( bank.getX() == nearestBank.getX() && bank.getY() == nearestBank.getY() && bank.getZ() == nearestBank.getZ() ) {
					plugin.logger.info( "Usunięto bank!" );
					deleted = true;
				}
				else
					locs.add( bank.getWorld().getName() + " " + bank.getX() + " " + bank.getY() + " " + bank.getZ() );
			}
		}
		
		plugin.moneyConfig.set( "banks", locs );
		plugin.saveMoneyConfig();
		
		return deleted;
	}
	
	public List<Location> getBanks() {
		List<?> banksList = plugin.moneyConfig.getList("banks");
		List<Location> banks = new ArrayList<Location>();
		
		if( banksList == null || banksList.size() == 0 ) return banks;
		
		for( Object item : banksList ) {
			if( item != null && item instanceof String == true ) {
				String[] args = ((String) item).split(" ");
				
				banks.add(
						new Location(
									Bukkit.getWorld(args[0]), 
									Double.valueOf(args[1]), 
									Double.valueOf(args[2]), 
									Double.valueOf(args[3])
								)
						);
			}
		}
		
		return banks;
	}
	
	public Map<String, Long> getMoney( Object player ) {
		String uuid = (player instanceof String) ? (String) player: ((Player)player).getUniqueId().toString();
		Map<String, Long> money = new HashMap<String, Long>();
		
		Long miedziak = plugin.moneyConfig.getLong( "personal." + uuid + ".miedziak" );
		Long srebrnik = plugin.moneyConfig.getLong( "personal." + uuid + ".srebrnik" );
		Long złotnik = plugin.moneyConfig.getLong( "personal." + uuid + ".złotnik" );
		
		money.put("miedziak", (miedziak != null) ? miedziak : 0l );
		money.put("srebrnik", (srebrnik != null) ? srebrnik : 0l );
		money.put("złotnik",  (złotnik  != null) ? złotnik  : 0l );
		
		return money;
	}
	public Map<String, Long> getBankMoney( Object player ) {
		String uuid = (player instanceof String) ? (String) player: ((Player)player).getUniqueId().toString();
		Map<String, Long> money = new HashMap<String, Long>();
		
		Long miedziak = plugin.moneyConfig.getLong( "bank." + uuid + ".miedziak" );
		Long srebrnik = plugin.moneyConfig.getLong( "bank." + uuid + ".srebrnik" );
		Long złotnik = plugin.moneyConfig.getLong( "bank." + uuid + ".złotnik" );
		
		money.put("miedziak", (miedziak != null) ? miedziak : 0l );
		money.put("srebrnik", (srebrnik != null) ? srebrnik : 0l );
		money.put("złotnik",  (złotnik  != null) ? złotnik  : 0l );
		
		return money;
	}
	
	public Boolean setMoney( String uuid, String type, Long ammount ) {
		if( uuid == null || moneyTypes.contains(type) == false || ammount == null ) return false;
		
		plugin.moneyConfig.set( "personal." + uuid + "." + type, ammount);
		plugin.saveMoneyConfig();
		
		return true;
	}
	public Boolean setBankMoney( String uuid, String type, Long ammount ) {
		if( uuid == null || moneyTypes.contains(type) == false || ammount == null ) return false;
		
		plugin.moneyConfig.set( "bank." + uuid + "." + type, ammount);
		plugin.saveMoneyConfig();
		
		return true;
	}
	
	public Boolean transferToBank( Player player, String type, Long ammount, Map<String, Long> money, Map<String, Long> bankMoney ) {
		if ( moneyTypes.contains(type) == false ) return false;
		if( money == null ) money = getMoney(player);
		if( bankMoney == null ) bankMoney = getBankMoney(player);
		
		Long rest = money.get(type) - ammount;
		if( rest < 0 ) return false;
		
		Boolean check1 = setMoney(player.getUniqueId().toString(), type, rest);
		if( check1 == false ) return false;
		Boolean check2 = setBankMoney(player.getUniqueId().toString(), type, bankMoney.get(type) + ammount);
		if( check2 == false ) return false;
		
		return true;
	}
	public Boolean transferFromBank( Player player, String type, Long ammount, Map<String, Long> money, Map<String, Long> bankMoney ) {
		if ( moneyTypes.contains(type) == false ) return false;
		if( money == null ) money = getMoney(player);
		if( bankMoney == null ) bankMoney = getBankMoney(player);
		
		Long rest = bankMoney.get(type) - ammount;
		if( rest < 0 ) return false;
		
		Boolean check1 = setBankMoney(player.getUniqueId().toString(), type, rest);
		if( check1 == false ) return false;
		Boolean check2 = setMoney(player.getUniqueId().toString(), type, money.get(type) + ammount);
		if( check2 == false ) return false;
		
		return true;
	}
	
	public Boolean transferMoney( Object from, Object to, String type, Long ammount) {
		if ( moneyTypes.contains(type) == false ) return false;
		if ( from == null || to == null || ammount <= 0 ) return false;
		
		String uuid1 = (from instanceof String)? (String) from: ((Player)from).getUniqueId().toString();
		String uuid2 = (to instanceof String)? (String) to: ((Player)to).getUniqueId().toString();
		
		
		Map<String, Long> money = getMoney( uuid1 );
		Map<String, Long> targetmoney = getMoney( uuid2 );
		
		Long has = money.get(type);
		if( has < ammount || has - ammount < 0 ) return false;

		plugin.moneyConfig.set( "personal." + uuid1 + "." + type, has - ammount);
		plugin.moneyConfig.set( "personal." + uuid2 + "." + type, targetmoney.get(type) + ammount );
		plugin.saveMoneyConfig();
		
		updateMoneyScore(uuid1);
		updateMoneyScore(uuid2);
		
		return true;
	}
	public Boolean transferBankMoney( Object from, Object to, String type, Long ammount) {
		if ( moneyTypes.contains(type) == false ) return false;
		if ( from == null || to == null || ammount <= 0 ) return false;
		
		String uuid1 = (from instanceof String)? (String) from: ((Player)from).getUniqueId().toString();
		String uuid2 = (to instanceof String)? (String) to: ((Player)to).getUniqueId().toString();
		
		Map<String, Long> money = getBankMoney( from );
		Map<String, Long> targetmoney = getBankMoney( to );
		
		Long has = money.get(type);
		if( has < ammount || has - ammount < 0 ) return false;
		
		plugin.moneyConfig.set( "bank." + uuid1 + "." + type, has - ammount);
		plugin.moneyConfig.set( "bank." + uuid2 + "." + type, targetmoney.get(type) + ammount );
		plugin.saveMoneyConfig();
		
		return true;
	}
	
	public Double getConversion( String from, String to ) {
		return plugin.moneyConfig.getDouble( "conversion." + from + "." + to );
	}
	public Boolean setConversion( String from, String to, Long conv ) {
		if( conv == null || conv <= 0 || moneyTypes.contains(from) == false || moneyTypes.contains(to) == false ) return false;
		
		Double converted = 1.0/conv;
		
		plugin.moneyConfig.set( "conversion." + from + "." + to, conv);
		plugin.moneyConfig.set( "conversion." + to + "." + from, converted );
		plugin.saveMoneyConfig();
		
		return true;
	}
	
	public List<Player> getNearbyPlayers( Player player, int len ) {
		Collection<Entity> nearbyPlayers = player.getWorld().getNearbyEntities( player.getLocation(), len, len, len );
		
		return nearbyPlayers.stream()
				.filter(e -> e instanceof Player && !e.getName().equals( player.getName() ) )
				.map(e-> (Player) e)
				.collect( Collectors.toList() );
	}
	public List<Player> getNearbyPlayers( Location loc, int len ) {
		Collection<Entity> nearbyPlayers = loc.getWorld().getNearbyEntities( loc, len, len, len );
		
		return nearbyPlayers.stream()
				.filter(e -> e instanceof Player )
				.map(e-> (Player) e)
				.collect( Collectors.toList() );
	}
	
	public Boolean isWithinReach( Player player, Player target, Long len) {
		return player.getLocation().distance(target.getLocation()) < len;
	}
	public Boolean isWithinReach( Player player, String target, Long len) {
		List<Player> playerList = player.getWorld().getPlayers();
		Player ptarget = null;
		
		for( Player p : playerList )
			if( p.getName().equals(target)) 
				ptarget = p;
		
		return ptarget != null ? player.getLocation().distance(ptarget.getLocation()) < len : false;
	}
	
	
	public void setTPL(Player player, String level) {
		plugin.tpl.set(player.getUniqueId().toString() + ".level", level);
		plugin.saveTPLFile();
	}
	public String getLevel(Player player) {
		String level = "0";
		
		if( plugin.tpl.contains(player.getUniqueId().toString() + ".level") ) {
			level = (String) plugin.tpl.get(player.getUniqueId().toString() + ".level");			
		} else {
			setTPL(player, level);
		}
		
		return level;
	}
	
    public void setOTP(Player player, String name) {
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".X", player.getLocation().getX());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Y", player.getLocation().getY());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Z", player.getLocation().getZ());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Yaw", player.getLocation().getYaw());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".Pitch", player.getLocation().getPitch());
        plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name + ".World", player.getLocation().getWorld().getName());
        plugin.saveOTPFile();
    }
    
	public Map<String, Object> getOTP(Player player) {
		if( plugin.tps.getConfigurationSection( "tps." + player.getUniqueId().toString() ) == null )
			return new HashMap<String, Object>();
		
    	Map<String, Object> list = plugin.tps.getConfigurationSection("tps." + player.getUniqueId().toString() ).getValues( false ); 
    	return list;
    }
	
	public Boolean setType(Player player, String type) {
		if( types.contains( type ) == false ) return false;
		
		plugin.tpl.set( player.getUniqueId().toString() + ".type", type);
		plugin.saveTPLFile();
		
		return true;
	}
	public String getType(Player player) {
		String type = ""; 
		if( plugin.tpl.contains( player.getUniqueId().toString() + ".type") )
			type = plugin.tpl.getString( player.getUniqueId().toString() + ".type" );
		return type;
	}
	
    public Boolean sendOTP(Player player, String name) {
    	World world = player.getWorld();
    	Location tpLoc = getOTPLocation( player, name );
        
        world.playSound( tpLoc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1 );
        
        List<Player> sendTo = getNearbyPlayers(tpLoc, 15);
        for( Player sender : sendTo )
        	sender.sendMessage( ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD + " [Niedaleko słychać odgłos teleportacji]");
        
        boolean send = player.teleport( tpLoc );
        
        return send;
    }
    public Boolean sendOTP(Player player, String name, Player target) {
    	World world = player.getWorld();
    	Location tpLoc = getOTPLocation( player, name );
    	
    	world.playSound( tpLoc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1 );
        List<Player> sendTo = getNearbyPlayers(tpLoc, 15);
        for( Player sender : sendTo )
        	sender.sendMessage( ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD + " [Niedaleko słychać odgłos teleportacji]");
        
        boolean send = target.teleport( tpLoc );
        
        return send;
    }
    public Boolean sendOTP(Player player, Location loc) {
    	World world = player.getWorld();
        
        world.playSound( loc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1 );
        
        List<Player> sendTo = getNearbyPlayers(loc, 15);
        for( Player sender : sendTo )
        	sender.sendMessage( ChatColor.DARK_GRAY + "[L]" + ChatColor.GOLD + " [Niedaleko słychać odgłos teleportacji]");
        
        boolean send = player.teleport( loc );
        
        return send;
    }
	
    public Location getOTPLocation(Player player, String name) {
        return new Location(
                Bukkit.getWorld(plugin.tps.getString("tps." + player.getUniqueId().toString() + "." + name + ".World")),
                plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".X"),
                plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".Y"),
                plugin.tps.getDouble("tps." + player.getUniqueId().toString() + "." + name + ".Z"),
                plugin.tps.getLong("tps." + player.getUniqueId().toString() + "." + name + ".Yaw"),
                plugin.tps.getLong("tps." + player.getUniqueId().toString() + "." + name + ".Pitch")
        );
    }
    
    public void deleteOTP(Player player, String name) {
    	plugin.tps.set("tps." + player.getUniqueId().toString() + "." + name, null);
        plugin.saveOTPFile();
    	return;
    }

    public boolean tpsIsNull(Player player, String name) {
        return plugin.tps.getString( "tps." + player.getUniqueId() + "." + name ) == null;
    }
    
    public List<Player> getPlayers() {
    	return plugin.server.getWorlds().get(0).getPlayers();
    }
    
	public void tpEffect( Player player, String locName, Player target ) { 
		executeTP( player, locName, target );
	}
	public void tpEffect( Player player, String locName ) {
		executeTP( player, locName, null );
	}
	public void tpEffect( Player player, Location loc ) {
		executeTP( player, loc );
	}
    
	void executeTP( Player player, String locName, Player target ) {
		Player tpd = target != null? target : player;
		
		Effect tpParticles = getPlayerEffect( tpd );
		tpParticles.duration = 4 * 20;
		tpParticles.callback = new Runnable() {
            @Override
            public void run() {
            	if( locName != null && !locName.isEmpty() ) {
                	if( target != null ) {
                		sendOTP( player, locName, target );
                		tpEffect( player, null, target );
                	}
                	else {
                		sendOTP( player, locName );
                		tpEffect( player, null, target );
                	}
            	}
            }
		};
		player.getWorld().playSound( tpd.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1 );
		
		tpParticles.start();
	}
	void executeTP( Player player, Location loc ) {
		Effect tpParticles = getPlayerEffect( player );
		tpParticles.duration = 4 * 20;
		tpParticles.callback = new Runnable() {
            @Override
            public void run() {
                sendOTP( player, loc );
                tpEffect( player, null, null );
            }
		};
		player.getWorld().playSound( player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1 );
		
		tpParticles.start();
	}
	
	public void itemTP( Player player, NBTCompound comp ) {
		
		// String enchanter = comp.getString("enchanter");
		int maxLen = comp.getInteger("maxLength");
		int enchCld = comp.getInteger("cld");
		int maxUse = comp.hasKey("maxUse")? comp.getInteger("maxUse"): Integer.MAX_VALUE;
		int used = comp.hasKey("used")? comp.getInteger("used"): 0;
		// location
		double x = comp.getDouble("x");
		double y = comp.getDouble("y");
		double z = comp.getDouble("z");
		float yaw = comp.getFloat("yaw");
		float pitch = comp.getFloat("pitch");
		
		Location loc = new Location(player.getWorld(), x, y, z, yaw, pitch);
		
		if( player.getLocation().distance(loc) > maxLen ) {
			player.sendMessage( Main.prefixError + "Nie można użyć przedmiotu, za daleko!" );
			return;
		}
		
		if( used >= maxUse ) {
			player.sendMessage( Main.prefixError + "Nie można już użyć przedmiotu!" );
			return;
		}
		
		
		if ( cooldownTimeOTP.containsKey(player) ) {
			player.sendMessage( Main.prefixError + "Musisz odpocząć " + ChatColor.RED + cooldownTimeOTP.get(player) + ChatColor.GRAY + " sekund.");
		} else {
			if( comp.hasKey("maxUse") ) {
				used++;
				comp.setInteger("used", used);
			}

			tpEffect(player, loc);
			setCoolDownTimeOTP(player, enchCld);
			player.sendMessage( Main.prefixInfo + "Teleportowano do punktu z przedmiotu!" );
		}
		
	}
    
	public Effect getPlayerEffect( Player player ) {
		Effect ef;
		String type = getType( player );
		
		if( type.equals("ignis") ) {
			TornadoEffect tpParticles = new TornadoEffect( plugin.effectManager );
			tpParticles.setEntity(player);
			tpParticles.tornadoHeight = (float) 2.4;
			tpParticles.maxTornadoRadius = (float) 1.5;
			tpParticles.yOffset = -2;
			tpParticles.showCloud = false;
			
			return tpParticles;
		} else if( type.equals("caligo") ) {
			VortexEffect tpParticles = new VortexEffect( plugin.effectManager );
			tpParticles.setEntity(player);
			tpParticles.particle = Particle.SMOKE_LARGE;
			tpParticles.radius = (float) 1.5;
			tpParticles.radials = 30;
			tpParticles.circles = 30;
			tpParticles.helixes = 30;
			
			return tpParticles;
		} else if ( type.equals("lux") ) {
			DonutEffect tpParticles = new DonutEffect( plugin.effectManager );
			tpParticles.setEntity(player);
			tpParticles.radiusDonut = (float) 1.8;
			tpParticles.particle = Particle.TOTEM;
			
			return tpParticles;
		} else if ( type.equals("aeter") ) {
			TornadoEffect tpParticles = new TornadoEffect( plugin.effectManager );
			tpParticles.setEntity(player);
			tpParticles.tornadoHeight = (float) 2.4;
			tpParticles.maxTornadoRadius = (float) 1.5;
			tpParticles.yOffset = -1;
			tpParticles.showTornado = false;
			
			return tpParticles;
		} else if ( type.equals("aqua") ) {
			FountainEffect tpParticles = new FountainEffect( plugin.effectManager );
			tpParticles.setEntity(player);
			tpParticles.radius = 2;
			tpParticles.height = 2;
			tpParticles.radiusSpout = 1;
			tpParticles.heightSpout = 2;
			
			return tpParticles;
		} else if ( type.equals("geo") ) {
			EarthEffect tpParticles = new EarthEffect( plugin.effectManager );
			tpParticles.setEntity(player);
			tpParticles.radius = (float) 1.5;
			
			return tpParticles;
		} else if ( type.equals("electro") ) {
			StarEffect tpParticles = new StarEffect( plugin.effectManager );
			tpParticles.setEntity(player);
			tpParticles.innerRadius = (float) 0.5;
			tpParticles.spikeHeight = (float) 2;
			tpParticles.particle = Particle.SPELL_WITCH;
			tpParticles.color = Color.PURPLE;
			
			return tpParticles;
		} else {
			TornadoEffect tpParticles = new TornadoEffect( plugin.effectManager );
			tpParticles.setEntity(player);
			tpParticles.tornadoHeight = (float) 2.4;
			tpParticles.maxTornadoRadius = (float) 1.5;
			tpParticles.yOffset = -2;
			tpParticles.showCloud = false;
			
			ef = tpParticles;
		}
		
		return ef;
	}
	
	
	public void setCoolDownTimeOTP(Player player, int coolDown) {
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
    public void setCoolDownTimeSetOTP(Player player, int coolDown) {
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
	
}
