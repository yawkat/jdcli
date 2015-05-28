package at.yawk.jdcli.db.maven;

import at.yawk.jdcli.db.ArtifactProvider;
import at.yawk.jdcli.db.ArtifactProviderBinding;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class MavenArtifactProviderBinding implements ArtifactProviderBinding {
    private Path repositoryPath = Paths.get(System.getProperty("user.home"), ".m2", "repository");

    @Override
    public ArtifactProvider<?> buildProvider() {
        return new MavenArtifactProvider(repositoryPath);
    }
}
