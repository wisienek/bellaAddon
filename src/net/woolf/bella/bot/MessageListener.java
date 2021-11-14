package net.woolf.bella.bot;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.woolf.bella.bot.Bot;


@SuppressWarnings("unused")
public class MessageListener extends ListenerAdapter {

	private Bot bot;
	
    public MessageListener(Bot _bot) {
		this.bot = _bot;
	}

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
    	if (event.getGuild() == null)
            return;
    	
    	Member gosc = event.getMember();
    	
        // Only accept commands from guilds
        switch ( event.getName() ) {
        	case "who" :
        		if( gosc.hasPermission(Permission.USE_SLASH_COMMANDS) == false ) {
        			event.reply( "Nie masz permissi!" ).queue();
        			return;
        		}
        		
        		Boolean ekipa = event.getOption("ekipa").getAsBoolean();
        		
        		if( ekipa == null || ekipa == false ) 
        			ShowPlayers(event);
        		else 
        			ShowOPs(event);
        		
        		break;
        	case "pogoda" :
        		if( gosc.hasPermission(Permission.USE_SLASH_COMMANDS) == false ) {
        			event.reply( "Nie masz permissi!" ).queue();
        			return;
        		}
        		
        		
        		event.reply( "Pogoda command!" ).queue();
        		
        		break;
	        default:
	            event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
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