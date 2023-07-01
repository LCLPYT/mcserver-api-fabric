package work.lclpnet.serverimpl.kibu.cmd;

import net.minecraft.server.MinecraftServer;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.util.KibuPlatformBridge;

import javax.annotation.Nullable;

public class KibuCommands {

    private final MCServerAPI api;
    private final KibuPlatformBridge platformBridge;
    private final ServerContext serverContext;
    private final ConfigAccess configAccess;

    public KibuCommands(@Nullable MCServerAPI api, KibuPlatformBridge platformBridge, ServerContext serverContext, ConfigAccess configAccess) {
        this.api = api;
        this.platformBridge = platformBridge;
        this.serverContext = serverContext;
        this.configAccess = configAccess;
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

        new StatsCommand(api, platformBridge, serverContext, configAccess, server).register(context);
    }
}
