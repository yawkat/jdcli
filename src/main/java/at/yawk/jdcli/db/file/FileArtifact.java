package at.yawk.jdcli.db.file;

import at.yawk.jdcli.db.Artifact;
import at.yawk.jdcli.db.ArtifactComparator;
import at.yawk.jdcli.db.ArtifactProvider;
import at.yawk.jdcli.db.Util;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class FileArtifact implements Artifact, ArtifactProvider<FileArtifact> {
    private final FileBinding binding;

    @Override
    public ArtifactProvider getProvider() {
        return this;
    }

    @Override
    public String getId() {
        return binding.getId();
    }

    @Override
    public String getVersion() {
        return binding.getVersion();
    }

    @Override
    @SneakyThrows
    public String computeHash() {
        return Util.hashFile(binding.getPath());
    }

    @Override
    public String resolveClassPath() throws Exception {
        return binding.getClasspath();
    }

    @Override
    public Path extract(Path tempDirectory) throws IOException {
        if (Files.isDirectory(binding.getPath())) {
            return binding.getPath();
        } else {
            Util.extractZip(binding.getPath(), tempDirectory);
            return tempDirectory;
        }
    }

    @Override
    public List<Artifact> listArtifacts() {
        return Collections.singletonList(this);
    }

    @Override
    public ArtifactComparator<FileArtifact> getVersionComparator() {
        return new ArtifactComparator<FileArtifact>() {
            @Override
            public int compare(Artifact a, Artifact b) {
                return 0;
            }

            @Override
            public boolean canCompare(FileArtifact fileArtifact, Artifact b) {
                return b == fileArtifact;
            }
        };
    }

    @Override
    public String toString() {
        return "file:" + getId() + ":" + getVersion() + ":" + binding.getPath();
    }
}
