package Types;

public enum SqlQueries {

  DELETE_ACCOUNT_CONNECTION("DELETE FROM game_dc_link WHERE uuid=? AND discordId=?;"),
  CONNECT_ACCOUNT("INSERT INTO game_dc_link(discordId, uuid, playerName) VALUES (?, ?, ?);"),
  GET_CONNECTED_ACCOUNT(
      "SELECT g1.discordId, g1.uuid, g1.playerName FROM game_dc_link g1 WHERE g1.discordId=(SELECT g2.discordId FROM game_dc_link g2 WHERE g2.uuid=? LIMIT 1);"),
  GET_BACKPACK("SELECT * FROM backpack WHERE uuid=?;"),
  UPDATE_BACKPACK_ITEMS("UPDATE backpack SET itemData=? WHERE uuid=?;"),
  CREATE_BACKPACK("INSERT INTO backpack(uuid, `inventoryName`) VALUES(UUID(), ?);"),
  SELECT_BACKPACK_BYNAME_LAST(
      "SELECT * FROM backpack WHERE inventoryName=? ORDER BY createdAt DESC LIMIT 1;");

  private final String text;

  SqlQueries(
      final String text
  ) {
    this.text = text;
  }

  @Override
  public String toString() {
    return text;
  }
}
