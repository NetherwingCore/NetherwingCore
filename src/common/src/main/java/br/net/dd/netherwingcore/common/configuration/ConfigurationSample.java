package br.net.dd.netherwingcore.common.configuration;

/**
 * This class serves as a representation of a configuration sample.
 * It acts as a holder for an instance of a {@link Configuration} object
 * and a file name associated with the configuration.
 *
 * The {@code ConfigurationSample} class provides methods to get and set
 * the configuration instance as well as the file name.
 */
public class ConfigurationSample {

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
