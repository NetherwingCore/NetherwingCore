package br.net.dd.netherwingcore.common.configuration;

/**
 * This abstract class represents a sample configuration that can be used to initialize the application's {@link Configuration}.
 *
 * <p>Subclasses of {@code ConfigurationSample} should provide specific implementations for loading and saving configuration data,
 * as well as defining the structure of the configuration file.</p>
 */
public abstract class ConfigurationSample {

    /**
     * The configuration object associated with this sample.
     */
    private Configuration configuration;

    /**
     * The name of the file where the configuration is saved or retrieved.
     */
    private String fileName;

    /**
     * Retrieves the file name associated with this configuration.
     *
     * @return the file name as a {@code String}.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name associated with this configuration.
     *
     * @param fileName the file name to set as a {@code String}.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Retrieves the configuration object associated with this sample.
     *
     * @return the {@link Configuration} instance.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration object associated with this sample.
     *
     * @param configuration the {@link Configuration} instance to set.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
