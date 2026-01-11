package net.woolf.bella.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import Types.SqlQueries;
import net.woolf.bella.Main;
import net.woolf.bella.models.AccountLinkModel;
import net.woolf.bella.utils.DbUtils;

public class AccountLinkRepository {

  private final DbUtils dbUtils;

  public AccountLinkRepository(
      DbUtils dbUtils
  ) {
    this.dbUtils = dbUtils;
  }

  public boolean removeAccount(
      @Nonnull String uuid
  ) {
    Map<String, String[]> check = getConnectedAccounts( uuid );

    if ( check == null || !check.containsKey( uuid ) )
      return false;

    dbUtils.logger.info( "[GameLinkAccount] - REMOVING dc link for uuid: " + uuid );

    try (
        var st = dbUtils.connection
            .prepareStatement( SqlQueries.DELETE_ACCOUNT_CONNECTION.toString() )
    ) {
      st.setString( 1, uuid );
      st.setString( 2, check.get( uuid )[1] );

      int rs = st.executeUpdate();

      return rs != 0;
    } catch ( SQLException e ) {
      e.printStackTrace();
      return false;
    }
  }

  @Nullable
  public String connectAccount(
      @Nonnull String discordId,
      @Nonnull String uuid
  ) {
    Map<String, String[]> check = getConnectedAccounts( uuid );

    if ( check != null && check.size() > 0 ) {
      dbUtils.logger.info( "Gracz dcid - " + discordId
          + " próbował podlinkować już połączone uuid: " + uuid );
      return "Konto z uuid: " + uuid + " Jest już podłączone do";
    }

    Player player = Main.getInstance().putils.resolveUUIDToOnlinePlayer( uuid );

    if ( player == null ) {
      Main.getInstance().logger.info( "ERROR Nie znaleziono gracza z uuid: " + uuid );
      return "Nie znaleziono online konta z uuid: " + uuid;
    }

    try ( var st = dbUtils.connection.prepareStatement( SqlQueries.CONNECT_ACCOUNT.toString() ) ) {
      String name = player.getName();

      if ( name == null || uuid.equals( name ) ) {
        dbUtils.logger.info( "[GameLinkAccount] - Name same as UUID: " + uuid );
        return "Nieoczekiwany błąd!";
      }

      dbUtils.logger.info( "[GameLinkAccount] - Creating link ('" + discordId + "', '" + uuid
          + "', '" + name + "');" );

      st.setString( 1, discordId );
      st.setString( 2, uuid );
      st.setString( 3, name );

      int rs = st.executeUpdate();

      if ( rs == 0 ) {
        dbUtils.logger
            .info( "[GameLinkAccount] - ERROR przy dodawaniu uuid do discorda! Nie dodano pola! ("
                + rs + ")" );

        return "Niepoprawny wynik query: " + rs;
      }

      dbUtils.logger
          .info( "[GameLinkAccount] - Dodano " + uuid + " (" + name + ") do dc: " + discordId );

      return "Podłączono Konto " + name + " (`" + uuid + "`) z discordem!";
    } catch ( SQLException e ) {
      dbUtils.logger.info( "[GameLinkAccount] - ERROR przy dodawaniu uuid do discorda!" );
      e.printStackTrace();

      return "ERROR z query do bazy danych!";
    }
  }

  @Nullable
  public Map<String, String[]> getConnectedAccounts(
      @Nonnull String uuid
  ) {
    if ( uuid.length() == 0 ) {
      Main.getInstance().logger.info( "No key or id!" );
      return null;
    }

    try (
        var st = dbUtils.connection.prepareStatement( SqlQueries.GET_CONNECTED_ACCOUNT.toString() )
    ) {
      st.setString( 1, uuid );

      ResultSet rs = st.executeQuery();

      Map<String, String[]> accounts = new HashMap<>();
      while ( rs.next() ) {
        String dcid = rs.getString( "discordId" );
        String playerUUID = rs.getString( "uuid" );
        String playerName = rs.getString( "playerName" );

        if (
          dcid == null || dcid.length() == 0 || playerUUID == null || playerUUID.length() == 0
              || playerName == null || playerName.length() == 0
        ) {
          dbUtils.logger.info( "nullish values: " + dcid + ", " + playerUUID + ", " + playerName
              + " for: " + uuid );
        } else {
          String[] pdata = { playerName, dcid };
          accounts.put( playerUUID, pdata );

          dbUtils.logger.info( "Connected account: " + dcid + " <-> " + playerUUID + " ("
              + playerName + ")" );
        }
      }

      return accounts;
    } catch ( SQLException e ) {
      dbUtils.logger.info( "Error while fetching connected accounts!" );
      e.printStackTrace();
    }

    return null;
  }
}
