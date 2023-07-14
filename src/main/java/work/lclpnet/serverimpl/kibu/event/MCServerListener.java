package work.lclpnet.serverimpl.kibu.event;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.hook.player.PlayerInventoryHooks;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lclpnetwork.ext.LCLPMinecraftAPI;
import work.lclpnet.lclpnetwork.facade.MCStats;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.util.ServerCache;
import work.lclpnet.serverimpl.kibu.MCServerKibu;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.network.NetworkHandler;
import work.lclpnet.serverimpl.kibu.util.StatsDisplay;
import work.lclpnet.serverimpl.kibu.util.StatsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MCServerListener implements HookListenerModule {

    private final ServerCache serverCache;
    private final ConfigAccess configAccess;
    private final StatsManager statsManager;
    private final StatsDisplay statsDisplay;
    private final Logger logger;

    public MCServerListener(ServerCache serverCache, ConfigAccess configAccess, StatsManager statsManager,
                            StatsDisplay statsDisplay, Logger logger) {
        this.serverCache = serverCache;
        this.configAccess = configAccess;
        this.statsManager = statsManager;
        this.statsDisplay = statsDisplay;
        this.logger = logger;
    }

    @Override
    public void registerListeners(HookRegistrar registrar) {
        registrar.registerHook(PlayerConnectionHooks.JOIN, this::onJoin);
        registrar.registerHook(PlayerConnectionHooks.QUIT, this::onQuit);

        registrar.registerHook(PlayerInventoryHooks.MODIFY_INVENTORY, clickEvent -> {
            onModifyInventory(clickEvent);
            return false;
        });
    }

    private void onJoin(ServerPlayerEntity player) {
        updateLastSeen(player, true);
    }

    private void onQuit(ServerPlayerEntity player) {
        updateLastSeen(player, false)
                .exceptionally(ignored -> null)
                .thenRun(() -> serverCache.dropAllCachesFor(player.getUuid().toString()));
    }

    private void onModifyInventory(PlayerInventoryHooks.ClickEvent event) {
        if (event.action() != SlotActionType.PICKUP) return;

        Inventory inventory = event.inventory();
        if (inventory == null) return;

        var statsInv = statsManager.getStatsInventory(inventory);
        if (statsInv == null) return;

        ServerPlayerEntity player = event.player();

        ItemStack stack = event.clickedStack();
        if (stack == null) return;

        MCStats.Entry group = statsInv.getItemStackGroup(stack);
        if (group != null) {
            if (group.getChildren() == null) return;

            List<MCStats.Entry> items = new ArrayList<>(group.getChildren());
            items.remove(group);

            statsDisplay.createStatsInv(
                    statsInv.getTitle(),
                    group,
                    items,
                    0,
                    player,
                    statsInv
            ).open(player);
            return;
        }

        if (stack.equals(statsInv.getNextPageItem())) {
            statsDisplay.createStatsInv(
                    statsInv.getTitle(),
                    statsInv.getMainEntry(),
                    statsInv.getItems(),
                    statsInv.getPage() + 1,
                    player,
                    statsInv.getParent()
            ).open(player);
            return;
        }

        if (stack.equals(statsInv.getPrevPageItem())) {
            statsDisplay.createStatsInv(
                    statsInv.getTitle(),
                    statsInv.getMainEntry(),
                    statsInv.getItems(),
                    statsInv.getPage() - 1,
                    player,
                    statsInv.getParent()
            ).open(player);
            return;
        }

        if (stack.equals(statsInv.getBackItem())) {
            StatsManager.StatsInventory parent = statsInv.getParent();
            if (parent == null) return;

            statsDisplay.createStatsInv(
                    parent.getTitle(),
                    parent.getMainEntry(),
                    parent.getItems(),
                    parent.getPage(),
                    player,
                    parent.getParent()
            ).open(player);
        }
    }

    private CompletableFuture<Void> updateLastSeen(ServerPlayerEntity player, boolean forceLoadPlayer) {
        NetworkHandler networkHandler = MCServerKibu.getInstance().getNetworkHandler();

        String uuid = player.getUuid().toString();

        Optional<MCServerAPI> optApi = networkHandler.getApi();

        if (optApi.isEmpty()) {
            if (forceLoadPlayer) {
                return serverCache.refreshPlayer(LCLPMinecraftAPI.INSTANCE, uuid);
            }

            return CompletableFuture.completedFuture(null);
        }

        return optApi.get().updateLastSeen(uuid, serverCache).exceptionally(ex -> {
            if (configAccess.getConfig().debug) {
                ex.printStackTrace();
            }

            return null;
        }).thenAccept(res -> {
            if (res == null) {
                logger.warn("Could not update last seen for player '{}'.", player.getEntityName());
            }
        });
    }
}
