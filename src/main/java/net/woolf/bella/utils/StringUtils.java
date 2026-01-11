package net.woolf.bella.utils;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class StringUtils {

  public static String limitLength(
      @NotNull final String limited,
      final int limitor
  ) {
    return limited.length() > limitor ? limited.substring( 0, limitor ) : limited;
  }

  public static String escapeDiscordMarkdown(
      @NotNull String text
  ) {
    return text.replace( "\\", "\\\\" )
        .replace( "*", "\\*" )
        .replace( "_", "\\_" )
        .replace( "~", "\\~" )
        .replace( "|", "\\|" )
        .replace( ">", "\\>" );
  }

  public static String synthesizeForDc(
      @NotNull String msg
  ) {
    return msg.replaceAll( "§.", "" )
        .replaceAll( "@(here|everyone)", "" )
        .replaceAll( "<@\\d{0,24}>", "" );
  }

  public static List<String> divideStringIntoDCMessages(
      @NotNull String msg
  ) {
    List<String> messages = new ArrayList<>();

    while ( msg.length() > 0 ) {
      if ( msg.length() <= 1800 ) {
        messages.add( msg );
        break;
      }

      int splitAt = msg.lastIndexOf( '\n', 1800 );
      if ( splitAt <= 0 )
        splitAt = 1800;

      messages.add( msg.substring( 0, splitAt ) );
      msg = msg.substring( splitAt ).replaceFirst( "^\n", "" );
    }

    return messages;
  }

  public static String formatLocation(
      @NotNull Location loc
  ) {
    return String.format( "%s %s %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() );
  }

  public static String getHourMinutes() {
    LocalDateTime now = LocalDateTime.now();
    int hours = now.getHour();
    int minutes = now.getMinute();

    return "[" + ( hours < 10 ? "0" + hours : hours ) + ":"
        + ( minutes < 10 ? "0" + minutes : minutes ) + "] ";
  }
}
