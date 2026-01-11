package net.woolf.bella;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import net.luckperms.api.LuckPerms;
import net.woolf.bella.events.ArmorListener;
import net.woolf.bella.events.ArmourEquipEventListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.slikey.effectlib.EffectManager;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.commands.CommandManager;
import net.woolf.bella.events.BackpackEvents;
import net.woolf.bella.events.BellaEvents;
import net.woolf.bella.utils.ConfigManager;
import net.woolf.bella.utils.CooldownUtils;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.EffectUtils;
import net.woolf.bella.utils.MoneyUtils;
import net.woolf.bella.utils.PlayerUtils;
import net.woolf.bella.utils.TeleportUtils;

public class Main extends JavaPlugin {

  public static final String prefixError = ChatColor.DARK_RED + "[" + ChatColor.RED + "ERROR"
      + ChatColor.DARK_RED + "] " + ChatColor.GRAY;
  public static final String prefixInfo = ChatColor.GRAY + "[" + ChatColor.YELLOW + "INFO"
      + ChatColor.GRAY + "] " + ChatColor.WHITE;

  public final Logger logger = this.getLogger();
  public final Server server = getServer();
  public final EffectManager effectManager = new EffectManager( this );

  public final PlayerUtils putils = new PlayerUtils( this );
  public final MoneyUtils mutils = new MoneyUtils( this );
  public final ConfigManager configManager = new ConfigManager( this );
  public final CooldownUtils cooldownUtils = new CooldownUtils( this );
  public final EffectUtils effectUtils = new EffectUtils( this );
  public final TeleportUtils teleportUtils = new TeleportUtils( this, configManager, effectUtils,
      cooldownUtils );
  public LuckPerms lpApi;

  public final Bot bot = new Bot( this );

  @Deprecated
  public final Utils utils = new Utils( this );

  @Override
  public void onEnable() {
    configManager.initializeConfigs();

    PluginManager pm = Bukkit.getServer().getPluginManager();
    pm.registerEvents( new BellaEvents( this ), this );
    pm.registerEvents( new BackpackEvents(), this );
    pm.registerEvents( new ArmorListener( this ), this );
    pm.registerEvents( new ArmourEquipEventListener(), this );

    CommandManager.getInstance().initCommands( this );

    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
        .getRegistration( LuckPerms.class );
    if ( provider != null ) {
      this.lpApi = provider.getProvider();
    } else {
      this.logger.info( "No LP API!" );
    }

    try {
      DbUtils.getInstance();
    } catch ( SQLException | IOException e1 ) {
      logger.info( "Error while loading DB instance" );
      e1.printStackTrace();
    }
  }

  @Override
  public void onDisable() {
    this.effectManager.dispose();
    if ( this.bot.api != null ) {
      this.bot.api.shutdownNow();
    }
  }

  public static Main getInstance() {
    return (Main) Bukkit.getPluginManager().getPlugin( "BellaAddon" );
  }

  public boolean isTest() {
    return Main.getInstance().server.getMotd().equals( "test" );
  }
}
