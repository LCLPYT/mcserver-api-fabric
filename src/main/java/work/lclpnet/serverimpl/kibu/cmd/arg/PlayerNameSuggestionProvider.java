package work.lclpnet.serverimpl.kibu.cmd.arg;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class PlayerNameSuggestionProvider implements SuggestionProvider<CommandSourceStack> {

    protected PlayerNameSuggestionProvider() {}

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();

        SharedSuggestionProvider.suggest(server.getPlayerList().getPlayerNamesArray(), builder);

        return builder.buildFuture();
    }

    public static PlayerNameSuggestionProvider getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static final PlayerNameSuggestionProvider instance = new PlayerNameSuggestionProvider();
    }
}
