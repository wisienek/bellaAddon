package net.woolf.bella.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import Types.BotChannels;
import net.woolf.bella.commands.ItemEnchanter;
import net.woolf.bella.commands.WymienCommand;
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

import Types.BackpackNBTKeys;
import classes.Backpack;
import de.tr7zw.nbtapi.NBTItem;
import net.woolf.bella.Main;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.utils.PlayerUtils;
import net.woolf.bella.utils.StringUtils;

public class BellaEvents implements Listener {

  private final Main plugin;

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
    String actionsFormatted = ChatUtils.formatChatMessage( msg );

    event.setMessage( actionsFormatted );

    String userPrefix = PlayerUtils.getPlayerPrefix( event.getPlayer() )
        .replaceAll( "(§.)|(&.)", "" );
    String playerName = StringUtils.escapeDiscordMarkdown( event.getPlayer().getName() );
    String messageContent = StringUtils
        .escapeDiscordMarkdown( actionsFormatted.replaceAll( "§.", "" ) );

    ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.LocalPrefix + " "
        + userPrefix + " " + playerName + ": `" + messageContent + "`" );
  }

  @EventHandler
  public void onPlayerJoin(
      PlayerJoinEvent event
  ) {
    Collection<? extends Player> onlineCollection = plugin.server.getOnlinePlayers();
    List<Player> online = new ArrayList<>( onlineCollection );
    plugin.bot.updatePresence( "Graczy online: " + ( online.size() + 1 ) );
  }

  @EventHandler
  public void onPlayerQuit(
      PlayerQuitEvent event
  ) {
    Collection<? extends Player> onlineCollection = plugin.server.getOnlinePlayers();
    List<Player> online = new ArrayList<>( onlineCollection );
    int size = online.size() - 1;

    plugin.bot.updatePresence( size > 0 ? "Graczy online: " + size : "Czekam na graczy..." );
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerInteract(
      PlayerInteractEvent event
  ) {
    Player player = event.getPlayer();

    if ( player.isSneaking() ) {
      List<Entity> passengers = player.getPassengers();

      if ( passengers.size() > 0 )
        for ( Entity Passenger : passengers )
          player.removePassenger( Passenger );
    }

    ItemStack item = player.getInventory().getItemInMainHand();
    if ( item != null && item.getType() != Material.AIR ) {
      NBTItem nbti = new NBTItem( item, true );

      if ( nbti.hasKey( "teleportEnchantment" ) ) {
        event.setCancelled( true );
        ItemEnchanter.teleportPlayerWithItem( player, nbti );
      } else if ( nbti.hasKey( BackpackNBTKeys.ISBACKPACK.toString() ) ) {
        Backpack.OpenBackpackEvent( player, nbti, item );
      } else if ( nbti.hasKey( WymienCommand.MoneyNbtTag ) ) {
        event.setCancelled( true );

        WymienCommand.moveMoneyToPurse( player, nbti, item, player.isSneaking() );
      }
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(
      PlayerInteractEntityEvent event
  ) {
    if ( !event.getHand().equals( EquipmentSlot.HAND ) )
      return;

    Entity clicked = event.getRightClicked();
    Player player = event.getPlayer();

    if ( clicked instanceof Player ) {
      Player target = (Player) clicked;

      boolean canBeRidden = plugin.configManager.playerConfig
          .getBoolean( target.getUniqueId().toString() + ".canBeRidden" );

      if ( canBeRidden ) {
        List<Entity> passangers = target.getPassengers();

        if ( passangers.size() == 0 )
          target.addPassenger( player );
      }
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

    List<String> args = new LinkedList<>();
    Collections.addAll( args, event.getMessage().split( " " ) );
    String cmd = args.get( 0 ).replace( "/", "" );
    args.remove( 0 );

    String userPrefix = PlayerUtils.getPlayerPrefix( event.getPlayer() );

    String playerName = StringUtils.escapeDiscordMarkdown( player.getName() );
    String cleanPrefix = userPrefix.replaceAll( "(§.)|(&.)", "" );

    switch ( cmd ) {
      case "ooc":
      case "o": {
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.OOCPrefix + " "
            + playerName + ": `(" + content + ")`" );
        break;
      }

      case "me": {
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.LocalPrefix
            + " " + cleanPrefix + " " + playerName + ": `*" + content + "*`" );
        break;
      }

      case "k": {
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.ShoutPrefix
            + " " + cleanPrefix + " " + playerName + ": `" + content + "`" );
        break;
      }

      case "do": {
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.LocalPrefix
            + " " + cleanPrefix + " " + playerName + ": `**" + content + "**`" );
        break;
      }

      case "s": {
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.WhisperPrefix
            + " " + cleanPrefix + " " + playerName + ": `" + content + "`" );
        break;
      }

      case "msg":
      case "tell":
      case "whisper":
      case "w":
      case "m": {
        if ( args.isEmpty() )
          break;
        String target = StringUtils.escapeDiscordMarkdown( args.get( 0 ) );
        args.remove( 0 );
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils
            .cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.PrivateMessagePrefix
                + " " + playerName + " -> " + target + ": `" + content + "`" );
        break;
      }

      case "r":
      case "reply": {
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils
            .cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.PrivateMessagePrefix
                + " " + playerName + " -> [reply]: `" + content + "`" );
        break;
      }

      case "dice": {
        if ( args.isEmpty() )
          break;
        try {
          int sides = Integer.parseInt( args.get( 0 ) );
          if ( sides == 2 ) {
            int result = (int) ( Math.random() * 2 );
            String outcome = result == 0 ? "orzeł" : "reszka";
            ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.DicePrefix
                + " " + cleanPrefix + " " + playerName + ": `Rzut monetą - " + outcome + "`" );
          } else if ( sides >= 4 && sides <= 100 ) {
            int result = (int) ( Math.random() * sides ) + 1;
            ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.DicePrefix
                + " " + cleanPrefix + " " + playerName + ": `Rzut k" + sides + " - wypadło "
                + result + "`" );
          }
        } catch ( NumberFormatException ignored ) {
        }
        break;
      }

      case "globalnar": {
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.GlobalPrefix
            + " " + playerName + ": `" + content + "`" );
        break;
      }

      case "midnar":
      case "localnar": {
        Location loc = player.getLocation();
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), ChatUtils.LocalPrefix
            + " {" + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "} "
            + playerName + ": `" + content + "`" );
        break;
      }

      case "privnar": {
        if ( args.isEmpty() )
          break;
        String narrated = StringUtils.escapeDiscordMarkdown( args.get( 0 ) );
        args.remove( 0 );
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), "**[PRIVNAR]** "
            + playerName + " -> " + narrated + ": `" + content + "`" );
        break;
      }

      case "helpop": {
        String hourFormat = StringUtils.getHourMinutes();
        String content = StringUtils.escapeDiscordMarkdown( String.join( " ", args ) );
        this.plugin.bot.sendLog( String
            .format( "%s `%s`: `%s`", hourFormat, playerName, content ), BotChannels.HelpopLogId
                .toString() );
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
    if ( player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR ) {
      return;
    }

    if ( !PlayerUtils.playerArmourHasEffect( player, "doublejump" ) )
      return;

    Location playerLocation = player.getLocation();

    if ( player.getWorld().getBlockAt( playerLocation ).getType() == Material.WATER )
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
      Material blockMaterial = player.getLocation().subtract( 0.0, 1.0, 0.0 ).getBlock().getType();

      if (
        player.getGameMode() != GameMode.CREATIVE && blockMaterial != Material.AIR
            && blockMaterial != Material.WATER && !player.isFlying()
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
}
