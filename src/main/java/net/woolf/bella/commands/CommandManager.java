package net.woolf.bella.commands;

import net.woolf.bella.Main;
import net.woolf.bella.utils.ChatUtils;
import net.woolf.bella.commands.AtpCommand;
import net.woolf.bella.commands.BankCommand;
import net.woolf.bella.commands.DateCommand;
import net.woolf.bella.commands.JazdaCommand;
import net.woolf.bella.commands.MoneyCommand;
import net.woolf.bella.commands.OocCommand;
import net.woolf.bella.commands.OtpCommand;
import net.woolf.bella.commands.LinkCommand;

@SuppressWarnings("unused")
public final class CommandManager {

	private static CommandManager INSTANCE;

	private CommandManager() {
	}

	public static CommandManager getInstance() {
		if ( INSTANCE == null ) {
			INSTANCE = new CommandManager();
		}

		return INSTANCE;
	}

	public void initCommands(
			Main instance
	) {
		new LinkCommand( instance );
		new OtpCommand( instance );
		new AtpCommand( instance );
		new OocCommand( instance );
		new DateCommand( instance );
		new MoneyCommand( instance );
		new BankCommand( instance );
		new ChatUtils( instance );
		new JazdaCommand( instance );
		new RnarCommand( instance );
		new ItemEnchanter( instance );
		new TestCommand();
		new PlecakCommand();
		new ListCommand();
	}
}
