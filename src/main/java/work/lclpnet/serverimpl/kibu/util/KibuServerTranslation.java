package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.access.PlayerLanguage;
import work.lclpnet.serverapi.msg.ServerTranslations;

import java.util.concurrent.CompletableFuture;

public class KibuServerTranslation {

    private final ServerTranslations serverTranslations;

    public KibuServerTranslation(ServerTranslations serverTranslations) {
        this.serverTranslations = serverTranslations;
    }

    public CompletableFuture<Void> init() {
        return serverTranslations.reloadTranslations();
    }

    public String translate(ServerPlayerEntity player, String key, Object... substitutes) {
        String language = PlayerLanguage.getLanguage(player);

        return serverTranslations.getTranslation(player.getUuid().toString(), language, key, substitutes);
    }

    public ServerTranslations getServerTranslations() {
        return serverTranslations;
    }
}
