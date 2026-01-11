package net.woolf.bella.bot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.woolf.bella.bot.commands.BankCommand;
import net.woolf.bella.bot.commands.DiscordCommand;
import net.woolf.bella.bot.commands.LinkDiscordCommand;
import net.woolf.bella.bot.commands.NarracjaCommand;
import net.woolf.bella.bot.commands.PogodaCommand;
import net.woolf.bella.bot.commands.PortfelCommand;
import net.woolf.bella.bot.commands.WhoCommand;

public class MessageListener extends ListenerAdapter {

  private final Bot bot;
  private final Map<String, DiscordCommand> commands;

  public MessageListener(
      Bot _bot
  ) {
    this.bot = _bot;
    this.commands = new HashMap<>();
    this.registerCommands();
  }

  private void registerCommands() {
    this.commands.put( "who", new WhoCommand() );
    this.commands.put( "portfel", new PortfelCommand() );
    this.commands.put( "bank", new BankCommand() );
    this.commands.put( "narracja", new NarracjaCommand() );
    this.commands.put( "pogoda", new PogodaCommand() );
    this.commands.put( "link", new LinkDiscordCommand() );
  }

  @Override
  public void onSlashCommandInteraction(
      @Nonnull SlashCommandInteractionEvent event
  ) {
    String commandName = event.getName();
    DiscordCommand command = this.commands.get( commandName );

    if ( command == null ) {
      event.reply( "Nieznana komenda: " + commandName ).queue();
      return;
    }

    command.execute( event, this.bot );
  }
}
