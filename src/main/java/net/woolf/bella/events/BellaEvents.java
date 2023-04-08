package net.woolf.bella.events;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import Types.BotChannels;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.model.user.User;
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

  public BellaEvents (
      Main main
  ) {
    this.plugin = main;
  }

  @EventHandler( priority = EventPriority.HIGH )
  public void onPlayerChat (
      AsyncPlayerChatEvent event
  ) {
    String msg = event.getMessage();
    String actionsFormatted = ChatUtils.formatChatMessage(msg);

    event.setMessage(actionsFormatted);

    String userPrefix = "[-]";

    User user = this.plugin.lpApi.getUserManager().getUser(event.getPlayer().getUniqueId());
    if ( user != null ) {
      CachedDataManager cachedData = user.getCachedData();
      String lpPrefix = cachedData.getMetaData().getPrefix();

      if ( lpPrefix != null ) {
        userPrefix = lpPrefix;
      }
    }

    ChatUtils.cacheMessageForBotLog(BotChannels.ChatLogId.toString(),
                                    ChatUtils.LocalPrefix + " " + userPrefix.replaceAll("(§.)|(&.)|(`)",
                                                                                        "") + " " + event.getPlayer()
                                                                                                         .getName() + ": " + "`" + actionsFormatted.replaceAll(
                                        "(§.)|(`)", "") + "`");
  }

  @EventHandler
  public void onPlayerJoin (
      PlayerJoinEvent event
  ) {
    List<Player> online = plugin.utils.getPlayers();
    plugin.bot.updatePresence("Graczy online: " + ( online.size() + 1 ));
  }

  @EventHandler
  public void onPlayerQuit (
      PlayerQuitEvent event
  ) {
    List<Player> online = plugin.utils.getPlayers();
    int size = online.size() - 1;

    plugin.bot.updatePresence(size > 0 ? "Graczy online: " + size : "Czekam na graczy...");
  }

  @EventHandler( priority = EventPriority.HIGH )
  public void onPlayerInteract (
      PlayerInteractEvent event
  ) {
    Player player = event.getPlayer();

    if ( player.isSneaking() ) {
      List<Entity> passengers = player.getPassengers();

      if ( passengers.size() > 0 ) for ( Entity Passenger : passengers )
        player.removePassenger(Passenger);
    }

    ItemStack item = player.getInventory().getItemInMainHand();
    if ( item != null && item.getType() != Material.AIR ) {
      NBTItem nbti = new NBTItem(item, true);

      if ( nbti.hasKey("teleportEnchantment") ) {
        event.setCancelled(true);
        ItemEnchanter.teleportPlayerWithItem(player, nbti);
      }
      else if ( nbti.hasKey(BackpackNBTKeys.ISBACKPACK.toString()) ) {
        Backpack.OpenBackpackEvent(player, nbti, item);
      }
      else if ( nbti.hasKey(WymienCommand.MoneyNbtTag) ) {
        event.setCancelled(true);

        WymienCommand.moveMoneyToPurse(player, nbti, item, player.isSneaking());
      }
    }
  }

  @EventHandler
  public void onPlayerInteractEntity (
      PlayerInteractEntityEvent event
  ) {
    if ( !event.getHand().equals(EquipmentSlot.HAND) ) return;

    Entity clicked = event.getRightClicked();
    Player player = event.getPlayer();

    if ( clicked instanceof Player ) {
      Player target = (Player) clicked;

      boolean canBeRidden = plugin.playerConfig.getBoolean(target.getUniqueId().toString() + ".canBeRidden");

      if ( canBeRidden ) {
        List<Entity> passangers = target.getPassengers();

        if ( passangers.size() == 0 ) target.addPassenger(player);
      }
    }
  }

  @EventHandler
  public void onPlayerItemDamageEvent (
      PlayerItemDamageEvent event
  ) {
    event.setCancelled(true);
  }

  @EventHandler
  public boolean onPlayerCommandPreprocessEvent (
      PlayerCommandPreprocessEvent event
  ) {
    Player player = event.getPlayer();

    List<String> args = new LinkedList<>();
    Collections.addAll(args, event.getMessage().split(" "));

    String cmd = args.get(0).replace("/", "");

    args.remove(0);

    switch ( cmd ) {
      case "ooc": {
        ChatUtils.cacheMessageForBotLog(BotChannels.ChatLogId.toString(),
                                        ChatUtils.OOCPrefix + " " + player.getName() + ": `(" + String.join(" ", args)
                                                                                                      .replaceAll("`",
                                                                                                                  "") + ")`");
        break;
      }

      case "me":
      case "k": {
        ChatUtils.cacheMessageForBotLog(BotChannels.ChatLogId.toString(),
                                        ChatUtils.LocalPrefix + " [" + player.getDisplayName() + "] " + player.getName() + ": `*" + String.join(
                                            " ", args).replaceAll("`", "") + "*`");
        break;
      }

      case "do": {
        ChatUtils.cacheMessageForBotLog(BotChannels.ChatLogId.toString(),
                                        ChatUtils.LocalPrefix + " [" + player.getDisplayName() + "] " + player.getName() + ": `**" + String.join(
                                            " ", args).replaceAll("`", "") + "**`");
        break;
      }

      case "s": {
        ChatUtils.cacheMessageForBotLog(BotChannels.ChatLogId.toString(),
                                        ChatUtils.WhisperPrefix + " [" + player.getDisplayName() + "] " + player.getName() + ": `" + String.join(
                                            " ", args).replaceAll("`", "") + "`");
        break;
      }

      case "globalnar": {
        ChatUtils.cacheMessageForBotLog(BotChannels.ChatLogId.toString(),
                                        ChatUtils.GlobalPrefix + " [" + player.getName() + "] `" + String.join(" ",
                                                                                                               args)
                                                                                                         .replaceAll(
                                                                                                             "`",
                                                                                                             "") + "`");
        break;
      }

      case "midnar":
      case "localnar": {
        Location loc = player.getLocation();
        ChatUtils.cacheMessageForBotLog(BotChannels.ChatLogId.toString(),
                                        ChatUtils.LocalPrefix + " {" + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "} " + " [" + player.getName() + "] `" + String.join(
                                            " ", args).replaceAll("`", "") + "`");
        break;
      }

      case "privnar": {
        String narrated = args.get(0);
        args.remove(0);

        ChatUtils.cacheMessageForBotLog(BotChannels.ChatLogId.toString(),
                                        "**[PRIVNAR]** " + "[" + player.getName() + " -> " + narrated + "] `" + String.join(
                                            " ", args).replaceAll("`", "") + "`");
        break;
      }

      case "helpop": {
        String hourFormat = StringUtils.getHourMinutes();

        this.plugin.bot.sendLog(String.format("%s `%s`: `%s`", hourFormat, player.getName(),
                                              StringUtils.synthesizeForDc(String.join(" ", args))),
                                BotChannels.HelpopLogId.toString());

        break;
      }
    }

    return true;
  }

  @EventHandler
  public void onPlayerToggleFlight (
      final PlayerToggleFlightEvent event
  ) {
    final Player player = event.getPlayer();
    if ( player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR ) {
      return;
    }

    if ( !PlayerUtils.playerArmourHasEffect(player, "doublejump") ) return;

    Location ploc = player.getLocation();

    if ( player.getWorld().getBlockAt(ploc).getType() == Material.WATER ) return;

    this.onPlayerDoubleJump(player);
    event.setCancelled(true);
    player.setAllowFlight(false);
    player.setFlying(false);
    player.setVelocity(player.getLocation().getDirection().multiply(1.35).setY(1));
  }

  @EventHandler
  public void onPlayerMove (
      final PlayerMoveEvent event
  ) {
    final Player player = event.getPlayer();

    if ( PlayerUtils.playerArmourHasEffect(player, "doublejump") ) {
      Material blockMaterial = player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType();

      if ( player.getGameMode() != GameMode.CREATIVE && blockMaterial != Material.AIR && blockMaterial != Material.WATER && blockMaterial != Material.STATIONARY_WATER && !player.isFlying() ) {
        player.setAllowFlight(true);
      }
    }
  }

  public void onPlayerDoubleJump (
      final Player p
  ) {
    p.playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, null);
    p.playSound(p.getLocation(), Sound.ENTITY_RABBIT_JUMP, 1.3f, 1.0f);
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
