package at.yawk.jdcli.doclet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
public class Invoker {
    private Invoker() {}

    @SneakyThrows(URISyntaxException.class)
    public static ProcessBuilder createDocletProcess(Path input, Path targetDirectory, String classPath, DocletSettings docletSettings)
            throws IOException {
        StringBuilder subpackageBuilder = new StringBuilder();
        Files.list(input).forEach(f -> {
            if (!Files.isDirectory(f)) { return; }
            if (subpackageBuilder.length() > 0) { subpackageBuilder.append(':'); }
            subpackageBuilder.append(f.getFileName());
        });

        Path classpath = Paths.get(Invoker.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return new ProcessBuilder().command(
                "javadoc",
                "-quiet", //"-Xdoclint:none",
                "-doclet", "at.yawk.jdcli.doclet.Doclet",
                "-docletpath", classpath.toString(),
                "-sourcepath", input.toString(),
                "-classpath", classPath,
                "-subpackages", subpackageBuilder.toString(),
                "-w", Integer.toString(docletSettings.getTerminalWidth()),
                "-d", targetDirectory.toAbsolutePath().toString()
        );
    }
}
