package Types;

import java.io.File;

public enum ConfigFiles {

	// DB config
	DB("database.yml"),
	// WARPS config
	WARPS(File.separator + ".." + File.separator + "EasyWarp" + File.separator + "warps.yml");

	private final String text;

	ConfigFiles(
			final String text
	) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
