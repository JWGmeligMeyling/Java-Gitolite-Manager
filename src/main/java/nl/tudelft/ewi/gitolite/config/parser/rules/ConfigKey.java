package nl.tudelft.ewi.gitolite.config.parser.rules;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import nl.tudelft.ewi.gitolite.config.objects.Rule;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * Configuration files may be extended with configuration keys if this is enabled in the {@code .gitolite.rc} file.
 * See <a href="http://gitolite.com/gitolite/rc.html">http://gitolite.com/gitolite/rc.html</a>.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@EqualsAndHashCode
public class ConfigKey implements Rule {

	private final static Pattern UNSAFE_PATT = Pattern.compile("[`~#\\$\\&()|<>]");

	@Getter
	protected final String key;

	@Getter
	protected final String value;

	/**
	 * Create a new {@code ConfigKey}.
	 *
	 * @param key
	 * @param value
	 */
	public ConfigKey(String key, String value) {
		if(UNSAFE_PATT.matcher(value).find()) {
			throw new IllegalArgumentException(String.format("Value contains unsafe characters!"));
		}
		this.key = key;
		this.value = value;

	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(toString());
		writer.write('\n');
	}

	@Override
	public String toString() {
		return String.format("config %s = \"%s\"", key, value);
	}

}
