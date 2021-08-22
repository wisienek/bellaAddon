package net.woolf.bella;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

import de.slikey.effectlib.EffectManager;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.commands.atpCommand;
import net.woolf.bella.commands.oocCommand;
import net.woolf.bella.commands.otpCommand;
import net.woolf.bella.events.BellaEvents;

@SuppressWarnings("unused")
public class Main extends JavaPlugin {
	
	public final Logger logger = this.getLogger();
	public final Server server = getServer();
	public final EffectManager effectManager = new EffectManager(this);
    public final Utils utils = new Utils(this);
    public final Bot bot = new Bot(this);


    public static final String prefixError = ChatColor.DARK_RED + "[" + ChatColor.RED + "ERROR" + ChatColor.DARK_RED + "] " + ChatColor.GRAY;
    public static final String prefixInfo  = ChatColor.GRAY + "[" + ChatColor.YELLOW + "INFO" + ChatColor.GRAY + "] " + ChatColor.WHITE;
    

	public File file = new File(getDataFolder(), "tpInfo.yml");
	public File filelvl = new File(getDataFolder(), "tpLevels.yml");
	
	public YamlConfiguration tps = YamlConfiguration.loadConfiguration(file);
	public YamlConfiguration tpl = YamlConfiguration.loadConfiguration(filelvl);
    public FileConfiguration config = getConfig();
    
    
	@Override
	public void onEnable() {
	    PluginManager pm = Bukkit.getServer().getPluginManager();
	    pm.registerEvents(new BellaEvents(this), this);
		
		new otpCommand(this);
		new atpCommand(this);
		new oocCommand(this);
		
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

        if (!file.exists()) {
            saveOTPFile();
        }
        if(!filelvl.exists()) {
        	saveTPLFile();
        }
	}
	
    @Override
    public void onDisable() {
        effectManager.dispose();
        bot.api.disconnect();
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
	

}
