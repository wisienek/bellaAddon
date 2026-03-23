package net.woolf.bella.bridge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.woolf.bella.Main;

import java.util.Map;

public class BridgeHandler {

  private static final Gson GSON = new Gson();
  private final Main plugin;

  public BridgeHandler(
      Main plugin
  ) {
    this.plugin = plugin;
  }

  public String handle(
      String json
  ) {
    JsonObject result = new JsonObject();
    try {
      JsonObject req = JsonParser.parseString( json ).getAsJsonObject();
      String action = req.get( "action" ).getAsString();
      if ( req.has( "id" ) )
        result.addProperty( "id", req.get( "id" ).getAsString() );

      switch ( action ) {
        case "ping" -> result.addProperty( "pong", true );
        case "getBalance" -> handleGetBalance( req, result );
        case "trySpend" -> handleTrySpend( req, result );
        case "deposit" -> handleDeposit( req, result );
        case "getConversions" -> handleGetConversions( result );
        default -> result.addProperty( "error", "Unknown action: " + action );
      }
    } catch ( Exception e ) {
      result.addProperty( "error", e.getMessage() );
    }
    return GSON.toJson( result );
  }

  private void handleGetBalance(
      JsonObject req,
      JsonObject result
  ) {
    String uuid = req.get( "uuid" ).getAsString();
    Map<String, Long> purse = plugin.mutils.getMoney( uuid );
    Map<String, Long> bank = plugin.mutils.getBankMoney( uuid );

    JsonObject purseObj = new JsonObject();
    JsonObject bankObj = new JsonObject();

    if ( purse != null )
      purse.forEach( purseObj::addProperty );
    if ( bank != null )
      bank.forEach( bankObj::addProperty );

    result.add( "purse", purseObj );
    result.add( "bank", bankObj );
  }

  private void handleTrySpend(
      JsonObject req,
      JsonObject result
  ) {
    String uuid = req.get( "uuid" ).getAsString();
    String type = req.get( "type" ).getAsString();
    long amount = req.get( "amount" ).getAsLong();

    boolean success = plugin.mutils.trySpendWithConversion( uuid, type, amount );
    result.addProperty( "success", success );
    if ( !success )
      result.addProperty( "reason", "Insufficient funds" );
  }

  private void handleDeposit(
      JsonObject req,
      JsonObject result
  ) {
    String uuid = req.get( "uuid" ).getAsString();
    String type = req.get( "type" ).getAsString();
    int amount = req.get( "amount" ).getAsInt();

    plugin.mutils.giveMoney( uuid, type, amount );
    result.addProperty( "success", true );
  }

  private void handleGetConversions(
      JsonObject result
  ) {
    JsonObject convs = new JsonObject();
    String[] types = { "koga", "loren", "auren" };
    for ( String from : types ) {
      for ( String to : types ) {
        if ( from.equals( to ) )
          continue;
        Double rate = plugin.mutils.getConversion( from, to );
        if ( rate != null && rate > 0 )
          convs.addProperty( from + "_" + to, rate );
      }
    }
    result.add( "conversions", convs );
  }
}
