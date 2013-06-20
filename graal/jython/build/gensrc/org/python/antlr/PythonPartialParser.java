// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g 2013-06-19 17:22:41

package org.python.antlr;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
/** Python 2.3.3 Grammar
 *
 *  Terence Parr and Loring Craymer
 *  February 2004
 *
 *  Converted to ANTLR v3 November 2005 by Terence Parr.
 *
 *  This grammar was derived automatically from the Python 2.3.3
 *  parser grammar to get a syntactically correct ANTLR grammar
 *  for Python.  Then Terence hand tweaked it to be semantically
 *  correct; i.e., removed lookahead issues etc...  It is LL(1)
 *  except for the (sometimes optional) trailing commas and semi-colons.
 *  It needs two symbols of lookahead in this case.
 *
 *  Starting with Loring's preliminary lexer for Python, I modified it
 *  to do my version of the whole nasty INDENT/DEDENT issue just so I
 *  could understand the problem better.  This grammar requires
 *  PythonTokenStream.java to work.  Also I used some rules from the
 *  semi-formal grammar on the web for Python (automatically
 *  translated to ANTLR format by an ANTLR grammar, naturally <grin>).
 *  The lexical rules for python are particularly nasty and it took me
 *  a long time to get it 'right'; i.e., think about it in the proper
 *  way.  Resist changing the lexer unless you've used ANTLR a lot. ;)
 *
 *  I (Terence) tested this by running it on the jython-2.1/Lib
 *  directory of 40k lines of Python.
 *
 *  REQUIRES ANTLR v3
 *
 *  Updated to Python 2.5 by Frank Wierzbicki.
 *
 */
