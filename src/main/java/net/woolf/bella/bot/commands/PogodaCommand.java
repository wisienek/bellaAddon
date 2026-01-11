package net.woolf.bella.bot.commands;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.woolf.bella.bot.Bot;

public class PogodaCommand implements DiscordCommand {

    @Override
    public void execute(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot
    ) {
        if ( !this.checkGuildPermissions( event.getGuild(), true ) ) {
            event.reply( "Gildia nie jest na whiteliście!" ).queue();
            return;
        }

        event.reply( "Pogoda wkrótce!" ).queue();
    }
}
