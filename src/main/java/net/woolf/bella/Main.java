package net.woolf.bella;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import de.slikey.effectlib.EffectManager;
import net.woolf.bella.Utils;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.commands.CommandManager;
import net.woolf.bella.events.BellaEvents;
import net.woolf.bella.utils.MoneyUtils;
import net.woolf.bella.utils.PlayerUtils;


@SuppressWarnings("unused")
public class Main extends JavaPlugin {
	
    public static final String prefixError = ChatColor.DARK_RED + "[" + ChatColor.RED + "ERROR" + ChatColor.DARK_RED + "] " + ChatColor.GRAY;
    public static final String prefixInfo  = ChatColor.GRAY + "[" + ChatColor.YELLOW + "INFO" + ChatColor.GRAY + "] " + ChatColor.WHITE;
	
	public final Logger logger = this.getLogger();
	public final Server server = getServer();
	public final EffectManager effectManager = new EffectManager(this);
	
    public final Utils utils = new Utils(this);
    public final PlayerUtils putils = new PlayerUtils(this);
    public final MoneyUtils mutils = new MoneyUtils(this);
    
    public final Bot bot = new Bot(this);

	private File file = new File(getDataFolder(), "tpInfo.yml");
	private File filelvl = new File(getDataFolder(), "tpLevels.yml");
	private File fileMoney = new File(getDataFolder(), "money.yml");
	private File fileEmoji = new File(getDataFolder(), "emoji.yml");
	private File filePlayerConfig = new File(getDataFolder(), "playerConfig.yml");
	
	public YamlConfiguration tps = YamlConfiguration.loadConfiguration(file);
	public YamlConfiguration tpl = YamlConfiguration.loadConfiguration(filelvl);
	public YamlConfiguration moneyConfig = YamlConfiguration.loadConfiguration(fileMoney);
	public YamlConfiguration emojiConfig = YamlConfiguration.loadConfiguration(fileEmoji);
    public FileConfiguration config = getConfig();
    public YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(filePlayerConfig);
    
	@Override
	public void onEnable() {
	    PluginManager pm = Bukkit.getServer().getPluginManager();
	    pm.registerEvents(new BellaEvents(this), this);
	    
	    CommandManager.getInstance().initCommands(this);

		// otp conf
		config.addDefault("OTP-command-delay", true);
		config.addDefault("setOTP-command-delay", true);
		config.addDefault("OTP-time-delay", 30);
		config.addDefault("setOTP-time-delay", 30);
		config.addDefault("show-setOTP-message", false);
		config.addDefault("setOTP-message", "&7Teleportowano na miejsce!");
		// atp conf
		for( int i=0; i <= 5; i++ ) {
			config.addDefault("tp-level-"+i+"-cld", 		String.valueOf( (int)  ( 30/(i+1) ) 						));
			config.addDefault("tp-level-"+i+"-radius", 		String.valueOf( (int)  ( i*400 )  							));
			config.addDefault("tp-level-"+i+"-maxp",   		String.valueOf( (int)  ( i*1 )  							));
			config.addDefault("tp-level-"+i+"-maxpoints", 	String.valueOf( (int)  ( 0 + Math.floor( (3*i) / (i*0.5) ))	));
			config.addDefault("tp-level-"+i+"-setmaxuse", 	String.valueOf( (int)  ( i * 15 )							));
		}
		
		saveDefaultConfig();
        config.options().copyDefaults(true);

        try {
        	File cfile = new File( getDataFolder() + File.separator + "config.yml" );
        	if( !cfile.exists() )
        		cfile.createNewFile();
        	
            config.save( getDataFolder() + File.separator + "config.yml" );
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        if(!file.exists()) saveOTPFile();
        if(!filelvl.exists()) saveTPLFile();
        if(!fileEmoji.exists()) saveEmojiFile();
        if(!filePlayerConfig.exists()) savePlayerConfig();
        
	}

	@Override
    public void onDisable() {
        this.effectManager.dispose();
        this.bot.api.shutdownNow();
    }
	
    private void saveEmojiFile() {
		try {
			emojiConfig.save(fileEmoji);
		} catch (IOException e) {
            getLogger().info("Could not save emoji file.\nHere is the stack trace:");
            e.printStackTrace();
        }
	}
    
    public void saveOTPFile() {
        try {
            tps.save(file);
        } catch (IOException e) {
            getLogger().info("Could not save tps file.\nHere is the stack trace:");
            e.printStackTrace();
        }
    }
    public void saveTPLFile() {
    	try {
    		tpl.save(filelvl);
    	} catch (IOException e) {
            getLogger().info("Could not save tpl file.\nHere is the stack trace:");
            e.printStackTrace();
        }
    }
    public void saveMoneyConfig() {
    	try {
    		moneyConfig.save(fileMoney);
    	} catch (IOException e) {
            getLogger().info("Could not save moeny file.\nHere is the stack trace:");
            e.printStackTrace();
        }
    }
	public void savePlayerConfig() {
		try {
			playerConfig.save(filePlayerConfig);
		} catch (IOException e) {
            getLogger().info("Could not save playerConfig file.\nHere is the stack trace:");
            e.printStackTrace();
        }
	}
	
	public static Main getInstance() {
		return (Main) Bukkit.getPluginManager().getPlugin("BellaAddon");
	}
}
