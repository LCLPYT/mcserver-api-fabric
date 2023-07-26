package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.access.PlayerLanguage;
import work.lclpnet.kibu.translate.hook.LanguageChangedCallback;
import work.lclpnet.lclpnetwork.facade.MCPlayer;
import work.lclpnet.serverapi.util.ServerCache;

import java.util.UUID;

public class KibuServerCache extends ServerCache {

    private final MinecraftServer server;

    public KibuServerCache(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void cachePlayer(MCPlayer player) {
        super.cachePlayer(player);

        PlayerManager playerManager = server.getPlayerManager();
        if (playerManager == null) return;

        UUID uuid = UUID.fromString(player.getUuid());
        ServerPlayerEntity serverPlayer = playerManager.getPlayer(uuid);

        if (serverPlayer == null) return;

        String playerLanguage = player.getLanguage();
        String language = playerLanguage != null ? playerLanguage : PlayerLanguage.getLanguage(serverPlayer);

        server.submit(() -> LanguageChangedCallback.HOOK.invoker().onChanged(serverPlayer, language, LanguageChangedCallback.Reason.OTHER));
    }
}
