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
        return root.resolve(clazz.qualifiedName().replace('.', '/') + EXTENSION);
    }

    @Override
    public Writer openWriter(ClassDoc classDoc) throws IOException {
        Path targetFile = getPath(classDoc);
        if (!Files.isDirectory(targetFile.getParent())) {
            Files.createDirectories(targetFile.getParent());
        }
        System.out.println("ow " + targetFile);
        return Files.newBufferedWriter(targetFile);
    }
}
