package net.woolf.bella.bot.commands;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.woolf.bella.bot.Bot;

public interface DiscordCommand {

    void execute(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot
    );

    default boolean checkGuildPermissions(
            @Nullable Guild guild,
            boolean requireMaster
    ) {
        if ( guild == null )
            return false;

        if ( requireMaster )
            return DiscordCommandHelper.isMasterGuild( guild.getId() );

        return DiscordCommandHelper.isMasterGuild( guild.getId() )
                || DiscordCommandHelper.isAllowedGuild( guild.getId() );
    }
}
