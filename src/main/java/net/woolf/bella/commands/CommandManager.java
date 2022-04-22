package net.woolf.bella.commands;

import net.woolf.bella.Main;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.commands.atpCommand;
import net.woolf.bella.commands.bankCommand;
import net.woolf.bella.commands.dateCommand;
import net.woolf.bella.commands.jazdaCommand;
import net.woolf.bella.commands.moneyCommand;
import net.woolf.bella.commands.oocCommand;
import net.woolf.bella.commands.otpCommand;

@SuppressWarnings("unused")
public final class CommandManager {

	private static CommandManager INSTANCE;
	
	private CommandManager() {}
	
	public static CommandManager getInstance() {
		if ( INSTANCE == null ) {
			INSTANCE = new CommandManager();
		}
		
		return INSTANCE;
	}
	
	public void initCommands(Main instance) {
		// teleportacja
		new otpCommand(instance);
		// teleportacja dla adminów
		new atpCommand(instance);
		// ładne ooc
		new oocCommand(instance);
		// aktualna data
		new dateCommand(instance);
		// kasa portfelowa
		new moneyCommand(instance);
		// kasa bankowa
		new bankCommand(instance);
		// utilsy chatowe
		new ChatUtils(instance);
		// jazda na graczach
		new jazdaCommand(instance);
		// narracja na range
		new rnarCommand(instance);
	}
}
