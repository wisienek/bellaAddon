package net.woolf.bella.bridge;

import java.util.Map;
import java.util.UUID;

import net.bella.bridge.api.IEconomyBridge;
import net.woolf.bella.Main;

public class EconomyBridgeImpl implements IEconomyBridge {

  private final Main plugin;

  public EconomyBridgeImpl(Main plugin) {
    this.plugin = plugin;
  }

  @Override
  public double getBalance(UUID playerUuid, String currencyType) {
    Map<String, Long> purse = plugin.mutils.getMoney( playerUuid.toString() );
    return purse != null ? purse.getOrDefault( currencyType, 0L ).doubleValue() : 0.0;
  }

  @Override
  public boolean withdraw(UUID playerUuid, String currencyType, double amount) {
    if ( amount <= 0 )
      return true;
    return plugin.mutils.trySpendWithConversion( playerUuid.toString(), currencyType, (long) amount );
  }

  @Override
  public boolean deposit(UUID playerUuid, String currencyType, double amount) {
    if ( amount <= 0 )
      return true;
    plugin.mutils.giveMoney( playerUuid.toString(), currencyType, (int) amount );
    return true;
  }

  @Override
  public boolean has(UUID playerUuid, String currencyType, double amount) {
    if ( amount <= 0 )
      return true;

    Map<String, Long> purse = plugin.mutils.getMoney( playerUuid.toString() );
    if ( purse == null )
      return false;

    long required = (long) Math.ceil( amount );
    long direct = purse.getOrDefault( currencyType, 0L );
    if ( direct >= required )
      return true;

    Map<String, Long> working = new java.util.HashMap<>( purse );
    long deficit = required - direct;
    working.put( currencyType, 0L );

    String[] spendOrder = { "auren", "loren", "koga" };
    for ( String other : spendOrder ) {
      if ( other.equals( currencyType ) || deficit <= 0 )
        continue;

      Double convRate = plugin.mutils.getConversion( other, currencyType );
      if ( convRate == null || convRate <= 0 )
        continue;

      long haveOther = working.getOrDefault( other, 0L );
      if ( haveOther <= 0 )
        continue;

      long canGet = (long) ( haveOther / convRate );
      if ( canGet <= 0 )
        continue;

      long toGet = Math.min( canGet, deficit );
      long toSpend = (long) Math.ceil( toGet * convRate );
      toSpend = Math.min( toSpend, haveOther );
      long actualGet = (long) ( toSpend / convRate );

      working.put( other, haveOther - toSpend );
      deficit -= actualGet;
    }

    return deficit <= 0;
  }

  @Override
  public double getConversion(
      String fromCurrency,
      String toCurrency
  ) {
    if ( fromCurrency == null || toCurrency == null || fromCurrency.equals( toCurrency ) )
      return 0.0;
    Double d = plugin.mutils.getConversion( fromCurrency, toCurrency );
    return d != null && d > 0 ? d : 0.0;
  }

  @Override
  public String getCurrencyName(String currencyType) {
    if ( currencyType == null || currencyType.isEmpty() )
      return "Currency";
    return currencyType.substring( 0, 1 ).toUpperCase() + currencyType.substring( 1 );
  }
}
