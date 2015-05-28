package at.yawk.jdcli;

import at.yawk.jdcli.db.ArtifactProviderBinding;
import at.yawk.jdcli.db.DatabaseManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * @author yawkat
 */
public class Updater {
    private static final String APP_NAME = "jdcli";

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk7Module());

        Path dataDir = getXdgDir("XDG_DATA_HOME", ".local/share");
        Config config;
        Path configFile = dataDir.resolve("config.json");
        if (Files.exists(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                config = objectMapper.readValue(in, Config.class);
            }
        } else {
            config = new Config();
            if (!Files.exists(configFile.getParent())) {
                Files.createDirectories(configFile.getParent());
            }
            try (OutputStream out = Files.newOutputStream(configFile)) {
                objectMapper.writeValue(out, config);
            }
        }

        DatabaseManager manager = new DatabaseManager(
                dataDir,
                config.getArtifactProviders().stream()
                        .map(ArtifactProviderBinding::buildProvider)
                        .collect(Collectors.toList()),
                config.getDoclet()
        );

        manager.updateAndLink();
    }

    private static Path getXdgDir(String variable, String def) {
        String env = System.getenv(variable);
        if (env == null) {
            return Paths.get(System.getProperty("user.home"), def, APP_NAME);
        } else {
            return Paths.get(env, APP_NAME);
        }
    }
}
