package net.woolf.bella.bot.commands;

import javax.annotation.Nonnull;

import Types.BotChannels;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.utils.FileReader;
import net.woolf.bella.utils.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NarracjaCommand implements DiscordCommand {

    private static final String NARRATOR_ROLE_ID = "809423929864749086";

    @Override
    public void execute(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot
    ) {
        if ( !this.checkGuildPermissions( event.getGuild(), true ) ) {
            event.reply( "Gildia nie jest na whiteliście!" ).queue();
            return;
        }

        if (
            event.getMember() == null
                    || !DiscordCommandHelper.hasRole( event.getMember(), NARRATOR_ROLE_ID )
        ) {
            event.reply( "Nie posiadasz roli narratora!" ).queue();
            return;
        }

        String subCommand = event.getSubcommandName();

        if ( subCommand == null )
            return;

        OptionMapping textOpt = event.getOption( "text" );
        if ( textOpt == null ) {
            event.reply( "Musisz podać tekst narracji!" ).queue();
            return;
        }

        String narrMessage = textOpt.getAsString();

        OptionMapping warpOpt = event.getOption( "warp" );
        OptionMapping userOpt = event.getOption( "user" );
        OptionMapping rangeOpt = event.getOption( "range" );

        String warp = warpOpt != null ? warpOpt.getAsString() : null;
        String user = userOpt != null ? userOpt.getAsString() : null;
        Long range = rangeOpt != null ? rangeOpt.getAsLong() : 20L;

        switch ( subCommand ) {
            case "globalna":
                this.handleGlobal( event, bot, narrMessage );
                break;

            case "lokalna":
                this.handleLocal( event, bot, narrMessage, warp, user, range );
                break;

            case "prywatna":
                this.handlePrivate( event, bot, narrMessage, user );
                break;
        }
    }

    private void handleGlobal(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot,
            @Nonnull String narrMessage
    ) {
        String message = ChatColor.RED + "[G] " + ChatColor.YELLOW + "[" + narrMessage + "]";

        String memberName = event.getMember() != null
                && event.getMember().getEffectiveName() != null
                        ? event.getMember().getEffectiveName()
                        : "Unknown";
        String logMessage = ChatUtils.DCNarrationPrefix + " " + ChatUtils.GlobalPrefix + " ["
                + memberName + "]" + " `[" + narrMessage + "]`";

        bot.plugin.server.getOnlinePlayers().forEach( p -> p.sendMessage( message ) );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), logMessage );

        event.reply( "Wysłano narrację!" ).queue();
    }

    private void handleLocal(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot,
            @Nonnull String narrMessage,
            String warp,
            String user,
            @Nonnull Long range
    ) {
        if ( warp == null && user == null ) {
            event.reply( "Nie wiadomo gdzie wysłać narrację!" ).queue();
            return;
        }

        String message = ChatColor.RED + "[L] " + ChatColor.YELLOW + "[" + narrMessage + "]";

        String memberName = event.getMember() != null
                && event.getMember().getEffectiveName() != null
                        ? event.getMember().getEffectiveName()
                        : "Unknown";
        String target = user != null ? user : ( warp != null ? warp : "unknown" );
        String logMessage = ChatUtils.DCNarrationPrefix + " " + ChatUtils.LocalPrefix + " ["
                + memberName + "]" + " {`" + target + "`} <`" + range + "`> `[" + narrMessage
                + "]`";

        Location location = user != null && bot.plugin.server.getPlayer( user ) != null
                ? bot.plugin.server.getPlayer( user ).getLocation()
                : ( warp != null ? FileReader.getWarp( warp ) : null );

        if ( location == null ) {
            event.reply( "Nie znaleziono lokalizacji!" ).queue();
            return;
        }

        PlayerUtils.getPlayersWithinRange( location, range, null )
                .forEach( p -> p.sendMessage( message ) );

        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), logMessage );

        event.reply( "Wysłano narrację!" ).queue();
    }

    private void handlePrivate(
            @Nonnull SlashCommandInteractionEvent event,
            @Nonnull Bot bot,
            @Nonnull String narrMessage,
            String user
    ) {
        if ( user == null ) {
            event.reply( "Nie wiadomo komu wysłać narrację!" ).queue();
            return;
        }

        Player player = bot.plugin.server.getPlayer( user );

        if ( player == null || !player.isOnline() ) {
            event.reply( "Nie znaleziono gracza lub offline!" ).queue();
            return;
        }

        String message = ChatColor.RED + "[L] " + ChatColor.YELLOW + "[" + narrMessage + "]";

        String memberName = event.getMember() != null
                && event.getMember().getEffectiveName() != null
                        ? event.getMember().getEffectiveName()
                        : "Unknown";
        String logMessage = ChatUtils.DCNarrationPrefix + " " + ChatUtils.WhisperPrefix + " ["
                + memberName + " -> " + user + "]" + " `[" + narrMessage + "]`";

        player.sendMessage( message );
        ChatUtils.cacheMessageForBotLog( BotChannels.ChatLogId.toString(), logMessage );

        event.reply( "Wysłano narrację!" ).queue();
    }
}
