package at.yawk.jdcli.doclet;

import com.sun.javadoc.ClassDoc;
import java.io.IOException;
import java.io.Writer;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class StreamTarget implements Target {
    private final Writer target;

    @Override
    public Writer openWriter(ClassDoc classDoc) throws IOException {
        return new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                target.write(cbuf, off, len);
            }

            @Override
            public void flush() throws IOException {
                target.flush();
            }

            @Override
            public void close() throws IOException {}
        };
    }
}
