package work.lclpnet.serverimpl.kibu.network;

import org.slf4j.Logger;
import work.lclpnet.lclpnetwork.api.APIAccess;
import work.lclpnet.lclpnetwork.api.APIAuthAccess;
import work.lclpnet.serverapi.MCServerAPI;
import work.lclpnet.serverimpl.kibu.config.ConfigAccess;
import work.lclpnet.serverimpl.kibu.config.MCSConfig;
import work.lclpnet.storage.LocalLCLPStorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkHandler {

    private final ConfigAccess configAccess;
    private final Logger logger;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private MCServerAPI api;

    public NetworkHandler(ConfigAccess configAccess, Logger logger) {
        this.configAccess = configAccess;
        this.logger = logger;
    }

    public void init() {
        final MCSConfig config = configAccess.getConfig();

        final String token;

        try {
            token = loadToken(config);
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to load access token... Will fallback to no-op", e);
            } else {
                logger.warn("Failed to load access token... Will fallback to no-op");
            }
            return;
        }

        APIAuthAccess authAccess = new APIAuthAccess(token);
        authAccess.setHost(config.host);
        authAccess.setCustomExecutor(executor);

        try {
            authAccess = APIAccess.withAuthCheck(authAccess).join();
        } catch (RuntimeException e) {
            logger.error("Could not login to LCLPNetwork... Will fallback to no-op");
            return;
        }

        api = new MCServerAPI(authAccess);

        logger.info("Logged into LCLPNetwork successfully");
    }

    private String loadToken(MCSConfig config) throws IOException {
        final String appName = config.appName;

        if (appName == null) {
            throw new IllegalStateException("app_name was not set in the config!");
        }

        final Path dir = LocalLCLPStorage.getDirectory(
                appName,
                "access_tokens",
                Objects.requireNonNull(config.environment, "Environment not set in config"),
                "dedicated_server"
        ).toPath();

        Path tokenFile = dir.resolve("lclpnetwork.token");

        if (!Files.isRegularFile(tokenFile)) {
            throw new FileNotFoundException("'%s' does not exist!".formatted(tokenFile));
        }

        return Files.readString(tokenFile, StandardCharsets.UTF_8);
    }

    public Optional<MCServerAPI> getApi() {
        return Optional.ofNullable(api);
    }
}
