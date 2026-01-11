package net.woolf.bella.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import Types.SqlQueries;
import classes.Backpack;
import net.woolf.bella.models.BackpackModel;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.InventorySerializer;

public class BackpackRepository {

  private final DbUtils dbUtils;

  public BackpackRepository(
      DbUtils dbUtils
  ) {
    this.dbUtils = dbUtils;
  }

  @Nullable
  public BackpackModel getBackpackModel(
      @Nonnull String baguuid
  ) {
    if ( baguuid.length() == 0 ) {
      return null;
    }

    try ( var st = dbUtils.connection.prepareStatement( SqlQueries.GET_BACKPACK.toString() ) ) {
      st.setString( 1, baguuid );

      ResultSet rs = st.executeQuery();

      if ( rs.next() ) {
        String uuid = rs.getString( "uuid" );
        String inventoryName = rs.getString( "inventoryName" );
        String itemData = rs.getString( "itemData" );

        return new BackpackModel( uuid, inventoryName, itemData );
      }

      return null;
    } catch ( SQLException e ) {
      dbUtils.logger.info( "Error while fetching Backpack data!" );
      e.printStackTrace();
      return null;
    }
  }

  @Nullable
  public Backpack getBackpack(
      @Nonnull String baguuid
  ) {
    BackpackModel model = getBackpackModel( baguuid );
    if ( model == null )
      return null;

    Inventory inv = null;

    if ( model.getItemData() != null && model.getItemData().length() > 0 ) {
      try {
        inv = InventorySerializer.StringToInventory( model.getItemData() );
      } catch ( ParseException e ) {
        dbUtils.logger.warning( "Error parsing inventory data for backpack: " + baguuid );
        e.printStackTrace();
      }
    }

    Backpack bp = inv == null ? new Backpack()
        : new Backpack( inv.getSize(), model.getUuid(), model.getInventoryName() );

    bp.setBagID( model.getUuid() );
    bp.setTitle( model.getInventoryName() );

    if ( inv != null )
      bp.setInventory( inv );
    else
      dbUtils.logger
          .info( String.format( "[BackPack] Nullish inventory for %s", model.getUuid() ) );

    return bp;
  }

  @Nullable
  public String createBackpack(
      @NotNull String name
  ) {
    try (
        var st = dbUtils.connection.prepareStatement( SqlQueries.CREATE_BACKPACK
            .toString(), Statement.RETURN_GENERATED_KEYS )
    ) {
      st.setString( 1, name );

      int affectedRows = st.executeUpdate();

      if ( affectedRows == 0 ) {
        dbUtils.logger.info( "[BackPack] Error: No rows affected when creating backpack!" );
        return null;
      }

      try ( ResultSet generatedKeys = st.getGeneratedKeys() ) {
        if ( generatedKeys.next() ) {
          String uuid = generatedKeys.getString( 1 );
          return uuid;
        } else {
          return getBackpackUUIDByName( name );
        }
      }
    } catch ( SQLException e ) {
      dbUtils.logger.info( "[BackPack] Error przy zapisywaniu plecaka!" );
      e.printStackTrace();
      return null;
    }
  }

  @Nullable
  public String getBackpackUUIDByName(
      @NotNull final String name
  ) {
    try (
        var st = dbUtils.connection
            .prepareStatement( SqlQueries.SELECT_BACKPACK_BYNAME_LAST.toString() )
    ) {
      st.setString( 1, name );

      ResultSet rs = st.executeQuery();

      if ( rs.next() ) {
        String uuid = rs.getString( "uuid" );
        if ( uuid == null || uuid.length() == 0 ) {
          dbUtils.logger
              .info( String.format( "[BackPack] UUID: %s, invName: %s NO UUID!!!!", uuid, name ) );
        } else {
          return uuid;
        }
      }

      return null;
    } catch ( SQLException e ) {
      dbUtils.logger.info( "[BackPack] Error przy wczytywaniu uuid plecaka!" );
      e.printStackTrace();
      return null;
    }
  }

  public void saveBackpack(
      @NotNull final Backpack bp
  ) {
    try (
        var st = dbUtils.connection.prepareStatement( SqlQueries.UPDATE_BACKPACK_ITEMS.toString() )
    ) {
      st.setString( 1, InventorySerializer.InventoryToString( bp.getInventory() ) );
      st.setString( 2, bp.getBagID() );

      int rs = st.executeUpdate();

      if ( rs == 0 )
        dbUtils.logger
            .info( String.format( "[BackPack] Error przy zapisywaniu plecaka! (%s)", rs ) );
    } catch ( SQLException e ) {
      dbUtils.logger.info( "[BackPack] Error przy zapisywaniu plecaka!" );
      e.printStackTrace();
    }
  }
}
