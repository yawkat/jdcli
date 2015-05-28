package at.yawk.jdcli.doclet;

import java.util.*;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class MonospacedTextEmitter implements TextEmitter {
    private static final int INDENT_UNIT = 4;

    private static final String BOLD = "\033[1m";
    private static final String UNBOLD = "\033[21m";

    private final Consumer<String> target;
    private final int width;

    private StringBuilder blockBuilder = new StringBuilder();
    private Deque<Integer> indentStack = new ArrayDeque<>();
    private int indent;
    private int nextIndent;
    private boolean bold;

    @Override
    public void indent(int n) {
        finishBlock();
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                indentByChars(INDENT_UNIT);
            }
        } else {
            for (int i = n; i < 0; i++) {
                nextIndent -= indentStack.pollLast();
            }
        }
        indent = nextIndent;
    }

    @Override
    public void lineUpIndentWithText() {
        int width = 0;
        for (int i = 0; i < blockBuilder.length(); i++) {
            int formatLength = formatSequenceLengthAt(blockBuilder, i);
            if (formatLength != 0) {
                i += formatLength - 1;
                continue;
            }
            width++;
        }
        indentByChars(width);
    }

    private void indentByChars(int delta) {
        int newIndent = Math.min(nextIndent + delta, this.width / 2);
        indentStack.addLast(newIndent - nextIndent);
        nextIndent = newIndent;
    }

    private static String indentStr(int len) {
        char[] chars = new char[len];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    private void writeWithIndent(CharSequence seq) {
        target.accept(indentStr(indent) + seq);
        indent = nextIndent;
    }

    private static List<String> wrap(CharSequence input, int maxWidthFirst, int maxWidthOther) {
        List<String> lines = new ArrayList<>();

        StringBuilder lineBuilder = new StringBuilder();
        int width = 0;
        int maxWidth = maxWidthFirst;
        for (int i = 0; i < input.length(); i++) {
            int seqLen = formatSequenceLengthAt(input, i);
            if (seqLen != 0) {
                lineBuilder.append(input.subSequence(i, seqLen + i));
                i += seqLen - 1; // i++ in loop
                continue;
            }

            lineBuilder.append(input.charAt(i));
            width++;

            if (width > maxWidth) {
                int lastSpace = lineBuilder.lastIndexOf(" ");
                if (lastSpace != -1) {
                    i = (i - lineBuilder.length()) + lastSpace + 1;
                    lineBuilder.setLength(lastSpace);
                }
                lines.add(lineBuilder.toString());

                // not first line anymore
                maxWidth = maxWidthOther;
                // clear line
                lineBuilder.setLength(0);
                width = 0;
            }
        }
        if (lineBuilder.length() != 0) {
            lines.add(lineBuilder.toString());
        }

        return lines;
    }

    private static int formatSequenceLengthAt(CharSequence input, int i) {
        for (String f : new String[]{ BOLD, UNBOLD }) {
            if (input.length() - i >= f.length() &&
                f.contentEquals(input.subSequence(i, i + f.length()))) {
                return f.length();
            }
        }
        return 0;
    }

    @Override
    public void emit(CharSequence sequence) {
        int lastWritten = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '\n') {
                blockBuilder.append(sequence.subSequence(lastWritten, i));
                finishBlock();
                lastWritten = i + 1;
            }
        }
        blockBuilder.append(sequence.subSequence(lastWritten, sequence.length()));
        finishPartial();
    }

    @Override
    public void newLine() {
        finishBlock();
    }

    /**
     * Flush all but the last line in blockBuilder to output.
     */
    private void finishPartial() {
        int maxWidthFirst = this.width - indent;
        int maxWidthOther = this.width - nextIndent;
        List<String> wrapped = wrap(blockBuilder, maxWidthFirst, maxWidthOther);
        for (int i = 0; i < wrapped.size() - 1; i++) {
            writeWithIndent(wrapped.get(i));
        }
        blockBuilder.setLength(0);
        if (!wrapped.isEmpty()) {
            // readd last line to builder
            blockBuilder.append(wrapped.get(wrapped.size() - 1));
        }
    }

    /**
     * Flush blockBuilder to output and clear it.
     */
    private void finishBlock() {
        finishPartial();
        if (blockBuilder.length() != 0) {
            writeWithIndent(blockBuilder.toString());
            blockBuilder.setLength(0);
        }
    }

    @Override
    public void newParagraph() {
        finishBlock();
        target.accept(""); // empty line
    }

    @Override
    public void bold(boolean bold) {
        this.bold = bold;
        emit(bold ? BOLD : UNBOLD);
    }
}
