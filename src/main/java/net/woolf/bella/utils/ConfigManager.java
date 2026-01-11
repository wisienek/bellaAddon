package net.woolf.bella.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.woolf.bella.Main;

public class ConfigManager {

  private final Main plugin;
  private final Logger logger;

  private final File fileOTP;
  private final File fileLevel;
  private final File fileMoney;
  private final File fileEmoji;
  private final File filePlayerConfig;

  public final YamlConfiguration tps;
  public final YamlConfiguration tpl;
  public final YamlConfiguration moneyConfig;
  public final YamlConfiguration emojiConfig;
  public final FileConfiguration config;
  public final YamlConfiguration playerConfig;

  public ConfigManager(
      Main plugin
  ) {
    this.plugin = plugin;
    this.logger = plugin.getLogger();

    this.fileOTP = new File( plugin.getDataFolder(), "tpInfo.yml" );
    this.fileLevel = new File( plugin.getDataFolder(), "tpLevels.yml" );
    this.fileMoney = new File( plugin.getDataFolder(), "money.yml" );
    this.fileEmoji = new File( plugin.getDataFolder(), "emoji.yml" );
    this.filePlayerConfig = new File( plugin.getDataFolder(), "playerConfig.yml" );

    this.tps = YamlConfiguration.loadConfiguration( fileOTP );
    this.tpl = YamlConfiguration.loadConfiguration( fileLevel );
    this.moneyConfig = YamlConfiguration.loadConfiguration( fileMoney );
    this.emojiConfig = YamlConfiguration.loadConfiguration( fileEmoji );
    this.config = plugin.getConfig();
    this.playerConfig = YamlConfiguration.loadConfiguration( filePlayerConfig );
  }

  public void initializeConfigs() {
    if ( !plugin.getDataFolder().exists() ) {
      plugin.getDataFolder().mkdirs();
    }

    config.addDefault( "OTP-command-delay", true );
    config.addDefault( "setOTP-command-delay", true );
    config.addDefault( "OTP-time-delay", 30 );
    config.addDefault( "setOTP-time-delay", 30 );
    config.addDefault( "show-setOTP-message", false );
    config.addDefault( "setOTP-message", "&7Teleportowano na miejsce!" );

    for ( int i = 0; i <= 5; i++ ) {
      config.addDefault( "tp-level-" + i + "-cld", String.valueOf( 30 / ( i + 1 ) ) );
      config.addDefault( "tp-level-" + i + "-radius", String.valueOf( i * 400 ) );
      config.addDefault( "tp-level-" + i + "-maxp", String.valueOf( i ) );
      config.addDefault( "tp-level-" + i + "-maxpoints", String
          .valueOf( (int) ( 0 + Math.floor( ( 3 * i ) / ( i * 0.5 ) ) ) ) );
      config.addDefault( "tp-level-" + i + "-setmaxuse", String.valueOf( i * 15 ) );
    }

    config.options().copyDefaults( true );

    try {
      File cfile = new File( plugin.getDataFolder(), "config.yml" );
      if ( !cfile.exists() ) {
        cfile.createNewFile();
      }
      config.save( cfile );
    } catch ( IOException e ) {
      logger.severe( "Could not save config.yml" );
      e.printStackTrace();
    }

    if ( !fileOTP.exists() )
      saveOTPFile();
    if ( !fileLevel.exists() )
      saveTPLFile();
    if ( !fileEmoji.exists() )
      saveEmojiFile();
    if ( !filePlayerConfig.exists() )
      savePlayerConfig();
  }

  public void saveOTPFile() {
    try {
      tps.save( fileOTP );
    } catch ( IOException e ) {
      logger.severe( "Could not save tps file." );
      e.printStackTrace();
    }
  }

  public void saveTPLFile() {
    try {
      tpl.save( fileLevel );
    } catch ( IOException e ) {
      logger.severe( "Could not save tpl file." );
      e.printStackTrace();
    }
  }

  public void saveMoneyConfig() {
    try {
      moneyConfig.save( fileMoney );
    } catch ( IOException e ) {
      logger.severe( "Could not save money file." );
      e.printStackTrace();
    }
  }

  public void saveEmojiFile() {
    try {
      emojiConfig.save( fileEmoji );
    } catch ( IOException e ) {
      logger.severe( "Could not save emoji file." );
      e.printStackTrace();
    }
  }

  public void savePlayerConfig() {
    try {
      playerConfig.save( filePlayerConfig );
    } catch ( IOException e ) {
      logger.severe( "Could not save playerConfig file." );
      e.printStackTrace();
    }
  }
}
