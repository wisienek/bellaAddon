package net.woolf.bella.bot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
//import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.woolf.bella.Main;

public class Bot {
	public Main plugin;
	
	public JDA api;
	public CommandListUpdateAction commands;
	
	public Bot(Main main) {
		this.plugin = main;
		
		File passwd = new File( plugin.getDataFolder(), "pwd.txt" );
		
		if( !passwd.exists() ) {
			plugin.logger.info( "Nie udało się wczytać hasła do bota!" );
			return;
		}
		
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
	
	private void setupCommands() {
		CommandListUpdateAction commands = api.updateCommands();
		
		ArrayList<OptionData> userCollection = new ArrayList<OptionData>();
    	userCollection.add(new OptionData(OptionType.STRING, "user", "Nick gracza którego chcesz sprawdzić", false));
    	userCollection.add(new OptionData(OptionType.USER, "dcuser", "Oznaczony gracz (Musi mieć podpięte konto)", false));
    	
    	ArrayList<OptionData> moneyInfo = new ArrayList<OptionData>();
    	moneyInfo.add(new OptionData(OptionType.INTEGER, "ile", "Ile kasy dodać graczowi", true));
    	moneyInfo.add(
    		new OptionData(OptionType.STRING, "typ", "Typ pieniążka", true)
	    		.addChoices(
	    			new Choice("Miedziak", "miedziak"),
	    			new Choice("Srebrnik", "srebrnik"),
	    			new Choice("Złotnik", "złotnik")
	    		)
    	);
    	
    	List<OptionData> moneyAndUsers = Stream.concat(userCollection.stream(), moneyInfo.stream())
                .distinct()
                .collect(Collectors.toList());

		SubcommandData sprawdz = new SubcommandData("sprawdz", "Sprawdza stan gotówki").addOptions(userCollection);
		SubcommandData dodaj = new SubcommandData("dodaj", "Dodaje kasę dla gracza").addOptions(moneyAndUsers);
		SubcommandData zabierz = new SubcommandData("zabierz", "Zabiera kasę od gracza").addOptions(moneyAndUsers);
		
		
        commands.addCommands(
        	new CommandData("who", "Listuje wszystkich aktywnych użytkowników")
        		.addOptions(
        			new OptionData(OptionType.BOOLEAN, "ekipa", "czy wyświetlić też ekipę").setRequired(true)
                ),
        		
        	new CommandData("link", "Dodaje konto serwerowe do discorda")
        		.addOption(OptionType.STRING, "code", "Kod wygenerowany w grze", true),
        		
        	new CommandData("portfel", "Komendy portfelowe admina")
	        	.addSubcommands(
	        		sprawdz,
	        		dodaj,
	        		zabierz
	        	),
	        	
	        new CommandData("bank", "Komendy bankowe admina")
        		.addSubcommands(
	        		sprawdz,
	        		dodaj,
	        		zabierz
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
		
		
//		plugin.logger.info( "Wysyłam moneyLog: " + msg );
		channel.sendMessage( msg ).complete(); //Message returnedMessage =
//		
////		if(returnedMessage != null)
////			plugin.logger.info( "Msg id: " + returnedMessage.getId() );
	}
}
