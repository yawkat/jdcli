package at.yawk.jdcli.doclet;

import com.sun.javadoc.ClassDoc;
import java.io.IOException;
import java.io.Writer;

/**
 * @author yawkat
 */
public interface Target {
    Writer openWriter(ClassDoc classDoc) throws IOException;
}
