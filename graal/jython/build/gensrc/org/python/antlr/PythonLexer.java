// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /Users/zwei/Workspace/zippy/zippy/grammar/Python.g 2013-06-19 17:22:39

package org.python.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class PythonLexer extends Lexer {
    public static final int BACKQUOTE=85;
    public static final int SLASHEQUAL=54;
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
    public static final int VBAR=71;
    public static final int MINUSEQUAL=52;
    public static final int RPAREN=44;
    public static final int NAME=9;
    public static final int IMPORT=28;
    public static final int GREATER=65;
    public static final int DOUBLESTAREQUAL=61;
    public static final int LESS=64;
    public static final int RETURN=37;
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
    public static final int WITH=40;
    public static final int PERCENT=78;
    public static final int EXEC=22;
    public static final int MINUS=76;
    public static final int SEMI=50;
    public static final int PRINT=11;
    public static final int TRIQUOTE=94;
    public static final int COLON=45;
    public static final int TRAILBACKSLASH=6;
    public static final int NEWLINE=7;
    public static final int AMPEREQUAL=56;
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
    int implicitLineJoiningLevel = 0;
    int startPos=-1;

    //For use in partial parsing.
    public boolean eofWhileNested = false;
    public boolean partial = false;
    public boolean single = false;

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
                    reportError(nva);
                    errorHandler.recover(this, nva); // throw out current char and try again
                } catch (FailedPredicateException fp) {
                    //XXX: added this for failed STRINGPART -- the FailedPredicateException
                    //     hides a NoViableAltException.  This should be the only
                    //     FailedPredicateException that gets thrown by the lexer.
                    reportError(fp);
                    errorHandler.recover(this, fp); // throw out current char and try again
                } catch (RecognitionException re) {
                    reportError(re);
                    // match() routine has already called recover()
                }
            }
        }
        @Override
        public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
            //Do nothing. We will handle error display elsewhere.
        }



    // delegates
    // delegators

    public PythonLexer() {;} 
    public PythonLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public PythonLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Users/zwei/Workspace/zippy/zippy/grammar/Python.g"; }

    // $ANTLR start "AS"
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2223:11: ( 'as' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2223:13: 'as'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2224:11: ( 'assert' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2224:13: 'assert'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2225:11: ( 'break' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2225:13: 'break'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2226:11: ( 'class' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2226:13: 'class'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2227:11: ( 'continue' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2227:13: 'continue'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2228:11: ( 'def' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2228:13: 'def'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2229:11: ( 'del' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2229:13: 'del'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2230:11: ( 'elif' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2230:13: 'elif'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2231:11: ( 'except' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2231:13: 'except'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2232:11: ( 'exec' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2232:13: 'exec'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2233:11: ( 'finally' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2233:13: 'finally'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2234:11: ( 'from' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2234:13: 'from'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2235:11: ( 'for' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2235:13: 'for'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2236:11: ( 'global' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2236:13: 'global'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2237:11: ( 'if' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2237:13: 'if'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2238:11: ( 'import' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2238:13: 'import'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2239:11: ( 'in' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2239:13: 'in'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2240:11: ( 'is' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2240:13: 'is'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2241:11: ( 'lambda' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2241:13: 'lambda'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2242:11: ( 'else' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2242:13: 'else'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2243:11: ( 'pass' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2243:13: 'pass'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2244:11: ( 'print' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2244:13: 'print'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2245:11: ( 'raise' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2245:13: 'raise'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2246:11: ( 'return' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2246:13: 'return'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2247:11: ( 'try' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2247:13: 'try'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2248:11: ( 'while' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2248:13: 'while'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2249:11: ( 'with' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2249:13: 'with'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2250:11: ( 'yield' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2250:13: 'yield'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2255:11: ( '(' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2255:13: '('
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2257:11: ( ')' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2257:13: ')'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2259:11: ( '[' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2259:13: '['
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2261:11: ( ']' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2261:13: ']'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2263:11: ( ':' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2263:13: ':'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2265:10: ( ',' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2265:12: ','
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2267:9: ( ';' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2267:11: ';'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2269:9: ( '+' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2269:11: '+'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2271:10: ( '-' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2271:12: '-'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2273:9: ( '*' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2273:11: '*'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2275:10: ( '/' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2275:12: '/'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2277:9: ( '|' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2277:11: '|'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2279:10: ( '&' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2279:12: '&'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2281:9: ( '<' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2281:11: '<'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2283:12: ( '>' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2283:14: '>'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2285:11: ( '=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2285:13: '='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2287:12: ( '%' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2287:14: '%'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2289:14: ( '`' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2289:16: '`'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2291:11: ( '{' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2291:13: '{'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2293:11: ( '}' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2293:13: '}'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2295:15: ( '^' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2295:17: '^'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2297:10: ( '~' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2297:12: '~'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2299:10: ( '==' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2299:12: '=='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2301:13: ( '!=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2301:15: '!='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2303:13: ( '<>' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2303:15: '<>'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2305:14: ( '<=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2305:16: '<='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2307:14: ( '<<' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2307:16: '<<'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2309:17: ( '>=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2309:19: '>='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2311:15: ( '>>' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2311:17: '>>'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2313:14: ( '+=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2313:16: '+='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2315:15: ( '-=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2315:17: '-='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2317:15: ( '**' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2317:17: '**'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2319:14: ( '*=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2319:16: '*='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2321:16: ( '//' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2321:18: '//'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2323:15: ( '/=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2323:17: '/='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2325:14: ( '|=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2325:16: '|='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2327:17: ( '%=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2327:19: '%='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2329:15: ( '&=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2329:17: '&='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2331:20: ( '^=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2331:22: '^='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2333:19: ( '<<=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2333:21: '<<='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2335:20: ( '>>=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2335:22: '>>='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2337:20: ( '**=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2337:22: '**='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2339:21: ( '//=' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2339:23: '//='
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2341:5: ( '.' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2341:7: '.'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2343:4: ( '@' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2343:6: '@'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2345:5: ( 'and' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2345:7: 'and'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2347:4: ( 'or' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2347:6: 'or'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2349:5: ( 'not' )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2349:7: 'not'
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2352:5: ( '.' DIGITS ( Exponent )? | DIGITS '.' Exponent | DIGITS ( '.' ( DIGITS ( Exponent )? )? | Exponent ) )
            int alt5=3;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2352:9: '.' DIGITS ( Exponent )?
                    {
                    match('.'); 
                    mDIGITS(); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2352:20: ( Exponent )?
                    int alt1=2;
                    int LA1_0 = input.LA(1);

                    if ( (LA1_0=='E'||LA1_0=='e') ) {
                        alt1=1;
                    }
                    switch (alt1) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2352:21: Exponent
                            {
                            mExponent(); 

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2353:9: DIGITS '.' Exponent
                    {
                    mDIGITS(); 
                    match('.'); 
                    mExponent(); 

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2354:9: DIGITS ( '.' ( DIGITS ( Exponent )? )? | Exponent )
                    {
                    mDIGITS(); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2354:16: ( '.' ( DIGITS ( Exponent )? )? | Exponent )
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
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2354:17: '.' ( DIGITS ( Exponent )? )?
                            {
                            match('.'); 
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2354:21: ( DIGITS ( Exponent )? )?
                            int alt3=2;
                            int LA3_0 = input.LA(1);

                            if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                                alt3=1;
                            }
                            switch (alt3) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2354:22: DIGITS ( Exponent )?
                                    {
                                    mDIGITS(); 
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2354:29: ( Exponent )?
                                    int alt2=2;
                                    int LA2_0 = input.LA(1);

                                    if ( (LA2_0=='E'||LA2_0=='e') ) {
                                        alt2=1;
                                    }
                                    switch (alt2) {
                                        case 1 :
                                            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2354:30: Exponent
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
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2354:45: Exponent
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2358:5: ( INT ( 'l' | 'L' ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2358:9: INT ( 'l' | 'L' )
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2363:5: ( ( 'e' | 'E' ) ( '+' | '-' )? DIGITS )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2363:10: ( 'e' | 'E' ) ( '+' | '-' )? DIGITS
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2363:22: ( '+' | '-' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='+'||LA6_0=='-') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2366:5: ( '0' ( 'x' | 'X' ) ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+ | '0' ( 'o' | 'O' ) ( '0' .. '7' )* | '0' ( '0' .. '7' )* | '0' ( 'b' | 'B' ) ( '0' .. '1' )* | '1' .. '9' ( DIGITS )* )
            int alt12=5;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='0') ) {
                switch ( input.LA(2) ) {
                case 'X':
                case 'x':
                    {
                    alt12=1;
                    }
                    break;
                case 'O':
                case 'o':
                    {
                    alt12=2;
                    }
                    break;
                case 'B':
                case 'b':
                    {
                    alt12=4;
                    }
                    break;
                default:
                    alt12=3;}

            }
            else if ( ((LA12_0>='1' && LA12_0<='9')) ) {
                alt12=5;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2367:9: '0' ( 'x' | 'X' ) ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
                    {
                    match('0'); 
                    if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2367:25: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )+
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
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:
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
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2369:9: '0' ( 'o' | 'O' ) ( '0' .. '7' )*
                    {
                    match('0'); 
                    if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2369:25: ( '0' .. '7' )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0>='0' && LA8_0<='7')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2369:27: '0' .. '7'
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
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2370:9: '0' ( '0' .. '7' )*
                    {
                    match('0'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2370:14: ( '0' .. '7' )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( ((LA9_0>='0' && LA9_0<='7')) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2370:16: '0' .. '7'
                    	    {
                    	    matchRange('0','7'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);


                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2372:9: '0' ( 'b' | 'B' ) ( '0' .. '1' )*
                    {
                    match('0'); 
                    if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2372:25: ( '0' .. '1' )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0>='0' && LA10_0<='1')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2372:27: '0' .. '1'
                    	    {
                    	    matchRange('0','1'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);


                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2374:9: '1' .. '9' ( DIGITS )*
                    {
                    matchRange('1','9'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2374:18: ( DIGITS )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( ((LA11_0>='0' && LA11_0<='9')) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2374:18: DIGITS
                    	    {
                    	    mDIGITS(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop11;
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2378:5: ( ( DIGITS )+ ( 'j' | 'J' ) | FLOAT ( 'j' | 'J' ) )
            int alt14=2;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2378:9: ( DIGITS )+ ( 'j' | 'J' )
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2378:9: ( DIGITS )+
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
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2378:9: DIGITS
                    	    {
                    	    mDIGITS(); 

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
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2379:9: FLOAT ( 'j' | 'J' )
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2383:8: ( ( '0' .. '9' )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2383:10: ( '0' .. '9' )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2383:10: ( '0' .. '9' )+
            int cnt15=0;
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( ((LA15_0>='0' && LA15_0<='9')) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2383:12: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt15 >= 1 ) break loop15;
                        EarlyExitException eee =
                            new EarlyExitException(15, input);
                        throw eee;
                }
                cnt15++;
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2385:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2385:10: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2386:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( ((LA16_0>='0' && LA16_0<='9')||(LA16_0>='A' && LA16_0<='Z')||LA16_0=='_'||(LA16_0>='a' && LA16_0<='z')) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:
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
            	    break loop16;
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:5: ( ( 'r' | 'u' | 'b' | 'ur' | 'br' | 'R' | 'U' | 'B' | 'UR' | 'BR' | 'uR' | 'Ur' | 'Br' | 'bR' )? ( '\\'\\'\\'' ( options {greedy=false; } : TRIAPOS )* '\\'\\'\\'' | '\"\"\"' ( options {greedy=false; } : TRIQUOTE )* '\"\"\"' | '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* '\"' | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* '\\'' ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:9: ( 'r' | 'u' | 'b' | 'ur' | 'br' | 'R' | 'U' | 'B' | 'UR' | 'BR' | 'uR' | 'Ur' | 'Br' | 'bR' )? ( '\\'\\'\\'' ( options {greedy=false; } : TRIAPOS )* '\\'\\'\\'' | '\"\"\"' ( options {greedy=false; } : TRIQUOTE )* '\"\"\"' | '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* '\"' | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* '\\'' )
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:9: ( 'r' | 'u' | 'b' | 'ur' | 'br' | 'R' | 'U' | 'B' | 'UR' | 'BR' | 'uR' | 'Ur' | 'Br' | 'bR' )?
            int alt17=15;
            alt17 = dfa17.predict(input);
            switch (alt17) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:10: 'r'
                    {
                    match('r'); 

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:14: 'u'
                    {
                    match('u'); 

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:18: 'b'
                    {
                    match('b'); 

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:22: 'ur'
                    {
                    match("ur"); 


                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:27: 'br'
                    {
                    match("br"); 


                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:32: 'R'
                    {
                    match('R'); 

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:36: 'U'
                    {
                    match('U'); 

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:40: 'B'
                    {
                    match('B'); 

                    }
                    break;
                case 9 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:44: 'UR'
                    {
                    match("UR"); 


                    }
                    break;
                case 10 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:49: 'BR'
                    {
                    match("BR"); 


                    }
                    break;
                case 11 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:54: 'uR'
                    {
                    match("uR"); 


                    }
                    break;
                case 12 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:59: 'Ur'
                    {
                    match("Ur"); 


                    }
                    break;
                case 13 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:64: 'Br'
                    {
                    match("Br"); 


                    }
                    break;
                case 14 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2393:69: 'bR'
                    {
                    match("bR"); 


                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2394:9: ( '\\'\\'\\'' ( options {greedy=false; } : TRIAPOS )* '\\'\\'\\'' | '\"\"\"' ( options {greedy=false; } : TRIQUOTE )* '\"\"\"' | '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* '\"' | '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* '\\'' )
            int alt22=4;
            int LA22_0 = input.LA(1);

            if ( (LA22_0=='\'') ) {
                int LA22_1 = input.LA(2);

                if ( (LA22_1=='\'') ) {
                    int LA22_3 = input.LA(3);

                    if ( (LA22_3=='\'') ) {
                        alt22=1;
                    }
                    else {
                        alt22=4;}
                }
                else if ( ((LA22_1>='\u0000' && LA22_1<='\t')||(LA22_1>='\u000B' && LA22_1<='&')||(LA22_1>='(' && LA22_1<='\uFFFF')) ) {
                    alt22=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 22, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA22_0=='\"') ) {
                int LA22_2 = input.LA(2);

                if ( (LA22_2=='\"') ) {
                    int LA22_5 = input.LA(3);

                    if ( (LA22_5=='\"') ) {
                        alt22=2;
                    }
                    else {
                        alt22=3;}
                }
                else if ( ((LA22_2>='\u0000' && LA22_2<='\t')||(LA22_2>='\u000B' && LA22_2<='!')||(LA22_2>='#' && LA22_2<='\uFFFF')) ) {
                    alt22=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 22, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2394:13: '\\'\\'\\'' ( options {greedy=false; } : TRIAPOS )* '\\'\\'\\''
                    {
                    match("'''"); 

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2394:22: ( options {greedy=false; } : TRIAPOS )*
                    loop18:
                    do {
                        int alt18=2;
                        int LA18_0 = input.LA(1);

                        if ( (LA18_0=='\'') ) {
                            int LA18_1 = input.LA(2);

                            if ( (LA18_1=='\'') ) {
                                int LA18_3 = input.LA(3);

                                if ( (LA18_3=='\'') ) {
                                    alt18=2;
                                }
                                else if ( ((LA18_3>='\u0000' && LA18_3<='&')||(LA18_3>='(' && LA18_3<='\uFFFF')) ) {
                                    alt18=1;
                                }


                            }
                            else if ( ((LA18_1>='\u0000' && LA18_1<='&')||(LA18_1>='(' && LA18_1<='\uFFFF')) ) {
                                alt18=1;
                            }


                        }
                        else if ( ((LA18_0>='\u0000' && LA18_0<='&')||(LA18_0>='(' && LA18_0<='\uFFFF')) ) {
                            alt18=1;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2394:47: TRIAPOS
                    	    {
                    	    mTRIAPOS(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop18;
                        }
                    } while (true);

                    match("'''"); 


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2395:13: '\"\"\"' ( options {greedy=false; } : TRIQUOTE )* '\"\"\"'
                    {
                    match("\"\"\""); 

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2395:19: ( options {greedy=false; } : TRIQUOTE )*
                    loop19:
                    do {
                        int alt19=2;
                        int LA19_0 = input.LA(1);

                        if ( (LA19_0=='\"') ) {
                            int LA19_1 = input.LA(2);

                            if ( (LA19_1=='\"') ) {
                                int LA19_3 = input.LA(3);

                                if ( (LA19_3=='\"') ) {
                                    alt19=2;
                                }
                                else if ( ((LA19_3>='\u0000' && LA19_3<='!')||(LA19_3>='#' && LA19_3<='\uFFFF')) ) {
                                    alt19=1;
                                }


                            }
                            else if ( ((LA19_1>='\u0000' && LA19_1<='!')||(LA19_1>='#' && LA19_1<='\uFFFF')) ) {
                                alt19=1;
                            }


                        }
                        else if ( ((LA19_0>='\u0000' && LA19_0<='!')||(LA19_0>='#' && LA19_0<='\uFFFF')) ) {
                            alt19=1;
                        }


                        switch (alt19) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2395:44: TRIQUOTE
                    	    {
                    	    mTRIQUOTE(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop19;
                        }
                    } while (true);

                    match("\"\"\""); 


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2396:13: '\"' ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )* '\"'
                    {
                    match('\"'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2396:17: ( ESC | ~ ( '\\\\' | '\\n' | '\"' ) )*
                    loop20:
                    do {
                        int alt20=3;
                        int LA20_0 = input.LA(1);

                        if ( (LA20_0=='\\') ) {
                            alt20=1;
                        }
                        else if ( ((LA20_0>='\u0000' && LA20_0<='\t')||(LA20_0>='\u000B' && LA20_0<='!')||(LA20_0>='#' && LA20_0<='[')||(LA20_0>=']' && LA20_0<='\uFFFF')) ) {
                            alt20=2;
                        }


                        switch (alt20) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2396:18: ESC
                    	    {
                    	    mESC(); 

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2396:22: ~ ( '\\\\' | '\\n' | '\"' )
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
                    	    break loop20;
                        }
                    } while (true);

                    match('\"'); 

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2397:13: '\\'' ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )* '\\''
                    {
                    match('\''); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2397:18: ( ESC | ~ ( '\\\\' | '\\n' | '\\'' ) )*
                    loop21:
                    do {
                        int alt21=3;
                        int LA21_0 = input.LA(1);

                        if ( (LA21_0=='\\') ) {
                            alt21=1;
                        }
                        else if ( ((LA21_0>='\u0000' && LA21_0<='\t')||(LA21_0>='\u000B' && LA21_0<='&')||(LA21_0>='(' && LA21_0<='[')||(LA21_0>=']' && LA21_0<='\uFFFF')) ) {
                            alt21=2;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2397:19: ESC
                    	    {
                    	    mESC(); 

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2397:23: ~ ( '\\\\' | '\\n' | '\\'' )
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
                    	    break loop21;
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

    // $ANTLR start "TRIQUOTE"
    public final void mTRIQUOTE() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:5: ( ( '\"' )? ( '\"' )? ( ESC | ~ ( '\\\\' | '\"' ) )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:7: ( '\"' )? ( '\"' )? ( ESC | ~ ( '\\\\' | '\"' ) )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:7: ( '\"' )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0=='\"') ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:7: '\"'
                    {
                    match('\"'); 

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:12: ( '\"' )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0=='\"') ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:12: '\"'
                    {
                    match('\"'); 

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:17: ( ESC | ~ ( '\\\\' | '\"' ) )+
            int cnt25=0;
            loop25:
            do {
                int alt25=3;
                int LA25_0 = input.LA(1);

                if ( (LA25_0=='\\') ) {
                    alt25=1;
                }
                else if ( ((LA25_0>='\u0000' && LA25_0<='!')||(LA25_0>='#' && LA25_0<='[')||(LA25_0>=']' && LA25_0<='\uFFFF')) ) {
                    alt25=2;
                }


                switch (alt25) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:18: ESC
            	    {
            	    mESC(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2409:22: ~ ( '\\\\' | '\"' )
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
            	    if ( cnt25 >= 1 ) break loop25;
                        EarlyExitException eee =
                            new EarlyExitException(25, input);
                        throw eee;
                }
                cnt25++;
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:5: ( ( '\\'' )? ( '\\'' )? ( ESC | ~ ( '\\\\' | '\\'' ) )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:7: ( '\\'' )? ( '\\'' )? ( ESC | ~ ( '\\\\' | '\\'' ) )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:7: ( '\\'' )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0=='\'') ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:7: '\\''
                    {
                    match('\''); 

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:13: ( '\\'' )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0=='\'') ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:13: '\\''
                    {
                    match('\''); 

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:19: ( ESC | ~ ( '\\\\' | '\\'' ) )+
            int cnt28=0;
            loop28:
            do {
                int alt28=3;
                int LA28_0 = input.LA(1);

                if ( (LA28_0=='\\') ) {
                    alt28=1;
                }
                else if ( ((LA28_0>='\u0000' && LA28_0<='&')||(LA28_0>='(' && LA28_0<='[')||(LA28_0>=']' && LA28_0<='\uFFFF')) ) {
                    alt28=2;
                }


                switch (alt28) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:20: ESC
            	    {
            	    mESC(); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2415:24: ~ ( '\\\\' | '\\'' )
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
            	    if ( cnt28 >= 1 ) break loop28;
                        EarlyExitException eee =
                            new EarlyExitException(28, input);
                        throw eee;
                }
                cnt28++;
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2420:5: ( '\\\\' . )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2420:10: '\\\\' .
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

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2428:5: ( '\\\\' ( '\\r' )? '\\n' ( ' ' | '\\t' )* ( COMMENT | nl= NEWLINE | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2428:10: '\\\\' ( '\\r' )? '\\n' ( ' ' | '\\t' )* ( COMMENT | nl= NEWLINE | )
            {
            match('\\'); 
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2428:15: ( '\\r' )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0=='\r') ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2428:16: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }

            match('\n'); 
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2428:28: ( ' ' | '\\t' )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0=='\t'||LA30_0==' ') ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:
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
            	    break loop30;
                }
            } while (true);

             _channel=HIDDEN; 
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2429:10: ( COMMENT | nl= NEWLINE | )
            int alt31=3;
            int LA31_0 = input.LA(1);

            if ( (LA31_0=='\t'||LA31_0==' ') && ((startPos==0))) {
                alt31=1;
            }
            else if ( (LA31_0=='#') ) {
                alt31=1;
            }
            else if ( (LA31_0=='\n'||(LA31_0>='\f' && LA31_0<='\r')) ) {
                alt31=2;
            }
            else {
                alt31=3;}
            switch (alt31) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2429:12: COMMENT
                    {
                    mCOMMENT(); 

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2430:12: nl= NEWLINE
                    {
                    int nlStart1808 = getCharIndex();
                    mNEWLINE(); 
                    nl = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, nlStart1808, getCharIndex()-1);

                                   emit(new CommonToken(NEWLINE,nl.getText()));
                               

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2435:10: 
                    {
                    }
                    break;

            }


                           if (input.LA(1) == -1) {
                               throw new ParseException("unexpected character after line continuation character");
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

                int newlines = 0;

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2452:5: ( ( ( '\\u000C' )? ( '\\r' )? '\\n' )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2452:9: ( ( '\\u000C' )? ( '\\r' )? '\\n' )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2452:9: ( ( '\\u000C' )? ( '\\r' )? '\\n' )+
            int cnt34=0;
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0=='\n'||(LA34_0>='\f' && LA34_0<='\r')) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2452:10: ( '\\u000C' )? ( '\\r' )? '\\n'
            	    {
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2452:10: ( '\\u000C' )?
            	    int alt32=2;
            	    int LA32_0 = input.LA(1);

            	    if ( (LA32_0=='\f') ) {
            	        alt32=1;
            	    }
            	    switch (alt32) {
            	        case 1 :
            	            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2452:11: '\\u000C'
            	            {
            	            match('\f'); 

            	            }
            	            break;

            	    }

            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2452:21: ( '\\r' )?
            	    int alt33=2;
            	    int LA33_0 = input.LA(1);

            	    if ( (LA33_0=='\r') ) {
            	        alt33=1;
            	    }
            	    switch (alt33) {
            	        case 1 :
            	            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2452:22: '\\r'
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
            	    if ( cnt34 >= 1 ) break loop34;
                        EarlyExitException eee =
                            new EarlyExitException(34, input);
                        throw eee;
                }
                cnt34++;
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
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2458:5: ({...}? => ( ' ' | '\\t' | '\\u000C' )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2458:10: {...}? => ( ' ' | '\\t' | '\\u000C' )+
            {
            if ( !((startPos>0)) ) {
                throw new FailedPredicateException(input, "WS", "startPos>0");
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2458:26: ( ' ' | '\\t' | '\\u000C' )+
            int cnt35=0;
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0=='\t'||LA35_0=='\f'||LA35_0==' ') ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:
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
            	    if ( cnt35 >= 1 ) break loop35;
                        EarlyExitException eee =
                            new EarlyExitException(35, input);
                        throw eee;
                }
                cnt35++;
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

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2472:5: ({...}? => ({...}? ( ' ' | '\\t' )+ | ( ' ' | '\\t' )+ ( ( '\\r' )? '\\n' )* ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2472:9: {...}? => ({...}? ( ' ' | '\\t' )+ | ( ' ' | '\\t' )+ ( ( '\\r' )? '\\n' )* )
            {
            if ( !((startPos==0)) ) {
                throw new FailedPredicateException(input, "LEADING_WS", "startPos==0");
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2473:9: ({...}? ( ' ' | '\\t' )+ | ( ' ' | '\\t' )+ ( ( '\\r' )? '\\n' )* )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==' ') ) {
                int LA40_1 = input.LA(2);

                if ( ((implicitLineJoiningLevel>0)) ) {
                    alt40=1;
                }
                else if ( (true) ) {
                    alt40=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 40, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA40_0=='\t') ) {
                int LA40_2 = input.LA(2);

                if ( ((implicitLineJoiningLevel>0)) ) {
                    alt40=1;
                }
                else if ( (true) ) {
                    alt40=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 40, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2473:13: {...}? ( ' ' | '\\t' )+
                    {
                    if ( !((implicitLineJoiningLevel>0)) ) {
                        throw new FailedPredicateException(input, "LEADING_WS", "implicitLineJoiningLevel>0");
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2473:43: ( ' ' | '\\t' )+
                    int cnt36=0;
                    loop36:
                    do {
                        int alt36=2;
                        int LA36_0 = input.LA(1);

                        if ( (LA36_0=='\t'||LA36_0==' ') ) {
                            alt36=1;
                        }


                        switch (alt36) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:
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
                    	    if ( cnt36 >= 1 ) break loop36;
                                EarlyExitException eee =
                                    new EarlyExitException(36, input);
                                throw eee;
                        }
                        cnt36++;
                    } while (true);

                    _channel=HIDDEN;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2474:14: ( ' ' | '\\t' )+ ( ( '\\r' )? '\\n' )*
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2474:14: ( ' ' | '\\t' )+
                    int cnt37=0;
                    loop37:
                    do {
                        int alt37=3;
                        int LA37_0 = input.LA(1);

                        if ( (LA37_0==' ') ) {
                            alt37=1;
                        }
                        else if ( (LA37_0=='\t') ) {
                            alt37=2;
                        }


                        switch (alt37) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2474:20: ' '
                    	    {
                    	    match(' '); 
                    	     spaces++; 

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2475:19: '\\t'
                    	    {
                    	    match('\t'); 
                    	     spaces += 8; spaces -= (spaces % 8); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt37 >= 1 ) break loop37;
                                EarlyExitException eee =
                                    new EarlyExitException(37, input);
                                throw eee;
                        }
                        cnt37++;
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2477:14: ( ( '\\r' )? '\\n' )*
                    loop39:
                    do {
                        int alt39=2;
                        int LA39_0 = input.LA(1);

                        if ( (LA39_0=='\n'||LA39_0=='\r') ) {
                            alt39=1;
                        }


                        switch (alt39) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2477:16: ( '\\r' )? '\\n'
                    	    {
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2477:16: ( '\\r' )?
                    	    int alt38=2;
                    	    int LA38_0 = input.LA(1);

                    	    if ( (LA38_0=='\r') ) {
                    	        alt38=1;
                    	    }
                    	    switch (alt38) {
                    	        case 1 :
                    	            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2477:17: '\\r'
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
                    	    break loop39;
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
                                       } else if (this.single && newlines == 1) {
                                           // This is here for this case in interactive mode:
                                           //
                                           // def foo():
                                           //   print 1
                                           //   <spaces but no code>
                                           //
                                           // The above would complete in interactive mode instead
                                           // of giving ... to wait for more input.
                                           //
                                           throw new ParseException("Trailing space in single mode.");
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

            // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2548:5: ({...}? => ( ' ' | '\\t' )* '#' (~ '\\n' )* ( '\\n' )+ | '#' (~ '\\n' )* )
            int alt45=2;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2548:10: {...}? => ( ' ' | '\\t' )* '#' (~ '\\n' )* ( '\\n' )+
                    {
                    if ( !((startPos==0)) ) {
                        throw new FailedPredicateException(input, "COMMENT", "startPos==0");
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2548:27: ( ' ' | '\\t' )*
                    loop41:
                    do {
                        int alt41=2;
                        int LA41_0 = input.LA(1);

                        if ( (LA41_0=='\t'||LA41_0==' ') ) {
                            alt41=1;
                        }


                        switch (alt41) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:
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
                    	    break loop41;
                        }
                    } while (true);

                    match('#'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2548:43: (~ '\\n' )*
                    loop42:
                    do {
                        int alt42=2;
                        int LA42_0 = input.LA(1);

                        if ( ((LA42_0>='\u0000' && LA42_0<='\t')||(LA42_0>='\u000B' && LA42_0<='\uFFFF')) ) {
                            alt42=1;
                        }


                        switch (alt42) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2548:44: ~ '\\n'
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
                    	    break loop42;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2548:52: ( '\\n' )+
                    int cnt43=0;
                    loop43:
                    do {
                        int alt43=2;
                        int LA43_0 = input.LA(1);

                        if ( (LA43_0=='\n') ) {
                            alt43=1;
                        }


                        switch (alt43) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2548:52: '\\n'
                    	    {
                    	    match('\n'); 

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


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2549:10: '#' (~ '\\n' )*
                    {
                    match('#'); 
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2549:14: (~ '\\n' )*
                    loop44:
                    do {
                        int alt44=2;
                        int LA44_0 = input.LA(1);

                        if ( ((LA44_0>='\u0000' && LA44_0<='\t')||(LA44_0>='\u000B' && LA44_0<='\uFFFF')) ) {
                            alt44=1;
                        }


                        switch (alt44) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:2549:15: ~ '\\n'
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
                    	    break loop44;
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
        // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:8: ( AS | ASSERT | BREAK | CLASS | CONTINUE | DEF | DELETE | ELIF | EXCEPT | EXEC | FINALLY | FROM | FOR | GLOBAL | IF | IMPORT | IN | IS | LAMBDA | ORELSE | PASS | PRINT | RAISE | RETURN | TRY | WHILE | WITH | YIELD | LPAREN | RPAREN | LBRACK | RBRACK | COLON | COMMA | SEMI | PLUS | MINUS | STAR | SLASH | VBAR | AMPER | LESS | GREATER | ASSIGN | PERCENT | BACKQUOTE | LCURLY | RCURLY | CIRCUMFLEX | TILDE | EQUAL | NOTEQUAL | ALT_NOTEQUAL | LESSEQUAL | LEFTSHIFT | GREATEREQUAL | RIGHTSHIFT | PLUSEQUAL | MINUSEQUAL | DOUBLESTAR | STAREQUAL | DOUBLESLASH | SLASHEQUAL | VBAREQUAL | PERCENTEQUAL | AMPEREQUAL | CIRCUMFLEXEQUAL | LEFTSHIFTEQUAL | RIGHTSHIFTEQUAL | DOUBLESTAREQUAL | DOUBLESLASHEQUAL | DOT | AT | AND | OR | NOT | FLOAT | LONGINT | INT | COMPLEX | NAME | STRING | CONTINUED_LINE | NEWLINE | WS | LEADING_WS | COMMENT )
        int alt46=87;
        alt46 = dfa46.predict(input);
        switch (alt46) {
            case 1 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:10: AS
                {
                mAS(); 

                }
                break;
            case 2 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:13: ASSERT
                {
                mASSERT(); 

                }
                break;
            case 3 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:20: BREAK
                {
                mBREAK(); 

                }
                break;
            case 4 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:26: CLASS
                {
                mCLASS(); 

                }
                break;
            case 5 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:32: CONTINUE
                {
                mCONTINUE(); 

                }
                break;
            case 6 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:41: DEF
                {
                mDEF(); 

                }
                break;
            case 7 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:45: DELETE
                {
                mDELETE(); 

                }
                break;
            case 8 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:52: ELIF
                {
                mELIF(); 

                }
                break;
            case 9 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:57: EXCEPT
                {
                mEXCEPT(); 

                }
                break;
            case 10 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:64: EXEC
                {
                mEXEC(); 

                }
                break;
            case 11 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:69: FINALLY
                {
                mFINALLY(); 

                }
                break;
            case 12 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:77: FROM
                {
                mFROM(); 

                }
                break;
            case 13 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:82: FOR
                {
                mFOR(); 

                }
                break;
            case 14 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:86: GLOBAL
                {
                mGLOBAL(); 

                }
                break;
            case 15 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:93: IF
                {
                mIF(); 

                }
                break;
            case 16 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:96: IMPORT
                {
                mIMPORT(); 

                }
                break;
            case 17 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:103: IN
                {
                mIN(); 

                }
                break;
            case 18 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:106: IS
                {
                mIS(); 

                }
                break;
            case 19 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:109: LAMBDA
                {
                mLAMBDA(); 

                }
                break;
            case 20 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:116: ORELSE
                {
                mORELSE(); 

                }
                break;
            case 21 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:123: PASS
                {
                mPASS(); 

                }
                break;
            case 22 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:128: PRINT
                {
                mPRINT(); 

                }
                break;
            case 23 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:134: RAISE
                {
                mRAISE(); 

                }
                break;
            case 24 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:140: RETURN
                {
                mRETURN(); 

                }
                break;
            case 25 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:147: TRY
                {
                mTRY(); 

                }
                break;
            case 26 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:151: WHILE
                {
                mWHILE(); 

                }
                break;
            case 27 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:157: WITH
                {
                mWITH(); 

                }
                break;
            case 28 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:162: YIELD
                {
                mYIELD(); 

                }
                break;
            case 29 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:168: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 30 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:175: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 31 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:182: LBRACK
                {
                mLBRACK(); 

                }
                break;
            case 32 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:189: RBRACK
                {
                mRBRACK(); 

                }
                break;
            case 33 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:196: COLON
                {
                mCOLON(); 

                }
                break;
            case 34 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:202: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 35 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:208: SEMI
                {
                mSEMI(); 

                }
                break;
            case 36 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:213: PLUS
                {
                mPLUS(); 

                }
                break;
            case 37 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:218: MINUS
                {
                mMINUS(); 

                }
                break;
            case 38 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:224: STAR
                {
                mSTAR(); 

                }
                break;
            case 39 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:229: SLASH
                {
                mSLASH(); 

                }
                break;
            case 40 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:235: VBAR
                {
                mVBAR(); 

                }
                break;
            case 41 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:240: AMPER
                {
                mAMPER(); 

                }
                break;
            case 42 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:246: LESS
                {
                mLESS(); 

                }
                break;
            case 43 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:251: GREATER
                {
                mGREATER(); 

                }
                break;
            case 44 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:259: ASSIGN
                {
                mASSIGN(); 

                }
                break;
            case 45 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:266: PERCENT
                {
                mPERCENT(); 

                }
                break;
            case 46 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:274: BACKQUOTE
                {
                mBACKQUOTE(); 

                }
                break;
            case 47 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:284: LCURLY
                {
                mLCURLY(); 

                }
                break;
            case 48 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:291: RCURLY
                {
                mRCURLY(); 

                }
                break;
            case 49 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:298: CIRCUMFLEX
                {
                mCIRCUMFLEX(); 

                }
                break;
            case 50 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:309: TILDE
                {
                mTILDE(); 

                }
                break;
            case 51 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:315: EQUAL
                {
                mEQUAL(); 

                }
                break;
            case 52 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:321: NOTEQUAL
                {
                mNOTEQUAL(); 

                }
                break;
            case 53 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:330: ALT_NOTEQUAL
                {
                mALT_NOTEQUAL(); 

                }
                break;
            case 54 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:343: LESSEQUAL
                {
                mLESSEQUAL(); 

                }
                break;
            case 55 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:353: LEFTSHIFT
                {
                mLEFTSHIFT(); 

                }
                break;
            case 56 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:363: GREATEREQUAL
                {
                mGREATEREQUAL(); 

                }
                break;
            case 57 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:376: RIGHTSHIFT
                {
                mRIGHTSHIFT(); 

                }
                break;
            case 58 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:387: PLUSEQUAL
                {
                mPLUSEQUAL(); 

                }
                break;
            case 59 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:397: MINUSEQUAL
                {
                mMINUSEQUAL(); 

                }
                break;
            case 60 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:408: DOUBLESTAR
                {
                mDOUBLESTAR(); 

                }
                break;
            case 61 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:419: STAREQUAL
                {
                mSTAREQUAL(); 

                }
                break;
            case 62 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:429: DOUBLESLASH
                {
                mDOUBLESLASH(); 

                }
                break;
            case 63 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:441: SLASHEQUAL
                {
                mSLASHEQUAL(); 

                }
                break;
            case 64 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:452: VBAREQUAL
                {
                mVBAREQUAL(); 

                }
                break;
            case 65 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:462: PERCENTEQUAL
                {
                mPERCENTEQUAL(); 

                }
                break;
            case 66 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:475: AMPEREQUAL
                {
                mAMPEREQUAL(); 

                }
                break;
            case 67 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:486: CIRCUMFLEXEQUAL
                {
                mCIRCUMFLEXEQUAL(); 

                }
                break;
            case 68 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:502: LEFTSHIFTEQUAL
                {
                mLEFTSHIFTEQUAL(); 

                }
                break;
            case 69 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:517: RIGHTSHIFTEQUAL
                {
                mRIGHTSHIFTEQUAL(); 

                }
                break;
            case 70 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:533: DOUBLESTAREQUAL
                {
                mDOUBLESTAREQUAL(); 

                }
                break;
            case 71 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:549: DOUBLESLASHEQUAL
                {
                mDOUBLESLASHEQUAL(); 

                }
                break;
            case 72 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:566: DOT
                {
                mDOT(); 

                }
                break;
            case 73 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:570: AT
                {
                mAT(); 

                }
                break;
            case 74 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:573: AND
                {
                mAND(); 

                }
                break;
            case 75 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:577: OR
                {
                mOR(); 

                }
                break;
            case 76 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:580: NOT
                {
                mNOT(); 

                }
                break;
            case 77 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:584: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 78 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:590: LONGINT
                {
                mLONGINT(); 

                }
                break;
            case 79 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:598: INT
                {
                mINT(); 

                }
                break;
            case 80 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:602: COMPLEX
                {
                mCOMPLEX(); 

                }
                break;
            case 81 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:610: NAME
                {
                mNAME(); 

                }
                break;
            case 82 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:615: STRING
                {
                mSTRING(); 

                }
                break;
            case 83 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:622: CONTINUED_LINE
                {
                mCONTINUED_LINE(); 

                }
                break;
            case 84 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:637: NEWLINE
                {
                mNEWLINE(); 

                }
                break;
            case 85 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:645: WS
                {
                mWS(); 

                }
                break;
            case 86 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:648: LEADING_WS
                {
                mLEADING_WS(); 

                }
                break;
            case 87 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Python.g:1:659: COMMENT
                {
                mCOMMENT(); 

                }
                break;

        }

    }


    protected DFA5 dfa5 = new DFA5(this);
    protected DFA14 dfa14 = new DFA14(this);
    protected DFA17 dfa17 = new DFA17(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA46 dfa46 = new DFA46(this);
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
            return "2351:1: FLOAT : ( '.' DIGITS ( Exponent )? | DIGITS '.' Exponent | DIGITS ( '.' ( DIGITS ( Exponent )? )? | Exponent ) );";
        }
    }
    static final String DFA14_eotS =
        "\4\uffff";
    static final String DFA14_eofS =
        "\4\uffff";
    static final String DFA14_minS =
        "\2\56\2\uffff";
    static final String DFA14_maxS =
        "\1\71\1\152\2\uffff";
    static final String DFA14_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA14_specialS =
        "\4\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\2\1\uffff\12\1",
            "\1\2\1\uffff\12\1\13\uffff\1\2\4\uffff\1\3\32\uffff\1\2\4\uffff"+
            "\1\3",
            "",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "2377:1: COMPLEX : ( ( DIGITS )+ ( 'j' | 'J' ) | FLOAT ( 'j' | 'J' ) );";
        }
    }
    static final String DFA17_eotS =
        "\24\uffff";
    static final String DFA17_eofS =
        "\24\uffff";
    static final String DFA17_minS =
        "\1\42\1\uffff\2\42\1\uffff\2\42\15\uffff";
    static final String DFA17_maxS =
        "\1\165\1\uffff\2\162\1\uffff\2\162\15\uffff";
    static final String DFA17_acceptS =
        "\1\uffff\1\1\2\uffff\1\6\2\uffff\1\17\1\4\1\13\1\2\1\5\1\16\1\3"+
        "\1\11\1\14\1\7\1\12\1\15\1\10";
    static final String DFA17_specialS =
        "\24\uffff}>";
    static final String[] DFA17_transitionS = {
            "\1\7\4\uffff\1\7\32\uffff\1\6\17\uffff\1\4\2\uffff\1\5\14\uffff"+
            "\1\3\17\uffff\1\1\2\uffff\1\2",
            "",
            "\1\12\4\uffff\1\12\52\uffff\1\11\37\uffff\1\10",
            "\1\15\4\uffff\1\15\52\uffff\1\14\37\uffff\1\13",
            "",
            "\1\20\4\uffff\1\20\52\uffff\1\16\37\uffff\1\17",
            "\1\23\4\uffff\1\23\52\uffff\1\21\37\uffff\1\22",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA17_eot = DFA.unpackEncodedString(DFA17_eotS);
    static final short[] DFA17_eof = DFA.unpackEncodedString(DFA17_eofS);
    static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars(DFA17_minS);
    static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars(DFA17_maxS);
    static final short[] DFA17_accept = DFA.unpackEncodedString(DFA17_acceptS);
    static final short[] DFA17_special = DFA.unpackEncodedString(DFA17_specialS);
    static final short[][] DFA17_transition;

    static {
        int numStates = DFA17_transitionS.length;
        DFA17_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA17_transition[i] = DFA.unpackEncodedString(DFA17_transitionS[i]);
        }
    }

    class DFA17 extends DFA {

        public DFA17(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 17;
            this.eot = DFA17_eot;
            this.eof = DFA17_eof;
            this.min = DFA17_min;
            this.max = DFA17_max;
            this.accept = DFA17_accept;
            this.special = DFA17_special;
            this.transition = DFA17_transition;
        }
        public String getDescription() {
            return "2393:9: ( 'r' | 'u' | 'b' | 'ur' | 'br' | 'R' | 'U' | 'B' | 'UR' | 'BR' | 'uR' | 'Ur' | 'Br' | 'bR' )?";
        }
    }
    static final String DFA45_eotS =
        "\2\uffff\2\4\1\uffff";
    static final String DFA45_eofS =
        "\5\uffff";
    static final String DFA45_minS =
        "\1\11\1\uffff\2\0\1\uffff";
    static final String DFA45_maxS =
        "\1\43\1\uffff\2\uffff\1\uffff";
    static final String DFA45_acceptS =
        "\1\uffff\1\1\2\uffff\1\2";
    static final String DFA45_specialS =
        "\1\1\1\uffff\1\2\1\0\1\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\1\26\uffff\1\1\2\uffff\1\2",
            "",
            "\12\3\1\1\ufff5\3",
            "\12\3\1\1\ufff5\3",
            ""
    };

    static final short[] DFA45_eot = DFA.unpackEncodedString(DFA45_eotS);
    static final short[] DFA45_eof = DFA.unpackEncodedString(DFA45_eofS);
    static final char[] DFA45_min = DFA.unpackEncodedStringToUnsignedChars(DFA45_minS);
    static final char[] DFA45_max = DFA.unpackEncodedStringToUnsignedChars(DFA45_maxS);
    static final short[] DFA45_accept = DFA.unpackEncodedString(DFA45_acceptS);
    static final short[] DFA45_special = DFA.unpackEncodedString(DFA45_specialS);
    static final short[][] DFA45_transition;

    static {
        int numStates = DFA45_transitionS.length;
        DFA45_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA45_transition[i] = DFA.unpackEncodedString(DFA45_transitionS[i]);
        }
    }

    class DFA45 extends DFA {

        public DFA45(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 45;
            this.eot = DFA45_eot;
            this.eof = DFA45_eof;
            this.min = DFA45_min;
            this.max = DFA45_max;
            this.accept = DFA45_accept;
            this.special = DFA45_special;
            this.transition = DFA45_transition;
        }
        public String getDescription() {
            return "2527:1: COMMENT : ({...}? => ( ' ' | '\\t' )* '#' (~ '\\n' )* ( '\\n' )+ | '#' (~ '\\n' )* );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA45_3 = input.LA(1);

                         
                        int index45_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA45_3>='\u0000' && LA45_3<='\t')||(LA45_3>='\u000B' && LA45_3<='\uFFFF')) ) {s = 3;}

                        else if ( (LA45_3=='\n') && ((startPos==0))) {s = 1;}

                        else s = 4;

                         
                        input.seek(index45_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA45_0 = input.LA(1);

                         
                        int index45_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA45_0=='\t'||LA45_0==' ') && ((startPos==0))) {s = 1;}

                        else if ( (LA45_0=='#') ) {s = 2;}

                         
                        input.seek(index45_0);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA45_2 = input.LA(1);

                         
                        int index45_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA45_2>='\u0000' && LA45_2<='\t')||(LA45_2>='\u000B' && LA45_2<='\uFFFF')) ) {s = 3;}

                        else if ( (LA45_2=='\n') && ((startPos==0))) {s = 1;}

                        else s = 4;

                         
                        input.seek(index45_2);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 45, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA46_eotS =
        "\1\uffff\16\60\7\uffff\1\123\1\125\1\130\1\133\1\135\1\137\1\143"+
        "\1\146\1\150\1\152\3\uffff\1\154\2\uffff\1\156\1\uffff\2\60\2\165"+
        "\4\60\3\uffff\1\u0082\1\uffff\1\u0084\1\u0086\1\uffff\1\u0088\14"+
        "\60\1\u0097\1\60\1\u0099\1\u009a\11\60\4\uffff\1\u00a5\2\uffff\1"+
        "\u00a7\10\uffff\1\u00a9\2\uffff\1\u00ab\7\uffff\1\u00ac\1\uffff"+
        "\1\u00ae\1\60\1\uffff\3\165\2\uffff\1\u00ac\3\uffff\1\165\6\60\5"+
        "\uffff\1\60\1\uffff\1\u00b8\3\60\1\u00bc\1\u00bd\6\60\1\u00c4\1"+
        "\60\1\uffff\1\60\2\uffff\5\60\1\u00cc\3\60\13\uffff\1\u00d2\3\165"+
        "\1\uffff\1\u00ac\1\uffff\1\u00ac\1\60\1\uffff\3\60\2\uffff\1\u00da"+
        "\1\u00db\1\60\1\u00dd\1\60\1\u00df\1\uffff\3\60\1\u00e3\3\60\1\uffff"+
        "\1\60\1\u00e8\1\60\1\uffff\1\u00ac\2\uffff\1\u00ac\1\uffff\1\60"+
        "\1\u00ed\1\u00ee\1\60\2\uffff\1\60\1\uffff\1\60\1\uffff\3\60\1\uffff"+
        "\1\u00f5\1\u00f6\1\60\1\u00f8\1\uffff\1\u00f9\1\uffff\1\u00ac\1"+
        "\u00fa\2\uffff\1\60\1\u00fc\1\60\1\u00fe\1\u00ff\1\u0100\2\uffff"+
        "\1\u0101\3\uffff\1\60\1\uffff\1\u0103\4\uffff\1\u0104\2\uffff";
    static final String DFA46_eofS =
        "\u0105\uffff";
    static final String DFA46_minS =
        "\1\11\1\156\1\42\1\154\1\145\1\154\1\151\1\154\1\146\2\141\1\42"+
        "\1\162\1\150\1\151\7\uffff\2\75\1\52\1\57\2\75\1\74\3\75\3\uffff"+
        "\1\75\2\uffff\1\60\1\uffff\1\162\1\157\2\56\4\42\3\uffff\1\12\1"+
        "\uffff\2\11\1\uffff\1\60\1\144\2\42\1\141\1\156\1\146\1\151\1\143"+
        "\1\156\1\157\1\162\1\157\1\60\1\160\2\60\1\155\1\163\2\151\1\164"+
        "\1\171\1\151\1\164\1\145\4\uffff\1\75\2\uffff\1\75\10\uffff\1\75"+
        "\2\uffff\1\75\7\uffff\1\60\1\uffff\1\60\1\164\3\60\1\56\2\uffff"+
        "\1\60\1\56\1\uffff\1\53\1\56\6\42\2\uffff\1\0\1\uffff\1\0\1\145"+
        "\1\uffff\1\60\1\141\1\163\1\164\2\60\1\146\2\145\1\143\1\141\1\155"+
        "\1\60\1\142\1\uffff\1\157\2\uffff\1\142\1\163\1\156\1\163\1\165"+
        "\1\60\1\154\1\150\1\154\11\uffff\1\53\1\uffff\4\60\1\53\3\60\1\162"+
        "\1\uffff\1\153\1\163\1\151\2\uffff\2\60\1\160\1\60\1\154\1\60\1"+
        "\uffff\1\141\1\162\1\144\1\60\1\164\1\145\1\162\1\uffff\1\145\1"+
        "\60\1\144\2\60\1\uffff\2\60\1\53\1\164\2\60\1\156\2\uffff\1\164"+
        "\1\uffff\1\154\1\uffff\1\154\1\164\1\141\1\uffff\2\60\1\156\1\60"+
        "\1\uffff\4\60\2\uffff\1\165\1\60\1\171\3\60\2\uffff\1\60\3\uffff"+
        "\1\145\1\uffff\1\60\4\uffff\1\60\2\uffff";
    static final String DFA46_maxS =
        "\1\176\1\163\1\162\1\157\1\145\1\170\1\162\1\154\1\163\1\141\1\162"+
        "\1\145\1\162\2\151\7\uffff\6\75\2\76\2\75\3\uffff\1\75\2\uffff\1"+
        "\71\1\uffff\1\162\1\157\1\170\1\154\1\162\1\47\2\162\3\uffff\1\15"+
        "\1\uffff\2\43\1\uffff\1\172\1\144\1\145\1\47\1\141\1\156\1\154\1"+
        "\163\1\145\1\156\1\157\1\162\1\157\1\172\1\160\2\172\1\155\1\163"+
        "\2\151\1\164\1\171\1\151\1\164\1\145\4\uffff\1\75\2\uffff\1\75\10"+
        "\uffff\1\75\2\uffff\1\75\7\uffff\1\152\1\uffff\1\172\1\164\1\146"+
        "\3\154\2\uffff\2\152\1\uffff\1\71\1\154\6\47\2\uffff\1\0\1\uffff"+
        "\1\0\1\145\1\uffff\1\172\1\141\1\163\1\164\2\172\1\146\2\145\1\143"+
        "\1\141\1\155\1\172\1\142\1\uffff\1\157\2\uffff\1\142\1\163\1\156"+
        "\1\163\1\165\1\172\1\154\1\150\1\154\11\uffff\1\71\1\uffff\1\172"+
        "\3\154\1\71\1\152\1\71\1\152\1\162\1\uffff\1\153\1\163\1\151\2\uffff"+
        "\2\172\1\160\1\172\1\154\1\172\1\uffff\1\141\1\162\1\144\1\172\1"+
        "\164\1\145\1\162\1\uffff\1\145\1\172\1\144\1\71\1\152\1\uffff\1"+
        "\71\1\152\1\71\1\164\2\172\1\156\2\uffff\1\164\1\uffff\1\154\1\uffff"+
        "\1\154\1\164\1\141\1\uffff\2\172\1\156\1\172\1\uffff\1\172\1\71"+
        "\1\152\1\172\2\uffff\1\165\1\172\1\171\3\172\2\uffff\1\172\3\uffff"+
        "\1\145\1\uffff\1\172\4\uffff\1\172\2\uffff";
    static final String DFA46_acceptS =
        "\17\uffff\1\35\1\36\1\37\1\40\1\41\1\42\1\43\12\uffff\1\56\1\57"+
        "\1\60\1\uffff\1\62\1\64\1\uffff\1\111\10\uffff\1\121\1\122\1\123"+
        "\1\uffff\1\124\2\uffff\1\127\32\uffff\1\72\1\44\1\73\1\45\1\uffff"+
        "\1\75\1\46\1\uffff\1\77\1\47\1\100\1\50\1\102\1\51\1\65\1\66\1\uffff"+
        "\1\52\1\70\1\uffff\1\53\1\63\1\54\1\101\1\55\1\103\1\61\1\uffff"+
        "\1\110\6\uffff\1\117\1\116\2\uffff\1\120\10\uffff\1\125\1\126\1"+
        "\uffff\1\127\2\uffff\1\1\16\uffff\1\17\1\uffff\1\21\1\22\11\uffff"+
        "\1\106\1\74\1\107\1\76\1\104\1\67\1\105\1\71\1\115\1\uffff\1\113"+
        "\11\uffff\1\112\3\uffff\1\6\1\7\6\uffff\1\15\7\uffff\1\31\5\uffff"+
        "\1\114\7\uffff\1\10\1\24\1\uffff\1\12\1\uffff\1\14\3\uffff\1\25"+
        "\4\uffff\1\33\4\uffff\1\3\1\4\6\uffff\1\26\1\27\1\uffff\1\32\1\34"+
        "\1\2\1\uffff\1\11\1\uffff\1\16\1\20\1\23\1\30\1\uffff\1\13\1\5";
    static final String DFA46_specialS =
        "\1\3\62\uffff\1\2\1\uffff\1\1\1\0\115\uffff\1\4\1\uffff\1\5\176"+
        "\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\66\1\64\1\uffff\1\63\1\64\22\uffff\1\65\1\45\1\61\1\67\1"+
            "\uffff\1\37\1\33\1\61\1\17\1\20\1\30\1\26\1\24\1\27\1\46\1\31"+
            "\1\52\11\53\1\23\1\25\1\34\1\36\1\35\1\uffff\1\47\1\60\1\57"+
            "\17\60\1\55\2\60\1\56\5\60\1\21\1\62\1\22\1\43\1\60\1\40\1\1"+
            "\1\2\1\3\1\4\1\5\1\6\1\7\1\60\1\10\2\60\1\11\1\60\1\51\1\50"+
            "\1\12\1\60\1\13\1\60\1\14\1\54\1\60\1\15\1\60\1\16\1\60\1\41"+
            "\1\32\1\42\1\44",
            "\1\71\4\uffff\1\70",
            "\1\61\4\uffff\1\61\52\uffff\1\73\37\uffff\1\72",
            "\1\74\2\uffff\1\75",
            "\1\76",
            "\1\77\13\uffff\1\100",
            "\1\101\5\uffff\1\103\2\uffff\1\102",
            "\1\104",
            "\1\105\6\uffff\1\106\1\107\4\uffff\1\110",
            "\1\111",
            "\1\112\20\uffff\1\113",
            "\1\61\4\uffff\1\61\71\uffff\1\114\3\uffff\1\115",
            "\1\116",
            "\1\117\1\120",
            "\1\121",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\122",
            "\1\124",
            "\1\126\22\uffff\1\127",
            "\1\131\15\uffff\1\132",
            "\1\134",
            "\1\136",
            "\1\142\1\141\1\140",
            "\1\144\1\145",
            "\1\147",
            "\1\151",
            "",
            "",
            "",
            "\1\153",
            "",
            "",
            "\12\155",
            "",
            "\1\157",
            "\1\160",
            "\1\167\1\uffff\10\164\2\170\10\uffff\1\163\2\uffff\1\172\4"+
            "\uffff\1\171\1\uffff\1\166\2\uffff\1\162\10\uffff\1\161\11\uffff"+
            "\1\163\2\uffff\1\172\4\uffff\1\171\1\uffff\1\166\2\uffff\1\162"+
            "\10\uffff\1\161",
            "\1\167\1\uffff\12\173\13\uffff\1\172\4\uffff\1\171\1\uffff"+
            "\1\166\30\uffff\1\172\4\uffff\1\171\1\uffff\1\166",
            "\1\61\4\uffff\1\61\52\uffff\1\175\37\uffff\1\174",
            "\1\61\4\uffff\1\61",
            "\1\61\4\uffff\1\61\52\uffff\1\176\37\uffff\1\177",
            "\1\61\4\uffff\1\61\52\uffff\1\u0080\37\uffff\1\u0081",
            "",
            "",
            "",
            "\1\64\2\uffff\1\64",
            "",
            "\1\66\1\u0083\1\uffff\1\u0082\1\u0083\22\uffff\1\65\2\uffff"+
            "\1\u0085",
            "\1\66\1\u0083\1\uffff\1\u0082\1\u0083\22\uffff\1\65\2\uffff"+
            "\1\u0085",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\22\60\1\u0087\7\60",
            "\1\u0089",
            "\1\61\4\uffff\1\61\75\uffff\1\u008a",
            "\1\61\4\uffff\1\61",
            "\1\u008b",
            "\1\u008c",
            "\1\u008d\5\uffff\1\u008e",
            "\1\u008f\11\uffff\1\u0090",
            "\1\u0091\1\uffff\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u0098",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u009b",
            "\1\u009c",
            "\1\u009d",
            "\1\u009e",
            "\1\u009f",
            "\1\u00a0",
            "\1\u00a1",
            "\1\u00a2",
            "\1\u00a3",
            "",
            "",
            "",
            "",
            "\1\u00a4",
            "",
            "",
            "\1\u00a6",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00a8",
            "",
            "",
            "\1\u00aa",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\155\13\uffff\1\u00ad\4\uffff\1\171\32\uffff\1\u00ad\4\uffff"+
            "\1\171",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00af",
            "\12\u00b0\7\uffff\6\u00b0\32\uffff\6\u00b0",
            "\10\u00b1\24\uffff\1\166\37\uffff\1\166",
            "\2\u00b2\32\uffff\1\166\37\uffff\1\166",
            "\1\167\1\uffff\10\164\2\170\13\uffff\1\172\4\uffff\1\171\1"+
            "\uffff\1\166\30\uffff\1\172\4\uffff\1\171\1\uffff\1\166",
            "",
            "",
            "\12\u00b4\13\uffff\1\u00b3\4\uffff\1\171\32\uffff\1\u00b3\4"+
            "\uffff\1\171",
            "\1\167\1\uffff\12\170\13\uffff\1\172\4\uffff\1\171\32\uffff"+
            "\1\172\4\uffff\1\171",
            "",
            "\1\u00b5\1\uffff\1\u00b5\2\uffff\12\u00b6",
            "\1\167\1\uffff\12\173\13\uffff\1\172\4\uffff\1\171\1\uffff"+
            "\1\166\30\uffff\1\172\4\uffff\1\171\1\uffff\1\166",
            "\1\61\4\uffff\1\61",
            "\1\61\4\uffff\1\61",
            "\1\61\4\uffff\1\61",
            "\1\61\4\uffff\1\61",
            "\1\61\4\uffff\1\61",
            "\1\61\4\uffff\1\61",
            "",
            "",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\u00b7",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00b9",
            "\1\u00ba",
            "\1\u00bb",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00be",
            "\1\u00bf",
            "\1\u00c0",
            "\1\u00c1",
            "\1\u00c2",
            "\1\u00c3",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00c5",
            "",
            "\1\u00c6",
            "",
            "",
            "\1\u00c7",
            "\1\u00c8",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00cd",
            "\1\u00ce",
            "\1\u00cf",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u00d0\1\uffff\1\u00d0\2\uffff\12\u00d1",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\u00b0\7\uffff\6\u00b0\5\uffff\1\166\24\uffff\6\u00b0\5"+
            "\uffff\1\166",
            "\10\u00b1\24\uffff\1\166\37\uffff\1\166",
            "\2\u00b2\32\uffff\1\166\37\uffff\1\166",
            "\1\u00d3\1\uffff\1\u00d3\2\uffff\12\u00d4",
            "\12\u00b4\13\uffff\1\u00d5\4\uffff\1\171\32\uffff\1\u00d5\4"+
            "\uffff\1\171",
            "\12\u00b6",
            "\12\u00b6\20\uffff\1\171\37\uffff\1\171",
            "\1\u00d6",
            "",
            "\1\u00d7",
            "\1\u00d8",
            "\1\u00d9",
            "",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00dc",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00de",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00e0",
            "\1\u00e1",
            "\1\u00e2",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00e4",
            "\1\u00e5",
            "\1\u00e6",
            "",
            "\1\u00e7",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00e9",
            "\12\u00d1",
            "\12\u00d1\20\uffff\1\171\37\uffff\1\171",
            "",
            "\12\u00d4",
            "\12\u00d4\20\uffff\1\171\37\uffff\1\171",
            "\1\u00ea\1\uffff\1\u00ea\2\uffff\12\u00eb",
            "\1\u00ec",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00ef",
            "",
            "",
            "\1\u00f0",
            "",
            "\1\u00f1",
            "",
            "\1\u00f2",
            "\1\u00f3",
            "\1\u00f4",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00f7",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\u00eb",
            "\12\u00eb\20\uffff\1\171\37\uffff\1\171",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "",
            "\1\u00fb",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00fd",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "",
            "",
            "\1\u0102",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "",
            "",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            ""
    };

    static final short[] DFA46_eot = DFA.unpackEncodedString(DFA46_eotS);
    static final short[] DFA46_eof = DFA.unpackEncodedString(DFA46_eofS);
    static final char[] DFA46_min = DFA.unpackEncodedStringToUnsignedChars(DFA46_minS);
    static final char[] DFA46_max = DFA.unpackEncodedStringToUnsignedChars(DFA46_maxS);
    static final short[] DFA46_accept = DFA.unpackEncodedString(DFA46_acceptS);
    static final short[] DFA46_special = DFA.unpackEncodedString(DFA46_specialS);
    static final short[][] DFA46_transition;

    static {
        int numStates = DFA46_transitionS.length;
        DFA46_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA46_transition[i] = DFA.unpackEncodedString(DFA46_transitionS[i]);
        }
    }

    class DFA46 extends DFA {

        public DFA46(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 46;
            this.eot = DFA46_eot;
            this.eof = DFA46_eof;
            this.min = DFA46_min;
            this.max = DFA46_max;
            this.accept = DFA46_accept;
            this.special = DFA46_special;
            this.transition = DFA46_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( AS | ASSERT | BREAK | CLASS | CONTINUE | DEF | DELETE | ELIF | EXCEPT | EXEC | FINALLY | FROM | FOR | GLOBAL | IF | IMPORT | IN | IS | LAMBDA | ORELSE | PASS | PRINT | RAISE | RETURN | TRY | WHILE | WITH | YIELD | LPAREN | RPAREN | LBRACK | RBRACK | COLON | COMMA | SEMI | PLUS | MINUS | STAR | SLASH | VBAR | AMPER | LESS | GREATER | ASSIGN | PERCENT | BACKQUOTE | LCURLY | RCURLY | CIRCUMFLEX | TILDE | EQUAL | NOTEQUAL | ALT_NOTEQUAL | LESSEQUAL | LEFTSHIFT | GREATEREQUAL | RIGHTSHIFT | PLUSEQUAL | MINUSEQUAL | DOUBLESTAR | STAREQUAL | DOUBLESLASH | SLASHEQUAL | VBAREQUAL | PERCENTEQUAL | AMPEREQUAL | CIRCUMFLEXEQUAL | LEFTSHIFTEQUAL | RIGHTSHIFTEQUAL | DOUBLESTAREQUAL | DOUBLESLASHEQUAL | DOT | AT | AND | OR | NOT | FLOAT | LONGINT | INT | COMPLEX | NAME | STRING | CONTINUED_LINE | NEWLINE | WS | LEADING_WS | COMMENT );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA46_54 = input.LA(1);

                         
                        int index46_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA46_54=='\n'||LA46_54=='\r') && ((startPos==0))) {s = 131;}

                        else if ( (LA46_54==' ') && (((startPos==0)||(startPos>0)))) {s = 53;}

                        else if ( (LA46_54=='\t') && (((startPos==0)||(startPos>0)))) {s = 54;}

                        else if ( (LA46_54=='#') && ((startPos==0))) {s = 133;}

                        else if ( (LA46_54=='\f') && ((startPos>0))) {s = 130;}

                        else s = 134;

                         
                        input.seek(index46_54);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA46_53 = input.LA(1);

                         
                        int index46_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA46_53=='\n'||LA46_53=='\r') && ((startPos==0))) {s = 131;}

                        else if ( (LA46_53==' ') && (((startPos==0)||(startPos>0)))) {s = 53;}

                        else if ( (LA46_53=='\t') && (((startPos==0)||(startPos>0)))) {s = 54;}

                        else if ( (LA46_53=='#') && ((startPos==0))) {s = 133;}

                        else if ( (LA46_53=='\f') && ((startPos>0))) {s = 130;}

                        else s = 132;

                         
                        input.seek(index46_53);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA46_51 = input.LA(1);

                         
                        int index46_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA46_51=='\n'||LA46_51=='\r') ) {s = 52;}

                        else s = 130;

                         
                        input.seek(index46_51);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA46_0 = input.LA(1);

                         
                        int index46_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA46_0=='a') ) {s = 1;}

                        else if ( (LA46_0=='b') ) {s = 2;}

                        else if ( (LA46_0=='c') ) {s = 3;}

                        else if ( (LA46_0=='d') ) {s = 4;}

                        else if ( (LA46_0=='e') ) {s = 5;}

                        else if ( (LA46_0=='f') ) {s = 6;}

                        else if ( (LA46_0=='g') ) {s = 7;}

                        else if ( (LA46_0=='i') ) {s = 8;}

                        else if ( (LA46_0=='l') ) {s = 9;}

                        else if ( (LA46_0=='p') ) {s = 10;}

                        else if ( (LA46_0=='r') ) {s = 11;}

                        else if ( (LA46_0=='t') ) {s = 12;}

                        else if ( (LA46_0=='w') ) {s = 13;}

                        else if ( (LA46_0=='y') ) {s = 14;}

                        else if ( (LA46_0=='(') ) {s = 15;}

                        else if ( (LA46_0==')') ) {s = 16;}

                        else if ( (LA46_0=='[') ) {s = 17;}

                        else if ( (LA46_0==']') ) {s = 18;}

                        else if ( (LA46_0==':') ) {s = 19;}

                        else if ( (LA46_0==',') ) {s = 20;}

                        else if ( (LA46_0==';') ) {s = 21;}

                        else if ( (LA46_0=='+') ) {s = 22;}

                        else if ( (LA46_0=='-') ) {s = 23;}

                        else if ( (LA46_0=='*') ) {s = 24;}

                        else if ( (LA46_0=='/') ) {s = 25;}

                        else if ( (LA46_0=='|') ) {s = 26;}

                        else if ( (LA46_0=='&') ) {s = 27;}

                        else if ( (LA46_0=='<') ) {s = 28;}

                        else if ( (LA46_0=='>') ) {s = 29;}

                        else if ( (LA46_0=='=') ) {s = 30;}

                        else if ( (LA46_0=='%') ) {s = 31;}

                        else if ( (LA46_0=='`') ) {s = 32;}

                        else if ( (LA46_0=='{') ) {s = 33;}

                        else if ( (LA46_0=='}') ) {s = 34;}

                        else if ( (LA46_0=='^') ) {s = 35;}

                        else if ( (LA46_0=='~') ) {s = 36;}

                        else if ( (LA46_0=='!') ) {s = 37;}

                        else if ( (LA46_0=='.') ) {s = 38;}

                        else if ( (LA46_0=='@') ) {s = 39;}

                        else if ( (LA46_0=='o') ) {s = 40;}

                        else if ( (LA46_0=='n') ) {s = 41;}

                        else if ( (LA46_0=='0') ) {s = 42;}

                        else if ( ((LA46_0>='1' && LA46_0<='9')) ) {s = 43;}

                        else if ( (LA46_0=='u') ) {s = 44;}

                        else if ( (LA46_0=='R') ) {s = 45;}

                        else if ( (LA46_0=='U') ) {s = 46;}

                        else if ( (LA46_0=='B') ) {s = 47;}

                        else if ( (LA46_0=='A'||(LA46_0>='C' && LA46_0<='Q')||(LA46_0>='S' && LA46_0<='T')||(LA46_0>='V' && LA46_0<='Z')||LA46_0=='_'||LA46_0=='h'||(LA46_0>='j' && LA46_0<='k')||LA46_0=='m'||LA46_0=='q'||LA46_0=='s'||LA46_0=='v'||LA46_0=='x'||LA46_0=='z') ) {s = 48;}

                        else if ( (LA46_0=='\"'||LA46_0=='\'') ) {s = 49;}

                        else if ( (LA46_0=='\\') ) {s = 50;}

                        else if ( (LA46_0=='\f') ) {s = 51;}

                        else if ( (LA46_0=='\n'||LA46_0=='\r') ) {s = 52;}

                        else if ( (LA46_0==' ') && (((startPos==0)||(startPos>0)))) {s = 53;}

                        else if ( (LA46_0=='\t') && (((startPos==0)||(startPos>0)))) {s = 54;}

                        else if ( (LA46_0=='#') ) {s = 55;}

                         
                        input.seek(index46_0);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA46_132 = input.LA(1);

                         
                        int index46_132 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((startPos>0)) ) {s = 130;}

                        else if ( ((((startPos==0)&&(implicitLineJoiningLevel>0))||(startPos==0))) ) {s = 131;}

                         
                        input.seek(index46_132);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA46_134 = input.LA(1);

                         
                        int index46_134 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((startPos>0)) ) {s = 130;}

                        else if ( ((((startPos==0)&&(implicitLineJoiningLevel>0))||(startPos==0))) ) {s = 131;}

                         
                        input.seek(index46_134);
                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 46, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}