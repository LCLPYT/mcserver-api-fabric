package work.lclpnet.serverimpl.kibu.config;

import org.json.JSONObject;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.config.json.JsonConfigFactory;

public class MCSConfig implements JsonConfig {

    public boolean debug = false;
    public String appName = "mcserver";
    public String environment = "live";
    public String host = "https://lclpnet.work";

    public MCSConfig() {}

    public MCSConfig(JSONObject json) {
        if (json.has("debug")) {
            debug = json.getBoolean("debug");
        }

        if (json.has("network")) {
            JSONObject network = json.getJSONObject("network");

            if (network.has("app_name")) {
                appName = network.getString("app_name");
            }

            if (network.has("environment")) {
                environment = network.getString("environment");
            }

            if (network.has("host")) {
                host = network.getString("host");
            }
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("debug", debug);

        JSONObject network = new JSONObject();

        network.put("app_name", appName != null ? appName : JSONObject.NULL);
        network.put("environment", environment);

        json.put("network", network);

        return json;
    }

    public static final JsonConfigFactory<MCSConfig> FACTORY = new JsonConfigFactory<>() {
        @Override
        public MCSConfig createDefaultConfig() {
            return new MCSConfig();
        }

        @Override
        public MCSConfig createConfig(JSONObject json) {
            return new MCSConfig(json);
        }
    };
}
