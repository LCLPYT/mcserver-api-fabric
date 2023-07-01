package work.lclpnet.serverimpl.kibu.util;

import work.lclpnet.mplugins.MPluginsAPI;
import work.lclpnet.plugin.load.LoadedPlugin;
import work.lclpnet.translations.loader.translation.MultiSourceTranslationLoader;
import work.lclpnet.translations.loader.translation.SPITranslationLoader;
import work.lclpnet.translations.model.LanguageCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class KibuSPITranslationLoader extends MultiSourceTranslationLoader {

    @Override
    protected void collectFutures(List<CompletableFuture<? extends LanguageCollection>> futures) {
        final Set<ClassLoader> loaders = new HashSet<>();
        Set<LoadedPlugin> plugins = MPluginsAPI.get().getPluginFrame().getPluginManager().getPlugins();

        for (LoadedPlugin plugin : plugins) {
            loaders.add(plugin.getPlugin().getClass().getClassLoader());
        }

        for (ClassLoader loader : loaders) {
            SPITranslationLoader spiLoader = new SPITranslationLoader(loader);
            futures.add(spiLoader.load());
        }
    }
}
