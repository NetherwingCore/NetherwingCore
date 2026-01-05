package br.net.dd.netherwingcore.common.cache;

import br.net.dd.netherwingcore.common.configuration.Configuration;
import br.net.dd.netherwingcore.common.configuration.ConfigurationController;
import br.net.dd.netherwingcore.common.configuration.ConfigurationSample;

public class Cache {

    private static Cache instance = null;

    private Configuration configuration;

    private Cache() {
    }

    public static void loadConfig(ConfigurationSample sample) {
        if (instance == null) {
            instance = new Cache();
        }
        ConfigurationController controller = new ConfigurationController(sample);
        instance.configuration = controller.getConfiguration();
    }

    public static Configuration getConfiguration() {
        if (instance == null) {
            throw new IllegalStateException("Cache not initialized. Call loadConfig() first.");
        }
        return instance.configuration;
    }

}
