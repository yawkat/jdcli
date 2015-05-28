package at.yawk.jdcli;

import at.yawk.jdcli.db.ArtifactProviderBinding;
import at.yawk.jdcli.db.file.FileBinding;
import at.yawk.jdcli.db.maven.MavenArtifactProviderBinding;
import at.yawk.jdcli.doclet.DocletSettings;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class Config {
    private DocletSettings doclet = new DocletSettings();
    private List<ArtifactProviderBinding> artifactProviders;

    {
        MavenArtifactProviderBinding defaultMaven = new MavenArtifactProviderBinding();
        FileBinding defaultJdk = new FileBinding();
        defaultJdk.setId("jdk");
        defaultJdk.setPath(Paths.get(System.getProperty("java.home"), "..", "src.zip"));
        artifactProviders = Arrays.asList(
                defaultMaven,
                defaultJdk
        );
    }
}
