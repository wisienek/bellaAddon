package classes;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import Types.BackpackNBTKeys;
import Types.ShrinkApproach;
import de.tr7zw.nbtapi.NBTEntity;
import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;
import net.woolf.bella.utils.CacheUtils;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.InventoryCompressor;
import net.woolf.bella.utils.InventoryUtils;
import net.woolf.bella.utils.StringUtils;

public class Backpack implements IBackpack {

  private final Boolean isTest = Main.getInstance().isTest();

  private final Logger logger = Main.getInstance().logger;
  private static final ShrinkApproach Approach = ShrinkApproach.Compress;

  private final String titleFormat = "%s (%s)";
  private String title;
  private final Map<Player, Boolean> opened = new ConcurrentHashMap<>();
  private Inventory bp;
  private ItemStack bagItem;
  private int size;
  private boolean isDefaultSize = false;
  private String backpackID;
  private boolean hasChanged;

  public boolean savedToCache = false;
  public boolean savedToDB = false;

  public void setTitle(
      final @NotNull String title
  ) {
    this.title = StringUtils.limitLength( String.format( this.titleFormat, title, "" ), 32 );
  }

  public Backpack() {
    this( 54 );
    this.isDefaultSize = true;
  }

  public Backpack(
      int size
  ) {
    this( size, "", "Plecak" );
  }

  public Backpack(
      int size,
      String ID,
      String _title
  ) {
    title = StringUtils.limitLength( String.format( this.titleFormat, _title, ID ), 32 );

    bp = Bukkit.createInventory( this, size, ChatColor.AQUA + title );
    this.size = size;
    backpackID = ID;
  }

  public boolean isDefaultSize() {
    return this.isDefaultSize;
  }

  public void setInventory(
      @NotNull Inventory inv
  ) {
    // this.bp.setContents( inv.getContents() );
    this.bp.setStorageContents( inv.getStorageContents() );
  }

  public String getBagID() {
    return backpackID;
  }

  public void setBagID(
      String id
  ) {
    backpackID = id;
  }

  @Override
  public void open(
      final @NotNull Player player,
      final boolean editable
  ) {
    open( player, editable, null );
  }

  @Override
  public void open(
      final @NotNull Player player,
      final boolean editable,
      final @Nullable String title
  ) {
    boolean isAdmin = player.hasPermission( "beloris.admin" );

    NBTEntity nbti = new NBTEntity( player );
    nbti.setString( BackpackNBTKeys.OPENED.toString(), this.backpackID );

    opened.put( player, isAdmin || editable );
    player.openInventory( bp );
  }

  public void close(
      Player p
  ) {
    opened.remove( p );

    NBTEntity nbti = new NBTEntity( p );
    nbti.removeKey( BackpackNBTKeys.OPENED.toString() );

    if ( !this.isOpen() )
      save();
  }

  public void closeAll() {
    opened.forEach( (key, value) -> {
      key.closeInventory();

      NBTEntity nbti = new NBTEntity( key );
      nbti.removeKey( BackpackNBTKeys.OPENED.toString() );
    } );

    opened.clear();
    save();
  }

  @Override
  public boolean isOpen() {
    return !opened.isEmpty();
  }

  @Override
  public int getSize() {
    return size;
  }

  public @NotNull List<ItemStack> setSize(
      int newSize,
      boolean saveBackPack
  ) {
    // close all opened views of the inventory
    opened.forEach( (key, value) -> key.closeInventory() );

    List<ItemStack> removedItems;
    ItemStack[] itemStackArray;
    if ( bp.getSize() > newSize ) {
      InventoryCompressor compressor = new InventoryCompressor( bp.getContents(), newSize );
      switch ( Backpack.Approach ) {
        case Fast:
          compressor.fast();
          break;

        case Compress:
          compressor.compress();
          break;

        case Sort:
          compressor.sort();
          break;
      }

      itemStackArray = compressor.getTargetStacks();
      removedItems = compressor.getToMuch();
    } else {
      itemStackArray = bp.getContents();
      removedItems = new ArrayList<ItemStack>();
    }

    if ( this.isTest ) {
      String collectedRemoved = getItemDataTOString( removedItems );

      String collectedItems = this.getItemDataTOString( Arrays.asList( itemStackArray ) );

      this.logger.info( String
          .format( "Current size %d, new: %d, TooMuch: %d, RemovedItems: %s, items to put: %s", bp
              .getSize(), newSize, removedItems.size(), collectedRemoved, collectedItems ) );
    }

    bp = Bukkit.createInventory( bp.getHolder(), newSize, title );

    for ( int i = 0; i < itemStackArray.length; i++ ) {
      ItemStack cu = itemStackArray[i];

      if ( cu != null && cu.getType() != Material.AIR ) {
        if ( this.isTest )
          this.logger.info( String.format( "Setting item %s to slot %d", itemStackArray[i].getData()
              .getItemType(), i ) );

        bp.setItem( i, itemStackArray[i] );
      }

    }

    if ( saveBackPack ) {
      setChanged();
      save();
    }

    size = newSize;
    opened.forEach( (key, value) -> key.openInventory( bp ) );

    return removedItems;
  }

