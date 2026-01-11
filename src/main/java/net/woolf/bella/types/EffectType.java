package net.woolf.bella.types;

import org.bukkit.Color;
import org.bukkit.Particle;

public enum EffectType {

  IGNIS("ignis"), AQUA("aqua"), GEO("geo"), ELECTRO("electro"), AETER("aeter"), CALIGO("caligo"),
  LUX("lux");

  private final String name;

  EffectType(
      String name
  ) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static EffectType fromString(
      String name
  ) {
    if ( name == null || name.isEmpty() )
      return IGNIS;

    for ( EffectType type : values() ) {
      if ( type.name.equalsIgnoreCase( name ) )
        return type;
    }

    return IGNIS;
  }

  public Particle getDefaultParticle() {
    switch ( this ) {
      case LUX:
        return Particle.TOTEM;

      case ELECTRO:
        return Particle.SPELL_WITCH;

      case CALIGO:
        return Particle.SMOKE_LARGE;

      default:
        return Particle.CLOUD;
    }
  }

  public Color getDefaultColor() {
    switch ( this ) {
      case ELECTRO:
        return Color.PURPLE;

      default:
        return null;
    }
  }
}
