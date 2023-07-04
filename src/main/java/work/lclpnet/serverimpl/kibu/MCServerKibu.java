package work.lclpnet.serverimpl.kibu;

import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.net.NetworkHandler;
import work.lclpnet.translate.TranslationService;

public interface MCServerKibu {

    NetworkHandler getNetworkHandler();

    ServerContext getServerContext();

    TranslationService getTranslationService();

    static MCServerKibu getInstance() {
        return MCServerKibuPlugin.getInstance();
    }
}
