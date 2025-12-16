package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import work.lclpnet.kibu.access.PlayerLanguage;
import work.lclpnet.kibu.translate.hook.LanguageChangedCallback;
import work.lclpnet.lclpnetwork.facade.MCPlayer;
import work.lclpnet.serverapi.util.ServerCache;

import java.util.UUID;

public class FabricServerCache extends ServerCache {

    private final MinecraftServer server;

    public FabricServerCache(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void cachePlayer(MCPlayer player) {
        super.cachePlayer(player);

        PlayerList playerManager = server.getPlayerList();
        if (playerManager == null) return;

        UUID uuid = UUID.fromString(player.getUuid());
        ServerPlayer serverPlayer = playerManager.getPlayer(uuid);

        if (serverPlayer == null) return;

        String playerLanguage = player.getLanguage();
        String language = playerLanguage != null ? playerLanguage : PlayerLanguage.getLanguage(serverPlayer);

        server.execute(() -> LanguageChangedCallback.HOOK.invoker().onChanged(serverPlayer, language, LanguageChangedCallback.Reason.OTHER));
    }
}
