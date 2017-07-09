package io.threesixty.tutorial.commons.text;

import org.apache.commons.text.diff.CommandVisitor;
import org.apache.commons.text.diff.StringsComparator;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestStringDiff {

    @Test
    public void testDifferences() {
        final String left = "Mark";
        final String right = "Marcus";
        final String expected = "Mar<span class='diff_insert'>cus</span><span class='diff_delete'>k</span>";

        MarkupCommandVisitor visitor = new MarkupCommandVisitor();
        new StringsComparator(left, right).getScript().visit(visitor);

        Assert.assertEquals(expected, visitor.getMarkup());
    }

    @Test
    public void testPreInsert() {
        final String left = "Mark";
        final String right = "Mr Marcus";
        final String expected = "<span class='diff_insert'>Mr </span>Mar<span class='diff_insert'>cus</span><span class='diff_delete'>k</span>";

        MarkupCommandVisitor visitor = new MarkupCommandVisitor();
        new StringsComparator(left, right).getScript().visit(visitor);

        Assert.assertEquals(expected, visitor.getMarkup());
    }

    @Test
    public void testPostInsert() {
        final String left = "Mark";
        final String right = "Markus";
        final String expected = "Mark<span class='diff_insert'>us</span>";

        MarkupCommandVisitor visitor = new MarkupCommandVisitor();
        new StringsComparator(left, right).getScript().visit(visitor);

        Assert.assertEquals(expected, visitor.getMarkup());
    }

    @Test
    public void testPostDelete() {
        final String left = "Phillips";
        final String right = "Phillip";
        final String expected = "Phillip<span class='diff_delete'>s</span>";

        MarkupCommandVisitor visitor = new MarkupCommandVisitor();
        new StringsComparator(left, right).getScript().visit(visitor);

        Assert.assertEquals(expected, visitor.getMarkup());
    }

    static class MarkupCommandVisitor implements CommandVisitor<Character> {

        private static final String CLASS_INSERT = "diff_insert";
        private static final String CLASS_DELETE = "diff_delete";
        private static final String SPAN_PREFIX = "<span class='";
        private static final String SPAN_POSTFIX = "'>";
        private static final String SPAN_END = "</span>";

        /**
         * A map containing the command and the CSS class
         */
        private Map<Command, String> markUpCommands = new HashMap<>();
        /**
         * The current command
         */
        private Command command = Command.NONE;
        /**
         * The buffer for the diff text
         */
        private StringBuffer buffer = new StringBuffer();

        public MarkupCommandVisitor() {
            this.markUpCommands.put(Command.DELETE, CLASS_DELETE);
            this.markUpCommands.put(Command.INSERT, CLASS_INSERT);
        }

        public String getMarkup() {
            if (isCurrentMarkupCommand()) {
                visit(Command.END, Optional.empty());
            }

            return this.buffer.toString();
        }

        public void visitInsertCommand(final Character character) {
            visit(Command.INSERT, Optional.of(character));
        }

        public void visitKeepCommand(final Character character) {
            visit(Command.KEEP, Optional.of(character));
        }

        public void visitDeleteCommand(final Character character) {
            visit(Command.DELETE, Optional.of(character));
        }

        private void visit(final Command nextCommand, final Optional<Character> character) {

            if (!isCurrentCommand(nextCommand)) {
                /* Close off the current marked-up command */
                if (isCurrentMarkupCommand()) {
                    buffer.append(SPAN_END);
                }
                /* Open the current marked-up command */
                if (isMarkupCommand(nextCommand)) {
                    buffer.append(SPAN_PREFIX);
                    buffer.append(this.markUpCommands.get(nextCommand));
                    buffer.append(SPAN_POSTFIX);
                }
            }
            character.ifPresent(buffer::append);
            command = nextCommand;
        }

        private boolean isMarkupCommand(final Command command) {
            return this.markUpCommands.containsKey(command);
        }

        private boolean isCurrentMarkupCommand() {
            return this.markUpCommands.containsKey(this.command);
        }

        private boolean isCurrentCommand(final Command command) {
            return this.command == command;
        }
    }

    enum Command {
        NONE, INSERT, KEEP, DELETE, END
    }
}
