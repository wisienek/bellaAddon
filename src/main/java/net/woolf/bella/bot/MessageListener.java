package net.woolf.bella.bot;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;
import net.woolf.bella.commands.LinkCommand;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.FileReader;
import net.woolf.bella.utils.MoneyUtils;
import net.woolf.bella.utils.PlayerUtils;
import org.bukkit.permissions.ServerOperator;

@SuppressWarnings( "unused" )
public class MessageListener extends ListenerAdapter {

  private static final HashSet<String> MasterGuilds = new HashSet<>(
      Arrays.asList("809181125640454194", "522449658505723905"));
  private static final HashSet<String> AllowedGuilds = new HashSet<>(Collections.singletonList("840884051174752256"));

  private final Bot bot;

  public MessageListener (
      Bot _bot
  ) {
    this.bot = _bot;
    MoneyUtils mutils = new MoneyUtils(_bot.plugin);
  }

  @Override
  public void onSlashCommand (
      SlashCommandEvent event
  ) {
    Guild guild = event.getGuild();

    if ( !this.checkGuildPerms(guild, false) ) {
      event.reply("Gildia nie jest na whiteliście!").queue();
      return;
    }

    Member gosc = event.getMember();

    assert gosc != null;
    if ( !gosc.hasPermission(Permission.USE_SLASH_COMMANDS) ) {
      event.reply("Nie masz permissi!").queue();
      return;
    }

    switch ( event.getName() ) {
      case "who": {
        if ( !this.checkGuildPerms(guild, true) ) {
          event.reply("Gildia nie jest na whiteliście!").queue();
          return;
        }

        OptionMapping ekipaOpt = event.getOption("ekipa");

        boolean ekipa = ekipaOpt != null && ekipaOpt.getAsBoolean();

        if ( !ekipa ) ShowPlayers(event);
        else ShowOPs(event);

        break;
      }

      case "portfel": {
        if ( !this.checkGuildPerms(guild, true) ) {
          event.reply("Gildia nie jest na whiteliście!").queue();
          return;
        }

        String subCommand = event.getSubcommandName();

        if ( event.getOption("user") == null && event.getOption("dcuser") == null ) {
          event.reply("Musisz podać nick lub oznaczyć gracza!").queue();
          return;
        }

        String playerName = Objects.requireNonNull(event.getOption("user")).getAsString();

        this.handleMoneyManip(event, subCommand, playerName, false);

        break;
      }

      case "bank": {
        if ( !this.checkGuildPerms(guild, true) ) {
          event.reply("Gildia nie jest na whiteliście!").queue();
          return;
        }

        String subCommand = event.getSubcommandName();

        if ( event.getOption("user") == null && event.getOption("dcuser") == null ) {
          event.reply("Musisz podać nick lub oznaczyć gracza!").queue();
          return;
        }

        String playerName = Objects.requireNonNull(event.getOption("user")).getAsString();

        this.handleMoneyManip(event, subCommand, playerName, true);

        break;
      }

      case "narracja": {
        if ( !this.checkGuildPerms(guild, true) ) {
          event.reply("Gildia nie jest na whiteliście!").queue();
          return;
        }

        if ( !MessageListener.hasRole(event.getMember(), "809423929864749086") ) {
          event.reply("Nie posiadasz roli narratora!").queue();
          return;
        }

        this.handleNarration(event);

        break;
      }

      case "pogoda": {
        if ( !this.checkGuildPerms(guild, true) ) {
          event.reply("Gildia nie jest na whiteliście!").queue();
          return;
        }

        event.reply("Pogoda wkrótce!").queue();

        break;
      }

      case "link": {
        OptionMapping codeOpt = event.getOption("code");
        String code = codeOpt != null ? codeOpt.getAsString() : null;
        if ( code == null ) {
          event.reply("Musisz podać poprawny kod!").setEphemeral(true).queue();
          return;
        }

        if ( !LinkCommand.CachedKeys.containsKey(code) || LinkCommand.CachedKeys.get(code) == null ) {
          event.reply("Wprowadzony kod jest niepoprawny: `" + code + "`").setEphemeral(true).queue();
          return;
        }

        this.linkAccount(event, LinkCommand.CachedKeys.get(code));

        LinkCommand.CachedKeys.remove(code);

        break;
      }
    }
  }

