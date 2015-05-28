package at.yawk.jdcli.db.maven;

import at.yawk.jdcli.db.Artifact;
import at.yawk.jdcli.db.ArtifactComparator;

/**
 * @author yawkat
 */
abstract class AbstractMavenArtifactComparator implements ArtifactComparator<MavenArtifact> {
    @Override
    public int compare(Artifact a, Artifact b) {
        return compare((MavenArtifact) a, (MavenArtifact) b);
    }

    protected abstract int compare(MavenArtifact a, MavenArtifact b);

    @Override
    public boolean canCompare(MavenArtifact a, Artifact b) {
        return b instanceof MavenArtifact;
    }
}
