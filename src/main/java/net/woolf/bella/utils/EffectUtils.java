package net.woolf.bella.utils;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.effect.DonutEffect;
import de.slikey.effectlib.effect.EarthEffect;
import de.slikey.effectlib.effect.FountainEffect;
import de.slikey.effectlib.effect.StarEffect;
import de.slikey.effectlib.effect.TornadoEffect;
import de.slikey.effectlib.effect.VortexEffect;
import net.woolf.bella.Main;
import net.woolf.bella.types.EffectType;

public class EffectUtils {

  private final Main plugin;

  public EffectUtils(
      Main plugin
  ) {
    this.plugin = plugin;
  }

  public Effect getPlayerEffect(
      Player player,
      String typeName
  ) {
    EffectType type = EffectType.fromString( typeName );

    switch ( type ) {
      case IGNIS: {
        TornadoEffect tpParticles = new TornadoEffect( plugin.effectManager );
        tpParticles.setEntity( player );
        tpParticles.tornadoHeight = (float) 2.4;
        tpParticles.maxTornadoRadius = (float) 1.5;
        tpParticles.yOffset = -2;
        tpParticles.showCloud = false;
        return tpParticles;
      }

      case CALIGO: {
        VortexEffect tpParticles = new VortexEffect( plugin.effectManager );
        tpParticles.setEntity( player );
        tpParticles.particle = Particle.SMOKE_LARGE;
        tpParticles.radius = (float) 1.5;
        tpParticles.radials = 30;
        tpParticles.circles = 30;
        tpParticles.helixes = 30;
        return tpParticles;
      }

      case LUX: {
        DonutEffect tpParticles = new DonutEffect( plugin.effectManager );
        tpParticles.setEntity( player );
        tpParticles.radiusDonut = (float) 1.8;
        tpParticles.particle = Particle.TOTEM;
        return tpParticles;
      }

      case AETER: {
        TornadoEffect tpParticles = new TornadoEffect( plugin.effectManager );
        tpParticles.setEntity( player );
        tpParticles.tornadoHeight = (float) 2.4;
        tpParticles.maxTornadoRadius = (float) 1.5;
        tpParticles.yOffset = -1;
        tpParticles.showTornado = false;
        return tpParticles;
      }

      case AQUA: {
        FountainEffect tpParticles = new FountainEffect( plugin.effectManager );
        tpParticles.setEntity( player );
        tpParticles.radius = 2;
        tpParticles.height = 2;
        tpParticles.radiusSpout = 1;
        tpParticles.heightSpout = 2;
        return tpParticles;
      }

      case GEO: {
        EarthEffect tpParticles = new EarthEffect( plugin.effectManager );
        tpParticles.setEntity( player );
        tpParticles.radius = (float) 1.5;
        return tpParticles;
      }

      case ELECTRO: {
        StarEffect tpParticles = new StarEffect( plugin.effectManager );
        tpParticles.setEntity( player );
        tpParticles.innerRadius = (float) 0.5;
        tpParticles.spikeHeight = (float) 2;
        tpParticles.particle = Particle.SPELL_WITCH;
        tpParticles.color = Color.PURPLE;
        return tpParticles;
      }

      default: {
        TornadoEffect tpParticles = new TornadoEffect( plugin.effectManager );
        tpParticles.setEntity( player );
        tpParticles.tornadoHeight = (float) 2.4;
        tpParticles.maxTornadoRadius = (float) 1.5;
        tpParticles.yOffset = -2;
        tpParticles.showCloud = false;
        return tpParticles;
      }
    }
  }
}
