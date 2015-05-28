package at.yawk.jdcli.db;

import at.yawk.jdcli.db.file.FileBinding;
import at.yawk.jdcli.db.maven.MavenArtifactProviderBinding;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author yawkat
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MavenArtifactProviderBinding.class, name = "maven"),
        @JsonSubTypes.Type(value = FileBinding.class, name = "file"),
})
public interface ArtifactProviderBinding {
    ArtifactProvider<?> buildProvider();
}
