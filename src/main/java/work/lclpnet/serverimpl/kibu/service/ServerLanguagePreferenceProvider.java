package work.lclpnet.serverimpl.kibu.service;

import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.serverapi.msg.ServerTranslations;
import work.lclpnet.translate.pref.LanguagePreferenceProvider;

import java.util.Optional;

public class ServerLanguagePreferenceProvider implements LanguagePreferenceProvider {

    private final ServerTranslations serverTranslations;

    public ServerLanguagePreferenceProvider(ServerTranslations serverTranslations) {
        this.serverTranslations = serverTranslations;
    }

    @Override
    public Optional<String> getLanguagePreference(ServerPlayerEntity player) {
        String uuid = player.getUuid().toString();
        return Optional.ofNullable(serverTranslations.getPreferredLanguage(uuid));
    }
}
