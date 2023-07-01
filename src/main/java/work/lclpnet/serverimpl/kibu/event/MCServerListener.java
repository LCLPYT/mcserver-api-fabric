package work.lclpnet.serverimpl.kibu.event;

import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.serverapi.util.ServerCache;
import work.lclpnet.serverimpl.kibu.MCServerKibu;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.net.NetworkHandler;

import java.util.concurrent.CompletableFuture;

public class MCServerListener implements HookListenerModule {

    private final ServerCache serverCache;
    private final ConfigAccess configAccess;
    private final Logger logger;

    public MCServerListener(ServerCache serverCache, ConfigAccess configAccess, Logger logger) {
        this.serverCache = serverCache;
        this.configAccess = configAccess;
        this.logger = logger;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerConnectionHooks.JOIN, this::onJoin);
        registrar.registerHook(PlayerConnectionHooks.QUIT, this::onQuit);
    }

    private void onJoin(ServerPlayerEntity player) {
        updateLastSeen(player);
    }

    private void onQuit(ServerPlayerEntity player) {
        updateLastSeen(player).thenRun(() -> serverCache.dropAllCachesFor(player.getUuid().toString()));
    }

    private CompletableFuture<Void> updateLastSeen(ServerPlayerEntity player) {
        NetworkHandler networkHandler = MCServerKibu.getInstance().getNetworkHandler();

        return networkHandler.getApi().map(api -> api.updateLastSeen(player.getUuid().toString()).exceptionally(ex -> {
            if (configAccess.getConfig().debug) {
                ex.printStackTrace();
            }

            return null;
        }).thenAccept(res -> {
            if (res == null) {
                logger.warn("Could not update last seen for player '{}'.", player.getEntityName());
            }
        })).orElse(null);
    }
}
