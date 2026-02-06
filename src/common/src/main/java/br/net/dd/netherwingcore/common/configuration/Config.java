package br.net.dd.netherwingcore.common.configuration;

/**
 * This class represents a singleton Config responsible for managing the application's {@link Configuration}.
 *
 * <p>The {@link Config} class ensures that the application's configuration is loaded and accessible globally
 * during runtime. It follows the Singleton design pattern to ensure a single instance of the config exists
 * throughout the application lifecycle.</p>
 *
 * <p>Usage guide:</p>
 * <ul>
 *   <li>Prior to calling {@link #get()}, initialize the config using {@link #loadConfig(ConfigurationSample)}.</li>
 *   <li>Attempting to access the configuration before initialization will result in an {@link IllegalStateException}.</li>
 * </ul>
 */
public class Config {

    private static Configuration configuration;

    private static Config instance;

    /**
     * Private constructor to prevent direct instantiation of the class.
     */
    private Config() {
        configuration = new Configuration();
    }

    /**
     * Loads the application's configuration into the config using the specified {@link ConfigurationSample}.
     * If the config instance does not already exist, it will be created and initialized during this method call.
     *
     * @param sample The configuration sample used to initialize the configuration.
     */
    public static void loadConfig(ConfigurationSample sample) {
        if (instance == null) {
            instance = new Config();
        }
        ConfigurationController controller = new ConfigurationController(sample);
        configuration = controller.getConfiguration();
    }

    /**
     * Retrieves the cached {@link Configuration}.
     *
     * <p>Ensure that {@link #loadConfig(ConfigurationSample)} is invoked before calling this method,
     * otherwise an {@link IllegalStateException} will be thrown.</p>
     *
     * @return The application's {@link Configuration} object.
     * @throws IllegalStateException If the config has not been initialized via {@link #loadConfig(ConfigurationSample)}.
     */
    public static Integer get(String key, Integer defaultValue) {
        if (instance == null) {
            throw new IllegalStateException("Cache not initialized. Call loadConfig() first.");
        }
        return configuration.get(key, defaultValue);
    }

    /**
     * Retrieves the cached {@link Configuration}.
     *
     * <p>Ensure that {@link #loadConfig(ConfigurationSample)} is invoked before calling this method,
     * otherwise an {@link IllegalStateException} will be thrown.</p>
     *
     * @return The application's {@link Configuration} object.
     * @throws IllegalStateException If the config has not been initialized via {@link #loadConfig(ConfigurationSample)}.
     */
    public static String get(String key, String defaultValue) {
        if (instance == null) {
            throw new IllegalStateException("Cache not initialized. Call loadConfig() first.");
        }
        return configuration.get(key, defaultValue);
    }

    /**
     * Retrieves the cached {@link Configuration}.
     *
     * <p>Ensure that {@link #loadConfig(ConfigurationSample)} is invoked before calling this method,
     * otherwise an {@link IllegalStateException} will be thrown.</p>
     *
     * @return The application's {@link Configuration} object.
     * @throws IllegalStateException If the config has not been initialized via {@link #loadConfig(ConfigurationSample)}.
     */
    public static Configuration get() {
        if (instance == null) {
            throw new IllegalStateException("Cache not initialized. Call loadConfig() first.");
        }
        return configuration;
    }

}
