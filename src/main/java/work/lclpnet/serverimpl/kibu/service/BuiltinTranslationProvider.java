package work.lclpnet.serverimpl.kibu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.lclpnet.serverimpl.kibu.MCServerKibuPlugin;
import work.lclpnet.translations.loader.TranslationProvider;
import work.lclpnet.translations.loader.language.LanguageLoader;
import work.lclpnet.translations.loader.language.UrlLanguageLoader;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public class BuiltinTranslationProvider implements TranslationProvider {

    private final Logger logger = LoggerFactory.getLogger(MCServerKibuPlugin.ID);

    @Override
    public LanguageLoader create() {
        URL[] urls = UrlLanguageLoader.getResourceLocations(this);
        List<String> directories = Collections.singletonList("lang/");

        return new UrlLanguageLoader(urls, directories, logger);
    }
}
