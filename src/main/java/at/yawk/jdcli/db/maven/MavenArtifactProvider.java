package at.yawk.jdcli.db.maven;

import at.yawk.jdcli.db.Artifact;
import at.yawk.jdcli.db.ArtifactComparator;
import at.yawk.jdcli.db.ArtifactProvider;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

/**
 * @author yawkat
 */
class MavenArtifactProvider implements ArtifactProvider<MavenArtifact> {
    final RepositorySystem system;
    final DefaultRepositorySystemSession session;
    final RemoteRepository centralRepository;

    MavenArtifactProvider(Path repositoryPath) {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                exception.printStackTrace();
            }
        });

        system = locator.getService(RepositorySystem.class);
        session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepository = new LocalRepository(repositoryPath.toFile());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepository));
        centralRepository = new RemoteRepository.Builder(
                "central", "default", "http://central.maven.org/maven2/").build();
    }

    @Override
    @SneakyThrows
    public List<Artifact> listArtifacts() {
        List<Artifact> artifacts = new ArrayList<>();
        Path repositoryPath = session.getLocalRepository().getBasedir().toPath();
        Files.walkFileTree(repositoryPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith("-sources.jar")) {
                    Path relative = repositoryPath.relativize(file);
                    String version = relative.getName(relative.getNameCount() - 2).toString();
                    String artifactId = relative.getName(relative.getNameCount() - 3).toString();
                    String groupId = relative.subpath(0, relative.getNameCount() - 3).toString().replace('/', '.');
                    artifacts.add(new MavenArtifact(
                            MavenArtifactProvider.this,
                            new DefaultArtifact(groupId, artifactId, null, version)
                    ));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return artifacts;
    }

    @Override
    public ArtifactComparator<MavenArtifact> getVersionComparator() {
        return new MavenArtifactVersionComparator();
    }
}
