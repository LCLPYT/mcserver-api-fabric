package work.lclpnet.serverimpl.kibu;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.translations.DefaultLanguageTranslator;
import work.lclpnet.translations.loader.language.UrlLanguageLoader;
import work.lclpnet.translations.loader.translation.DirectTranslationLoader;
import work.lclpnet.translations.network.LCLPNetworkLanguageLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

class MCServerTranslations {

    private MCServerTranslations() {}

    public static Result load(Logger logger) {
        var locations = FabricLoader.getInstance()
                .getModContainer(MCServerFabricMod.ID)
                .orElseThrow(() -> new NoSuchElementException("Failed to find mod container"))
                .getRootPaths()
                .stream()
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (MalformedURLException e) {
                        logger.error("Failed to convert path {} to url", path, e);
                        return null;
                    }
                }).filter(Objects::nonNull)
                .toArray(URL[]::new);

        var builtinLoaderLoader = new UrlLanguageLoader(locations, List.of("assets/" + MCServerFabricMod.ID + "/lang/"), logger);

        List<String> apps = Collections.singletonList("mc_server");
        var remoteLoader = new LCLPNetworkLanguageLoader(apps, null, logger);

        var translationLoader = new DirectTranslationLoader(builtinLoaderLoader, remoteLoader);

        DefaultLanguageTranslator translator = new DefaultLanguageTranslator(translationLoader);

        TranslationService translationService = new TranslationService(translator);
        var whenLoaded = translator.reload();

        return new Result(translationService, whenLoaded);
    }

    public record Result(TranslationService translations, CompletableFuture<Void> whenLoaded) {}
}
