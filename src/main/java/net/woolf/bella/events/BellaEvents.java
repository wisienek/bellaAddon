package net.woolf.bella.events;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/* import com.codingforcookies.armorequip.ArmorEquipEvent; */

import Types.BackpackNBTKeys;
import classes.Backpack;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.utils.CacheUtils;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.PlayerUtils;
import net.woolf.bella.utils.StringUtils;

public class BellaEvents implements Listener {

	private Main plugin;

	public BellaEvents(
			Main main
	) {
		this.plugin = main;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat(
			AsyncPlayerChatEvent event
	) {
		String msg = event.getMessage();

		// String newMsg = ChatUtils.formatDOaction(msg);
		// newMsg = ChatUtils.formatMEaction(newMsg);
		// String newMsg = ChatUtils.formatOOC(msg);
		String newMsg = ChatUtils.formatEmojis( msg );

		// Send chat
		event.setMessage( newMsg );

		ChatUtils.cacheMessageForChatLog( ChatUtils.LocalPrefix + " ["
				+ event.getPlayer().getDisplayName() + "] " + event.getPlayer().getName() + ": `"
				+ newMsg.replaceAll( "(§.)|(`)", "" ) + "`" );
	}

	@EventHandler
	public void onPlayerJoin(
			PlayerJoinEvent event
	) {
		List<Player> online = plugin.utils.getPlayers();
		plugin.bot.updatePresence( "Graczy online: " + ( online.size() + 1 ) );
	}

	@EventHandler
	public void onPlayerQuit(
			PlayerQuitEvent event
	) {
		List<Player> online = plugin.utils.getPlayers();
		plugin.bot.updatePresence( "Graczy online: " + ( online.size() - 1 ) );
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(
			PlayerInteractEvent event
	) {
		Player player = event.getPlayer();

		if ( player.isSneaking() ) {
			List<Entity> passangers = player.getPassengers();

			if ( passangers.size() > 0 )
				for ( Entity Passanger : passangers )
					player.removePassenger( Passanger );
		}

		ItemStack item = player.getInventory().getItemInMainHand();
		if ( item != null && item.getType() != Material.AIR ) {
			NBTItem nbti = new NBTItem( item, true );

			if ( nbti.hasKey( "teleportEnchantment" ) ) {
				event.setCancelled( true );
				Location playerLoc = player.getLocation();

				NBTCompound comp = nbti.getCompound( "teleportEnchantment" );
				plugin.utils.itemTP( player, comp );

				double x = comp.getDouble( "x" );
				double y = comp.getDouble( "y" );
				double z = comp.getDouble( "z" );

				this.plugin.bot.sendLog( String
						.format( "[%s] teleportował {%d %d %d} -> {%d %d %d} (item)", player
								.getName(), playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc
										.getBlockZ(), x, y, z ), Bot.VariousLogId );

			} else if ( nbti.hasKey( BackpackNBTKeys.ISBACKPACK.toString() ) ) {
				String bagUUID = nbti.getString( BackpackNBTKeys.UUID.toString() );
				Boolean allowsMultiple = nbti
						.getBoolean( BackpackNBTKeys.ALLOW_MULTIPLE_VIEWERS.toString() );

				if ( CacheUtils.hasKey( bagUUID ) && !allowsMultiple ) {
					player.sendMessage( Main.prefixError
							+ "Plecak jest już przez kogoś otwarty i nie pozwala na kilku widzów, albo cache wariuje ;? "
							+ ( allowsMultiple ? "1" : "0" ) );
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
						bag.setSize( nbti.getInteger( BackpackNBTKeys.ROWS.toString() )
								* 9, false );

					bag.open( player, true );

				} catch ( SQLException | IOException e ) {
					player.sendMessage( Main.prefixError
							+ "Pojawił się błąd przy pobieraniu informacji o plecaku!" );
					e.printStackTrace();
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(
			PlayerInteractEntityEvent event
	) {
		if ( event.getHand().equals( EquipmentSlot.HAND ) == false )
			return;

		Entity clicked = event.getRightClicked();
		Player player = event.getPlayer();

		if ( clicked instanceof Player ) {
			Player target = (Player) clicked;

			Boolean canBeRidden = plugin.playerConfig
					.getBoolean( target.getUniqueId().toString() + ".canBeRidden" );

			if ( canBeRidden == true ) {
				List<Entity> passangers = target.getPassengers();

				if ( passangers.size() == 0 )
					target.addPassenger( player );
			}

			// if ( player.isSneaking() ) {
			// boolean isAdmin = player.hasPermission( Permissions.ADMIN.toString() );
			//
			//
			// // check player stats
			// }
		}
	}

	@EventHandler
	public void onPlayerItemDamageEvent(
			PlayerItemDamageEvent event
	) {
		event.setCancelled( true );
	}

	@EventHandler
	public boolean onPlayerCommandPreprocessEvent(
			PlayerCommandPreprocessEvent event
	) {
		Player player = event.getPlayer();

		List<String> args = new LinkedList<String>();
		Collections.addAll( args, event.getMessage().split( " " ) );

		String cmd = args.get( 0 ).replace( "/", "" );

		args.remove( 0 );

		switch ( cmd ) {
			case "ooc": {
				ChatUtils.cacheMessageForChatLog( ChatUtils.OOCPrefix + " " + player.getName()
						+ ": `(" + String.join( " ", args ).replaceAll( "`", "" ) + ")`" );
				break;
			}

			case "me":
			case "k": {
				ChatUtils.cacheMessageForChatLog( ChatUtils.LocalPrefix + " ["
						+ player.getDisplayName() + "] " + player.getName() + ": `*"
						+ String.join( " ", args ).replaceAll( "`", "" ) + "*`" );
				break;
			}

			case "do": {
				ChatUtils.cacheMessageForChatLog( ChatUtils.LocalPrefix + " ["
						+ player.getDisplayName() + "] " + player.getName() + ": `**"
						+ String.join( " ", args ).replaceAll( "`", "" ) + "**`" );
				break;
			}

			case "s": {
				ChatUtils.cacheMessageForChatLog( ChatUtils.WhisperPrefix + " ["
						+ player.getDisplayName() + "] " + player.getName() + ": `"
						+ String.join( " ", args ).replaceAll( "`", "" ) + "`" );
				break;
			}

			case "globalnar": {
				ChatUtils.cacheMessageForChatLog( ChatUtils.GlobalPrefix + " [" + player.getName()
						+ "] `" + String.join( " ", args ).replaceAll( "`", "" ) + "`" );
				break;
			}

			case "midnar":
			case "localnar": {
				Location loc = player.getLocation();
				ChatUtils.cacheMessageForChatLog( ChatUtils.LocalPrefix + " {" + loc.getBlockX()
						+ " " + loc.getBlockY() + " " + loc.getBlockZ() + "} " + " ["
						+ player.getName() + "] `" + String.join( " ", args ).replaceAll( "`", "" )
						+ "`" );
				break;
			}

			case "privnar": {
				String narrated = args.get( 0 );
				args.remove( 0 );

				ChatUtils.cacheMessageForChatLog( "**[PRIVNAR]** " + "[" + player.getName() + " -> "
						+ narrated + "] `" + String.join( " ", args ).replaceAll( "`", "" ) + "`" );
				break;
			}

			case "helpop": {
				String hourFormat = StringUtils.getHourMinutes();

				this.plugin.bot.sendLog( String
						.format( "%s `%s`: `%s`", hourFormat, player.getName(), StringUtils
								.synthesizeForDc( String.join( " ", args ) ) ), Bot.HelpopLogId );

				break;
			}
		}

		return true;
	}

	@EventHandler
	public void onPlayerToggleFlight(
			final PlayerToggleFlightEvent event
	) {
		final Player player = event.getPlayer();
		if (
			player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR
		) {
			return;
		}

		if ( !PlayerUtils.playerArmourHasEffect( player, "doublejump" ) )
			return;

		Location ploc = player.getLocation();

		if ( player.getWorld().getBlockAt( ploc ).getType() == Material.WATER )
			return;

		this.onPlayerDoubleJump( player );
		event.setCancelled( true );
		player.setAllowFlight( false );
		player.setFlying( false );
		player.setVelocity( player.getLocation().getDirection().multiply( 1.35 ).setY( 1 ) );
	}

	@EventHandler
	public void onPlayerMove(
			final PlayerMoveEvent event
	) {
		final Player player = event.getPlayer();

		if ( PlayerUtils.playerArmourHasEffect( player, "doublejump" ) ) {
			Material blockMaterial = player.getLocation()
					.subtract( 0.0, 1.0, 0.0 )
					.getBlock()
					.getType();

			if (
				player.getGameMode() != GameMode.CREATIVE && blockMaterial != Material.AIR
						&& blockMaterial != Material.WATER
						&& blockMaterial != Material.STATIONARY_WATER && !player.isFlying()
			) {
				player.setAllowFlight( true );
			}
		}
	}

	public void onPlayerDoubleJump(
			final Player p
	) {
		p.playEffect( p.getLocation(), Effect.MOBSPAWNER_FLAMES, null );
		p.playSound( p.getLocation(), Sound.ENTITY_RABBIT_JUMP, 1.3f, 1.0f );
	}

	// @EventHandler(priority = EventPriority.HIGHEST)
	// public void equip(
	// final ArmorEquipEvent event
	// ) {
	// System.out.println( "ArmorEquipEvent - " + event.getMethod() );
	// System.out.println( "Type: " + event.getType() );
	// System.out.println( "New: "
	// + ( event.getNewArmorPiece() != null ? event.getNewArmorPiece().getType()
	// : "null" ) );
	// System.out.println( "Old: "
	// + ( event.getOldArmorPiece() != null ? event.getOldArmorPiece().getType()
	// : "null" ) );
	//
	// }

}
