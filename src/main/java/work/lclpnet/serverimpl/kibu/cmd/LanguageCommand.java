package work.lclpnet.serverimpl.kibu.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
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

    private LiteralArgumentBuilder<CommandSourceStack> command(String name) {
        final ServerCache cache = getContext().getCache();

        return Commands.literal(name)
                .executes(this::getLanguage)
                .then(Commands.argument("language", StringArgumentType.word())
                        .suggests(new LanguageSuggestionProvider(cache))
                        .executes(this::setLanguage));
    }

    private int getLanguage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        execute(player.getUUID().toString(), new Object[0]);

        return 1;
    }

    private int setLanguage(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        String language = StringArgumentType.getString(ctx, "language");

        execute(player.getUUID().toString(), new Object[]{ language });

        return 1;
    }
}
