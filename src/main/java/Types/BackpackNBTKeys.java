package Types;

public enum BackpackNBTKeys {

	ISBACKPACK("isbackpack"), UUID("backpack-uuid"), ROWS("backpack-rows"),
	OPENED("backpack-opened"), ALLOW_MULTIPLE_VIEWERS("multiple-viewers");

	private final String text;

	BackpackNBTKeys(
			final String text
	) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
