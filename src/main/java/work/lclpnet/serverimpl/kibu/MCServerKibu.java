package work.lclpnet.serverimpl.kibu;

import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.network.NetworkHandler;

public interface MCServerKibu {

    NetworkHandler getNetworkHandler();

    ServerContext getServerContext();

    TranslationService getTranslationService();

    static MCServerKibu getInstance() {
        return MCServerKibuPlugin.getInstance();
    }
}
