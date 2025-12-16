package work.lclpnet.serverimpl.kibu.cmd.arg;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import work.lclpnet.serverapi.util.ServerCache;

import java.util.concurrent.CompletableFuture;

public class LanguageSuggestionProvider implements SuggestionProvider<CommandSourceStack> {

    private final ServerCache serverCache;

    public LanguageSuggestionProvider(ServerCache serverCache) {
        this.serverCache = serverCache;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        SharedSuggestionProvider.suggest(serverCache.getRegisteredLanguages(), builder);

        return builder.buildFuture();
    }
}
