package work.lclpnet.serverimpl.kibu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.plugin.KibuPlugin;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.mplugins.ext.WorldStateListener;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.msg.ServerTranslations;
import work.lclpnet.serverapi.util.ServerCache;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.cmd.LanguageCommand;
import work.lclpnet.serverimpl.kibu.cmd.MCLinkCommand;
import work.lclpnet.serverimpl.kibu.cmd.StatsCommand;
import work.lclpnet.serverimpl.kibu.config.ConfigManager;
import work.lclpnet.serverimpl.kibu.event.MCServerListener;
import work.lclpnet.serverimpl.kibu.network.NetworkHandler;
import work.lclpnet.serverimpl.kibu.service.ServerLanguagePreferenceProvider;
import work.lclpnet.serverimpl.kibu.util.KibuPlatformBridge;
import work.lclpnet.serverimpl.kibu.util.KibuSPITranslationLoader;
import work.lclpnet.serverimpl.kibu.util.StatsDisplay;
import work.lclpnet.serverimpl.kibu.util.StatsManager;

import java.nio.file.Path;

public class MCServerKibuPlugin extends KibuPlugin implements MCServerKibu, ServerContext, WorldStateListener {

    public static final String ID = "mcserver-api";
    private static final Logger logger = LoggerFactory.getLogger(ID);
    private static MCServerKibuPlugin instance = null;
    private TranslationService translationService = null;
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

        translationService = loadTranslations(serverCache);

        statsDisplay = new StatsDisplay(translationService, statsManager, logger);

        registerHooks(new MCServerListener(serverCache, configManager, statsManager, statsDisplay, logger));
    }

    @Override
    public void onWorldReady() {
        final MinecraftServer server = getEnvironment().getServer();
        final KibuPlatformBridge platformBridge = new KibuPlatformBridge(server.getPlayerManager(), translationService, logger);
        final MCServerAPI api = networkHandler.getApi().orElse(null);

        if (api == null) return;

        new LanguageCommand(api, platformBridge, this, configManager).register(this);
        new MCLinkCommand(api, platformBridge, this, configManager).register(this);
        new StatsCommand(api, platformBridge, this, configManager, server, statsDisplay).register(this);
    }

    @Override
    public void onWorldUnready() {

    }

    private TranslationService loadTranslations(ServerCache cache) {
        final KibuSPITranslationLoader loader = new KibuSPITranslationLoader();
        final ServerTranslations serverTranslations;

        try {
            serverTranslations = new ServerTranslations(cache, loader);
            serverTranslations.reloadTranslations().join();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not initialize server translations", e);
        }

        final ServerLanguagePreferenceProvider provider = new ServerLanguagePreferenceProvider(serverTranslations);

        return new TranslationService(serverTranslations.getTranslator(), provider);
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
    public TranslationService getTranslationService() {
        return translationService;
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
