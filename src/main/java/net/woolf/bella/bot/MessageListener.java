package net.woolf.bella.bot;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.md_5.bungee.api.ChatColor;
import net.woolf.bella.Main;
import net.woolf.bella.bot.Bot;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.utils.FileReader;
import net.woolf.bella.utils.MoneyUtils;
import net.woolf.bella.utils.PlayerUtils;


@SuppressWarnings("unused")
public class MessageListener extends ListenerAdapter {
	
	private static final HashSet<String> MasterGuilds = new HashSet<String>( Arrays.asList( "809181125640454194", "522449658505723905" ) );
	private static final HashSet<String> AllowedGuilds = new HashSet<String>( Arrays.asList( "840884051174752256" ) );

	private Bot bot;
	private MoneyUtils mutils;
	
    public MessageListener(Bot _bot) {
		this.bot = _bot;
		this.mutils = new MoneyUtils(_bot.plugin);
	}

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
    	Guild guild = event.getGuild();
    	
    	if (!this.checkGuildPerms(guild, false)) {
    		event.reply( "Gildia nie jest na whiteliście!" ).queue();
    		return;
    	}
    	
    	Member gosc = event.getMember();
    	
		if( gosc.hasPermission(Permission.USE_SLASH_COMMANDS) == false ) {
			event.reply( "Nie masz permissi!" ).queue();
			return;
		}
    	
