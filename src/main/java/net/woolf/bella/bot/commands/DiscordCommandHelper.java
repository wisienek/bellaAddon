package net.woolf.bella.bot.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Member;

public class DiscordCommandHelper {

    private static final HashSet<String> MASTER_GUILDS = new HashSet<>(
            Arrays.asList( "809181125640454194", "522449658505723905" ) );
    private static final HashSet<String> ALLOWED_GUILDS = new HashSet<>(
            Collections.singletonList( "840884051174752256" ) );

    public static boolean isMasterGuild(
            @Nonnull String guildId
    ) {
        return MASTER_GUILDS.contains( guildId );
    }

    public static boolean isAllowedGuild(
            @Nonnull String guildId
    ) {
        return ALLOWED_GUILDS.contains( guildId );
    }

    public static boolean hasRole(
            @Nonnull Member member,
            @Nonnull String roleId
    ) {
        if ( member == null || roleId == null )
            return false;

        return member.getRoles().stream().anyMatch( o -> o.getId().equals( roleId ) );
    }
}
