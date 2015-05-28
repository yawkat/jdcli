package at.yawk.jdcli.doclet;

import com.sun.javadoc.*;

/**
 * @author yawkat
 */
public class ClassDocWriter {
    private final TextEmitter emitter;

    public ClassDocWriter(TextEmitter emitter) {
        this.emitter = emitter;
    }

    public void write(ClassDoc classDoc) {
        emitter.emit(classDoc.modifiers() + " ");
        String pkg = classDoc.containingPackage().name();
        emitter.emit(pkg.isEmpty() ? "" : pkg + ".");
        emitter.bold(true);
        emitter.emit(classDoc.qualifiedName().substring(pkg.isEmpty() ? 0 : pkg.length() + 1));
        emitter.bold(false);
        Type sup = classDoc.superclassType();
        if (sup != null) {
            emitter.emit("\n  extends " + sup.qualifiedTypeName());
        }
        for (int i = 0; i < classDoc.interfaceTypes().length; i++) {
            emitter.emit(i == 0 ? "\n  implements " : ",\n             ");
            emitter.emit(classDoc.interfaceTypes()[i].qualifiedTypeName());
        }
        emitter.newParagraph();
        emitter.indent();
        emitComment(classDoc);
        emitter.newParagraph();

        if (classDoc.innerClasses().length != 0) {
            emitTitle("Inner Classes");
            for (ClassDoc inner : classDoc.innerClasses()) {
                emitter.emit(inner.qualifiedName());
                emitter.newParagraph();
            }
        }

        if (classDoc.constructors().length != 0) {
            emitTitle("Constructors");
            for (ConstructorDoc constructor : classDoc.constructors()) {
                writeExecutable(constructor);
            }
        }
        if (classDoc.fields().length != 0) {
            emitTitle("Fields");
            for (FieldDoc field : classDoc.fields()) {
                writeField(field);
            }
        }
        if (classDoc.methods().length != 0) {
            emitTitle("Methods");
            for (MethodDoc method : classDoc.methods()) {
                writeExecutable(method);
            }
        }
        emitter.unindent();
    }

    private void writeField(FieldDoc field) {
        if (!field.modifiers().isEmpty()) {
            emitter.emit(field.modifiers() + " ");
        }
        emitter.emit(field.type().simpleTypeName() + " ");
        emitter.bold(true);
        emitter.emit(field.name());
        emitter.bold(false);
        if (field.constantValueExpression() != null) {
            emitter.emit(" = " + field.constantValueExpression());
        }
        emitter.indent();
        emitComment(field);
        emitter.unindent();
        emitter.newParagraph();
    }

    private void writeExecutable(ExecutableMemberDoc doc) {
        if (!doc.modifiers().isEmpty()) {
            emitter.emit(doc.modifiers() + " ");
        }
        if (doc instanceof MethodDoc) {
            emitter.emit(((MethodDoc) doc).returnType().simpleTypeName() + " ");
            emitter.bold(true);
            emitter.emit(doc.name());
            emitter.bold(false);
        } else {
            emitter.bold(true);
            emitter.emit(doc.containingClass().simpleTypeName());
            emitter.bold(false);
        }
        emitter.emit("(");
        emitter.lineUpIndentWithText();
        for (int i = 0; i < doc.parameters().length; i++) {
            Parameter parameter = doc.parameters()[i];
            if (i != 0) { emitter.emit(", "); }
            emitter.emit(parameter.type().simpleTypeName() + "\u00a0" + parameter.name());
        }
        emitter.emit(")");
        emitter.unindent();
        for (int i = 0; i < doc.thrownExceptionTypes().length; i++) {
            emitter.emit(i == 0 ? "\n  throws " : ",\n         ");
            emitter.emit(doc.thrownExceptionTypes()[i].qualifiedTypeName());
        }
        emitter.indent();
        emitComment(doc);
        emitter.unindent();
        emitter.newParagraph();
    }

    ////// UTIL

    private void emitComment(Doc doc) {
        String text = doc.commentText().trim();
        if (!text.isEmpty()) {
            emitter.newParagraph();
            emitHtml(text);
        }
        if (doc.tags().length != 0) {
            emitter.newParagraph();
            for (Tag tag : doc.tags()) {
                emitTag(tag);
            }
        }
    }

    private void emitTitle(String title) {
        emitter.unindent();
        emitter.bold(true);
        emitter.emit(title.toUpperCase());
        emitter.bold(false);
        emitter.indent();
        emitter.newParagraph();
    }

    private void emitTag(Tag tag) {
        emitter.emit(tag.name() + ' ');
        emitter.lineUpIndentWithText();
        emitter.emit(tag.text());
        emitter.unindent();
    }

    private void emitHtml(String html) {
        new HtmlWriter(emitter).write(html);
    }
}
