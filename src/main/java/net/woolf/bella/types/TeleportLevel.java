package net.woolf.bella.types;

public class TeleportLevel {

  private final int level;
  private final int cooldown;
  private final int radius;
  private final int maxPlayers;
  private final int maxPoints;
  private final int setMaxUse;

  public TeleportLevel(
      int level,
      int cooldown,
      int radius,
      int maxPlayers,
      int maxPoints,
      int setMaxUse
  ) {
    this.level = level;
    this.cooldown = cooldown;
    this.radius = radius;
    this.maxPlayers = maxPlayers;
    this.maxPoints = maxPoints;
    this.setMaxUse = setMaxUse;
  }

  public int getLevel() {
    return level;
  }

  public int getCooldown() {
    return cooldown;
  }

  public int getRadius() {
    return radius;
  }

  public int getMaxPlayers() {
    return maxPlayers;
  }

  public int getMaxPoints() {
    return maxPoints;
  }

  public int getSetMaxUse() {
    return setMaxUse;
  }

  public static TeleportLevel fromConfig(
      org.bukkit.configuration.file.FileConfiguration config,
      int level
  ) {
    String levelStr = String.valueOf( level );
    int cooldown = Integer.parseInt( config.getString( "tp-level-" + levelStr + "-cld", "30" ) );
    int radius = Integer.parseInt( config.getString( "tp-level-" + levelStr + "-radius", "0" ) );
    int maxPlayers = Integer.parseInt( config.getString( "tp-level-" + levelStr + "-maxp", "0" ) );
    int maxPoints = Integer
        .parseInt( config.getString( "tp-level-" + levelStr + "-maxpoints", "0" ) );
    int setMaxUse = Integer
        .parseInt( config.getString( "tp-level-" + levelStr + "-setmaxuse", "0" ) );

    return new TeleportLevel( level, cooldown, radius, maxPlayers, maxPoints, setMaxUse );
  }
}