  private String getItemDataTOString(
      List<ItemStack> list
  ) {
    StringBuilder cumulated = new StringBuilder();

    for ( ItemStack item : list )
      if ( item != null && item.getType() != Material.AIR )
        cumulated.append( String.format( "- %d -", item.getAmount() ) );

    return cumulated.toString();
  }

  @Override
  public ItemStack getBagItem() {
    return this.bagItem;
  }

  @Override
  public void setBagItem(
      @NotNull final ItemStack item
  ) {
    this.bagItem = item;
  }

  @Override
  public @NotNull Inventory getInventory() {
    return bp;
  }

  @Override
  public boolean hasChanged() {
    return hasChanged;
  }

  @Override
  public void setChanged() {
    hasChanged = true;

    savedToCache = false;
    savedToDB = false;

    if ( this.isTest )
      this.logger.info( "Setting changed variables!" );
  }

  @Override
  public void save() {
    saveToCache( this );

    if ( this.hasChanged() ) {
      try {
        DbUtils.getInstance().saveBackpack( this );

        hasChanged = false;
        savedToDB = true;
        savedToCache = false;

      } catch ( SQLException | IOException e ) {
        this.logger.info( String.format( "Error while Saving backpack %s", this.backpackID ) );
        e.printStackTrace();
      }
    }

    if ( !this.isOpen() )
      CacheUtils.removeFromCache( backpackID );
  }

  public void backup() {
    // saveToCache( this );

    try {
      DbUtils.getInstance().backupBackPack( this );
    } catch ( SQLException | IOException e ) {
      this.logger.info( String.format( "Error while backing-up backpack %s", this.backpackID ) );
      e.printStackTrace();
    }
  }

  @Override
  public void clear() {
    bp.clear();
    setChanged();
    save();
  }

  @Override
  public void drop(
      final @NotNull Location location
  ) {
    InventoryUtils.dropInventory( bp, location );
    setChanged();
    save();
  }

  public static Backpack getCachedInfo(
      @Nonnull String baguuid
  ) {
    if ( !CacheUtils.hasKey( baguuid ) ) {
      Main.getInstance().logger
          .info( String.format( "Tried to access bag but was not in cache %s", baguuid ) );
      return null;
    }

    return (Backpack) CacheUtils.getObject( baguuid );
  }

  public boolean readFromCache() {
    if ( !CacheUtils.hasKey( backpackID ) )
      return false;

    Backpack fromCache = (Backpack) CacheUtils.getObject( backpackID );
    if ( fromCache == null )
      return false;

    this.size = fromCache.size;
    this.bp = fromCache.bp;
    this.title = fromCache.title;

    this.hasChanged = false;
    this.savedToCache = true;
    this.savedToDB = false;

    this.opened.clear();

    if ( this.isTest )
      this.logger.info( String.format( "Read backpack %s from cache", this.backpackID ) );

    return true;
  }

  public static void saveToCache(
      @Nonnull Backpack bp
  ) {
    if ( bp.savedToCache )
      return;

    Main plugin = Main.getInstance();
    String id = bp.getBagID();
    CacheUtils.addToCache( id, bp );

    if ( plugin.isTest() )
      plugin.logger.info( String.format( "Saved bp to cache: %s", id ) );

    bp.savedToCache = true;
  }

  public static void OpenBackpackEvent(
      Player player,
      NBTItem nbti,
      ItemStack item
  ) {
    String bagUUID = nbti.getString( BackpackNBTKeys.UUID.toString() );
    Boolean allowsMultiple = nbti.getBoolean( BackpackNBTKeys.ALLOW_MULTIPLE_VIEWERS.toString() );

    if ( CacheUtils.hasKey( bagUUID ) && !allowsMultiple ) {
      player.sendMessage( Main.prefixError
          + "Plecak jest już przez kogoś otwarty i nie pozwala na kilku widzów, albo cache "
          + "wariuje ;? " + "0" );
      return;
    }

    try {
      Backpack bag = new Backpack();
      bag.setBagID( bagUUID );
      bag = DbUtils.getInstance().getBackpackInfo( bagUUID );

      if ( bag == null ) {
        player.sendMessage( Main.prefixError + "Nie można było odczytać plecaka!" );
        return;
      }

      bag.setBagItem( item );
      if ( bag.isDefaultSize() )
        bag.setSize( nbti.getInteger( BackpackNBTKeys.ROWS.toString() ) * 9, false );

      bag.open( player, true );

    } catch ( SQLException | IOException e ) {
      player.sendMessage( Main.prefixError
          + "Pojawił się błąd przy pobieraniu informacji o plecaku!" );
      e.printStackTrace();
    }
  }
}
