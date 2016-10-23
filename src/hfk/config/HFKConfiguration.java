package hfk.config;

import hfk.game.InputMap;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author LostMekka
 */
public class HFKConfiguration {

	// TODO: merge with GameSettings?

	public static final String DEFAULT_CONFIG_FILE_NAME = "config.properties";

	public static HFKConfiguration fromFile() {
		return fromFile(DEFAULT_CONFIG_FILE_NAME);
	}

	public static HFKConfiguration fromFile(String filename) {
		Properties properties = new Properties();
		File f = new File(filename);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Could not find config file! Creating default file.");
			HFKConfiguration configuration = fromDefaults();
			configuration.writeToDisk(filename);
			return configuration;
		}
		try {
			properties.load(new FileReader(filename));
		} catch (IOException e) {
			System.out.println("Error parsing config file! Falling back on defaults.");
			return fromDefaults();
		}
		return new HFKConfiguration(properties);
	}

	public static HFKConfiguration fromDefaults() {
		Properties properties = new Properties();

		Stream.of(InputMap.Action.values())
				.forEach(action -> {
					String property = action.name();
					String value = action.defaultsStream()
							.map(InputMap.InputSource::getSlickInputFieldName)
							.collect(Collectors.joining(","));
					properties.put(property, value);
				});
		return new HFKConfiguration(properties);
	}

	private Properties properties;

	private HFKConfiguration(Properties properties) {
		this.properties = properties;
	}

	public boolean writeToDisk() {
		return writeToDisk(DEFAULT_CONFIG_FILE_NAME);
	}

	public boolean writeToDisk(String fileName) {
		try {
			// create comments
			Collector<CharSequence, ?, String> join = Collectors.joining(", ", "[", "]");
			String comment = "Format: <action name>=<input source name 1>[,<input source name 2>,[...]]";
			comment += "\nAvailable action names: " + InputMap.InputSource
					.getAvailableActionNames()
					.collect(join);
			comment += "\nAvailable input source names: " + InputMap.InputSource
					.getAvailableSlickInputFieldNames()
					.collect(join);
			// create temporary properties object that enumerates the desired keys in order
			Properties tmp = new Properties() {
				@Override
				public synchronized Enumeration<Object> keys() {
					return Collections.enumeration(
							InputMap.InputSource
							.getAvailableActionNames()
							.collect(Collectors.toList())
					);
				}
			};
			tmp.putAll(properties);
			tmp.store(new FileWriter(fileName), comment);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void fillInputMap(InputMap inputMap) {
		properties.stringPropertyNames().forEach(actionName -> {
			// get Action enum instance for action name
			InputMap.Action action;
			try {
				action = InputMap.Action.valueOf(actionName);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format(
						"Could not initialize input: Action for name \"%s\" not found!",
						actionName
				));
			}
			// map specified input sources to action
			try {
			Pattern.compile(",").splitAsStream(properties.getProperty(actionName))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.map(InputMap.InputSource::forFieldName)
					.forEach(inputSource -> inputMap.addInput(inputSource, action));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Could not initialize input: " + e.getMessage());
			}
		});
	}

}
