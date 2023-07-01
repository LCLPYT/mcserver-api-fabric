package work.lclpnet.serverimpl.kibu.config;

import org.slf4j.Logger;
import work.lclpnet.config.json.ConfigHandler;
import work.lclpnet.config.json.FileConfigSerializer;

import java.nio.file.Path;

public class ConfigManager implements ConfigAccess {

    private final ConfigHandler<MCSConfig> configHandler;

    public ConfigManager(Path configPath, Logger logger) {
        var serializer = new FileConfigSerializer<>(MCSConfig.FACTORY, logger);
        this.configHandler = new ConfigHandler<>(configPath, serializer, logger);
    }

    public void init() {
        configHandler.loadConfig();
    }

    @Override
    public MCSConfig getConfig() {
        return configHandler.getConfig();
    }
}
