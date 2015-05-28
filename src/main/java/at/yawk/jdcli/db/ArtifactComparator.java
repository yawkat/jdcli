package at.yawk.jdcli.db;

import java.util.Comparator;

/**
 * @author yawkat
 */
public interface ArtifactComparator<A extends Artifact> extends Comparator<Artifact> {
    /**
     * Compare the two given artifacts. The first artifact can be assumed to extend A (our type parameter), the second
     * will either extend A as well or be accepted by #canCompare.
     */
    @Override
    int compare(Artifact a, Artifact b);

    /**
     * @return Whether we can compare the second artifact to the first artifact using this comparator.
     */
    boolean canCompare(A a, Artifact b);
}