public class PythonPartialParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "INDENT", "DEDENT", "TRAILBACKSLASH", "NEWLINE", "LEADING_WS", "NAME", "DOT", "PRINT", "AND", "AS", "ASSERT", "BREAK", "CLASS", "CONTINUE", "DEF", "DELETE", "ELIF", "EXCEPT", "EXEC", "FINALLY", "FROM", "FOR", "GLOBAL", "IF", "IMPORT", "IN", "IS", "LAMBDA", "NOT", "OR", "ORELSE", "PASS", "RAISE", "RETURN", "TRY", "WHILE", "WITH", "YIELD", "AT", "LPAREN", "RPAREN", "COLON", "ASSIGN", "COMMA", "STAR", "DOUBLESTAR", "SEMI", "PLUSEQUAL", "MINUSEQUAL", "STAREQUAL", "SLASHEQUAL", "PERCENTEQUAL", "AMPEREQUAL", "VBAREQUAL", "CIRCUMFLEXEQUAL", "LEFTSHIFTEQUAL", "RIGHTSHIFTEQUAL", "DOUBLESTAREQUAL", "DOUBLESLASHEQUAL", "RIGHTSHIFT", "LESS", "GREATER", "EQUAL", "GREATEREQUAL", "LESSEQUAL", "ALT_NOTEQUAL", "NOTEQUAL", "VBAR", "CIRCUMFLEX", "AMPER", "LEFTSHIFT", "PLUS", "MINUS", "SLASH", "PERCENT", "DOUBLESLASH", "TILDE", "LBRACK", "RBRACK", "LCURLY", "RCURLY", "BACKQUOTE", "INT", "LONGINT", "FLOAT", "COMPLEX", "STRING", "DIGITS", "Exponent", "TRIAPOS", "TRIQUOTE", "ESC", "COMMENT", "CONTINUED_LINE", "WS", "TRISTRINGPART", "STRINGPART"
    };
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

    // delegates
    // delegators


        public PythonPartialParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PythonPartialParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PythonPartialParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g"; }


        private ErrorHandler errorHandler = new FailFastHandler();

        protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow)
            throws RecognitionException {

            Object o = errorHandler.recoverFromMismatchedToken(this, input, ttype, follow);
            if (o != null) {
                return o;
            }
            return super.recoverFromMismatchedToken(input, ttype, follow);
        }




    // $ANTLR start "single_input"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:166:1: single_input : ( NEWLINE | simple_stmt | compound_stmt ( NEWLINE )? );
    public final void single_input() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:168:5: ( NEWLINE | simple_stmt | compound_stmt ( NEWLINE )? )
            int alt2=3;
            switch ( input.LA(1) ) {
            case NEWLINE:
                {
                alt2=1;
                }
                break;
            case TRAILBACKSLASH:
            case NAME:
            case PRINT:
            case ASSERT:
            case BREAK:
            case CONTINUE:
            case DELETE:
            case EXEC:
            case FROM:
            case GLOBAL:
            case IMPORT:
            case LAMBDA:
            case NOT:
            case PASS:
            case RAISE:
            case RETURN:
            case YIELD:
            case LPAREN:
            case PLUS:
            case MINUS:
            case TILDE:
            case LBRACK:
            case LCURLY:
            case BACKQUOTE:
            case INT:
            case LONGINT:
            case FLOAT:
            case COMPLEX:
            case STRING:
            case TRISTRINGPART:
            case STRINGPART:
                {
                alt2=2;
                }
                break;
            case CLASS:
            case DEF:
            case FOR:
            case IF:
            case TRY:
            case WHILE:
            case WITH:
            case AT:
                {
                alt2=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:168:7: NEWLINE
                    {
                    match(input,NEWLINE,FOLLOW_NEWLINE_in_single_input72); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:169:7: simple_stmt
                    {
                    pushFollow(FOLLOW_simple_stmt_in_single_input80);
                    simple_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:170:7: compound_stmt ( NEWLINE )?
                    {
                    pushFollow(FOLLOW_compound_stmt_in_single_input88);
                    compound_stmt();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:170:21: ( NEWLINE )?
                    int alt1=2;
                    int LA1_0 = input.LA(1);

                    if ( (LA1_0==NEWLINE) ) {
                        alt1=1;
                    }
                    switch (alt1) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:170:21: NEWLINE
                            {
                            match(input,NEWLINE,FOLLOW_NEWLINE_in_single_input90); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "single_input"


    // $ANTLR start "eval_input"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:174:1: eval_input : ( LEADING_WS )? ( NEWLINE )* ( testlist )? ( NEWLINE )* EOF ;
    public final void eval_input() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:5: ( ( LEADING_WS )? ( NEWLINE )* ( testlist )? ( NEWLINE )* EOF )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:7: ( LEADING_WS )? ( NEWLINE )* ( testlist )? ( NEWLINE )* EOF
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:7: ( LEADING_WS )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==LEADING_WS) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:7: LEADING_WS
                    {
                    match(input,LEADING_WS,FOLLOW_LEADING_WS_in_eval_input109); if (state.failed) return ;

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:19: ( NEWLINE )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==NEWLINE) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:20: NEWLINE
            	    {
            	    match(input,NEWLINE,FOLLOW_NEWLINE_in_eval_input113); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:30: ( testlist )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==TRAILBACKSLASH||LA5_0==NAME||(LA5_0>=LAMBDA && LA5_0<=NOT)||LA5_0==LPAREN||(LA5_0>=PLUS && LA5_0<=MINUS)||(LA5_0>=TILDE && LA5_0<=LBRACK)||LA5_0==LCURLY||(LA5_0>=BACKQUOTE && LA5_0<=STRING)||(LA5_0>=TRISTRINGPART && LA5_0<=STRINGPART)) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:30: testlist
                    {
                    pushFollow(FOLLOW_testlist_in_eval_input117);
                    testlist();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:40: ( NEWLINE )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==NEWLINE) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:175:41: NEWLINE
            	    {
            	    match(input,NEWLINE,FOLLOW_NEWLINE_in_eval_input121); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_eval_input125); if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "eval_input"


    // $ANTLR start "dotted_attr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:179:1: dotted_attr : NAME ( ( DOT NAME )+ | ) ;
    public final void dotted_attr() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:180:5: ( NAME ( ( DOT NAME )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:180:7: NAME ( ( DOT NAME )+ | )
            {
            match(input,NAME,FOLLOW_NAME_in_dotted_attr143); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:181:7: ( ( DOT NAME )+ | )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==DOT) ) {
                alt8=1;
            }
            else if ( (LA8_0==NEWLINE||LA8_0==LPAREN) ) {
                alt8=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:181:9: ( DOT NAME )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:181:9: ( DOT NAME )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==DOT) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:181:10: DOT NAME
                    	    {
                    	    match(input,DOT,FOLLOW_DOT_in_dotted_attr154); if (state.failed) return ;
                    	    match(input,NAME,FOLLOW_NAME_in_dotted_attr156); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt7 >= 1 ) break loop7;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(7, input);
                                throw eee;
                        }
                        cnt7++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:183:7: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "dotted_attr"


    // $ANTLR start "attr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:189:1: attr : ( NAME | AND | AS | ASSERT | BREAK | CLASS | CONTINUE | DEF | DELETE | ELIF | EXCEPT | EXEC | FINALLY | FROM | FOR | GLOBAL | IF | IMPORT | IN | IS | LAMBDA | NOT | OR | ORELSE | PASS | PRINT | RAISE | RETURN | TRY | WHILE | WITH | YIELD );
    public final void attr() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:190:5: ( NAME | AND | AS | ASSERT | BREAK | CLASS | CONTINUE | DEF | DELETE | ELIF | EXCEPT | EXEC | FINALLY | FROM | FOR | GLOBAL | IF | IMPORT | IN | IS | LAMBDA | NOT | OR | ORELSE | PASS | PRINT | RAISE | RETURN | TRY | WHILE | WITH | YIELD )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
            {
            if ( input.LA(1)==NAME||(input.LA(1)>=PRINT && input.LA(1)<=YIELD) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "attr"


    // $ANTLR start "decorator"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:225:1: decorator : AT dotted_attr ( LPAREN ( arglist | ) RPAREN | ) NEWLINE ;
    public final void decorator() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:226:5: ( AT dotted_attr ( LPAREN ( arglist | ) RPAREN | ) NEWLINE )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:226:7: AT dotted_attr ( LPAREN ( arglist | ) RPAREN | ) NEWLINE
            {
            match(input,AT,FOLLOW_AT_in_decorator460); if (state.failed) return ;
            pushFollow(FOLLOW_dotted_attr_in_decorator462);
            dotted_attr();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:227:5: ( LPAREN ( arglist | ) RPAREN | )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==LPAREN) ) {
                alt10=1;
            }
            else if ( (LA10_0==NEWLINE) ) {
                alt10=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:227:7: LPAREN ( arglist | ) RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_decorator470); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:228:7: ( arglist | )
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==TRAILBACKSLASH||LA9_0==NAME||(LA9_0>=LAMBDA && LA9_0<=NOT)||LA9_0==LPAREN||(LA9_0>=STAR && LA9_0<=DOUBLESTAR)||(LA9_0>=PLUS && LA9_0<=MINUS)||(LA9_0>=TILDE && LA9_0<=LBRACK)||LA9_0==LCURLY||(LA9_0>=BACKQUOTE && LA9_0<=STRING)||(LA9_0>=TRISTRINGPART && LA9_0<=STRINGPART)) ) {
                        alt9=1;
                    }
                    else if ( (LA9_0==RPAREN) ) {
                        alt9=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 9, 0, input);

                        throw nvae;
                    }
                    switch (alt9) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:228:9: arglist
                            {
                            pushFollow(FOLLOW_arglist_in_decorator480);
                            arglist();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:230:7: 
                            {
                            }
                            break;

                    }

                    match(input,RPAREN,FOLLOW_RPAREN_in_decorator504); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:233:5: 
                    {
                    }
                    break;

            }

            match(input,NEWLINE,FOLLOW_NEWLINE_in_decorator518); if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "decorator"


    // $ANTLR start "decorators"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:237:1: decorators : ( decorator )+ ;
    public final void decorators() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:238:5: ( ( decorator )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:238:7: ( decorator )+
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:238:7: ( decorator )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==AT) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:238:7: decorator
            	    {
            	    pushFollow(FOLLOW_decorator_in_decorators536);
            	    decorator();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
            } while (true);


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "decorators"


    // $ANTLR start "funcdef"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:242:1: funcdef : ( decorators )? DEF NAME parameters COLON suite ;
    public final void funcdef() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:243:5: ( ( decorators )? DEF NAME parameters COLON suite )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:243:7: ( decorators )? DEF NAME parameters COLON suite
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:243:7: ( decorators )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==AT) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:243:7: decorators
                    {
                    pushFollow(FOLLOW_decorators_in_funcdef555);
                    decorators();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,DEF,FOLLOW_DEF_in_funcdef558); if (state.failed) return ;
            match(input,NAME,FOLLOW_NAME_in_funcdef560); if (state.failed) return ;
            pushFollow(FOLLOW_parameters_in_funcdef562);
            parameters();

            state._fsp--;
            if (state.failed) return ;
            match(input,COLON,FOLLOW_COLON_in_funcdef564); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_funcdef566);
            suite();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "funcdef"


    // $ANTLR start "parameters"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:247:1: parameters : LPAREN ( varargslist | ) RPAREN ;
    public final void parameters() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:248:5: ( LPAREN ( varargslist | ) RPAREN )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:248:7: LPAREN ( varargslist | ) RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_parameters584); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:249:7: ( varargslist | )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==NAME||LA13_0==LPAREN||(LA13_0>=STAR && LA13_0<=DOUBLESTAR)) ) {
                alt13=1;
            }
            else if ( (LA13_0==RPAREN) ) {
                alt13=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:249:8: varargslist
                    {
                    pushFollow(FOLLOW_varargslist_in_parameters593);
                    varargslist();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:251:7: 
                    {
                    }
                    break;

            }

            match(input,RPAREN,FOLLOW_RPAREN_in_parameters617); if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "parameters"


    // $ANTLR start "defparameter"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:256:1: defparameter : fpdef ( ASSIGN test )? ;
    public final void defparameter() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:257:5: ( fpdef ( ASSIGN test )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:257:7: fpdef ( ASSIGN test )?
            {
            pushFollow(FOLLOW_fpdef_in_defparameter635);
            fpdef();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:257:13: ( ASSIGN test )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==ASSIGN) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:257:14: ASSIGN test
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_defparameter638); if (state.failed) return ;
                    pushFollow(FOLLOW_test_in_defparameter640);
                    test();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "defparameter"


    // $ANTLR start "varargslist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:263:1: varargslist : ( defparameter ( options {greedy=true; } : COMMA defparameter )* ( COMMA ( STAR NAME ( COMMA DOUBLESTAR NAME )? | DOUBLESTAR NAME )? )? | STAR NAME ( COMMA DOUBLESTAR NAME )? | DOUBLESTAR NAME );
    public final void varargslist() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:264:5: ( defparameter ( options {greedy=true; } : COMMA defparameter )* ( COMMA ( STAR NAME ( COMMA DOUBLESTAR NAME )? | DOUBLESTAR NAME )? )? | STAR NAME ( COMMA DOUBLESTAR NAME )? | DOUBLESTAR NAME )
            int alt20=3;
            switch ( input.LA(1) ) {
            case NAME:
            case LPAREN:
                {
                alt20=1;
                }
                break;
            case STAR:
                {
                alt20=2;
                }
                break;
            case DOUBLESTAR:
                {
                alt20=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }

            switch (alt20) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:264:7: defparameter ( options {greedy=true; } : COMMA defparameter )* ( COMMA ( STAR NAME ( COMMA DOUBLESTAR NAME )? | DOUBLESTAR NAME )? )?
                    {
                    pushFollow(FOLLOW_defparameter_in_varargslist662);
                    defparameter();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:264:20: ( options {greedy=true; } : COMMA defparameter )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( (LA15_0==COMMA) ) {
                            int LA15_1 = input.LA(2);

                            if ( (LA15_1==NAME||LA15_1==LPAREN) ) {
                                alt15=1;
                            }


                        }


                        switch (alt15) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:264:44: COMMA defparameter
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_varargslist672); if (state.failed) return ;
                    	    pushFollow(FOLLOW_defparameter_in_varargslist674);
                    	    defparameter();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:265:7: ( COMMA ( STAR NAME ( COMMA DOUBLESTAR NAME )? | DOUBLESTAR NAME )? )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==COMMA) ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:265:8: COMMA ( STAR NAME ( COMMA DOUBLESTAR NAME )? | DOUBLESTAR NAME )?
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_varargslist685); if (state.failed) return ;
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:266:11: ( STAR NAME ( COMMA DOUBLESTAR NAME )? | DOUBLESTAR NAME )?
                            int alt17=3;
                            int LA17_0 = input.LA(1);

                            if ( (LA17_0==STAR) ) {
                                alt17=1;
                            }
                            else if ( (LA17_0==DOUBLESTAR) ) {
                                alt17=2;
                            }
                            switch (alt17) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:266:12: STAR NAME ( COMMA DOUBLESTAR NAME )?
                                    {
                                    match(input,STAR,FOLLOW_STAR_in_varargslist698); if (state.failed) return ;
                                    match(input,NAME,FOLLOW_NAME_in_varargslist700); if (state.failed) return ;
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:266:22: ( COMMA DOUBLESTAR NAME )?
                                    int alt16=2;
                                    int LA16_0 = input.LA(1);

                                    if ( (LA16_0==COMMA) ) {
                                        alt16=1;
                                    }
                                    switch (alt16) {
                                        case 1 :
                                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:266:23: COMMA DOUBLESTAR NAME
                                            {
                                            match(input,COMMA,FOLLOW_COMMA_in_varargslist703); if (state.failed) return ;
                                            match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_varargslist705); if (state.failed) return ;
                                            match(input,NAME,FOLLOW_NAME_in_varargslist707); if (state.failed) return ;

                                            }
                                            break;

                                    }


                                    }
                                    break;
                                case 2 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:267:13: DOUBLESTAR NAME
                                    {
                                    match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_varargslist723); if (state.failed) return ;
                                    match(input,NAME,FOLLOW_NAME_in_varargslist725); if (state.failed) return ;

                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:270:7: STAR NAME ( COMMA DOUBLESTAR NAME )?
                    {
                    match(input,STAR,FOLLOW_STAR_in_varargslist755); if (state.failed) return ;
                    match(input,NAME,FOLLOW_NAME_in_varargslist757); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:270:17: ( COMMA DOUBLESTAR NAME )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==COMMA) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:270:18: COMMA DOUBLESTAR NAME
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_varargslist760); if (state.failed) return ;
                            match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_varargslist762); if (state.failed) return ;
                            match(input,NAME,FOLLOW_NAME_in_varargslist764); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:271:7: DOUBLESTAR NAME
                    {
                    match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_varargslist774); if (state.failed) return ;
                    match(input,NAME,FOLLOW_NAME_in_varargslist776); if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "varargslist"


    // $ANTLR start "fpdef"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:275:1: fpdef : ( NAME | ( LPAREN fpdef COMMA )=> LPAREN fplist RPAREN | LPAREN fplist RPAREN );
    public final void fpdef() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:276:5: ( NAME | ( LPAREN fpdef COMMA )=> LPAREN fplist RPAREN | LPAREN fplist RPAREN )
            int alt21=3;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==NAME) ) {
                alt21=1;
            }
            else if ( (LA21_0==LPAREN) ) {
                int LA21_2 = input.LA(2);

                if ( (synpred1_PythonPartial()) ) {
                    alt21=2;
                }
                else if ( (true) ) {
                    alt21=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 21, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:276:7: NAME
                    {
                    match(input,NAME,FOLLOW_NAME_in_fpdef794); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:277:7: ( LPAREN fpdef COMMA )=> LPAREN fplist RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_fpdef812); if (state.failed) return ;
                    pushFollow(FOLLOW_fplist_in_fpdef814);
                    fplist();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_fpdef816); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:278:7: LPAREN fplist RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_fpdef824); if (state.failed) return ;
                    pushFollow(FOLLOW_fplist_in_fpdef826);
                    fplist();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RPAREN,FOLLOW_RPAREN_in_fpdef828); if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "fpdef"


    // $ANTLR start "fplist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:282:1: fplist : fpdef ( options {greedy=true; } : COMMA fpdef )* ( COMMA )? ;
    public final void fplist() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:283:5: ( fpdef ( options {greedy=true; } : COMMA fpdef )* ( COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:283:7: fpdef ( options {greedy=true; } : COMMA fpdef )* ( COMMA )?
            {
            pushFollow(FOLLOW_fpdef_in_fplist846);
            fpdef();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:284:7: ( options {greedy=true; } : COMMA fpdef )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==COMMA) ) {
                    int LA22_1 = input.LA(2);

                    if ( (LA22_1==NAME||LA22_1==LPAREN) ) {
                        alt22=1;
                    }


                }


                switch (alt22) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:284:31: COMMA fpdef
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_fplist862); if (state.failed) return ;
            	    pushFollow(FOLLOW_fpdef_in_fplist864);
            	    fpdef();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:284:45: ( COMMA )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==COMMA) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:284:46: COMMA
                    {
                    match(input,COMMA,FOLLOW_COMMA_in_fplist869); if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "fplist"


    // $ANTLR start "stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:288:1: stmt : ( simple_stmt | compound_stmt );
    public final void stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:289:5: ( simple_stmt | compound_stmt )
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==TRAILBACKSLASH||LA24_0==NAME||LA24_0==PRINT||(LA24_0>=ASSERT && LA24_0<=BREAK)||LA24_0==CONTINUE||LA24_0==DELETE||LA24_0==EXEC||LA24_0==FROM||LA24_0==GLOBAL||LA24_0==IMPORT||(LA24_0>=LAMBDA && LA24_0<=NOT)||(LA24_0>=PASS && LA24_0<=RETURN)||LA24_0==YIELD||LA24_0==LPAREN||(LA24_0>=PLUS && LA24_0<=MINUS)||(LA24_0>=TILDE && LA24_0<=LBRACK)||LA24_0==LCURLY||(LA24_0>=BACKQUOTE && LA24_0<=STRING)||(LA24_0>=TRISTRINGPART && LA24_0<=STRINGPART)) ) {
                alt24=1;
            }
            else if ( (LA24_0==CLASS||LA24_0==DEF||LA24_0==FOR||LA24_0==IF||(LA24_0>=TRY && LA24_0<=WITH)||LA24_0==AT) ) {
                alt24=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:289:7: simple_stmt
                    {
                    pushFollow(FOLLOW_simple_stmt_in_stmt889);
                    simple_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:290:7: compound_stmt
                    {
                    pushFollow(FOLLOW_compound_stmt_in_stmt897);
                    compound_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "stmt"


    // $ANTLR start "simple_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:294:1: simple_stmt : small_stmt ( options {greedy=true; } : SEMI small_stmt )* ( SEMI )? ( NEWLINE | EOF ) ;
    public final void simple_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:295:5: ( small_stmt ( options {greedy=true; } : SEMI small_stmt )* ( SEMI )? ( NEWLINE | EOF ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:295:7: small_stmt ( options {greedy=true; } : SEMI small_stmt )* ( SEMI )? ( NEWLINE | EOF )
            {
            pushFollow(FOLLOW_small_stmt_in_simple_stmt915);
            small_stmt();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:295:18: ( options {greedy=true; } : SEMI small_stmt )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==SEMI) ) {
                    int LA25_1 = input.LA(2);

                    if ( (LA25_1==TRAILBACKSLASH||LA25_1==NAME||LA25_1==PRINT||(LA25_1>=ASSERT && LA25_1<=BREAK)||LA25_1==CONTINUE||LA25_1==DELETE||LA25_1==EXEC||LA25_1==FROM||LA25_1==GLOBAL||LA25_1==IMPORT||(LA25_1>=LAMBDA && LA25_1<=NOT)||(LA25_1>=PASS && LA25_1<=RETURN)||LA25_1==YIELD||LA25_1==LPAREN||(LA25_1>=PLUS && LA25_1<=MINUS)||(LA25_1>=TILDE && LA25_1<=LBRACK)||LA25_1==LCURLY||(LA25_1>=BACKQUOTE && LA25_1<=STRING)||(LA25_1>=TRISTRINGPART && LA25_1<=STRINGPART)) ) {
                        alt25=1;
                    }


                }


                switch (alt25) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:295:42: SEMI small_stmt
            	    {
            	    match(input,SEMI,FOLLOW_SEMI_in_simple_stmt925); if (state.failed) return ;
            	    pushFollow(FOLLOW_small_stmt_in_simple_stmt927);
            	    small_stmt();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:295:60: ( SEMI )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==SEMI) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:295:61: SEMI
                    {
                    match(input,SEMI,FOLLOW_SEMI_in_simple_stmt932); if (state.failed) return ;

                    }
                    break;

            }

            if ( input.LA(1)==EOF||input.LA(1)==NEWLINE ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "simple_stmt"


    // $ANTLR start "small_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:300:1: small_stmt : ( expr_stmt | print_stmt | del_stmt | pass_stmt | flow_stmt | import_stmt | global_stmt | exec_stmt | assert_stmt );
    public final void small_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:300:12: ( expr_stmt | print_stmt | del_stmt | pass_stmt | flow_stmt | import_stmt | global_stmt | exec_stmt | assert_stmt )
            int alt27=9;
            alt27 = dfa27.predict(input);
            switch (alt27) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:300:14: expr_stmt
                    {
                    pushFollow(FOLLOW_expr_stmt_in_small_stmt955);
                    expr_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:301:14: print_stmt
                    {
                    pushFollow(FOLLOW_print_stmt_in_small_stmt970);
                    print_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:302:14: del_stmt
                    {
                    pushFollow(FOLLOW_del_stmt_in_small_stmt985);
                    del_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:303:14: pass_stmt
                    {
                    pushFollow(FOLLOW_pass_stmt_in_small_stmt1000);
                    pass_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:304:14: flow_stmt
                    {
                    pushFollow(FOLLOW_flow_stmt_in_small_stmt1015);
                    flow_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:305:14: import_stmt
                    {
                    pushFollow(FOLLOW_import_stmt_in_small_stmt1030);
                    import_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:306:14: global_stmt
                    {
                    pushFollow(FOLLOW_global_stmt_in_small_stmt1045);
                    global_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:307:14: exec_stmt
                    {
                    pushFollow(FOLLOW_exec_stmt_in_small_stmt1060);
                    exec_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 9 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:308:14: assert_stmt
                    {
                    pushFollow(FOLLOW_assert_stmt_in_small_stmt1075);
                    assert_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "small_stmt"


    // $ANTLR start "expr_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:313:1: expr_stmt : ( ( testlist augassign )=> testlist ( ( augassign yield_expr ) | ( augassign testlist ) ) | ( testlist ASSIGN )=> testlist ( | ( ( ASSIGN testlist )+ ) | ( ( ASSIGN yield_expr )+ ) ) | testlist ) ;
    public final void expr_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:314:5: ( ( ( testlist augassign )=> testlist ( ( augassign yield_expr ) | ( augassign testlist ) ) | ( testlist ASSIGN )=> testlist ( | ( ( ASSIGN testlist )+ ) | ( ( ASSIGN yield_expr )+ ) ) | testlist ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:314:7: ( ( testlist augassign )=> testlist ( ( augassign yield_expr ) | ( augassign testlist ) ) | ( testlist ASSIGN )=> testlist ( | ( ( ASSIGN testlist )+ ) | ( ( ASSIGN yield_expr )+ ) ) | testlist )
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:314:7: ( ( testlist augassign )=> testlist ( ( augassign yield_expr ) | ( augassign testlist ) ) | ( testlist ASSIGN )=> testlist ( | ( ( ASSIGN testlist )+ ) | ( ( ASSIGN yield_expr )+ ) ) | testlist )
            int alt32=3;
            alt32 = dfa32.predict(input);
            switch (alt32) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:314:8: ( testlist augassign )=> testlist ( ( augassign yield_expr ) | ( augassign testlist ) )
                    {
                    pushFollow(FOLLOW_testlist_in_expr_stmt1110);
                    testlist();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:315:9: ( ( augassign yield_expr ) | ( augassign testlist ) )
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( ((LA28_0>=PLUSEQUAL && LA28_0<=DOUBLESLASHEQUAL)) ) {
                        int LA28_1 = input.LA(2);

                        if ( (LA28_1==YIELD) ) {
                            alt28=1;
                        }
                        else if ( (LA28_1==TRAILBACKSLASH||LA28_1==NAME||(LA28_1>=LAMBDA && LA28_1<=NOT)||LA28_1==LPAREN||(LA28_1>=PLUS && LA28_1<=MINUS)||(LA28_1>=TILDE && LA28_1<=LBRACK)||LA28_1==LCURLY||(LA28_1>=BACKQUOTE && LA28_1<=STRING)||(LA28_1>=TRISTRINGPART && LA28_1<=STRINGPART)) ) {
                            alt28=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 28, 1, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 28, 0, input);

                        throw nvae;
                    }
                    switch (alt28) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:315:11: ( augassign yield_expr )
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:315:11: ( augassign yield_expr )
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:315:12: augassign yield_expr
                            {
                            pushFollow(FOLLOW_augassign_in_expr_stmt1123);
                            augassign();

                            state._fsp--;
                            if (state.failed) return ;
                            pushFollow(FOLLOW_yield_expr_in_expr_stmt1125);
                            yield_expr();

                            state._fsp--;
                            if (state.failed) return ;

                            }


                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:317:11: ( augassign testlist )
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:317:11: ( augassign testlist )
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:317:12: augassign testlist
                            {
                            pushFollow(FOLLOW_augassign_in_expr_stmt1150);
                            augassign();

                            state._fsp--;
                            if (state.failed) return ;
                            pushFollow(FOLLOW_testlist_in_expr_stmt1152);
                            testlist();

                            state._fsp--;
                            if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:320:7: ( testlist ASSIGN )=> testlist ( | ( ( ASSIGN testlist )+ ) | ( ( ASSIGN yield_expr )+ ) )
                    {
                    pushFollow(FOLLOW_testlist_in_expr_stmt1190);
                    testlist();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:321:9: ( | ( ( ASSIGN testlist )+ ) | ( ( ASSIGN yield_expr )+ ) )
                    int alt31=3;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==EOF||LA31_0==NEWLINE||LA31_0==SEMI) ) {
                        alt31=1;
                    }
                    else if ( (LA31_0==ASSIGN) ) {
                        int LA31_2 = input.LA(2);

                        if ( (LA31_2==YIELD) ) {
                            alt31=3;
                        }
                        else if ( (LA31_2==TRAILBACKSLASH||LA31_2==NAME||(LA31_2>=LAMBDA && LA31_2<=NOT)||LA31_2==LPAREN||(LA31_2>=PLUS && LA31_2<=MINUS)||(LA31_2>=TILDE && LA31_2<=LBRACK)||LA31_2==LCURLY||(LA31_2>=BACKQUOTE && LA31_2<=STRING)||(LA31_2>=TRISTRINGPART && LA31_2<=STRINGPART)) ) {
                            alt31=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 31, 2, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 31, 0, input);

                        throw nvae;
                    }
                    switch (alt31) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:322:9: 
                            {
                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:322:11: ( ( ASSIGN testlist )+ )
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:322:11: ( ( ASSIGN testlist )+ )
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:322:12: ( ASSIGN testlist )+
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:322:12: ( ASSIGN testlist )+
                            int cnt29=0;
                            loop29:
                            do {
                                int alt29=2;
                                int LA29_0 = input.LA(1);

                                if ( (LA29_0==ASSIGN) ) {
                                    alt29=1;
                                }


                                switch (alt29) {
                            	case 1 :
                            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:322:13: ASSIGN testlist
                            	    {
                            	    match(input,ASSIGN,FOLLOW_ASSIGN_in_expr_stmt1214); if (state.failed) return ;
                            	    pushFollow(FOLLOW_testlist_in_expr_stmt1216);
                            	    testlist();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt29 >= 1 ) break loop29;
                            	    if (state.backtracking>0) {state.failed=true; return ;}
                                        EarlyExitException eee =
                                            new EarlyExitException(29, input);
                                        throw eee;
                                }
                                cnt29++;
                            } while (true);


                            }


                            }
                            break;
                        case 3 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:324:11: ( ( ASSIGN yield_expr )+ )
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:324:11: ( ( ASSIGN yield_expr )+ )
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:324:12: ( ASSIGN yield_expr )+
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:324:12: ( ASSIGN yield_expr )+
                            int cnt30=0;
                            loop30:
                            do {
                                int alt30=2;
                                int LA30_0 = input.LA(1);

                                if ( (LA30_0==ASSIGN) ) {
                                    alt30=1;
                                }


                                switch (alt30) {
                            	case 1 :
                            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:324:13: ASSIGN yield_expr
                            	    {
                            	    match(input,ASSIGN,FOLLOW_ASSIGN_in_expr_stmt1244); if (state.failed) return ;
                            	    pushFollow(FOLLOW_yield_expr_in_expr_stmt1246);
                            	    yield_expr();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt30 >= 1 ) break loop30;
                            	    if (state.backtracking>0) {state.failed=true; return ;}
                                        EarlyExitException eee =
                                            new EarlyExitException(30, input);
                                        throw eee;
                                }
                                cnt30++;
                            } while (true);


                            }


                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:327:7: testlist
                    {
                    pushFollow(FOLLOW_testlist_in_expr_stmt1278);
                    testlist();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "expr_stmt"


    // $ANTLR start "augassign"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:333:1: augassign : ( PLUSEQUAL | MINUSEQUAL | STAREQUAL | SLASHEQUAL | PERCENTEQUAL | AMPEREQUAL | VBAREQUAL | CIRCUMFLEXEQUAL | LEFTSHIFTEQUAL | RIGHTSHIFTEQUAL | DOUBLESTAREQUAL | DOUBLESLASHEQUAL );
    public final void augassign() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:334:5: ( PLUSEQUAL | MINUSEQUAL | STAREQUAL | SLASHEQUAL | PERCENTEQUAL | AMPEREQUAL | VBAREQUAL | CIRCUMFLEXEQUAL | LEFTSHIFTEQUAL | RIGHTSHIFTEQUAL | DOUBLESTAREQUAL | DOUBLESLASHEQUAL )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
            {
            if ( (input.LA(1)>=PLUSEQUAL && input.LA(1)<=DOUBLESLASHEQUAL) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "augassign"


    // $ANTLR start "print_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:350:1: print_stmt : PRINT ( printlist | RIGHTSHIFT printlist | ) ;
    public final void print_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:351:5: ( PRINT ( printlist | RIGHTSHIFT printlist | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:351:7: PRINT ( printlist | RIGHTSHIFT printlist | )
            {
            match(input,PRINT,FOLLOW_PRINT_in_print_stmt1410); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:352:7: ( printlist | RIGHTSHIFT printlist | )
            int alt33=3;
            switch ( input.LA(1) ) {
            case TRAILBACKSLASH:
            case NAME:
            case LAMBDA:
            case NOT:
            case LPAREN:
            case PLUS:
            case MINUS:
            case TILDE:
            case LBRACK:
            case LCURLY:
            case BACKQUOTE:
            case INT:
            case LONGINT:
            case FLOAT:
            case COMPLEX:
            case STRING:
            case TRISTRINGPART:
            case STRINGPART:
                {
                alt33=1;
                }
                break;
            case RIGHTSHIFT:
                {
                alt33=2;
                }
                break;
            case EOF:
            case NEWLINE:
            case SEMI:
                {
                alt33=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }

            switch (alt33) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:352:8: printlist
                    {
                    pushFollow(FOLLOW_printlist_in_print_stmt1419);
                    printlist();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:353:9: RIGHTSHIFT printlist
                    {
                    match(input,RIGHTSHIFT,FOLLOW_RIGHTSHIFT_in_print_stmt1429); if (state.failed) return ;
                    pushFollow(FOLLOW_printlist_in_print_stmt1431);
                    printlist();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:355:7: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "print_stmt"


    // $ANTLR start "printlist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:359:1: printlist : ( ( test COMMA )=> test ( options {k=2; } : COMMA test )* ( COMMA )? | test );
    public final void printlist() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:360:5: ( ( test COMMA )=> test ( options {k=2; } : COMMA test )* ( COMMA )? | test )
            int alt36=2;
            alt36 = dfa36.predict(input);
            switch (alt36) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:360:7: ( test COMMA )=> test ( options {k=2; } : COMMA test )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_test_in_printlist1482);
                    test();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:361:13: ( options {k=2; } : COMMA test )*
                    loop34:
                    do {
                        int alt34=2;
                        alt34 = dfa34.predict(input);
                        switch (alt34) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:361:30: COMMA test
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_printlist1493); if (state.failed) return ;
                    	    pushFollow(FOLLOW_test_in_printlist1495);
                    	    test();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop34;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:362:10: ( COMMA )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==COMMA) ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:362:11: COMMA
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_printlist1509); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:363:7: test
                    {
                    pushFollow(FOLLOW_test_in_printlist1519);
                    test();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "printlist"


    // $ANTLR start "del_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:367:1: del_stmt : DELETE exprlist ;
    public final void del_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:368:5: ( DELETE exprlist )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:368:7: DELETE exprlist
            {
            match(input,DELETE,FOLLOW_DELETE_in_del_stmt1537); if (state.failed) return ;
            pushFollow(FOLLOW_exprlist_in_del_stmt1539);
            exprlist();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "del_stmt"


    // $ANTLR start "pass_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:372:1: pass_stmt : PASS ;
    public final void pass_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:373:5: ( PASS )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:373:7: PASS
            {
            match(input,PASS,FOLLOW_PASS_in_pass_stmt1557); if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "pass_stmt"


    // $ANTLR start "flow_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:377:1: flow_stmt : ( break_stmt | continue_stmt | return_stmt | raise_stmt | yield_stmt );
    public final void flow_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:378:5: ( break_stmt | continue_stmt | return_stmt | raise_stmt | yield_stmt )
            int alt37=5;
            switch ( input.LA(1) ) {
            case BREAK:
                {
                alt37=1;
                }
                break;
            case CONTINUE:
                {
                alt37=2;
                }
                break;
            case RETURN:
                {
                alt37=3;
                }
                break;
            case RAISE:
                {
                alt37=4;
                }
                break;
            case YIELD:
                {
                alt37=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }

            switch (alt37) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:378:7: break_stmt
                    {
                    pushFollow(FOLLOW_break_stmt_in_flow_stmt1575);
                    break_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:379:7: continue_stmt
                    {
                    pushFollow(FOLLOW_continue_stmt_in_flow_stmt1583);
                    continue_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:380:7: return_stmt
                    {
                    pushFollow(FOLLOW_return_stmt_in_flow_stmt1591);
                    return_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:381:7: raise_stmt
                    {
                    pushFollow(FOLLOW_raise_stmt_in_flow_stmt1599);
                    raise_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:382:7: yield_stmt
                    {
                    pushFollow(FOLLOW_yield_stmt_in_flow_stmt1607);
                    yield_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "flow_stmt"


    // $ANTLR start "break_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:386:1: break_stmt : BREAK ;
    public final void break_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:387:5: ( BREAK )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:387:7: BREAK
            {
            match(input,BREAK,FOLLOW_BREAK_in_break_stmt1625); if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "break_stmt"


    // $ANTLR start "continue_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:391:1: continue_stmt : CONTINUE ;
    public final void continue_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:392:5: ( CONTINUE )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:392:7: CONTINUE
            {
            match(input,CONTINUE,FOLLOW_CONTINUE_in_continue_stmt1643); if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "continue_stmt"


    // $ANTLR start "return_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:396:1: return_stmt : RETURN ( testlist | ) ;
    public final void return_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:397:5: ( RETURN ( testlist | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:397:7: RETURN ( testlist | )
            {
            match(input,RETURN,FOLLOW_RETURN_in_return_stmt1661); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:398:7: ( testlist | )
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==TRAILBACKSLASH||LA38_0==NAME||(LA38_0>=LAMBDA && LA38_0<=NOT)||LA38_0==LPAREN||(LA38_0>=PLUS && LA38_0<=MINUS)||(LA38_0>=TILDE && LA38_0<=LBRACK)||LA38_0==LCURLY||(LA38_0>=BACKQUOTE && LA38_0<=STRING)||(LA38_0>=TRISTRINGPART && LA38_0<=STRINGPART)) ) {
                alt38=1;
            }
            else if ( (LA38_0==EOF||LA38_0==NEWLINE||LA38_0==SEMI) ) {
                alt38=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }
            switch (alt38) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:398:8: testlist
                    {
                    pushFollow(FOLLOW_testlist_in_return_stmt1670);
                    testlist();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:400:7: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "return_stmt"


    // $ANTLR start "yield_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:404:1: yield_stmt : yield_expr ;
    public final void yield_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:405:5: ( yield_expr )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:405:7: yield_expr
            {
            pushFollow(FOLLOW_yield_expr_in_yield_stmt1704);
            yield_expr();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "yield_stmt"


    // $ANTLR start "raise_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:409:1: raise_stmt : RAISE ( test ( COMMA test ( COMMA test )? )? )? ;
    public final void raise_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:410:5: ( RAISE ( test ( COMMA test ( COMMA test )? )? )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:410:7: RAISE ( test ( COMMA test ( COMMA test )? )? )?
            {
            match(input,RAISE,FOLLOW_RAISE_in_raise_stmt1722); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:410:13: ( test ( COMMA test ( COMMA test )? )? )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==TRAILBACKSLASH||LA41_0==NAME||(LA41_0>=LAMBDA && LA41_0<=NOT)||LA41_0==LPAREN||(LA41_0>=PLUS && LA41_0<=MINUS)||(LA41_0>=TILDE && LA41_0<=LBRACK)||LA41_0==LCURLY||(LA41_0>=BACKQUOTE && LA41_0<=STRING)||(LA41_0>=TRISTRINGPART && LA41_0<=STRINGPART)) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:410:14: test ( COMMA test ( COMMA test )? )?
                    {
                    pushFollow(FOLLOW_test_in_raise_stmt1725);
                    test();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:410:19: ( COMMA test ( COMMA test )? )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==COMMA) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:410:20: COMMA test ( COMMA test )?
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_raise_stmt1728); if (state.failed) return ;
                            pushFollow(FOLLOW_test_in_raise_stmt1730);
                            test();

                            state._fsp--;
                            if (state.failed) return ;
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:411:9: ( COMMA test )?
                            int alt39=2;
                            int LA39_0 = input.LA(1);

                            if ( (LA39_0==COMMA) ) {
                                alt39=1;
                            }
                            switch (alt39) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:411:10: COMMA test
                                    {
                                    match(input,COMMA,FOLLOW_COMMA_in_raise_stmt1741); if (state.failed) return ;
                                    pushFollow(FOLLOW_test_in_raise_stmt1743);
                                    test();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "raise_stmt"


    // $ANTLR start "import_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:415:1: import_stmt : ( import_name | import_from );
    public final void import_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:416:5: ( import_name | import_from )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==IMPORT) ) {
                alt42=1;
            }
            else if ( (LA42_0==FROM) ) {
                alt42=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;
            }
            switch (alt42) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:416:7: import_name
                    {
                    pushFollow(FOLLOW_import_name_in_import_stmt1767);
                    import_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:417:7: import_from
                    {
                    pushFollow(FOLLOW_import_from_in_import_stmt1775);
                    import_from();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "import_stmt"


    // $ANTLR start "import_name"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:421:1: import_name : IMPORT dotted_as_names ;
    public final void import_name() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:422:5: ( IMPORT dotted_as_names )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:422:7: IMPORT dotted_as_names
            {
            match(input,IMPORT,FOLLOW_IMPORT_in_import_name1793); if (state.failed) return ;
            pushFollow(FOLLOW_dotted_as_names_in_import_name1795);
            dotted_as_names();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "import_name"


    // $ANTLR start "import_from"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:427:1: import_from : FROM ( ( DOT )* dotted_name | ( DOT )+ ) IMPORT ( STAR | import_as_names | LPAREN import_as_names ( COMMA )? RPAREN ) ;
    public final void import_from() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:5: ( FROM ( ( DOT )* dotted_name | ( DOT )+ ) IMPORT ( STAR | import_as_names | LPAREN import_as_names ( COMMA )? RPAREN ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:7: FROM ( ( DOT )* dotted_name | ( DOT )+ ) IMPORT ( STAR | import_as_names | LPAREN import_as_names ( COMMA )? RPAREN )
            {
            match(input,FROM,FOLLOW_FROM_in_import_from1814); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:12: ( ( DOT )* dotted_name | ( DOT )+ )
            int alt45=2;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:13: ( DOT )* dotted_name
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:13: ( DOT )*
                    loop43:
                    do {
                        int alt43=2;
                        int LA43_0 = input.LA(1);

                        if ( (LA43_0==DOT) ) {
                            alt43=1;
                        }


                        switch (alt43) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:13: DOT
                    	    {
                    	    match(input,DOT,FOLLOW_DOT_in_import_from1817); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop43;
                        }
                    } while (true);

                    pushFollow(FOLLOW_dotted_name_in_import_from1820);
                    dotted_name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:32: ( DOT )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:32: ( DOT )+
                    int cnt44=0;
                    loop44:
                    do {
                        int alt44=2;
                        int LA44_0 = input.LA(1);

                        if ( (LA44_0==DOT) ) {
                            alt44=1;
                        }


                        switch (alt44) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:428:32: DOT
                    	    {
                    	    match(input,DOT,FOLLOW_DOT_in_import_from1824); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt44 >= 1 ) break loop44;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(44, input);
                                throw eee;
                        }
                        cnt44++;
                    } while (true);


                    }
                    break;

            }

            match(input,IMPORT,FOLLOW_IMPORT_in_import_from1828); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:429:9: ( STAR | import_as_names | LPAREN import_as_names ( COMMA )? RPAREN )
            int alt47=3;
            switch ( input.LA(1) ) {
            case STAR:
                {
                alt47=1;
                }
                break;
            case NAME:
                {
                alt47=2;
                }
                break;
            case LPAREN:
                {
                alt47=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }

            switch (alt47) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:429:10: STAR
                    {
                    match(input,STAR,FOLLOW_STAR_in_import_from1839); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:430:11: import_as_names
                    {
                    pushFollow(FOLLOW_import_as_names_in_import_from1851);
                    import_as_names();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:431:11: LPAREN import_as_names ( COMMA )? RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_import_from1863); if (state.failed) return ;
                    pushFollow(FOLLOW_import_as_names_in_import_from1865);
                    import_as_names();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:431:34: ( COMMA )?
                    int alt46=2;
                    int LA46_0 = input.LA(1);

                    if ( (LA46_0==COMMA) ) {
                        alt46=1;
                    }
                    switch (alt46) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:431:34: COMMA
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_import_from1867); if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,RPAREN,FOLLOW_RPAREN_in_import_from1870); if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "import_from"


    // $ANTLR start "import_as_names"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:436:1: import_as_names : import_as_name ( COMMA import_as_name )* ;
    public final void import_as_names() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:437:5: ( import_as_name ( COMMA import_as_name )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:437:7: import_as_name ( COMMA import_as_name )*
            {
            pushFollow(FOLLOW_import_as_name_in_import_as_names1898);
            import_as_name();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:437:22: ( COMMA import_as_name )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==COMMA) ) {
                    int LA48_2 = input.LA(2);

                    if ( (LA48_2==NAME) ) {
                        alt48=1;
                    }


                }


                switch (alt48) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:437:23: COMMA import_as_name
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_import_as_names1901); if (state.failed) return ;
            	    pushFollow(FOLLOW_import_as_name_in_import_as_names1903);
            	    import_as_name();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "import_as_names"


    // $ANTLR start "import_as_name"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:441:1: import_as_name : NAME ( AS NAME )? ;
    public final void import_as_name() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:442:5: ( NAME ( AS NAME )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:442:7: NAME ( AS NAME )?
            {
            match(input,NAME,FOLLOW_NAME_in_import_as_name1923); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:442:12: ( AS NAME )?
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==AS) ) {
                alt49=1;
            }
            switch (alt49) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:442:13: AS NAME
                    {
                    match(input,AS,FOLLOW_AS_in_import_as_name1926); if (state.failed) return ;
                    match(input,NAME,FOLLOW_NAME_in_import_as_name1928); if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "import_as_name"


    // $ANTLR start "dotted_as_name"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:447:1: dotted_as_name : dotted_name ( AS NAME )? ;
    public final void dotted_as_name() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:448:5: ( dotted_name ( AS NAME )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:448:7: dotted_name ( AS NAME )?
            {
            pushFollow(FOLLOW_dotted_name_in_dotted_as_name1949);
            dotted_name();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:448:19: ( AS NAME )?
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==AS) ) {
                alt50=1;
            }
            switch (alt50) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:448:20: AS NAME
                    {
                    match(input,AS,FOLLOW_AS_in_dotted_as_name1952); if (state.failed) return ;
                    match(input,NAME,FOLLOW_NAME_in_dotted_as_name1954); if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "dotted_as_name"


    // $ANTLR start "dotted_as_names"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:452:1: dotted_as_names : dotted_as_name ( COMMA dotted_as_name )* ;
    public final void dotted_as_names() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:453:5: ( dotted_as_name ( COMMA dotted_as_name )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:453:7: dotted_as_name ( COMMA dotted_as_name )*
            {
            pushFollow(FOLLOW_dotted_as_name_in_dotted_as_names1974);
            dotted_as_name();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:453:22: ( COMMA dotted_as_name )*
            loop51:
            do {
                int alt51=2;
                int LA51_0 = input.LA(1);

                if ( (LA51_0==COMMA) ) {
                    alt51=1;
                }


                switch (alt51) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:453:23: COMMA dotted_as_name
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_dotted_as_names1977); if (state.failed) return ;
            	    pushFollow(FOLLOW_dotted_as_name_in_dotted_as_names1979);
            	    dotted_as_name();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "dotted_as_names"


    // $ANTLR start "dotted_name"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:457:1: dotted_name : NAME ( DOT attr )* ;
    public final void dotted_name() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:458:5: ( NAME ( DOT attr )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:458:7: NAME ( DOT attr )*
            {
            match(input,NAME,FOLLOW_NAME_in_dotted_name1999); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:458:12: ( DOT attr )*
            loop52:
            do {
                int alt52=2;
                int LA52_0 = input.LA(1);

                if ( (LA52_0==DOT) ) {
                    alt52=1;
                }


                switch (alt52) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:458:13: DOT attr
            	    {
            	    match(input,DOT,FOLLOW_DOT_in_dotted_name2002); if (state.failed) return ;
            	    pushFollow(FOLLOW_attr_in_dotted_name2004);
            	    attr();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "dotted_name"


    // $ANTLR start "global_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:462:1: global_stmt : GLOBAL NAME ( COMMA NAME )* ;
    public final void global_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:463:5: ( GLOBAL NAME ( COMMA NAME )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:463:7: GLOBAL NAME ( COMMA NAME )*
            {
            match(input,GLOBAL,FOLLOW_GLOBAL_in_global_stmt2024); if (state.failed) return ;
            match(input,NAME,FOLLOW_NAME_in_global_stmt2026); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:463:19: ( COMMA NAME )*
            loop53:
            do {
                int alt53=2;
                int LA53_0 = input.LA(1);

                if ( (LA53_0==COMMA) ) {
                    alt53=1;
                }


                switch (alt53) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:463:20: COMMA NAME
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_global_stmt2029); if (state.failed) return ;
            	    match(input,NAME,FOLLOW_NAME_in_global_stmt2031); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop53;
                }
            } while (true);


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "global_stmt"


    // $ANTLR start "exec_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:467:1: exec_stmt : EXEC expr ( IN test ( COMMA test )? )? ;
    public final void exec_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:468:5: ( EXEC expr ( IN test ( COMMA test )? )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:468:7: EXEC expr ( IN test ( COMMA test )? )?
            {
            match(input,EXEC,FOLLOW_EXEC_in_exec_stmt2051); if (state.failed) return ;
            pushFollow(FOLLOW_expr_in_exec_stmt2053);
            expr();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:468:17: ( IN test ( COMMA test )? )?
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==IN) ) {
                alt55=1;
            }
            switch (alt55) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:468:18: IN test ( COMMA test )?
                    {
                    match(input,IN,FOLLOW_IN_in_exec_stmt2056); if (state.failed) return ;
                    pushFollow(FOLLOW_test_in_exec_stmt2058);
                    test();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:468:26: ( COMMA test )?
                    int alt54=2;
                    int LA54_0 = input.LA(1);

                    if ( (LA54_0==COMMA) ) {
                        alt54=1;
                    }
                    switch (alt54) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:468:27: COMMA test
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_exec_stmt2061); if (state.failed) return ;
                            pushFollow(FOLLOW_test_in_exec_stmt2063);
                            test();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "exec_stmt"


    // $ANTLR start "assert_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:472:1: assert_stmt : ASSERT test ( COMMA test )? ;
    public final void assert_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:473:5: ( ASSERT test ( COMMA test )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:473:7: ASSERT test ( COMMA test )?
            {
            match(input,ASSERT,FOLLOW_ASSERT_in_assert_stmt2085); if (state.failed) return ;
            pushFollow(FOLLOW_test_in_assert_stmt2087);
            test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:473:19: ( COMMA test )?
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==COMMA) ) {
                alt56=1;
            }
            switch (alt56) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:473:20: COMMA test
                    {
                    match(input,COMMA,FOLLOW_COMMA_in_assert_stmt2090); if (state.failed) return ;
                    pushFollow(FOLLOW_test_in_assert_stmt2092);
                    test();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "assert_stmt"


    // $ANTLR start "compound_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:477:1: compound_stmt : ( if_stmt | while_stmt | for_stmt | try_stmt | with_stmt | ( ( decorators )? DEF )=> funcdef | ( ( decorators )? CLASS )=> classdef | decorators );
    public final void compound_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:478:5: ( if_stmt | while_stmt | for_stmt | try_stmt | with_stmt | ( ( decorators )? DEF )=> funcdef | ( ( decorators )? CLASS )=> classdef | decorators )
            int alt57=8;
            alt57 = dfa57.predict(input);
            switch (alt57) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:478:7: if_stmt
                    {
                    pushFollow(FOLLOW_if_stmt_in_compound_stmt2112);
                    if_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:479:7: while_stmt
                    {
                    pushFollow(FOLLOW_while_stmt_in_compound_stmt2120);
                    while_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:480:7: for_stmt
                    {
                    pushFollow(FOLLOW_for_stmt_in_compound_stmt2128);
                    for_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:481:7: try_stmt
                    {
                    pushFollow(FOLLOW_try_stmt_in_compound_stmt2136);
                    try_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:482:7: with_stmt
                    {
                    pushFollow(FOLLOW_with_stmt_in_compound_stmt2144);
                    with_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:483:7: ( ( decorators )? DEF )=> funcdef
                    {
                    pushFollow(FOLLOW_funcdef_in_compound_stmt2161);
                    funcdef();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:484:7: ( ( decorators )? CLASS )=> classdef
                    {
                    pushFollow(FOLLOW_classdef_in_compound_stmt2178);
                    classdef();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:485:7: decorators
                    {
                    pushFollow(FOLLOW_decorators_in_compound_stmt2186);
                    decorators();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "compound_stmt"


    // $ANTLR start "if_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:489:1: if_stmt : IF test COLON suite ( elif_clause )? ;
    public final void if_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:490:5: ( IF test COLON suite ( elif_clause )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:490:7: IF test COLON suite ( elif_clause )?
            {
            match(input,IF,FOLLOW_IF_in_if_stmt2204); if (state.failed) return ;
            pushFollow(FOLLOW_test_in_if_stmt2206);
            test();

            state._fsp--;
            if (state.failed) return ;
            match(input,COLON,FOLLOW_COLON_in_if_stmt2208); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_if_stmt2210);
            suite();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:490:27: ( elif_clause )?
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==ELIF||LA58_0==ORELSE) ) {
                alt58=1;
            }
            switch (alt58) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:490:27: elif_clause
                    {
                    pushFollow(FOLLOW_elif_clause_in_if_stmt2212);
                    elif_clause();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "if_stmt"


    // $ANTLR start "elif_clause"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:494:1: elif_clause : ( else_clause | ELIF test COLON suite ( elif_clause | ) );
    public final void elif_clause() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:495:5: ( else_clause | ELIF test COLON suite ( elif_clause | ) )
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==ORELSE) ) {
                alt60=1;
            }
            else if ( (LA60_0==ELIF) ) {
                alt60=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 60, 0, input);

                throw nvae;
            }
            switch (alt60) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:495:7: else_clause
                    {
                    pushFollow(FOLLOW_else_clause_in_elif_clause2231);
                    else_clause();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:496:7: ELIF test COLON suite ( elif_clause | )
                    {
                    match(input,ELIF,FOLLOW_ELIF_in_elif_clause2239); if (state.failed) return ;
                    pushFollow(FOLLOW_test_in_elif_clause2241);
                    test();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,COLON,FOLLOW_COLON_in_elif_clause2243); if (state.failed) return ;
                    pushFollow(FOLLOW_suite_in_elif_clause2245);
                    suite();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:497:9: ( elif_clause | )
                    int alt59=2;
                    int LA59_0 = input.LA(1);

                    if ( (LA59_0==ELIF||LA59_0==ORELSE) ) {
                        alt59=1;
                    }
                    else if ( (LA59_0==EOF||(LA59_0>=DEDENT && LA59_0<=NEWLINE)||LA59_0==NAME||LA59_0==PRINT||(LA59_0>=ASSERT && LA59_0<=DELETE)||LA59_0==EXEC||(LA59_0>=FROM && LA59_0<=IMPORT)||(LA59_0>=LAMBDA && LA59_0<=NOT)||(LA59_0>=PASS && LA59_0<=LPAREN)||(LA59_0>=PLUS && LA59_0<=MINUS)||(LA59_0>=TILDE && LA59_0<=LBRACK)||LA59_0==LCURLY||(LA59_0>=BACKQUOTE && LA59_0<=STRING)||(LA59_0>=TRISTRINGPART && LA59_0<=STRINGPART)) ) {
                        alt59=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 59, 0, input);

                        throw nvae;
                    }
                    switch (alt59) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:497:10: elif_clause
                            {
                            pushFollow(FOLLOW_elif_clause_in_elif_clause2256);
                            elif_clause();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:499:9: 
                            {
                            }
                            break;

                    }


                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "elif_clause"


    // $ANTLR start "else_clause"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:503:1: else_clause : ORELSE COLON suite ;
    public final void else_clause() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:504:5: ( ORELSE COLON suite )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:504:7: ORELSE COLON suite
            {
            match(input,ORELSE,FOLLOW_ORELSE_in_else_clause2294); if (state.failed) return ;
            match(input,COLON,FOLLOW_COLON_in_else_clause2296); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_else_clause2298);
            suite();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "else_clause"


    // $ANTLR start "while_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:508:1: while_stmt : WHILE test COLON suite ( ORELSE COLON suite )? ;
    public final void while_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:509:5: ( WHILE test COLON suite ( ORELSE COLON suite )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:509:7: WHILE test COLON suite ( ORELSE COLON suite )?
            {
            match(input,WHILE,FOLLOW_WHILE_in_while_stmt2316); if (state.failed) return ;
            pushFollow(FOLLOW_test_in_while_stmt2318);
            test();

            state._fsp--;
            if (state.failed) return ;
            match(input,COLON,FOLLOW_COLON_in_while_stmt2320); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_while_stmt2322);
            suite();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:509:30: ( ORELSE COLON suite )?
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( (LA61_0==ORELSE) ) {
                alt61=1;
            }
            switch (alt61) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:509:31: ORELSE COLON suite
                    {
                    match(input,ORELSE,FOLLOW_ORELSE_in_while_stmt2325); if (state.failed) return ;
                    match(input,COLON,FOLLOW_COLON_in_while_stmt2327); if (state.failed) return ;
                    pushFollow(FOLLOW_suite_in_while_stmt2329);
                    suite();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "while_stmt"


    // $ANTLR start "for_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:513:1: for_stmt : FOR exprlist IN testlist COLON suite ( ORELSE COLON suite )? ;
    public final void for_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:514:5: ( FOR exprlist IN testlist COLON suite ( ORELSE COLON suite )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:514:7: FOR exprlist IN testlist COLON suite ( ORELSE COLON suite )?
            {
            match(input,FOR,FOLLOW_FOR_in_for_stmt2349); if (state.failed) return ;
            pushFollow(FOLLOW_exprlist_in_for_stmt2351);
            exprlist();

            state._fsp--;
            if (state.failed) return ;
            match(input,IN,FOLLOW_IN_in_for_stmt2353); if (state.failed) return ;
            pushFollow(FOLLOW_testlist_in_for_stmt2355);
            testlist();

            state._fsp--;
            if (state.failed) return ;
            match(input,COLON,FOLLOW_COLON_in_for_stmt2357); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_for_stmt2359);
            suite();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:515:9: ( ORELSE COLON suite )?
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==ORELSE) ) {
                alt62=1;
            }
            switch (alt62) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:515:10: ORELSE COLON suite
                    {
                    match(input,ORELSE,FOLLOW_ORELSE_in_for_stmt2370); if (state.failed) return ;
                    match(input,COLON,FOLLOW_COLON_in_for_stmt2372); if (state.failed) return ;
                    pushFollow(FOLLOW_suite_in_for_stmt2374);
                    suite();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "for_stmt"


    // $ANTLR start "try_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:523:1: try_stmt : TRY COLON suite ( ( except_clause )+ ( ORELSE COLON suite )? ( FINALLY COLON suite )? | FINALLY COLON suite )? ;
    public final void try_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:524:5: ( TRY COLON suite ( ( except_clause )+ ( ORELSE COLON suite )? ( FINALLY COLON suite )? | FINALLY COLON suite )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:524:7: TRY COLON suite ( ( except_clause )+ ( ORELSE COLON suite )? ( FINALLY COLON suite )? | FINALLY COLON suite )?
            {
            match(input,TRY,FOLLOW_TRY_in_try_stmt2398); if (state.failed) return ;
            match(input,COLON,FOLLOW_COLON_in_try_stmt2400); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_try_stmt2402);
            suite();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:525:7: ( ( except_clause )+ ( ORELSE COLON suite )? ( FINALLY COLON suite )? | FINALLY COLON suite )?
            int alt66=3;
            int LA66_0 = input.LA(1);

            if ( (LA66_0==EXCEPT) ) {
                alt66=1;
            }
            else if ( (LA66_0==FINALLY) ) {
                alt66=2;
            }
            switch (alt66) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:525:9: ( except_clause )+ ( ORELSE COLON suite )? ( FINALLY COLON suite )?
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:525:9: ( except_clause )+
                    int cnt63=0;
                    loop63:
                    do {
                        int alt63=2;
                        int LA63_0 = input.LA(1);

                        if ( (LA63_0==EXCEPT) ) {
                            alt63=1;
                        }


                        switch (alt63) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:525:9: except_clause
                    	    {
                    	    pushFollow(FOLLOW_except_clause_in_try_stmt2412);
                    	    except_clause();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt63 >= 1 ) break loop63;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(63, input);
                                throw eee;
                        }
                        cnt63++;
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:525:24: ( ORELSE COLON suite )?
                    int alt64=2;
                    int LA64_0 = input.LA(1);

                    if ( (LA64_0==ORELSE) ) {
                        alt64=1;
                    }
                    switch (alt64) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:525:25: ORELSE COLON suite
                            {
                            match(input,ORELSE,FOLLOW_ORELSE_in_try_stmt2416); if (state.failed) return ;
                            match(input,COLON,FOLLOW_COLON_in_try_stmt2418); if (state.failed) return ;
                            pushFollow(FOLLOW_suite_in_try_stmt2420);
                            suite();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:525:46: ( FINALLY COLON suite )?
                    int alt65=2;
                    int LA65_0 = input.LA(1);

                    if ( (LA65_0==FINALLY) ) {
                        alt65=1;
                    }
                    switch (alt65) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:525:47: FINALLY COLON suite
                            {
                            match(input,FINALLY,FOLLOW_FINALLY_in_try_stmt2425); if (state.failed) return ;
                            match(input,COLON,FOLLOW_COLON_in_try_stmt2427); if (state.failed) return ;
                            pushFollow(FOLLOW_suite_in_try_stmt2429);
                            suite();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:526:9: FINALLY COLON suite
                    {
                    match(input,FINALLY,FOLLOW_FINALLY_in_try_stmt2441); if (state.failed) return ;
                    match(input,COLON,FOLLOW_COLON_in_try_stmt2443); if (state.failed) return ;
                    pushFollow(FOLLOW_suite_in_try_stmt2445);
                    suite();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "try_stmt"


    // $ANTLR start "with_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:531:1: with_stmt : WITH test ( with_var )? COLON suite ;
    public final void with_stmt() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:532:5: ( WITH test ( with_var )? COLON suite )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:532:7: WITH test ( with_var )? COLON suite
            {
            match(input,WITH,FOLLOW_WITH_in_with_stmt2474); if (state.failed) return ;
            pushFollow(FOLLOW_test_in_with_stmt2476);
            test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:532:17: ( with_var )?
            int alt67=2;
            int LA67_0 = input.LA(1);

            if ( (LA67_0==NAME||LA67_0==AS) ) {
                alt67=1;
            }
            switch (alt67) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:532:18: with_var
                    {
                    pushFollow(FOLLOW_with_var_in_with_stmt2479);
                    with_var();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,COLON,FOLLOW_COLON_in_with_stmt2483); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_with_stmt2485);
            suite();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "with_stmt"


    // $ANTLR start "with_var"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:536:1: with_var : ( AS | NAME ) expr ;
    public final void with_var() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:537:5: ( ( AS | NAME ) expr )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:537:7: ( AS | NAME ) expr
            {
            if ( input.LA(1)==NAME||input.LA(1)==AS ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            pushFollow(FOLLOW_expr_in_with_var2511);
            expr();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "with_var"


    // $ANTLR start "except_clause"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:541:1: except_clause : EXCEPT ( test ( COMMA test )? )? COLON suite ;
    public final void except_clause() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:542:5: ( EXCEPT ( test ( COMMA test )? )? COLON suite )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:542:7: EXCEPT ( test ( COMMA test )? )? COLON suite
            {
            match(input,EXCEPT,FOLLOW_EXCEPT_in_except_clause2529); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:542:14: ( test ( COMMA test )? )?
            int alt69=2;
            int LA69_0 = input.LA(1);

            if ( (LA69_0==TRAILBACKSLASH||LA69_0==NAME||(LA69_0>=LAMBDA && LA69_0<=NOT)||LA69_0==LPAREN||(LA69_0>=PLUS && LA69_0<=MINUS)||(LA69_0>=TILDE && LA69_0<=LBRACK)||LA69_0==LCURLY||(LA69_0>=BACKQUOTE && LA69_0<=STRING)||(LA69_0>=TRISTRINGPART && LA69_0<=STRINGPART)) ) {
                alt69=1;
            }
            switch (alt69) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:542:15: test ( COMMA test )?
                    {
                    pushFollow(FOLLOW_test_in_except_clause2532);
                    test();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:542:20: ( COMMA test )?
                    int alt68=2;
                    int LA68_0 = input.LA(1);

                    if ( (LA68_0==COMMA) ) {
                        alt68=1;
                    }
                    switch (alt68) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:542:21: COMMA test
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_except_clause2535); if (state.failed) return ;
                            pushFollow(FOLLOW_test_in_except_clause2537);
                            test();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }

            match(input,COLON,FOLLOW_COLON_in_except_clause2543); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_except_clause2545);
            suite();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "except_clause"


    // $ANTLR start "suite"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:546:1: suite : ( simple_stmt | NEWLINE ( EOF | ( DEDENT )+ EOF | INDENT ( stmt )+ ( DEDENT | EOF ) ) );
    public final void suite() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:547:5: ( simple_stmt | NEWLINE ( EOF | ( DEDENT )+ EOF | INDENT ( stmt )+ ( DEDENT | EOF ) ) )
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( (LA73_0==TRAILBACKSLASH||LA73_0==NAME||LA73_0==PRINT||(LA73_0>=ASSERT && LA73_0<=BREAK)||LA73_0==CONTINUE||LA73_0==DELETE||LA73_0==EXEC||LA73_0==FROM||LA73_0==GLOBAL||LA73_0==IMPORT||(LA73_0>=LAMBDA && LA73_0<=NOT)||(LA73_0>=PASS && LA73_0<=RETURN)||LA73_0==YIELD||LA73_0==LPAREN||(LA73_0>=PLUS && LA73_0<=MINUS)||(LA73_0>=TILDE && LA73_0<=LBRACK)||LA73_0==LCURLY||(LA73_0>=BACKQUOTE && LA73_0<=STRING)||(LA73_0>=TRISTRINGPART && LA73_0<=STRINGPART)) ) {
                alt73=1;
            }
            else if ( (LA73_0==NEWLINE) ) {
                alt73=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                throw nvae;
            }
            switch (alt73) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:547:7: simple_stmt
                    {
                    pushFollow(FOLLOW_simple_stmt_in_suite2563);
                    simple_stmt();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:548:7: NEWLINE ( EOF | ( DEDENT )+ EOF | INDENT ( stmt )+ ( DEDENT | EOF ) )
                    {
                    match(input,NEWLINE,FOLLOW_NEWLINE_in_suite2571); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:548:15: ( EOF | ( DEDENT )+ EOF | INDENT ( stmt )+ ( DEDENT | EOF ) )
                    int alt72=3;
                    switch ( input.LA(1) ) {
                    case EOF:
                        {
                        alt72=1;
                        }
                        break;
                    case DEDENT:
                        {
                        alt72=2;
                        }
                        break;
                    case INDENT:
                        {
                        alt72=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 72, 0, input);

                        throw nvae;
                    }

                    switch (alt72) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:548:16: EOF
                            {
                            match(input,EOF,FOLLOW_EOF_in_suite2574); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:549:17: ( DEDENT )+ EOF
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:549:17: ( DEDENT )+
                            int cnt70=0;
                            loop70:
                            do {
                                int alt70=2;
                                int LA70_0 = input.LA(1);

                                if ( (LA70_0==DEDENT) ) {
                                    alt70=1;
                                }


                                switch (alt70) {
                            	case 1 :
                            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:549:18: DEDENT
                            	    {
                            	    match(input,DEDENT,FOLLOW_DEDENT_in_suite2593); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt70 >= 1 ) break loop70;
                            	    if (state.backtracking>0) {state.failed=true; return ;}
                                        EarlyExitException eee =
                                            new EarlyExitException(70, input);
                                        throw eee;
                                }
                                cnt70++;
                            } while (true);

                            match(input,EOF,FOLLOW_EOF_in_suite2597); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:550:17: INDENT ( stmt )+ ( DEDENT | EOF )
                            {
                            match(input,INDENT,FOLLOW_INDENT_in_suite2615); if (state.failed) return ;
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:550:24: ( stmt )+
                            int cnt71=0;
                            loop71:
                            do {
                                int alt71=2;
                                int LA71_0 = input.LA(1);

                                if ( (LA71_0==TRAILBACKSLASH||LA71_0==NAME||LA71_0==PRINT||(LA71_0>=ASSERT && LA71_0<=DELETE)||LA71_0==EXEC||(LA71_0>=FROM && LA71_0<=IMPORT)||(LA71_0>=LAMBDA && LA71_0<=NOT)||(LA71_0>=PASS && LA71_0<=LPAREN)||(LA71_0>=PLUS && LA71_0<=MINUS)||(LA71_0>=TILDE && LA71_0<=LBRACK)||LA71_0==LCURLY||(LA71_0>=BACKQUOTE && LA71_0<=STRING)||(LA71_0>=TRISTRINGPART && LA71_0<=STRINGPART)) ) {
                                    alt71=1;
                                }


                                switch (alt71) {
                            	case 1 :
                            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:550:25: stmt
                            	    {
                            	    pushFollow(FOLLOW_stmt_in_suite2618);
                            	    stmt();

                            	    state._fsp--;
                            	    if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt71 >= 1 ) break loop71;
                            	    if (state.backtracking>0) {state.failed=true; return ;}
                                        EarlyExitException eee =
                                            new EarlyExitException(71, input);
                                        throw eee;
                                }
                                cnt71++;
                            } while (true);

                            if ( input.LA(1)==EOF||input.LA(1)==DEDENT ) {
                                input.consume();
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "suite"


    // $ANTLR start "test"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:557:1: test : ( or_test ( ( IF or_test ORELSE )=> IF or_test ORELSE test | ) | lambdef );
    public final void test() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:558:5: ( or_test ( ( IF or_test ORELSE )=> IF or_test ORELSE test | ) | lambdef )
            int alt75=2;
            int LA75_0 = input.LA(1);

            if ( (LA75_0==TRAILBACKSLASH||LA75_0==NAME||LA75_0==NOT||LA75_0==LPAREN||(LA75_0>=PLUS && LA75_0<=MINUS)||(LA75_0>=TILDE && LA75_0<=LBRACK)||LA75_0==LCURLY||(LA75_0>=BACKQUOTE && LA75_0<=STRING)||(LA75_0>=TRISTRINGPART && LA75_0<=STRINGPART)) ) {
                alt75=1;
            }
            else if ( (LA75_0==LAMBDA) ) {
                alt75=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 75, 0, input);

                throw nvae;
            }
            switch (alt75) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:558:6: or_test ( ( IF or_test ORELSE )=> IF or_test ORELSE test | )
                    {
                    pushFollow(FOLLOW_or_test_in_test2723);
                    or_test();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:559:7: ( ( IF or_test ORELSE )=> IF or_test ORELSE test | )
                    int alt74=2;
                    alt74 = dfa74.predict(input);
                    switch (alt74) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:559:9: ( IF or_test ORELSE )=> IF or_test ORELSE test
                            {
                            match(input,IF,FOLLOW_IF_in_test2743); if (state.failed) return ;
                            pushFollow(FOLLOW_or_test_in_test2745);
                            or_test();

                            state._fsp--;
                            if (state.failed) return ;
                            match(input,ORELSE,FOLLOW_ORELSE_in_test2747); if (state.failed) return ;
                            pushFollow(FOLLOW_test_in_test2749);
                            test();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:561:7: 
                            {
                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:562:7: lambdef
                    {
                    pushFollow(FOLLOW_lambdef_in_test2773);
                    lambdef();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "test"


    // $ANTLR start "or_test"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:566:1: or_test : and_test ( ( OR and_test )+ | ) ;
    public final void or_test() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:567:5: ( and_test ( ( OR and_test )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:567:7: and_test ( ( OR and_test )+ | )
            {
            pushFollow(FOLLOW_and_test_in_or_test2791);
            and_test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:568:9: ( ( OR and_test )+ | )
            int alt77=2;
            int LA77_0 = input.LA(1);

            if ( (LA77_0==OR) ) {
                alt77=1;
            }
            else if ( (LA77_0==EOF||LA77_0==NEWLINE||LA77_0==NAME||LA77_0==AS||LA77_0==FOR||LA77_0==IF||LA77_0==ORELSE||(LA77_0>=RPAREN && LA77_0<=COMMA)||(LA77_0>=SEMI && LA77_0<=DOUBLESLASHEQUAL)||LA77_0==RBRACK||(LA77_0>=RCURLY && LA77_0<=BACKQUOTE)) ) {
                alt77=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 77, 0, input);

                throw nvae;
            }
            switch (alt77) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:568:11: ( OR and_test )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:568:11: ( OR and_test )+
                    int cnt76=0;
                    loop76:
                    do {
                        int alt76=2;
                        int LA76_0 = input.LA(1);

                        if ( (LA76_0==OR) ) {
                            alt76=1;
                        }


                        switch (alt76) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:568:12: OR and_test
                    	    {
                    	    match(input,OR,FOLLOW_OR_in_or_test2804); if (state.failed) return ;
                    	    pushFollow(FOLLOW_and_test_in_or_test2806);
                    	    and_test();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt76 >= 1 ) break loop76;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(76, input);
                                throw eee;
                        }
                        cnt76++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:571:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "or_test"


    // $ANTLR start "and_test"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:575:1: and_test : not_test ( ( AND not_test )+ | ) ;
    public final void and_test() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:576:5: ( not_test ( ( AND not_test )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:576:7: not_test ( ( AND not_test )+ | )
            {
            pushFollow(FOLLOW_not_test_in_and_test2857);
            not_test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:577:9: ( ( AND not_test )+ | )
            int alt79=2;
            int LA79_0 = input.LA(1);

            if ( (LA79_0==AND) ) {
                alt79=1;
            }
            else if ( (LA79_0==EOF||LA79_0==NEWLINE||LA79_0==NAME||LA79_0==AS||LA79_0==FOR||LA79_0==IF||(LA79_0>=OR && LA79_0<=ORELSE)||(LA79_0>=RPAREN && LA79_0<=COMMA)||(LA79_0>=SEMI && LA79_0<=DOUBLESLASHEQUAL)||LA79_0==RBRACK||(LA79_0>=RCURLY && LA79_0<=BACKQUOTE)) ) {
                alt79=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 79, 0, input);

                throw nvae;
            }
            switch (alt79) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:577:11: ( AND not_test )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:577:11: ( AND not_test )+
                    int cnt78=0;
                    loop78:
                    do {
                        int alt78=2;
                        int LA78_0 = input.LA(1);

                        if ( (LA78_0==AND) ) {
                            alt78=1;
                        }


                        switch (alt78) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:577:12: AND not_test
                    	    {
                    	    match(input,AND,FOLLOW_AND_in_and_test2870); if (state.failed) return ;
                    	    pushFollow(FOLLOW_not_test_in_and_test2872);
                    	    not_test();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt78 >= 1 ) break loop78;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(78, input);
                                throw eee;
                        }
                        cnt78++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:580:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "and_test"


    // $ANTLR start "not_test"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:584:1: not_test : ( NOT not_test | comparison );
    public final void not_test() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:585:5: ( NOT not_test | comparison )
            int alt80=2;
            int LA80_0 = input.LA(1);

            if ( (LA80_0==NOT) ) {
                alt80=1;
            }
            else if ( (LA80_0==TRAILBACKSLASH||LA80_0==NAME||LA80_0==LPAREN||(LA80_0>=PLUS && LA80_0<=MINUS)||(LA80_0>=TILDE && LA80_0<=LBRACK)||LA80_0==LCURLY||(LA80_0>=BACKQUOTE && LA80_0<=STRING)||(LA80_0>=TRISTRINGPART && LA80_0<=STRINGPART)) ) {
                alt80=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 80, 0, input);

                throw nvae;
            }
            switch (alt80) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:585:7: NOT not_test
                    {
                    match(input,NOT,FOLLOW_NOT_in_not_test2923); if (state.failed) return ;
                    pushFollow(FOLLOW_not_test_in_not_test2925);
                    not_test();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:586:7: comparison
                    {
                    pushFollow(FOLLOW_comparison_in_not_test2933);
                    comparison();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "not_test"


    // $ANTLR start "comparison"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:590:1: comparison : expr ( ( comp_op expr )+ | ) ;
    public final void comparison() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:591:5: ( expr ( ( comp_op expr )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:591:7: expr ( ( comp_op expr )+ | )
            {
            pushFollow(FOLLOW_expr_in_comparison2951);
            expr();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:592:8: ( ( comp_op expr )+ | )
            int alt82=2;
            int LA82_0 = input.LA(1);

            if ( ((LA82_0>=IN && LA82_0<=IS)||LA82_0==NOT||(LA82_0>=LESS && LA82_0<=NOTEQUAL)) ) {
                alt82=1;
            }
            else if ( (LA82_0==EOF||LA82_0==NEWLINE||LA82_0==NAME||(LA82_0>=AND && LA82_0<=AS)||LA82_0==FOR||LA82_0==IF||(LA82_0>=OR && LA82_0<=ORELSE)||(LA82_0>=RPAREN && LA82_0<=COMMA)||(LA82_0>=SEMI && LA82_0<=DOUBLESLASHEQUAL)||LA82_0==RBRACK||(LA82_0>=RCURLY && LA82_0<=BACKQUOTE)) ) {
                alt82=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                throw nvae;
            }
            switch (alt82) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:592:10: ( comp_op expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:592:10: ( comp_op expr )+
                    int cnt81=0;
                    loop81:
                    do {
                        int alt81=2;
                        int LA81_0 = input.LA(1);

                        if ( ((LA81_0>=IN && LA81_0<=IS)||LA81_0==NOT||(LA81_0>=LESS && LA81_0<=NOTEQUAL)) ) {
                            alt81=1;
                        }


                        switch (alt81) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:592:12: comp_op expr
                    	    {
                    	    pushFollow(FOLLOW_comp_op_in_comparison2964);
                    	    comp_op();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    pushFollow(FOLLOW_expr_in_comparison2966);
                    	    expr();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt81 >= 1 ) break loop81;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(81, input);
                                throw eee;
                        }
                        cnt81++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:595:8: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "comparison"


    // $ANTLR start "comp_op"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:599:1: comp_op : ( LESS | GREATER | EQUAL | GREATEREQUAL | LESSEQUAL | ALT_NOTEQUAL | NOTEQUAL | IN | NOT IN | IS | IS NOT );
    public final void comp_op() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:600:5: ( LESS | GREATER | EQUAL | GREATEREQUAL | LESSEQUAL | ALT_NOTEQUAL | NOTEQUAL | IN | NOT IN | IS | IS NOT )
            int alt83=11;
            alt83 = dfa83.predict(input);
            switch (alt83) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:600:7: LESS
                    {
                    match(input,LESS,FOLLOW_LESS_in_comp_op3014); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:601:7: GREATER
                    {
                    match(input,GREATER,FOLLOW_GREATER_in_comp_op3022); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:602:7: EQUAL
                    {
                    match(input,EQUAL,FOLLOW_EQUAL_in_comp_op3030); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:603:7: GREATEREQUAL
                    {
                    match(input,GREATEREQUAL,FOLLOW_GREATEREQUAL_in_comp_op3038); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:604:7: LESSEQUAL
                    {
                    match(input,LESSEQUAL,FOLLOW_LESSEQUAL_in_comp_op3046); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:605:7: ALT_NOTEQUAL
                    {
                    match(input,ALT_NOTEQUAL,FOLLOW_ALT_NOTEQUAL_in_comp_op3054); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:606:7: NOTEQUAL
                    {
                    match(input,NOTEQUAL,FOLLOW_NOTEQUAL_in_comp_op3062); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:607:7: IN
                    {
                    match(input,IN,FOLLOW_IN_in_comp_op3070); if (state.failed) return ;

                    }
                    break;
                case 9 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:608:7: NOT IN
                    {
                    match(input,NOT,FOLLOW_NOT_in_comp_op3078); if (state.failed) return ;
                    match(input,IN,FOLLOW_IN_in_comp_op3080); if (state.failed) return ;

                    }
                    break;
                case 10 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:609:7: IS
                    {
                    match(input,IS,FOLLOW_IS_in_comp_op3088); if (state.failed) return ;

                    }
                    break;
                case 11 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:610:7: IS NOT
                    {
                    match(input,IS,FOLLOW_IS_in_comp_op3096); if (state.failed) return ;
                    match(input,NOT,FOLLOW_NOT_in_comp_op3098); if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "comp_op"


    // $ANTLR start "expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:614:1: expr : xor_expr ( ( VBAR xor_expr )+ | ) ;
    public final void expr() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:615:5: ( xor_expr ( ( VBAR xor_expr )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:615:7: xor_expr ( ( VBAR xor_expr )+ | )
            {
            pushFollow(FOLLOW_xor_expr_in_expr3116);
            xor_expr();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:616:9: ( ( VBAR xor_expr )+ | )
            int alt85=2;
            int LA85_0 = input.LA(1);

            if ( (LA85_0==VBAR) ) {
                alt85=1;
            }
            else if ( (LA85_0==EOF||LA85_0==NEWLINE||LA85_0==NAME||(LA85_0>=AND && LA85_0<=AS)||LA85_0==FOR||LA85_0==IF||(LA85_0>=IN && LA85_0<=IS)||(LA85_0>=NOT && LA85_0<=ORELSE)||(LA85_0>=RPAREN && LA85_0<=COMMA)||(LA85_0>=SEMI && LA85_0<=DOUBLESLASHEQUAL)||(LA85_0>=LESS && LA85_0<=NOTEQUAL)||LA85_0==RBRACK||(LA85_0>=RCURLY && LA85_0<=BACKQUOTE)) ) {
                alt85=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 85, 0, input);

                throw nvae;
            }
            switch (alt85) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:616:11: ( VBAR xor_expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:616:11: ( VBAR xor_expr )+
                    int cnt84=0;
                    loop84:
                    do {
                        int alt84=2;
                        int LA84_0 = input.LA(1);

                        if ( (LA84_0==VBAR) ) {
                            alt84=1;
                        }


                        switch (alt84) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:616:12: VBAR xor_expr
                    	    {
                    	    match(input,VBAR,FOLLOW_VBAR_in_expr3129); if (state.failed) return ;
                    	    pushFollow(FOLLOW_xor_expr_in_expr3131);
                    	    xor_expr();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt84 >= 1 ) break loop84;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(84, input);
                                throw eee;
                        }
                        cnt84++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:619:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "expr"


    // $ANTLR start "xor_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:623:1: xor_expr : and_expr ( ( CIRCUMFLEX and_expr )+ | ) ;
    public final void xor_expr() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:624:5: ( and_expr ( ( CIRCUMFLEX and_expr )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:624:7: and_expr ( ( CIRCUMFLEX and_expr )+ | )
            {
            pushFollow(FOLLOW_and_expr_in_xor_expr3182);
            and_expr();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:625:9: ( ( CIRCUMFLEX and_expr )+ | )
            int alt87=2;
            int LA87_0 = input.LA(1);

            if ( (LA87_0==CIRCUMFLEX) ) {
                alt87=1;
            }
            else if ( (LA87_0==EOF||LA87_0==NEWLINE||LA87_0==NAME||(LA87_0>=AND && LA87_0<=AS)||LA87_0==FOR||LA87_0==IF||(LA87_0>=IN && LA87_0<=IS)||(LA87_0>=NOT && LA87_0<=ORELSE)||(LA87_0>=RPAREN && LA87_0<=COMMA)||(LA87_0>=SEMI && LA87_0<=DOUBLESLASHEQUAL)||(LA87_0>=LESS && LA87_0<=VBAR)||LA87_0==RBRACK||(LA87_0>=RCURLY && LA87_0<=BACKQUOTE)) ) {
                alt87=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                throw nvae;
            }
            switch (alt87) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:625:11: ( CIRCUMFLEX and_expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:625:11: ( CIRCUMFLEX and_expr )+
                    int cnt86=0;
                    loop86:
                    do {
                        int alt86=2;
                        int LA86_0 = input.LA(1);

                        if ( (LA86_0==CIRCUMFLEX) ) {
                            alt86=1;
                        }


                        switch (alt86) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:625:12: CIRCUMFLEX and_expr
                    	    {
                    	    match(input,CIRCUMFLEX,FOLLOW_CIRCUMFLEX_in_xor_expr3195); if (state.failed) return ;
                    	    pushFollow(FOLLOW_and_expr_in_xor_expr3197);
                    	    and_expr();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt86 >= 1 ) break loop86;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(86, input);
                                throw eee;
                        }
                        cnt86++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:628:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "xor_expr"


    // $ANTLR start "and_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:632:1: and_expr : shift_expr ( ( AMPER shift_expr )+ | ) ;
    public final void and_expr() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:633:5: ( shift_expr ( ( AMPER shift_expr )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:633:7: shift_expr ( ( AMPER shift_expr )+ | )
            {
            pushFollow(FOLLOW_shift_expr_in_and_expr3248);
            shift_expr();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:634:9: ( ( AMPER shift_expr )+ | )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( (LA89_0==AMPER) ) {
                alt89=1;
            }
            else if ( (LA89_0==EOF||LA89_0==NEWLINE||LA89_0==NAME||(LA89_0>=AND && LA89_0<=AS)||LA89_0==FOR||LA89_0==IF||(LA89_0>=IN && LA89_0<=IS)||(LA89_0>=NOT && LA89_0<=ORELSE)||(LA89_0>=RPAREN && LA89_0<=COMMA)||(LA89_0>=SEMI && LA89_0<=DOUBLESLASHEQUAL)||(LA89_0>=LESS && LA89_0<=CIRCUMFLEX)||LA89_0==RBRACK||(LA89_0>=RCURLY && LA89_0<=BACKQUOTE)) ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }
            switch (alt89) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:634:11: ( AMPER shift_expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:634:11: ( AMPER shift_expr )+
                    int cnt88=0;
                    loop88:
                    do {
                        int alt88=2;
                        int LA88_0 = input.LA(1);

                        if ( (LA88_0==AMPER) ) {
                            alt88=1;
                        }


                        switch (alt88) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:634:12: AMPER shift_expr
                    	    {
                    	    match(input,AMPER,FOLLOW_AMPER_in_and_expr3261); if (state.failed) return ;
                    	    pushFollow(FOLLOW_shift_expr_in_and_expr3263);
                    	    shift_expr();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt88 >= 1 ) break loop88;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(88, input);
                                throw eee;
                        }
                        cnt88++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:637:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "and_expr"


    // $ANTLR start "shift_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:641:1: shift_expr : arith_expr ( ( shift_op arith_expr )+ | ) ;
    public final void shift_expr() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:642:5: ( arith_expr ( ( shift_op arith_expr )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:642:7: arith_expr ( ( shift_op arith_expr )+ | )
            {
            pushFollow(FOLLOW_arith_expr_in_shift_expr3314);
            arith_expr();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:643:9: ( ( shift_op arith_expr )+ | )
            int alt91=2;
            int LA91_0 = input.LA(1);

            if ( (LA91_0==RIGHTSHIFT||LA91_0==LEFTSHIFT) ) {
                alt91=1;
            }
            else if ( (LA91_0==EOF||LA91_0==NEWLINE||LA91_0==NAME||(LA91_0>=AND && LA91_0<=AS)||LA91_0==FOR||LA91_0==IF||(LA91_0>=IN && LA91_0<=IS)||(LA91_0>=NOT && LA91_0<=ORELSE)||(LA91_0>=RPAREN && LA91_0<=COMMA)||(LA91_0>=SEMI && LA91_0<=DOUBLESLASHEQUAL)||(LA91_0>=LESS && LA91_0<=AMPER)||LA91_0==RBRACK||(LA91_0>=RCURLY && LA91_0<=BACKQUOTE)) ) {
                alt91=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 91, 0, input);

                throw nvae;
            }
            switch (alt91) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:643:11: ( shift_op arith_expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:643:11: ( shift_op arith_expr )+
                    int cnt90=0;
                    loop90:
                    do {
                        int alt90=2;
                        int LA90_0 = input.LA(1);

                        if ( (LA90_0==RIGHTSHIFT||LA90_0==LEFTSHIFT) ) {
                            alt90=1;
                        }


                        switch (alt90) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:643:13: shift_op arith_expr
                    	    {
                    	    pushFollow(FOLLOW_shift_op_in_shift_expr3328);
                    	    shift_op();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    pushFollow(FOLLOW_arith_expr_in_shift_expr3330);
                    	    arith_expr();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt90 >= 1 ) break loop90;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(90, input);
                                throw eee;
                        }
                        cnt90++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:646:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "shift_expr"


    // $ANTLR start "shift_op"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:649:1: shift_op : ( LEFTSHIFT | RIGHTSHIFT );
    public final void shift_op() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:650:5: ( LEFTSHIFT | RIGHTSHIFT )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
            {
            if ( input.LA(1)==RIGHTSHIFT||input.LA(1)==LEFTSHIFT ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "shift_op"


    // $ANTLR start "arith_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:655:1: arith_expr : term ( ( arith_op term )+ | ) ;
    public final void arith_expr() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:656:5: ( term ( ( arith_op term )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:656:7: term ( ( arith_op term )+ | )
            {
            pushFollow(FOLLOW_term_in_arith_expr3406);
            term();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:657:9: ( ( arith_op term )+ | )
            int alt93=2;
            int LA93_0 = input.LA(1);

            if ( ((LA93_0>=PLUS && LA93_0<=MINUS)) ) {
                alt93=1;
            }
            else if ( (LA93_0==EOF||LA93_0==NEWLINE||LA93_0==NAME||(LA93_0>=AND && LA93_0<=AS)||LA93_0==FOR||LA93_0==IF||(LA93_0>=IN && LA93_0<=IS)||(LA93_0>=NOT && LA93_0<=ORELSE)||(LA93_0>=RPAREN && LA93_0<=COMMA)||(LA93_0>=SEMI && LA93_0<=LEFTSHIFT)||LA93_0==RBRACK||(LA93_0>=RCURLY && LA93_0<=BACKQUOTE)) ) {
                alt93=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 93, 0, input);

                throw nvae;
            }
            switch (alt93) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:657:11: ( arith_op term )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:657:11: ( arith_op term )+
                    int cnt92=0;
                    loop92:
                    do {
                        int alt92=2;
                        int LA92_0 = input.LA(1);

                        if ( ((LA92_0>=PLUS && LA92_0<=MINUS)) ) {
                            alt92=1;
                        }


                        switch (alt92) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:657:12: arith_op term
                    	    {
                    	    pushFollow(FOLLOW_arith_op_in_arith_expr3419);
                    	    arith_op();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    pushFollow(FOLLOW_term_in_arith_expr3421);
                    	    term();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt92 >= 1 ) break loop92;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(92, input);
                                throw eee;
                        }
                        cnt92++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:660:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "arith_expr"


    // $ANTLR start "arith_op"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:663:1: arith_op : ( PLUS | MINUS );
    public final void arith_op() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:664:5: ( PLUS | MINUS )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
            {
            if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "arith_op"


    // $ANTLR start "term"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:669:1: term : factor ( ( term_op factor )+ | ) ;
    public final void term() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:670:5: ( factor ( ( term_op factor )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:670:7: factor ( ( term_op factor )+ | )
            {
            pushFollow(FOLLOW_factor_in_term3497);
            factor();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:671:9: ( ( term_op factor )+ | )
            int alt95=2;
            int LA95_0 = input.LA(1);

            if ( (LA95_0==STAR||(LA95_0>=SLASH && LA95_0<=DOUBLESLASH)) ) {
                alt95=1;
            }
            else if ( (LA95_0==EOF||LA95_0==NEWLINE||LA95_0==NAME||(LA95_0>=AND && LA95_0<=AS)||LA95_0==FOR||LA95_0==IF||(LA95_0>=IN && LA95_0<=IS)||(LA95_0>=NOT && LA95_0<=ORELSE)||(LA95_0>=RPAREN && LA95_0<=COMMA)||(LA95_0>=SEMI && LA95_0<=MINUS)||LA95_0==RBRACK||(LA95_0>=RCURLY && LA95_0<=BACKQUOTE)) ) {
                alt95=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 95, 0, input);

                throw nvae;
            }
            switch (alt95) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:671:11: ( term_op factor )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:671:11: ( term_op factor )+
                    int cnt94=0;
                    loop94:
                    do {
                        int alt94=2;
                        int LA94_0 = input.LA(1);

                        if ( (LA94_0==STAR||(LA94_0>=SLASH && LA94_0<=DOUBLESLASH)) ) {
                            alt94=1;
                        }


                        switch (alt94) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:671:12: term_op factor
                    	    {
                    	    pushFollow(FOLLOW_term_op_in_term3510);
                    	    term_op();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    pushFollow(FOLLOW_factor_in_term3512);
                    	    factor();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt94 >= 1 ) break loop94;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(94, input);
                                throw eee;
                        }
                        cnt94++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:674:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "term_op"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:677:1: term_op : ( STAR | SLASH | PERCENT | DOUBLESLASH );
    public final void term_op() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:678:5: ( STAR | SLASH | PERCENT | DOUBLESLASH )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:
            {
            if ( input.LA(1)==STAR||(input.LA(1)>=SLASH && input.LA(1)<=DOUBLESLASH) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "term_op"


    // $ANTLR start "factor"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:685:1: factor : ( PLUS factor | MINUS factor | TILDE factor | power | TRAILBACKSLASH );
    public final void factor() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:686:5: ( PLUS factor | MINUS factor | TILDE factor | power | TRAILBACKSLASH )
            int alt96=5;
            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt96=1;
                }
                break;
            case MINUS:
                {
                alt96=2;
                }
                break;
            case TILDE:
                {
                alt96=3;
                }
                break;
            case NAME:
            case LPAREN:
            case LBRACK:
            case LCURLY:
            case BACKQUOTE:
            case INT:
            case LONGINT:
            case FLOAT:
            case COMPLEX:
            case STRING:
            case TRISTRINGPART:
            case STRINGPART:
                {
                alt96=4;
                }
                break;
            case TRAILBACKSLASH:
                {
                alt96=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 96, 0, input);

                throw nvae;
            }

            switch (alt96) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:686:7: PLUS factor
                    {
                    match(input,PLUS,FOLLOW_PLUS_in_factor3600); if (state.failed) return ;
                    pushFollow(FOLLOW_factor_in_factor3602);
                    factor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:687:7: MINUS factor
                    {
                    match(input,MINUS,FOLLOW_MINUS_in_factor3610); if (state.failed) return ;
                    pushFollow(FOLLOW_factor_in_factor3612);
                    factor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:688:7: TILDE factor
                    {
                    match(input,TILDE,FOLLOW_TILDE_in_factor3620); if (state.failed) return ;
                    pushFollow(FOLLOW_factor_in_factor3622);
                    factor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:689:7: power
                    {
                    pushFollow(FOLLOW_power_in_factor3630);
                    power();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:690:7: TRAILBACKSLASH
                    {
                    match(input,TRAILBACKSLASH,FOLLOW_TRAILBACKSLASH_in_factor3638); if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "factor"


    // $ANTLR start "power"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:694:1: power : atom ( trailer )* ( options {greedy=true; } : DOUBLESTAR factor )? ;
    public final void power() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:695:5: ( atom ( trailer )* ( options {greedy=true; } : DOUBLESTAR factor )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:695:7: atom ( trailer )* ( options {greedy=true; } : DOUBLESTAR factor )?
            {
            pushFollow(FOLLOW_atom_in_power3656);
            atom();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:695:12: ( trailer )*
            loop97:
            do {
                int alt97=2;
                int LA97_0 = input.LA(1);

                if ( (LA97_0==DOT||LA97_0==LPAREN||LA97_0==LBRACK) ) {
                    alt97=1;
                }


                switch (alt97) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:695:13: trailer
            	    {
            	    pushFollow(FOLLOW_trailer_in_power3659);
            	    trailer();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop97;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:695:23: ( options {greedy=true; } : DOUBLESTAR factor )?
            int alt98=2;
            int LA98_0 = input.LA(1);

            if ( (LA98_0==DOUBLESTAR) ) {
                alt98=1;
            }
            switch (alt98) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:695:47: DOUBLESTAR factor
                    {
                    match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_power3671); if (state.failed) return ;
                    pushFollow(FOLLOW_factor_in_power3673);
                    factor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "power"


    // $ANTLR start "atom"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:703:1: atom : ( LPAREN ( yield_expr | testlist_gexp | ) RPAREN | LBRACK ( listmaker | ) RBRACK | LCURLY ( dictmaker | ) RCURLY | BACKQUOTE testlist BACKQUOTE | NAME | INT | LONGINT | FLOAT | COMPLEX | ( STRING )+ | TRISTRINGPART | STRINGPART TRAILBACKSLASH );
    public final void atom() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:704:5: ( LPAREN ( yield_expr | testlist_gexp | ) RPAREN | LBRACK ( listmaker | ) RBRACK | LCURLY ( dictmaker | ) RCURLY | BACKQUOTE testlist BACKQUOTE | NAME | INT | LONGINT | FLOAT | COMPLEX | ( STRING )+ | TRISTRINGPART | STRINGPART TRAILBACKSLASH )
            int alt103=12;
            switch ( input.LA(1) ) {
            case LPAREN:
                {
                alt103=1;
                }
                break;
            case LBRACK:
                {
                alt103=2;
                }
                break;
            case LCURLY:
                {
                alt103=3;
                }
                break;
            case BACKQUOTE:
                {
                alt103=4;
                }
                break;
            case NAME:
                {
                alt103=5;
                }
                break;
            case INT:
                {
                alt103=6;
                }
                break;
            case LONGINT:
                {
                alt103=7;
                }
                break;
            case FLOAT:
                {
                alt103=8;
                }
                break;
            case COMPLEX:
                {
                alt103=9;
                }
                break;
            case STRING:
                {
                alt103=10;
                }
                break;
            case TRISTRINGPART:
                {
                alt103=11;
                }
                break;
            case STRINGPART:
                {
                alt103=12;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 103, 0, input);

                throw nvae;
            }

            switch (alt103) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:704:7: LPAREN ( yield_expr | testlist_gexp | ) RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_atom3697); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:705:7: ( yield_expr | testlist_gexp | )
                    int alt99=3;
                    switch ( input.LA(1) ) {
                    case YIELD:
                        {
                        alt99=1;
                        }
                        break;
                    case TRAILBACKSLASH:
                    case NAME:
                    case LAMBDA:
                    case NOT:
                    case LPAREN:
                    case PLUS:
                    case MINUS:
                    case TILDE:
                    case LBRACK:
                    case LCURLY:
                    case BACKQUOTE:
                    case INT:
                    case LONGINT:
                    case FLOAT:
                    case COMPLEX:
                    case STRING:
                    case TRISTRINGPART:
                    case STRINGPART:
                        {
                        alt99=2;
                        }
                        break;
                    case RPAREN:
                        {
                        alt99=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 99, 0, input);

                        throw nvae;
                    }

                    switch (alt99) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:705:9: yield_expr
                            {
                            pushFollow(FOLLOW_yield_expr_in_atom3707);
                            yield_expr();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:706:9: testlist_gexp
                            {
                            pushFollow(FOLLOW_testlist_gexp_in_atom3717);
                            testlist_gexp();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:708:7: 
                            {
                            }
                            break;

                    }

                    match(input,RPAREN,FOLLOW_RPAREN_in_atom3741); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:710:7: LBRACK ( listmaker | ) RBRACK
                    {
                    match(input,LBRACK,FOLLOW_LBRACK_in_atom3749); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:711:7: ( listmaker | )
                    int alt100=2;
                    int LA100_0 = input.LA(1);

                    if ( (LA100_0==TRAILBACKSLASH||LA100_0==NAME||(LA100_0>=LAMBDA && LA100_0<=NOT)||LA100_0==LPAREN||(LA100_0>=PLUS && LA100_0<=MINUS)||(LA100_0>=TILDE && LA100_0<=LBRACK)||LA100_0==LCURLY||(LA100_0>=BACKQUOTE && LA100_0<=STRING)||(LA100_0>=TRISTRINGPART && LA100_0<=STRINGPART)) ) {
                        alt100=1;
                    }
                    else if ( (LA100_0==RBRACK) ) {
                        alt100=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 100, 0, input);

                        throw nvae;
                    }
                    switch (alt100) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:711:8: listmaker
                            {
                            pushFollow(FOLLOW_listmaker_in_atom3758);
                            listmaker();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:713:7: 
                            {
                            }
                            break;

                    }

                    match(input,RBRACK,FOLLOW_RBRACK_in_atom3782); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:715:7: LCURLY ( dictmaker | ) RCURLY
                    {
                    match(input,LCURLY,FOLLOW_LCURLY_in_atom3790); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:716:8: ( dictmaker | )
                    int alt101=2;
                    int LA101_0 = input.LA(1);

                    if ( (LA101_0==TRAILBACKSLASH||LA101_0==NAME||(LA101_0>=LAMBDA && LA101_0<=NOT)||LA101_0==LPAREN||(LA101_0>=PLUS && LA101_0<=MINUS)||(LA101_0>=TILDE && LA101_0<=LBRACK)||LA101_0==LCURLY||(LA101_0>=BACKQUOTE && LA101_0<=STRING)||(LA101_0>=TRISTRINGPART && LA101_0<=STRINGPART)) ) {
                        alt101=1;
                    }
                    else if ( (LA101_0==RCURLY) ) {
                        alt101=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 101, 0, input);

                        throw nvae;
                    }
                    switch (alt101) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:716:9: dictmaker
                            {
                            pushFollow(FOLLOW_dictmaker_in_atom3800);
                            dictmaker();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:718:8: 
                            {
                            }
                            break;

                    }

                    match(input,RCURLY,FOLLOW_RCURLY_in_atom3827); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:720:8: BACKQUOTE testlist BACKQUOTE
                    {
                    match(input,BACKQUOTE,FOLLOW_BACKQUOTE_in_atom3836); if (state.failed) return ;
                    pushFollow(FOLLOW_testlist_in_atom3838);
                    testlist();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,BACKQUOTE,FOLLOW_BACKQUOTE_in_atom3840); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:721:8: NAME
                    {
                    match(input,NAME,FOLLOW_NAME_in_atom3849); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:722:8: INT
                    {
                    match(input,INT,FOLLOW_INT_in_atom3858); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:723:8: LONGINT
                    {
                    match(input,LONGINT,FOLLOW_LONGINT_in_atom3867); if (state.failed) return ;

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:724:8: FLOAT
                    {
                    match(input,FLOAT,FOLLOW_FLOAT_in_atom3876); if (state.failed) return ;

                    }
                    break;
                case 9 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:725:8: COMPLEX
                    {
                    match(input,COMPLEX,FOLLOW_COMPLEX_in_atom3885); if (state.failed) return ;

                    }
                    break;
                case 10 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:726:8: ( STRING )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:726:8: ( STRING )+
                    int cnt102=0;
                    loop102:
                    do {
                        int alt102=2;
                        int LA102_0 = input.LA(1);

                        if ( (LA102_0==STRING) ) {
                            alt102=1;
                        }


                        switch (alt102) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:726:9: STRING
                    	    {
                    	    match(input,STRING,FOLLOW_STRING_in_atom3895); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt102 >= 1 ) break loop102;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(102, input);
                                throw eee;
                        }
                        cnt102++;
                    } while (true);


                    }
                    break;
                case 11 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:727:8: TRISTRINGPART
                    {
                    match(input,TRISTRINGPART,FOLLOW_TRISTRINGPART_in_atom3906); if (state.failed) return ;

                    }
                    break;
                case 12 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:728:8: STRINGPART TRAILBACKSLASH
                    {
                    match(input,STRINGPART,FOLLOW_STRINGPART_in_atom3915); if (state.failed) return ;
                    match(input,TRAILBACKSLASH,FOLLOW_TRAILBACKSLASH_in_atom3917); if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "atom"


    // $ANTLR start "listmaker"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:732:1: listmaker : test ( list_for | ( options {greedy=true; } : COMMA test )* ) ( COMMA )? ;
    public final void listmaker() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:733:5: ( test ( list_for | ( options {greedy=true; } : COMMA test )* ) ( COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:733:7: test ( list_for | ( options {greedy=true; } : COMMA test )* ) ( COMMA )?
            {
            pushFollow(FOLLOW_test_in_listmaker3936);
            test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:734:9: ( list_for | ( options {greedy=true; } : COMMA test )* )
            int alt105=2;
            int LA105_0 = input.LA(1);

            if ( (LA105_0==FOR) ) {
                alt105=1;
            }
            else if ( (LA105_0==COMMA||LA105_0==RBRACK) ) {
                alt105=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 105, 0, input);

                throw nvae;
            }
            switch (alt105) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:734:10: list_for
                    {
                    pushFollow(FOLLOW_list_for_in_listmaker3947);
                    list_for();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:735:11: ( options {greedy=true; } : COMMA test )*
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:735:11: ( options {greedy=true; } : COMMA test )*
                    loop104:
                    do {
                        int alt104=2;
                        int LA104_0 = input.LA(1);

                        if ( (LA104_0==COMMA) ) {
                            int LA104_1 = input.LA(2);

                            if ( (LA104_1==TRAILBACKSLASH||LA104_1==NAME||(LA104_1>=LAMBDA && LA104_1<=NOT)||LA104_1==LPAREN||(LA104_1>=PLUS && LA104_1<=MINUS)||(LA104_1>=TILDE && LA104_1<=LBRACK)||LA104_1==LCURLY||(LA104_1>=BACKQUOTE && LA104_1<=STRING)||(LA104_1>=TRISTRINGPART && LA104_1<=STRINGPART)) ) {
                                alt104=1;
                            }


                        }


                        switch (alt104) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:735:35: COMMA test
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_listmaker3967); if (state.failed) return ;
                    	    pushFollow(FOLLOW_test_in_listmaker3969);
                    	    test();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop104;
                        }
                    } while (true);


                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:736:11: ( COMMA )?
            int alt106=2;
            int LA106_0 = input.LA(1);

            if ( (LA106_0==COMMA) ) {
                alt106=1;
            }
            switch (alt106) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:736:12: COMMA
                    {
                    match(input,COMMA,FOLLOW_COMMA_in_listmaker3984); if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "listmaker"


    // $ANTLR start "testlist_gexp"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:740:1: testlist_gexp : test ( ( ( options {k=2; } : COMMA test )* ( COMMA )? ) | ( gen_for ) ) ;
    public final void testlist_gexp() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:741:5: ( test ( ( ( options {k=2; } : COMMA test )* ( COMMA )? ) | ( gen_for ) ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:741:7: test ( ( ( options {k=2; } : COMMA test )* ( COMMA )? ) | ( gen_for ) )
            {
            pushFollow(FOLLOW_test_in_testlist_gexp4004);
            test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:742:9: ( ( ( options {k=2; } : COMMA test )* ( COMMA )? ) | ( gen_for ) )
            int alt109=2;
            int LA109_0 = input.LA(1);

            if ( (LA109_0==RPAREN||LA109_0==COMMA) ) {
                alt109=1;
            }
            else if ( (LA109_0==FOR) ) {
                alt109=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 109, 0, input);

                throw nvae;
            }
            switch (alt109) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:742:11: ( ( options {k=2; } : COMMA test )* ( COMMA )? )
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:742:11: ( ( options {k=2; } : COMMA test )* ( COMMA )? )
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:742:12: ( options {k=2; } : COMMA test )* ( COMMA )?
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:742:12: ( options {k=2; } : COMMA test )*
                    loop107:
                    do {
                        int alt107=2;
                        alt107 = dfa107.predict(input);
                        switch (alt107) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:742:29: COMMA test
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_testlist_gexp4026); if (state.failed) return ;
                    	    pushFollow(FOLLOW_test_in_testlist_gexp4028);
                    	    test();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop107;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:742:42: ( COMMA )?
                    int alt108=2;
                    int LA108_0 = input.LA(1);

                    if ( (LA108_0==COMMA) ) {
                        alt108=1;
                    }
                    switch (alt108) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:742:43: COMMA
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_testlist_gexp4033); if (state.failed) return ;

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:744:11: ( gen_for )
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:744:11: ( gen_for )
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:744:12: gen_for
                    {
                    pushFollow(FOLLOW_gen_for_in_testlist_gexp4060);
                    gen_for();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "testlist_gexp"


    // $ANTLR start "lambdef"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:750:1: lambdef : LAMBDA ( varargslist )? COLON test ;
    public final void lambdef() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:751:5: ( LAMBDA ( varargslist )? COLON test )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:751:7: LAMBDA ( varargslist )? COLON test
            {
            match(input,LAMBDA,FOLLOW_LAMBDA_in_lambdef4100); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:751:14: ( varargslist )?
            int alt110=2;
            int LA110_0 = input.LA(1);

            if ( (LA110_0==NAME||LA110_0==LPAREN||(LA110_0>=STAR && LA110_0<=DOUBLESTAR)) ) {
                alt110=1;
            }
            switch (alt110) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:751:15: varargslist
                    {
                    pushFollow(FOLLOW_varargslist_in_lambdef4103);
                    varargslist();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,COLON,FOLLOW_COLON_in_lambdef4107); if (state.failed) return ;
            pushFollow(FOLLOW_test_in_lambdef4109);
            test();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "lambdef"


    // $ANTLR start "trailer"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:755:1: trailer : ( LPAREN ( arglist | ) RPAREN | LBRACK subscriptlist RBRACK | DOT attr );
    public final void trailer() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:756:5: ( LPAREN ( arglist | ) RPAREN | LBRACK subscriptlist RBRACK | DOT attr )
            int alt112=3;
            switch ( input.LA(1) ) {
            case LPAREN:
                {
                alt112=1;
                }
                break;
            case LBRACK:
                {
                alt112=2;
                }
                break;
            case DOT:
                {
                alt112=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 112, 0, input);

                throw nvae;
            }

            switch (alt112) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:756:7: LPAREN ( arglist | ) RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_trailer4127); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:757:9: ( arglist | )
                    int alt111=2;
                    int LA111_0 = input.LA(1);

                    if ( (LA111_0==TRAILBACKSLASH||LA111_0==NAME||(LA111_0>=LAMBDA && LA111_0<=NOT)||LA111_0==LPAREN||(LA111_0>=STAR && LA111_0<=DOUBLESTAR)||(LA111_0>=PLUS && LA111_0<=MINUS)||(LA111_0>=TILDE && LA111_0<=LBRACK)||LA111_0==LCURLY||(LA111_0>=BACKQUOTE && LA111_0<=STRING)||(LA111_0>=TRISTRINGPART && LA111_0<=STRINGPART)) ) {
                        alt111=1;
                    }
                    else if ( (LA111_0==RPAREN) ) {
                        alt111=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 111, 0, input);

                        throw nvae;
                    }
                    switch (alt111) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:757:10: arglist
                            {
                            pushFollow(FOLLOW_arglist_in_trailer4138);
                            arglist();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:759:9: 
                            {
                            }
                            break;

                    }

                    match(input,RPAREN,FOLLOW_RPAREN_in_trailer4166); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:761:7: LBRACK subscriptlist RBRACK
                    {
                    match(input,LBRACK,FOLLOW_LBRACK_in_trailer4174); if (state.failed) return ;
                    pushFollow(FOLLOW_subscriptlist_in_trailer4176);
                    subscriptlist();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,RBRACK,FOLLOW_RBRACK_in_trailer4178); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:762:7: DOT attr
                    {
                    match(input,DOT,FOLLOW_DOT_in_trailer4186); if (state.failed) return ;
                    pushFollow(FOLLOW_attr_in_trailer4188);
                    attr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "trailer"


    // $ANTLR start "subscriptlist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:766:1: subscriptlist : subscript ( options {greedy=true; } : COMMA subscript )* ( COMMA )? ;
    public final void subscriptlist() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:767:5: ( subscript ( options {greedy=true; } : COMMA subscript )* ( COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:767:7: subscript ( options {greedy=true; } : COMMA subscript )* ( COMMA )?
            {
            pushFollow(FOLLOW_subscript_in_subscriptlist4206);
            subscript();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:767:17: ( options {greedy=true; } : COMMA subscript )*
            loop113:
            do {
                int alt113=2;
                int LA113_0 = input.LA(1);

                if ( (LA113_0==COMMA) ) {
                    int LA113_1 = input.LA(2);

                    if ( (LA113_1==TRAILBACKSLASH||(LA113_1>=NAME && LA113_1<=DOT)||(LA113_1>=LAMBDA && LA113_1<=NOT)||LA113_1==LPAREN||LA113_1==COLON||(LA113_1>=PLUS && LA113_1<=MINUS)||(LA113_1>=TILDE && LA113_1<=LBRACK)||LA113_1==LCURLY||(LA113_1>=BACKQUOTE && LA113_1<=STRING)||(LA113_1>=TRISTRINGPART && LA113_1<=STRINGPART)) ) {
                        alt113=1;
                    }


                }


                switch (alt113) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:767:41: COMMA subscript
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_subscriptlist4216); if (state.failed) return ;
            	    pushFollow(FOLLOW_subscript_in_subscriptlist4218);
            	    subscript();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop113;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:767:59: ( COMMA )?
            int alt114=2;
            int LA114_0 = input.LA(1);

            if ( (LA114_0==COMMA) ) {
                alt114=1;
            }
            switch (alt114) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:767:60: COMMA
                    {
                    match(input,COMMA,FOLLOW_COMMA_in_subscriptlist4223); if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "subscriptlist"


    // $ANTLR start "subscript"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:771:1: subscript : ( DOT DOT DOT | ( test COLON )=> test ( COLON ( test )? ( sliceop )? )? | ( COLON )=> COLON ( test )? ( sliceop )? | test );
    public final void subscript() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:772:5: ( DOT DOT DOT | ( test COLON )=> test ( COLON ( test )? ( sliceop )? )? | ( COLON )=> COLON ( test )? ( sliceop )? | test )
            int alt120=4;
            alt120 = dfa120.predict(input);
            switch (alt120) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:772:7: DOT DOT DOT
                    {
                    match(input,DOT,FOLLOW_DOT_in_subscript4243); if (state.failed) return ;
                    match(input,DOT,FOLLOW_DOT_in_subscript4245); if (state.failed) return ;
                    match(input,DOT,FOLLOW_DOT_in_subscript4247); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:773:7: ( test COLON )=> test ( COLON ( test )? ( sliceop )? )?
                    {
                    pushFollow(FOLLOW_test_in_subscript4266);
                    test();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:774:12: ( COLON ( test )? ( sliceop )? )?
                    int alt117=2;
                    int LA117_0 = input.LA(1);

                    if ( (LA117_0==COLON) ) {
                        alt117=1;
                    }
                    switch (alt117) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:774:13: COLON ( test )? ( sliceop )?
                            {
                            match(input,COLON,FOLLOW_COLON_in_subscript4269); if (state.failed) return ;
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:774:19: ( test )?
                            int alt115=2;
                            int LA115_0 = input.LA(1);

                            if ( (LA115_0==TRAILBACKSLASH||LA115_0==NAME||(LA115_0>=LAMBDA && LA115_0<=NOT)||LA115_0==LPAREN||(LA115_0>=PLUS && LA115_0<=MINUS)||(LA115_0>=TILDE && LA115_0<=LBRACK)||LA115_0==LCURLY||(LA115_0>=BACKQUOTE && LA115_0<=STRING)||(LA115_0>=TRISTRINGPART && LA115_0<=STRINGPART)) ) {
                                alt115=1;
                            }
                            switch (alt115) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:774:20: test
                                    {
                                    pushFollow(FOLLOW_test_in_subscript4272);
                                    test();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }

                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:774:27: ( sliceop )?
                            int alt116=2;
                            int LA116_0 = input.LA(1);

                            if ( (LA116_0==COLON) ) {
                                alt116=1;
                            }
                            switch (alt116) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:774:28: sliceop
                                    {
                                    pushFollow(FOLLOW_sliceop_in_subscript4277);
                                    sliceop();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:775:7: ( COLON )=> COLON ( test )? ( sliceop )?
                    {
                    match(input,COLON,FOLLOW_COLON_in_subscript4298); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:776:13: ( test )?
                    int alt118=2;
                    int LA118_0 = input.LA(1);

                    if ( (LA118_0==TRAILBACKSLASH||LA118_0==NAME||(LA118_0>=LAMBDA && LA118_0<=NOT)||LA118_0==LPAREN||(LA118_0>=PLUS && LA118_0<=MINUS)||(LA118_0>=TILDE && LA118_0<=LBRACK)||LA118_0==LCURLY||(LA118_0>=BACKQUOTE && LA118_0<=STRING)||(LA118_0>=TRISTRINGPART && LA118_0<=STRINGPART)) ) {
                        alt118=1;
                    }
                    switch (alt118) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:776:14: test
                            {
                            pushFollow(FOLLOW_test_in_subscript4301);
                            test();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:776:21: ( sliceop )?
                    int alt119=2;
                    int LA119_0 = input.LA(1);

                    if ( (LA119_0==COLON) ) {
                        alt119=1;
                    }
                    switch (alt119) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:776:22: sliceop
                            {
                            pushFollow(FOLLOW_sliceop_in_subscript4306);
                            sliceop();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:777:7: test
                    {
                    pushFollow(FOLLOW_test_in_subscript4316);
                    test();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "subscript"


    // $ANTLR start "sliceop"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:781:1: sliceop : COLON ( test | ) ;
    public final void sliceop() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:782:5: ( COLON ( test | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:782:7: COLON ( test | )
            {
            match(input,COLON,FOLLOW_COLON_in_sliceop4334); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:783:6: ( test | )
            int alt121=2;
            int LA121_0 = input.LA(1);

            if ( (LA121_0==TRAILBACKSLASH||LA121_0==NAME||(LA121_0>=LAMBDA && LA121_0<=NOT)||LA121_0==LPAREN||(LA121_0>=PLUS && LA121_0<=MINUS)||(LA121_0>=TILDE && LA121_0<=LBRACK)||LA121_0==LCURLY||(LA121_0>=BACKQUOTE && LA121_0<=STRING)||(LA121_0>=TRISTRINGPART && LA121_0<=STRINGPART)) ) {
                alt121=1;
            }
            else if ( (LA121_0==COMMA||LA121_0==RBRACK) ) {
                alt121=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 121, 0, input);

                throw nvae;
            }
            switch (alt121) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:783:7: test
                    {
                    pushFollow(FOLLOW_test_in_sliceop4342);
                    test();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:785:6: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "sliceop"


    // $ANTLR start "exprlist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:789:1: exprlist : ( ( expr COMMA )=> expr ( options {k=2; } : COMMA expr )* ( COMMA )? | expr );
    public final void exprlist() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:790:5: ( ( expr COMMA )=> expr ( options {k=2; } : COMMA expr )* ( COMMA )? | expr )
            int alt124=2;
            alt124 = dfa124.predict(input);
            switch (alt124) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:790:7: ( expr COMMA )=> expr ( options {k=2; } : COMMA expr )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_expr_in_exprlist4382);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:790:28: ( options {k=2; } : COMMA expr )*
                    loop122:
                    do {
                        int alt122=2;
                        alt122 = dfa122.predict(input);
                        switch (alt122) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:790:45: COMMA expr
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_exprlist4393); if (state.failed) return ;
                    	    pushFollow(FOLLOW_expr_in_exprlist4395);
                    	    expr();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop122;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:790:58: ( COMMA )?
                    int alt123=2;
                    int LA123_0 = input.LA(1);

                    if ( (LA123_0==COMMA) ) {
                        alt123=1;
                    }
                    switch (alt123) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:790:59: COMMA
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_exprlist4400); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:791:7: expr
                    {
                    pushFollow(FOLLOW_expr_in_exprlist4410);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "exprlist"


    // $ANTLR start "del_list"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:796:1: del_list : expr ( options {k=2; } : COMMA expr )* ( COMMA )? ;
    public final void del_list() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:797:5: ( expr ( options {k=2; } : COMMA expr )* ( COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:797:7: expr ( options {k=2; } : COMMA expr )* ( COMMA )?
            {
            pushFollow(FOLLOW_expr_in_del_list4429);
            expr();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:797:12: ( options {k=2; } : COMMA expr )*
            loop125:
            do {
                int alt125=2;
                alt125 = dfa125.predict(input);
                switch (alt125) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:797:29: COMMA expr
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_del_list4440); if (state.failed) return ;
            	    pushFollow(FOLLOW_expr_in_del_list4442);
            	    expr();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop125;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:797:42: ( COMMA )?
            int alt126=2;
            int LA126_0 = input.LA(1);

            if ( (LA126_0==COMMA) ) {
                alt126=1;
            }
            switch (alt126) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:797:43: COMMA
                    {
                    match(input,COMMA,FOLLOW_COMMA_in_del_list4447); if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "del_list"


    // $ANTLR start "testlist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:801:1: testlist : ( ( test COMMA )=> test ( options {k=2; } : COMMA test )* ( COMMA )? | test );
    public final void testlist() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:802:5: ( ( test COMMA )=> test ( options {k=2; } : COMMA test )* ( COMMA )? | test )
            int alt129=2;
            alt129 = dfa129.predict(input);
            switch (alt129) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:802:7: ( test COMMA )=> test ( options {k=2; } : COMMA test )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_test_in_testlist4478);
                    test();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:803:12: ( options {k=2; } : COMMA test )*
                    loop127:
                    do {
                        int alt127=2;
                        alt127 = dfa127.predict(input);
                        switch (alt127) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:803:29: COMMA test
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_testlist4489); if (state.failed) return ;
                    	    pushFollow(FOLLOW_test_in_testlist4491);
                    	    test();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop127;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:803:42: ( COMMA )?
                    int alt128=2;
                    int LA128_0 = input.LA(1);

                    if ( (LA128_0==COMMA) ) {
                        alt128=1;
                    }
                    switch (alt128) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:803:43: COMMA
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_testlist4496); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:804:7: test
                    {
                    pushFollow(FOLLOW_test_in_testlist4506);
                    test();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "testlist"


    // $ANTLR start "dictmaker"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:808:1: dictmaker : test COLON test ( options {k=2; } : COMMA test COLON test )* ( COMMA )? ;
    public final void dictmaker() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:809:5: ( test COLON test ( options {k=2; } : COMMA test COLON test )* ( COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:809:7: test COLON test ( options {k=2; } : COMMA test COLON test )* ( COMMA )?
            {
            pushFollow(FOLLOW_test_in_dictmaker4524);
            test();

            state._fsp--;
            if (state.failed) return ;
            match(input,COLON,FOLLOW_COLON_in_dictmaker4526); if (state.failed) return ;
            pushFollow(FOLLOW_test_in_dictmaker4528);
            test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:810:9: ( options {k=2; } : COMMA test COLON test )*
            loop130:
            do {
                int alt130=2;
                alt130 = dfa130.predict(input);
                switch (alt130) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:810:25: COMMA test COLON test
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_dictmaker4546); if (state.failed) return ;
            	    pushFollow(FOLLOW_test_in_dictmaker4548);
            	    test();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    match(input,COLON,FOLLOW_COLON_in_dictmaker4550); if (state.failed) return ;
            	    pushFollow(FOLLOW_test_in_dictmaker4552);
            	    test();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop130;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:811:9: ( COMMA )?
            int alt131=2;
            int LA131_0 = input.LA(1);

            if ( (LA131_0==COMMA) ) {
                alt131=1;
            }
            switch (alt131) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:811:10: COMMA
                    {
                    match(input,COMMA,FOLLOW_COMMA_in_dictmaker4565); if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "dictmaker"


    // $ANTLR start "classdef"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:815:1: classdef : ( decorators )? CLASS NAME ( LPAREN ( testlist )? RPAREN )? COLON suite ;
    public final void classdef() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:816:5: ( ( decorators )? CLASS NAME ( LPAREN ( testlist )? RPAREN )? COLON suite )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:816:7: ( decorators )? CLASS NAME ( LPAREN ( testlist )? RPAREN )? COLON suite
            {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:816:7: ( decorators )?
            int alt132=2;
            int LA132_0 = input.LA(1);

            if ( (LA132_0==AT) ) {
                alt132=1;
            }
            switch (alt132) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:816:7: decorators
                    {
                    pushFollow(FOLLOW_decorators_in_classdef4585);
                    decorators();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            match(input,CLASS,FOLLOW_CLASS_in_classdef4588); if (state.failed) return ;
            match(input,NAME,FOLLOW_NAME_in_classdef4590); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:816:30: ( LPAREN ( testlist )? RPAREN )?
            int alt134=2;
            int LA134_0 = input.LA(1);

            if ( (LA134_0==LPAREN) ) {
                alt134=1;
            }
            switch (alt134) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:816:31: LPAREN ( testlist )? RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_classdef4593); if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:816:38: ( testlist )?
                    int alt133=2;
                    int LA133_0 = input.LA(1);

                    if ( (LA133_0==TRAILBACKSLASH||LA133_0==NAME||(LA133_0>=LAMBDA && LA133_0<=NOT)||LA133_0==LPAREN||(LA133_0>=PLUS && LA133_0<=MINUS)||(LA133_0>=TILDE && LA133_0<=LBRACK)||LA133_0==LCURLY||(LA133_0>=BACKQUOTE && LA133_0<=STRING)||(LA133_0>=TRISTRINGPART && LA133_0<=STRINGPART)) ) {
                        alt133=1;
                    }
                    switch (alt133) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:816:38: testlist
                            {
                            pushFollow(FOLLOW_testlist_in_classdef4595);
                            testlist();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }

                    match(input,RPAREN,FOLLOW_RPAREN_in_classdef4598); if (state.failed) return ;

                    }
                    break;

            }

            match(input,COLON,FOLLOW_COLON_in_classdef4602); if (state.failed) return ;
            pushFollow(FOLLOW_suite_in_classdef4604);
            suite();

            state._fsp--;
            if (state.failed) return ;

            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "classdef"


    // $ANTLR start "arglist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:820:1: arglist : ( argument ( COMMA argument )* ( COMMA ( STAR test ( COMMA DOUBLESTAR test )? | DOUBLESTAR test )? )? | STAR test ( COMMA DOUBLESTAR test )? | DOUBLESTAR test );
    public final void arglist() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:821:5: ( argument ( COMMA argument )* ( COMMA ( STAR test ( COMMA DOUBLESTAR test )? | DOUBLESTAR test )? )? | STAR test ( COMMA DOUBLESTAR test )? | DOUBLESTAR test )
            int alt140=3;
            switch ( input.LA(1) ) {
            case TRAILBACKSLASH:
            case NAME:
            case LAMBDA:
            case NOT:
            case LPAREN:
            case PLUS:
            case MINUS:
            case TILDE:
            case LBRACK:
            case LCURLY:
            case BACKQUOTE:
            case INT:
            case LONGINT:
            case FLOAT:
            case COMPLEX:
            case STRING:
            case TRISTRINGPART:
            case STRINGPART:
                {
                alt140=1;
                }
                break;
            case STAR:
                {
                alt140=2;
                }
                break;
            case DOUBLESTAR:
                {
                alt140=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 140, 0, input);

                throw nvae;
            }

            switch (alt140) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:821:7: argument ( COMMA argument )* ( COMMA ( STAR test ( COMMA DOUBLESTAR test )? | DOUBLESTAR test )? )?
                    {
                    pushFollow(FOLLOW_argument_in_arglist4622);
                    argument();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:821:16: ( COMMA argument )*
                    loop135:
                    do {
                        int alt135=2;
                        int LA135_0 = input.LA(1);

                        if ( (LA135_0==COMMA) ) {
                            int LA135_1 = input.LA(2);

                            if ( (LA135_1==TRAILBACKSLASH||LA135_1==NAME||(LA135_1>=LAMBDA && LA135_1<=NOT)||LA135_1==LPAREN||(LA135_1>=PLUS && LA135_1<=MINUS)||(LA135_1>=TILDE && LA135_1<=LBRACK)||LA135_1==LCURLY||(LA135_1>=BACKQUOTE && LA135_1<=STRING)||(LA135_1>=TRISTRINGPART && LA135_1<=STRINGPART)) ) {
                                alt135=1;
                            }


                        }


                        switch (alt135) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:821:17: COMMA argument
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_arglist4625); if (state.failed) return ;
                    	    pushFollow(FOLLOW_argument_in_arglist4627);
                    	    argument();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop135;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:822:11: ( COMMA ( STAR test ( COMMA DOUBLESTAR test )? | DOUBLESTAR test )? )?
                    int alt138=2;
                    int LA138_0 = input.LA(1);

                    if ( (LA138_0==COMMA) ) {
                        alt138=1;
                    }
                    switch (alt138) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:822:12: COMMA ( STAR test ( COMMA DOUBLESTAR test )? | DOUBLESTAR test )?
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_arglist4642); if (state.failed) return ;
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:823:15: ( STAR test ( COMMA DOUBLESTAR test )? | DOUBLESTAR test )?
                            int alt137=3;
                            int LA137_0 = input.LA(1);

                            if ( (LA137_0==STAR) ) {
                                alt137=1;
                            }
                            else if ( (LA137_0==DOUBLESTAR) ) {
                                alt137=2;
                            }
                            switch (alt137) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:823:17: STAR test ( COMMA DOUBLESTAR test )?
                                    {
                                    match(input,STAR,FOLLOW_STAR_in_arglist4660); if (state.failed) return ;
                                    pushFollow(FOLLOW_test_in_arglist4662);
                                    test();

                                    state._fsp--;
                                    if (state.failed) return ;
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:823:27: ( COMMA DOUBLESTAR test )?
                                    int alt136=2;
                                    int LA136_0 = input.LA(1);

                                    if ( (LA136_0==COMMA) ) {
                                        alt136=1;
                                    }
                                    switch (alt136) {
                                        case 1 :
                                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:823:28: COMMA DOUBLESTAR test
                                            {
                                            match(input,COMMA,FOLLOW_COMMA_in_arglist4665); if (state.failed) return ;
                                            match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_arglist4667); if (state.failed) return ;
                                            pushFollow(FOLLOW_test_in_arglist4669);
                                            test();

                                            state._fsp--;
                                            if (state.failed) return ;

                                            }
                                            break;

                                    }


                                    }
                                    break;
                                case 2 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:824:17: DOUBLESTAR test
                                    {
                                    match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_arglist4689); if (state.failed) return ;
                                    pushFollow(FOLLOW_test_in_arglist4691);
                                    test();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:827:7: STAR test ( COMMA DOUBLESTAR test )?
                    {
                    match(input,STAR,FOLLOW_STAR_in_arglist4729); if (state.failed) return ;
                    pushFollow(FOLLOW_test_in_arglist4731);
                    test();

                    state._fsp--;
                    if (state.failed) return ;
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:827:17: ( COMMA DOUBLESTAR test )?
                    int alt139=2;
                    int LA139_0 = input.LA(1);

                    if ( (LA139_0==COMMA) ) {
                        alt139=1;
                    }
                    switch (alt139) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:827:18: COMMA DOUBLESTAR test
                            {
                            match(input,COMMA,FOLLOW_COMMA_in_arglist4734); if (state.failed) return ;
                            match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_arglist4736); if (state.failed) return ;
                            pushFollow(FOLLOW_test_in_arglist4738);
                            test();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:828:7: DOUBLESTAR test
                    {
                    match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_arglist4748); if (state.failed) return ;
                    pushFollow(FOLLOW_test_in_arglist4750);
                    test();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "arglist"


    // $ANTLR start "argument"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:832:1: argument : test ( ( ASSIGN test ) | gen_for | ) ;
    public final void argument() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:833:5: ( test ( ( ASSIGN test ) | gen_for | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:833:7: test ( ( ASSIGN test ) | gen_for | )
            {
            pushFollow(FOLLOW_test_in_argument4768);
            test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:834:9: ( ( ASSIGN test ) | gen_for | )
            int alt141=3;
            switch ( input.LA(1) ) {
            case ASSIGN:
                {
                alt141=1;
                }
                break;
            case FOR:
                {
                alt141=2;
                }
                break;
            case RPAREN:
            case COMMA:
                {
                alt141=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 141, 0, input);

                throw nvae;
            }

            switch (alt141) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:834:10: ( ASSIGN test )
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:834:10: ( ASSIGN test )
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:834:11: ASSIGN test
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_argument4780); if (state.failed) return ;
                    pushFollow(FOLLOW_test_in_argument4782);
                    test();

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:835:11: gen_for
                    {
                    pushFollow(FOLLOW_gen_for_in_argument4795);
                    gen_for();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:837:9: 
                    {
                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "argument"


    // $ANTLR start "list_iter"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:841:1: list_iter : ( list_for | list_if );
    public final void list_iter() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:842:5: ( list_for | list_if )
            int alt142=2;
            int LA142_0 = input.LA(1);

            if ( (LA142_0==FOR) ) {
                alt142=1;
            }
            else if ( (LA142_0==IF) ) {
                alt142=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 142, 0, input);

                throw nvae;
            }
            switch (alt142) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:842:7: list_for
                    {
                    pushFollow(FOLLOW_list_for_in_list_iter4833);
                    list_for();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:843:7: list_if
                    {
                    pushFollow(FOLLOW_list_if_in_list_iter4841);
                    list_if();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "list_iter"


    // $ANTLR start "list_for"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:847:1: list_for : FOR exprlist IN testlist ( list_iter )? ;
    public final void list_for() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:848:5: ( FOR exprlist IN testlist ( list_iter )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:848:7: FOR exprlist IN testlist ( list_iter )?
            {
            match(input,FOR,FOLLOW_FOR_in_list_for4859); if (state.failed) return ;
            pushFollow(FOLLOW_exprlist_in_list_for4861);
            exprlist();

            state._fsp--;
            if (state.failed) return ;
            match(input,IN,FOLLOW_IN_in_list_for4863); if (state.failed) return ;
            pushFollow(FOLLOW_testlist_in_list_for4865);
            testlist();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:848:32: ( list_iter )?
            int alt143=2;
            int LA143_0 = input.LA(1);

            if ( (LA143_0==FOR||LA143_0==IF) ) {
                alt143=1;
            }
            switch (alt143) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:848:33: list_iter
                    {
                    pushFollow(FOLLOW_list_iter_in_list_for4868);
                    list_iter();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "list_for"


    // $ANTLR start "list_if"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:852:1: list_if : IF test ( list_iter )? ;
    public final void list_if() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:853:5: ( IF test ( list_iter )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:853:7: IF test ( list_iter )?
            {
            match(input,IF,FOLLOW_IF_in_list_if4888); if (state.failed) return ;
            pushFollow(FOLLOW_test_in_list_if4890);
            test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:853:15: ( list_iter )?
            int alt144=2;
            int LA144_0 = input.LA(1);

            if ( (LA144_0==FOR||LA144_0==IF) ) {
                alt144=1;
            }
            switch (alt144) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:853:16: list_iter
                    {
                    pushFollow(FOLLOW_list_iter_in_list_if4893);
                    list_iter();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "list_if"


    // $ANTLR start "gen_iter"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:857:1: gen_iter : ( gen_for | gen_if );
    public final void gen_iter() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:858:5: ( gen_for | gen_if )
            int alt145=2;
            int LA145_0 = input.LA(1);

            if ( (LA145_0==FOR) ) {
                alt145=1;
            }
            else if ( (LA145_0==IF) ) {
                alt145=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 145, 0, input);

                throw nvae;
            }
            switch (alt145) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:858:7: gen_for
                    {
                    pushFollow(FOLLOW_gen_for_in_gen_iter4913);
                    gen_for();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:859:7: gen_if
                    {
                    pushFollow(FOLLOW_gen_if_in_gen_iter4921);
                    gen_if();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "gen_iter"


    // $ANTLR start "gen_for"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:863:1: gen_for : FOR exprlist IN or_test ( gen_iter )? ;
    public final void gen_for() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:864:5: ( FOR exprlist IN or_test ( gen_iter )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:864:7: FOR exprlist IN or_test ( gen_iter )?
            {
            match(input,FOR,FOLLOW_FOR_in_gen_for4939); if (state.failed) return ;
            pushFollow(FOLLOW_exprlist_in_gen_for4941);
            exprlist();

            state._fsp--;
            if (state.failed) return ;
            match(input,IN,FOLLOW_IN_in_gen_for4943); if (state.failed) return ;
            pushFollow(FOLLOW_or_test_in_gen_for4945);
            or_test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:864:31: ( gen_iter )?
            int alt146=2;
            int LA146_0 = input.LA(1);

            if ( (LA146_0==FOR||LA146_0==IF) ) {
                alt146=1;
            }
            switch (alt146) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:864:31: gen_iter
                    {
                    pushFollow(FOLLOW_gen_iter_in_gen_for4947);
                    gen_iter();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "gen_for"


    // $ANTLR start "gen_if"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:868:1: gen_if : IF test ( gen_iter )? ;
    public final void gen_if() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:869:5: ( IF test ( gen_iter )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:869:7: IF test ( gen_iter )?
            {
            match(input,IF,FOLLOW_IF_in_gen_if4966); if (state.failed) return ;
            pushFollow(FOLLOW_test_in_gen_if4968);
            test();

            state._fsp--;
            if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:869:15: ( gen_iter )?
            int alt147=2;
            int LA147_0 = input.LA(1);

            if ( (LA147_0==FOR||LA147_0==IF) ) {
                alt147=1;
            }
            switch (alt147) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:869:15: gen_iter
                    {
                    pushFollow(FOLLOW_gen_iter_in_gen_if4970);
                    gen_iter();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "gen_if"


    // $ANTLR start "yield_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:873:1: yield_expr : YIELD ( testlist )? ;
    public final void yield_expr() throws RecognitionException {
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:874:5: ( YIELD ( testlist )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:874:7: YIELD ( testlist )?
            {
            match(input,YIELD,FOLLOW_YIELD_in_yield_expr4989); if (state.failed) return ;
            // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:874:13: ( testlist )?
            int alt148=2;
            int LA148_0 = input.LA(1);

            if ( (LA148_0==TRAILBACKSLASH||LA148_0==NAME||(LA148_0>=LAMBDA && LA148_0<=NOT)||LA148_0==LPAREN||(LA148_0>=PLUS && LA148_0<=MINUS)||(LA148_0>=TILDE && LA148_0<=LBRACK)||LA148_0==LCURLY||(LA148_0>=BACKQUOTE && LA148_0<=STRING)||(LA148_0>=TRISTRINGPART && LA148_0<=STRINGPART)) ) {
                alt148=1;
            }
            switch (alt148) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:874:13: testlist
                    {
                    pushFollow(FOLLOW_testlist_in_yield_expr4991);
                    testlist();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }


            }

        }

        catch (RecognitionException e) {
            throw e;
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "yield_expr"

    // $ANTLR start synpred1_PythonPartial
    public final void synpred1_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:277:7: ( LPAREN fpdef COMMA )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:277:8: LPAREN fpdef COMMA
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred1_PythonPartial803); if (state.failed) return ;
        pushFollow(FOLLOW_fpdef_in_synpred1_PythonPartial805);
        fpdef();

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred1_PythonPartial807); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_PythonPartial

    // $ANTLR start synpred2_PythonPartial
    public final void synpred2_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:314:8: ( testlist augassign )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:314:9: testlist augassign
        {
        pushFollow(FOLLOW_testlist_in_synpred2_PythonPartial1103);
        testlist();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_augassign_in_synpred2_PythonPartial1105);
        augassign();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_PythonPartial

    // $ANTLR start synpred3_PythonPartial
    public final void synpred3_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:320:7: ( testlist ASSIGN )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:320:8: testlist ASSIGN
        {
        pushFollow(FOLLOW_testlist_in_synpred3_PythonPartial1183);
        testlist();

        state._fsp--;
        if (state.failed) return ;
        match(input,ASSIGN,FOLLOW_ASSIGN_in_synpred3_PythonPartial1185); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_PythonPartial

    // $ANTLR start synpred4_PythonPartial
    public final void synpred4_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:360:7: ( test COMMA )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:360:8: test COMMA
        {
        pushFollow(FOLLOW_test_in_synpred4_PythonPartial1468);
        test();

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred4_PythonPartial1470); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_PythonPartial

    // $ANTLR start synpred5_PythonPartial
    public final void synpred5_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:483:7: ( ( decorators )? DEF )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:483:8: ( decorators )? DEF
        {
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:483:8: ( decorators )?
        int alt149=2;
        int LA149_0 = input.LA(1);

        if ( (LA149_0==AT) ) {
            alt149=1;
        }
        switch (alt149) {
            case 1 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:483:8: decorators
                {
                pushFollow(FOLLOW_decorators_in_synpred5_PythonPartial2153);
                decorators();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        match(input,DEF,FOLLOW_DEF_in_synpred5_PythonPartial2156); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_PythonPartial

    // $ANTLR start synpred6_PythonPartial
    public final void synpred6_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:484:7: ( ( decorators )? CLASS )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:484:8: ( decorators )? CLASS
        {
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:484:8: ( decorators )?
        int alt150=2;
        int LA150_0 = input.LA(1);

        if ( (LA150_0==AT) ) {
            alt150=1;
        }
        switch (alt150) {
            case 1 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:484:8: decorators
                {
                pushFollow(FOLLOW_decorators_in_synpred6_PythonPartial2170);
                decorators();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        match(input,CLASS,FOLLOW_CLASS_in_synpred6_PythonPartial2173); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_PythonPartial

    // $ANTLR start synpred7_PythonPartial
    public final void synpred7_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:559:9: ( IF or_test ORELSE )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:559:10: IF or_test ORELSE
        {
        match(input,IF,FOLLOW_IF_in_synpred7_PythonPartial2734); if (state.failed) return ;
        pushFollow(FOLLOW_or_test_in_synpred7_PythonPartial2736);
        or_test();

        state._fsp--;
        if (state.failed) return ;
        match(input,ORELSE,FOLLOW_ORELSE_in_synpred7_PythonPartial2738); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_PythonPartial

    // $ANTLR start synpred8_PythonPartial
    public final void synpred8_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:773:7: ( test COLON )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:773:8: test COLON
        {
        pushFollow(FOLLOW_test_in_synpred8_PythonPartial4256);
        test();

        state._fsp--;
        if (state.failed) return ;
        match(input,COLON,FOLLOW_COLON_in_synpred8_PythonPartial4258); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_PythonPartial

    // $ANTLR start synpred9_PythonPartial
    public final void synpred9_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:775:7: ( COLON )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:775:8: COLON
        {
        match(input,COLON,FOLLOW_COLON_in_synpred9_PythonPartial4290); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_PythonPartial

    // $ANTLR start synpred10_PythonPartial
    public final void synpred10_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:790:7: ( expr COMMA )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:790:8: expr COMMA
        {
        pushFollow(FOLLOW_expr_in_synpred10_PythonPartial4375);
        expr();

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred10_PythonPartial4377); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_PythonPartial

    // $ANTLR start synpred11_PythonPartial
    public final void synpred11_PythonPartial_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:802:7: ( test COMMA )
        // /Users/zwei/Workspace/zippy/zippy/grammar/PythonPartial.g:802:8: test COMMA
        {
        pushFollow(FOLLOW_test_in_synpred11_PythonPartial4468);
        test();

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred11_PythonPartial4470); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_PythonPartial

    // Delegated rules

    public final boolean synpred9_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred10_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_PythonPartial() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_PythonPartial_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA27 dfa27 = new DFA27(this);
    protected DFA32 dfa32 = new DFA32(this);
    protected DFA36 dfa36 = new DFA36(this);
    protected DFA34 dfa34 = new DFA34(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA57 dfa57 = new DFA57(this);
    protected DFA74 dfa74 = new DFA74(this);
    protected DFA83 dfa83 = new DFA83(this);
    protected DFA107 dfa107 = new DFA107(this);
    protected DFA120 dfa120 = new DFA120(this);
    protected DFA124 dfa124 = new DFA124(this);
    protected DFA122 dfa122 = new DFA122(this);
    protected DFA125 dfa125 = new DFA125(this);
    protected DFA129 dfa129 = new DFA129(this);
    protected DFA127 dfa127 = new DFA127(this);
    protected DFA130 dfa130 = new DFA130(this);
    static final String DFA27_eotS =
        "\12\uffff";
    static final String DFA27_eofS =
        "\12\uffff";
    static final String DFA27_minS =
        "\1\6\11\uffff";
    static final String DFA27_maxS =
        "\1\144\11\uffff";
    static final String DFA27_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11";
    static final String DFA27_specialS =
        "\12\uffff}>";
    static final String[] DFA27_transitionS = {
            "\1\1\2\uffff\1\1\1\uffff\1\2\2\uffff\1\11\1\5\1\uffff\1\5\1"+
            "\uffff\1\3\2\uffff\1\10\1\uffff\1\6\1\uffff\1\7\1\uffff\1\6"+
            "\2\uffff\2\1\2\uffff\1\4\2\5\3\uffff\1\5\1\uffff\1\1\37\uffff"+
            "\2\1\3\uffff\2\1\1\uffff\1\1\1\uffff\6\1\10\uffff\2\1",
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
            return "300:1: small_stmt : ( expr_stmt | print_stmt | del_stmt | pass_stmt | flow_stmt | import_stmt | global_stmt | exec_stmt | assert_stmt );";
        }
    }
    static final String DFA32_eotS =
        "\26\uffff";
    static final String DFA32_eofS =
        "\26\uffff";
    static final String DFA32_minS =
        "\1\6\22\0\3\uffff";
    static final String DFA32_maxS =
        "\1\144\22\0\3\uffff";
    static final String DFA32_acceptS =
        "\23\uffff\1\1\1\2\1\3";
    static final String DFA32_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\3\uffff}>";
    static final String[] DFA32_transitionS = {
            "\1\21\2\uffff\1\11\25\uffff\1\22\1\1\12\uffff\1\5\37\uffff\1"+
            "\2\1\3\3\uffff\1\4\1\6\1\uffff\1\7\1\uffff\1\10\1\12\1\13\1"+
            "\14\1\15\1\16\10\uffff\1\17\1\20",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA32_eot = DFA.unpackEncodedString(DFA32_eotS);
    static final short[] DFA32_eof = DFA.unpackEncodedString(DFA32_eofS);
    static final char[] DFA32_min = DFA.unpackEncodedStringToUnsignedChars(DFA32_minS);
    static final char[] DFA32_max = DFA.unpackEncodedStringToUnsignedChars(DFA32_maxS);
    static final short[] DFA32_accept = DFA.unpackEncodedString(DFA32_acceptS);
    static final short[] DFA32_special = DFA.unpackEncodedString(DFA32_specialS);
    static final short[][] DFA32_transition;

    static {
        int numStates = DFA32_transitionS.length;
        DFA32_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA32_transition[i] = DFA.unpackEncodedString(DFA32_transitionS[i]);
        }
    }

    class DFA32 extends DFA {

        public DFA32(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 32;
            this.eot = DFA32_eot;
            this.eof = DFA32_eof;
            this.min = DFA32_min;
            this.max = DFA32_max;
            this.accept = DFA32_accept;
            this.special = DFA32_special;
            this.transition = DFA32_transition;
        }
        public String getDescription() {
            return "314:7: ( ( testlist augassign )=> testlist ( ( augassign yield_expr ) | ( augassign testlist ) ) | ( testlist ASSIGN )=> testlist ( | ( ( ASSIGN testlist )+ ) | ( ( ASSIGN yield_expr )+ ) ) | testlist )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA32_1 = input.LA(1);

                         
                        int index32_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA32_2 = input.LA(1);

                         
                        int index32_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA32_3 = input.LA(1);

                         
                        int index32_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA32_4 = input.LA(1);

                         
                        int index32_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA32_5 = input.LA(1);

                         
                        int index32_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA32_6 = input.LA(1);

                         
                        int index32_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA32_7 = input.LA(1);

                         
                        int index32_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA32_8 = input.LA(1);

                         
                        int index32_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA32_9 = input.LA(1);

                         
                        int index32_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA32_10 = input.LA(1);

                         
                        int index32_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA32_11 = input.LA(1);

                         
                        int index32_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA32_12 = input.LA(1);

                         
                        int index32_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA32_13 = input.LA(1);

                         
                        int index32_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA32_14 = input.LA(1);

                         
                        int index32_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA32_15 = input.LA(1);

                         
                        int index32_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA32_16 = input.LA(1);

                         
                        int index32_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA32_17 = input.LA(1);

                         
                        int index32_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA32_18 = input.LA(1);

                         
                        int index32_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_PythonPartial()) ) {s = 19;}

                        else if ( (synpred3_PythonPartial()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index32_18);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 32, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA36_eotS =
        "\25\uffff";
    static final String DFA36_eofS =
        "\25\uffff";
    static final String DFA36_minS =
        "\1\6\22\0\2\uffff";
    static final String DFA36_maxS =
        "\1\144\22\0\2\uffff";
    static final String DFA36_acceptS =
        "\23\uffff\1\1\1\2";
    static final String DFA36_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\2\uffff}>";
    static final String[] DFA36_transitionS = {
            "\1\21\2\uffff\1\11\25\uffff\1\22\1\1\12\uffff\1\5\37\uffff\1"+
            "\2\1\3\3\uffff\1\4\1\6\1\uffff\1\7\1\uffff\1\10\1\12\1\13\1"+
            "\14\1\15\1\16\10\uffff\1\17\1\20",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA36_eot = DFA.unpackEncodedString(DFA36_eotS);
    static final short[] DFA36_eof = DFA.unpackEncodedString(DFA36_eofS);
    static final char[] DFA36_min = DFA.unpackEncodedStringToUnsignedChars(DFA36_minS);
    static final char[] DFA36_max = DFA.unpackEncodedStringToUnsignedChars(DFA36_maxS);
    static final short[] DFA36_accept = DFA.unpackEncodedString(DFA36_acceptS);
    static final short[] DFA36_special = DFA.unpackEncodedString(DFA36_specialS);
    static final short[][] DFA36_transition;

    static {
        int numStates = DFA36_transitionS.length;
        DFA36_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA36_transition[i] = DFA.unpackEncodedString(DFA36_transitionS[i]);
        }
    }

    class DFA36 extends DFA {

        public DFA36(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 36;
            this.eot = DFA36_eot;
            this.eof = DFA36_eof;
            this.min = DFA36_min;
            this.max = DFA36_max;
            this.accept = DFA36_accept;
            this.special = DFA36_special;
            this.transition = DFA36_transition;
        }
        public String getDescription() {
            return "359:1: printlist : ( ( test COMMA )=> test ( options {k=2; } : COMMA test )* ( COMMA )? | test );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA36_1 = input.LA(1);

                         
                        int index36_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA36_2 = input.LA(1);

                         
                        int index36_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA36_3 = input.LA(1);

                         
                        int index36_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA36_4 = input.LA(1);

                         
                        int index36_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA36_5 = input.LA(1);

                         
                        int index36_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA36_6 = input.LA(1);

                         
                        int index36_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA36_7 = input.LA(1);

                         
                        int index36_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA36_8 = input.LA(1);

                         
                        int index36_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA36_9 = input.LA(1);

                         
                        int index36_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA36_10 = input.LA(1);

                         
                        int index36_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA36_11 = input.LA(1);

                         
                        int index36_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA36_12 = input.LA(1);

                         
                        int index36_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA36_13 = input.LA(1);

                         
                        int index36_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA36_14 = input.LA(1);

                         
                        int index36_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA36_15 = input.LA(1);

                         
                        int index36_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA36_16 = input.LA(1);

                         
                        int index36_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA36_17 = input.LA(1);

                         
                        int index36_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA36_18 = input.LA(1);

                         
                        int index36_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index36_18);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 36, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA34_eotS =
        "\30\uffff";
    static final String DFA34_eofS =
        "\2\2\26\uffff";
    static final String DFA34_minS =
        "\1\7\1\6\26\uffff";
    static final String DFA34_maxS =
        "\1\62\1\144\26\uffff";
    static final String DFA34_acceptS =
        "\2\uffff\1\2\1\uffff\1\1\23\uffff";
    static final String DFA34_specialS =
        "\30\uffff}>";
    static final String[] DFA34_transitionS = {
            "\1\2\47\uffff\1\1\2\uffff\1\2",
            "\1\4\1\2\1\uffff\1\4\25\uffff\2\4\12\uffff\1\4\6\uffff\1\2"+
            "\30\uffff\2\4\3\uffff\2\4\1\uffff\1\4\1\uffff\6\4\10\uffff\2"+
            "\4",
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

    static final short[] DFA34_eot = DFA.unpackEncodedString(DFA34_eotS);
    static final short[] DFA34_eof = DFA.unpackEncodedString(DFA34_eofS);
    static final char[] DFA34_min = DFA.unpackEncodedStringToUnsignedChars(DFA34_minS);
    static final char[] DFA34_max = DFA.unpackEncodedStringToUnsignedChars(DFA34_maxS);
    static final short[] DFA34_accept = DFA.unpackEncodedString(DFA34_acceptS);
    static final short[] DFA34_special = DFA.unpackEncodedString(DFA34_specialS);
    static final short[][] DFA34_transition;

    static {
        int numStates = DFA34_transitionS.length;
        DFA34_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA34_transition[i] = DFA.unpackEncodedString(DFA34_transitionS[i]);
        }
    }

    class DFA34 extends DFA {

        public DFA34(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 34;
            this.eot = DFA34_eot;
            this.eof = DFA34_eof;
            this.min = DFA34_min;
            this.max = DFA34_max;
            this.accept = DFA34_accept;
            this.special = DFA34_special;
            this.transition = DFA34_transition;
        }
        public String getDescription() {
            return "()* loopback of 361:13: ( options {k=2; } : COMMA test )*";
        }
    }
    static final String DFA45_eotS =
        "\4\uffff";
    static final String DFA45_eofS =
        "\4\uffff";
    static final String DFA45_minS =
        "\2\11\2\uffff";
    static final String DFA45_maxS =
        "\1\12\1\34\2\uffff";
    static final String DFA45_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA45_specialS =
        "\4\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\2\1\1",
            "\1\2\1\1\21\uffff\1\3",
            "",
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
            return "428:12: ( ( DOT )* dotted_name | ( DOT )+ )";
        }
    }
    static final String DFA57_eotS =
        "\12\uffff";
    static final String DFA57_eofS =
        "\12\uffff";
    static final String DFA57_minS =
        "\1\20\5\uffff\1\0\3\uffff";
    static final String DFA57_maxS =
        "\1\52\5\uffff\1\0\3\uffff";
    static final String DFA57_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\uffff\1\6\1\7\1\10";
    static final String DFA57_specialS =
        "\1\0\5\uffff\1\1\3\uffff}>";
    static final String[] DFA57_transitionS = {
            "\1\10\1\uffff\1\7\6\uffff\1\3\1\uffff\1\1\12\uffff\1\4\1\2\1"+
            "\5\1\uffff\1\6",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA57_eot = DFA.unpackEncodedString(DFA57_eotS);
    static final short[] DFA57_eof = DFA.unpackEncodedString(DFA57_eofS);
    static final char[] DFA57_min = DFA.unpackEncodedStringToUnsignedChars(DFA57_minS);
    static final char[] DFA57_max = DFA.unpackEncodedStringToUnsignedChars(DFA57_maxS);
    static final short[] DFA57_accept = DFA.unpackEncodedString(DFA57_acceptS);
    static final short[] DFA57_special = DFA.unpackEncodedString(DFA57_specialS);
    static final short[][] DFA57_transition;

    static {
        int numStates = DFA57_transitionS.length;
        DFA57_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA57_transition[i] = DFA.unpackEncodedString(DFA57_transitionS[i]);
        }
    }

    class DFA57 extends DFA {

        public DFA57(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 57;
            this.eot = DFA57_eot;
            this.eof = DFA57_eof;
            this.min = DFA57_min;
            this.max = DFA57_max;
            this.accept = DFA57_accept;
            this.special = DFA57_special;
            this.transition = DFA57_transition;
        }
        public String getDescription() {
            return "477:1: compound_stmt : ( if_stmt | while_stmt | for_stmt | try_stmt | with_stmt | ( ( decorators )? DEF )=> funcdef | ( ( decorators )? CLASS )=> classdef | decorators );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA57_0 = input.LA(1);

                         
                        int index57_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA57_0==IF) ) {s = 1;}

                        else if ( (LA57_0==WHILE) ) {s = 2;}

                        else if ( (LA57_0==FOR) ) {s = 3;}

                        else if ( (LA57_0==TRY) ) {s = 4;}

                        else if ( (LA57_0==WITH) ) {s = 5;}

                        else if ( (LA57_0==AT) ) {s = 6;}

                        else if ( (LA57_0==DEF) && (synpred5_PythonPartial())) {s = 7;}

                        else if ( (LA57_0==CLASS) && (synpred6_PythonPartial())) {s = 8;}

                         
                        input.seek(index57_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA57_6 = input.LA(1);

                         
                        int index57_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_PythonPartial()) ) {s = 7;}

                        else if ( (synpred6_PythonPartial()) ) {s = 8;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index57_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 57, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA74_eotS =
        "\20\uffff";
    static final String DFA74_eofS =
        "\1\2\17\uffff";
    static final String DFA74_minS =
        "\1\7\1\0\16\uffff";
    static final String DFA74_maxS =
        "\1\125\1\0\16\uffff";
    static final String DFA74_acceptS =
        "\2\uffff\1\2\14\uffff\1\1";
    static final String DFA74_specialS =
        "\1\uffff\1\0\16\uffff}>";
    static final String[] DFA74_transitionS = {
            "\1\2\1\uffff\1\2\3\uffff\1\2\13\uffff\1\2\1\uffff\1\1\20\uffff"+
            "\4\2\2\uffff\15\2\23\uffff\1\2\1\uffff\2\2",
            "\1\uffff",
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
            "",
            ""
    };

    static final short[] DFA74_eot = DFA.unpackEncodedString(DFA74_eotS);
    static final short[] DFA74_eof = DFA.unpackEncodedString(DFA74_eofS);
    static final char[] DFA74_min = DFA.unpackEncodedStringToUnsignedChars(DFA74_minS);
    static final char[] DFA74_max = DFA.unpackEncodedStringToUnsignedChars(DFA74_maxS);
    static final short[] DFA74_accept = DFA.unpackEncodedString(DFA74_acceptS);
    static final short[] DFA74_special = DFA.unpackEncodedString(DFA74_specialS);
    static final short[][] DFA74_transition;

    static {
        int numStates = DFA74_transitionS.length;
        DFA74_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA74_transition[i] = DFA.unpackEncodedString(DFA74_transitionS[i]);
        }
    }

    class DFA74 extends DFA {

        public DFA74(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 74;
            this.eot = DFA74_eot;
            this.eof = DFA74_eof;
            this.min = DFA74_min;
            this.max = DFA74_max;
            this.accept = DFA74_accept;
            this.special = DFA74_special;
            this.transition = DFA74_transition;
        }
        public String getDescription() {
            return "559:7: ( ( IF or_test ORELSE )=> IF or_test ORELSE test | )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA74_1 = input.LA(1);

                         
                        int index74_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_PythonPartial()) ) {s = 15;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index74_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 74, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA83_eotS =
        "\15\uffff";
    static final String DFA83_eofS =
        "\15\uffff";
    static final String DFA83_minS =
        "\1\35\11\uffff\1\6\2\uffff";
    static final String DFA83_maxS =
        "\1\106\11\uffff\1\144\2\uffff";
    static final String DFA83_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\uffff\1\13\1\12";
    static final String DFA83_specialS =
        "\15\uffff}>";
    static final String[] DFA83_transitionS = {
            "\1\10\1\12\1\uffff\1\11\37\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\14\2\uffff\1\14\26\uffff\1\13\12\uffff\1\14\37\uffff\2\14"+
            "\3\uffff\2\14\1\uffff\1\14\1\uffff\6\14\10\uffff\2\14",
            "",
            ""
    };

    static final short[] DFA83_eot = DFA.unpackEncodedString(DFA83_eotS);
    static final short[] DFA83_eof = DFA.unpackEncodedString(DFA83_eofS);
    static final char[] DFA83_min = DFA.unpackEncodedStringToUnsignedChars(DFA83_minS);
    static final char[] DFA83_max = DFA.unpackEncodedStringToUnsignedChars(DFA83_maxS);
    static final short[] DFA83_accept = DFA.unpackEncodedString(DFA83_acceptS);
    static final short[] DFA83_special = DFA.unpackEncodedString(DFA83_specialS);
    static final short[][] DFA83_transition;

    static {
        int numStates = DFA83_transitionS.length;
        DFA83_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA83_transition[i] = DFA.unpackEncodedString(DFA83_transitionS[i]);
        }
    }

    class DFA83 extends DFA {

        public DFA83(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 83;
            this.eot = DFA83_eot;
            this.eof = DFA83_eof;
            this.min = DFA83_min;
            this.max = DFA83_max;
            this.accept = DFA83_accept;
            this.special = DFA83_special;
            this.transition = DFA83_transition;
        }
        public String getDescription() {
            return "599:1: comp_op : ( LESS | GREATER | EQUAL | GREATEREQUAL | LESSEQUAL | ALT_NOTEQUAL | NOTEQUAL | IN | NOT IN | IS | IS NOT );";
        }
    }
    static final String DFA107_eotS =
        "\26\uffff";
    static final String DFA107_eofS =
        "\26\uffff";
    static final String DFA107_minS =
        "\1\54\1\6\24\uffff";
    static final String DFA107_maxS =
        "\1\57\1\144\24\uffff";
    static final String DFA107_acceptS =
        "\2\uffff\1\2\1\uffff\1\1\21\uffff";
    static final String DFA107_specialS =
        "\26\uffff}>";
    static final String[] DFA107_transitionS = {
            "\1\2\2\uffff\1\1",
            "\1\4\2\uffff\1\4\25\uffff\2\4\12\uffff\1\4\1\2\36\uffff\2\4"+
            "\3\uffff\2\4\1\uffff\1\4\1\uffff\6\4\10\uffff\2\4",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA107_eot = DFA.unpackEncodedString(DFA107_eotS);
    static final short[] DFA107_eof = DFA.unpackEncodedString(DFA107_eofS);
    static final char[] DFA107_min = DFA.unpackEncodedStringToUnsignedChars(DFA107_minS);
    static final char[] DFA107_max = DFA.unpackEncodedStringToUnsignedChars(DFA107_maxS);
    static final short[] DFA107_accept = DFA.unpackEncodedString(DFA107_acceptS);
    static final short[] DFA107_special = DFA.unpackEncodedString(DFA107_specialS);
    static final short[][] DFA107_transition;

    static {
        int numStates = DFA107_transitionS.length;
        DFA107_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA107_transition[i] = DFA.unpackEncodedString(DFA107_transitionS[i]);
        }
    }

    class DFA107 extends DFA {

        public DFA107(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 107;
            this.eot = DFA107_eot;
            this.eof = DFA107_eof;
            this.min = DFA107_min;
            this.max = DFA107_max;
            this.accept = DFA107_accept;
            this.special = DFA107_special;
            this.transition = DFA107_transition;
        }
        public String getDescription() {
            return "()* loopback of 742:12: ( options {k=2; } : COMMA test )*";
        }
    }
    static final String DFA120_eotS =
        "\27\uffff";
    static final String DFA120_eofS =
        "\27\uffff";
    static final String DFA120_minS =
        "\1\6\1\uffff\22\0\3\uffff";
    static final String DFA120_maxS =
        "\1\144\1\uffff\22\0\3\uffff";
    static final String DFA120_acceptS =
        "\1\uffff\1\1\22\uffff\1\3\1\2\1\4";
    static final String DFA120_specialS =
        "\1\0\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\3\uffff}>";
    static final String[] DFA120_transitionS = {
            "\1\22\2\uffff\1\12\1\1\24\uffff\1\23\1\2\12\uffff\1\6\1\uffff"+
            "\1\24\35\uffff\1\3\1\4\3\uffff\1\5\1\7\1\uffff\1\10\1\uffff"+
            "\1\11\1\13\1\14\1\15\1\16\1\17\10\uffff\1\20\1\21",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA120_eot = DFA.unpackEncodedString(DFA120_eotS);
    static final short[] DFA120_eof = DFA.unpackEncodedString(DFA120_eofS);
    static final char[] DFA120_min = DFA.unpackEncodedStringToUnsignedChars(DFA120_minS);
    static final char[] DFA120_max = DFA.unpackEncodedStringToUnsignedChars(DFA120_maxS);
    static final short[] DFA120_accept = DFA.unpackEncodedString(DFA120_acceptS);
    static final short[] DFA120_special = DFA.unpackEncodedString(DFA120_specialS);
    static final short[][] DFA120_transition;

    static {
        int numStates = DFA120_transitionS.length;
        DFA120_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA120_transition[i] = DFA.unpackEncodedString(DFA120_transitionS[i]);
        }
    }

    class DFA120 extends DFA {

        public DFA120(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 120;
            this.eot = DFA120_eot;
            this.eof = DFA120_eof;
            this.min = DFA120_min;
            this.max = DFA120_max;
            this.accept = DFA120_accept;
            this.special = DFA120_special;
            this.transition = DFA120_transition;
        }
        public String getDescription() {
            return "771:1: subscript : ( DOT DOT DOT | ( test COLON )=> test ( COLON ( test )? ( sliceop )? )? | ( COLON )=> COLON ( test )? ( sliceop )? | test );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA120_0 = input.LA(1);

                         
                        int index120_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA120_0==DOT) ) {s = 1;}

                        else if ( (LA120_0==NOT) ) {s = 2;}

                        else if ( (LA120_0==PLUS) ) {s = 3;}

                        else if ( (LA120_0==MINUS) ) {s = 4;}

                        else if ( (LA120_0==TILDE) ) {s = 5;}

                        else if ( (LA120_0==LPAREN) ) {s = 6;}

                        else if ( (LA120_0==LBRACK) ) {s = 7;}

                        else if ( (LA120_0==LCURLY) ) {s = 8;}

                        else if ( (LA120_0==BACKQUOTE) ) {s = 9;}

                        else if ( (LA120_0==NAME) ) {s = 10;}

                        else if ( (LA120_0==INT) ) {s = 11;}

                        else if ( (LA120_0==LONGINT) ) {s = 12;}

                        else if ( (LA120_0==FLOAT) ) {s = 13;}

                        else if ( (LA120_0==COMPLEX) ) {s = 14;}

                        else if ( (LA120_0==STRING) ) {s = 15;}

                        else if ( (LA120_0==TRISTRINGPART) ) {s = 16;}

                        else if ( (LA120_0==STRINGPART) ) {s = 17;}

                        else if ( (LA120_0==TRAILBACKSLASH) ) {s = 18;}

                        else if ( (LA120_0==LAMBDA) ) {s = 19;}

                        else if ( (LA120_0==COLON) && (synpred9_PythonPartial())) {s = 20;}

                         
                        input.seek(index120_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA120_2 = input.LA(1);

                         
                        int index120_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA120_3 = input.LA(1);

                         
                        int index120_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA120_4 = input.LA(1);

                         
                        int index120_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA120_5 = input.LA(1);

                         
                        int index120_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA120_6 = input.LA(1);

                         
                        int index120_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA120_7 = input.LA(1);

                         
                        int index120_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA120_8 = input.LA(1);

                         
                        int index120_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA120_9 = input.LA(1);

                         
                        int index120_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA120_10 = input.LA(1);

                         
                        int index120_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA120_11 = input.LA(1);

                         
                        int index120_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA120_12 = input.LA(1);

                         
                        int index120_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA120_13 = input.LA(1);

                         
                        int index120_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA120_14 = input.LA(1);

                         
                        int index120_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA120_15 = input.LA(1);

                         
                        int index120_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA120_16 = input.LA(1);

                         
                        int index120_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA120_17 = input.LA(1);

                         
                        int index120_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA120_18 = input.LA(1);

                         
                        int index120_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA120_19 = input.LA(1);

                         
                        int index120_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_PythonPartial()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index120_19);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 120, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA124_eotS =
        "\23\uffff";
    static final String DFA124_eofS =
        "\23\uffff";
    static final String DFA124_minS =
        "\1\6\20\0\2\uffff";
    static final String DFA124_maxS =
        "\1\144\20\0\2\uffff";
    static final String DFA124_acceptS =
        "\21\uffff\1\1\1\2";
    static final String DFA124_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\2\uffff}>";
    static final String[] DFA124_transitionS = {
            "\1\20\2\uffff\1\10\41\uffff\1\4\37\uffff\1\1\1\2\3\uffff\1\3"+
            "\1\5\1\uffff\1\6\1\uffff\1\7\1\11\1\12\1\13\1\14\1\15\10\uffff"+
            "\1\16\1\17",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA124_eot = DFA.unpackEncodedString(DFA124_eotS);
    static final short[] DFA124_eof = DFA.unpackEncodedString(DFA124_eofS);
    static final char[] DFA124_min = DFA.unpackEncodedStringToUnsignedChars(DFA124_minS);
    static final char[] DFA124_max = DFA.unpackEncodedStringToUnsignedChars(DFA124_maxS);
    static final short[] DFA124_accept = DFA.unpackEncodedString(DFA124_acceptS);
    static final short[] DFA124_special = DFA.unpackEncodedString(DFA124_specialS);
    static final short[][] DFA124_transition;

    static {
        int numStates = DFA124_transitionS.length;
        DFA124_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA124_transition[i] = DFA.unpackEncodedString(DFA124_transitionS[i]);
        }
    }

    class DFA124 extends DFA {

        public DFA124(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 124;
            this.eot = DFA124_eot;
            this.eof = DFA124_eof;
            this.min = DFA124_min;
            this.max = DFA124_max;
            this.accept = DFA124_accept;
            this.special = DFA124_special;
            this.transition = DFA124_transition;
        }
        public String getDescription() {
            return "789:1: exprlist : ( ( expr COMMA )=> expr ( options {k=2; } : COMMA expr )* ( COMMA )? | expr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA124_1 = input.LA(1);

                         
                        int index124_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA124_2 = input.LA(1);

                         
                        int index124_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA124_3 = input.LA(1);

                         
                        int index124_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA124_4 = input.LA(1);

                         
                        int index124_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA124_5 = input.LA(1);

                         
                        int index124_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA124_6 = input.LA(1);

                         
                        int index124_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA124_7 = input.LA(1);

                         
                        int index124_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA124_8 = input.LA(1);

                         
                        int index124_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA124_9 = input.LA(1);

                         
                        int index124_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA124_10 = input.LA(1);

                         
                        int index124_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA124_11 = input.LA(1);

                         
                        int index124_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA124_12 = input.LA(1);

                         
                        int index124_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA124_13 = input.LA(1);

                         
                        int index124_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA124_14 = input.LA(1);

                         
                        int index124_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA124_15 = input.LA(1);

                         
                        int index124_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA124_16 = input.LA(1);

                         
                        int index124_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_PythonPartial()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index124_16);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 124, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA122_eotS =
        "\30\uffff";
    static final String DFA122_eofS =
        "\2\2\26\uffff";
    static final String DFA122_minS =
        "\1\7\1\6\26\uffff";
    static final String DFA122_maxS =
        "\1\62\1\144\26\uffff";
    static final String DFA122_acceptS =
        "\2\uffff\1\2\2\uffff\1\1\22\uffff";
    static final String DFA122_specialS =
        "\30\uffff}>";
    static final String[] DFA122_transitionS = {
            "\1\2\25\uffff\1\2\21\uffff\1\1\2\uffff\1\2",
            "\1\5\1\2\1\uffff\1\5\23\uffff\1\2\15\uffff\1\5\6\uffff\1\2"+
            "\30\uffff\2\5\3\uffff\2\5\1\uffff\1\5\1\uffff\6\5\10\uffff\2"+
            "\5",
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

    static final short[] DFA122_eot = DFA.unpackEncodedString(DFA122_eotS);
    static final short[] DFA122_eof = DFA.unpackEncodedString(DFA122_eofS);
    static final char[] DFA122_min = DFA.unpackEncodedStringToUnsignedChars(DFA122_minS);
    static final char[] DFA122_max = DFA.unpackEncodedStringToUnsignedChars(DFA122_maxS);
    static final short[] DFA122_accept = DFA.unpackEncodedString(DFA122_acceptS);
    static final short[] DFA122_special = DFA.unpackEncodedString(DFA122_specialS);
    static final short[][] DFA122_transition;

    static {
        int numStates = DFA122_transitionS.length;
        DFA122_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA122_transition[i] = DFA.unpackEncodedString(DFA122_transitionS[i]);
        }
    }

    class DFA122 extends DFA {

        public DFA122(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 122;
            this.eot = DFA122_eot;
            this.eof = DFA122_eof;
            this.min = DFA122_min;
            this.max = DFA122_max;
            this.accept = DFA122_accept;
            this.special = DFA122_special;
            this.transition = DFA122_transition;
        }
        public String getDescription() {
            return "()* loopback of 790:28: ( options {k=2; } : COMMA expr )*";
        }
    }
    static final String DFA125_eotS =
        "\24\uffff";
    static final String DFA125_eofS =
        "\2\2\22\uffff";
    static final String DFA125_minS =
        "\1\57\1\6\22\uffff";
    static final String DFA125_maxS =
        "\1\57\1\144\22\uffff";
    static final String DFA125_acceptS =
        "\2\uffff\1\2\1\uffff\1\1\17\uffff";
    static final String DFA125_specialS =
        "\24\uffff}>";
    static final String[] DFA125_transitionS = {
            "\1\1",
            "\1\4\2\uffff\1\4\41\uffff\1\4\37\uffff\2\4\3\uffff\2\4\1\uffff"+
            "\1\4\1\uffff\6\4\10\uffff\2\4",
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
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA125_eot = DFA.unpackEncodedString(DFA125_eotS);
    static final short[] DFA125_eof = DFA.unpackEncodedString(DFA125_eofS);
    static final char[] DFA125_min = DFA.unpackEncodedStringToUnsignedChars(DFA125_minS);
    static final char[] DFA125_max = DFA.unpackEncodedStringToUnsignedChars(DFA125_maxS);
    static final short[] DFA125_accept = DFA.unpackEncodedString(DFA125_acceptS);
    static final short[] DFA125_special = DFA.unpackEncodedString(DFA125_specialS);
    static final short[][] DFA125_transition;

    static {
        int numStates = DFA125_transitionS.length;
        DFA125_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA125_transition[i] = DFA.unpackEncodedString(DFA125_transitionS[i]);
        }
    }

    class DFA125 extends DFA {

        public DFA125(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 125;
            this.eot = DFA125_eot;
            this.eof = DFA125_eof;
            this.min = DFA125_min;
            this.max = DFA125_max;
            this.accept = DFA125_accept;
            this.special = DFA125_special;
            this.transition = DFA125_transition;
        }
        public String getDescription() {
            return "()* loopback of 797:12: ( options {k=2; } : COMMA expr )*";
        }
    }
    static final String DFA129_eotS =
        "\25\uffff";
    static final String DFA129_eofS =
        "\25\uffff";
    static final String DFA129_minS =
        "\1\6\22\0\2\uffff";
    static final String DFA129_maxS =
        "\1\144\22\0\2\uffff";
    static final String DFA129_acceptS =
        "\23\uffff\1\1\1\2";
    static final String DFA129_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\2\uffff}>";
    static final String[] DFA129_transitionS = {
            "\1\21\2\uffff\1\11\25\uffff\1\22\1\1\12\uffff\1\5\37\uffff\1"+
            "\2\1\3\3\uffff\1\4\1\6\1\uffff\1\7\1\uffff\1\10\1\12\1\13\1"+
            "\14\1\15\1\16\10\uffff\1\17\1\20",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA129_eot = DFA.unpackEncodedString(DFA129_eotS);
    static final short[] DFA129_eof = DFA.unpackEncodedString(DFA129_eofS);
    static final char[] DFA129_min = DFA.unpackEncodedStringToUnsignedChars(DFA129_minS);
    static final char[] DFA129_max = DFA.unpackEncodedStringToUnsignedChars(DFA129_maxS);
    static final short[] DFA129_accept = DFA.unpackEncodedString(DFA129_acceptS);
    static final short[] DFA129_special = DFA.unpackEncodedString(DFA129_specialS);
    static final short[][] DFA129_transition;

    static {
        int numStates = DFA129_transitionS.length;
        DFA129_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA129_transition[i] = DFA.unpackEncodedString(DFA129_transitionS[i]);
        }
    }

    class DFA129 extends DFA {

        public DFA129(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 129;
            this.eot = DFA129_eot;
            this.eof = DFA129_eof;
            this.min = DFA129_min;
            this.max = DFA129_max;
            this.accept = DFA129_accept;
            this.special = DFA129_special;
            this.transition = DFA129_transition;
        }
        public String getDescription() {
            return "801:1: testlist : ( ( test COMMA )=> test ( options {k=2; } : COMMA test )* ( COMMA )? | test );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA129_1 = input.LA(1);

                         
                        int index129_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA129_2 = input.LA(1);

                         
                        int index129_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA129_3 = input.LA(1);

                         
                        int index129_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA129_4 = input.LA(1);

                         
                        int index129_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA129_5 = input.LA(1);

                         
                        int index129_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA129_6 = input.LA(1);

                         
                        int index129_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA129_7 = input.LA(1);

                         
                        int index129_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA129_8 = input.LA(1);

                         
                        int index129_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA129_9 = input.LA(1);

                         
                        int index129_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA129_10 = input.LA(1);

                         
                        int index129_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA129_11 = input.LA(1);

                         
                        int index129_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA129_12 = input.LA(1);

                         
                        int index129_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA129_13 = input.LA(1);

                         
                        int index129_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA129_14 = input.LA(1);

                         
                        int index129_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA129_15 = input.LA(1);

                         
                        int index129_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA129_16 = input.LA(1);

                         
                        int index129_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA129_17 = input.LA(1);

                         
                        int index129_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA129_18 = input.LA(1);

                         
                        int index129_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_PythonPartial()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index129_18);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 129, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA127_eotS =
        "\52\uffff";
    static final String DFA127_eofS =
        "\2\2\50\uffff";
    static final String DFA127_minS =
        "\1\7\1\6\50\uffff";
    static final String DFA127_maxS =
        "\1\125\1\144\50\uffff";
    static final String DFA127_acceptS =
        "\2\uffff\1\2\12\uffff\1\1\6\uffff\1\1\25\uffff";
    static final String DFA127_specialS =
        "\52\uffff}>";
    static final String[] DFA127_transitionS = {
            "\1\2\21\uffff\1\2\1\uffff\1\2\20\uffff\3\2\1\1\2\uffff\15\2"+
            "\23\uffff\1\2\2\uffff\1\2",
            "\1\15\1\2\1\uffff\1\15\17\uffff\1\2\1\uffff\1\2\3\uffff\2\15"+
            "\12\uffff\1\15\4\2\2\uffff\15\2\14\uffff\2\15\3\uffff\2\15\1"+
            "\2\1\15\1\uffff\1\24\5\15\10\uffff\2\15",
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
            "",
            "",
            "",
            ""
    };

    static final short[] DFA127_eot = DFA.unpackEncodedString(DFA127_eotS);
    static final short[] DFA127_eof = DFA.unpackEncodedString(DFA127_eofS);
    static final char[] DFA127_min = DFA.unpackEncodedStringToUnsignedChars(DFA127_minS);
    static final char[] DFA127_max = DFA.unpackEncodedStringToUnsignedChars(DFA127_maxS);
    static final short[] DFA127_accept = DFA.unpackEncodedString(DFA127_acceptS);
    static final short[] DFA127_special = DFA.unpackEncodedString(DFA127_specialS);
    static final short[][] DFA127_transition;

    static {
        int numStates = DFA127_transitionS.length;
        DFA127_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA127_transition[i] = DFA.unpackEncodedString(DFA127_transitionS[i]);
        }
    }

    class DFA127 extends DFA {

        public DFA127(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 127;
            this.eot = DFA127_eot;
            this.eof = DFA127_eof;
            this.min = DFA127_min;
            this.max = DFA127_max;
            this.accept = DFA127_accept;
            this.special = DFA127_special;
            this.transition = DFA127_transition;
        }
        public String getDescription() {
            return "()* loopback of 803:12: ( options {k=2; } : COMMA test )*";
        }
    }
    static final String DFA130_eotS =
        "\26\uffff";
    static final String DFA130_eofS =
        "\26\uffff";
    static final String DFA130_minS =
        "\1\57\1\6\24\uffff";
    static final String DFA130_maxS =
        "\1\124\1\144\24\uffff";
    static final String DFA130_acceptS =
        "\2\uffff\1\2\1\1\22\uffff";
    static final String DFA130_specialS =
        "\26\uffff}>";
    static final String[] DFA130_transitionS = {
            "\1\1\44\uffff\1\2",
            "\1\3\2\uffff\1\3\25\uffff\2\3\12\uffff\1\3\37\uffff\2\3\3\uffff"+
            "\2\3\1\uffff\1\3\1\2\6\3\10\uffff\2\3",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA130_eot = DFA.unpackEncodedString(DFA130_eotS);
    static final short[] DFA130_eof = DFA.unpackEncodedString(DFA130_eofS);
    static final char[] DFA130_min = DFA.unpackEncodedStringToUnsignedChars(DFA130_minS);
    static final char[] DFA130_max = DFA.unpackEncodedStringToUnsignedChars(DFA130_maxS);
    static final short[] DFA130_accept = DFA.unpackEncodedString(DFA130_acceptS);
    static final short[] DFA130_special = DFA.unpackEncodedString(DFA130_specialS);
    static final short[][] DFA130_transition;

    static {
        int numStates = DFA130_transitionS.length;
        DFA130_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA130_transition[i] = DFA.unpackEncodedString(DFA130_transitionS[i]);
        }
    }

    class DFA130 extends DFA {

        public DFA130(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 130;
            this.eot = DFA130_eot;
            this.eof = DFA130_eof;
            this.min = DFA130_min;
            this.max = DFA130_max;
            this.accept = DFA130_accept;
            this.special = DFA130_special;
            this.transition = DFA130_transition;
        }
        public String getDescription() {
            return "()* loopback of 810:9: ( options {k=2; } : COMMA test COLON test )*";
        }
    }
 

    public static final BitSet FOLLOW_NEWLINE_in_single_input72 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_stmt_in_single_input80 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compound_stmt_in_single_input88 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_NEWLINE_in_single_input90 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEADING_WS_in_eval_input109 = new BitSet(new long[]{0x00000801800002C0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_NEWLINE_in_eval_input113 = new BitSet(new long[]{0x00000801800002C0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_eval_input117 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_eval_input121 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_EOF_in_eval_input125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_dotted_attr143 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_DOT_in_dotted_attr154 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_dotted_attr156 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_set_in_attr0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_decorator460 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_dotted_attr_in_decorator462 = new BitSet(new long[]{0x0000080000000080L});
    public static final BitSet FOLLOW_LPAREN_in_decorator470 = new BitSet(new long[]{0x0003180180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_arglist_in_decorator480 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_decorator504 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_decorator518 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_decorator_in_decorators536 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_decorators_in_funcdef555 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_DEF_in_funcdef558 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_funcdef560 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_parameters_in_funcdef562 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_funcdef564 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_funcdef566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_parameters584 = new BitSet(new long[]{0x0003180000000200L});
    public static final BitSet FOLLOW_varargslist_in_parameters593 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_parameters617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fpdef_in_defparameter635 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_defparameter638 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_defparameter640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defparameter_in_varargslist662 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_varargslist672 = new BitSet(new long[]{0x0000080000000200L});
    public static final BitSet FOLLOW_defparameter_in_varargslist674 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_varargslist685 = new BitSet(new long[]{0x0003000000000002L});
    public static final BitSet FOLLOW_STAR_in_varargslist698 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist700 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_varargslist703 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_varargslist705 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_varargslist723 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_varargslist755 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist757 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_varargslist760 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_varargslist762 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_varargslist774 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_fpdef794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_fpdef812 = new BitSet(new long[]{0x0000080000000200L});
    public static final BitSet FOLLOW_fplist_in_fpdef814 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_fpdef816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_fpdef824 = new BitSet(new long[]{0x0000080000000200L});
    public static final BitSet FOLLOW_fplist_in_fpdef826 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_fpdef828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fpdef_in_fplist846 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_fplist862 = new BitSet(new long[]{0x0000080000000200L});
    public static final BitSet FOLLOW_fpdef_in_fplist864 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_fplist869 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_stmt_in_stmt889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compound_stmt_in_stmt897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_small_stmt_in_simple_stmt915 = new BitSet(new long[]{0x0004000000000080L});
    public static final BitSet FOLLOW_SEMI_in_simple_stmt925 = new BitSet(new long[]{0x00000A39954ACA40L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_small_stmt_in_simple_stmt927 = new BitSet(new long[]{0x0004000000000080L});
    public static final BitSet FOLLOW_SEMI_in_simple_stmt932 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_set_in_simple_stmt936 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_stmt_in_small_stmt955 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_print_stmt_in_small_stmt970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_del_stmt_in_small_stmt985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pass_stmt_in_small_stmt1000 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_flow_stmt_in_small_stmt1015 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_stmt_in_small_stmt1030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_global_stmt_in_small_stmt1045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exec_stmt_in_small_stmt1060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assert_stmt_in_small_stmt1075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1110 = new BitSet(new long[]{0x7FF8000000000000L});
    public static final BitSet FOLLOW_augassign_in_expr_stmt1123 = new BitSet(new long[]{0x0000023000028000L});
    public static final BitSet FOLLOW_yield_expr_in_expr_stmt1125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_augassign_in_expr_stmt1150 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1190 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_expr_stmt1214 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1216 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_expr_stmt1244 = new BitSet(new long[]{0x0000023000028000L});
    public static final BitSet FOLLOW_yield_expr_in_expr_stmt1246 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1278 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_augassign0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRINT_in_print_stmt1410 = new BitSet(new long[]{0x8000080180000242L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_printlist_in_print_stmt1419 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHTSHIFT_in_print_stmt1429 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_printlist_in_print_stmt1431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_printlist1482 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_printlist1493 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_printlist1495 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_printlist1509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_printlist1519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DELETE_in_del_stmt1537 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_exprlist_in_del_stmt1539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PASS_in_pass_stmt1557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_break_stmt_in_flow_stmt1575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_continue_stmt_in_flow_stmt1583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_return_stmt_in_flow_stmt1591 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_raise_stmt_in_flow_stmt1599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_yield_stmt_in_flow_stmt1607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_break_stmt1625 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTINUE_in_continue_stmt1643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_return_stmt1661 = new BitSet(new long[]{0x0000080180000242L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_return_stmt1670 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_yield_expr_in_yield_stmt1704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RAISE_in_raise_stmt1722 = new BitSet(new long[]{0x0000080180000242L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_raise_stmt1725 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_raise_stmt1728 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_raise_stmt1730 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_raise_stmt1741 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_raise_stmt1743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_name_in_import_stmt1767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_from_in_import_stmt1775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_in_import_name1793 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_dotted_as_names_in_import_name1795 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_import_from1814 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_DOT_in_import_from1817 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_dotted_name_in_import_from1820 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_DOT_in_import_from1824 = new BitSet(new long[]{0x0000000010000400L});
    public static final BitSet FOLLOW_IMPORT_in_import_from1828 = new BitSet(new long[]{0x0001080000000200L});
    public static final BitSet FOLLOW_STAR_in_import_from1839 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_as_names_in_import_from1851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_import_from1863 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_import_as_names_in_import_from1865 = new BitSet(new long[]{0x0000900000000000L});
    public static final BitSet FOLLOW_COMMA_in_import_from1867 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_import_from1870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_as_name_in_import_as_names1898 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_import_as_names1901 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_import_as_name_in_import_as_names1903 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_NAME_in_import_as_name1923 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_AS_in_import_as_name1926 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_import_as_name1928 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dotted_name_in_dotted_as_name1949 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_AS_in_dotted_as_name1952 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_dotted_as_name1954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dotted_as_name_in_dotted_as_names1974 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_dotted_as_names1977 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_dotted_as_name_in_dotted_as_names1979 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_NAME_in_dotted_name1999 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_DOT_in_dotted_name2002 = new BitSet(new long[]{0x000003FFFFFFFA00L});
    public static final BitSet FOLLOW_attr_in_dotted_name2004 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_GLOBAL_in_global_stmt2024 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_global_stmt2026 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_global_stmt2029 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_global_stmt2031 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_EXEC_in_exec_stmt2051 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_expr_in_exec_stmt2053 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_IN_in_exec_stmt2056 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_exec_stmt2058 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_exec_stmt2061 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_exec_stmt2063 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSERT_in_assert_stmt2085 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_assert_stmt2087 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_assert_stmt2090 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_assert_stmt2092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_if_stmt_in_compound_stmt2112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_while_stmt_in_compound_stmt2120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_for_stmt_in_compound_stmt2128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_try_stmt_in_compound_stmt2136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_with_stmt_in_compound_stmt2144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcdef_in_compound_stmt2161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classdef_in_compound_stmt2178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_decorators_in_compound_stmt2186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_if_stmt2204 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_if_stmt2206 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_if_stmt2208 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_if_stmt2210 = new BitSet(new long[]{0x0000000400100002L});
    public static final BitSet FOLLOW_elif_clause_in_if_stmt2212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_else_clause_in_elif_clause2231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELIF_in_elif_clause2239 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_elif_clause2241 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_elif_clause2243 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_elif_clause2245 = new BitSet(new long[]{0x0000000400100002L});
    public static final BitSet FOLLOW_elif_clause_in_elif_clause2256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORELSE_in_else_clause2294 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_else_clause2296 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_else_clause2298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_while_stmt2316 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_while_stmt2318 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_while_stmt2320 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_while_stmt2322 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_ORELSE_in_while_stmt2325 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_while_stmt2327 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_while_stmt2329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_for_stmt2349 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_exprlist_in_for_stmt2351 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_IN_in_for_stmt2353 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_for_stmt2355 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_for_stmt2357 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_for_stmt2359 = new BitSet(new long[]{0x0000000400000002L});
    public static final BitSet FOLLOW_ORELSE_in_for_stmt2370 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_for_stmt2372 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_for_stmt2374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRY_in_try_stmt2398 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_try_stmt2400 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_try_stmt2402 = new BitSet(new long[]{0x0000000000A00002L});
    public static final BitSet FOLLOW_except_clause_in_try_stmt2412 = new BitSet(new long[]{0x0000000400A00002L});
    public static final BitSet FOLLOW_ORELSE_in_try_stmt2416 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_try_stmt2418 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_try_stmt2420 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_FINALLY_in_try_stmt2425 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_try_stmt2427 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_try_stmt2429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FINALLY_in_try_stmt2441 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_try_stmt2443 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_try_stmt2445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WITH_in_with_stmt2474 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_with_stmt2476 = new BitSet(new long[]{0x0000200000002200L});
    public static final BitSet FOLLOW_with_var_in_with_stmt2479 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_with_stmt2483 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_with_stmt2485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_with_var2503 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_expr_in_with_var2511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXCEPT_in_except_clause2529 = new BitSet(new long[]{0x0000280180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_except_clause2532 = new BitSet(new long[]{0x0000A00000000000L});
    public static final BitSet FOLLOW_COMMA_in_except_clause2535 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_except_clause2537 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_except_clause2543 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_except_clause2545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_stmt_in_suite2563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEWLINE_in_suite2571 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_EOF_in_suite2574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEDENT_in_suite2593 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_EOF_in_suite2597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INDENT_in_suite2615 = new BitSet(new long[]{0x00000FF99F4FCA40L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_stmt_in_suite2618 = new BitSet(new long[]{0x00000FF99F4FCA60L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_set_in_suite2622 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_test_in_test2723 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_IF_in_test2743 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_or_test_in_test2745 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_ORELSE_in_test2747 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_test2749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lambdef_in_test2773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_test_in_or_test2791 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_OR_in_or_test2804 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_and_test_in_or_test2806 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_not_test_in_and_test2857 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_AND_in_and_test2870 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_not_test_in_and_test2872 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_NOT_in_not_test2923 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_not_test_in_not_test2925 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_in_not_test2933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_comparison2951 = new BitSet(new long[]{0x0000000160000002L,0x000000000000007FL});
    public static final BitSet FOLLOW_comp_op_in_comparison2964 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_expr_in_comparison2966 = new BitSet(new long[]{0x0000000160000002L,0x000000000000007FL});
    public static final BitSet FOLLOW_LESS_in_comp_op3014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp_op3022 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUAL_in_comp_op3030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATEREQUAL_in_comp_op3038 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESSEQUAL_in_comp_op3046 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALT_NOTEQUAL_in_comp_op3054 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTEQUAL_in_comp_op3062 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_comp_op3070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_comp_op3078 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_IN_in_comp_op3080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_comp_op3088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_comp_op3096 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NOT_in_comp_op3098 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xor_expr_in_expr3116 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_VBAR_in_expr3129 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_xor_expr_in_expr3131 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_and_expr_in_xor_expr3182 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_CIRCUMFLEX_in_xor_expr3195 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_and_expr_in_xor_expr3197 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_shift_expr_in_and_expr3248 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_AMPER_in_and_expr3261 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_shift_expr_in_and_expr3263 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_arith_expr_in_shift_expr3314 = new BitSet(new long[]{0x8000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_shift_op_in_shift_expr3328 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_arith_expr_in_shift_expr3330 = new BitSet(new long[]{0x8000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_set_in_shift_op0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_arith_expr3406 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001800L});
    public static final BitSet FOLLOW_arith_op_in_arith_expr3419 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_term_in_arith_expr3421 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001800L});
    public static final BitSet FOLLOW_set_in_arith_op0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_factor_in_term3497 = new BitSet(new long[]{0x0001000000000002L,0x000000000000E000L});
    public static final BitSet FOLLOW_term_op_in_term3510 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_factor_in_term3512 = new BitSet(new long[]{0x0001000000000002L,0x000000000000E000L});
    public static final BitSet FOLLOW_set_in_term_op0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_factor3600 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_factor_in_factor3602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_factor3610 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_factor_in_factor3612 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_factor3620 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_factor_in_factor3622 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_power_in_factor3630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRAILBACKSLASH_in_factor3638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_power3656 = new BitSet(new long[]{0x0002080000000402L,0x0000000000020000L});
    public static final BitSet FOLLOW_trailer_in_power3659 = new BitSet(new long[]{0x0002080000000402L,0x0000000000020000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_power3671 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_factor_in_power3673 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom3697 = new BitSet(new long[]{0x00001A3180028240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_yield_expr_in_atom3707 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_testlist_gexp_in_atom3717 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_atom3741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACK_in_atom3749 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EF1800L});
    public static final BitSet FOLLOW_listmaker_in_atom3758 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACK_in_atom3782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LCURLY_in_atom3790 = new BitSet(new long[]{0x0000080180000240L,0x0000001807FB1800L});
    public static final BitSet FOLLOW_dictmaker_in_atom3800 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_RCURLY_in_atom3827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BACKQUOTE_in_atom3836 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_atom3838 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_BACKQUOTE_in_atom3840 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_atom3849 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_atom3858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LONGINT_in_atom3867 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_atom3876 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMPLEX_in_atom3885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_atom3895 = new BitSet(new long[]{0x0000000000000002L,0x0000000004000000L});
    public static final BitSet FOLLOW_TRISTRINGPART_in_atom3906 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRINGPART_in_atom3915 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_TRAILBACKSLASH_in_atom3917 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_listmaker3936 = new BitSet(new long[]{0x0000800002000002L});
    public static final BitSet FOLLOW_list_for_in_listmaker3947 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_listmaker3967 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_listmaker3969 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_listmaker3984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_testlist_gexp4004 = new BitSet(new long[]{0x0000800002000002L});
    public static final BitSet FOLLOW_COMMA_in_testlist_gexp4026 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_testlist_gexp4028 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_testlist_gexp4033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gen_for_in_testlist_gexp4060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LAMBDA_in_lambdef4100 = new BitSet(new long[]{0x0003280000000200L});
    public static final BitSet FOLLOW_varargslist_in_lambdef4103 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_lambdef4107 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_lambdef4109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_trailer4127 = new BitSet(new long[]{0x0003180180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_arglist_in_trailer4138 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_trailer4166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACK_in_trailer4174 = new BitSet(new long[]{0x0000280180000640L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_subscriptlist_in_trailer4176 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACK_in_trailer4178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_trailer4186 = new BitSet(new long[]{0x000003FFFFFFFA00L});
    public static final BitSet FOLLOW_attr_in_trailer4188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subscript_in_subscriptlist4206 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_subscriptlist4216 = new BitSet(new long[]{0x0000280180000640L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_subscript_in_subscriptlist4218 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_subscriptlist4223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_subscript4243 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_subscript4245 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_subscript4247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_subscript4266 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_COLON_in_subscript4269 = new BitSet(new long[]{0x0000280180000242L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_subscript4272 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_sliceop_in_subscript4277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_subscript4298 = new BitSet(new long[]{0x0000280180000242L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_subscript4301 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_sliceop_in_subscript4306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_subscript4316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_sliceop4334 = new BitSet(new long[]{0x0000080180000242L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_sliceop4342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_exprlist4382 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_exprlist4393 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_expr_in_exprlist4395 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_exprlist4400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_exprlist4410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_del_list4429 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_del_list4440 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_expr_in_del_list4442 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_del_list4447 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_testlist4478 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_testlist4489 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_testlist4491 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_testlist4496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_testlist4506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_dictmaker4524 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dictmaker4526 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_dictmaker4528 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_dictmaker4546 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_dictmaker4548 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dictmaker4550 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_dictmaker4552 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_dictmaker4565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_decorators_in_classdef4585 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CLASS_in_classdef4588 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_classdef4590 = new BitSet(new long[]{0x0000280000000000L});
    public static final BitSet FOLLOW_LPAREN_in_classdef4593 = new BitSet(new long[]{0x0000180180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_classdef4595 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_RPAREN_in_classdef4598 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_classdef4602 = new BitSet(new long[]{0x00000A39954ACAC0L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_suite_in_classdef4604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argument_in_arglist4622 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist4625 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_argument_in_arglist4627 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist4642 = new BitSet(new long[]{0x0003000000000002L});
    public static final BitSet FOLLOW_STAR_in_arglist4660 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_arglist4662 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist4665 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_arglist4667 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_arglist4669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_arglist4689 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_arglist4691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_arglist4729 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_arglist4731 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist4734 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_arglist4736 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_arglist4738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_arglist4748 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_arglist4750 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_argument4768 = new BitSet(new long[]{0x0000C00002000000L});
    public static final BitSet FOLLOW_ASSIGN_in_argument4780 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_argument4782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gen_for_in_argument4795 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_list_for_in_list_iter4833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_list_if_in_list_iter4841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_list_for4859 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_exprlist_in_list_for4861 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_IN_in_list_for4863 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_list_for4865 = new BitSet(new long[]{0x000000000A000002L});
    public static final BitSet FOLLOW_list_iter_in_list_for4868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_list_if4888 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_list_if4890 = new BitSet(new long[]{0x000000000A000002L});
    public static final BitSet FOLLOW_list_iter_in_list_if4893 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gen_for_in_gen_iter4913 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gen_if_in_gen_iter4921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_gen_for4939 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_exprlist_in_gen_for4941 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_IN_in_gen_for4943 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_or_test_in_gen_for4945 = new BitSet(new long[]{0x000080000A000002L});
    public static final BitSet FOLLOW_gen_iter_in_gen_for4947 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_gen_if4966 = new BitSet(new long[]{0x0000080180000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_test_in_gen_if4968 = new BitSet(new long[]{0x000080000A000002L});
    public static final BitSet FOLLOW_gen_iter_in_gen_if4970 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_YIELD_in_yield_expr4989 = new BitSet(new long[]{0x0000080180000242L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_testlist_in_yield_expr4991 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred1_PythonPartial803 = new BitSet(new long[]{0x0000080000000200L});
    public static final BitSet FOLLOW_fpdef_in_synpred1_PythonPartial805 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred1_PythonPartial807 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_testlist_in_synpred2_PythonPartial1103 = new BitSet(new long[]{0x7FF8000000000000L});
    public static final BitSet FOLLOW_augassign_in_synpred2_PythonPartial1105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_testlist_in_synpred3_PythonPartial1183 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_ASSIGN_in_synpred3_PythonPartial1185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_synpred4_PythonPartial1468 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred4_PythonPartial1470 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_decorators_in_synpred5_PythonPartial2153 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_DEF_in_synpred5_PythonPartial2156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_decorators_in_synpred6_PythonPartial2170 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CLASS_in_synpred6_PythonPartial2173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_synpred7_PythonPartial2734 = new BitSet(new long[]{0x0000080100000240L,0x0000001807EB1800L});
    public static final BitSet FOLLOW_or_test_in_synpred7_PythonPartial2736 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_ORELSE_in_synpred7_PythonPartial2738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_synpred8_PythonPartial4256 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_synpred8_PythonPartial4258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_synpred9_PythonPartial4290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_synpred10_PythonPartial4375 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred10_PythonPartial4377 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_synpred11_PythonPartial4468 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred11_PythonPartial4470 = new BitSet(new long[]{0x0000000000000002L});

}