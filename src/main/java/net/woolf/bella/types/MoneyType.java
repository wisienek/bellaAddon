package net.woolf.bella.types;

public enum MoneyType {

  MIEDZIAK("miedziak"), SREBRNIK("srebrnik"), ZLOTNIK("złotnik");

  private final String name;

  MoneyType(
      String name
  ) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static MoneyType fromString(
      String name
  ) {
    if ( name == null || name.isEmpty() )
      return null;

    for ( MoneyType type : values() ) {
      if ( type.name.equalsIgnoreCase( name ) )
        return type;
    }

    return null;
  }
}
