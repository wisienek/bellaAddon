package net.woolf.bella.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.PlayerUtils;
import net.woolf.bella.utils.StringUtils;

public class ListCommand implements CommandExecutor {

  private final Main plugin;

  private final HashMap<Player, Integer> cooldownTime = new HashMap<>();
  private final HashMap<Player, BukkitRunnable> cooldownTask = new HashMap<>();

  ListCommand () {
    this.plugin = Main.getInstance();

    this.plugin.getCommand("wiadomosc").setExecutor(this);
  }

  private String getUsage () {
    return "/wiadomosc <zwierze> <do_kogo> - Przygotowuje wiadomość";
  }

  @Override
  public boolean onCommand (
      CommandSender sender, Command cmd, String alias, String[] args
  ) {
    // if ( !sender.hasPermission( TestCommand.Permission ) )
    // return true;

    if ( !( sender instanceof Player ) ) return true;

    Player player = (Player) sender;

    if ( args.length < 2 ) {
      player.sendMessage(Main.prefixError + this.getUsage());
      return true;
    }
    OfflinePlayer target = PlayerUtils.getOfflinePlayer(args[1]);

    if ( target == null || target.getUniqueId().toString() == null ) {
      player.sendMessage(Main.prefixError + "Nie znaleziono gracza " + args[1]);
      return true;
    }

    String messageToSend = StringUtils.synthesizeForDc(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));

    if ( messageToSend.length() == 0 ) {
      player.sendMessage(Main.prefixError + "Brak wiadomości!");
      return true;
    }

    try {
      Map<String, String[]> playerAccounts = DbUtils.getInstance()
                                                    .getConnectedAccounts(player.getUniqueId().toString());

      if ( playerAccounts == null ) {
        player.sendMessage(Main.prefixError + "Musisz mieć podłączone konto do DC aby wysłać wiadmość!");
        return true;
      }

      Map<String, String[]> targetAccounts = DbUtils.getInstance()
                                                    .getConnectedAccounts(target.getUniqueId().toString());

      if ( targetAccounts == null ) {
        player.sendMessage(Main.prefixError + "Gracz " + args[1] + " nie ma podłączonych kont.");
        return true;
      }

      String[] targetDCID = targetAccounts.get(target.getUniqueId().toString());

      if ( targetDCID == null ) {
        player.sendMessage(Main.prefixError + args[1] + " Nie ma podłączonego discorda!");
        return true;
      }

      if ( cooldownTime.containsKey(player) ) {
        player.sendMessage(Main.prefixError + "Musisz poczekać " + ChatColor.RED + cooldownTime.get(
            player) + ChatColor.GRAY + " sekund aby ponownie wysłać wiadomość!");

        return true;
      }

      this.sendMessageDC(player.getName(), args[1], targetDCID[1], args[0], messageToSend);

      player.performCommand(String.format("do Wysłał pewny list przez <%s>", args[0]));
      this.plugin.logger.info(String.format("%s sent message to %s (%s)", player.getName(), args[1], targetDCID[1]));

      player.sendMessage(Main.prefixInfo + "Wysłano wiadomość do: " + target.getName());

      // TODO: add cooldowntime to config
      this.setLetterCoolDownTime(player, 300);

    } catch ( SQLException | IOException e ) {
      e.printStackTrace();

      player.sendMessage(Main.prefixError + "Wystąpił nieoczekiwany błąd!");
    }

    return true;

  }

  private void sendMessageDC (
      @Nonnull String fromUserName, @Nonnull String toUserName, @Nonnull String toDCID, @Nonnull String carrier,
      @Nonnull String message
  ) {
    String synthesizedMessage = StringUtils.synthesizeForDc(message);

    this.plugin.bot.sendMessageToUser(toDCID,
        String.format("%s Dostarczył Wiadomość od %s:\n```%s```", carrier, fromUserName, synthesizedMessage));

    this.plugin.bot.sendLog(
        String.format("`%s` Wysłał Wiadomość do `%s` za pomocą `%s`:\n```%s```", fromUserName, toUserName, carrier,
            synthesizedMessage), Bot.VariousLogId);
  }

  private void setLetterCoolDownTime (
      Player player, int coolDown
  ) {
    cooldownTime.put(player, coolDown);
    cooldownTask.put(player, new BukkitRunnable() {

      public void run () {
        cooldownTime.put(player, cooldownTime.get(player) - 1);
        if ( cooldownTime.get(player) == 0 ) {
          cooldownTime.remove(player);
          cooldownTask.remove(player);
          cancel();
        }
      }
    });
    cooldownTask.get(player).runTaskTimer(plugin, 20, 20);
  }

}
