package nl.tudelft.ewi.gitolite.config.permission;

import nl.tudelft.ewi.gitolite.config.objects.Writable;

import java.io.IOException;
import java.io.Writer;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public enum BasePermission implements Writable {

	/**
	 * Allow pretty much anything -- fast-forward, rewind or delete branches or tags.
	 */
	RW_PLUS("RW+"),

	/**
	 * Allow fast-forward push of a branch, or create new branch/tag.
	 */
	RW("RW"),

	/**
	 * Allow read operations only.
	 */
	R("R"),

	/**
	 * Create repositories.
	 */
	C("C"),

	/**
	 * Deny access.
	 */
	DENY("-");


	private final String field;

	BasePermission(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}



	public static BasePermission parse(String input) {
		for(BasePermission basePermission : values()) {
			if(basePermission.field.equals(input)) {
				return basePermission;
			}
		}
		throw new IllegalArgumentException("No permission found for " + input);
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(getField());
	}

}
