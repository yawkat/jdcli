package at.yawk.jdcli.doclet;

import com.sun.javadoc.ClassDoc;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class DirectoryTarget implements Target {
    private static final String EXTENSION = ".txt";

    private final Path root;

    private Path getPath(ClassDoc clazz) {
        if (clazz.containingClass() == null) {
            return root.resolve(clazz.qualifiedName().replace('.', '/') + EXTENSION);
        } else {
            Path containingPath = getPath(clazz.containingClass());
            return containingPath.subpath(0, containingPath.getNameCount() - 1)
                    .resolve(containingPath.getFileName().toString() + '.' + clazz.name() + EXTENSION);
        }
    }

    @Override
    public Writer openWriter(ClassDoc classDoc) throws IOException {
        Path targetFile = getPath(classDoc);
        if (!Files.isDirectory(targetFile.getParent())) {
            Files.createDirectories(targetFile.getParent());
        }
        return Files.newBufferedWriter(targetFile);
    }
}
