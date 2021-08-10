package net.woolf.bella;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

public class Bot {
	
	public DiscordApi api;
	private Main plugin;
	
	public Bot(Main main) {
		this.plugin = main;
		
		File passwd = new File( plugin.getDataFolder(), "pwd.txt" );
		if( passwd.exists() ) {
			try {
				Scanner myReader = new Scanner(passwd);
				String pwd = myReader.nextLine();
				myReader.close();
				
				if( pwd.isBlank() || pwd.isEmpty() )
					throw new FileNotFoundException("pwd isblank");
				
				api = new DiscordApiBuilder()
				        .setToken( pwd )
				        .login().join();
				plugin.logger.info( "Zalogowano bota!" );
				updatePresence("Online!");
				
				SlashCommand.with("who", "Pokazuje listę graczy").createGlobal(api).join();
		        api.addSlashCommandCreateListener(event -> {
		            SlashCommandInteraction slashCommandInteraction = event.getSlashCommandInteraction();
		            if (slashCommandInteraction.getCommandName().equals("who")) {
		            	
		            	List<Player> online = plugin.utils.getPlayers();
		            	StringBuilder os = new StringBuilder();
		            	os.append("Gracze online ("+ online.size() +" / "+ plugin.server.getMaxPlayers() +"):");
		            	for( Player player : online )
		            		os.append( "\n- " + player.getName() );
		            	
		                slashCommandInteraction.createImmediateResponder()
		                    .setContent( os.toString() )
		                    .respond();
		            }
		        });
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updatePresence( String msg ) {
		api.updateActivity( msg );
	}
}
