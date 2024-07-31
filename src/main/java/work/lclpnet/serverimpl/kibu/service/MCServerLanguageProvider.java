package work.lclpnet.serverimpl.kibu.service;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.kibu.translate.pref.LanguagePreferenceProvider;
import work.lclpnet.lclpnetwork.facade.MCPlayer;
import work.lclpnet.serverapi.util.ServerCache;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class MCServerLanguageProvider implements LanguagePreferenceProvider {

    private static final AtomicReference<ServerCache> staticCache = new AtomicReference<>(null);

    @Override
    public Optional<String> getLanguagePreference(ServerPlayerEntity player) {
        ServerCache serverCache = staticCache.get();

        if (serverCache == null) return Optional.empty();

        String uuid = player.getUuid().toString();
        MCPlayer mcPlayer = serverCache.getPlayer(uuid);

        if (mcPlayer == null) return Optional.empty();

        return Optional.ofNullable(mcPlayer.getLanguage());
    }

    public static void setServerCache(@Nullable ServerCache serverCache) {
        staticCache.set(serverCache);
    }
}
