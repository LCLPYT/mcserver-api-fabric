package work.lclpnet.serverimpl.kibu.cmd;

import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.cmd.ICommandScheme;
import work.lclpnet.serverapi.cmd.IDebuggable;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.util.KibuPlatformBridge;

public abstract class PlatformCommand<T> implements ICommandScheme.IPlatformCommandScheme<T>, IDebuggable {

    private final MCServerAPI api;
    private final KibuPlatformBridge platformBridge;
    private final ServerContext context;
    private final ConfigAccess configAccess;

    public PlatformCommand(MCServerAPI api, KibuPlatformBridge platformBridge, ServerContext context, ConfigAccess configAccess) {
        this.api = api;
        this.platformBridge = platformBridge;
        this.context = context;
        this.configAccess = configAccess;
    }

    @Override
    public MCServerAPI getAPI() {
        return api;
    }

    @Override
    public KibuPlatformBridge getPlatformBridge() {
        return platformBridge;
    }

    @Override
    public ServerContext getContext() {
        return context;
    }

    @Override
    public boolean shouldDebug() {
        return configAccess.getConfig().debug;
    }
}
