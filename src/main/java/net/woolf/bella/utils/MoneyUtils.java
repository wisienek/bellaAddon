package net.woolf.bella.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.woolf.bella.Main;

public class MoneyUtils {

	public static final Set<String> moneyTypes = new HashSet<String>(
			Arrays.asList( "miedziak", "srebrnik", "złotnik" ) );
	private Main plugin;

	public MoneyUtils(
			Main main
	) {
		this.plugin = main;
	}

	public Boolean isNearBank(
			Location loc
	) {
		if ( loc == null || loc instanceof Location == false )
			return false;
		List<Location> banks = getBanks();

		for ( Location bank : banks ) {
			if ( bank == null ) {
				plugin.logger.info( "Bank null!" );
				continue;
			} else {
				if ( loc.distance( bank ) <= 20 )
					return true;
			}
		}

		return false;
	}

	public Location getNearestBank(
			Location loc
	) {
		if ( loc == null || loc instanceof Location == false )
			return null;

		List<Location> banks = getBanks();

		for ( Location bank : banks ) {
			if ( bank == null ) {
				plugin.logger.info( "Bank null!" );
				continue;
			} else {
				if ( loc.distance( bank ) <= 20 )
					return bank;
			}
		}

		return null;
	}

	public Boolean createBank(
			Location loc
	) {
		List<Location> banks = getBanks();

		for ( Location bank : banks )
			if ( bank != null )
				if ( loc.distance( bank ) < 50 )
					return false;

		List<String> locs = new ArrayList<String>();

		banks.add( loc );

		for ( Location bank : banks )
			locs.add( bank.getWorld().getName() + " " + bank.getX() + " " + bank.getY() + " "
					+ bank.getZ() );

		plugin.moneyConfig.set( "banks", locs );

		plugin.saveMoneyConfig();
		return true;
	}

	public Boolean deleteBank(
			Player player
	) {
		if ( player == null )
			return false;

		Location nearestBank = getNearestBank( player.getLocation() );
		if ( nearestBank == null )
			return false;

		List<Location> banks = getBanks();
		List<String> locs = new ArrayList<String>();
		Boolean deleted = false;

		for ( Location bank : banks ) {
			if ( bank != null ) {
				if (
					bank.getX() == nearestBank.getX() && bank.getY() == nearestBank.getY()
							&& bank.getZ() == nearestBank.getZ()
				) {
					plugin.logger.info( "Usunięto bank!" );
					deleted = true;
				} else
					locs.add( bank.getWorld().getName() + " " + bank.getX() + " " + bank.getY()
							+ " " + bank.getZ() );
			}
		}

		plugin.moneyConfig.set( "banks", locs );
		plugin.saveMoneyConfig();

		return deleted;
	}

	public List<Location> getBanks() {
		List<?> banksList = plugin.moneyConfig.getList( "banks" );
		List<Location> banks = new ArrayList<Location>();

		if ( banksList == null || banksList.size() == 0 )
			return banks;

		for ( Object item : banksList ) {
			if ( item != null && item instanceof String == true ) {
				String[] args = ( (String) item ).split( " " );

				banks.add( new Location( Bukkit.getWorld( args[0] ), Double.valueOf( args[1] ),
						Double.valueOf( args[2] ), Double.valueOf( args[3] ) ) );
			}
		}

		return banks;
	}

	public Map<String, Long> getMoney(
			@Nonnull Object player
	) {
		String uuid = ( player instanceof String ) ? (String) player
				: ( (Player) player ).getUniqueId().toString();

		if ( uuid == null ) {
			this.plugin.logger.warning( "UUID resolved to null! Player: "
					+ ( player instanceof String ? player : ( (Player) player ).getName() ) );
			return null;
		}

		Map<String, Long> money = new HashMap<String, Long>();

		Long miedziak = plugin.moneyConfig.getLong( "personal." + uuid + ".miedziak" );
		Long srebrnik = plugin.moneyConfig.getLong( "personal." + uuid + ".srebrnik" );
		Long złotnik = plugin.moneyConfig.getLong( "personal." + uuid + ".złotnik" );

		money.put( "miedziak", ( miedziak != null ) ? miedziak : 0l );
		money.put( "srebrnik", ( srebrnik != null ) ? srebrnik : 0l );
		money.put( "złotnik", ( złotnik != null ) ? złotnik : 0l );

		return money;
	}

	public Map<String, Long> getBankMoney(
			Object player
	) {
		String uuid = ( player instanceof String ) ? (String) player
				: ( (Player) player ).getUniqueId().toString();
		Map<String, Long> money = new HashMap<String, Long>();

		Long miedziak = plugin.moneyConfig.getLong( "bank." + uuid + ".miedziak" );
		Long srebrnik = plugin.moneyConfig.getLong( "bank." + uuid + ".srebrnik" );
		Long złotnik = plugin.moneyConfig.getLong( "bank." + uuid + ".złotnik" );

		money.put( "miedziak", ( miedziak != null ) ? miedziak : 0l );
		money.put( "srebrnik", ( srebrnik != null ) ? srebrnik : 0l );
		money.put( "złotnik", ( złotnik != null ) ? złotnik : 0l );

		return money;
	}

