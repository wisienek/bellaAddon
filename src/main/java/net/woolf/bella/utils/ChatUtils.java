package net.woolf.bella.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

import net.woolf.bella.Main;

public class ChatUtils {
	
	private static Main plugin;

	public ChatUtils(Main main) {
		ChatUtils.plugin = main;
	}

	/*
	public static String formatDOaction( String message ) {
		String newMsg = "";
		
		Pattern pattern = Pattern.compile( "\\*\\*(.*?)\\*\\*" );
		Matcher matcher = pattern.matcher( message );
		Boolean found = matcher.find();
		
		while( found ) {
			
			//get text before action
			int index = message.indexOf("**");
			String first = message.substring(0, index+2).replace("**", "");
			message = message.substring(index+2);
			
			//get action text till end
			int index2 = message.indexOf("**");
			String second = (index2 == -1) ? message : message.substring(0, index2+2).replace("**", "");
			
			message = message.substring( index2 > -1 ? index2 + 2 : 0 );
			
			newMsg += first + ChatColor.YELLOW + "**" + second + "**" + ChatColor.WHITE ;
			
			found = matcher.find();
		}
		
		if( message.length() > 0 ) {
			newMsg += message;
		}
		
		
		return (newMsg.length() == 0) ? 
				message 
			: 
				newMsg;
	}
	public static String formatMEaction( String message ) {
		String newMsg = "";
		
		Pattern pattern = Pattern.compile( "\\*(.*?)\\*" );
		Matcher matcher = pattern.matcher( message );
		Boolean found = matcher.find();
		
		while( found ) {
			
			//get text before action
			int index = message.indexOf("*");
			String first = message.substring(0, index+1).replace("*", "");
			message = message.substring(index+1);
			
			//get action text till end
			int index2 = message.indexOf("*");
			String second = (index2 == -1) ? message : message.substring(0, index2+1).replace("*", "");
			message = message.substring( index2 > -1 ? index2 + 1 : 0 );
			
			newMsg += first + ChatColor.YELLOW + "*" + second + "*" + ChatColor.WHITE ;
			
			found = matcher.find();
		}
		
		if( message.length() > 0 ) {
			newMsg += message;
		}
		
		
		return (newMsg.length() == 0) ? 
				message 
			: 
				newMsg;
	}
	*/
	public static String formatOOC( String message ) {
		String newMsg = "";
		
		Pattern pattern = Pattern.compile( "\\((.*?)\\)" );
		Matcher matcher = pattern.matcher( message );
		Boolean found = matcher.find();
		
		while( found ) {
			
			//get text before action
			int index = message.indexOf("(");
			String first = message.substring(0, index+1).replace("(", "");
			message = message.substring(index+1);
			
			//get action text till end
			int index2 = message.indexOf(")");
			String second = (index2 == -1) ? message : message.substring(0, index2+1).replace(")", "");
			
			message = message.substring( index2 > -1 ? index2 + 1 : 0 );
			
			newMsg += first + ChatColor.GRAY + "(" + second + ")" + ChatColor.WHITE ;
			
			found = matcher.find();
		}
		
		if( message.length() > 0 ) {
			newMsg += message;
		}
		
		
		return (newMsg.length() == 0) ? 
				message 
			: 
				newMsg;
	}

	public static String formatEmojis( String message ) {
		String newMsg = message;
		
		Map<String, Object> mapa = plugin.emojiConfig.getValues(false);
		
		for( String emoji : mapa.keySet() ) 
			newMsg = newMsg.replaceAll( "(?i)" + escapeMetaCharacters(emoji), ChatColor.YELLOW + "*" + (String) mapa.get(emoji) + "*" + ChatColor.WHITE );
		
		return newMsg;
	}
	
	public static String escapeMetaCharacters(String inputString){
	    final String[] metaCharacters = {"\\","^","$","{","}","[","]","(",")",".","*","+","?","|","<",">","-","&","%"};

	    for (int i = 0 ; i < metaCharacters.length ; i++){
	        if(inputString.contains(metaCharacters[i])){
	            inputString = inputString.replace(metaCharacters[i],"\\"+metaCharacters[i]);
	        }
	    }
	    return inputString;
	}
	
	
	
}
