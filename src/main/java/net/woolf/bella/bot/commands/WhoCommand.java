package net.woolf.bella.bot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.woolf.bella.bot.Bot;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;

public class WhoCommand implements DiscordCommand {

    @Override
    public void execute(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot
    ) {
        if ( !this.checkGuildPermissions( event.getGuild(), true ) ) {
            event.reply( "Gildia nie jest na whiteliście!" ).queue();
            return;
        }

        OptionMapping ekipaOpt = event.getOption( "ekipa" );
        boolean ekipa = ekipaOpt != null && ekipaOpt.getAsBoolean();

        if ( ekipa )
            this.showOPs( event, bot );
        else
            this.showPlayers( event, bot );
    }

    private void showPlayers(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot
    ) {
        List<Player> online = new ArrayList<>( bot.plugin.server.getOnlinePlayers() );

        StringBuilder os = new StringBuilder();
        os.append( String.format( "Gracze online (%d / %d):", online.size(), bot.plugin.server
                .getMaxPlayers() ) );

        for ( Player player : online ) {
            String name = player.getName();
            if ( name != null )
                os.append( String.format( "\n- `%s`", name ) );
        }

        event.reply( os.toString() ).queue();
    }

    private void showOPs(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot
    ) {
        List<Player> online = new ArrayList<>( bot.plugin.server.getOnlinePlayers() ).stream()
                .filter( ServerOperator::isOp )
                .collect( Collectors.toList() );

        StringBuilder os = new StringBuilder();
        os.append( String.format( "Ekipa online (%d / %d):", online.size(), bot.plugin.server
                .getMaxPlayers() ) );

        for ( Player player : online ) {
            String name = player.getName();
            if ( name != null )
                os.append( String.format( "\n- `%s`", name ) );
        }

        event.reply( os.toString() ).queue();
    }
}
