package net.woolf.bella.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;

import Types.CacheKeys;
import net.woolf.bella.Main;
import net.woolf.bella.bot.Bot;

public class ChatUtils {

	public static final String LocalPrefix = "**[L]**";
	public static final String RangePrefix = "**[R]**";
	public static final String GlobalPrefix = "**[G]**";
	public static final String OOCPrefix = "**[OOC]**";
	public static final String WhisperPrefix = "**[S]**";
	public static final String DCNarrationPrefix = "**[DC]**";

	private static final Map<String, String> cachedMessages = new HashMap<String, String>();

	private static Map<String, Timer> timerMap = new HashMap<String, Timer>();
	public static final Long PullTime = 100l;

	private static Main plugin;

	public ChatUtils(
			Main main
	) {
		ChatUtils.plugin = main;
	}

	public static void cacheMessageForChatLog(
			@Nullable String message
	) {
		ChatUtils.cacheMessageForChatLog( CacheKeys.ChatCacheKey.toString(), message, false );
		return;
	}

	public static void cacheMessageForChatLog(
			@Nonnull String cacheKey,
			@Nullable String message,
			Boolean sendFirst
	) {
		if ( sendFirst )
			ChatUtils.sendCachedMessage( cacheKey );

		if ( message != null ) {
			String hourFormat = StringUtils.getHourMinutes();

			message = StringUtils.synthesizeForDc( message );

			String cachedMessage = ChatUtils.cachedMessages.containsKey( cacheKey )
					? ChatUtils.cachedMessages.get( cacheKey )
					: "";

			String newMsg = cachedMessage
					.concat( ( cachedMessage.length() > 0 ? "\n" : "" ) + hourFormat + message );

			if ( newMsg.length() >= 2000 )
				ChatUtils.sendCachedMessage( cacheKey );

			ChatUtils.cachedMessages.put( cacheKey, newMsg );

			if ( ChatUtils.timerMap.get( cacheKey ) == null ) {
				Timer newTimer = new Timer();
				newTimer.schedule( new TimerTask() {

					@Override
					public void run() {
						ChatUtils.timerMap.remove( cacheKey );

						ChatUtils.sendCachedMessage( cacheKey );
					}
				}, ChatUtils.PullTime * 1000L );

				ChatUtils.timerMap.put( cacheKey, newTimer );
			}
		}
	}

	public static String formatOOC(
			String message
	) {
		String newMsg = "";

		Pattern pattern = Pattern.compile( "\\((.*?)\\)" );
		Matcher matcher = pattern.matcher( message );
		Boolean found = matcher.find();

		while ( found ) {
			// get text before action
			int index = message.indexOf( "(" );
			String first = message.substring( 0, index + 1 ).replace( "(", "" );
			message = message.substring( index + 1 );

			// get action text till end
			int index2 = message.indexOf( ")" );
			String second = ( index2 == -1 ) ? message
					: message.substring( 0, index2 + 1 ).replace( ")", "" );

			message = message.substring( index2 > -1 ? index2 + 1 : 0 );

			newMsg += first + ChatColor.GRAY + "(" + second + ")" + ChatColor.WHITE;

			found = matcher.find();
		}

		if ( message.length() > 0 ) {
			newMsg += message;
		}

		return ( newMsg.length() == 0 ) ? message : newMsg;
	}

	public static String formatEmojis(
			String message
	) {
		String newMsg = message;

		Map<String, Object> mapa = plugin.emojiConfig.getValues( false );

		for ( String emoji : mapa.keySet() )
			newMsg = newMsg.replaceAll( "(?i)" + escapeMetaCharacters( emoji ), ChatColor.YELLOW
					+ "*" + (String) mapa.get( emoji ) + "*" + ChatColor.WHITE );

		return newMsg;
	}

	public static String escapeMetaCharacters(
			String inputString
	) {
		final String[] metaCharacters = { "\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*",
				"+", "?", "|", "<", ">", "-", "&", "%" };

		for ( int i = 0; i < metaCharacters.length; i++ ) {
			if ( inputString.contains( metaCharacters[i] ) ) {
				inputString = inputString.replace( metaCharacters[i], "\\" + metaCharacters[i] );
			}
		}
		return inputString;
	}

	private static void sendCachedMessage(
			String cacheKey
	) {
		String cachedMessage = ChatUtils.cachedMessages.get( cacheKey );
		int length = cachedMessage.length();

		if ( length > 0 ) {
			do {
				String partial = cachedMessage
						.substring( 0, 2000 > cachedMessage.length() ? cachedMessage.length()
								: 2000 );

				ChatUtils.plugin.bot.sendLog( partial, Bot.ChatLogId );

				cachedMessage = cachedMessage.replace( partial, "" );

			} while ( cachedMessage.length() >= 2000 );

			ChatUtils.cachedMessages.put( cacheKey, "" );
		}
	}

}