        switch ( event.getName() ) {
        	case "who": {
            	if (!this.checkGuildPerms(guild, true)) {
            		event.reply( "Gildia nie jest na whiteliście!" ).queue();
            		return;
            	}
            	
        		OptionMapping ekipaOpt = event.getOption("ekipa");
        		
        		Boolean ekipa = ekipaOpt != null ? ekipaOpt.getAsBoolean() : false;
        		
        		if( ekipa == null || ekipa == false ) 
        			ShowPlayers(event);
        		else 
        			ShowOPs(event);
        		
        		break;
        	}
        	case "portfel": {
            	if (!this.checkGuildPerms(guild, true)) {
            		event.reply( "Gildia nie jest na whiteliście!" ).queue();
            		return;
            	}
            	
        		String subCommand = event.getSubcommandName();
        		
        		if( event.getOption("user") == null && event.getOption("dcuser") == null ) {
        			event.reply("Musisz podać nick lub oznaczyć gracza!").queue();
        			return;
        		}
        		
       			String playerName = event.getOption("user").getAsString();
        		
       			this.handleMoneyManip(event, subCommand, playerName, false);
        		
        		break;
        	}
        	case "bank": {
            	if (!this.checkGuildPerms(guild, true)) {
            		event.reply( "Gildia nie jest na whiteliście!" ).queue();
            		return;
            	}
            	
        		String subCommand = event.getSubcommandName();
        		
        		if( event.getOption("user") == null && event.getOption("dcuser") == null ) {
        			event.reply("Musisz podać nick lub oznaczyć gracza!").queue();
        			return;
        		}
        		
       			String playerName = event.getOption("user").getAsString();
        		
       			this.handleMoneyManip(event, subCommand, playerName, true);
    				        		
        		break;
        	}
        	case "narracja": {
            	if (!this.checkGuildPerms(guild, true)) {
            		event.reply( "Gildia nie jest na whiteliście!" ).queue();
            		return;
            	}
            	
        		if( !MessageListener.hasRole(event.getMember(), "809423929864749086") ) {
        			event.reply("Nie posiadasz roli narratora!").queue();
        			return;
        		}
        		
        		this.handleNarration(event);
        		
        		break;
        	}
        	case "pogoda" : {
            	if (!this.checkGuildPerms(guild, true)) {
            		event.reply( "Gildia nie jest na whiteliście!" ).queue();
            		return;
            	}
            	
        		event.reply( "Pogoda wkrótce!" ).queue();
        		
        		break;
        	}
        	case "link": {
        		event.reply("Linkowanie discorda z nickiem w grze").queue();
        	
        		break;
        	}
        }
    }
    
    private void handleNarration( SlashCommandEvent event ) {
    	String subCommand = event.getSubcommandName();
    	
    	if(subCommand == null) return;
    	
    	String narrMessage = event.getOption("text").getAsString();
    	
    	String warp = event.getOption("warp") != null ? event.getOption("warp").getAsString() : null;
    	String user = event.getOption("user") != null ? event.getOption("user").getAsString() : null;
    	Long range = event.getOption("range") != null ? event.getOption("range").getAsLong() : 20;
    	
    	switch(subCommand) {
	    	case "globalna": {
	    		String message = 
	    				ChatColor.RED + "[G] " + 
	    				ChatColor.YELLOW + "[" + narrMessage + "]";
	    		
	    		String logMessage = 
	    			ChatUtils.DCNarrationPrefix + " " + 
		    		ChatUtils.GlobalPrefix + 
		    		" [" + event.getMember().getEffectiveName() + "]" + 
		    		" [" + narrMessage + "]";
	    		
	    		this.bot.plugin.server.getOnlinePlayers().stream().forEach(p -> p.sendMessage(message));
	    		ChatUtils.cacheMessageForChatLog(logMessage);
	    		
	    		event.reply("Wysłano narrację!").queue();
	    		return;
	    	}
	    	case "lokalna": {
	    		if( warp == null && user == null ) {
	    			event.reply("Nie wiadomo gdzie wysłać narrację!").queue();
	    			return;
	    		}
	    		
	    		String message = 
	    				ChatColor.RED + "[L] " + 
	    				ChatColor.YELLOW + "[" + narrMessage + "]";
	    		
	    		String logMessage =
		    			ChatUtils.DCNarrationPrefix + " " + 
		    		    ChatUtils.LocalPrefix + 
		    		    " [" + event.getMember().getEffectiveName() + "]" + 
		    		    " {" + (user != null ? user : warp ) + "} " + String.valueOf(range) +
		    		    " [" + narrMessage + "]";
	    		
	    		Location location = user != null ? this.bot.plugin.server.getPlayer(user).getLocation() : FileReader.getWarp(warp);
	    		
	    		PlayerUtils.getPlayersWithinRange(location, range)
	    			.stream()
	    			.forEach(p -> p.sendMessage(message));
	    		
	    		ChatUtils.cacheMessageForChatLog(logMessage);
	    		
	    		event.reply("Wysłano narrację!").queue();
	    		return;
	    	}
	    	case "prywatna": {
	    		if( user == null  ) {
	    			event.reply("Nie wiadomo komu wysłać narrację!").queue();
	    			return;
	    		}
	    		
	    		Player player = this.bot.plugin.server.getPlayer(user);
	    		
	    		if( player == null || player.isOnline() == false ) {
	    			event.reply("Nie znaleziono gracza lub offline!").queue();
	    			return;
	    		}
	    		
	    		
	    		String message = 
	    				ChatColor.RED + "[L] " + 
	    				ChatColor.YELLOW + "[" + narrMessage + "]";
	    		
	    		String logMessage =
		    			ChatUtils.DCNarrationPrefix + " " + 
		    		    ChatUtils.WhisperPrefix + 
		    		    " [" + event.getMember().getEffectiveName() + " -> " + user + "]" + 
		    		    " [" + narrMessage + "]";
	    		
	    		player.sendMessage(message);
	    		ChatUtils.cacheMessageForChatLog(logMessage);
	    		
	    		event.reply("Wysłano narrację!").queue();
	    		return;
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
	
	private boolean checkGuildPerms(Guild guild, boolean checkMaster) {
		if(guild == null) return false;
		
		if(checkMaster && !MessageListener.MasterGuilds.contains(guild.getId()))
			return false;
		
		if(
			!MessageListener.MasterGuilds.contains(guild.getId()) && 
    		!MessageListener.AllowedGuilds.contains(guild.getId())
    	) return false;
		
		return true;
	}
    
	private static boolean hasRole(Member member, String roleId) {
		if(member == null || roleId == null) return false;
		
		return member.getRoles().stream().filter(o -> o.getId().equals("809423929864749086")).findFirst().isPresent();
	}
}