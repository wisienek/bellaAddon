package net.woolf.bella.bot;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.woolf.bella.Main;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.utils.MoneyUtils;


@SuppressWarnings("unused")
public class MessageListener extends ListenerAdapter {
	
	private static final HashSet<String> AllowedGuilds = new HashSet<String>( Arrays.asList( "809181125640454194", "522449658505723905" ) );

	private Bot bot;
	private MoneyUtils mutils;
	
    public MessageListener(Bot _bot) {
		this.bot = _bot;
		this.mutils = new MoneyUtils(_bot.plugin);
	}

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
    	Guild guild = event.getGuild();
    	
    	if (guild == null || (guild != null && MessageListener.AllowedGuilds.contains(guild.getId()) == false)) {
    		event.reply( "Gildia nie jest na whiteliście!" ).queue();
    		return;
    	}
    	
    	Member gosc = event.getMember();
    	
		if( gosc.hasPermission(Permission.USE_SLASH_COMMANDS) == false ) {
			event.reply( "Nie masz permissi!" ).queue();
			return;
		}
    	
        switch ( event.getName() ) {
        	case "who" :
        		OptionMapping ekipaOpt = event.getOption("ekipa");
        		
        		Boolean ekipa = ekipaOpt != null ? ekipaOpt.getAsBoolean() : false;
        		
        		if( ekipa == null || ekipa == false ) 
        			ShowPlayers(event);
        		else 
        			ShowOPs(event);
        		
        		break;
        	case "portfel": {
        		String subCommand = event.getSubcommandName();
        		
        		if( event.getOption("user") == null && event.getOption("dcuser") == null ) {
        			event.reply("Musisz podać nick lub oznaczyć gracza!").queue();
        			return;
        		}
        		
       			String playerName = event.getOption("user").getAsString();
       			// event.getOption("dcuser").getAsUser() ????
        		
       			this.handleMoneyManip(event, subCommand, playerName, false);
        		
        		break;
        	}
        	case "bank": {
        		String subCommand = event.getSubcommandName();
        		
        		if( event.getOption("user") == null && event.getOption("dcuser") == null ) {
        			event.reply("Musisz podać nick lub oznaczyć gracza!").queue();
        			return;
        		}
        		
       			String playerName = event.getOption("user").getAsString();
       			// event.getOption("dcuser").getAsUser() ????
        		
       			this.handleMoneyManip(event, subCommand, playerName, true);
    				        		
        		break;
        	}
        	case "pogoda" : {
        		event.reply( "Pogoda wkrótce!" ).queue();
        		
        		break;
        	}
        	case "link": { 
        		event.reply("Linkowanie discorda z nickiem w grze").queue();
        	
        		break;
        	}
        }
    }
    
    private void handleMoneyManip(SlashCommandEvent event, String subCommand, String playerName, Boolean isBank ) {
    	String uuid = bot.plugin.putils.resolveUUID(playerName);
    	
		if( uuid == null ) {
			event.reply("Nie znaleziono UUID gracza!").queue();
			return;
		}
		
    	OptionMapping ileOpt = event.getOption("ile");
		OptionMapping typOpt = event.getOption("typ");
		
		long ile = ileOpt != null ? ileOpt.getAsLong() : 0;
		String typ = typOpt != null ? typOpt.getAsString() : null;
		
		Map<String, Long> money = isBank == true ? 
				bot.plugin.mutils.getBankMoney( uuid ) 
			: 
				bot.plugin.mutils.getMoney( uuid );
			
		if( subCommand.equals("sprawdz") ) {
			event.reply(
				"Gracz " + playerName + " " +
				"Ma w " + ( isBank ? "banku" : "portfelu" ) + ": \n" + 
				"- **" + String.valueOf(money.get("miedziak")) + "** Miedziaków \n" +
				"- **" + String.valueOf(money.get("srebrnik")) + "** Srebrników \n" +
				"- **" + String.valueOf(money.get("złotnik")) + "** Złotników \n"
			).queue();
			
			return;
		} else if ( subCommand.equals("dodaj") ) {
			if( ile == 0 || ile < 0 || typ == null ) {
				event.reply("Niepoprawne dane!").queue();
				return;
			}
			
			long ileMa = money.get(typ);
			
			boolean check = isBank ? 
					bot.plugin.mutils.setBankMoney(uuid, typ, ileMa + ile)
				:
					bot.plugin.mutils.setMoney(uuid, typ, ileMa + ile);
					
			if( check == false ) {
				event.reply("Coś poszło nie tak!").queue();
				return;
			}
			
			event.reply("Dodano **" + ile + "** " + typ + " do " + ( isBank ? "banku" : "portfelu" ) + " gracza " + playerName).queue();;
			return;
		} else if ( subCommand.equals("zabierz") ) {
			if( ile == 0 || ile < 0 || typ == null ) {
				event.reply("Niepoprawne dane!").queue();
				return;
			}
			
			long ileMa = money.get(typ);
			
			if( ileMa - ile < 0 ) {
				event.reply("Nie można wykonać operacji, po zabraniu będzie miał w banku mniej niż minimalna kwota!").queue();
				return;
			}
			
			boolean check = isBank ? 
					bot.plugin.mutils.setBankMoney(uuid, typ, ileMa - ile)
				:
					bot.plugin.mutils.setMoney(uuid, typ, ileMa - ile);
			
			if( check == false ) {
				event.reply("Coś poszło nie tak!").queue();
				return;
			}
			
			event.reply("Zabrano **" + ile + "** " + typ + " z " + ( isBank ? "banku" : "portfelu" ) + " gracza " + playerName).queue();;
			return;
		}
		
		event.reply( "Nieobsłużona komenda..." ).queue();
		return;
    }
	
	private void ShowPlayers(SlashCommandEvent event) {
    	List<Player> online = bot.plugin.utils.getPlayers();
    	
    	StringBuilder os = new StringBuilder();
    	os.append("Gracze online ("+ online.size() +" / "+ bot.plugin.server.getMaxPlayers() +"):");
    	for( Player player : online )
    		os.append( "\n- `" + player.getName() + "`" );
    	
    	event.reply( os.toString() ).queue();	
	}

	private void ShowOPs(SlashCommandEvent event) {
		List<Player> online = bot.plugin.utils.getPlayers().stream().filter( p -> p.isOp() ).collect( Collectors.toList() );
    	
    	StringBuilder os = new StringBuilder();
    	os.append("Ekipa online ("+ online.size() +" / "+ bot.plugin.server.getMaxPlayers() +"):");
    	for( Player player : online )
    		os.append( "\n- `" + player.getName() + "`" );
    	
    	event.reply( os.toString() ).queue();
	}       
    
}