package net.woolf.bella.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import net.woolf.bella.Main;

public class FileReader {

  private static final File WarpConfigFile = new File(
      Main.getInstance().getDataFolder() + File.separator + ".." + File.separator + "EasyWarp", "warps.yml");

  private static final File DBConfigFile = new File(Main.getInstance().getDataFolder(), "database.yml");

  public static YamlConfiguration getWarpConfig () {
    if ( !WarpConfigFile.exists() ) return null;

    return YamlConfiguration.loadConfiguration(WarpConfigFile);
  }

  public static Location getWarp (
      @Nonnull String name
  ) {
    YamlConfiguration config = FileReader.getWarpConfig();

    assert config != null;
    if ( !config.contains("warps." + name) ) return null;

    String baseName = "warps." + name + ".";

    String worldName = config.getString(baseName + "world");
    double x = config.getDouble(baseName + "x");
    double y = config.getDouble(baseName + "y");
    double z = config.getDouble(baseName + "z");

    return new Location(Main.getInstance().server.getWorld(worldName), x, y, z);
  }

  public static Map<String, String> getDBConfig () throws IOException {
    Map<String, String> config = new HashMap<>();

    if ( !DBConfigFile.exists() ) {
      FileReader.saveDBConfig();
      return config;
    }

    YamlConfiguration dbconfig = YamlConfiguration.loadConfiguration(DBConfigFile);

    config.put("BaseURL", dbconfig.getString("BaseURL"));
    config.put("Host", dbconfig.getString("Host"));
    config.put("Port", dbconfig.getString("Port"));
    config.put("Database", dbconfig.getString("Database"));
    config.put("User", dbconfig.getString("User"));
    config.put("Password", dbconfig.getString("Password"));

    return config;
  }

  public static void saveDBConfig () throws IOException {
    YamlConfiguration dbconfig = new YamlConfiguration();
    dbconfig.addDefault("BaseURL", "jdbc:mysql://");
    dbconfig.addDefault("Host", "sql.pukawka.pl");
    dbconfig.addDefault("Port", "3306");
    dbconfig.addDefault("Database", "");
    dbconfig.addDefault("User", "");
    dbconfig.addDefault("Password", "");

    dbconfig.save(DBConfigFile);
  }
}
