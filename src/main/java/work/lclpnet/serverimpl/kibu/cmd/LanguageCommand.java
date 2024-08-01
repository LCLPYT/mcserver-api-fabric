package work.lclpnet.serverimpl.kibu.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverapi.cmd.LanguageCommandScheme;
import work.lclpnet.serverapi.util.ServerCache;
import work.lclpnet.serverapi.util.ServerContext;
import work.lclpnet.serverimpl.kibu.cmd.arg.LanguageSuggestionProvider;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.util.FabricPlatformBridge;

public class LanguageCommand extends PlatformCommand<Boolean> implements LanguageCommandScheme {

    public LanguageCommand(MCServerAPI api, FabricPlatformBridge platformBridge, ServerContext context, ConfigAccess configAccess) {
        super(api, platformBridge, context, configAccess);
    }

    public void register(CommandRegistrar registrar) {
        registrar.registerCommand(command(getName()));

        // aliases
        registrar.registerCommand(command("lang"));
    }

    private LiteralArgumentBuilder<ServerCommandSource> command(String name) {
        final ServerCache cache = getContext().getCache();

        return CommandManager.literal(name)
                .executes(this::getLanguage)
                .then(CommandManager.argument("language", StringArgumentType.word())
                        .suggests(new LanguageSuggestionProvider(cache))
                        .executes(this::setLanguage));
    }

    private int getLanguage(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

        execute(player.getUuid().toString(), new Object[0]);

        return 1;
    }

    private int setLanguage(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

        String language = StringArgumentType.getString(ctx, "language");

        execute(player.getUuid().toString(), new Object[]{ language });

        return 1;
    }
}
