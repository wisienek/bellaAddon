package net.woolf.bella.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
// import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.woolf.bella.Main;
import net.woolf.bella.utils.StringUtils;

public class Bot {
  public Main plugin;

  public JDA api;
  public CommandListUpdateAction commands;

  public Bot (
      Main main
  ) {
    this.plugin = main;

    File passwd = new File(plugin.getDataFolder(), "pwd.txt");

    if ( !passwd.exists() ) {
      plugin.logger.info("Nie udało się wczytać hasła do bota!");
      return;
    }

    try {
      Scanner myReader = new Scanner(passwd);
      String pwd = myReader.nextLine();
      myReader.close();

      if ( pwd == null || pwd.length() == 0 ) throw new FileNotFoundException("pwd isblank");

      final Set<GatewayIntent> intents = new HashSet<>(
          Arrays.asList(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
              GatewayIntent.GUILD_MESSAGES));

      JDABuilder builder = JDABuilder.createDefault(pwd)
                                     .enableIntents(intents)
                                     .setActivity(Activity.watching("Online!"));

      MessageListener listener = new MessageListener(this);
      builder.addEventListeners(listener);

      api = builder.build().awaitReady();

      plugin.logger.info("Zalogowano bota!");

      setupCommands();
    } catch ( FileNotFoundException | LoginException | InterruptedException e ) {
      e.printStackTrace();
    }
  }

  private void setupCommands () {
    CommandListUpdateAction commands = api.updateCommands();

    ArrayList<OptionData> userCollection = new ArrayList<>();
    userCollection.add(new OptionData(OptionType.STRING, "user", "Nick gracza którego chcesz sprawdzić", false));
    userCollection.add(new OptionData(OptionType.USER, "dcuser", "Oznaczony gracz (Musi mieć podpięte konto)", false));

    ArrayList<OptionData> moneyInfo = new ArrayList<>();
    moneyInfo.add(new OptionData(OptionType.INTEGER, "ile", "Ile kasy dodać graczowi", true));
    moneyInfo.add(
        new OptionData(OptionType.STRING, "typ", "Typ pieniążka", true).addChoices(new Choice("Miedziak", "miedziak"),
            new Choice("Srebrnik", "srebrnik"), new Choice("Złotnik", "złotnik")));

    OptionData narrText = new OptionData(OptionType.STRING, "text", "Text narracji", true);

    ArrayList<OptionData> narrCollection = new ArrayList<>();
    narrCollection.add(narrText);
    narrCollection.add(new OptionData(OptionType.STRING, "warp", "Na jakiego warpa wysłać narrację (lokalna)", false));
    narrCollection.add(
        new OptionData(OptionType.STRING, "user", "Nick gracza wokół którego chcesz wysłać narrację", false));
    narrCollection.add(new OptionData(OptionType.INTEGER, "range", "Jaki ma być range narracji (default 20m.)", false));

    List<OptionData> moneyAndUsers = Stream.concat(moneyInfo.stream(), userCollection.stream())
                                           .distinct()
                                           .collect(Collectors.toList());

    SubcommandData sprawdz = new SubcommandData("sprawdz", "Sprawdza stan gotówki").addOptions(userCollection);
    SubcommandData dodaj = new SubcommandData("dodaj", "Dodaje kasę dla gracza").addOptions(moneyAndUsers);
    SubcommandData zabierz = new SubcommandData("zabierz", "Zabiera kasę od gracza").addOptions(moneyAndUsers);

    SubcommandData globalNar = new SubcommandData("globalna", "Globalna narrracja").addOptions(narrText);
    SubcommandData lokalNar = new SubcommandData("lokalna", "Lokalna narrracja - podaj warpa lub koordy").addOptions(
        narrCollection);
    SubcommandData privNar = new SubcommandData("prywatna", "Prywatna narrracja").addOptions(narrCollection);

    commands.addCommands(new CommandData("who", "Listuje wszystkich aktywnych użytkowników").addOptions(
            new OptionData(OptionType.BOOLEAN, "ekipa", "czy wyświetlić też ekipę").setRequired(true)),

        new CommandData("link", "Dodaje konto serwerowe do discorda").addOption(OptionType.STRING, "code",
            "Kod wygenerowany w grze", true),

        new CommandData("portfel", "Komendy portfelowe admina").addSubcommands(sprawdz, dodaj, zabierz),

        new CommandData("bank", "Komendy bankowe admina").addSubcommands(sprawdz, dodaj, zabierz),

        new CommandData("narracja", "Wysyła narrację na serwer").addSubcommands(globalNar, lokalNar, privNar));

    commands.queue();

    plugin.logger.info("Zarejestrowano komendy!");
  }

  public void updatePresence (
      @Nonnull String msg
  ) {
    api.getPresence().setActivity(Activity.watching(msg));
  }

  public void sendLog (
      @Nonnull final String msg, @Nonnull final String logsID
  ) {
    TextChannel channel = api.getTextChannelById(logsID);
    if ( channel == null ) {
      plugin.logger.info("Nie można było rozwiązać kanału: " + logsID);
      return;
    }

    channel.sendMessage(msg).complete();
  }

  public void sendMessageToUser (
      @Nonnull final String userId, @Nonnull final String msg
  ) {
    User user = api.retrieveUserById(userId).complete();

    if ( user == null ) {
      this.plugin.logger.info("Nullish user on BOT.sendmessageToUser id: " + userId);
      return;
    }

    if ( msg.length() >= 1800 ) {
      List<String> messages = StringUtils.divideStringIntoDCMessages(msg);

      for ( String message : messages )
        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)).queue();

    }
    else {
      user.openPrivateChannel().flatMap(channel -> channel.sendMessage(msg)).queue();
    }

  }
}
