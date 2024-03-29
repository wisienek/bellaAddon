package net.woolf.bella;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import net.luckperms.api.LuckPerms;
import net.woolf.bella.events.ArmourEquipEventListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.slikey.effectlib.EffectManager;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.commands.CommandManager;
//import net.woolf.bella.events.ArmorListener;
import net.woolf.bella.events.BackpackEvents;
import net.woolf.bella.events.BellaEvents;
import net.woolf.bella.utils.DbUtils;
import net.woolf.bella.utils.MoneyUtils;
import net.woolf.bella.utils.PlayerUtils;

@SuppressWarnings( "unused" )
public class Main extends JavaPlugin {

  public static final String prefixError = ChatColor.DARK_RED + "[" + ChatColor.RED + "ERROR" + ChatColor.DARK_RED +
      "] " + ChatColor.GRAY;
  public static final String prefixInfo =
      ChatColor.GRAY + "[" + ChatColor.YELLOW + "INFO" + ChatColor.GRAY + "] " + ChatColor.WHITE;

  public final Logger logger = this.getLogger();
  public final Server server = getServer();
  public final EffectManager effectManager = new EffectManager(this);

  public final Utils utils = new Utils(this);
  public final PlayerUtils putils = new PlayerUtils(this);
  public final MoneyUtils mutils = new MoneyUtils(this);
  public LuckPerms lpApi;

  public final Bot bot = new Bot(this);

  // TODO: move to FileManager
  private final File file = new File(getDataFolder(), "tpInfo.yml");
  private final File filelvl = new File(getDataFolder(), "tpLevels.yml");
  private final File fileMoney = new File(getDataFolder(), "money.yml");
  private final File fileEmoji = new File(getDataFolder(), "emoji.yml");
  private final File filePlayerConfig = new File(getDataFolder(), "playerConfig.yml");

  public YamlConfiguration tps = YamlConfiguration.loadConfiguration(file);
  public YamlConfiguration tpl = YamlConfiguration.loadConfiguration(filelvl);
  public YamlConfiguration moneyConfig = YamlConfiguration.loadConfiguration(fileMoney);
  public YamlConfiguration emojiConfig = YamlConfiguration.loadConfiguration(fileEmoji);
  public FileConfiguration config = getConfig();
  public YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(filePlayerConfig);

  @Override
  public void onEnable () {
    PluginManager pm = Bukkit.getServer().getPluginManager();
    pm.registerEvents(new BellaEvents(this), this);
    pm.registerEvents(new BackpackEvents(), this);
    pm.registerEvents(new ArmourEquipEventListener(), this);

    CommandManager.getInstance().initCommands(this);

    RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    if ( provider != null ) {
      this.lpApi = provider.getProvider();
    }
    else {
      this.logger.info("No LP API!");
    }

    try {
      DbUtils.getInstance();
    } catch ( SQLException | IOException e1 ) {
      logger.info("Error while loading DB instance");
      e1.printStackTrace();
    }
  }

  @Override
  public void onDisable () {
    this.effectManager.dispose();
    this.bot.api.shutdownNow();
  }

  private void handleConfigs () {
    // otp conf
    config.addDefault("OTP-command-delay", true);
    config.addDefault("setOTP-command-delay", true);
    config.addDefault("OTP-time-delay", 30);
    config.addDefault("setOTP-time-delay", 30);
    config.addDefault("show-setOTP-message", false);
    config.addDefault("setOTP-message", "&7Teleportowano na miejsce!");
    // atp conf
    for ( int i = 0; i <= 5; i++ ) {
      config.addDefault("tp-level-" + i + "-cld", String.valueOf(30 / ( i + 1 )));
      config.addDefault("tp-level-" + i + "-radius", String.valueOf(i * 400));
      config.addDefault("tp-level-" + i + "-maxp", String.valueOf(i));
      config.addDefault("tp-level-" + i + "-maxpoints",
                        String.valueOf((int) ( 0 + Math.floor(( 3 * i ) / ( i * 0.5 )) )));
      config.addDefault("tp-level-" + i + "-setmaxuse", String.valueOf(i * 15));
    }

    saveDefaultConfig();
    config.options().copyDefaults(true);

    try {
      File cfile = new File(getDataFolder() + File.separator + "config.yml");
      if ( !cfile.exists() ) cfile.createNewFile();

      config.save(getDataFolder() + File.separator + "config.yml");
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    if ( !file.exists() ) saveOTPFile();
    if ( !filelvl.exists() ) saveTPLFile();
    if ( !fileEmoji.exists() ) saveEmojiFile();
    if ( !filePlayerConfig.exists() ) savePlayerConfig();
  }

  private void saveEmojiFile () {
    try {
      emojiConfig.save(fileEmoji);
    } catch ( IOException e ) {
      getLogger().info("Could not save emoji file.\nHere is the stack trace:");
      e.printStackTrace();
    }
  }

  public void saveOTPFile () {
    try {
      tps.save(file);
    } catch ( IOException e ) {
      getLogger().info("Could not save tps file.\nHere is the stack trace:");
      e.printStackTrace();
    }
  }

  public void saveTPLFile () {
    try {
      tpl.save(filelvl);
    } catch ( IOException e ) {
      getLogger().info("Could not save tpl file.\nHere is the stack trace:");
      e.printStackTrace();
    }
  }

  public void saveMoneyConfig () {
    try {
      moneyConfig.save(fileMoney);
    } catch ( IOException e ) {
      getLogger().info("Could not save moeny file.\nHere is the stack trace:");
      e.printStackTrace();
    }
  }

  public void savePlayerConfig () {
    try {
      playerConfig.save(filePlayerConfig);
    } catch ( IOException e ) {
      getLogger().info("Could not save playerConfig file.\nHere is the stack trace:");
      e.printStackTrace();
    }
  }

  public static Main getInstance () {
    return (Main) Bukkit.getPluginManager().getPlugin("BellaAddon");
  }

  public boolean isTest () {
    return Main.getInstance().server.getMotd().equals("test");
  }
}
