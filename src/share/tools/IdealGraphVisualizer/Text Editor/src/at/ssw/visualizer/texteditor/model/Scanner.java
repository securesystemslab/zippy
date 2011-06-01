package at.ssw.visualizer.texteditor.model;

import java.util.BitSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenContextPath;

/**
 * The implementing class must specify the used <code> TokenID</code>s and
 * implement the scanner for the specified text.
 * 
 * @author Alexander Reder
 * @author Christian Wimmer
 */
public abstract class Scanner extends Syntax {
    protected static final int EOF = 0;
    protected static final BitSet DIGIT = charsOf("0123456789");
    protected static final BitSet HEX = charsOf("0123456789abcdefABCDEF");
    protected static final BitSet LETTER = charsOf("_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    protected static final BitSet LETTER_DIGIT = charsOf("0123456789_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    protected static final BitSet LC_LETTER = charsOf("_abcdefghijklmnopqrstuvwxyz");
    protected static final BitSet LC_LETTER_DIGIT = charsOf("0123456789_abcdefghijklmnopqrstuvwxyz");

    protected static BitSet charsOf(String s) {
        BitSet result = new BitSet();
        for (int i = 0; i < s.length(); i++) {
            result.set(s.charAt(i));
        }
        return result;
    }

    protected BitSet whitespace;
    protected char ch;

    public Scanner(String whitespace, TokenContextPath tokenContextPath) {
        this.whitespace = charsOf(whitespace);
        this.whitespace.set(EOF);

        this.tokenContextPath = tokenContextPath;
    }

    public void setText(Document document) {
        try {
            setText(document.getText(0, document.getLength()), 0, document.getLength());
        } catch (BadLocationException ex) {
            Logger logger = Logger.getLogger(Scanner.class.getName());
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void setText(String s, int offset, int length) {
        this.buffer = s.toCharArray();
        this.offset = offset;
        this.tokenOffset = offset;
        this.stopOffset = Math.min(buffer.length, offset + length);
    }

    public String getTokenString() {
        return new String(buffer, getTokenOffset(), getTokenLength());
    }

    public void findTokenBegin(int offset) {
        this.offset = Math.max(offset, 0);
        findTokenBegin();
    }

    /**
     * If offset is in a token this method will read backwards until a
     * whitespace character occurs.
     */
    protected void findTokenBegin() {
        if (offset >= stopOffset) {
            offset = stopOffset - 1;
        }

        if (!whitespace.get(buffer[offset])) {
            while (offset > 0 && !whitespace.get(buffer[offset - 1])) {
                offset--;
            }
        }
        ch = buffer[offset];
        tokenOffset = offset;
    }

    /**
     * Reads the next character.
     */
    protected void readNext() {
        offset++;
        if (offset < stopOffset) {
            ch = buffer[offset];
        } else {
            ch = EOF;
        }
    }

    protected boolean isWhitespace() {
        boolean result = false;
        while (whitespace.get(ch) && ch != EOF) {
            result = true;
            readNext();
        }
        return result;
    }

    /**
     * Read to the next whitespace
     */
    protected void readToWhitespace() {
        do {
            readNext();
        } while (!whitespace.get(ch));
    }

    private boolean readNextOrRestart(boolean result, boolean readNext) {
        if (result) {
            if (readNext) {
                readNext();
            }
        } else {
            offset = tokenOffset;
            ch = buffer[offset];
        }
        return result;
    }

    protected boolean isKeyword(Set<String> keywords) {
        int beginOffset = offset;
        int endOffset = offset;
        while (endOffset < stopOffset && !whitespace.get(buffer[endOffset])) {
            endOffset++;
        }
        String word = new String(buffer, beginOffset, endOffset - beginOffset);
        if (!keywords.contains(word)) {
            return false;
        }

        offset = endOffset - 1;
        readNext();
        return true;
    }

    protected boolean expectEnd() {
        return readNextOrRestart(whitespace.get(ch), false);
    }

    protected boolean expectEnd(char expected) {
        return readNextOrRestart(ch == expected, false) && offset + 1 < stopOffset && whitespace.get(buffer[offset + 1]);
    }

    protected boolean expectChar(char expected) {
        return readNextOrRestart(ch == expected, true);
    }

    protected boolean expectChar(BitSet expected) {
        return readNextOrRestart(expected.get(ch), true);
    }

    protected boolean expectChars(BitSet expected) {
        while (expected.get(ch)) {
            readNext();
        }
        return true;
    }

    protected boolean skipUntil(char expected) {
        while (ch != expected && !whitespace.get(ch)) {
            readNext();
        }
        return expectChar(expected);
    }

    protected boolean beforeChar(char before) {
        int curOffset = offset - 1;
        while (curOffset >= 0 && buffer[curOffset] != before && whitespace.get(buffer[curOffset])) {
            curOffset--;
        }
        return curOffset >= 0 && buffer[curOffset] == before;
    }

    protected boolean beforeChars(BitSet before) {
        int curOffset = offset - 1;
        while (curOffset >= 0 && !before.get(buffer[curOffset]) && whitespace.get(buffer[curOffset])) {
            curOffset--;
        }
        return curOffset >= 0 && before.get(buffer[curOffset]);
    }
}
