package work.lclpnet.serverimpl.kibu.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.cmd.MCLinkCommandScheme;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.util.FabricPlatformBridge;

public class MCLinkCommand extends PlatformCommand<Boolean> implements MCLinkCommandScheme {

    public MCLinkCommand(MCServerAPI api, FabricPlatformBridge platformBridge, ServerContext context, ConfigAccess configAccess) {
        super(api, platformBridge, context, configAccess);
    }

    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command());
    }

    private LiteralArgumentBuilder<ServerCommandSource> command() {
        return CommandManager.literal(getName())
                .executes(this::exec);
    }

    private int exec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

        execute(player.getUuid().toString(), new Object[0]);

        return 1;
    }
}
