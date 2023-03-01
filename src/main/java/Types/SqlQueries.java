package Types;

public enum SqlQueries {

	DELETE_ACCOUNT_CONNECTION("DELETE FROM game_dc_link WHERE uuid=? AND discordId=?;"),
	CONNECT_ACCOUNT("INSERT INTO game_dc_link(discordId, uuid, playerName) VALUES (?, ?, ?);"),
	GET_CONNECTED_ACCOUNT(
			"SELECT * FROM game_dc_link WHERE discordId=(SELECT discordId FROM game_dc_link WHERE uuid=?);"),
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
