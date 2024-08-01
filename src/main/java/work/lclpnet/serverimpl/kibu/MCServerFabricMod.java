package work.lclpnet.serverimpl.kibu;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.kibu.cmd.impl.CommandContainer;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.hook.HookContainer;
import work.lclpnet.kibu.hook.world.ServerWorldReadyCallback;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.util.ServerCache;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.cmd.LanguageCommand;
import work.lclpnet.serverimpl.kibu.cmd.MCLinkCommand;
import work.lclpnet.serverimpl.kibu.cmd.StatsCommand;
import work.lclpnet.serverimpl.kibu.config.ConfigManager;
import work.lclpnet.serverimpl.kibu.event.MCServerListener;
import work.lclpnet.serverimpl.kibu.network.NetworkHandler;
import work.lclpnet.serverimpl.kibu.service.MCServerLanguageProvider;
import work.lclpnet.serverimpl.kibu.util.FabricPlatformBridge;
import work.lclpnet.serverimpl.kibu.util.FabricServerCache;
import work.lclpnet.serverimpl.kibu.util.StatsDisplay;
import work.lclpnet.serverimpl.kibu.util.StatsManager;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class MCServerFabricMod implements DedicatedServerModInitializer, MCServerFabric, ServerContext {

    public static final String ID = "mcserver-api";
    private static final Logger logger = LoggerFactory.getLogger(ID);
    private static MCServerFabricMod instance = null;
    private Translations Translations = null;
    private NetworkHandler networkHandler = null;
    private ServerCache serverCache = null;
    private ConfigManager configManager = null;
    private final StatsManager statsManager = new StatsManager();
    private StatsDisplay statsDisplay = null;

    @Override
    public void onInitializeServer() {
        instance = this;

        var loadingTranslations = MCServerTranslations.load(logger);
        Translations = loadingTranslations.translations();

        final Path configFile = FabricLoader.getInstance().getConfigDir().resolve(ID).resolve("config.json");

        configManager = new ConfigManager(configFile, logger);
        networkHandler = new NetworkHandler(configManager, logger);
        statsDisplay = new StatsDisplay(Translations, statsManager, logger);

        var whenWorldReady = new CompletableFuture<MinecraftServer>();
        ServerWorldReadyCallback.HOOK.register(whenWorldReady::complete);

        var whenConfigReady = CompletableFuture.runAsync(configManager::init);
        var whenNetworkReady = whenConfigReady.thenRunAsync(networkHandler::init);

        Thread.startVirtualThread(() -> {
            loadingTranslations.whenLoaded().join();
            whenConfigReady.join();
            whenNetworkReady.join();

            MinecraftServer server = whenWorldReady.join();

            onLoaded(server);
        });
    }

    private void onLoaded(MinecraftServer server) {
        serverCache = new FabricServerCache(server);
        networkHandler.getApi().ifPresent(serverCache::init);

        MCServerLanguageProvider.setServerCache(serverCache);

        HookContainer hooks = new HookContainer();
        hooks.registerHooks(new MCServerListener(serverCache, configManager, statsManager, statsDisplay, logger));

        final FabricPlatformBridge platformBridge = new FabricPlatformBridge(server.getPlayerManager(), Translations, logger);
        final MCServerAPI api = networkHandler.getApi().orElse(null);

        if (api == null) return;

        CommandRegistrar commands = new CommandContainer();

        new LanguageCommand(api, platformBridge, this, configManager).register(commands);
        new MCLinkCommand(api, platformBridge, this, configManager).register(commands);
        new StatsCommand(api, platformBridge, this, configManager, server, statsDisplay).register(commands);
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
    public Translations getTranslations() {
        return Translations;
    }

    @Override
    public ServerCache getCache() {
        return serverCache;
    }

    static MCServerFabricMod getInstance() {
        if (instance == null) throw new IllegalStateException("MCServerKibu is not yet loaded");

        return instance;
    }
}
