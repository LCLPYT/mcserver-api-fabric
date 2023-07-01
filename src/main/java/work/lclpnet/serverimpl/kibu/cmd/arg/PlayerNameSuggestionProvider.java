package work.lclpnet.serverimpl.kibu.cmd.arg;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class PlayerNameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    protected PlayerNameSuggestionProvider() {}

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();

        CommandSource.suggestMatching(server.getPlayerManager().getPlayerNames(), builder);

        return builder.buildFuture();
    }

    public static PlayerNameSuggestionProvider getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static final PlayerNameSuggestionProvider instance = new PlayerNameSuggestionProvider();
    }
}
