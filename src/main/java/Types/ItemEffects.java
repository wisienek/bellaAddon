package Types;

import java.util.HashMap;
import java.util.Map;

public enum ItemEffects {

  TEST("test"), FLIGHT("lot"), INVISIBILITY("niewidzialnosc"), SPEED("predkosc"), GLOW("swiecenie");

  private final String text;
  public static final Map<String, ItemEffects> stringToEnum = new HashMap<>();

  static {
    for ( ItemEffects item : values() ) {
      stringToEnum.put( item.toString(), item );
    }
  }

  ItemEffects(
      final String text
  ) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }

  public static ItemEffects fromString(
      final String text
  ) {
    return stringToEnum.get( text );
  }
}
