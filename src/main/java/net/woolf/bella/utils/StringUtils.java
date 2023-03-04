package net.woolf.bella.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class StringUtils {

  public static String limitLength (
      @NotNull final String limited, final int limitor
  ) {
    return limited.length() > limitor ? limited.substring(0, limitor) : limited;
  }

  public static String synthesizeForDc (
      @NotNull String msg
  ) {
    return msg.replaceAll("§.", "")
              .replaceAll("@(here|everyone)", "")
              .replaceAll("<@\\d{0,24}>", "")
              .replaceAll("\\|", "");
  }

  public static List<String> divideStringIntoDCMessages (
      @NotNull String msg
  ) {
    List<String> messages = new ArrayList<>();

    do {
      String split = msg.substring(0, 1800);

      messages.add(split);

      msg = msg.substring(1800);
    } while ( msg.length() >= 1800 );

    return messages;
  }

  public static String formatLocation (
      @NotNull Location loc
  ) {
    return String.format("%s %s %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
  }

  @SuppressWarnings( "deprecation" )
  public static String getHourMinutes () {
    Date today = new Date();
    int hours = today.getHours();
    int minutes = today.getMinutes();

    return "[" + ( hours < 10 ? "0" + hours : hours ) + ":" + ( minutes < 10 ? "0" + minutes : minutes ) + "] ";
  }
}
