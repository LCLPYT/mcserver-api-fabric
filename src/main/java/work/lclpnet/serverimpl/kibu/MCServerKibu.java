package work.lclpnet.serverimpl.kibu;

import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.net.NetworkHandler;
import work.lclpnet.serverimpl.kibu.util.KibuServerTranslation;

public interface MCServerKibu {

    NetworkHandler getNetworkHandler();

    ServerContext getServerContext();

    KibuServerTranslation getTranslations();

    static MCServerKibu getInstance() {
        return MCServerKibuPlugin.getInstance();
    }
}
