package work.lclpnet.serverimpl.kibu;

import org.slf4j.Logger;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.translations.DefaultLanguageTranslator;
import work.lclpnet.translations.loader.MultiTranslationLoader;
import work.lclpnet.translations.network.LCLPNetworkTranslationLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class MCServerTranslations {

    private MCServerTranslations() {}

    public static Result load(Logger logger) {
        var parent = new MultiTranslationLoader();
        parent.addLoader(ModTranslations.assetTranslationLoader(MCServerFabricMod.ID, logger));

        List<String> apps = List.of("mc_server");
        var remoteLoader = new LCLPNetworkTranslationLoader(apps, null, logger);
        parent.addLoader(remoteLoader);

        DefaultLanguageTranslator translator = new DefaultLanguageTranslator(parent);

        Translations translations = new Translations(translator);
        var whenLoaded = translator.reload();

        return new Result(translations, whenLoaded);
    }

    public record Result(Translations translations, CompletableFuture<Void> whenLoaded) {}
}
