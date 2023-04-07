package net.woolf.bella.utils;

import java.util.*;
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

  enum TextType {
    NORMAL, DESCRIPTION, OOC, ACTION
  }

  static class TextSegment {
    String text;
    TextType type;

    TextSegment (String text, TextType type) {
      this.text = text;
      this.type = type;
    }

    @Override
    public String toString () {
      switch ( type ) {
        case NORMAL:
          return ChatColor.GRAY + text + ChatColor.GRAY;
        case DESCRIPTION:
          return ChatColor.GOLD + "**" + text + "**" + ChatColor.GRAY;
        case OOC:
          return ChatColor.DARK_GRAY + "(" + text + ")" + ChatColor.GRAY;
        case ACTION:
          return ChatColor.YELLOW + "*" + text + "*" + ChatColor.GRAY;
        default:
          return "";
      }
    }
  }

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

  public static String formatChatMessage (
      @Nonnull String initial
  ) {
    List<TextSegment> segments = new ArrayList<>();
    Pattern pattern = Pattern.compile("(\\*\\*.*?\\*\\*)|(\\(.*?\\))|(\\*.*?\\*)|([^\\*\\(]+)");
    Matcher matcher = pattern.matcher(initial);

    while ( matcher.find() ) {
      String match = matcher.group();
      if ( match.startsWith("**") && match.endsWith("**") ) {
        segments.add(new TextSegment(match.substring(2, match.length() - 2), TextType.DESCRIPTION));
      }
      else if ( match.startsWith("(") && match.endsWith(")") ) {
        segments.add(new TextSegment(match.substring(1, match.length() - 1), TextType.OOC));
      }
      else if ( match.startsWith("*") && match.endsWith("*") ) {
        segments.add(new TextSegment(match.substring(1, match.length() - 1), TextType.ACTION));
      }
      else {
        segments.add(new TextSegment(match, TextType.NORMAL));
      }
    }

    StringBuilder sb = new StringBuilder();
    for ( TextSegment segment : segments ) {
      if ( segment.type == TextType.NORMAL ) sb.append(ChatUtils.formatEmojis(segment.toString()));
      else sb.append(segment);
    }

    return sb.toString();
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

  public static String formatEmojis (
      String message
  ) {
    String newMsg = message;

    Map<String, Object> map = plugin.emojiConfig.getValues(false);

    for ( String emoji : map.keySet() )
      newMsg = newMsg.replaceAll("(?i)" + escapeMetaCharacters(emoji),
          ChatColor.YELLOW + "*" + map.get(emoji) + "*" + ChatColor.WHITE);

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
