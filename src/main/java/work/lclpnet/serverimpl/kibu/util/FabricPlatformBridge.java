package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.serverapi.msg.MCMessage;
import work.lclpnet.serverapi.util.IPlatformBridge;
import work.lclpnet.serverapi.util.MojangAPI;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FabricPlatformBridge implements IPlatformBridge {

    private final PlayerList playerManager;
    private final Logger logger;
    private final FabricMCMessageImpl messageSerializer;

    public FabricPlatformBridge(PlayerList playerManager, Translations translations, Logger logger) {
        this.playerManager = playerManager;
        this.logger = logger;
        this.messageSerializer = new FabricMCMessageImpl(translations);
    }

    @Override
    public void sendMessageTo(String playerUuid, MCMessage msg) {
        UUID uuid = UUID.fromString(playerUuid);
        ServerPlayer player = playerManager.getPlayer(uuid);

        if (player == null) {
            logger.error("There is no player with UUID '{}' online!", playerUuid);
            return;
        }

        Component message = convertMessage(msg, player);

        player.sendSystemMessage(message);
    }

    public MutableComponent convertMessage(MCMessage msg, ServerPlayer player) {
        return messageSerializer.convert(msg, player);
    }

    @Override
    public CompletableFuture<String> getPlayerNameByUUID(String uuid) {
        ServerPlayer online = playerManager.getPlayerByName(uuid);

        if (online != null) {
            return CompletableFuture.completedFuture(online.getScoreboardName());
        }

        return MojangAPI.getUsernameByUUID(uuid);
    }

    @Override
    public CompletableFuture<String> getPlayerUUIDByName(String name) {
        ServerPlayer online = playerManager.getPlayerByName(name);

        if (online != null) {
            return CompletableFuture.completedFuture(online.getUUID().toString());
        }

        return MojangAPI.getUUIDByUsername(name);
    }
}
