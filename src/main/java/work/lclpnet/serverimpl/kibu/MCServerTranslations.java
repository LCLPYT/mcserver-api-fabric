package work.lclpnet.serverimpl.kibu;

import org.slf4j.Logger;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.util.ModTranslations;
import work.lclpnet.translations.DefaultLanguageTranslator;
import work.lclpnet.translations.loader.translation.DirectTranslationLoader;
import work.lclpnet.translations.loader.translation.MultiTranslationLoader;
import work.lclpnet.translations.network.LCLPNetworkLanguageLoader;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class MCServerTranslations {

    private MCServerTranslations() {}

    public static Result load(Logger logger) {
        var builtinTranslationLoaderLoader = ModTranslations.assetTranslationLoader(MCServerFabricMod.ID, logger);

        List<String> apps = Collections.singletonList("mc_server");
        var remoteLanguageLoader = new LCLPNetworkLanguageLoader(apps, null, logger);
        var remoteTranslationLoader = new DirectTranslationLoader(remoteLanguageLoader);

        var translationLoader = new MultiTranslationLoader(builtinTranslationLoaderLoader, remoteTranslationLoader);

        DefaultLanguageTranslator translator = new DefaultLanguageTranslator(translationLoader);

        Translations Translations = new Translations(translator);
        var whenLoaded = translator.reload();

        return new Result(Translations, whenLoaded);
    }

    public record Result(Translations translations, CompletableFuture<Void> whenLoaded) {}
}
