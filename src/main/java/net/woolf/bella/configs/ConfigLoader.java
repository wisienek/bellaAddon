package net.woolf.bella.configs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import Types.ConfigFiles;
import net.woolf.bella.Main;

public class ConfigLoader {

	private final File baseConfigFile = Main.getInstance().getDataFolder();

	private final Map<String, YamlConfiguration> configs = new HashMap<String, YamlConfiguration>();

	ConfigLoader() {
		readConfigDirectory();

		try {
			saveDefaults();
		} catch ( IOException e ) {
			Main.getInstance().logger.info( "Error while saving default configs!" );
			e.printStackTrace();
		}
	}

	private void readConfigDirectory() {

		for ( final File inFolder : baseConfigFile.listFiles() ) {
			if ( isYamlFile( inFolder.getName() ) ) {
				YamlConfiguration config = YamlConfiguration.loadConfiguration( inFolder );

				if ( config != null )
					configs.put( inFolder.getName(), config );
			}

		}
	}

	private void saveDefaults() throws IOException {
		if ( configs.containsKey( ConfigFiles.DB.toString() ) == false )
			getDefaultDBConfig().save( new File( baseConfigFile, ConfigFiles.DB.toString() ) );

	}

	private YamlConfiguration getDefaultDBConfig() {
		YamlConfiguration prodDB = new YamlConfiguration();
		prodDB.addDefault( "BaseURL", "jdbc:mysql://" );
		prodDB.addDefault( "Host", "sql.pukawka.pl" );
		prodDB.addDefault( "Port", "3306" );
		prodDB.addDefault( "Database", "" );
		prodDB.addDefault( "User", "" );
		prodDB.addDefault( "Password", "" );

		YamlConfiguration stageDB = new YamlConfiguration();
		stageDB.addDefault( "BaseURL", "jdbc:mysql://" );
		stageDB.addDefault( "Host", "sql.pukawka.pl" );
		stageDB.addDefault( "Port", "3306" );
		stageDB.addDefault( "Database", "" );
		stageDB.addDefault( "User", "" );
		stageDB.addDefault( "Password", "" );

		YamlConfiguration dbconfig = new YamlConfiguration();
		dbconfig.addDefault( "prod", prodDB );
		dbconfig.addDefault( "stage", stageDB );

		return dbconfig;
	}

	private boolean isYamlFile(
			String fileName
	) {
		if ( fileName == null )
			return false;
		if ( fileName.contains( "." ) == false )
			return false;

		return fileName.substring( fileName.lastIndexOf( "." ) + 1 ).equals( "yml" );
	}
}
