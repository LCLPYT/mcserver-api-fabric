package work.lclpnet.serverimpl.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;

public class MCServerFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        System.out.println("Test");
    }
}
