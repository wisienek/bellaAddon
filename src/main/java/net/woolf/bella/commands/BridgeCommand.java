package net.woolf.bella.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.bella.bridge.BridgeRegistry;
import net.woolf.bella.Main;

public class BridgeCommand implements CommandExecutor {

  private final Main plugin;

  public BridgeCommand(Main main) {
    this.plugin = main;
    plugin.getCommand( "bridge" ).setExecutor( this );
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if ( BridgeRegistry.isReady() ) {
      sender.sendMessage( Main.prefixInfo + "Bridge is registered. Economy and Permissions are available for the mod." );
      sender.sendMessage( ChatColor.GRAY + "In-game (with the mod): run /bridgetest to see your purse balance via the bridge." );
    } else {
      sender.sendMessage( Main.prefixError + "Bridge is NOT ready. Check that both Economy and Permissions were registered in onEnable." );
    }
    return true;
  }
}