	public Boolean setMoney(
			String uuid, String type, Long ammount
	) {
		if ( uuid == null || moneyTypes.contains( type ) == false || ammount == null )
			return false;

		plugin.moneyConfig.set( "personal." + uuid + "." + type, ammount );
		plugin.saveMoneyConfig();

		return true;
	}

	public Boolean setBankMoney(
			String uuid, String type, Long ammount
	) {
		if ( uuid == null || moneyTypes.contains( type ) == false || ammount == null )
			return false;

		plugin.moneyConfig.set( "bank." + uuid + "." + type, ammount );
		plugin.saveMoneyConfig();

		return true;
	}

	public Boolean transferToBank(
			Player player, String type, Long ammount, Map<String, Long> money,
			Map<String, Long> bankMoney
	) {
		if ( moneyTypes.contains( type ) == false )
			return false;
		if ( money == null )
			money = getMoney( player );
		if ( bankMoney == null )
			bankMoney = getBankMoney( player );

		Long rest = money.get( type ) - ammount;
		if ( rest < 0 )
			return false;

		Boolean check1 = setMoney( player.getUniqueId().toString(), type, rest );
		if ( check1 == false )
			return false;
		Boolean check2 = setBankMoney( player.getUniqueId().toString(), type,
				bankMoney.get( type ) + ammount );
		if ( check2 == false )
			return false;

		return true;
	}

	public Boolean transferFromBank(
			Player player, String type, Long ammount, Map<String, Long> money,
			Map<String, Long> bankMoney
	) {
		if ( moneyTypes.contains( type ) == false )
			return false;
		if ( money == null )
			money = getMoney( player );
		if ( bankMoney == null )
			bankMoney = getBankMoney( player );

		Long rest = bankMoney.get( type ) - ammount;
		if ( rest < 0 )
			return false;

		Boolean check1 = setBankMoney( player.getUniqueId().toString(), type, rest );
		if ( check1 == false )
			return false;
		Boolean check2 = setMoney( player.getUniqueId().toString(), type,
				money.get( type ) + ammount );
		if ( check2 == false )
			return false;

		return true;
	}

	public Boolean transferMoney(
			Object from, Object to, String type, Long ammount
	) {
		if ( moneyTypes.contains( type ) == false )
			return false;
		if ( from == null || to == null || ammount <= 0 )
			return false;

		String uuid1 = ( from instanceof String ) ? (String) from
				: ( (Player) from ).getUniqueId().toString();
		String uuid2 = ( to instanceof String ) ? (String) to
				: ( (Player) to ).getUniqueId().toString();

		Map<String, Long> money = getMoney( uuid1 );
		Map<String, Long> targetmoney = getMoney( uuid2 );

		Long has = money.get( type );
		if ( has < ammount || has - ammount < 0 )
			return false;

		plugin.moneyConfig.set( "personal." + uuid1 + "." + type, has - ammount );
		plugin.moneyConfig.set( "personal." + uuid2 + "." + type,
				targetmoney.get( type ) + ammount );
		plugin.saveMoneyConfig();

		// updateMoneyScore(uuid1);
		// updateMoneyScore(uuid2);

		return true;
	}

	public Boolean transferBankMoney(
			Object from, Object to, String type, Long ammount
	) {
		if ( moneyTypes.contains( type ) == false )
			return false;
		if ( from == null || to == null || ammount <= 0 )
			return false;

		String uuid1 = ( from instanceof String ) ? (String) from
				: ( (Player) from ).getUniqueId().toString();
		String uuid2 = ( to instanceof String ) ? (String) to
				: ( (Player) to ).getUniqueId().toString();

		Map<String, Long> money = getBankMoney( from );
		Map<String, Long> targetmoney = getBankMoney( to );

		Long has = money.get( type );
		if ( has < ammount || has - ammount < 0 )
			return false;

		plugin.moneyConfig.set( "bank." + uuid1 + "." + type, has - ammount );
		plugin.moneyConfig.set( "bank." + uuid2 + "." + type, targetmoney.get( type ) + ammount );
		plugin.saveMoneyConfig();

		return true;
	}

	public Double getConversion(
			String from, String to
	) {
		return plugin.moneyConfig.getDouble( "conversion." + from + "." + to );
	}

	public Boolean setConversion(
			String from, String to, Long conv
	) {
		if (
			conv == null || conv <= 0 || moneyTypes.contains( from ) == false
					|| moneyTypes.contains( to ) == false
		)
			return false;

		Double converted = 1.0 / conv;

		plugin.moneyConfig.set( "conversion." + from + "." + to, conv );
		plugin.moneyConfig.set( "conversion." + to + "." + from, converted );
		plugin.saveMoneyConfig();

		return true;
	}
}
