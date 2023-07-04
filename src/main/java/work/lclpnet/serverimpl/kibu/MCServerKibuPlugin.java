package work.lclpnet.serverimpl.kibu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.plugin.KibuPlugin;
import work.lclpnet.mplugins.ext.WorldStateListener;
import work.lclpnet.serverapi.msg.ServerTranslations;
import work.lclpnet.serverapi.util.ServerCache;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.cmd.KibuCommands;
import work.lclpnet.serverimpl.kibu.config.ConfigManager;
import work.lclpnet.serverimpl.kibu.event.MCServerListener;
import work.lclpnet.serverimpl.kibu.net.NetworkHandler;
import work.lclpnet.serverimpl.kibu.util.*;

import java.nio.file.Path;

public class MCServerKibuPlugin extends KibuPlugin implements MCServerKibu, ServerContext, WorldStateListener {

    public static final String ID = "mcserver-api";
    private static final Logger logger = LoggerFactory.getLogger(ID);
    private static MCServerKibuPlugin instance = null;
    private KibuServerTranslation translations = null;
    private NetworkHandler networkHandler = null;
    private ServerCache serverCache = null;
    private ConfigManager configManager = null;
    private final StatsManager statsManager = new StatsManager();
    private StatsDisplay statsDisplay = null;

    @Override
    protected void loadKibuPlugin() {
        instance = this;

        final Path configFile = FabricLoader.getInstance().getConfigDir().resolve(ID).resolve("config.json");
        configManager = new ConfigManager(configFile, logger);

        configManager.init();

        networkHandler = new NetworkHandler(configManager, logger);
        networkHandler.init();

        serverCache = new ServerCache();
        networkHandler.getApi().ifPresent(serverCache::init);

        loadTranslations(serverCache);

        statsDisplay = new StatsDisplay(translations, statsManager, logger);

        registerHooks(new MCServerListener(serverCache, configManager, statsManager, statsDisplay, logger));
    }

    @Override
    public void onWorldReady() {
        final MinecraftServer server = getEnvironment().getServer();
        final KibuPlatformBridge platformBridge = new KibuPlatformBridge(server.getPlayerManager(), translations, logger);

        new KibuCommands(networkHandler.getApi().orElse(null), platformBridge, this, configManager, translations, statsManager, statsDisplay)
                .register(this);
    }

    @Override
    public void onWorldUnready() {

    }

    private void loadTranslations(ServerCache cache) {
        KibuSPITranslationLoader loader = new KibuSPITranslationLoader();

        try {
            ServerTranslations serverTranslations = new ServerTranslations(cache, loader);

            translations = new KibuServerTranslation(serverTranslations);
            translations.init().join();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not initialize translation service", e);
        }
    }


    @Override
    public NetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    @Override
    public ServerContext getServerContext() {
        return this;
    }

    @Override
    public KibuServerTranslation getTranslations() {
        return translations;
    }

    @Override
    public ServerCache getCache() {
        return serverCache;
    }

    static MCServerKibuPlugin getInstance() {
        if (instance == null) throw new IllegalStateException("MCServerKibu is not yet loaded");

        return instance;
    }
}
