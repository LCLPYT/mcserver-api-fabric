package work.lclpnet.serverimpl.kibu.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.inv.type.KibuInventory;
import work.lclpnet.lclpnetwork.facade.MCStats;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.cmd.StatsCommandScheme;
import work.lclpnet.serverapi.msg.MCMessage;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.cmd.arg.PlayerNameSuggestionProvider;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.util.FabricPlatformBridge;
import work.lclpnet.serverimpl.kibu.util.StatsDisplay;

import java.util.ArrayList;
import java.util.UUID;

public class StatsCommand extends PlatformCommand<Boolean> implements StatsCommandScheme {

    private final MinecraftServer server;
    private final StatsDisplay statsDisplay;

    public StatsCommand(MCServerAPI api, FabricPlatformBridge platformBridge, ServerContext context, ConfigAccess configAccess,
                        MinecraftServer server, StatsDisplay statsDisplay) {
        super(api, platformBridge, context, configAccess);
        this.server = server;
        this.statsDisplay = statsDisplay;
    }

    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command());
    }

    private LiteralArgumentBuilder<ServerCommandSource> command() {
        return CommandManager.literal(getName())
                .executes(this::showOwnStats)
                .then(CommandManager.argument("player", StringArgumentType.word())
                        .suggests(PlayerNameSuggestionProvider.getInstance())
                        .executes(this::showStats));
    }

    private int showOwnStats(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

        execute(player.getUuid().toString(), new Object[0]);

        return 1;
    }

    private int showStats(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

        String target = StringArgumentType.getString(ctx, "player");

        execute(player.getUuid().toString(), new Object[]{ target });

        return 1;
    }

    @Override
    public void openStats(String invokerUuid, String targetUuid, MCMessage title, MCStats targetStats) {
        final ServerPlayerEntity invoker = server.getPlayerManager().getPlayer(UUID.fromString(invokerUuid));
        if (invoker == null) throw new NullPointerException("Invoker is null");

        Text titleText = Text.literal(getPlatformBridge().convertMessage(title, invoker).getString());

        var entries = new ArrayList<>(targetStats.getStats());
        MCStats.Entry mainEntry = targetStats.getModule("general");

        if (mainEntry != null) entries.remove(mainEntry);

        KibuInventory inv = statsDisplay.createStatsInv(titleText, mainEntry, entries, 0, invoker, null);

        inv.open(invoker);
    }
}
