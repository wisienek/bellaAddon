package net.woolf.bella.utils;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;

import net.woolf.bella.Main;

public class ChatUtils {
	public static final String LocalPrefix = "**[L]**";
	public static final String RangePrefix = "**[R]**";
	public static final String GlobalPrefix = "**[G]**";
	public static final String OOCPrefix = "**[OOC]**";
	public static final String WhisperPrefix = "**[S]**";
	public static final String DCNarrationPrefix = "**[DC]**";
	
	private static String cachedMessage = "";
	private static Timer timer;
	
	private static Main plugin;

	public ChatUtils(Main main) {
		ChatUtils.plugin = main;
	}
	
	@SuppressWarnings("deprecation")
	public static void cacheMessageForChatLog( 
		@Nullable String message
	) {
		if( message != null ) {
			Date today = new Date();
			int hours = today.getHours();
			int minutes =  today.getMinutes();
			String hourFormat = 
					"[" + ( hours < 10 ? "0" + String.valueOf(hours) : hours ) + 
					":" + 
					( minutes < 10 ? "0" + String.valueOf(minutes) : minutes ) + "] ";
			
			ChatUtils.cachedMessage = ChatUtils.cachedMessage.concat( 
				( ChatUtils.cachedMessage.length() > 0 ? "\n" : "" ) + 
				hourFormat + 
				message
					.replaceAll("§.", "")
					.replaceAll("@(here|everyone)", "")
					.replaceAll("<@\\d{0,24}>", "")
			);

			if( ChatUtils.timer == null ) {
				ChatUtils.timer = new Timer();
				ChatUtils.timer.schedule(new TimerTask() {
				    @Override
				    public void run() {
				    	ChatUtils.timer = null;
				    	ChatUtils.cacheMessageForChatLog( null );
				    }
				}, 120 * 1000L);
			}
		} else {
			int length = ChatUtils.cachedMessage.length();
			
			if( length > 0 ) {
				String messageToSend = ChatUtils.cachedMessage.substring( 0, length >= 2048 ? 2048 : length );
				ChatUtils.cachedMessage = ChatUtils.cachedMessage.substring( length >= 2048 ? 2048 : length );
				
				Main.getInstance().bot.chatLog( messageToSend );
			}
		}
	}
	
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
