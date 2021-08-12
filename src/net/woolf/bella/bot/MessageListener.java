package net.woolf.bella.bot;

import java.util.List;

import org.bukkit.entity.Player;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import net.woolf.bella.Main;

public class MessageListener implements MessageCreateListener {

	private Main plugin;
	
    public MessageListener(Main plugin2) {
		this.plugin = plugin2;
	}

	@Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("/who")) {
        	List<Player> online = plugin.utils.getPlayers();
        	
        	StringBuilder os = new StringBuilder();
        	os.append("Gracze online ("+ online.size() +" / "+ plugin.server.getMaxPlayers() +"):");
        	for( Player player : online )
        		os.append( "\n- `" + player.getName() + "`" );
        	
        	event.getMessage().reply( os.toString() );
        }
    }
	
}
