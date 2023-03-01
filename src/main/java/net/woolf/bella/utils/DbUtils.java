package net.woolf.bella.utils;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import Types.SqlQueries;
import classes.Backpack;
import net.woolf.bella.Main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DbUtils {

	private static final String BackPackPrefix = "[BackPack]";

	private static final String ConnectionOptions = "useUnicode=yes&characterEncoding=UTF-8";
	private static String BaseURL = "jdbc:mysql://";
	private static String Host = "sql.pukawka.pl";
	private static String Port = "3306";
	private static String Database = "";
	private static String User = "";
	private static String Password = "";

	private static DbUtils instance;

	public Connection connection;

	private final Logger logger = Main.getInstance().logger;
	private final Boolean isTest = Main.getInstance().isTest();

	private DbUtils() {
		this.connection = null;
	}

	public static DbUtils getInstance() throws SQLException, IOException {
		if ( DbUtils.instance == null ) {
			DbUtils.instance = new DbUtils();
		}

		DbUtils.instance.initConnection();

		return DbUtils.instance;
	}

	public void initConnection() throws SQLException, IOException {
		Map<String, String> config = FileReader.getDBConfig();
		BaseURL = config.get( "BaseURL" );
		Host = config.get( "Host" );
		Port = config.get( "Port" );
		Database = config.get( "Database" );
		User = config.get( "User" );
		Password = config.get( "Password" );

		if ( BaseURL == null || BaseURL.length() == 0 ) {
			this.logger.info( "No BaseURL for connection!" );
			return;
		}
		if ( Host == null || Host.length() == 0 ) {
			this.logger.info( "No Host for connection!" );
			return;
		}
		if ( Port == null || Port.length() == 0 ) {
			this.logger.info( "No Port for connection!" );
			return;
		}
		if ( Database == null || Database.length() == 0 ) {
			this.logger.info( "No Database for connection!" );
			return;
		}
		if ( BaseURL == null || BaseURL.length() == 0 ) {
			this.logger.info( "No BaseURL for connection!" );
			return;
		}
		if ( User == null || User.length() == 0 ) {
			this.logger.info( "No User for connection!" );
			return;
		}
		if ( Password == null || Password.length() == 0 ) {
			this.logger.info( "No User for connection!" );
			return;
		}

		String connectionUrl = String
				.format( "%s%s:%s/%s?autoReconnect=true&%s", BaseURL, Host, Port, Database, ConnectionOptions );

		if ( Main.getInstance().isTest() )
			connectionUrl += "&useSSL=false";

		this.connection = DriverManager.getConnection( connectionUrl, User, Password );

		this.logger.info( "Initiated DB connection!" );
	}

	public boolean removeAccount(
			@Nonnull String uuid
	) {
		Map<String, String[]> check = this.getConnectedAccounts( uuid );

		if ( check == null || check.containsKey( uuid ) == false )
			return false;

		this.logger.info( "[GameLinkAccount] - REMOVING dc link for uuid: " + uuid );

		PreparedStatement st = null;

		try {
			st = this.connection
					.prepareStatement( SqlQueries.DELETE_ACCOUNT_CONNECTION.toString() );
			st.setString( 0, uuid );
			st.setString( 1, check.get( uuid )[1] );

			int rs = st.executeUpdate();

			return rs != 0;
		} catch ( SQLException e ) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				st.close();
			} catch ( Exception e ) {
			}
		}
	}

	public String connectAccount(
			@Nonnull String discordId,
			@Nonnull String uuid
	) {
		Map<String, String[]> check = this.getConnectedAccounts( uuid );

		if ( check != null && check.size() > 0 ) {
			this.logger.info( "Gracz dcid - " + discordId
					+ " próbował podlinkować już połączone uuid: " + uuid );
			return "Konto z uuid: " + uuid + " Jest już podłączone do";
		}

		Player player = Main.getInstance().putils.resolveUUIDToOnlinePlayer( uuid );

		if ( player == null ) {
			Main.getInstance().logger.info( "ERROR Nie znaleziono gracza z uuid: " + uuid );
			return "Nie znaleziono online konta z uuid: " + uuid;
		}

		PreparedStatement st = null;

		try {
			String name = player == null ? null : player.getName();

			if ( name == null || uuid.equals( name ) ) {
				this.logger.info( "[GameLinkAccount] - Name same as UUID: " + uuid );
				return "Nieoczekiwany błąd!";
			}

			this.logger.info( "[GameLinkAccount] - Creating link ('" + discordId + "', '" + uuid
					+ "', '" + name + "');" );

			st = this.connection.prepareStatement( SqlQueries.CONNECT_ACCOUNT.toString() );
			st.setString( 1, discordId );
			st.setString( 2, uuid );
			st.setString( 3, name );

			int rs = st.executeUpdate();

			if ( rs == 0 ) {
				this.logger
						.info( "[GameLinkAccount] - ERROR przy dodawaniu uuid do discorda! Nie dodano pola! ("
								+ String.valueOf( rs ) + ")" );

				return "Niepoprawny wynik query: " + String.valueOf( rs );
			}

			this.logger.info( "[GameLinkAccount] - Dodano " + uuid + " (" + name + ") do dc: "
					+ discordId );

			return "Podłączono Konto " + name + " (`" + uuid + "`) z discordem!";
		} catch ( SQLException e ) {
			this.logger.info( "[GameLinkAccount] - ERROR przy dodawaniu uuid do discorda!" );
			e.printStackTrace();

			return "ERROR z query do bazy danych!";
		} finally {
			try {
				st.close();
			} catch ( Exception e ) {
			}
		}
	}

	public Map<String, String[]> getConnectedAccounts(
			@Nonnull String uuid
	) {
		if ( uuid == null || uuid.length() == 0 ) {
			Main.getInstance().logger.info( "No key or id!" );
			return null;
		}

		PreparedStatement st = null;

		try {
			st = this.connection.prepareStatement( SqlQueries.GET_CONNECTED_ACCOUNT.toString() );
			st.setString( 1, uuid );

			ResultSet rs = st.executeQuery();

			Map<String, String[]> accounts = new HashMap<String, String[]>();
			while ( rs.next() ) {
				String dcid = rs.getString( "discordId" );
				String playerUUID = rs.getString( "uuid" );
				String playerName = rs.getString( "playerName" );

				if (
					dcid == null || dcid.length() == 0 || playerUUID == null
							|| playerUUID.length() == 0 || playerName == null
							|| playerName.length() == 0
				) {
					this.logger.info( "nullish values: " + dcid + ", " + playerUUID + ", "
							+ playerName + " for: " + uuid );
				} else {
					String[] pdata = { playerName, dcid };
					accounts.put( playerUUID, pdata );

					this.logger.info( "Connected account: " + dcid + " <-> " + playerUUID + " ("
							+ playerName + ")" );
				}
			}

			return accounts;
		} catch ( SQLException e ) {
			this.logger.info( "Error while fetching connected accounts!" );
			e.printStackTrace();
		} finally {
			try {
				st.close();
			} catch ( Exception e ) {
			}
		}

		return null;
	}

	public Backpack getBackpackInfo(
			@Nonnull String baguuid
	) {
		if ( baguuid == null || baguuid.length() == 0 ) {
			Main.getInstance().logger.info( "No key or id!" );
			return null;
		}

		PreparedStatement st = null;

		try {
			st = this.connection.prepareStatement( SqlQueries.GET_BACKPACK.toString() );
			st.setString( 1, baguuid );

			ResultSet rs = st.executeQuery();

			while ( rs.next() ) {
				String uuid = rs.getString( "uuid" );
				String inventoryName = rs.getString( "inventoryName" );
				String itemData = rs.getString( "itemData" );
				Inventory inv = null;

				if ( itemData != null && itemData.length() > 0 )
					inv = InventorySerializer.StringToInventory( itemData );

				Backpack bp = inv == null ? new Backpack()
						: new Backpack( inv.getSize(), uuid, inventoryName );

				bp.setBagID( uuid );
				bp.setTitle( inventoryName );

				if ( inv != null )
					bp.setInventory( inv );
				else
					this.logger.info( String
							.format( "%s Nullish inventory for %s", BackPackPrefix, uuid ) );

				if ( this.isTest )
					this.logger.info( String.format( "Read backpack %s from DB!", uuid ) );

				return bp;
			}

			return null;
		} catch ( SQLException | ParseException e ) {
			this.logger.info( "Error while fetching Backpack data!" );
			e.printStackTrace();
		} finally {
			try {
				st.close();
			} catch ( Exception e ) {
			}
		}

		return null;
	}

	public String createBackpack(
			@NotNull String name
	) {
		PreparedStatement st = null;

		try {
			st = this.connection.prepareStatement( SqlQueries.CREATE_BACKPACK
					.toString(), Statement.KEEP_CURRENT_RESULT );
			st.setString( 1, name );

			st.executeUpdate();

			String uuid = getBackpackUUIDByName( name );

			if ( this.isTest )
				this.logger.info( String
						.format( "%s Retrieved inserted backpack by name: %s, uuid: %s", BackPackPrefix, name, uuid ) );

			return uuid;
		} catch ( SQLException e ) {
			this.logger
					.info( String.format( "%s Error przy zapisywaniu plecaka!", BackPackPrefix ) );
			e.printStackTrace();
		} finally {
			try {
				st.close();
			} catch ( Exception e ) {
			}
		}

		return null;
	}

	public String getBackpackUUIDByName(
			@NotNull final String name
	) {
		PreparedStatement st = null;

		try {
			st = this.connection
					.prepareStatement( SqlQueries.SELECT_BACKPACK_BYNAME_LAST.toString() );
			st.setString( 1, name );

			ResultSet rs = st.executeQuery();

			while ( rs.next() ) {
				String uuid = rs.getString( "uuid" );
				String inventoryName = rs.getString( "inventoryName" );
				String itemData = rs.getString( "itemData" );

				if ( uuid == null || uuid.length() == 0 ) {
					this.logger.info( String
							.format( "%s UUID: %s, invName: %s, itemData: %n NO UUID!!!!", BackPackPrefix, uuid, inventoryName, itemData
									.length() ) );
				} else {
					return uuid;
				}
			}

		} catch ( SQLException e ) {
			this.logger.info( String
					.format( "%s Error przy wczytywaniu uuid plecaka!", BackPackPrefix ) );
			e.printStackTrace();
		} finally {
			try {
				st.close();
			} catch ( Exception e ) {
			}
		}

		return null;
	}

	public void saveBackpack(
			@NotNull final Backpack bp
	) {
		PreparedStatement st = null;
		try {
			st = this.connection.prepareStatement( SqlQueries.UPDATE_BACKPACK_ITEMS.toString() );
			st.setString( 1, InventorySerializer.InventoryToString( bp.getInventory() ) );
			st.setString( 2, bp.getBagID() );

			int rs = st.executeUpdate();

			if ( rs == 0 )
				this.logger.info( String
						.format( "%s Error przy zapisywaniu plecaka! (%s)", DbUtils.BackPackPrefix, String
								.valueOf( rs ) ) );

			if ( this.isTest )
				this.logger.info( String
						.format( "%s Zapisano plecak %s", DbUtils.BackPackPrefix, bp.getBagID() ) );

		} catch ( SQLException e ) {
			this.logger
					.info( String.format( "%s Error przy zapisywaniu plecaka!", BackPackPrefix ) );
			e.printStackTrace();
		} finally {
			try {
				st.close();
			} catch ( Exception e ) {
			}
		}
	}

	public void backupBackPack(
			@NotNull final Backpack bp
	) {
		return;
	}
}
