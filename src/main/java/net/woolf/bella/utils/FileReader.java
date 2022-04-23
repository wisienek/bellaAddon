package net.woolf.bella.utils;

import java.io.File;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import net.woolf.bella.Main;

public class FileReader {
	public static YamlConfiguration getWarpConfig() {
		File configFile = new File(
				Main.getInstance().getDataFolder() + File.separator + ".." + File.separator + "EasyWarp", "warps.yml");

		if (!configFile.exists())
			return null;

		return YamlConfiguration.loadConfiguration(configFile);
	}

	public static Location getWarp(@Nonnull String name) {
		YamlConfiguration config = FileReader.getWarpConfig();

		if (name == null || !config.contains("warps." + name))
			return null;

		String baseName = "warps." + name + ".";

		String worldName = config.getString(baseName + "world");
		Double x = config.getDouble(baseName + "x");
		Double y = config.getDouble(baseName + "y");
		Double z = config.getDouble(baseName + "z");

		return new Location(Main.getInstance().server.getWorld(worldName), x, y, z);
	}

}
