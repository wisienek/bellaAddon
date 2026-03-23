package net.woolf.bella.bridge;

import net.bella.bridge.BridgeRegistry;
import net.woolf.bella.Main;

public class BridgeRegistrar {

  public static void register(Main plugin) {
    BridgeRegistry.register( new EconomyBridgeImpl( plugin ) );
    BridgeRegistry.register( new PermissionBridgeImpl( plugin ) );
    plugin.logger.info( "[BellaAddon] Bridge registered successfully" );
  }
}
