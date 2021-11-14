package net.woolf.bella.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.woolf.bella.Main;

public class Bot {
	
	public Main plugin;
	
	public JDA api;
	public CommandListUpdateAction commands;
	
	public Bot(Main main) {
		this.plugin = main;
		
		File passwd = new File( plugin.getDataFolder(), "pwd.txt" );
		if( passwd.exists() ) {
			try {
				Scanner myReader = new Scanner(passwd);
				String pwd = myReader.nextLine();
				myReader.close();
				
				if( pwd == null || pwd.isEmpty() || pwd.length() == 0 )
					throw new FileNotFoundException("pwd isblank");
				
				final Set<GatewayIntent> intents = new HashSet<GatewayIntent>( 
						Arrays.asList(
								GatewayIntent.GUILD_MEMBERS, 
								GatewayIntent.GUILD_MESSAGE_REACTIONS, 
								GatewayIntent.GUILD_MESSAGES
							) 
						);
				
				JDABuilder builder = JDABuilder
						.createDefault(pwd)
						.enableIntents( intents )
						.setActivity( Activity.watching("Online!") );
				
				MessageListener listener = new MessageListener(this);
				builder.addEventListeners( listener );
					
				api = builder.build().awaitReady();
				
				plugin.logger.info( "Zalogowano bota!" );
				
				setupCommands();
			} catch (FileNotFoundException | LoginException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setupCommands() {
		CommandListUpdateAction commands = api.updateCommands();
		
        commands.addCommands(
                new CommandData("who", "Listuje wszystkich aktywnych użytkowników")
                    .addOptions(
                    		new OptionData(OptionType.BOOLEAN, "ekipa", "czy wyświetlić też ekipę").setRequired(true)
                    )
            );
		
        commands.queue();
        
        plugin.logger.info( "Zarejestrowano komendy!" );
	}

	public void updatePresence( String msg ) {
		api.getPresence().setActivity( Activity.watching(msg) );
	}
	
	public void moneyLog( String msg ) {
		String logsID = "885517500261998633";
		
		TextChannel channel = api.getTextChannelById(logsID);
		if( channel == null ) {
			plugin.logger.info( "Nie można było rozwiązać kanału moneylog: " + logsID );
			return;
		}
		
		channel.sendMessage( msg );
	}
	
		
	
}
