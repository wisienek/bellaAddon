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

public class ChatUtils {

  public static final String LocalPrefix = "**[L]**";
  public static final String RangePrefix = "**[R]**";
  public static final String GlobalPrefix = "**[G]**";
  public static final String OOCPrefix = "**[OOC]**";
  public static final String WhisperPrefix = "**[S]**";
  public static final String DCNarrationPrefix = "**[DC]**";

  private static final Map<String, String> cachedMessages = new HashMap<>();

  private static final Map<String, Timer> timerMap = new HashMap<>();
  public static final Long PullTime = 100L;

  private static Main plugin;

  public ChatUtils (
      Main main
  ) {
    ChatUtils.plugin = main;
  }

  public static void cacheMessageForBotLog (
      @Nonnull String channel, @Nullable String message
  ) {
    ChatUtils.cacheMessageForBotLog(channel, String.format("%s-%s", CacheKeys.ChatCacheKey, channel), message, false);
  }

  public static void cacheMessageForBotLog (
      @Nonnull String channel, @Nonnull String cacheKey, @Nullable String message, Boolean sendFirst
  ) {
    if ( sendFirst ) ChatUtils.sendCachedMessage(channel, cacheKey);

    if ( message != null ) {
      String hourFormat = StringUtils.getHourMinutes();

      message = StringUtils.synthesizeForDc(message);

      String cachedMessage = ChatUtils.cachedMessages.getOrDefault(cacheKey, "");

      String newMsg = cachedMessage.concat(( cachedMessage.length() > 0 ? "\n" : "" ) + hourFormat + message);

      if ( newMsg.length() >= 2000 ) ChatUtils.sendCachedMessage(channel, cacheKey);

      ChatUtils.cachedMessages.put(cacheKey, newMsg);

      if ( ChatUtils.timerMap.get(cacheKey) == null ) {
        Timer newTimer = new Timer();
        newTimer.schedule(new TimerTask() {

          @Override
          public void run () {
            ChatUtils.timerMap.remove(cacheKey);

            ChatUtils.sendCachedMessage(channel, cacheKey);
          }
        }, ChatUtils.PullTime * 1000L);

        ChatUtils.timerMap.put(cacheKey, newTimer);
      }
    }
  }

  public static String formatOOC (
      String message
  ) {
    StringBuilder newMsg = new StringBuilder();

    Pattern pattern = Pattern.compile("\\((.*?)\\)");
    Matcher matcher = pattern.matcher(message);
    boolean found = matcher.find();

    while ( found ) {
      // get text before action
      int index = message.indexOf("(");
      String first = message.substring(0, index + 1).replace("(", "");
      message = message.substring(index + 1);

      // get action text till end
      int index2 = message.indexOf(")");
      String second = ( index2 == -1 ) ? message : message.substring(0, index2 + 1).replace(")", "");

      message = message.substring(index2 > -1 ? index2 + 1 : 0);

      newMsg.append(first).append(ChatColor.GRAY).append("(").append(second).append(")").append(ChatColor.WHITE);

      found = matcher.find();
    }

    if ( message.length() > 0 ) {
      newMsg.append(message);
    }

    return ( newMsg.length() == 0 ) ? message : newMsg.toString();
  }

  public static String formatEmojis (
      String message
  ) {
    String newMsg = message;

    Map<String, Object> mapa = plugin.emojiConfig.getValues(false);

    for ( String emoji : mapa.keySet() )
      newMsg = newMsg.replaceAll("(?i)" + escapeMetaCharacters(emoji),
          ChatColor.YELLOW + "*" + mapa.get(emoji) + "*" + ChatColor.WHITE);

    return newMsg;
  }

  public static String escapeMetaCharacters (
      String inputString
  ) {
    final String[] metaCharacters = { "\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<", ">"
        , "-", "&", "%" };

    for ( String metaCharacter : metaCharacters ) {
      if ( inputString.contains(metaCharacter) ) {
        inputString = inputString.replace(metaCharacter, "\\" + metaCharacter);
      }
    }
    return inputString;
  }

  private static void sendCachedMessage (
      @Nonnull String channel, String cacheKey
  ) {
    String cachedMessage = ChatUtils.cachedMessages.get(cacheKey);
    int length = cachedMessage.length();

    if ( length > 0 ) {
      do {
        String partial = cachedMessage.substring(0, Math.min(2000, cachedMessage.length()));

        ChatUtils.plugin.bot.sendLog(partial, channel);

        cachedMessage = cachedMessage.replace(partial, "");

      } while ( cachedMessage.length() >= 2000 );

      ChatUtils.cachedMessages.put(cacheKey, "");
    }
  }

}
