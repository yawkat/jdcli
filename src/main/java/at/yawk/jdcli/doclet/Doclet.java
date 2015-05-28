package at.yawk.jdcli.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Paths;

/**
 * @author yawkat
 */
public class Doclet {
    @SuppressWarnings("unused")
    public static boolean start(RootDoc root) throws IOException {
        int width = 80;
        Target target = null;
        for (String[] option : root.options()) {
            if (option[0].equals("-d")) {
                target = new DirectoryTarget(Paths.get(option[1]));
            } else if (option[0].equals("-o")) {
                target = new StreamTarget(new OutputStreamWriter(System.out));
            } else if (option[0].equals("-w")) {
                width = Integer.parseInt(option[1]);
            }
        }
        if (target == null) {
            target = new DirectoryTarget(Paths.get("."));
        }

        for (ClassDoc classDoc : root.classes()) {
            try (Writer writer = target.openWriter(classDoc)) {
                TextEmitter emitter = new MonospacedTextEmitter(s -> {
                    try {
                        writer.write(s);
                        writer.write('\n');
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }, width);
                new ClassDocWriter(emitter).write(classDoc);
            }
        }
        return true;
    }

    public static int optionLength(String option) {
        if (option.equals("-o") ||
            option.equals("-w") ||
            option.equals("-d")) {
            return 2;
        }
        return 0;
    }

    public static boolean validOptions(String options[][], DocErrorReporter reporter) {
        return true;
    }
}
