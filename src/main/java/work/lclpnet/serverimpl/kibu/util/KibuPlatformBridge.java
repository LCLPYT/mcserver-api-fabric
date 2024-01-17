package work.lclpnet.serverimpl.kibu.util;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.serverapi.msg.MCMessage;
import work.lclpnet.serverapi.util.IPlatformBridge;
import work.lclpnet.serverapi.util.MojangAPI;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class KibuPlatformBridge implements IPlatformBridge {

    private final PlayerManager playerManager;
    private final Logger logger;
    private final KibuMCMessageImpl messageSerializer;

    public KibuPlatformBridge(PlayerManager playerManager, TranslationService translations, Logger logger) {
        this.playerManager = playerManager;
        this.logger = logger;
        this.messageSerializer = new KibuMCMessageImpl(translations);
    }

    @Override
    public void sendMessageTo(String playerUuid, MCMessage msg) {
        UUID uuid = UUID.fromString(playerUuid);
        ServerPlayerEntity player = playerManager.getPlayer(uuid);

        if (player == null) {
            logger.error("There is no player with UUID '{}' online!", playerUuid);
            return;
        }

        Text message = convertMessage(msg, player);

        player.sendMessage(message);
    }

    public MutableText convertMessage(MCMessage msg, ServerPlayerEntity player) {
        return messageSerializer.convert(msg, player);
    }

    @Override
    public CompletableFuture<String> getPlayerNameByUUID(String uuid) {
        ServerPlayerEntity online = playerManager.getPlayer(uuid);

        if (online != null) {
            return CompletableFuture.completedFuture(online.getNameForScoreboard());
        }

        return MojangAPI.getUsernameByUUID(uuid);
    }

    @Override
    public CompletableFuture<String> getPlayerUUIDByName(String name) {
        ServerPlayerEntity online = playerManager.getPlayer(name);

        if (online != null) {
            return CompletableFuture.completedFuture(online.getUuid().toString());
        }

        return MojangAPI.getUUIDByUsername(name);
    }
}
