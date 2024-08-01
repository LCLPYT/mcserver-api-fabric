package work.lclpnet.serverimpl.kibu;

import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.network.NetworkHandler;

public interface MCServerFabric {

    NetworkHandler getNetworkHandler();

    ServerContext getServerContext();

    Translations getTranslations();

    static MCServerFabric getInstance() {
        return MCServerFabricMod.getInstance();
    }
}
