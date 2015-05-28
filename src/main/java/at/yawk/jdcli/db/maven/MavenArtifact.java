package at.yawk.jdcli.db.maven;

import at.yawk.jdcli.db.Artifact;
import at.yawk.jdcli.db.ArtifactProvider;
import at.yawk.jdcli.db.Util;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class MavenArtifact implements Artifact {
    private final MavenArtifactProvider provider;
    private final org.eclipse.aether.artifact.Artifact artifact;

    @Override
    public ArtifactProvider getProvider() {
        return provider;
    }

    @Override
    public String getId() {
        return artifact.getGroupId() + ':' + artifact.getArtifactId();
    }

    @Override
    public String getVersion() {
        return artifact.getVersion();
    }

    private Path getPath(String classifier) {
        String relative = provider.session.getLocalRepositoryManager()
                .getPathForLocalArtifact(getArtifact(classifier));
        return provider.session.getLocalRepository().getBasedir().toPath().resolve(relative);
    }

    private DefaultArtifact getArtifact(String classifier) {
        return new DefaultArtifact(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                classifier,
                "jar",
                artifact.getVersion()
        );
    }

    @Override
    @SneakyThrows
    public String computeHash() {
        return Util.hashFile(getPath("sources"));
    }

    @Override
    @SneakyThrows
    public String resolveClassPath() {
        CollectRequest request = new CollectRequest();
        request.setRoot(new Dependency(getArtifact(""), ""));
        request.addRepository(provider.centralRepository);
        CollectResult result = provider.system.collectDependencies(provider.session, request);
        DependencyNode root = result.getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(root);

        provider.system.resolveDependencies(provider.session, dependencyRequest);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        root.accept(nlg);
        return nlg.getClassPath();
    }

    @Override
    public Path extract(Path tempDirectory) throws IOException {
        Util.extractZip(getPath("sources"), tempDirectory);
        return tempDirectory;
    }

    @Override
    public String toString() {
        return "maven:" + getId() + ":" + getVersion();
    }
}
