package net.woolf.bella.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.woolf.bella.Main;

public class jazdaCommand implements CommandExecutor {

	private Main plugin;

	public jazdaCommand(Main main) {
		this.plugin = main;
		plugin.getCommand("jazda").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length == 0) {
				sender.sendMessage(Main.prefixError + getUsage());
				return true;
			}

			Player player = (Player) sender;

			if (args[0].equals("on") || args[0].equals("off")) {
				Boolean check = args[0].equals("on") ? true : false;

				plugin.putils.toggleJazda(player, check);
				player.sendMessage(Main.prefixInfo + "Ustawiono jazdę na: " + check);
			} else {
				player.sendMessage(Main.prefixError + getUsage());
				return true;
			}

			return true;
		} else {
			sender.sendMessage("Komenda tylko dla graczy!");
			return true;
		}
	}

	private String getUsage() {
		return "/jazda <on/off> - włącza lub wyłącza możliwość jazdy na tobie";
	}

}
