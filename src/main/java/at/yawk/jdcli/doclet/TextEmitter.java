package at.yawk.jdcli.doclet;

/**
 * @author yawkat
 */
public interface TextEmitter {
    void indent(int n);

    void lineUpIndentWithText();

    default void indent() {
        indent(1);
    }

    default void unindent() {
        indent(-1);
    }

    void emit(CharSequence sequence);

    void newLine();

    void newParagraph();

    void bold(boolean bold);
}
