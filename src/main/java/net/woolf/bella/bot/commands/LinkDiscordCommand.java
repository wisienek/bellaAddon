package net.woolf.bella.bot.commands;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.woolf.bella.Main;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.commands.LinkCommand;
import net.woolf.bella.utils.DbUtils;

import java.sql.SQLException;

public class LinkDiscordCommand implements DiscordCommand {

    @Override
    public void execute(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot
    ) {
        if ( !this.checkGuildPermissions( event.getGuild(), false ) ) {
            event.reply( "Gildia nie jest na whiteliście!" ).queue();
            return;
        }

        OptionMapping codeOpt = event.getOption( "code" );
        if ( codeOpt == null ) {
            event.reply( "Musisz podać poprawny kod!" ).setEphemeral( true ).queue();
            return;
        }

        String code = codeOpt.getAsString();

        if (
            !LinkCommand.CachedKeys.containsKey( code )
                    || LinkCommand.CachedKeys.get( code ) == null
        ) {
            event.reply( "Wprowadzony kod jest niepoprawny: `" + code + "`" )
                    .setEphemeral( true )
                    .queue();
            return;
        }

        String uuid = LinkCommand.CachedKeys.get( code );
        if ( uuid != null ) {
            this.linkAccount( event, bot, uuid );
            LinkCommand.CachedKeys.remove( code );
        }
    }

    private void linkAccount(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot,
            @Nonnull String uuid
    ) {
        try {
            DbUtils db = DbUtils.getInstance();

            if ( event.getMember() == null ) {
                event.reply( "Błąd: brak informacji o użytkowniku!" ).setEphemeral( true ).queue();
                return;
            }

            String msg = db.connectAccount( event.getMember().getId(), uuid );

            Map<String, String[]> accounts = db.getConnectedAccounts( uuid );

            if ( accounts == null || !accounts.containsKey( uuid ) ) {
                event.reply( msg ).queue();
                return;
            }

            String[] accountData = accounts.get( uuid );
            String accountName = accountData != null && accountData.length > 0
                    && accountData[0] != null ? accountData[0] : "Unknown";
            event.reply( "Połączono konto " + accountName + "(`" + uuid + "`) z discordem!" )
                    .setEphemeral( true )
                    .queue();
        } catch ( SQLException | IOException e ) {
            Main.getInstance().logger.info( "Niepowodzenie przy linkowaniu konta! DISCORD" );
            event.reply( "Jakiś błąd wywaliło przy odczytywaniu danych :(" )
                    .setEphemeral( true )
                    .queue();

            e.printStackTrace();
        }
    }
}
