package at.yawk.jdcli.db;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author yawkat
 */
public interface Artifact {
    ArtifactProvider getProvider();

    /**
     * The ID of this artifact, excluding version. For maven, this is 'com.example.groupId:artifactId'.
     */
    String getId();

    String getVersion();

    String computeHash();

    String resolveClassPath() throws Exception;

    /**
     * Extract this artifact to a directory. Implementation may use the given temp directory if needed but does not
     * have to.
     *
     * @return The directory this artifact was extracted to
     */
    Path extract(Path tempDirectory) throws IOException;
}
