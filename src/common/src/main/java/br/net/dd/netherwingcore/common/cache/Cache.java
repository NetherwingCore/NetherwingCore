package br.net.dd.netherwingcore.common.cache;

import br.net.dd.netherwingcore.common.configuration.Configuration;
import br.net.dd.netherwingcore.common.configuration.ConfigurationController;
import br.net.dd.netherwingcore.common.configuration.ConfigurationSample;

/**
 * This class represents a singleton Cache responsible for managing the application's {@link Configuration}.
 *
 * <p>The {@link Cache} class ensures that the application's configuration is loaded and accessible globally
 * during runtime. It follows the Singleton design pattern to ensure a single instance of the cache exists
 * throughout the application lifecycle.</p>
 *
 * <p>Usage guide:</p>
 * <ul>
 *   <li>Prior to calling {@link #getConfiguration()}, initialize the cache using {@link #loadConfig(ConfigurationSample)}.</li>
 *   <li>Attempting to access the configuration before initialization will result in an {@link IllegalStateException}.</li>
 * </ul>
 */
public class Cache {

    /**
     * The single instance of the Cache class (Singleton instance).
     */
    private static Cache instance = null;

    /**
     * The configuration object loaded into the cache.
     */
    private Configuration configuration;

    /**
     * Private constructor to prevent direct instantiation of the class.
     */
    private Cache() {
    }

    /**
     * Loads the application's configuration into the cache using the specified {@link ConfigurationSample}.
     * If the cache instance does not already exist, it will be created and initialized during this method call.
     *
     * @param sample The configuration sample used to initialize the configuration.
     */
    public static void loadConfig(ConfigurationSample sample) {
        if (instance == null) {
            instance = new Cache();
        }
        ConfigurationController controller = new ConfigurationController(sample);
        instance.configuration = controller.getConfiguration();
    }

    /**
     * Retrieves the cached {@link Configuration}.
     *
     * <p>Ensure that {@link #loadConfig(ConfigurationSample)} is invoked before calling this method,
     * otherwise an {@link IllegalStateException} will be thrown.</p>
     *
     * @return The application's {@link Configuration} object.
     * @throws IllegalStateException If the cache has not been initialized via {@link #loadConfig(ConfigurationSample)}.
     */
    public static Configuration getConfiguration() {
        if (instance == null) {
            throw new IllegalStateException("Cache not initialized. Call loadConfig() first.");
        }
        return instance.configuration;
    }

}
