package at.yawk.jdcli.db;

import java.util.List;

/**
 * @author yawkat
 */
public interface ArtifactProvider<A extends Artifact> {
    List<Artifact> listArtifacts();

    /**
     * Comparator that places early versions first.
     */
    ArtifactComparator<A> getVersionComparator();
}
