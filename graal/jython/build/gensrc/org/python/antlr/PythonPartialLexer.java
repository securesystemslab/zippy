// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g 2013-06-19 17:22:42

package org.python.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class PythonPartialLexer extends Lexer {
    public static final int SLASHEQUAL=54;
    public static final int BACKQUOTE=85;
    public static final int STAR=48;
    public static final int CIRCUMFLEXEQUAL=58;
    public static final int WHILE=39;
    public static final int TRIAPOS=93;
    public static final int ORELSE=34;
    public static final int GREATEREQUAL=67;
    public static final int COMPLEX=89;
    public static final int NOT=32;
    public static final int EXCEPT=21;
    public static final int EOF=-1;
    public static final int BREAK=15;
    public static final int PASS=35;
    public static final int LEADING_WS=8;
    public static final int NOTEQUAL=70;
    public static final int MINUSEQUAL=52;
    public static final int VBAR=71;
    public static final int RPAREN=44;
    public static final int IMPORT=28;
    public static final int NAME=9;
    public static final int GREATER=65;
    public static final int DOUBLESTAREQUAL=61;
    public static final int RETURN=37;
    public static final int LESS=64;
    public static final int RAISE=36;
    public static final int COMMENT=96;
    public static final int RBRACK=82;
    public static final int LCURLY=83;
    public static final int INT=86;
    public static final int DELETE=19;
    public static final int RIGHTSHIFT=63;
    public static final int ASSERT=14;
    public static final int TRY=38;
    public static final int DOUBLESLASHEQUAL=62;
    public static final int ELIF=20;
    public static final int WS=98;
    public static final int VBAREQUAL=57;
    public static final int OR=33;
    public static final int LONGINT=87;
    public static final int FROM=24;
    public static final int PERCENTEQUAL=55;
    public static final int LESSEQUAL=68;
    public static final int DOUBLESLASH=79;
    public static final int CLASS=16;
    public static final int CONTINUED_LINE=97;
    public static final int LBRACK=81;
    public static final int DEF=18;
    public static final int DOUBLESTAR=49;
    public static final int ESC=95;
    public static final int DIGITS=91;
    public static final int Exponent=92;
    public static final int FOR=25;
    public static final int DEDENT=5;
    public static final int FLOAT=88;
    public static final int AND=12;
    public static final int RIGHTSHIFTEQUAL=60;
    public static final int INDENT=4;
    public static final int LPAREN=43;
    public static final int IF=27;
    public static final int PLUSEQUAL=51;
    public static final int AT=42;
    public static final int AS=13;
    public static final int SLASH=77;
    public static final int IN=29;
    public static final int CONTINUE=17;
    public static final int COMMA=47;
    public static final int IS=30;
    public static final int AMPER=73;
    public static final int EQUAL=66;
    public static final int YIELD=41;
    public static final int TILDE=80;
    public static final int LEFTSHIFTEQUAL=59;
    public static final int LEFTSHIFT=74;
    public static final int PLUS=75;
    public static final int LAMBDA=31;
    public static final int DOT=10;
    public static final int TRISTRINGPART=99;
    public static final int STRINGPART=100;
    public static final int WITH=40;
    public static final int PERCENT=78;
    public static final int EXEC=22;
    public static final int MINUS=76;
    public static final int SEMI=50;
    public static final int PRINT=11;
    public static final int TRIQUOTE=94;
    public static final int COLON=45;
    public static final int TRAILBACKSLASH=6;
    public static final int AMPEREQUAL=56;
    public static final int NEWLINE=7;
    public static final int FINALLY=23;
    public static final int RCURLY=84;
    public static final int ASSIGN=46;
    public static final int GLOBAL=26;
    public static final int STAREQUAL=53;
    public static final int CIRCUMFLEX=72;
    public static final int STRING=90;
    public static final int ALT_NOTEQUAL=69;

    /** Handles context-sensitive lexing of implicit line joining such as
     *  the case where newline is ignored in cases like this:
     *  a = [3,
     *       4]
     */

    //For use in partial parsing.
    public boolean eofWhileNested = false;
    public boolean partial = false;

    int implicitLineJoiningLevel = 0;
    int startPos=-1;

    //If you want to use another error recovery mechanism change this
    //and the same one in the parser.
    private ErrorHandler errorHandler;

        public void setErrorHandler(ErrorHandler eh) {
            this.errorHandler = eh;
        }

        /**
         *  Taken directly from antlr's Lexer.java -- needs to be re-integrated every time
         *  we upgrade from Antlr (need to consider a Lexer subclass, though the issue would
         *  remain).
         */
        public Token nextToken() {
            startPos = getCharPositionInLine();
            while (true) {
                state.token = null;
                state.channel = Token.DEFAULT_CHANNEL;
                state.tokenStartCharIndex = input.index();
                state.tokenStartCharPositionInLine = input.getCharPositionInLine();
                state.tokenStartLine = input.getLine();
                state.text = null;
                if ( input.LA(1)==CharStream.EOF ) {
                    if (implicitLineJoiningLevel > 0) {
                        eofWhileNested = true;
                    }
                    return Token.EOF_TOKEN;
                }
                try {
                    mTokens();
                    if ( state.token==null ) {
                        emit();
                    }
                    else if ( state.token==Token.SKIP_TOKEN ) {
                        continue;
                    }
                    return state.token;
                } catch (NoViableAltException nva) {
                    errorHandler.reportError(this, nva);
                    errorHandler.recover(this, nva); // throw out current char and try again
                } catch (FailedPredicateException fp) {
                    //XXX: added this for failed STRINGPART -- the FailedPredicateException
                    //     hides a NoViableAltException.  This should be the only
                    //     FailedPredicateException that gets thrown by the lexer.
                    errorHandler.reportError(this, fp);
                    errorHandler.recover(this, fp); // throw out current char and try again
                } catch (RecognitionException re) {
                    errorHandler.reportError(this, re);
                    // match() routine has already called recover()
                }
            }
        }


    // delegates
    // delegators

    public PythonPartialLexer() {;} 
    public PythonPartialLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public PythonPartialLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g"; }

    // $ANTLR start "AS"
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:877:11: ( 'as' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:877:13: 'as'
            {
            match("as"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AS"

    // $ANTLR start "ASSERT"
    public final void mASSERT() throws RecognitionException {
        try {
            int _type = ASSERT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:878:11: ( 'assert' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:878:13: 'assert'
            {
            match("assert"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ASSERT"

    // $ANTLR start "BREAK"
    public final void mBREAK() throws RecognitionException {
        try {
            int _type = BREAK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:879:11: ( 'break' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:879:13: 'break'
            {
            match("break"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BREAK"

    // $ANTLR start "CLASS"
    public final void mCLASS() throws RecognitionException {
        try {
            int _type = CLASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:880:11: ( 'class' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:880:13: 'class'
            {
            match("class"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CLASS"

    // $ANTLR start "CONTINUE"
    public final void mCONTINUE() throws RecognitionException {
        try {
            int _type = CONTINUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:881:11: ( 'continue' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:881:13: 'continue'
            {
            match("continue"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CONTINUE"

    // $ANTLR start "DEF"
    public final void mDEF() throws RecognitionException {
        try {
            int _type = DEF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:882:11: ( 'def' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:882:13: 'def'
            {
            match("def"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DEF"

    // $ANTLR start "DELETE"
    public final void mDELETE() throws RecognitionException {
        try {
            int _type = DELETE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:883:11: ( 'del' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:883:13: 'del'
            {
            match("del"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DELETE"

    // $ANTLR start "ELIF"
    public final void mELIF() throws RecognitionException {
        try {
            int _type = ELIF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:884:11: ( 'elif' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:884:13: 'elif'
            {
            match("elif"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ELIF"

    // $ANTLR start "EXCEPT"
    public final void mEXCEPT() throws RecognitionException {
        try {
            int _type = EXCEPT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:885:11: ( 'except' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:885:13: 'except'
            {
            match("except"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXCEPT"

    // $ANTLR start "EXEC"
    public final void mEXEC() throws RecognitionException {
        try {
            int _type = EXEC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:886:11: ( 'exec' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:886:13: 'exec'
            {
            match("exec"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXEC"

    // $ANTLR start "FINALLY"
    public final void mFINALLY() throws RecognitionException {
        try {
            int _type = FINALLY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:887:11: ( 'finally' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:887:13: 'finally'
            {
            match("finally"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FINALLY"

    // $ANTLR start "FROM"
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:888:11: ( 'from' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:888:13: 'from'
            {
            match("from"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FROM"

    // $ANTLR start "FOR"
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:889:11: ( 'for' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:889:13: 'for'
            {
            match("for"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FOR"

    // $ANTLR start "GLOBAL"
    public final void mGLOBAL() throws RecognitionException {
        try {
            int _type = GLOBAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:890:11: ( 'global' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:890:13: 'global'
            {
            match("global"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GLOBAL"

    // $ANTLR start "IF"
    public final void mIF() throws RecognitionException {
        try {
            int _type = IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:891:11: ( 'if' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:891:13: 'if'
            {
            match("if"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IF"

    // $ANTLR start "IMPORT"
    public final void mIMPORT() throws RecognitionException {
        try {
            int _type = IMPORT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:892:11: ( 'import' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:892:13: 'import'
            {
            match("import"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMPORT"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:893:11: ( 'in' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:893:13: 'in'
            {
            match("in"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "IS"
    public final void mIS() throws RecognitionException {
        try {
            int _type = IS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:894:11: ( 'is' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:894:13: 'is'
            {
            match("is"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IS"

    // $ANTLR start "LAMBDA"
    public final void mLAMBDA() throws RecognitionException {
        try {
            int _type = LAMBDA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:895:11: ( 'lambda' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:895:13: 'lambda'
            {
            match("lambda"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LAMBDA"

    // $ANTLR start "ORELSE"
    public final void mORELSE() throws RecognitionException {
        try {
            int _type = ORELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:896:11: ( 'else' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:896:13: 'else'
            {
            match("else"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ORELSE"

    // $ANTLR start "PASS"
    public final void mPASS() throws RecognitionException {
        try {
            int _type = PASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:897:11: ( 'pass' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:897:13: 'pass'
            {
            match("pass"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PASS"

    // $ANTLR start "PRINT"
    public final void mPRINT() throws RecognitionException {
        try {
            int _type = PRINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:898:11: ( 'print' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:898:13: 'print'
            {
            match("print"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PRINT"

    // $ANTLR start "RAISE"
    public final void mRAISE() throws RecognitionException {
        try {
            int _type = RAISE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:899:11: ( 'raise' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:899:13: 'raise'
            {
            match("raise"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RAISE"

    // $ANTLR start "RETURN"
    public final void mRETURN() throws RecognitionException {
        try {
            int _type = RETURN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:900:11: ( 'return' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:900:13: 'return'
            {
            match("return"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RETURN"

    // $ANTLR start "TRY"
    public final void mTRY() throws RecognitionException {
        try {
            int _type = TRY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:901:11: ( 'try' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:901:13: 'try'
            {
            match("try"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRY"

    // $ANTLR start "WHILE"
    public final void mWHILE() throws RecognitionException {
        try {
            int _type = WHILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:902:11: ( 'while' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:902:13: 'while'
            {
            match("while"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHILE"

    // $ANTLR start "WITH"
    public final void mWITH() throws RecognitionException {
        try {
            int _type = WITH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:903:11: ( 'with' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:903:13: 'with'
            {
            match("with"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WITH"

    // $ANTLR start "YIELD"
    public final void mYIELD() throws RecognitionException {
        try {
            int _type = YIELD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:904:11: ( 'yield' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:904:13: 'yield'
            {
            match("yield"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "YIELD"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:906:11: ( '(' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:906:13: '('
            {
            match('('); 
            implicitLineJoiningLevel++;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:908:11: ( ')' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:908:13: ')'
            {
            match(')'); 
            implicitLineJoiningLevel--;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "LBRACK"
    public final void mLBRACK() throws RecognitionException {
        try {
            int _type = LBRACK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:910:11: ( '[' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:910:13: '['
            {
            match('['); 
            implicitLineJoiningLevel++;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACK"

    // $ANTLR start "RBRACK"
    public final void mRBRACK() throws RecognitionException {
        try {
            int _type = RBRACK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:912:11: ( ']' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:912:13: ']'
            {
            match(']'); 
            implicitLineJoiningLevel--;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACK"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:914:11: ( ':' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:914:13: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:916:10: ( ',' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:916:12: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "SEMI"
    public final void mSEMI() throws RecognitionException {
        try {
            int _type = SEMI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:918:9: ( ';' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:918:11: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMI"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:920:9: ( '+' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:920:11: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:922:10: ( '-' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:922:12: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "STAR"
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:924:9: ( '*' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:924:11: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STAR"

    // $ANTLR start "SLASH"
    public final void mSLASH() throws RecognitionException {
        try {
            int _type = SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:926:10: ( '/' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:926:12: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SLASH"

    // $ANTLR start "VBAR"
    public final void mVBAR() throws RecognitionException {
        try {
            int _type = VBAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:928:9: ( '|' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:928:11: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VBAR"

    // $ANTLR start "AMPER"
    public final void mAMPER() throws RecognitionException {
        try {
            int _type = AMPER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:930:10: ( '&' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:930:12: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AMPER"

    // $ANTLR start "LESS"
    public final void mLESS() throws RecognitionException {
        try {
            int _type = LESS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:932:9: ( '<' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:932:11: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESS"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:934:12: ( '>' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:934:14: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER"

    // $ANTLR start "ASSIGN"
    public final void mASSIGN() throws RecognitionException {
        try {
            int _type = ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:936:11: ( '=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:936:13: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ASSIGN"

    // $ANTLR start "PERCENT"
    public final void mPERCENT() throws RecognitionException {
        try {
            int _type = PERCENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:938:12: ( '%' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:938:14: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PERCENT"

    // $ANTLR start "BACKQUOTE"
    public final void mBACKQUOTE() throws RecognitionException {
        try {
            int _type = BACKQUOTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:940:14: ( '`' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:940:16: '`'
            {
            match('`'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BACKQUOTE"

    // $ANTLR start "LCURLY"
    public final void mLCURLY() throws RecognitionException {
        try {
            int _type = LCURLY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:942:11: ( '{' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:942:13: '{'
            {
            match('{'); 
            implicitLineJoiningLevel++;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LCURLY"

    // $ANTLR start "RCURLY"
    public final void mRCURLY() throws RecognitionException {
        try {
            int _type = RCURLY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:944:11: ( '}' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:944:13: '}'
            {
            match('}'); 
            implicitLineJoiningLevel--;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RCURLY"

    // $ANTLR start "CIRCUMFLEX"
    public final void mCIRCUMFLEX() throws RecognitionException {
        try {
            int _type = CIRCUMFLEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:946:15: ( '^' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:946:17: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CIRCUMFLEX"

    // $ANTLR start "TILDE"
    public final void mTILDE() throws RecognitionException {
        try {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:948:10: ( '~' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:948:12: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TILDE"

    // $ANTLR start "EQUAL"
    public final void mEQUAL() throws RecognitionException {
        try {
            int _type = EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:950:10: ( '==' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:950:12: '=='
            {
            match("=="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUAL"

    // $ANTLR start "NOTEQUAL"
    public final void mNOTEQUAL() throws RecognitionException {
        try {
            int _type = NOTEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:952:13: ( '!=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:952:15: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOTEQUAL"

    // $ANTLR start "ALT_NOTEQUAL"
    public final void mALT_NOTEQUAL() throws RecognitionException {
        try {
            int _type = ALT_NOTEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:954:13: ( '<>' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:954:15: '<>'
            {
            match("<>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ALT_NOTEQUAL"

    // $ANTLR start "LESSEQUAL"
    public final void mLESSEQUAL() throws RecognitionException {
        try {
            int _type = LESSEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:956:14: ( '<=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:956:16: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSEQUAL"

    // $ANTLR start "LEFTSHIFT"
    public final void mLEFTSHIFT() throws RecognitionException {
        try {
            int _type = LEFTSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:958:14: ( '<<' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:958:16: '<<'
            {
            match("<<"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LEFTSHIFT"

    // $ANTLR start "GREATEREQUAL"
    public final void mGREATEREQUAL() throws RecognitionException {
        try {
            int _type = GREATEREQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:960:17: ( '>=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:960:19: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATEREQUAL"

    // $ANTLR start "RIGHTSHIFT"
    public final void mRIGHTSHIFT() throws RecognitionException {
        try {
            int _type = RIGHTSHIFT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:962:15: ( '>>' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:962:17: '>>'
            {
            match(">>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RIGHTSHIFT"

    // $ANTLR start "PLUSEQUAL"
    public final void mPLUSEQUAL() throws RecognitionException {
        try {
            int _type = PLUSEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:964:14: ( '+=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:964:16: '+='
            {
            match("+="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUSEQUAL"

    // $ANTLR start "MINUSEQUAL"
    public final void mMINUSEQUAL() throws RecognitionException {
        try {
            int _type = MINUSEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:966:15: ( '-=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:966:17: '-='
            {
            match("-="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUSEQUAL"

    // $ANTLR start "DOUBLESTAR"
    public final void mDOUBLESTAR() throws RecognitionException {
        try {
            int _type = DOUBLESTAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:968:15: ( '**' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:968:17: '**'
            {
            match("**"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOUBLESTAR"

    // $ANTLR start "STAREQUAL"
    public final void mSTAREQUAL() throws RecognitionException {
        try {
            int _type = STAREQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:970:14: ( '*=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:970:16: '*='
            {
            match("*="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STAREQUAL"

    // $ANTLR start "DOUBLESLASH"
    public final void mDOUBLESLASH() throws RecognitionException {
        try {
            int _type = DOUBLESLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:972:16: ( '//' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:972:18: '//'
            {
            match("//"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOUBLESLASH"

    // $ANTLR start "SLASHEQUAL"
    public final void mSLASHEQUAL() throws RecognitionException {
        try {
            int _type = SLASHEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:974:15: ( '/=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:974:17: '/='
            {
            match("/="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SLASHEQUAL"

    // $ANTLR start "VBAREQUAL"
    public final void mVBAREQUAL() throws RecognitionException {
        try {
            int _type = VBAREQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:976:14: ( '|=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:976:16: '|='
            {
            match("|="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VBAREQUAL"

    // $ANTLR start "PERCENTEQUAL"
    public final void mPERCENTEQUAL() throws RecognitionException {
        try {
            int _type = PERCENTEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:978:17: ( '%=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:978:19: '%='
            {
            match("%="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PERCENTEQUAL"

    // $ANTLR start "AMPEREQUAL"
    public final void mAMPEREQUAL() throws RecognitionException {
        try {
            int _type = AMPEREQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:980:15: ( '&=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:980:17: '&='
            {
            match("&="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AMPEREQUAL"

    // $ANTLR start "CIRCUMFLEXEQUAL"
    public final void mCIRCUMFLEXEQUAL() throws RecognitionException {
        try {
            int _type = CIRCUMFLEXEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:982:20: ( '^=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:982:22: '^='
            {
            match("^="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CIRCUMFLEXEQUAL"

    // $ANTLR start "LEFTSHIFTEQUAL"
    public final void mLEFTSHIFTEQUAL() throws RecognitionException {
        try {
            int _type = LEFTSHIFTEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:984:19: ( '<<=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:984:21: '<<='
            {
            match("<<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LEFTSHIFTEQUAL"

    // $ANTLR start "RIGHTSHIFTEQUAL"
    public final void mRIGHTSHIFTEQUAL() throws RecognitionException {
        try {
            int _type = RIGHTSHIFTEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:986:20: ( '>>=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:986:22: '>>='
            {
            match(">>="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RIGHTSHIFTEQUAL"

    // $ANTLR start "DOUBLESTAREQUAL"
    public final void mDOUBLESTAREQUAL() throws RecognitionException {
        try {
            int _type = DOUBLESTAREQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:988:20: ( '**=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:988:22: '**='
            {
            match("**="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOUBLESTAREQUAL"

    // $ANTLR start "DOUBLESLASHEQUAL"
    public final void mDOUBLESLASHEQUAL() throws RecognitionException {
        try {
            int _type = DOUBLESLASHEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:990:21: ( '//=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:990:23: '//='
            {
            match("//="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOUBLESLASHEQUAL"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:992:5: ( '.' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:992:7: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "AT"
    public final void mAT() throws RecognitionException {
        try {
            int _type = AT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:994:4: ( '@' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:994:6: '@'
            {
            match('@'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AT"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:996:5: ( 'and' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:996:7: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:998:4: ( 'or' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:998:6: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1000:5: ( 'not' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1000:7: 'not'
            {
            match("not"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1003:5: ( '.' DIGITS ( Exponent )? | DIGITS '.' Exponent | DIGITS ( '.' ( DIGITS ( Exponent )? )? | Exponent ) )
            int alt5=3;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1003:9: '.' DIGITS ( Exponent )?
                    {
                    match('.'); 
                    mDIGITS(); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1003:20: ( Exponent )?
                    int alt1=2;
                    int LA1_0 = input.LA(1);

                    if ( (LA1_0=='E'||LA1_0=='e') ) {
                        alt1=1;
                    }
                    switch (alt1) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1003:21: Exponent
                            {
                            mExponent(); 

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1004:9: DIGITS '.' Exponent
                    {
                    mDIGITS(); 
                    match('.'); 
                    mExponent(); 

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1005:9: DIGITS ( '.' ( DIGITS ( Exponent )? )? | Exponent )
                    {
                    mDIGITS(); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1005:16: ( '.' ( DIGITS ( Exponent )? )? | Exponent )
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0=='.') ) {
                        alt4=1;
                    }
                    else if ( (LA4_0=='E'||LA4_0=='e') ) {
                        alt4=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 4, 0, input);

                        throw nvae;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1005:17: '.' ( DIGITS ( Exponent )? )?
                            {
                            match('.'); 
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1005:21: ( DIGITS ( Exponent )? )?
                            int alt3=2;
                            int LA3_0 = input.LA(1);

                            if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                                alt3=1;
                            }
                            switch (alt3) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1005:22: DIGITS ( Exponent )?
                                    {
                                    mDIGITS(); 
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1005:29: ( Exponent )?
                                    int alt2=2;
                                    int LA2_0 = input.LA(1);

                                    if ( (LA2_0=='E'||LA2_0=='e') ) {
                                        alt2=1;
                                    }
                                    switch (alt2) {
                                        case 1 :
                                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1005:30: Exponent
                                            {
                                            mExponent(); 

                                            }
                                            break;

                                    }


                                    }
                                    break;

                            }


                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1005:45: Exponent
                            {
                            mExponent(); 

                            }
                            break;

                    }


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "LONGINT"
    public final void mLONGINT() throws RecognitionException {
        try {
            int _type = LONGINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1009:5: ( INT ( 'l' | 'L' ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1009:9: INT ( 'l' | 'L' )
            {
            mINT(); 
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LONGINT"

    // $ANTLR start "Exponent"
    public final void mExponent() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1014:5: ( ( 'e' | 'E' ) ( '+' | '-' )? DIGITS )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1014:10: ( 'e' | 'E' ) ( '+' | '-' )? DIGITS
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1014:22: ( '+' | '-' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='+'||LA6_0=='-') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            mDIGITS(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "Exponent"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1017:5: ( '0' ( 'x' | 'X' ) ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+ | '0' ( '0' .. '7' )* | '1' .. '9' ( DIGITS )* )
            int alt10=3;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='0') ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1=='X'||LA10_1=='x') ) {
                    alt10=1;
                }
                else {
                    alt10=2;}
            }
            else if ( ((LA10_0>='1' && LA10_0<='9')) ) {
                alt10=3;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1018:9: '0' ( 'x' | 'X' ) ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
                    {
                    match('0'); 
                    if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1018:25: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( ((LA7_0>='0' && LA7_0<='9')||(LA7_0>='A' && LA7_0<='F')||(LA7_0>='a' && LA7_0<='f')) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
                    	    {
                    	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt7 >= 1 ) break loop7;
                                EarlyExitException eee =
                                    new EarlyExitException(7, input);
                                throw eee;
                        }
                        cnt7++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1020:9: '0' ( '0' .. '7' )*
                    {
                    match('0'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1020:14: ( '0' .. '7' )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0>='0' && LA8_0<='7')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1020:16: '0' .. '7'
                    	    {
                    	    matchRange('0','7'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop8;
                        }
                    } while (true);


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1021:9: '1' .. '9' ( DIGITS )*
                    {
                    matchRange('1','9'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1021:18: ( DIGITS )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( ((LA9_0>='0' && LA9_0<='9')) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1021:18: DIGITS
                    	    {
                    	    mDIGITS(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "COMPLEX"
    public final void mCOMPLEX() throws RecognitionException {
        try {
            int _type = COMPLEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1025:5: ( ( DIGITS )+ ( 'j' | 'J' ) | FLOAT ( 'j' | 'J' ) )
            int alt12=2;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1025:9: ( DIGITS )+ ( 'j' | 'J' )
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1025:9: ( DIGITS )+
                    int cnt11=0;
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0>='0' && LA11_0<='9')) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1025:9: DIGITS
                    	    {
                    	    mDIGITS(); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt11 >= 1 ) break loop11;
                                EarlyExitException eee =
                                    new EarlyExitException(11, input);
                                throw eee;
                        }
                        cnt11++;
                    } while (true);

                    if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1026:9: FLOAT ( 'j' | 'J' )
                    {
                    mFLOAT(); 
                    if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPLEX"

    // $ANTLR start "DIGITS"
    public final void mDIGITS() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1030:8: ( ( '0' .. '9' )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1030:10: ( '0' .. '9' )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1030:10: ( '0' .. '9' )+
            int cnt13=0;
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( ((LA13_0>='0' && LA13_0<='9')) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1030:12: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt13 >= 1 ) break loop13;
                        EarlyExitException eee =
                            new EarlyExitException(13, input);
                        throw eee;
                }
                cnt13++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGITS"

    // $ANTLR start "NAME"
    public final void mNAME() throws RecognitionException {
        try {
            int _type = NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1032:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1032:10: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1033:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( ((LA14_0>='0' && LA14_0<='9')||(LA14_0>='A' && LA14_0<='Z')||LA14_0=='_'||(LA14_0>='a' && LA14_0<='z')) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAME"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:5: ( ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )? ( '\\'\\'\\'' ( options {greedy=false; } : TRIAPOS )* '\\'\\'\\'' | '\"\"\"' ( options {greedy=false; } : TRIQUOTE )* '\"\"\"' | '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* '\"' | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* '\\'' ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:9: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )? ( '\\'\\'\\'' ( options {greedy=false; } : TRIAPOS )* '\\'\\'\\'' | '\"\"\"' ( options {greedy=false; } : TRIQUOTE )* '\"\"\"' | '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* '\"' | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* '\\'' )
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:9: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )?
            int alt15=9;
            alt15 = dfa15.predict(input);
            switch (alt15) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:10: 'r'
                    {
                    match('r'); 

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:14: 'u'
                    {
                    match('u'); 

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:18: 'ur'
                    {
                    match("ur"); 


                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:23: 'R'
                    {
                    match('R'); 

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:27: 'U'
                    {
                    match('U'); 

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:31: 'UR'
                    {
                    match("UR"); 


                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:36: 'uR'
                    {
                    match("uR"); 


                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1040:41: 'Ur'
                    {
                    match("Ur"); 


                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1041:9: ( '\\'\\'\\'' ( options {greedy=false; } : TRIAPOS )* '\\'\\'\\'' | '\"\"\"' ( options {greedy=false; } : TRIQUOTE )* '\"\"\"' | '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* '\"' | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* '\\'' )
            int alt20=4;
            int LA20_0 = input.LA(1);

            if ( (LA20_0=='\'') ) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1=='\'') ) {
                    int LA20_3 = input.LA(3);

                    if ( (LA20_3=='\'') ) {
                        alt20=1;
                    }
                    else {
                        alt20=4;}
                }
                else if ( ((LA20_1>='\u0000' && LA20_1<='\t')||(LA20_1>='\u000B' && LA20_1<='&')||(LA20_1>='(' && LA20_1<='\uFFFF')) ) {
                    alt20=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA20_0=='\"') ) {
                int LA20_2 = input.LA(2);

                if ( (LA20_2=='\"') ) {
                    int LA20_5 = input.LA(3);

                    if ( (LA20_5=='\"') ) {
                        alt20=2;
                    }
                    else {
                        alt20=3;}
                }
                else if ( ((LA20_2>='\u0000' && LA20_2<='\t')||(LA20_2>='\u000B' && LA20_2<='!')||(LA20_2>='#' && LA20_2<='\uFFFF')) ) {
                    alt20=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1041:13: '\\'\\'\\'' ( options {greedy=false; } : TRIAPOS )* '\\'\\'\\''
                    {
                    match("'''"); 

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1041:22: ( options {greedy=false; } : TRIAPOS )*
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( (LA16_0=='\'') ) {
                            int LA16_1 = input.LA(2);

                            if ( (LA16_1=='\'') ) {
                                int LA16_3 = input.LA(3);

                                if ( (LA16_3=='\'') ) {
                                    alt16=2;
                                }
                                else if ( ((LA16_3>='\u0000' && LA16_3<='&')||(LA16_3>='(' && LA16_3<='\uFFFF')) ) {
                                    alt16=1;
                                }


                            }
                            else if ( ((LA16_1>='\u0000' && LA16_1<='&')||(LA16_1>='(' && LA16_1<='\uFFFF')) ) {
                                alt16=1;
                            }


                        }
                        else if ( ((LA16_0>='\u0000' && LA16_0<='&')||(LA16_0>='(' && LA16_0<='\uFFFF')) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1041:47: TRIAPOS
                    	    {
                    	    mTRIAPOS(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop16;
                        }
                    } while (true);

                    match("'''"); 


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1042:13: '\"\"\"' ( options {greedy=false; } : TRIQUOTE )* '\"\"\"'
                    {
                    match("\"\"\""); 

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1042:19: ( options {greedy=false; } : TRIQUOTE )*
                    loop17:
                    do {
                        int alt17=2;
                        int LA17_0 = input.LA(1);

                        if ( (LA17_0=='\"') ) {
                            int LA17_1 = input.LA(2);

                            if ( (LA17_1=='\"') ) {
                                int LA17_3 = input.LA(3);

                                if ( (LA17_3=='\"') ) {
                                    alt17=2;
                                }
                                else if ( ((LA17_3>='\u0000' && LA17_3<='!')||(LA17_3>='#' && LA17_3<='\uFFFF')) ) {
                                    alt17=1;
                                }


                            }
                            else if ( ((LA17_1>='\u0000' && LA17_1<='!')||(LA17_1>='#' && LA17_1<='\uFFFF')) ) {
                                alt17=1;
                            }


                        }
                        else if ( ((LA17_0>='\u0000' && LA17_0<='!')||(LA17_0>='#' && LA17_0<='\uFFFF')) ) {
                            alt17=1;
                        }


                        switch (alt17) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1042:44: TRIQUOTE
                    	    {
                    	    mTRIQUOTE(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop17;
                        }
                    } while (true);

                    match("\"\"\""); 


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1043:13: '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* '\"'
                    {
                    match('\"'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1043:17: ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )*
                    loop18:
                    do {
                        int alt18=3;
                        int LA18_0 = input.LA(1);

                        if ( (LA18_0=='\\') ) {
                            alt18=1;
                        }
                        else if ( ((LA18_0>='\u0000' && LA18_0<='\t')||(LA18_0>='\u000B' && LA18_0<='!')||(LA18_0>='#' && LA18_0<='[')||(LA18_0>=']' && LA18_0<='\uFFFF')) ) {
                            alt18=2;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1043:18: ESC
                    	    {
                    	    mESC(); 

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1043:22: ~ ( '\\\\' | '\\n' | '\"' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop18;
                        }
                    } while (true);

                    match('\"'); 

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1044:13: '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* '\\''
                    {
                    match('\''); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1044:18: ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )*
                    loop19:
                    do {
                        int alt19=3;
                        int LA19_0 = input.LA(1);

                        if ( (LA19_0=='\\') ) {
                            alt19=1;
                        }
                        else if ( ((LA19_0>='\u0000' && LA19_0<='\t')||(LA19_0>='\u000B' && LA19_0<='&')||(LA19_0>='(' && LA19_0<='[')||(LA19_0>=']' && LA19_0<='\uFFFF')) ) {
                            alt19=2;
                        }


                        switch (alt19) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1044:19: ESC
                    	    {
                    	    mESC(); 

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1044:23: ~ ( '\\\\' | '\\n' | '\\'' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop19;
                        }
                    } while (true);

                    match('\''); 

                    }
                    break;

            }


                       if (state.tokenStartLine != input.getLine()) {
                           state.tokenStartLine = input.getLine();
                           state.tokenStartCharPositionInLine = -2;
                       }
                    

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "TRISTRINGPART"
    public final void mTRISTRINGPART() throws RecognitionException {
        try {
            int _type = TRISTRINGPART;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:5: ( ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )? ( '\\'\\'\\'' (~ ( '\\'\\'\\'' ) )* | '\"\"\"' (~ ( '\"\"\"' ) )* ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:7: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )? ( '\\'\\'\\'' (~ ( '\\'\\'\\'' ) )* | '\"\"\"' (~ ( '\"\"\"' ) )* )
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:7: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )?
            int alt21=9;
            alt21 = dfa21.predict(input);
            switch (alt21) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:8: 'r'
                    {
                    match('r'); 

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:12: 'u'
                    {
                    match('u'); 

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:16: 'ur'
                    {
                    match("ur"); 


                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:21: 'R'
                    {
                    match('R'); 

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:25: 'U'
                    {
                    match('U'); 

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:29: 'UR'
                    {
                    match("UR"); 


                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:34: 'uR'
                    {
                    match("uR"); 


                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1054:39: 'Ur'
                    {
                    match("Ur"); 


                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1055:9: ( '\\'\\'\\'' (~ ( '\\'\\'\\'' ) )* | '\"\"\"' (~ ( '\"\"\"' ) )* )
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0=='\'') ) {
                alt24=1;
            }
            else if ( (LA24_0=='\"') ) {
                alt24=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1055:13: '\\'\\'\\'' (~ ( '\\'\\'\\'' ) )*
                    {
                    match("'''"); 

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1055:22: (~ ( '\\'\\'\\'' ) )*
                    loop22:
                    do {
                        int alt22=2;
                        int LA22_0 = input.LA(1);

                        if ( ((LA22_0>='\u0000' && LA22_0<='\uFFFF')) ) {
                            alt22=1;
                        }


                        switch (alt22) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1055:22: ~ ( '\\'\\'\\'' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop22;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1056:13: '\"\"\"' (~ ( '\"\"\"' ) )*
                    {
                    match("\"\"\""); 

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1056:19: (~ ( '\"\"\"' ) )*
                    loop23:
                    do {
                        int alt23=2;
                        int LA23_0 = input.LA(1);

                        if ( ((LA23_0>='\u0000' && LA23_0<='\uFFFF')) ) {
                            alt23=1;
                        }


                        switch (alt23) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1056:19: ~ ( '\"\"\"' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop23;
                        }
                    } while (true);


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRISTRINGPART"

    // $ANTLR start "STRINGPART"
    public final void mSTRINGPART() throws RecognitionException {
        try {
            int _type = STRINGPART;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:5: ( ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )? ( '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* CONTINUED_LINE | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* CONTINUED_LINE ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:7: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )? ( '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* CONTINUED_LINE | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* CONTINUED_LINE )
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:7: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )?
            int alt25=9;
            alt25 = dfa25.predict(input);
            switch (alt25) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:8: 'r'
                    {
                    match('r'); 

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:12: 'u'
                    {
                    match('u'); 

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:16: 'ur'
                    {
                    match("ur"); 


                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:21: 'R'
                    {
                    match('R'); 

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:25: 'U'
                    {
                    match('U'); 

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:29: 'UR'
                    {
                    match("UR"); 


                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:34: 'uR'
                    {
                    match("uR"); 


                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1061:39: 'Ur'
                    {
                    match("Ur"); 


                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1062:9: ( '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* CONTINUED_LINE | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* CONTINUED_LINE )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0=='\"') ) {
                alt28=1;
            }
            else if ( (LA28_0=='\'') ) {
                alt28=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1062:13: '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* CONTINUED_LINE
                    {
                    match('\"'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1062:17: ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )*
                    loop26:
                    do {
                        int alt26=3;
                        alt26 = dfa26.predict(input);
                        switch (alt26) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1062:18: ESC
                    	    {
                    	    mESC(); 

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1062:22: ~ ( '\\\\' | '\\n' | '\"' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);

                    mCONTINUED_LINE(); 

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1063:13: '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* CONTINUED_LINE
                    {
                    match('\''); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1063:18: ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )*
                    loop27:
                    do {
                        int alt27=3;
                        alt27 = dfa27.predict(input);
                        switch (alt27) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1063:19: ESC
                    	    {
                    	    mESC(); 

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1063:23: ~ ( '\\\\' | '\\n' | '\\'' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop27;
                        }
                    } while (true);

                    mCONTINUED_LINE(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRINGPART"

    // $ANTLR start "TRIQUOTE"
    public final void mTRIQUOTE() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:5: ( ( '\"' )? ( '\"' )? ( ESC | ~ ( '\\\\' | '\"' ) )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:7: ( '\"' )? ( '\"' )? ( ESC | ~ ( '\\\\' | '\"' ) )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:7: ( '\"' )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0=='\"') ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:7: '\"'
                    {
                    match('\"'); 

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:12: ( '\"' )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0=='\"') ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:12: '\"'
                    {
                    match('\"'); 

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:17: ( ESC | ~ ( '\\\\' | '\"' ) )+
            int cnt31=0;
            loop31:
            do {
                int alt31=3;
                int LA31_0 = input.LA(1);

                if ( (LA31_0=='\\') ) {
                    alt31=1;
                }
                else if ( ((LA31_0>='\u0000' && LA31_0<='!')||(LA31_0>='#' && LA31_0<='[')||(LA31_0>=']' && LA31_0<='\uFFFF')) ) {
                    alt31=2;
                }


                switch (alt31) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:18: ESC
            	    {
            	    mESC(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1071:22: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt31 >= 1 ) break loop31;
                        EarlyExitException eee =
                            new EarlyExitException(31, input);
                        throw eee;
                }
                cnt31++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "TRIQUOTE"

    // $ANTLR start "TRIAPOS"
    public final void mTRIAPOS() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:5: ( ( '\\'' )? ( '\\'' )? ( ESC | ~ ( '\\\\' | '\\'' ) )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:7: ( '\\'' )? ( '\\'' )? ( ESC | ~ ( '\\\\' | '\\'' ) )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:7: ( '\\'' )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0=='\'') ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:7: '\\''
                    {
                    match('\''); 

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:13: ( '\\'' )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0=='\'') ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:13: '\\''
                    {
                    match('\''); 

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:19: ( ESC | ~ ( '\\\\' | '\\'' ) )+
            int cnt34=0;
            loop34:
            do {
                int alt34=3;
                int LA34_0 = input.LA(1);

                if ( (LA34_0=='\\') ) {
                    alt34=1;
                }
                else if ( ((LA34_0>='\u0000' && LA34_0<='&')||(LA34_0>='(' && LA34_0<='[')||(LA34_0>=']' && LA34_0<='\uFFFF')) ) {
                    alt34=2;
                }


                switch (alt34) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:20: ESC
            	    {
            	    mESC(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1077:24: ~ ( '\\\\' | '\\'' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt34 >= 1 ) break loop34;
                        EarlyExitException eee =
                            new EarlyExitException(34, input);
                        throw eee;
                }
                cnt34++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "TRIAPOS"

    // $ANTLR start "ESC"
    public final void mESC() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1082:5: ( '\\\\' . )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1082:10: '\\\\' .
            {
            match('\\'); 
            matchAny(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "ESC"

    // $ANTLR start "CONTINUED_LINE"
    public final void mCONTINUED_LINE() throws RecognitionException {
        try {
            int _type = CONTINUED_LINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            Token nl=null;


                boolean extraNewlines = false;

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1093:5: ( '\\\\' ( '\\r' )? '\\n' ( ' ' | '\\t' )* ( COMMENT | nl= NEWLINE | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1093:10: '\\\\' ( '\\r' )? '\\n' ( ' ' | '\\t' )* ( COMMENT | nl= NEWLINE | )
            {
            match('\\'); 
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1093:15: ( '\\r' )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0=='\r') ) {
                alt35=1;
            }
            switch (alt35) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1093:16: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }

            match('\n'); 
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1093:28: ( ' ' | '\\t' )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0=='\t'||LA36_0==' ') ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);

             _channel=HIDDEN; 
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1094:10: ( COMMENT | nl= NEWLINE | )
            int alt37=3;
            int LA37_0 = input.LA(1);

            if ( (LA37_0=='\t'||LA37_0==' ') && ((startPos==0))) {
                alt37=1;
            }
            else if ( (LA37_0=='#') ) {
                alt37=1;
            }
            else if ( (LA37_0=='\n'||(LA37_0>='\f' && LA37_0<='\r')) ) {
                alt37=2;
            }
            else {
                alt37=3;}
            switch (alt37) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1094:12: COMMENT
                    {
                    mCOMMENT(); 

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1095:12: nl= NEWLINE
                    {
                    int nlStart1929 = getCharIndex();
                    mNEWLINE(); 
                    nl = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, nlStart1929, getCharIndex()-1);

                                   extraNewlines = true;
                               

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1100:10: 
                    {
                    }
                    break;

            }


                           if (input.LA(1) == -1) {
                               if (extraNewlines) {
                                   throw new ParseException("invalid syntax");
                               }
                               emit(new CommonToken(TRAILBACKSLASH,"\\"));
                           }
                       

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CONTINUED_LINE"

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1117:5: ( ( ( '\\u000C' )? ( '\\r' )? '\\n' )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1117:9: ( ( '\\u000C' )? ( '\\r' )? '\\n' )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1117:9: ( ( '\\u000C' )? ( '\\r' )? '\\n' )+
            int cnt40=0;
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( (LA40_0=='\n'||(LA40_0>='\f' && LA40_0<='\r')) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1117:10: ( '\\u000C' )? ( '\\r' )? '\\n'
            	    {
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1117:10: ( '\\u000C' )?
            	    int alt38=2;
            	    int LA38_0 = input.LA(1);

            	    if ( (LA38_0=='\f') ) {
            	        alt38=1;
            	    }
            	    switch (alt38) {
            	        case 1 :
            	            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1117:11: '\\u000C'
            	            {
            	            match('\f'); 

            	            }
            	            break;

            	    }

            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1117:21: ( '\\r' )?
            	    int alt39=2;
            	    int LA39_0 = input.LA(1);

            	    if ( (LA39_0=='\r') ) {
            	        alt39=1;
            	    }
            	    switch (alt39) {
            	        case 1 :
            	            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1117:22: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;

            	    }

            	    match('\n'); 

            	    }
            	    break;

            	default :
            	    if ( cnt40 >= 1 ) break loop40;
                        EarlyExitException eee =
                            new EarlyExitException(40, input);
                        throw eee;
                }
                cnt40++;
            } while (true);


                     if ( startPos==0 || implicitLineJoiningLevel>0 )
                        _channel=HIDDEN;
                    

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NEWLINE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1123:5: ({...}? => ( ' ' | '\\t' | '\\u000C' )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1123:10: {...}? => ( ' ' | '\\t' | '\\u000C' )+
            {
            if ( !((startPos>0)) ) {
                throw new FailedPredicateException(input, "WS", "startPos>0");
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1123:26: ( ' ' | '\\t' | '\\u000C' )+
            int cnt41=0;
            loop41:
            do {
                int alt41=2;
                int LA41_0 = input.LA(1);

                if ( (LA41_0=='\t'||LA41_0=='\f'||LA41_0==' ') ) {
                    alt41=1;
                }


                switch (alt41) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)=='\f'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt41 >= 1 ) break loop41;
                        EarlyExitException eee =
                            new EarlyExitException(41, input);
                        throw eee;
                }
                cnt41++;
            } while (true);

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "LEADING_WS"
    public final void mLEADING_WS() throws RecognitionException {
        try {
            int _type = LEADING_WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;

                int spaces = 0;
                int newlines = 0;

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1137:5: ({...}? => ({...}? ( ' ' | '\\t' )+ | ( ' ' | '\\t' )+ ( ( '\\r' )? '\\n' )* ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1137:9: {...}? => ({...}? ( ' ' | '\\t' )+ | ( ' ' | '\\t' )+ ( ( '\\r' )? '\\n' )* )
            {
            if ( !((startPos==0)) ) {
                throw new FailedPredicateException(input, "LEADING_WS", "startPos==0");
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1138:9: ({...}? ( ' ' | '\\t' )+ | ( ' ' | '\\t' )+ ( ( '\\r' )? '\\n' )* )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==' ') ) {
                int LA46_1 = input.LA(2);

                if ( ((implicitLineJoiningLevel>0)) ) {
                    alt46=1;
                }
                else if ( (true) ) {
                    alt46=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 46, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA46_0=='\t') ) {
                int LA46_2 = input.LA(2);

                if ( ((implicitLineJoiningLevel>0)) ) {
                    alt46=1;
                }
                else if ( (true) ) {
                    alt46=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 46, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1138:13: {...}? ( ' ' | '\\t' )+
                    {
                    if ( !((implicitLineJoiningLevel>0)) ) {
                        throw new FailedPredicateException(input, "LEADING_WS", "implicitLineJoiningLevel>0");
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1138:43: ( ' ' | '\\t' )+
                    int cnt42=0;
                    loop42:
                    do {
                        int alt42=2;
                        int LA42_0 = input.LA(1);

                        if ( (LA42_0=='\t'||LA42_0==' ') ) {
                            alt42=1;
                        }


                        switch (alt42) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
                    	    {
                    	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt42 >= 1 ) break loop42;
                                EarlyExitException eee =
                                    new EarlyExitException(42, input);
                                throw eee;
                        }
                        cnt42++;
                    } while (true);

                    _channel=HIDDEN;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1139:14: ( ' ' | '\\t' )+ ( ( '\\r' )? '\\n' )*
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1139:14: ( ' ' | '\\t' )+
                    int cnt43=0;
                    loop43:
                    do {
                        int alt43=3;
                        int LA43_0 = input.LA(1);

                        if ( (LA43_0==' ') ) {
                            alt43=1;
                        }
                        else if ( (LA43_0=='\t') ) {
                            alt43=2;
                        }


                        switch (alt43) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1139:20: ' '
                    	    {
                    	    match(' '); 
                    	     spaces++; 

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1140:19: '\\t'
                    	    {
                    	    match('\t'); 
                    	     spaces += 8; spaces -= (spaces % 8); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt43 >= 1 ) break loop43;
                                EarlyExitException eee =
                                    new EarlyExitException(43, input);
                                throw eee;
                        }
                        cnt43++;
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1142:14: ( ( '\\r' )? '\\n' )*
                    loop45:
                    do {
                        int alt45=2;
                        int LA45_0 = input.LA(1);

                        if ( (LA45_0=='\n'||LA45_0=='\r') ) {
                            alt45=1;
                        }


                        switch (alt45) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1142:16: ( '\\r' )? '\\n'
                    	    {
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1142:16: ( '\\r' )?
                    	    int alt44=2;
                    	    int LA44_0 = input.LA(1);

                    	    if ( (LA44_0=='\r') ) {
                    	        alt44=1;
                    	    }
                    	    switch (alt44) {
                    	        case 1 :
                    	            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1142:17: '\\r'
                    	            {
                    	            match('\r'); 

                    	            }
                    	            break;

                    	    }

                    	    match('\n'); 
                    	    newlines++; 

                    	    }
                    	    break;

                    	default :
                    	    break loop45;
                        }
                    } while (true);


                                       if (input.LA(1) != -1 || newlines == 0) {
                                           // make a string of n spaces where n is column number - 1
                                           char[] indentation = new char[spaces];
                                           for (int i=0; i<spaces; i++) {
                                               indentation[i] = ' ';
                                           }
                                           CommonToken c = new CommonToken(LEADING_WS,new String(indentation));
                                           c.setLine(input.getLine());
                                           c.setCharPositionInLine(input.getCharPositionInLine());
                                           c.setStartIndex(input.index() - 1);
                                           c.setStopIndex(input.index() - 1);
                                           emit(c);
                                           // kill trailing newline if present and then ignore
                                           if (newlines != 0) {
                                               if (state.token!=null) {
                                                   state.token.setChannel(HIDDEN);
                                               } else {
                                                   _channel=HIDDEN;
                                               }
                                           }
                                       } else {
                                           // make a string of n newlines
                                           char[] nls = new char[newlines];
                                           for (int i=0; i<newlines; i++) {
                                               nls[i] = '\n';
                                           }
                                           CommonToken c = new CommonToken(NEWLINE,new String(nls));
                                           c.setLine(input.getLine());
                                           c.setCharPositionInLine(input.getCharPositionInLine());
                                           c.setStartIndex(input.index() - 1);
                                           c.setStopIndex(input.index() - 1);
                                           emit(c);
                                       }
                                    

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LEADING_WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;

                _channel=HIDDEN;

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1202:5: ({...}? => ( ' ' | '\\t' )* '#' (~ '\\n' )* ( '\\n' )+ | '#' (~ '\\n' )* )
            int alt51=2;
            alt51 = dfa51.predict(input);
            switch (alt51) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1202:10: {...}? => ( ' ' | '\\t' )* '#' (~ '\\n' )* ( '\\n' )+
                    {
                    if ( !((startPos==0)) ) {
                        throw new FailedPredicateException(input, "COMMENT", "startPos==0");
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1202:27: ( ' ' | '\\t' )*
                    loop47:
                    do {
                        int alt47=2;
                        int LA47_0 = input.LA(1);

                        if ( (LA47_0=='\t'||LA47_0==' ') ) {
                            alt47=1;
                        }


                        switch (alt47) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
                    	    {
                    	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop47;
                        }
                    } while (true);

                    match('#'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1202:43: (~ '\\n' )*
                    loop48:
                    do {
                        int alt48=2;
                        int LA48_0 = input.LA(1);

                        if ( ((LA48_0>='\u0000' && LA48_0<='\t')||(LA48_0>='\u000B' && LA48_0<='\uFFFF')) ) {
                            alt48=1;
                        }


                        switch (alt48) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1202:44: ~ '\\n'
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop48;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1202:52: ( '\\n' )+
                    int cnt49=0;
                    loop49:
                    do {
                        int alt49=2;
                        int LA49_0 = input.LA(1);

                        if ( (LA49_0=='\n') ) {
                            alt49=1;
                        }


                        switch (alt49) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1202:52: '\\n'
                    	    {
                    	    match('\n'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt49 >= 1 ) break loop49;
                                EarlyExitException eee =
                                    new EarlyExitException(49, input);
                                throw eee;
                        }
                        cnt49++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1203:10: '#' (~ '\\n' )*
                    {
                    match('#'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1203:14: (~ '\\n' )*
                    loop50:
                    do {
                        int alt50=2;
                        int LA50_0 = input.LA(1);

                        if ( ((LA50_0>='\u0000' && LA50_0<='\t')||(LA50_0>='\u000B' && LA50_0<='\uFFFF')) ) {
                            alt50=1;
                        }


                        switch (alt50) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1203:15: ~ '\\n'
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop50;
                        }
                    } while (true);


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    public void mTokens() throws RecognitionException {
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:8: ( AS | ASSERT | BREAK | CLASS | CONTINUE | DEF | DELETE | ELIF | EXCEPT | EXEC | FINALLY | FROM | FOR | GLOBAL | IF | IMPORT | IN | IS | LAMBDA | ORELSE | PASS | PRINT | RAISE | RETURN | TRY | WHILE | WITH | YIELD | LPAREN | RPAREN | LBRACK | RBRACK | COLON | COMMA | SEMI | PLUS | MINUS | STAR | SLASH | VBAR | AMPER | LESS | GREATER | ASSIGN | PERCENT | BACKQUOTE | LCURLY | RCURLY | CIRCUMFLEX | TILDE | EQUAL | NOTEQUAL | ALT_NOTEQUAL | LESSEQUAL | LEFTSHIFT | GREATEREQUAL | RIGHTSHIFT | PLUSEQUAL | MINUSEQUAL | DOUBLESTAR | STAREQUAL | DOUBLESLASH | SLASHEQUAL | VBAREQUAL | PERCENTEQUAL | AMPEREQUAL | CIRCUMFLEXEQUAL | LEFTSHIFTEQUAL | RIGHTSHIFTEQUAL | DOUBLESTAREQUAL | DOUBLESLASHEQUAL | DOT | AT | AND | OR | NOT | FLOAT | LONGINT | INT | COMPLEX | NAME | STRING | TRISTRINGPART | STRINGPART | CONTINUED_LINE | NEWLINE | WS | LEADING_WS | COMMENT )
        int alt52=89;
        alt52 = dfa52.predict(input);
        switch (alt52) {
            case 1 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:10: AS
                {
                mAS(); 

                }
                break;
            case 2 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:13: ASSERT
                {
                mASSERT(); 

                }
                break;
            case 3 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:20: BREAK
                {
                mBREAK(); 

                }
                break;
            case 4 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:26: CLASS
                {
                mCLASS(); 

                }
                break;
            case 5 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:32: CONTINUE
                {
                mCONTINUE(); 

                }
                break;
            case 6 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:41: DEF
                {
                mDEF(); 

                }
                break;
            case 7 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:45: DELETE
                {
                mDELETE(); 

                }
                break;
            case 8 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:52: ELIF
                {
                mELIF(); 

                }
                break;
            case 9 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:57: EXCEPT
                {
                mEXCEPT(); 

                }
                break;
            case 10 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:64: EXEC
                {
                mEXEC(); 

                }
                break;
            case 11 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:69: FINALLY
                {
                mFINALLY(); 

                }
                break;
            case 12 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:77: FROM
                {
                mFROM(); 

                }
                break;
            case 13 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:82: FOR
                {
                mFOR(); 

                }
                break;
            case 14 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:86: GLOBAL
                {
                mGLOBAL(); 

                }
                break;
            case 15 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:93: IF
                {
                mIF(); 

                }
                break;
            case 16 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:96: IMPORT
                {
                mIMPORT(); 

                }
                break;
            case 17 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:103: IN
                {
                mIN(); 

                }
                break;
            case 18 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:106: IS
                {
                mIS(); 

                }
                break;
            case 19 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:109: LAMBDA
                {
                mLAMBDA(); 

                }
                break;
            case 20 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:116: ORELSE
                {
                mORELSE(); 

                }
                break;
            case 21 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:123: PASS
                {
                mPASS(); 

                }
                break;
            case 22 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:128: PRINT
                {
                mPRINT(); 

                }
                break;
            case 23 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:134: RAISE
                {
                mRAISE(); 

                }
                break;
            case 24 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:140: RETURN
                {
                mRETURN(); 

                }
                break;
            case 25 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:147: TRY
                {
                mTRY(); 

                }
                break;
            case 26 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:151: WHILE
                {
                mWHILE(); 

                }
                break;
            case 27 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:157: WITH
                {
                mWITH(); 

                }
                break;
            case 28 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:162: YIELD
                {
                mYIELD(); 

                }
                break;
            case 29 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:168: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 30 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:175: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 31 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:182: LBRACK
                {
                mLBRACK(); 

                }
                break;
            case 32 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:189: RBRACK
                {
                mRBRACK(); 

                }
                break;
            case 33 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:196: COLON
                {
                mCOLON(); 

                }
                break;
            case 34 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:202: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 35 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:208: SEMI
                {
                mSEMI(); 

                }
                break;
            case 36 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:213: PLUS
                {
                mPLUS(); 

                }
                break;
            case 37 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:218: MINUS
                {
                mMINUS(); 

                }
                break;
            case 38 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:224: STAR
                {
                mSTAR(); 

                }
                break;
            case 39 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:229: SLASH
                {
                mSLASH(); 

                }
                break;
            case 40 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:235: VBAR
                {
                mVBAR(); 

                }
                break;
            case 41 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:240: AMPER
                {
                mAMPER(); 

                }
                break;
            case 42 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:246: LESS
                {
                mLESS(); 

                }
                break;
            case 43 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:251: GREATER
                {
                mGREATER(); 

                }
                break;
            case 44 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:259: ASSIGN
                {
                mASSIGN(); 

                }
                break;
            case 45 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:266: PERCENT
                {
                mPERCENT(); 

                }
                break;
            case 46 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:274: BACKQUOTE
                {
                mBACKQUOTE(); 

                }
                break;
            case 47 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:284: LCURLY
                {
                mLCURLY(); 

                }
                break;
            case 48 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:291: RCURLY
                {
                mRCURLY(); 

                }
                break;
            case 49 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:298: CIRCUMFLEX
                {
                mCIRCUMFLEX(); 

                }
                break;
            case 50 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:309: TILDE
                {
                mTILDE(); 

                }
                break;
            case 51 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:315: EQUAL
                {
                mEQUAL(); 

                }
                break;
            case 52 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:321: NOTEQUAL
                {
                mNOTEQUAL(); 

                }
                break;
            case 53 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:330: ALT_NOTEQUAL
                {
                mALT_NOTEQUAL(); 

                }
                break;
            case 54 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:343: LESSEQUAL
                {
                mLESSEQUAL(); 

                }
                break;
            case 55 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:353: LEFTSHIFT
                {
                mLEFTSHIFT(); 

                }
                break;
            case 56 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:363: GREATEREQUAL
                {
                mGREATEREQUAL(); 

                }
                break;
            case 57 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:376: RIGHTSHIFT
                {
                mRIGHTSHIFT(); 

                }
                break;
            case 58 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:387: PLUSEQUAL
                {
                mPLUSEQUAL(); 

                }
                break;
            case 59 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:397: MINUSEQUAL
                {
                mMINUSEQUAL(); 

                }
                break;
            case 60 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:408: DOUBLESTAR
                {
                mDOUBLESTAR(); 

                }
                break;
            case 61 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:419: STAREQUAL
                {
                mSTAREQUAL(); 

                }
                break;
            case 62 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:429: DOUBLESLASH
                {
                mDOUBLESLASH(); 

                }
                break;
            case 63 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:441: SLASHEQUAL
                {
                mSLASHEQUAL(); 

                }
                break;
            case 64 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:452: VBAREQUAL
                {
                mVBAREQUAL(); 

                }
                break;
            case 65 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:462: PERCENTEQUAL
                {
                mPERCENTEQUAL(); 

                }
                break;
            case 66 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:475: AMPEREQUAL
                {
                mAMPEREQUAL(); 

                }
                break;
            case 67 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:486: CIRCUMFLEXEQUAL
                {
                mCIRCUMFLEXEQUAL(); 

                }
                break;
            case 68 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:502: LEFTSHIFTEQUAL
                {
                mLEFTSHIFTEQUAL(); 

                }
                break;
            case 69 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:517: RIGHTSHIFTEQUAL
                {
                mRIGHTSHIFTEQUAL(); 

                }
                break;
            case 70 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:533: DOUBLESTAREQUAL
                {
                mDOUBLESTAREQUAL(); 

                }
                break;
            case 71 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:549: DOUBLESLASHEQUAL
                {
                mDOUBLESLASHEQUAL(); 

                }
                break;
            case 72 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:566: DOT
                {
                mDOT(); 

                }
                break;
            case 73 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:570: AT
                {
                mAT(); 

                }
                break;
            case 74 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:573: AND
                {
                mAND(); 

                }
                break;
            case 75 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:577: OR
                {
                mOR(); 

                }
                break;
            case 76 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:580: NOT
                {
                mNOT(); 

                }
                break;
            case 77 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:584: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 78 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:590: LONGINT
                {
                mLONGINT(); 

                }
                break;
            case 79 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:598: INT
                {
                mINT(); 

                }
                break;
            case 80 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:602: COMPLEX
                {
                mCOMPLEX(); 

                }
                break;
            case 81 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:610: NAME
                {
                mNAME(); 

                }
                break;
            case 82 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:615: STRING
                {
                mSTRING(); 

                }
                break;
            case 83 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:622: TRISTRINGPART
                {
                mTRISTRINGPART(); 

                }
                break;
            case 84 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:636: STRINGPART
                {
                mSTRINGPART(); 

                }
                break;
            case 85 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:647: CONTINUED_LINE
                {
                mCONTINUED_LINE(); 

                }
                break;
            case 86 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:662: NEWLINE
                {
                mNEWLINE(); 

                }
                break;
            case 87 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:670: WS
                {
                mWS(); 

                }
                break;
            case 88 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:673: LEADING_WS
                {
                mLEADING_WS(); 

                }
                break;
            case 89 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:1:684: COMMENT
                {
                mCOMMENT(); 

                }
                break;

        }

    }


    protected DFA5 dfa5 = new DFA5(this);
    protected DFA12 dfa12 = new DFA12(this);
    protected DFA15 dfa15 = new DFA15(this);
    protected DFA21 dfa21 = new DFA21(this);
    protected DFA25 dfa25 = new DFA25(this);
    protected DFA26 dfa26 = new DFA26(this);
    protected DFA27 dfa27 = new DFA27(this);
    protected DFA51 dfa51 = new DFA51(this);
    protected DFA52 dfa52 = new DFA52(this);
    static final String DFA5_eotS =
        "\3\uffff\1\4\2\uffff";
    static final String DFA5_eofS =
        "\6\uffff";
    static final String DFA5_minS =
        "\1\56\1\uffff\1\56\1\105\2\uffff";
    static final String DFA5_maxS =
        "\1\71\1\uffff\2\145\2\uffff";
    static final String DFA5_acceptS =
        "\1\uffff\1\1\2\uffff\1\3\1\2";
    static final String DFA5_specialS =
        "\6\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\1\1\uffff\12\2",
            "",
            "\1\3\1\uffff\12\2\13\uffff\1\4\37\uffff\1\4",
            "\1\5\37\uffff\1\5",
            "",
            ""
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "1002:1: FLOAT : ( '.' DIGITS ( Exponent )? | DIGITS '.' Exponent | DIGITS ( '.' ( DIGITS ( Exponent )? )? | Exponent ) );";
        }
    }
    static final String DFA12_eotS =
        "\4\uffff";
    static final String DFA12_eofS =
        "\4\uffff";
    static final String DFA12_minS =
        "\2\56\2\uffff";
    static final String DFA12_maxS =
        "\1\71\1\152\2\uffff";
    static final String DFA12_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA12_specialS =
        "\4\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\2\1\uffff\12\1",
            "\1\2\1\uffff\12\1\13\uffff\1\2\4\uffff\1\3\32\uffff\1\2\4\uffff"+
            "\1\3",
            "",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1024:1: COMPLEX : ( ( DIGITS )+ ( 'j' | 'J' ) | FLOAT ( 'j' | 'J' ) );";
        }
    }
    static final String DFA15_eotS =
        "\14\uffff";
    static final String DFA15_eofS =
        "\14\uffff";
    static final String DFA15_minS =
        "\1\42\1\uffff\1\42\1\uffff\1\42\7\uffff";
    static final String DFA15_maxS =
        "\1\165\1\uffff\1\162\1\uffff\1\162\7\uffff";
    static final String DFA15_acceptS =
        "\1\uffff\1\1\1\uffff\1\4\1\uffff\1\11\1\3\1\7\1\2\1\6\1\10\1\5";
    static final String DFA15_specialS =
        "\14\uffff}>";
    static final String[] DFA15_transitionS = {
            "\1\5\4\uffff\1\5\52\uffff\1\3\2\uffff\1\4\34\uffff\1\1\2\uffff"+
            "\1\2",
            "",
            "\1\10\4\uffff\1\10\52\uffff\1\7\37\uffff\1\6",
            "",
            "\1\13\4\uffff\1\13\52\uffff\1\11\37\uffff\1\12",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA15_eot = DFA.unpackEncodedString(DFA15_eotS);
    static final short[] DFA15_eof = DFA.unpackEncodedString(DFA15_eofS);
    static final char[] DFA15_min = DFA.unpackEncodedStringToUnsignedChars(DFA15_minS);
    static final char[] DFA15_max = DFA.unpackEncodedStringToUnsignedChars(DFA15_maxS);
    static final short[] DFA15_accept = DFA.unpackEncodedString(DFA15_acceptS);
    static final short[] DFA15_special = DFA.unpackEncodedString(DFA15_specialS);
    static final short[][] DFA15_transition;

    static {
        int numStates = DFA15_transitionS.length;
        DFA15_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA15_transition[i] = DFA.unpackEncodedString(DFA15_transitionS[i]);
        }
    }

    class DFA15 extends DFA {

        public DFA15(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 15;
            this.eot = DFA15_eot;
            this.eof = DFA15_eof;
            this.min = DFA15_min;
            this.max = DFA15_max;
            this.accept = DFA15_accept;
            this.special = DFA15_special;
            this.transition = DFA15_transition;
        }
        public String getDescription() {
            return "1040:9: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )?";
        }
    }
    static final String DFA21_eotS =
        "\14\uffff";
    static final String DFA21_eofS =
        "\14\uffff";
    static final String DFA21_minS =
        "\1\42\1\uffff\1\42\1\uffff\1\42\7\uffff";
    static final String DFA21_maxS =
        "\1\165\1\uffff\1\162\1\uffff\1\162\7\uffff";
    static final String DFA21_acceptS =
        "\1\uffff\1\1\1\uffff\1\4\1\uffff\1\11\1\3\1\7\1\2\1\6\1\10\1\5";
    static final String DFA21_specialS =
        "\14\uffff}>";
    static final String[] DFA21_transitionS = {
            "\1\5\4\uffff\1\5\52\uffff\1\3\2\uffff\1\4\34\uffff\1\1\2\uffff"+
            "\1\2",
            "",
            "\1\10\4\uffff\1\10\52\uffff\1\7\37\uffff\1\6",
            "",
            "\1\13\4\uffff\1\13\52\uffff\1\11\37\uffff\1\12",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA21_eot = DFA.unpackEncodedString(DFA21_eotS);
    static final short[] DFA21_eof = DFA.unpackEncodedString(DFA21_eofS);
    static final char[] DFA21_min = DFA.unpackEncodedStringToUnsignedChars(DFA21_minS);
    static final char[] DFA21_max = DFA.unpackEncodedStringToUnsignedChars(DFA21_maxS);
    static final short[] DFA21_accept = DFA.unpackEncodedString(DFA21_acceptS);
    static final short[] DFA21_special = DFA.unpackEncodedString(DFA21_specialS);
    static final short[][] DFA21_transition;

    static {
        int numStates = DFA21_transitionS.length;
        DFA21_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA21_transition[i] = DFA.unpackEncodedString(DFA21_transitionS[i]);
        }
    }

    class DFA21 extends DFA {

        public DFA21(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 21;
            this.eot = DFA21_eot;
            this.eof = DFA21_eof;
            this.min = DFA21_min;
            this.max = DFA21_max;
            this.accept = DFA21_accept;
            this.special = DFA21_special;
            this.transition = DFA21_transition;
        }
        public String getDescription() {
            return "1054:7: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )?";
        }
    }
    static final String DFA25_eotS =
        "\14\uffff";
    static final String DFA25_eofS =
        "\14\uffff";
    static final String DFA25_minS =
        "\1\42\1\uffff\1\42\1\uffff\1\42\7\uffff";
    static final String DFA25_maxS =
        "\1\165\1\uffff\1\162\1\uffff\1\162\7\uffff";
    static final String DFA25_acceptS =
        "\1\uffff\1\1\1\uffff\1\4\1\uffff\1\11\1\3\1\7\1\2\1\6\1\10\1\5";
    static final String DFA25_specialS =
        "\14\uffff}>";
    static final String[] DFA25_transitionS = {
            "\1\5\4\uffff\1\5\52\uffff\1\3\2\uffff\1\4\34\uffff\1\1\2\uffff"+
            "\1\2",
            "",
            "\1\10\4\uffff\1\10\52\uffff\1\7\37\uffff\1\6",
            "",
            "\1\13\4\uffff\1\13\52\uffff\1\11\37\uffff\1\12",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA25_eot = DFA.unpackEncodedString(DFA25_eotS);
    static final short[] DFA25_eof = DFA.unpackEncodedString(DFA25_eofS);
    static final char[] DFA25_min = DFA.unpackEncodedStringToUnsignedChars(DFA25_minS);
    static final char[] DFA25_max = DFA.unpackEncodedStringToUnsignedChars(DFA25_maxS);
    static final short[] DFA25_accept = DFA.unpackEncodedString(DFA25_acceptS);
    static final short[] DFA25_special = DFA.unpackEncodedString(DFA25_specialS);
    static final short[][] DFA25_transition;

    static {
        int numStates = DFA25_transitionS.length;
        DFA25_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA25_transition[i] = DFA.unpackEncodedString(DFA25_transitionS[i]);
        }
    }

    class DFA25 extends DFA {

        public DFA25(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 25;
            this.eot = DFA25_eot;
            this.eof = DFA25_eof;
            this.min = DFA25_min;
            this.max = DFA25_max;
            this.accept = DFA25_accept;
            this.special = DFA25_special;
            this.transition = DFA25_transition;
        }
        public String getDescription() {
            return "1061:7: ( 'r' | 'u' | 'ur' | 'R' | 'U' | 'UR' | 'uR' | 'Ur' )?";
        }
    }
    static final String DFA26_eotS =
        "\4\uffff\1\6\2\uffff\2\6\2\uffff\2\6\1\uffff\2\6\1\uffff";
    static final String DFA26_eofS =
        "\21\uffff";
    static final String DFA26_minS =
        "\2\0\1\uffff\2\0\2\uffff\6\0\1\uffff\2\0\1\uffff";
    static final String DFA26_maxS =
        "\2\uffff\1\uffff\2\uffff\2\uffff\6\uffff\1\uffff\2\uffff\1\uffff";
    static final String DFA26_acceptS =
        "\2\uffff\1\2\2\uffff\1\1\1\3\6\uffff\1\1\2\uffff\1\1";
    static final String DFA26_specialS =
        "\1\0\1\3\1\uffff\1\13\1\7\2\uffff\1\4\1\1\1\2\1\11\1\5\1\12\1\uffff"+
        "\1\10\1\6\1\uffff}>";
    static final String[] DFA26_transitionS = {
            "\12\2\1\uffff\27\2\1\uffff\71\2\1\1\uffa3\2",
            "\12\5\1\4\2\5\1\3\ufff2\5",
            "",
            "\12\5\1\6\27\5\1\uffff\uffdd\5",
            "\11\5\1\7\1\uffff\1\5\1\11\1\12\22\5\1\7\1\5\1\uffff\1\10\uffdc"+
            "\5",
            "",
            "",
            "\11\5\1\7\1\uffff\1\5\1\11\1\12\22\5\1\7\1\5\1\uffff\1\10\uffdc"+
            "\5",
            "\12\14\1\uffff\27\14\1\uffff\71\14\1\13\uffa3\14",
            "\12\5\1\6\2\5\1\12\24\5\1\uffff\uffdd\5",
            "\12\5\1\6\27\5\1\uffff\uffdd\5",
            "\12\17\1\15\2\17\1\16\ufff2\17",
            "\12\14\1\uffff\27\14\1\uffff\71\14\1\13\uffa3\14",
            "",
            "\12\14\1\20\27\14\1\uffff\71\14\1\13\uffa3\14",
            "\12\14\1\uffff\27\14\1\uffff\71\14\1\13\uffa3\14",
            ""
    };

    static final short[] DFA26_eot = DFA.unpackEncodedString(DFA26_eotS);
    static final short[] DFA26_eof = DFA.unpackEncodedString(DFA26_eofS);
    static final char[] DFA26_min = DFA.unpackEncodedStringToUnsignedChars(DFA26_minS);
    static final char[] DFA26_max = DFA.unpackEncodedStringToUnsignedChars(DFA26_maxS);
    static final short[] DFA26_accept = DFA.unpackEncodedString(DFA26_acceptS);
    static final short[] DFA26_special = DFA.unpackEncodedString(DFA26_specialS);
    static final short[][] DFA26_transition;

    static {
        int numStates = DFA26_transitionS.length;
        DFA26_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA26_transition[i] = DFA.unpackEncodedString(DFA26_transitionS[i]);
        }
    }

    class DFA26 extends DFA {

        public DFA26(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 26;
            this.eot = DFA26_eot;
            this.eof = DFA26_eof;
            this.min = DFA26_min;
            this.max = DFA26_max;
            this.accept = DFA26_accept;
            this.special = DFA26_special;
            this.transition = DFA26_transition;
        }
        public String getDescription() {
            return "()* loopback of 1062:17: ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA26_0 = input.LA(1);

                        s = -1;
                        if ( (LA26_0=='\\') ) {s = 1;}

                        else if ( ((LA26_0>='\u0000' && LA26_0<='\t')||(LA26_0>='\u000B' && LA26_0<='!')||(LA26_0>='#' && LA26_0<='[')||(LA26_0>=']' && LA26_0<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA26_8 = input.LA(1);

                        s = -1;
                        if ( (LA26_8=='\\') ) {s = 11;}

                        else if ( ((LA26_8>='\u0000' && LA26_8<='\t')||(LA26_8>='\u000B' && LA26_8<='!')||(LA26_8>='#' && LA26_8<='[')||(LA26_8>=']' && LA26_8<='\uFFFF')) ) {s = 12;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA26_9 = input.LA(1);

                        s = -1;
                        if ( (LA26_9=='\r') ) {s = 10;}

                        else if ( (LA26_9=='\n') ) {s = 6;}

                        else if ( ((LA26_9>='\u0000' && LA26_9<='\t')||(LA26_9>='\u000B' && LA26_9<='\f')||(LA26_9>='\u000E' && LA26_9<='!')||(LA26_9>='#' && LA26_9<='\uFFFF')) ) {s = 5;}

                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA26_1 = input.LA(1);

                        s = -1;
                        if ( (LA26_1=='\r') ) {s = 3;}

                        else if ( (LA26_1=='\n') ) {s = 4;}

                        else if ( ((LA26_1>='\u0000' && LA26_1<='\t')||(LA26_1>='\u000B' && LA26_1<='\f')||(LA26_1>='\u000E' && LA26_1<='\uFFFF')) ) {s = 5;}

                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA26_7 = input.LA(1);

                        s = -1;
                        if ( (LA26_7=='#') ) {s = 8;}

                        else if ( (LA26_7=='\t'||LA26_7==' ') ) {s = 7;}

                        else if ( (LA26_7=='\f') ) {s = 9;}

                        else if ( (LA26_7=='\r') ) {s = 10;}

                        else if ( ((LA26_7>='\u0000' && LA26_7<='\b')||LA26_7=='\u000B'||(LA26_7>='\u000E' && LA26_7<='\u001F')||LA26_7=='!'||(LA26_7>='$' && LA26_7<='\uFFFF')) ) {s = 5;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA26_11 = input.LA(1);

                        s = -1;
                        if ( (LA26_11=='\n') ) {s = 13;}

                        else if ( (LA26_11=='\r') ) {s = 14;}

                        else if ( ((LA26_11>='\u0000' && LA26_11<='\t')||(LA26_11>='\u000B' && LA26_11<='\f')||(LA26_11>='\u000E' && LA26_11<='\uFFFF')) ) {s = 15;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA26_15 = input.LA(1);

                        s = -1;
                        if ( (LA26_15=='\\') ) {s = 11;}

                        else if ( ((LA26_15>='\u0000' && LA26_15<='\t')||(LA26_15>='\u000B' && LA26_15<='!')||(LA26_15>='#' && LA26_15<='[')||(LA26_15>=']' && LA26_15<='\uFFFF')) ) {s = 12;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA26_4 = input.LA(1);

                        s = -1;
                        if ( (LA26_4=='\t'||LA26_4==' ') ) {s = 7;}

                        else if ( (LA26_4=='#') ) {s = 8;}

                        else if ( (LA26_4=='\f') ) {s = 9;}

                        else if ( (LA26_4=='\r') ) {s = 10;}

                        else if ( ((LA26_4>='\u0000' && LA26_4<='\b')||LA26_4=='\u000B'||(LA26_4>='\u000E' && LA26_4<='\u001F')||LA26_4=='!'||(LA26_4>='$' && LA26_4<='\uFFFF')) ) {s = 5;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA26_14 = input.LA(1);

                        s = -1;
                        if ( (LA26_14=='\n') ) {s = 16;}

                        else if ( (LA26_14=='\\') ) {s = 11;}

                        else if ( ((LA26_14>='\u0000' && LA26_14<='\t')||(LA26_14>='\u000B' && LA26_14<='!')||(LA26_14>='#' && LA26_14<='[')||(LA26_14>=']' && LA26_14<='\uFFFF')) ) {s = 12;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA26_10 = input.LA(1);

                        s = -1;
                        if ( ((LA26_10>='\u0000' && LA26_10<='\t')||(LA26_10>='\u000B' && LA26_10<='!')||(LA26_10>='#' && LA26_10<='\uFFFF')) ) {s = 5;}

                        else if ( (LA26_10=='\n') ) {s = 6;}

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA26_12 = input.LA(1);

                        s = -1;
                        if ( (LA26_12=='\\') ) {s = 11;}

                        else if ( ((LA26_12>='\u0000' && LA26_12<='\t')||(LA26_12>='\u000B' && LA26_12<='!')||(LA26_12>='#' && LA26_12<='[')||(LA26_12>=']' && LA26_12<='\uFFFF')) ) {s = 12;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA26_3 = input.LA(1);

                        s = -1;
                        if ( (LA26_3=='\n') ) {s = 6;}

                        else if ( ((LA26_3>='\u0000' && LA26_3<='\t')||(LA26_3>='\u000B' && LA26_3<='!')||(LA26_3>='#' && LA26_3<='\uFFFF')) ) {s = 5;}

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 26, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA27_eotS =
        "\4\uffff\1\6\2\uffff\2\6\2\uffff\3\6\1\uffff\1\6\1\uffff";
    static final String DFA27_eofS =
        "\21\uffff";
    static final String DFA27_minS =
        "\2\0\1\uffff\2\0\2\uffff\7\0\1\uffff\1\0\1\uffff";
    static final String DFA27_maxS =
        "\2\uffff\1\uffff\2\uffff\2\uffff\7\uffff\1\uffff\1\uffff\1\uffff";
    static final String DFA27_acceptS =
        "\2\uffff\1\2\2\uffff\1\1\1\3\7\uffff\1\1\1\uffff\1\1";
    static final String DFA27_specialS =
        "\1\13\1\10\1\uffff\1\6\1\11\2\uffff\1\7\1\5\1\3\1\2\1\0\1\1\1\4"+
        "\1\uffff\1\12\1\uffff}>";
    static final String[] DFA27_transitionS = {
            "\12\2\1\uffff\34\2\1\uffff\64\2\1\1\uffa3\2",
            "\12\5\1\4\2\5\1\3\ufff2\5",
            "",
            "\12\5\1\6\34\5\1\uffff\uffd8\5",
            "\11\5\1\7\1\uffff\1\5\1\11\1\12\22\5\1\7\2\5\1\10\3\5\1\uffff"+
            "\uffd8\5",
            "",
            "",
            "\11\5\1\7\1\uffff\1\5\1\11\1\12\22\5\1\7\2\5\1\10\3\5\1\uffff"+
            "\uffd8\5",
            "\12\14\1\uffff\34\14\1\uffff\64\14\1\13\uffa3\14",
            "\12\5\1\6\2\5\1\12\31\5\1\uffff\uffd8\5",
            "\12\5\1\6\34\5\1\uffff\uffd8\5",
            "\12\17\1\16\2\17\1\15\ufff2\17",
            "\12\14\1\uffff\34\14\1\uffff\64\14\1\13\uffa3\14",
            "\12\14\1\20\34\14\1\uffff\64\14\1\13\uffa3\14",
            "",
            "\12\14\1\uffff\34\14\1\uffff\64\14\1\13\uffa3\14",
            ""
    };

    static final short[] DFA27_eot = DFA.unpackEncodedString(DFA27_eotS);
    static final short[] DFA27_eof = DFA.unpackEncodedString(DFA27_eofS);
    static final char[] DFA27_min = DFA.unpackEncodedStringToUnsignedChars(DFA27_minS);
    static final char[] DFA27_max = DFA.unpackEncodedStringToUnsignedChars(DFA27_maxS);
    static final short[] DFA27_accept = DFA.unpackEncodedString(DFA27_acceptS);
    static final short[] DFA27_special = DFA.unpackEncodedString(DFA27_specialS);
    static final short[][] DFA27_transition;

    static {
        int numStates = DFA27_transitionS.length;
        DFA27_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA27_transition[i] = DFA.unpackEncodedString(DFA27_transitionS[i]);
        }
    }

    class DFA27 extends DFA {

        public DFA27(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 27;
            this.eot = DFA27_eot;
            this.eof = DFA27_eof;
            this.min = DFA27_min;
            this.max = DFA27_max;
            this.accept = DFA27_accept;
            this.special = DFA27_special;
            this.transition = DFA27_transition;
        }
        public String getDescription() {
            return "()* loopback of 1063:18: ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA27_11 = input.LA(1);

                        s = -1;
                        if ( (LA27_11=='\r') ) {s = 13;}

                        else if ( (LA27_11=='\n') ) {s = 14;}

                        else if ( ((LA27_11>='\u0000' && LA27_11<='\t')||(LA27_11>='\u000B' && LA27_11<='\f')||(LA27_11>='\u000E' && LA27_11<='\uFFFF')) ) {s = 15;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA27_12 = input.LA(1);

                        s = -1;
                        if ( (LA27_12=='\\') ) {s = 11;}

                        else if ( ((LA27_12>='\u0000' && LA27_12<='\t')||(LA27_12>='\u000B' && LA27_12<='&')||(LA27_12>='(' && LA27_12<='[')||(LA27_12>=']' && LA27_12<='\uFFFF')) ) {s = 12;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA27_10 = input.LA(1);

                        s = -1;
                        if ( (LA27_10=='\n') ) {s = 6;}

                        else if ( ((LA27_10>='\u0000' && LA27_10<='\t')||(LA27_10>='\u000B' && LA27_10<='&')||(LA27_10>='(' && LA27_10<='\uFFFF')) ) {s = 5;}

                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA27_9 = input.LA(1);

                        s = -1;
                        if ( (LA27_9=='\r') ) {s = 10;}

                        else if ( (LA27_9=='\n') ) {s = 6;}

                        else if ( ((LA27_9>='\u0000' && LA27_9<='\t')||(LA27_9>='\u000B' && LA27_9<='\f')||(LA27_9>='\u000E' && LA27_9<='&')||(LA27_9>='(' && LA27_9<='\uFFFF')) ) {s = 5;}

                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA27_13 = input.LA(1);

                        s = -1;
                        if ( (LA27_13=='\n') ) {s = 16;}

                        else if ( (LA27_13=='\\') ) {s = 11;}

                        else if ( ((LA27_13>='\u0000' && LA27_13<='\t')||(LA27_13>='\u000B' && LA27_13<='&')||(LA27_13>='(' && LA27_13<='[')||(LA27_13>=']' && LA27_13<='\uFFFF')) ) {s = 12;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA27_8 = input.LA(1);

                        s = -1;
                        if ( (LA27_8=='\\') ) {s = 11;}

                        else if ( ((LA27_8>='\u0000' && LA27_8<='\t')||(LA27_8>='\u000B' && LA27_8<='&')||(LA27_8>='(' && LA27_8<='[')||(LA27_8>=']' && LA27_8<='\uFFFF')) ) {s = 12;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA27_3 = input.LA(1);

                        s = -1;
                        if ( (LA27_3=='\n') ) {s = 6;}

                        else if ( ((LA27_3>='\u0000' && LA27_3<='\t')||(LA27_3>='\u000B' && LA27_3<='&')||(LA27_3>='(' && LA27_3<='\uFFFF')) ) {s = 5;}

                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA27_7 = input.LA(1);

                        s = -1;
                        if ( (LA27_7=='#') ) {s = 8;}

                        else if ( (LA27_7=='\t'||LA27_7==' ') ) {s = 7;}

                        else if ( (LA27_7=='\f') ) {s = 9;}

                        else if ( (LA27_7=='\r') ) {s = 10;}

                        else if ( ((LA27_7>='\u0000' && LA27_7<='\b')||LA27_7=='\u000B'||(LA27_7>='\u000E' && LA27_7<='\u001F')||(LA27_7>='!' && LA27_7<='\"')||(LA27_7>='$' && LA27_7<='&')||(LA27_7>='(' && LA27_7<='\uFFFF')) ) {s = 5;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA27_1 = input.LA(1);

                        s = -1;
                        if ( (LA27_1=='\r') ) {s = 3;}

                        else if ( (LA27_1=='\n') ) {s = 4;}

                        else if ( ((LA27_1>='\u0000' && LA27_1<='\t')||(LA27_1>='\u000B' && LA27_1<='\f')||(LA27_1>='\u000E' && LA27_1<='\uFFFF')) ) {s = 5;}

                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA27_4 = input.LA(1);

                        s = -1;
                        if ( (LA27_4=='\t'||LA27_4==' ') ) {s = 7;}

                        else if ( (LA27_4=='#') ) {s = 8;}

                        else if ( (LA27_4=='\f') ) {s = 9;}

                        else if ( (LA27_4=='\r') ) {s = 10;}

                        else if ( ((LA27_4>='\u0000' && LA27_4<='\b')||LA27_4=='\u000B'||(LA27_4>='\u000E' && LA27_4<='\u001F')||(LA27_4>='!' && LA27_4<='\"')||(LA27_4>='$' && LA27_4<='&')||(LA27_4>='(' && LA27_4<='\uFFFF')) ) {s = 5;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA27_15 = input.LA(1);

                        s = -1;
                        if ( (LA27_15=='\\') ) {s = 11;}

                        else if ( ((LA27_15>='\u0000' && LA27_15<='\t')||(LA27_15>='\u000B' && LA27_15<='&')||(LA27_15>='(' && LA27_15<='[')||(LA27_15>=']' && LA27_15<='\uFFFF')) ) {s = 12;}

                        else s = 6;

                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA27_0 = input.LA(1);

                        s = -1;
                        if ( (LA27_0=='\\') ) {s = 1;}

                        else if ( ((LA27_0>='\u0000' && LA27_0<='\t')||(LA27_0>='\u000B' && LA27_0<='&')||(LA27_0>='(' && LA27_0<='[')||(LA27_0>=']' && LA27_0<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 27, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA51_eotS =
        "\2\uffff\2\4\1\uffff";
    static final String DFA51_eofS =
        "\5\uffff";
    static final String DFA51_minS =
        "\1\11\1\uffff\2\0\1\uffff";
    static final String DFA51_maxS =
        "\1\43\1\uffff\2\uffff\1\uffff";
    static final String DFA51_acceptS =
        "\1\uffff\1\1\2\uffff\1\2";
    static final String DFA51_specialS =
        "\1\0\1\uffff\1\1\1\2\1\uffff}>";
    static final String[] DFA51_transitionS = {
            "\1\1\26\uffff\1\1\2\uffff\1\2",
            "",
            "\12\3\1\1\ufff5\3",
            "\12\3\1\1\ufff5\3",
            ""
    };

    static final short[] DFA51_eot = DFA.unpackEncodedString(DFA51_eotS);
    static final short[] DFA51_eof = DFA.unpackEncodedString(DFA51_eofS);
    static final char[] DFA51_min = DFA.unpackEncodedStringToUnsignedChars(DFA51_minS);
    static final char[] DFA51_max = DFA.unpackEncodedStringToUnsignedChars(DFA51_maxS);
    static final short[] DFA51_accept = DFA.unpackEncodedString(DFA51_acceptS);
    static final short[] DFA51_special = DFA.unpackEncodedString(DFA51_specialS);
    static final short[][] DFA51_transition;

    static {
        int numStates = DFA51_transitionS.length;
        DFA51_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA51_transition[i] = DFA.unpackEncodedString(DFA51_transitionS[i]);
        }
    }

    class DFA51 extends DFA {

        public DFA51(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 51;
            this.eot = DFA51_eot;
            this.eof = DFA51_eof;
            this.min = DFA51_min;
            this.max = DFA51_max;
            this.accept = DFA51_accept;
            this.special = DFA51_special;
            this.transition = DFA51_transition;
        }
        public String getDescription() {
            return "1181:1: COMMENT : ({...}? => ( ' ' | '\\t' )* '#' (~ '\\n' )* ( '\\n' )+ | '#' (~ '\\n' )* );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA51_0 = input.LA(1);

                         
                        int index51_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA51_0=='\t'||LA51_0==' ') && ((startPos==0))) {s = 1;}

                        else if ( (LA51_0=='#') ) {s = 2;}

                         
                        input.seek(index51_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA51_2 = input.LA(1);

                         
                        int index51_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA51_2>='\u0000' && LA51_2<='\t')||(LA51_2>='\u000B' && LA51_2<='\uFFFF')) ) {s = 3;}

                        else if ( (LA51_2=='\n') && ((startPos==0))) {s = 1;}

                        else s = 4;

                         
                        input.seek(index51_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA51_3 = input.LA(1);

                         
                        int index51_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA51_3>='\u0000' && LA51_3<='\t')||(LA51_3>='\u000B' && LA51_3<='\uFFFF')) ) {s = 3;}

                        else if ( (LA51_3=='\n') && ((startPos==0))) {s = 1;}

                        else s = 4;

                         
                        input.seek(index51_3);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 51, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA52_eotS =
        "\1\uffff\16\57\7\uffff\1\122\1\124\1\127\1\132\1\134\1\136\1\142"+
        "\1\145\1\147\1\151\3\uffff\1\153\2\uffff\1\155\1\uffff\2\57\2\161"+
        "\3\57\4\uffff\1\u0083\1\uffff\1\u0084\1\u0087\1\uffff\1\u0089\13"+
        "\57\1\u0098\1\57\1\u009a\1\u009b\11\57\4\uffff\1\u00a6\2\uffff\1"+
        "\u00a8\10\uffff\1\u00aa\2\uffff\1\u00ac\7\uffff\1\u00ad\1\uffff"+
        "\1\u00af\1\57\2\uffff\1\u00ad\1\161\4\uffff\1\161\4\57\1\u00b7\2"+
        "\uffff\1\u00b7\7\uffff\1\57\1\uffff\1\u00c0\3\57\1\u00c4\1\u00c5"+
        "\6\57\1\u00cc\1\57\1\uffff\1\57\2\uffff\5\57\1\u00d4\3\57\13\uffff"+
        "\1\u00da\1\161\1\uffff\1\u00ad\1\uffff\1\u00ad\1\u00e1\2\uffff\1"+
        "\u00e2\1\uffff\1\u00e1\1\uffff\1\u00e2\1\uffff\1\57\1\uffff\3\57"+
        "\2\uffff\1\u00f2\1\u00f3\1\57\1\u00f5\1\57\1\u00f7\1\uffff\3\57"+
        "\1\u00fb\3\57\1\uffff\1\57\1\u0100\1\57\1\uffff\1\u00ad\2\uffff"+
        "\1\u00ad\1\uffff\3\u00e1\2\uffff\2\u00e2\2\uffff\3\u00e1\2\u00e2"+
        "\2\uffff\1\57\1\u010f\1\u0110\1\57\2\uffff\1\57\1\uffff\1\57\1\uffff"+
        "\3\57\1\uffff\1\u0117\1\u0118\1\57\1\u011a\1\uffff\1\u011b\1\uffff"+
        "\1\u00ad\2\u00e1\2\u00e2\1\u00b7\2\u00e1\2\u00e2\1\u00b7\1\u0124"+
        "\2\uffff\1\57\1\u0126\1\57\1\u0128\1\u0129\1\u012a\2\uffff\1\u012b"+
        "\2\uffff\1\u00b7\3\u00e2\1\u00b7\3\u00e2\1\uffff\1\57\1\uffff\1"+
        "\u012d\4\uffff\1\u012e\2\uffff";
    static final String DFA52_eofS =
        "\u012f\uffff";
    static final String DFA52_minS =
        "\1\11\1\156\1\162\1\154\1\145\1\154\1\151\1\154\1\146\2\141\1\42"+
        "\1\162\1\150\1\151\7\uffff\2\75\1\52\1\57\2\75\1\74\3\75\3\uffff"+
        "\1\75\2\uffff\1\60\1\uffff\1\162\1\157\2\56\3\42\1\uffff\2\0\1\uffff"+
        "\1\12\1\uffff\2\11\1\uffff\1\60\1\144\1\145\1\141\1\156\1\146\1"+
        "\151\1\143\1\156\1\157\1\162\1\157\1\60\1\160\2\60\1\155\1\163\2"+
        "\151\1\164\1\171\1\151\1\164\1\145\4\uffff\1\75\2\uffff\1\75\10"+
        "\uffff\1\75\2\uffff\1\75\7\uffff\1\60\1\uffff\1\60\1\164\1\60\1"+
        "\uffff\1\60\2\56\2\uffff\1\53\1\56\4\42\1\47\2\0\1\42\2\0\1\uffff"+
        "\1\0\2\uffff\1\0\1\145\1\uffff\1\60\1\141\1\163\1\164\2\60\1\146"+
        "\2\145\1\143\1\141\1\155\1\60\1\142\1\uffff\1\157\2\uffff\1\142"+
        "\1\163\1\156\1\163\1\165\1\60\1\154\1\150\1\154\11\uffff\1\53\1"+
        "\uffff\2\60\1\53\3\60\1\0\1\uffff\7\0\1\162\1\uffff\1\153\1\163"+
        "\1\151\2\uffff\2\60\1\160\1\60\1\154\1\60\1\uffff\1\141\1\162\1"+
        "\144\1\60\1\164\1\145\1\162\1\uffff\1\145\1\60\1\144\2\60\1\uffff"+
        "\2\60\1\53\3\0\2\uffff\13\0\1\164\2\60\1\156\2\uffff\1\164\1\uffff"+
        "\1\154\1\uffff\1\154\1\164\1\141\1\uffff\2\60\1\156\1\60\1\uffff"+
        "\3\60\12\0\1\60\2\uffff\1\165\1\60\1\171\3\60\2\uffff\1\60\2\uffff"+
        "\10\0\1\uffff\1\145\1\uffff\1\60\4\uffff\1\60\2\uffff";
    static final String DFA52_maxS =
        "\1\176\1\163\1\162\1\157\1\145\1\170\1\162\1\154\1\163\1\141\1\162"+
        "\1\145\1\162\2\151\7\uffff\6\75\2\76\2\75\3\uffff\1\75\2\uffff\1"+
        "\71\1\uffff\1\162\1\157\1\170\1\154\1\162\1\47\1\162\1\uffff\2\uffff"+
        "\1\uffff\1\15\1\uffff\2\43\1\uffff\1\172\1\144\1\145\1\141\1\156"+
        "\1\154\1\163\1\145\1\156\1\157\1\162\1\157\1\172\1\160\2\172\1\155"+
        "\1\163\2\151\1\164\1\171\1\151\1\164\1\145\4\uffff\1\75\2\uffff"+
        "\1\75\10\uffff\1\75\2\uffff\1\75\7\uffff\1\152\1\uffff\1\172\1\164"+
        "\1\146\1\uffff\1\152\1\154\1\152\2\uffff\1\71\1\154\5\47\2\uffff"+
        "\1\42\2\uffff\1\uffff\1\0\2\uffff\1\0\1\145\1\uffff\1\172\1\141"+
        "\1\163\1\164\2\172\1\146\2\145\1\143\1\141\1\155\1\172\1\142\1\uffff"+
        "\1\157\2\uffff\1\142\1\163\1\156\1\163\1\165\1\172\1\154\1\150\1"+
        "\154\11\uffff\1\71\1\uffff\1\172\1\154\1\71\1\152\1\71\1\152\1\uffff"+
        "\1\uffff\7\uffff\1\162\1\uffff\1\153\1\163\1\151\2\uffff\2\172\1"+
        "\160\1\172\1\154\1\172\1\uffff\1\141\1\162\1\144\1\172\1\164\1\145"+
        "\1\162\1\uffff\1\145\1\172\1\144\1\71\1\152\1\uffff\1\71\1\152\1"+
        "\71\3\uffff\2\uffff\13\uffff\1\164\2\172\1\156\2\uffff\1\164\1\uffff"+
        "\1\154\1\uffff\1\154\1\164\1\141\1\uffff\2\172\1\156\1\172\1\uffff"+
        "\1\172\1\71\1\152\12\uffff\1\172\2\uffff\1\165\1\172\1\171\3\172"+
        "\2\uffff\1\172\2\uffff\10\uffff\1\uffff\1\145\1\uffff\1\172\4\uffff"+
        "\1\172\2\uffff";
    static final String DFA52_acceptS =
        "\17\uffff\1\35\1\36\1\37\1\40\1\41\1\42\1\43\12\uffff\1\56\1\57"+
        "\1\60\1\uffff\1\62\1\64\1\uffff\1\111\7\uffff\1\121\2\uffff\1\125"+
        "\1\uffff\1\126\2\uffff\1\131\31\uffff\1\72\1\44\1\73\1\45\1\uffff"+
        "\1\75\1\46\1\uffff\1\77\1\47\1\100\1\50\1\102\1\51\1\65\1\66\1\uffff"+
        "\1\52\1\70\1\uffff\1\53\1\63\1\54\1\101\1\55\1\103\1\61\1\uffff"+
        "\1\110\3\uffff\1\117\3\uffff\1\116\1\120\14\uffff\1\127\1\uffff"+
        "\1\131\1\130\2\uffff\1\1\16\uffff\1\17\1\uffff\1\21\1\22\11\uffff"+
        "\1\106\1\74\1\107\1\76\1\104\1\67\1\105\1\71\1\115\1\uffff\1\113"+
        "\7\uffff\1\122\10\uffff\1\112\3\uffff\1\6\1\7\6\uffff\1\15\7\uffff"+
        "\1\31\5\uffff\1\114\6\uffff\1\123\1\124\17\uffff\1\10\1\24\1\uffff"+
        "\1\12\1\uffff\1\14\3\uffff\1\25\4\uffff\1\33\16\uffff\1\3\1\4\6"+
        "\uffff\1\26\1\27\1\uffff\1\32\1\34\10\uffff\1\2\1\uffff\1\11\1\uffff"+
        "\1\16\1\20\1\23\1\30\1\uffff\1\13\1\5";
    static final String DFA52_specialS =
        "\1\23\57\uffff\1\12\1\41\1\uffff\1\34\1\uffff\1\17\1\16\107\uffff"+
        "\1\62\1\10\1\uffff\1\63\1\35\1\uffff\1\24\2\uffff\1\25\56\uffff"+
        "\1\54\1\uffff\1\37\1\26\1\56\1\5\1\3\1\30\1\21\37\uffff\1\33\1\44"+
        "\1\0\2\uffff\1\14\1\60\1\7\1\42\1\50\1\45\1\31\1\51\1\22\1\13\1"+
        "\2\26\uffff\1\57\1\46\1\53\1\36\1\43\1\52\1\4\1\47\1\1\1\61\16\uffff"+
        "\1\20\1\55\1\40\1\6\1\27\1\15\1\11\1\32\13\uffff}>";
    static final String[] DFA52_transitionS = {
            "\1\66\1\64\1\uffff\1\63\1\64\22\uffff\1\65\1\45\1\61\1\67\1"+
            "\uffff\1\37\1\33\1\60\1\17\1\20\1\30\1\26\1\24\1\27\1\46\1\31"+
            "\1\52\11\53\1\23\1\25\1\34\1\36\1\35\1\uffff\1\47\21\57\1\55"+
            "\2\57\1\56\5\57\1\21\1\62\1\22\1\43\1\57\1\40\1\1\1\2\1\3\1"+
            "\4\1\5\1\6\1\7\1\57\1\10\2\57\1\11\1\57\1\51\1\50\1\12\1\57"+
            "\1\13\1\57\1\14\1\54\1\57\1\15\1\57\1\16\1\57\1\41\1\32\1\42"+
            "\1\44",
            "\1\71\4\uffff\1\70",
            "\1\72",
            "\1\73\2\uffff\1\74",
            "\1\75",
            "\1\76\13\uffff\1\77",
            "\1\100\5\uffff\1\102\2\uffff\1\101",
            "\1\103",
            "\1\104\6\uffff\1\105\1\106\4\uffff\1\107",
            "\1\110",
            "\1\111\20\uffff\1\112",
            "\1\61\4\uffff\1\60\71\uffff\1\113\3\uffff\1\114",
            "\1\115",
            "\1\116\1\117",
            "\1\120",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\121",
            "\1\123",
            "\1\125\22\uffff\1\126",
            "\1\130\15\uffff\1\131",
            "\1\133",
            "\1\135",
            "\1\141\1\140\1\137",
            "\1\143\1\144",
            "\1\146",
            "\1\150",
            "",
            "",
            "",
            "\1\152",
            "",
            "",
            "\12\154",
            "",
            "\1\156",
            "\1\157",
            "\1\162\1\uffff\10\163\2\164\13\uffff\1\167\4\uffff\1\166\1"+
            "\uffff\1\165\13\uffff\1\160\14\uffff\1\167\4\uffff\1\166\1\uffff"+
            "\1\165\13\uffff\1\160",
            "\1\162\1\uffff\12\170\13\uffff\1\167\4\uffff\1\166\1\uffff"+
            "\1\165\30\uffff\1\167\4\uffff\1\166\1\uffff\1\165",
            "\1\61\4\uffff\1\60\52\uffff\1\172\37\uffff\1\171",
            "\1\61\4\uffff\1\60",
            "\1\61\4\uffff\1\60\52\uffff\1\173\37\uffff\1\174",
            "",
            "\12\177\1\uffff\34\177\1\175\64\177\1\176\uffa3\177",
            "\12\u0082\1\uffff\27\u0082\1\u0080\71\u0082\1\u0081\uffa3\u0082",
            "",
            "\1\64\2\uffff\1\64",
            "",
            "\1\66\1\u0086\1\uffff\1\u0083\1\u0086\22\uffff\1\65\2\uffff"+
            "\1\u0085",
            "\1\66\1\u0086\1\uffff\1\u0083\1\u0086\22\uffff\1\65\2\uffff"+
            "\1\u0085",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\22\57\1\u0088\7\57",
            "\1\u008a",
            "\1\u008b",
            "\1\u008c",
            "\1\u008d",
            "\1\u008e\5\uffff\1\u008f",
            "\1\u0090\11\uffff\1\u0091",
            "\1\u0092\1\uffff\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\1\u0097",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u0099",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u009c",
            "\1\u009d",
            "\1\u009e",
            "\1\u009f",
            "\1\u00a0",
            "\1\u00a1",
            "\1\u00a2",
            "\1\u00a3",
            "\1\u00a4",
            "",
            "",
            "",
            "",
            "\1\u00a5",
            "",
            "",
            "\1\u00a7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00a9",
            "",
            "",
            "\1\u00ab",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\154\13\uffff\1\u00ae\4\uffff\1\166\32\uffff\1\u00ae\4\uffff"+
            "\1\166",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u00b0",
            "\12\u00b1\7\uffff\6\u00b1\32\uffff\6\u00b1",
            "",
            "\12\u00b3\13\uffff\1\u00b2\4\uffff\1\166\32\uffff\1\u00b2\4"+
            "\uffff\1\166",
            "\1\162\1\uffff\10\163\2\164\13\uffff\1\167\4\uffff\1\166\1"+
            "\uffff\1\165\30\uffff\1\167\4\uffff\1\166\1\uffff\1\165",
            "\1\162\1\uffff\12\164\13\uffff\1\167\4\uffff\1\166\32\uffff"+
            "\1\167\4\uffff\1\166",
            "",
            "",
            "\1\u00b4\1\uffff\1\u00b4\2\uffff\12\u00b5",
            "\1\162\1\uffff\12\170\13\uffff\1\167\4\uffff\1\166\1\uffff"+
            "\1\165\30\uffff\1\167\4\uffff\1\166\1\uffff\1\165",
            "\1\61\4\uffff\1\60",
            "\1\61\4\uffff\1\60",
            "\1\61\4\uffff\1\60",
            "\1\61\4\uffff\1\60",
            "\1\u00b6",
            "\12\u00ba\1\u00b9\2\u00ba\1\u00b8\ufff2\u00ba",
            "\12\177\1\uffff\34\177\1\u00b7\64\177\1\176\uffa3\177",
            "\1\u00bb",
            "\12\u00be\1\u00bd\2\u00be\1\u00bc\ufff2\u00be",
            "\12\u0082\1\uffff\27\u0082\1\u00b7\71\u0082\1\u0081\uffa3\u0082",
            "",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "\1\u00bf",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u00c1",
            "\1\u00c2",
            "\1\u00c3",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u00c6",
            "\1\u00c7",
            "\1\u00c8",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u00cd",
            "",
            "\1\u00ce",
            "",
            "",
            "\1\u00cf",
            "\1\u00d0",
            "\1\u00d1",
            "\1\u00d2",
            "\1\u00d3",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u00d5",
            "\1\u00d6",
            "\1\u00d7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00d8\1\uffff\1\u00d8\2\uffff\12\u00d9",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\u00b1\7\uffff\6\u00b1\5\uffff\1\165\24\uffff\6\u00b1\5"+
            "\uffff\1\165",
            "\1\u00db\1\uffff\1\u00db\2\uffff\12\u00dc",
            "\12\u00b3\13\uffff\1\u00dd\4\uffff\1\166\32\uffff\1\u00dd\4"+
            "\uffff\1\166",
            "\12\u00b5",
            "\12\u00b5\20\uffff\1\166\37\uffff\1\166",
            "\47\u00e0\1\u00de\64\u00e0\1\u00df\uffa3\u00e0",
            "",
            "\12\177\1\u00e2\34\177\1\u00b7\64\177\1\176\uffa3\177",
            "\11\177\1\u00e3\1\uffff\1\177\1\u00e5\1\u00e6\22\177\1\u00e3"+
            "\2\177\1\u00e4\3\177\1\u00b7\64\177\1\176\uffa3\177",
            "\12\177\1\uffff\34\177\1\u00b7\64\177\1\176\uffa3\177",
            "\42\u00e9\1\u00e7\71\u00e9\1\u00e8\uffa3\u00e9",
            "\12\u0082\1\u00e2\27\u0082\1\u00b7\71\u0082\1\u0081\uffa3\u0082",
            "\11\u0082\1\u00ea\1\uffff\1\u0082\1\u00ec\1\u00ed\22\u0082"+
            "\1\u00ea\1\u0082\1\u00b7\1\u00eb\70\u0082\1\u0081\uffa3\u0082",
            "\12\u0082\1\uffff\27\u0082\1\u00b7\71\u0082\1\u0081\uffa3\u0082",
            "\1\u00ee",
            "",
            "\1\u00ef",
            "\1\u00f0",
            "\1\u00f1",
            "",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u00f4",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u00f6",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "",
            "\1\u00f8",
            "\1\u00f9",
            "\1\u00fa",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u00fc",
            "\1\u00fd",
            "\1\u00fe",
            "",
            "\1\u00ff",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u0101",
            "\12\u00d9",
            "\12\u00d9\20\uffff\1\166\37\uffff\1\166",
            "",
            "\12\u00dc",
            "\12\u00dc\20\uffff\1\166\37\uffff\1\166",
            "\1\u0102\1\uffff\1\u0102\2\uffff\12\u0103",
            "\47\u00e0\1\u0104\64\u00e0\1\u00df\uffa3\u00e0",
            "\0\u0105",
            "\47\u00e0\1\u00de\64\u00e0\1\u00df\uffa3\u00e0",
            "",
            "",
            "\11\177\1\u00e3\1\uffff\1\177\1\u00e5\1\u00e6\22\177\1\u00e3"+
            "\2\177\1\u00e4\3\177\1\u00b7\64\177\1\176\uffa3\177",
            "\12\u0107\1\uffff\34\u0107\1\u0108\64\u0107\1\u0106\uffa3\u0107",
            "\12\177\1\u00e2\2\177\1\u00e6\31\177\1\u00b7\64\177\1\176\uffa3"+
            "\177",
            "\12\177\1\u00e2\34\177\1\u00b7\64\177\1\176\uffa3\177",
            "\42\u00e9\1\u0109\71\u00e9\1\u00e8\uffa3\u00e9",
            "\0\u010a",
            "\42\u00e9\1\u00e7\71\u00e9\1\u00e8\uffa3\u00e9",
            "\11\u0082\1\u00ea\1\uffff\1\u0082\1\u00ec\1\u00ed\22\u0082"+
            "\1\u00ea\1\u0082\1\u00b7\1\u00eb\70\u0082\1\u0081\uffa3\u0082",
            "\12\u010c\1\uffff\27\u010c\1\u010d\71\u010c\1\u010b\uffa3\u010c",
            "\12\u0082\1\u00e2\2\u0082\1\u00ed\24\u0082\1\u00b7\71\u0082"+
            "\1\u0081\uffa3\u0082",
            "\12\u0082\1\u00e2\27\u0082\1\u00b7\71\u0082\1\u0081\uffa3\u0082",
            "\1\u010e",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u0111",
            "",
            "",
            "\1\u0112",
            "",
            "\1\u0113",
            "",
            "\1\u0114",
            "\1\u0115",
            "\1\u0116",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u0119",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\u0103",
            "\12\u0103\20\uffff\1\166\37\uffff\1\166",
            "\47\u00e0\1\u011c\64\u00e0\1\u00df\uffa3\u00e0",
            "\47\u00e0\1\u00de\64\u00e0\1\u00df\uffa3\u00e0",
            "\12\u011f\1\u011e\2\u011f\1\u011d\ufff2\u011f",
            "\12\u0107\1\uffff\34\u0107\1\u0108\64\u0107\1\u0106\uffa3\u0107",
            "\0\u00e2",
            "\42\u00e9\1\u0120\71\u00e9\1\u00e8\uffa3\u00e9",
            "\42\u00e9\1\u00e7\71\u00e9\1\u00e8\uffa3\u00e9",
            "\12\u0123\1\u0122\2\u0123\1\u0121\ufff2\u0123",
            "\12\u010c\1\uffff\27\u010c\1\u010d\71\u010c\1\u010b\uffa3\u010c",
            "\0\u00e2",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "",
            "",
            "\1\u0125",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\1\u0127",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "",
            "",
            "\0\u00e1",
            "\12\u0107\1\uffff\34\u0107\1\u0108\64\u0107\1\u0106\uffa3\u0107",
            "\11\177\1\u00e3\1\uffff\1\177\1\u00e5\1\u00e6\22\177\1\u00e3"+
            "\2\177\1\u00e4\3\177\1\u00b7\64\177\1\176\uffa3\177",
            "\12\u0107\1\uffff\34\u0107\1\u0108\64\u0107\1\u0106\uffa3\u0107",
            "\0\u00e1",
            "\12\u010c\1\uffff\27\u010c\1\u010d\71\u010c\1\u010b\uffa3\u010c",
            "\11\u0082\1\u00ea\1\uffff\1\u0082\1\u00ec\1\u00ed\22\u0082"+
            "\1\u00ea\1\u0082\1\u00b7\1\u00eb\70\u0082\1\u0081\uffa3\u0082",
            "\12\u010c\1\uffff\27\u010c\1\u010d\71\u010c\1\u010b\uffa3\u010c",
            "",
            "\1\u012c",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "",
            "",
            "",
            "",
            "\12\57\7\uffff\32\57\4\uffff\1\57\1\uffff\32\57",
            "",
            ""
    };

    static final short[] DFA52_eot = DFA.unpackEncodedString(DFA52_eotS);
    static final short[] DFA52_eof = DFA.unpackEncodedString(DFA52_eofS);
    static final char[] DFA52_min = DFA.unpackEncodedStringToUnsignedChars(DFA52_minS);
    static final char[] DFA52_max = DFA.unpackEncodedStringToUnsignedChars(DFA52_maxS);
    static final short[] DFA52_accept = DFA.unpackEncodedString(DFA52_acceptS);
    static final short[] DFA52_special = DFA.unpackEncodedString(DFA52_specialS);
    static final short[][] DFA52_transition;

    static {
        int numStates = DFA52_transitionS.length;
        DFA52_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA52_transition[i] = DFA.unpackEncodedString(DFA52_transitionS[i]);
        }
    }

    class DFA52 extends DFA {

        public DFA52(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 52;
            this.eot = DFA52_eot;
            this.eof = DFA52_eof;
            this.min = DFA52_min;
            this.max = DFA52_max;
            this.accept = DFA52_accept;
            this.special = DFA52_special;
            this.transition = DFA52_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( AS | ASSERT | BREAK | CLASS | CONTINUE | DEF | DELETE | ELIF | EXCEPT | EXEC | FINALLY | FROM | FOR | GLOBAL | IF | IMPORT | IN | IS | LAMBDA | ORELSE | PASS | PRINT | RAISE | RETURN | TRY | WHILE | WITH | YIELD | LPAREN | RPAREN | LBRACK | RBRACK | COLON | COMMA | SEMI | PLUS | MINUS | STAR | SLASH | VBAR | AMPER | LESS | GREATER | ASSIGN | PERCENT | BACKQUOTE | LCURLY | RCURLY | CIRCUMFLEX | TILDE | EQUAL | NOTEQUAL | ALT_NOTEQUAL | LESSEQUAL | LEFTSHIFT | GREATEREQUAL | RIGHTSHIFT | PLUSEQUAL | MINUSEQUAL | DOUBLESTAR | STAREQUAL | DOUBLESLASH | SLASHEQUAL | VBAREQUAL | PERCENTEQUAL | AMPEREQUAL | CIRCUMFLEXEQUAL | LEFTSHIFTEQUAL | RIGHTSHIFTEQUAL | DOUBLESTAREQUAL | DOUBLESLASHEQUAL | DOT | AT | AND | OR | NOT | FLOAT | LONGINT | INT | COMPLEX | NAME | STRING | TRISTRINGPART | STRINGPART | CONTINUED_LINE | NEWLINE | WS | LEADING_WS | COMMENT );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA52_224 = input.LA(1);

                        s = -1;
                        if ( (LA52_224=='\'') ) {s = 222;}

                        else if ( (LA52_224=='\\') ) {s = 223;}

                        else if ( ((LA52_224>='\u0000' && LA52_224<='&')||(LA52_224>='(' && LA52_224<='[')||(LA52_224>=']' && LA52_224<='\uFFFF')) ) {s = 224;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA52_268 = input.LA(1);

                        s = -1;
                        if ( (LA52_268=='\\') ) {s = 267;}

                        else if ( ((LA52_268>='\u0000' && LA52_268<='\t')||(LA52_268>='\u000B' && LA52_268<='!')||(LA52_268>='#' && LA52_268<='[')||(LA52_268>=']' && LA52_268<='\uFFFF')) ) {s = 268;}

                        else if ( (LA52_268=='\"') ) {s = 269;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA52_237 = input.LA(1);

                        s = -1;
                        if ( (LA52_237=='\\') ) {s = 129;}

                        else if ( ((LA52_237>='\u0000' && LA52_237<='\t')||(LA52_237>='\u000B' && LA52_237<='!')||(LA52_237>='#' && LA52_237<='[')||(LA52_237>=']' && LA52_237<='\uFFFF')) ) {s = 130;}

                        else if ( (LA52_237=='\"') ) {s = 183;}

                        else if ( (LA52_237=='\n') ) {s = 226;}

                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA52_188 = input.LA(1);

                        s = -1;
                        if ( (LA52_188=='\n') ) {s = 226;}

                        else if ( (LA52_188=='\\') ) {s = 129;}

                        else if ( ((LA52_188>='\u0000' && LA52_188<='\t')||(LA52_188>='\u000B' && LA52_188<='!')||(LA52_188>='#' && LA52_188<='[')||(LA52_188>=']' && LA52_188<='\uFFFF')) ) {s = 130;}

                        else if ( (LA52_188=='\"') ) {s = 183;}

                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA52_266 = input.LA(1);

                        s = -1;
                        if ( (LA52_266=='\"') ) {s = 231;}

                        else if ( (LA52_266=='\\') ) {s = 232;}

                        else if ( ((LA52_266>='\u0000' && LA52_266<='!')||(LA52_266>='#' && LA52_266<='[')||(LA52_266>=']' && LA52_266<='\uFFFF')) ) {s = 233;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA52_187 = input.LA(1);

                        s = -1;
                        if ( (LA52_187=='\"') ) {s = 231;}

                        else if ( (LA52_187=='\\') ) {s = 232;}

                        else if ( ((LA52_187>='\u0000' && LA52_187<='!')||(LA52_187>='#' && LA52_187<='[')||(LA52_187>=']' && LA52_187<='\uFFFF')) ) {s = 233;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA52_287 = input.LA(1);

                        s = -1;
                        if ( (LA52_287=='\\') ) {s = 262;}

                        else if ( ((LA52_287>='\u0000' && LA52_287<='\t')||(LA52_287>='\u000B' && LA52_287<='&')||(LA52_287>='(' && LA52_287<='[')||(LA52_287>=']' && LA52_287<='\uFFFF')) ) {s = 263;}

                        else if ( (LA52_287=='\'') ) {s = 264;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA52_229 = input.LA(1);

                        s = -1;
                        if ( (LA52_229=='\\') ) {s = 126;}

                        else if ( (LA52_229=='\r') ) {s = 230;}

                        else if ( (LA52_229=='\'') ) {s = 183;}

                        else if ( ((LA52_229>='\u0000' && LA52_229<='\t')||(LA52_229>='\u000B' && LA52_229<='\f')||(LA52_229>='\u000E' && LA52_229<='&')||(LA52_229>='(' && LA52_229<='[')||(LA52_229>=']' && LA52_229<='\uFFFF')) ) {s = 127;}

                        else if ( (LA52_229=='\n') ) {s = 226;}

                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA52_127 = input.LA(1);

                        s = -1;
                        if ( (LA52_127=='\\') ) {s = 126;}

                        else if ( ((LA52_127>='\u0000' && LA52_127<='\t')||(LA52_127>='\u000B' && LA52_127<='&')||(LA52_127>='(' && LA52_127<='[')||(LA52_127>=']' && LA52_127<='\uFFFF')) ) {s = 127;}

                        else if ( (LA52_127=='\'') ) {s = 183;}

                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA52_290 = input.LA(1);

                        s = -1;
                        if ( (LA52_290=='\\') ) {s = 129;}

                        else if ( (LA52_290=='\t'||LA52_290==' ') ) {s = 234;}

                        else if ( (LA52_290=='\"') ) {s = 183;}

                        else if ( (LA52_290=='#') ) {s = 235;}

                        else if ( (LA52_290=='\f') ) {s = 236;}

                        else if ( (LA52_290=='\r') ) {s = 237;}

                        else if ( ((LA52_290>='\u0000' && LA52_290<='\b')||LA52_290=='\u000B'||(LA52_290>='\u000E' && LA52_290<='\u001F')||LA52_290=='!'||(LA52_290>='$' && LA52_290<='[')||(LA52_290>=']' && LA52_290<='\uFFFF')) ) {s = 130;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA52_48 = input.LA(1);

                        s = -1;
                        if ( (LA52_48=='\'') ) {s = 125;}

                        else if ( (LA52_48=='\\') ) {s = 126;}

                        else if ( ((LA52_48>='\u0000' && LA52_48<='\t')||(LA52_48>='\u000B' && LA52_48<='&')||(LA52_48>='(' && LA52_48<='[')||(LA52_48>=']' && LA52_48<='\uFFFF')) ) {s = 127;}

                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA52_236 = input.LA(1);

                        s = -1;
                        if ( (LA52_236=='\\') ) {s = 129;}

                        else if ( (LA52_236=='\r') ) {s = 237;}

                        else if ( (LA52_236=='\"') ) {s = 183;}

                        else if ( ((LA52_236>='\u0000' && LA52_236<='\t')||(LA52_236>='\u000B' && LA52_236<='\f')||(LA52_236>='\u000E' && LA52_236<='!')||(LA52_236>='#' && LA52_236<='[')||(LA52_236>=']' && LA52_236<='\uFFFF')) ) {s = 130;}

                        else if ( (LA52_236=='\n') ) {s = 226;}

                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA52_227 = input.LA(1);

                        s = -1;
                        if ( (LA52_227=='\\') ) {s = 126;}

                        else if ( (LA52_227=='#') ) {s = 228;}

                        else if ( (LA52_227=='\'') ) {s = 183;}

                        else if ( (LA52_227=='\t'||LA52_227==' ') ) {s = 227;}

                        else if ( (LA52_227=='\f') ) {s = 229;}

                        else if ( (LA52_227=='\r') ) {s = 230;}

                        else if ( ((LA52_227>='\u0000' && LA52_227<='\b')||LA52_227=='\u000B'||(LA52_227>='\u000E' && LA52_227<='\u001F')||(LA52_227>='!' && LA52_227<='\"')||(LA52_227>='$' && LA52_227<='&')||(LA52_227>='(' && LA52_227<='[')||(LA52_227>=']' && LA52_227<='\uFFFF')) ) {s = 127;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA52_289 = input.LA(1);

                        s = -1;
                        if ( (LA52_289=='\\') ) {s = 267;}

                        else if ( ((LA52_289>='\u0000' && LA52_289<='\t')||(LA52_289>='\u000B' && LA52_289<='!')||(LA52_289>='#' && LA52_289<='[')||(LA52_289>=']' && LA52_289<='\uFFFF')) ) {s = 268;}

                        else if ( (LA52_289=='\"') ) {s = 269;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA52_54 = input.LA(1);

                         
                        int index52_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA52_54==' ') && (((startPos==0)||(startPos>0)))) {s = 53;}

                        else if ( (LA52_54=='\f') && ((startPos>0))) {s = 131;}

                        else if ( (LA52_54=='#') && ((startPos==0))) {s = 133;}

                        else if ( (LA52_54=='\n'||LA52_54=='\r') && ((startPos==0))) {s = 134;}

                        else if ( (LA52_54=='\t') && (((startPos==0)||(startPos>0)))) {s = 54;}

                        else s = 135;

                         
                        input.seek(index52_54);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA52_53 = input.LA(1);

                         
                        int index52_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA52_53==' ') && (((startPos==0)||(startPos>0)))) {s = 53;}

                        else if ( (LA52_53=='\f') && ((startPos>0))) {s = 131;}

                        else if ( (LA52_53=='#') && ((startPos==0))) {s = 133;}

                        else if ( (LA52_53=='\n'||LA52_53=='\r') && ((startPos==0))) {s = 134;}

                        else if ( (LA52_53=='\t') && (((startPos==0)||(startPos>0)))) {s = 54;}

                        else s = 132;

                         
                        input.seek(index52_53);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA52_284 = input.LA(1);

                        s = -1;
                        if ( ((LA52_284>='\u0000' && LA52_284<='\uFFFF')) ) {s = 225;}

                        else s = 183;

                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA52_190 = input.LA(1);

                        s = -1;
                        if ( (LA52_190=='\\') ) {s = 129;}

                        else if ( ((LA52_190>='\u0000' && LA52_190<='\t')||(LA52_190>='\u000B' && LA52_190<='!')||(LA52_190>='#' && LA52_190<='[')||(LA52_190>=']' && LA52_190<='\uFFFF')) ) {s = 130;}

                        else if ( (LA52_190=='\"') ) {s = 183;}

                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA52_235 = input.LA(1);

                        s = -1;
                        if ( (LA52_235=='\\') ) {s = 267;}

                        else if ( ((LA52_235>='\u0000' && LA52_235<='\t')||(LA52_235>='\u000B' && LA52_235<='!')||(LA52_235>='#' && LA52_235<='[')||(LA52_235>=']' && LA52_235<='\uFFFF')) ) {s = 268;}

                        else if ( (LA52_235=='\"') ) {s = 269;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA52_0 = input.LA(1);

                         
                        int index52_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA52_0=='a') ) {s = 1;}

                        else if ( (LA52_0=='b') ) {s = 2;}

                        else if ( (LA52_0=='c') ) {s = 3;}

                        else if ( (LA52_0=='d') ) {s = 4;}

                        else if ( (LA52_0=='e') ) {s = 5;}

                        else if ( (LA52_0=='f') ) {s = 6;}

                        else if ( (LA52_0=='g') ) {s = 7;}

                        else if ( (LA52_0=='i') ) {s = 8;}

                        else if ( (LA52_0=='l') ) {s = 9;}

                        else if ( (LA52_0=='p') ) {s = 10;}

                        else if ( (LA52_0=='r') ) {s = 11;}

                        else if ( (LA52_0=='t') ) {s = 12;}

                        else if ( (LA52_0=='w') ) {s = 13;}

                        else if ( (LA52_0=='y') ) {s = 14;}

                        else if ( (LA52_0=='(') ) {s = 15;}

                        else if ( (LA52_0==')') ) {s = 16;}

                        else if ( (LA52_0=='[') ) {s = 17;}

                        else if ( (LA52_0==']') ) {s = 18;}

                        else if ( (LA52_0==':') ) {s = 19;}

                        else if ( (LA52_0==',') ) {s = 20;}

                        else if ( (LA52_0==';') ) {s = 21;}

                        else if ( (LA52_0=='+') ) {s = 22;}

                        else if ( (LA52_0=='-') ) {s = 23;}

                        else if ( (LA52_0=='*') ) {s = 24;}

                        else if ( (LA52_0=='/') ) {s = 25;}

                        else if ( (LA52_0=='|') ) {s = 26;}

                        else if ( (LA52_0=='&') ) {s = 27;}

                        else if ( (LA52_0=='<') ) {s = 28;}

                        else if ( (LA52_0=='>') ) {s = 29;}

                        else if ( (LA52_0=='=') ) {s = 30;}

                        else if ( (LA52_0=='%') ) {s = 31;}

                        else if ( (LA52_0=='`') ) {s = 32;}

                        else if ( (LA52_0=='{') ) {s = 33;}

                        else if ( (LA52_0=='}') ) {s = 34;}

                        else if ( (LA52_0=='^') ) {s = 35;}

                        else if ( (LA52_0=='~') ) {s = 36;}

                        else if ( (LA52_0=='!') ) {s = 37;}

                        else if ( (LA52_0=='.') ) {s = 38;}

                        else if ( (LA52_0=='@') ) {s = 39;}

                        else if ( (LA52_0=='o') ) {s = 40;}

                        else if ( (LA52_0=='n') ) {s = 41;}

                        else if ( (LA52_0=='0') ) {s = 42;}

                        else if ( ((LA52_0>='1' && LA52_0<='9')) ) {s = 43;}

                        else if ( (LA52_0=='u') ) {s = 44;}

                        else if ( (LA52_0=='R') ) {s = 45;}

                        else if ( (LA52_0=='U') ) {s = 46;}

                        else if ( ((LA52_0>='A' && LA52_0<='Q')||(LA52_0>='S' && LA52_0<='T')||(LA52_0>='V' && LA52_0<='Z')||LA52_0=='_'||LA52_0=='h'||(LA52_0>='j' && LA52_0<='k')||LA52_0=='m'||LA52_0=='q'||LA52_0=='s'||LA52_0=='v'||LA52_0=='x'||LA52_0=='z') ) {s = 47;}

                        else if ( (LA52_0=='\'') ) {s = 48;}

                        else if ( (LA52_0=='\"') ) {s = 49;}

                        else if ( (LA52_0=='\\') ) {s = 50;}

                        else if ( (LA52_0=='\f') ) {s = 51;}

                        else if ( (LA52_0=='\n'||LA52_0=='\r') ) {s = 52;}

                        else if ( (LA52_0==' ') && (((startPos==0)||(startPos>0)))) {s = 53;}

                        else if ( (LA52_0=='\t') && (((startPos==0)||(startPos>0)))) {s = 54;}

                        else if ( (LA52_0=='#') ) {s = 55;}

                         
                        input.seek(index52_0);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA52_132 = input.LA(1);

                         
                        int index52_132 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((startPos>0)) ) {s = 131;}

                        else if ( (((startPos==0)||((startPos==0)&&(implicitLineJoiningLevel>0)))) ) {s = 134;}

                         
                        input.seek(index52_132);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA52_135 = input.LA(1);

                         
                        int index52_135 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((startPos>0)) ) {s = 131;}

                        else if ( (((startPos==0)||((startPos==0)&&(implicitLineJoiningLevel>0)))) ) {s = 134;}

                         
                        input.seek(index52_135);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA52_185 = input.LA(1);

                        s = -1;
                        if ( (LA52_185=='\\') ) {s = 126;}

                        else if ( (LA52_185=='\t'||LA52_185==' ') ) {s = 227;}

                        else if ( (LA52_185=='\'') ) {s = 183;}

                        else if ( (LA52_185=='#') ) {s = 228;}

                        else if ( (LA52_185=='\f') ) {s = 229;}

                        else if ( (LA52_185=='\r') ) {s = 230;}

                        else if ( ((LA52_185>='\u0000' && LA52_185<='\b')||LA52_185=='\u000B'||(LA52_185>='\u000E' && LA52_185<='\u001F')||(LA52_185>='!' && LA52_185<='\"')||(LA52_185>='$' && LA52_185<='&')||(LA52_185>='(' && LA52_185<='[')||(LA52_185>=']' && LA52_185<='\uFFFF')) ) {s = 127;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA52_288 = input.LA(1);

                        s = -1;
                        if ( ((LA52_288>='\u0000' && LA52_288<='\uFFFF')) ) {s = 225;}

                        else s = 183;

                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA52_189 = input.LA(1);

                        s = -1;
                        if ( (LA52_189=='\\') ) {s = 129;}

                        else if ( (LA52_189=='\t'||LA52_189==' ') ) {s = 234;}

                        else if ( (LA52_189=='\"') ) {s = 183;}

                        else if ( (LA52_189=='#') ) {s = 235;}

                        else if ( (LA52_189=='\f') ) {s = 236;}

                        else if ( (LA52_189=='\r') ) {s = 237;}

                        else if ( ((LA52_189>='\u0000' && LA52_189<='\b')||LA52_189=='\u000B'||(LA52_189>='\u000E' && LA52_189<='\u001F')||LA52_189=='!'||(LA52_189>='$' && LA52_189<='[')||(LA52_189>=']' && LA52_189<='\uFFFF')) ) {s = 130;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA52_233 = input.LA(1);

                        s = -1;
                        if ( (LA52_233=='\"') ) {s = 231;}

                        else if ( (LA52_233=='\\') ) {s = 232;}

                        else if ( ((LA52_233>='\u0000' && LA52_233<='!')||(LA52_233>='#' && LA52_233<='[')||(LA52_233>=']' && LA52_233<='\uFFFF')) ) {s = 233;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA52_291 = input.LA(1);

                        s = -1;
                        if ( (LA52_291=='\\') ) {s = 267;}

                        else if ( ((LA52_291>='\u0000' && LA52_291<='\t')||(LA52_291>='\u000B' && LA52_291<='!')||(LA52_291>='#' && LA52_291<='[')||(LA52_291>=']' && LA52_291<='\uFFFF')) ) {s = 268;}

                        else if ( (LA52_291=='\"') ) {s = 269;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA52_222 = input.LA(1);

                        s = -1;
                        if ( (LA52_222=='\'') ) {s = 260;}

                        else if ( (LA52_222=='\\') ) {s = 223;}

                        else if ( ((LA52_222>='\u0000' && LA52_222<='&')||(LA52_222>='(' && LA52_222<='[')||(LA52_222>=']' && LA52_222<='\uFFFF')) ) {s = 224;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA52_51 = input.LA(1);

                         
                        int index52_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA52_51=='\n'||LA52_51=='\r') ) {s = 52;}

                        else s = 131;

                         
                        input.seek(index52_51);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA52_130 = input.LA(1);

                        s = -1;
                        if ( (LA52_130=='\\') ) {s = 129;}

                        else if ( ((LA52_130>='\u0000' && LA52_130<='\t')||(LA52_130>='\u000B' && LA52_130<='!')||(LA52_130>='#' && LA52_130<='[')||(LA52_130>=']' && LA52_130<='\uFFFF')) ) {s = 130;}

                        else if ( (LA52_130=='\"') ) {s = 183;}

                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA52_263 = input.LA(1);

                        s = -1;
                        if ( (LA52_263=='\\') ) {s = 262;}

                        else if ( ((LA52_263>='\u0000' && LA52_263<='\t')||(LA52_263>='\u000B' && LA52_263<='&')||(LA52_263>='(' && LA52_263<='[')||(LA52_263>=']' && LA52_263<='\uFFFF')) ) {s = 263;}

                        else if ( (LA52_263=='\'') ) {s = 264;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA52_184 = input.LA(1);

                        s = -1;
                        if ( (LA52_184=='\n') ) {s = 226;}

                        else if ( (LA52_184=='\\') ) {s = 126;}

                        else if ( ((LA52_184>='\u0000' && LA52_184<='\t')||(LA52_184>='\u000B' && LA52_184<='&')||(LA52_184>='(' && LA52_184<='[')||(LA52_184>=']' && LA52_184<='\uFFFF')) ) {s = 127;}

                        else if ( (LA52_184=='\'') ) {s = 183;}

                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA52_286 = input.LA(1);

                        s = -1;
                        if ( (LA52_286=='\\') ) {s = 126;}

                        else if ( (LA52_286=='\t'||LA52_286==' ') ) {s = 227;}

                        else if ( (LA52_286=='\'') ) {s = 183;}

                        else if ( (LA52_286=='#') ) {s = 228;}

                        else if ( (LA52_286=='\f') ) {s = 229;}

                        else if ( (LA52_286=='\r') ) {s = 230;}

                        else if ( ((LA52_286>='\u0000' && LA52_286<='\b')||LA52_286=='\u000B'||(LA52_286>='\u000E' && LA52_286<='\u001F')||(LA52_286>='!' && LA52_286<='\"')||(LA52_286>='$' && LA52_286<='&')||(LA52_286>='(' && LA52_286<='[')||(LA52_286>=']' && LA52_286<='\uFFFF')) ) {s = 127;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA52_49 = input.LA(1);

                        s = -1;
                        if ( (LA52_49=='\"') ) {s = 128;}

                        else if ( (LA52_49=='\\') ) {s = 129;}

                        else if ( ((LA52_49>='\u0000' && LA52_49<='\t')||(LA52_49>='\u000B' && LA52_49<='!')||(LA52_49>='#' && LA52_49<='[')||(LA52_49>=']' && LA52_49<='\uFFFF')) ) {s = 130;}

                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA52_230 = input.LA(1);

                        s = -1;
                        if ( (LA52_230=='\\') ) {s = 126;}

                        else if ( ((LA52_230>='\u0000' && LA52_230<='\t')||(LA52_230>='\u000B' && LA52_230<='&')||(LA52_230>='(' && LA52_230<='[')||(LA52_230>=']' && LA52_230<='\uFFFF')) ) {s = 127;}

                        else if ( (LA52_230=='\'') ) {s = 183;}

                        else if ( (LA52_230=='\n') ) {s = 226;}

                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA52_264 = input.LA(1);

                        s = -1;
                        if ( ((LA52_264>='\u0000' && LA52_264<='\uFFFF')) ) {s = 226;}

                        else s = 183;

                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA52_223 = input.LA(1);

                        s = -1;
                        if ( ((LA52_223>='\u0000' && LA52_223<='\uFFFF')) ) {s = 261;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA52_232 = input.LA(1);

                        s = -1;
                        if ( ((LA52_232>='\u0000' && LA52_232<='\uFFFF')) ) {s = 266;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA52_261 = input.LA(1);

                        s = -1;
                        if ( (LA52_261=='\'') ) {s = 222;}

                        else if ( (LA52_261=='\\') ) {s = 223;}

                        else if ( ((LA52_261>='\u0000' && LA52_261<='&')||(LA52_261>='(' && LA52_261<='[')||(LA52_261>=']' && LA52_261<='\uFFFF')) ) {s = 224;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA52_267 = input.LA(1);

                        s = -1;
                        if ( (LA52_267=='\r') ) {s = 289;}

                        else if ( (LA52_267=='\n') ) {s = 290;}

                        else if ( ((LA52_267>='\u0000' && LA52_267<='\t')||(LA52_267>='\u000B' && LA52_267<='\f')||(LA52_267>='\u000E' && LA52_267<='\uFFFF')) ) {s = 291;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA52_231 = input.LA(1);

                        s = -1;
                        if ( (LA52_231=='\"') ) {s = 265;}

                        else if ( (LA52_231=='\\') ) {s = 232;}

                        else if ( ((LA52_231>='\u0000' && LA52_231<='!')||(LA52_231>='#' && LA52_231<='[')||(LA52_231>=']' && LA52_231<='\uFFFF')) ) {s = 233;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA52_234 = input.LA(1);

                        s = -1;
                        if ( (LA52_234=='\\') ) {s = 129;}

                        else if ( (LA52_234=='#') ) {s = 235;}

                        else if ( (LA52_234=='\"') ) {s = 183;}

                        else if ( (LA52_234=='\t'||LA52_234==' ') ) {s = 234;}

                        else if ( (LA52_234=='\f') ) {s = 236;}

                        else if ( (LA52_234=='\r') ) {s = 237;}

                        else if ( ((LA52_234>='\u0000' && LA52_234<='\b')||LA52_234=='\u000B'||(LA52_234>='\u000E' && LA52_234<='\u001F')||LA52_234=='!'||(LA52_234>='$' && LA52_234<='[')||(LA52_234>=']' && LA52_234<='\uFFFF')) ) {s = 130;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA52_265 = input.LA(1);

                        s = -1;
                        if ( (LA52_265=='\"') ) {s = 288;}

                        else if ( (LA52_265=='\\') ) {s = 232;}

                        else if ( ((LA52_265>='\u0000' && LA52_265<='!')||(LA52_265>='#' && LA52_265<='[')||(LA52_265>=']' && LA52_265<='\uFFFF')) ) {s = 233;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA52_262 = input.LA(1);

                        s = -1;
                        if ( (LA52_262=='\r') ) {s = 285;}

                        else if ( (LA52_262=='\n') ) {s = 286;}

                        else if ( ((LA52_262>='\u0000' && LA52_262<='\t')||(LA52_262>='\u000B' && LA52_262<='\f')||(LA52_262>='\u000E' && LA52_262<='\uFFFF')) ) {s = 287;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA52_182 = input.LA(1);

                        s = -1;
                        if ( (LA52_182=='\'') ) {s = 222;}

                        else if ( (LA52_182=='\\') ) {s = 223;}

                        else if ( ((LA52_182>='\u0000' && LA52_182<='&')||(LA52_182>='(' && LA52_182<='[')||(LA52_182>=']' && LA52_182<='\uFFFF')) ) {s = 224;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA52_285 = input.LA(1);

                        s = -1;
                        if ( (LA52_285=='\\') ) {s = 262;}

                        else if ( ((LA52_285>='\u0000' && LA52_285<='\t')||(LA52_285>='\u000B' && LA52_285<='&')||(LA52_285>='(' && LA52_285<='[')||(LA52_285>=']' && LA52_285<='\uFFFF')) ) {s = 263;}

                        else if ( (LA52_285=='\'') ) {s = 264;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA52_186 = input.LA(1);

                        s = -1;
                        if ( (LA52_186=='\\') ) {s = 126;}

                        else if ( ((LA52_186>='\u0000' && LA52_186<='\t')||(LA52_186>='\u000B' && LA52_186<='&')||(LA52_186>='(' && LA52_186<='[')||(LA52_186>=']' && LA52_186<='\uFFFF')) ) {s = 127;}

                        else if ( (LA52_186=='\'') ) {s = 183;}

                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA52_260 = input.LA(1);

                        s = -1;
                        if ( (LA52_260=='\'') ) {s = 284;}

                        else if ( (LA52_260=='\\') ) {s = 223;}

                        else if ( ((LA52_260>='\u0000' && LA52_260<='&')||(LA52_260>='(' && LA52_260<='[')||(LA52_260>=']' && LA52_260<='\uFFFF')) ) {s = 224;}

                        else s = 225;

                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA52_228 = input.LA(1);

                        s = -1;
                        if ( (LA52_228=='\\') ) {s = 262;}

                        else if ( ((LA52_228>='\u0000' && LA52_228<='\t')||(LA52_228>='\u000B' && LA52_228<='&')||(LA52_228>='(' && LA52_228<='[')||(LA52_228>=']' && LA52_228<='\uFFFF')) ) {s = 263;}

                        else if ( (LA52_228=='\'') ) {s = 264;}

                        else s = 226;

                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA52_269 = input.LA(1);

                        s = -1;
                        if ( ((LA52_269>='\u0000' && LA52_269<='\uFFFF')) ) {s = 226;}

                        else s = 183;

                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA52_126 = input.LA(1);

                        s = -1;
                        if ( (LA52_126=='\r') ) {s = 184;}

                        else if ( (LA52_126=='\n') ) {s = 185;}

                        else if ( ((LA52_126>='\u0000' && LA52_126<='\t')||(LA52_126>='\u000B' && LA52_126<='\f')||(LA52_126>='\u000E' && LA52_126<='\uFFFF')) ) {s = 186;}

                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA52_129 = input.LA(1);

                        s = -1;
                        if ( (LA52_129=='\r') ) {s = 188;}

                        else if ( (LA52_129=='\n') ) {s = 189;}

                        else if ( ((LA52_129>='\u0000' && LA52_129<='\t')||(LA52_129>='\u000B' && LA52_129<='\f')||(LA52_129>='\u000E' && LA52_129<='\uFFFF')) ) {s = 190;}

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 52, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}