  private void linkAccount (
      @Nonnull SlashCommandEvent event, @Nonnull String uuid
  ) {
    try {
      DbUtils db = DbUtils.getInstance();

      String msg = db.connectAccount(Objects.requireNonNull(event.getMember()).getId(), uuid);

      Map<String, String[]> accounts = db.getConnectedAccounts(uuid);

      if ( accounts == null || !accounts.containsKey(uuid) ) {
        event.reply(msg).queue();

        return;
      }

      event.reply("Połączono konto " + accounts.get(uuid)[0] + "(`" + uuid + "`) z discordem!")
           .setEphemeral(true)
           .queue();
    } catch ( SQLException | IOException e ) {
      Main.getInstance().logger.info("Niepowodzenie przy linkowaniu konta! DISCORD");
      event.reply("Jakiś błąd wywaliło przy odczytywaniu danych :(").setEphemeral(true).queue();

      e.printStackTrace();
    }
  }

  private void handleNarration (
      SlashCommandEvent event
  ) {
    String subCommand = event.getSubcommandName();

    if ( subCommand == null ) return;

    String narrMessage = Objects.requireNonNull(event.getOption("text")).getAsString();

    String warp = event.getOption("warp") != null
                  ? Objects.requireNonNull(event.getOption("warp")).getAsString()
                  : null;
    String user = event.getOption("user") != null
                  ? Objects.requireNonNull(event.getOption("user")).getAsString()
                  : null;
    Long range = event.getOption("range") != null ? Objects.requireNonNull(event.getOption("range")).getAsLong() : 20;

    switch ( subCommand ) {
      case "globalna": {
        String message = ChatColor.RED + "[G] " + ChatColor.YELLOW + "[" + narrMessage + "]";

        String logMessage = ChatUtils.DCNarrationPrefix + " " + ChatUtils.GlobalPrefix + " [" + Objects.requireNonNull(
            event.getMember()).getEffectiveName() + "]" + " `[" + narrMessage + "]`";

        this.bot.plugin.server.getOnlinePlayers().forEach(p -> p.sendMessage(message));
        ChatUtils.cacheMessageForChatLog(logMessage);

        event.reply("Wysłano narrację!").queue();
        return;
      }

      case "lokalna": {
        if ( warp == null && user == null ) {
          event.reply("Nie wiadomo gdzie wysłać narrację!").queue();
          return;
        }

        String message = ChatColor.RED + "[L] " + ChatColor.YELLOW + "[" + narrMessage + "]";

        String logMessage = ChatUtils.DCNarrationPrefix + " " + ChatUtils.LocalPrefix + " [" + Objects.requireNonNull(
            event.getMember()).getEffectiveName() + "]" + " {`" + ( user != null
                                                                    ? user
                                                                    : warp ) + "`} <`" + range + "`> `[" + narrMessage + "]`";

        Location location = user != null
                            ? this.bot.plugin.server.getPlayer(user).getLocation()
                            : FileReader.getWarp(warp);

        assert location != null;
        PlayerUtils.getPlayersWithinRange(location, range, null).forEach(p -> p.sendMessage(message));

        ChatUtils.cacheMessageForChatLog(logMessage);

        event.reply("Wysłano narrację!").queue();
        return;
      }

      case "prywatna": {
        if ( user == null ) {
          event.reply("Nie wiadomo komu wysłać narrację!").queue();
          return;
        }

        Player player = this.bot.plugin.server.getPlayer(user);

        if ( player == null || !player.isOnline() ) {
          event.reply("Nie znaleziono gracza lub offline!").queue();
          return;
        }

        String message = ChatColor.RED + "[L] " + ChatColor.YELLOW + "[" + narrMessage + "]";

        String logMessage = ChatUtils.DCNarrationPrefix + " " + ChatUtils.WhisperPrefix + " [" + Objects.requireNonNull(
            event.getMember()).getEffectiveName() + " -> " + user + "]" + " `[" + narrMessage + "]`";

        player.sendMessage(message);
        ChatUtils.cacheMessageForChatLog(logMessage);

        event.reply("Wysłano narrację!").queue();
      }
    }
  }

