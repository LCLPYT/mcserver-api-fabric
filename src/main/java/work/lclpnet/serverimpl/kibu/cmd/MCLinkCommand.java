package work.lclpnet.serverimpl.kibu.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
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

    private LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal(getName())
                .executes(this::exec);
    }

    private int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        execute(player.getUUID().toString(), new Object[0]);

        return 1;
    }
}
