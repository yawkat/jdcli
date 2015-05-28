package at.yawk.jdcli.db.file;

import at.yawk.jdcli.db.ArtifactProvider;
import at.yawk.jdcli.db.ArtifactProviderBinding;
import java.nio.file.Path;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class FileBinding implements ArtifactProviderBinding {
    private String id;
    private String version = "1.0";
    private Path path;
    private String classpath = "";

    @Override
    public ArtifactProvider<?> buildProvider() {
        return new FileArtifact(this);
    }
}