  private void handleMoneyManip (
      SlashCommandEvent event, String subCommand, String playerName, Boolean isBank
  ) {
    String uuid = bot.plugin.putils.resolvePlayerToUUID(playerName);

    if ( uuid == null ) {
      event.reply("Nie znaleziono UUID gracza!").queue();
      return;
    }

    OptionMapping ileOpt = event.getOption("ile");
    OptionMapping typOpt = event.getOption("typ");

    long ile = ileOpt != null ? ileOpt.getAsLong() : 0;
    String typ = typOpt != null ? typOpt.getAsString() : null;

    Map<String, Long> money = isBank ? bot.plugin.mutils.getBankMoney(uuid) : bot.plugin.mutils.getMoney(uuid);

    switch ( subCommand ) {
      case "sprawdz":
        event.reply(
            "Gracz " + playerName + " " + "Ma w " + ( isBank ? "banku" : "portfelu" ) + ": \n" + "- **" + money.get(
                "miedziak") + "** Miedziaków \n" + "- **" + money.get(
                "srebrnik") + "** Srebrników \n" + "- **" + money.get("złotnik") + "** Złotników \n").queue();

        return;
      case "dodaj": {
        if ( ile == 0 || ile < 0 || typ == null ) {
          event.reply("Niepoprawne dane!").queue();
          return;
        }

        long ileMa = money.get(typ);

        boolean check = isBank
                        ? bot.plugin.mutils.setBankMoney(uuid, typ, ileMa + ile)
                        : bot.plugin.mutils.setMoney(uuid, typ, ileMa + ile);

        if ( !check ) {
          event.reply("Coś poszło nie tak!").queue();
          return;
        }

        event.reply(
                 "Dodano **" + ile + "** " + typ + " do " + ( isBank ? "banku" : "portfelu" ) + " gracza " + playerName)
             .queue();
        return;
      }
      case "zabierz": {
        if ( ile == 0 || ile < 0 || typ == null ) {
          event.reply("Niepoprawne dane!").queue();
          return;
        }

        long ileMa = money.get(typ);

        if ( ileMa - ile < 0 ) {
          event.reply("Nie można wykonać operacji, po zabraniu będzie miał w banku mniej niż minimalna kwota!").queue();
          return;
        }

        boolean check = isBank
                        ? bot.plugin.mutils.setBankMoney(uuid, typ, ileMa - ile)
                        : bot.plugin.mutils.setMoney(uuid, typ, ileMa - ile);

        if ( !check ) {
          event.reply("Coś poszło nie tak!").queue();
          return;
        }

        event.reply(
                 "Zabrano **" + ile + "** " + typ + " z " + ( isBank ? "banku" : "portfelu" ) + " gracza " + playerName)
             .queue();
        return;
      }
    }

    event.reply("Nieobsłużona komenda...").queue();
  }

  private void ShowPlayers (
      SlashCommandEvent event
  ) {
    List<Player> online = bot.plugin.utils.getPlayers();

    StringBuilder os = new StringBuilder();
    os.append(String.format("Gracze online (%d / %d):", online.size(), bot.plugin.server.getMaxPlayers()));

    for ( Player player : online )
      os.append(String.format("\n- `%s`", player.getName()));

    event.reply(os.toString()).queue();
  }

  private void ShowOPs (
      SlashCommandEvent event
  ) {
    List<Player> online = bot.plugin.utils.getPlayers()
                                          .stream()
                                          .filter(ServerOperator::isOp)
                                          .collect(Collectors.toList());

    StringBuilder os = new StringBuilder();
    os.append(String.format("Ekipa online (%d / %d):", online.size(), bot.plugin.server.getMaxPlayers()));

    for ( Player player : online )
      os.append(String.format("\n- `%s`", player.getName()));

    event.reply(os.toString()).queue();
  }

  private boolean checkGuildPerms (
      Guild guild, boolean checkMaster
  ) {
    if ( guild == null ) return false;
    if ( checkMaster && !MessageListener.MasterGuilds.contains(guild.getId()) ) return false;

    return MessageListener.MasterGuilds.contains(guild.getId()) || MessageListener.AllowedGuilds.contains(
        guild.getId());
  }

  private static boolean hasRole (
      Member member, String roleId
  ) {
    if ( member == null || roleId == null ) return false;

    return member.getRoles().stream().anyMatch(o -> o.getId().equals(roleId));
  }
}
