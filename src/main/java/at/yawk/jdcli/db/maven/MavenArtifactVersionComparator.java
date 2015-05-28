package at.yawk.jdcli.db.maven;

/**
 * @author yawkat
 */
class MavenArtifactVersionComparator extends AbstractMavenArtifactComparator {
    @Override
    protected int compare(MavenArtifact a, MavenArtifact b) {
        return String.CASE_INSENSITIVE_ORDER.compare(a.getVersion(), b.getVersion());
    }
}
