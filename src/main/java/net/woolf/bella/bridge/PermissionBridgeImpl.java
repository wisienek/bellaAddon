package net.woolf.bella.bridge;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.bella.bridge.api.IPermissionBridge;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.woolf.bella.Main;

public class PermissionBridgeImpl implements IPermissionBridge {

  private final Main plugin;

  public PermissionBridgeImpl(
      Main plugin
  ) {
    this.plugin = plugin;
  }

  @Override
  public boolean hasPermission(
      UUID playerUuid,
      String permission
  ) {
    if ( plugin.lpApi == null )
      return false;
    User user = plugin.lpApi.getUserManager().getUser( playerUuid );
    if ( user == null )
      user = plugin.lpApi.getUserManager().loadUser( playerUuid ).join();
    return user != null
        && user.getCachedData().getPermissionData().checkPermission( permission ).asBoolean();
  }

  @Override
  public String getPrimaryGroup(
      UUID playerUuid
  ) {
    if ( plugin.lpApi == null )
      return "";
    User user = plugin.lpApi.getUserManager().getUser( playerUuid );
    if ( user == null )
      user = plugin.lpApi.getUserManager().loadUser( playerUuid ).join();
    if ( user == null )
      return "";
    return plugin.lpApi.getGroupManager().getGroup( user.getPrimaryGroup() ) != null
        ? user.getPrimaryGroup()
        : "";
  }

  @Override
  public List<String> getGroups(
      UUID playerUuid
  ) {
    if ( plugin.lpApi == null )
      return Collections.emptyList();
    User user = plugin.lpApi.getUserManager().getUser( playerUuid );
    if ( user == null )
      user = plugin.lpApi.getUserManager().loadUser( playerUuid ).join();
    if ( user == null )
      return Collections.emptyList();
    return user.getNodes()
        .stream()
        .filter( NodeType.INHERITANCE::matches )
        .map( NodeType.INHERITANCE::cast )
        .map( InheritanceNode::getGroupName )
        .collect( Collectors.toList() );
  }
}
