package work.lclpnet.serverimpl.kibu.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
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

    private LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal(getName())
                .executes(this::showOwnStats)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(PlayerNameSuggestionProvider.getInstance())
                        .executes(this::showStats));
    }

    private int showOwnStats(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        execute(player.getUUID().toString(), new Object[0]);

        return 1;
    }

    private int showStats(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        String target = StringArgumentType.getString(ctx, "player");

        execute(player.getUUID().toString(), new Object[]{ target });

        return 1;
    }

    @Override
    public void openStats(String invokerUuid, String targetUuid, MCMessage title, MCStats targetStats) {
        final ServerPlayer invoker = server.getPlayerList().getPlayer(UUID.fromString(invokerUuid));
        if (invoker == null) throw new NullPointerException("Invoker is null");

        Component titleText = Component.literal(getPlatformBridge().convertMessage(title, invoker).getString());

        var entries = new ArrayList<>(targetStats.getStats());
        MCStats.Entry mainEntry = targetStats.getModule("general");

        if (mainEntry != null) entries.remove(mainEntry);

        KibuInventory inv = statsDisplay.createStatsInv(titleText, mainEntry, entries, 0, invoker, null);

        inv.open(invoker);
    }
}
