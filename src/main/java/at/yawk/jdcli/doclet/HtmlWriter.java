package at.yawk.jdcli.doclet;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
class HtmlWriter {
    private static final Set<String> BLOCK_TAGS = new HashSet<>(Arrays.asList("pre", "li", "ul"));

    private final TextEmitter emitter;
    private final Deque<String> openTags = new ArrayDeque<>();
    private int indent = 0;

    public void write(String html) {
        List<String> blocks = Stream.of(html)
                // empty line is a block break
                .flatMap(block -> Arrays.stream(block.split("\n[\\s^\n]*\n")))
                        // <p> is a block break
                .flatMap(block -> Arrays.stream(block.split("<[pP]>")))
                        // trim whitespace
                .map(String::trim)
                        // remove empty blocks
                .filter(block -> !block.isEmpty())
                .collect(Collectors.toList());

        for (int i = 0; i < blocks.size(); i++) {
            if (i != 0) {
                emitter.newParagraph();
            }
            String block = blocks.get(i);
            // trim multi whitespace
            block = block.replaceAll("\\s+", " ");

            // write block
            writeHtmlBlock(block);
        }

        while (decrIndent()) {
            emitter.unindent();
        }
    }

    private void writeHtmlBlock(String html) {
        int lastEnd = 0;
        Matcher tagMatcher = Pattern.compile("(\\s*)<(/)?([^>]+)>(\\s*)").matcher(html);
        while (tagMatcher.find()) {
            writeText(html.substring(lastEnd, tagMatcher.start()));
            String tag = tagMatcher.group(3).toLowerCase();
            if (BLOCK_TAGS.contains(tag)) {
                boolean start = tagMatcher.group(2) == null;
                if (start) {
                    blockTag(tag, true);
                } else if (openTags.contains(tag)) {
                    // close other tags
                    while (true) {
                        String last = openTags.removeLast();
                        blockTag(tag, false);
                        if (last.equals(tag)) { break; }
                    }
                }
            } else {
                // just ignore the element, re-add the whitespace
                emitter.emit(tagMatcher.group(1));
                emitter.emit(tagMatcher.group(4));
            }
            lastEnd = tagMatcher.end();
        }
        writeText(html.substring(lastEnd));
    }

    private void writeText(String text) {
        int lastEnd = 0;
        Matcher instructionMatcher = Pattern.compile("\\{@(\\w+) ([^}]+)}").matcher(text);
        while (instructionMatcher.find()) {
            emitter.emit(text.substring(lastEnd, instructionMatcher.start()));

            String instruction = instructionMatcher.group(1);
            String value = instructionMatcher.group(2);
            switch (instruction.toLowerCase()) {
            case "link":
                emitter.bold(true);
                emitter.emit(value);
                emitter.bold(false);
                break;
            default:
                emitter.emit(value);
                break;
            }

            lastEnd = instructionMatcher.end();
        }
        emitter.emit(text.substring(lastEnd));
    }

    private void blockTag(String qname, boolean start) {
        switch (qname) {
        case "pre":
            if (start) {
                if (!openTags.contains(qname)) {
                    indent();
                    openTags.addLast(qname);
                }
            } else {
                unindent();
            }
            break;
        case "li":
            if (start) {
                boolean previousUnclosed = "li".equals(openTags.peekLast());
                if (previousUnclosed) {
                    // unindent previous
                    unindent();
                }
                emitter.newLine();
                emitter.emit(" - ");
                lineUpIndentWithText();
                if (!previousUnclosed) {
                    openTags.addLast(qname);
                }
            } else {
                unindent();
            }
            break;
        }
    }

    private boolean incrIndent() {
        indent++;
        return true;
    }

    private boolean decrIndent() {
        if (indent <= 0) { return false; }
        indent--;
        return true;
    }

    private void lineUpIndentWithText() {
        if (incrIndent()) {
            emitter.lineUpIndentWithText();
        }
    }

    private void indent() {
        if (incrIndent()) {
            emitter.indent();
        }
    }

    private void unindent() {
        if (decrIndent()) {
            emitter.unindent();
        }
    }
}
