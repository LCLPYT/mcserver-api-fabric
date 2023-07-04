package work.lclpnet.serverimpl.kibu.cmd;

import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.util.KibuPlatformBridge;
import work.lclpnet.serverimpl.kibu.util.KibuServerTranslation;
import work.lclpnet.serverimpl.kibu.util.StatsDisplay;
import work.lclpnet.serverimpl.kibu.util.StatsManager;

import javax.annotation.Nullable;

public class KibuCommands {

    private final MCServerAPI api;
    private final KibuPlatformBridge platformBridge;
    private final ServerContext serverContext;
    private final ConfigAccess configAccess;
    private final KibuServerTranslation translations;
    private final StatsManager statsManager;
    private final StatsDisplay statsDisplay;

    public KibuCommands(@Nullable MCServerAPI api, KibuPlatformBridge platformBridge, ServerContext serverContext,
                        ConfigAccess configAccess, KibuServerTranslation translations, StatsManager statsManager,
                        StatsDisplay statsDisplay) {
        this.api = api;
        this.platformBridge = platformBridge;
        this.serverContext = serverContext;
        this.configAccess = configAccess;
        this.translations = translations;
        this.statsManager = statsManager;
        this.statsDisplay = statsDisplay;
    }

    public void register(PluginContext context) {
        if (api == null) {
            // commands without MCServerAPI dependency go here
            return;
        }

        new LanguageCommand(api, platformBridge, serverContext, configAccess).register(context);
        new MCLinkCommand(api, platformBridge, serverContext, configAccess).register(context);

        final MinecraftServer server = context.getEnvironment().getServer();
        if (server == null) throw new IllegalStateException("Server is null");

        new StatsCommand(api, platformBridge, serverContext, configAccess, server, statsDisplay).register(context);
    }
}
