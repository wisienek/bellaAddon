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
  public static final String ShoutPrefix = "**[K]**";
  public static final String PrivateMessagePrefix = "**[PM]**";
  public static final String DicePrefix = "**[DICE]**";
  public static final String DCNarrationPrefix = "**[DC]**";

  private static final Map<String, StringBuilder> cachedMessages = new HashMap<>();
  private static final Map<String, Timer> timerMap = new HashMap<>();
  private static final int DISCORD_MAX_LENGTH = 2000;
  private static final int SAFE_SPLIT_LENGTH = 1900;
  public static final Long PullTime = 100L;

  private static Main plugin;

  enum TextType {
    NORMAL, DESCRIPTION, OOC, ACTION
  }

  static class TextSegment {

    String text;
    TextType type;

    TextSegment(
        String text,
        TextType type
    ) {
      this.text = text;
      this.type = type;
    }

    @Override
    public String toString() {
      switch ( type ) {
        case NORMAL:
          return ChatColor.WHITE + text + ChatColor.WHITE;

        case DESCRIPTION:
          return ChatColor.GOLD + "**" + text + "**" + ChatColor.WHITE;

        case OOC:
          return ChatColor.DARK_GRAY + "(" + text + ")" + ChatColor.WHITE;

        case ACTION:
          return ChatColor.YELLOW + "*" + text + "*" + ChatColor.WHITE;

        default:
          return "";
      }
    }
  }

  public ChatUtils(
      Main main
  ) {
    ChatUtils.plugin = main;
  }

  public static void cacheMessageForBotLog(
      @Nonnull String channel,
      @Nullable String message
  ) {
    ChatUtils.cacheMessageForBotLog( channel, String
        .format( "%s-%s", CacheKeys.ChatCacheKey, channel ), message, false );
  }

  public static String formatChatMessage(
      @Nonnull String initial
  ) {
    List<TextSegment> segments = new ArrayList<>();
    Pattern pattern = Pattern.compile( "(\\*\\*.*?\\*\\*)|(\\(.*?\\))|(\\*.*?\\*)|([^\\*\\(]+)" );
    Matcher matcher = pattern.matcher( initial );

    while ( matcher.find() ) {
      String match = matcher.group();
      if ( match.startsWith( "**" ) && match.endsWith( "**" ) ) {
        segments.add( new TextSegment( match.substring( 2, match.length() - 2 ),
            TextType.DESCRIPTION ) );
      } else if ( match.startsWith( "(" ) && match.endsWith( ")" ) ) {
        segments.add( new TextSegment( match.substring( 1, match.length() - 1 ), TextType.OOC ) );
      } else if ( match.startsWith( "*" ) && match.endsWith( "*" ) ) {
        segments
            .add( new TextSegment( match.substring( 1, match.length() - 1 ), TextType.ACTION ) );
      } else {
        segments.add( new TextSegment( match, TextType.NORMAL ) );
      }
    }

    StringBuilder sb = new StringBuilder();
    for ( TextSegment segment : segments ) {
      if ( segment.type == TextType.NORMAL )
        sb.append( ChatUtils.formatEmojis( segment.toString() ) );
      else
        sb.append( segment );
    }

    return sb.toString();
  }

  public static synchronized void cacheMessageForBotLog(
      @Nonnull String channel,
      @Nonnull String cacheKey,
      @Nullable String message,
      Boolean sendFirst
  ) {
    if ( sendFirst )
      ChatUtils.flushCache( channel, cacheKey );

    if ( message == null )
      return;

    String hourFormat = StringUtils.getHourMinutes();
    String sanitized = StringUtils.synthesizeForDc( message );
    String line = hourFormat + sanitized;

    if ( line.length() > DISCORD_MAX_LENGTH )
      line = line.substring( 0, DISCORD_MAX_LENGTH - 3 ) + "...";

    StringBuilder cache = ChatUtils.cachedMessages
        .computeIfAbsent( cacheKey, k -> new StringBuilder() );

    int newLength = cache.length() + ( cache.length() > 0 ? 1 : 0 ) + line.length();
    if ( newLength > SAFE_SPLIT_LENGTH && cache.length() > 0 )
      ChatUtils.flushCache( channel, cacheKey );

    if ( cache.length() > 0 )
      cache.append( "\n" );
    cache.append( line );

    scheduleFlush( channel, cacheKey );
  }

  private static void scheduleFlush(
      @Nonnull String channel,
      @Nonnull String cacheKey
  ) {
    if ( ChatUtils.timerMap.containsKey( cacheKey ) )
      return;

    Timer timer = new Timer();
    timer.schedule( new TimerTask() {

      @Override
      public void run() {
        ChatUtils.timerMap.remove( cacheKey );
        ChatUtils.flushCache( channel, cacheKey );
      }
    }, ChatUtils.PullTime * 1000L );

    ChatUtils.timerMap.put( cacheKey, timer );
  }

  public static String formatEmojis(
      String message
  ) {
    String newMsg = message;

    Map<String, Object> map = plugin.configManager.emojiConfig.getValues( false );

    for ( String emoji : map.keySet() )
      newMsg = newMsg.replaceAll( "(?i)" + escapeMetaCharacters( emoji ), ChatColor.YELLOW + "*"
          + map.get( emoji ) + "*" + ChatColor.WHITE );

    return newMsg;
  }

  public static String escapeMetaCharacters(
      String inputString
  ) {
    final String[] metaCharacters = { "\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+",
        "?", "|", "<", ">", "-", "&", "%" };

    for ( String metaCharacter : metaCharacters ) {
      if ( inputString.contains( metaCharacter ) ) {
        inputString = inputString.replace( metaCharacter, "\\" + metaCharacter );
      }
    }
    return inputString;
  }

  private static synchronized void flushCache(
      @Nonnull String channel,
      @Nonnull String cacheKey
  ) {
    StringBuilder cache = ChatUtils.cachedMessages.get( cacheKey );
    if ( cache == null || cache.length() == 0 )
      return;

    String content = cache.toString();
    cache.setLength( 0 );

    while ( content.length() > 0 ) {
      if ( content.length() <= DISCORD_MAX_LENGTH ) {
        ChatUtils.plugin.bot.sendLog( content, channel );
        break;
      }

      int splitAt = content.lastIndexOf( '\n', DISCORD_MAX_LENGTH );
      if ( splitAt <= 0 ) {
        ChatUtils.plugin.bot
            .sendLog( content.substring( 0, DISCORD_MAX_LENGTH - 3 ) + "...", channel );
        content = content.substring( DISCORD_MAX_LENGTH - 3 );
      } else {
        ChatUtils.plugin.bot.sendLog( content.substring( 0, splitAt ), channel );
        content = content.substring( splitAt + 1 );
      }
    }
  }

}
