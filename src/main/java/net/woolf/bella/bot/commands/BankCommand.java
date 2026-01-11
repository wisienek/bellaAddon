package net.woolf.bella.bot.commands;

import java.util.Map;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.woolf.bella.bot.Bot;

public class BankCommand implements DiscordCommand {

    @Override
    public void execute(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot
    ) {
        if ( !this.checkGuildPermissions( event.getGuild(), true ) ) {
            event.reply( "Gildia nie jest na whiteliście!" ).queue();
            return;
        }

        if ( event.getOption( "user" ) == null && event.getOption( "dcuser" ) == null ) {
            event.reply( "Musisz podać nick lub oznaczyć gracza!" ).queue();
            return;
        }

        OptionMapping userOpt = event.getOption( "user" );
        if ( userOpt == null ) {
            event.reply( "Musisz podać nick gracza!" ).queue();
            return;
        }

        String playerName = userOpt.getAsString();
        String subCommand = event.getSubcommandName();

        if ( subCommand == null ) {
            event.reply( "Nieobsłużona komenda..." ).queue();
            return;
        }

        this.handleMoneyManip( event, bot, subCommand, playerName, true );
    }

    private void handleMoneyManip(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot,
            @Nonnull String subCommand,
            @Nonnull String playerName,
            boolean isBank
    ) {
        String uuid = bot.plugin.putils.resolvePlayerToUUID( playerName );

        if ( uuid == null ) {
            event.reply( "Nie znaleziono UUID gracza!" ).queue();
            return;
        }

        OptionMapping ileOpt = event.getOption( "ile" );
        OptionMapping typOpt = event.getOption( "typ" );

        long ile = ileOpt != null ? ileOpt.getAsLong() : 0;
        String typ = typOpt != null ? typOpt.getAsString() : null;

        Map<String, Long> money = isBank ? bot.plugin.mutils.getBankMoney( uuid )
                : bot.plugin.mutils.getMoney( uuid );

        switch ( subCommand ) {
            case "sprawdz":
                event.reply( "Gracz " + playerName + " " + "Ma w "
                        + ( isBank ? "banku" : "portfelu" ) + ": \n" + "- **"
                        + money.get( "miedziak" ) + "** Miedziaków \n" + "- **"
                        + money.get( "srebrnik" ) + "** Srebrników \n" + "- **"
                        + money.get( "złotnik" ) + "** Złotników \n" ).queue();
                return;

            case "dodaj": {
                if ( ile == 0 || ile < 0 || typ == null ) {
                    event.reply( "Niepoprawne dane!" ).queue();
                    return;
                }

                long ileMa = money.get( typ );

                boolean check = isBank ? bot.plugin.mutils.setBankMoney( uuid, typ, ileMa + ile )
                        : bot.plugin.mutils.setMoney( uuid, typ, ileMa + ile );

                if ( !check ) {
                    event.reply( "Coś poszło nie tak!" ).queue();
                    return;
                }

                event.reply( "Dodano **" + ile + "** " + typ + " do "
                        + ( isBank ? "banku" : "portfelu" ) + " gracza " + playerName ).queue();
                return;
            }

            case "zabierz": {
                if ( ile == 0 || ile < 0 || typ == null ) {
                    event.reply( "Niepoprawne dane!" ).queue();
                    return;
                }

                long ileMa = money.get( typ );

                if ( ileMa - ile < 0 ) {
                    event.reply( "Nie można wykonać operacji, po zabraniu będzie miał w banku mniej niż minimalna kwota!" )
                            .queue();
                    return;
                }

                boolean check = isBank ? bot.plugin.mutils.setBankMoney( uuid, typ, ileMa - ile )
                        : bot.plugin.mutils.setMoney( uuid, typ, ileMa - ile );

                if ( !check ) {
                    event.reply( "Coś poszło nie tak!" ).queue();
                    return;
                }

                event.reply( "Zabrano **" + ile + "** " + typ + " z "
                        + ( isBank ? "banku" : "portfelu" ) + " gracza " + playerName ).queue();
                return;
            }
        }

        event.reply( "Nieobsłużona komenda..." ).queue();
    }
}
