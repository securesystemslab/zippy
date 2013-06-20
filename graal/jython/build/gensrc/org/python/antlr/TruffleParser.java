// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g 2013-06-19 17:22:40

package org.python.antlr;

import org.antlr.runtime.CommonToken;

import org.python.antlr.ParseException;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.arguments;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.BoolOp;
import org.python.antlr.ast.boolopType;
import org.python.antlr.ast.Break;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.Compare;
import org.python.antlr.ast.comprehension;
import org.python.antlr.ast.Context;
import org.python.antlr.ast.Continue;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Dict;
import org.python.antlr.ast.DictComp;
import org.python.antlr.ast.Ellipsis;
import org.python.antlr.ast.ErrorMod;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.Exec;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.ExtSlice;
import org.python.antlr.ast.For;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Index;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.keyword;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.None;
import org.python.antlr.ast.True;
import org.python.antlr.ast.False;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.operatorType;
import org.python.antlr.ast.Pass;
import org.python.antlr.ast.Print;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Repr;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Set;
import org.python.antlr.ast.SetComp;
import org.python.antlr.ast.Slice;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.TryExcept;
import org.python.antlr.ast.TryFinally;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.unaryopType;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.ast.nodes.NodeFactory;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;
import org.python.core.Options;
import org.python.antlr.ast.Nonlocal;


import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

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
 *
 *  Updated the original parser for Python 2.5 features. The parser has been
 *  altered to produce an AST - the AST work started from tne newcompiler
 *  grammar from Jim Baker.  The current parsing and compiling strategy looks
 *  like this:
 *
 *  Python source->Python.g->AST (org/python/parser/ast/*)->CodeCompiler(ASM)->.class
 */
public class TruffleParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "INDENT", "DEDENT", "TRAILBACKSLASH", "NEWLINE", "LEADING_WS", "NAME", "DOT", "PRINT", "AND", "AS", "ASSERT", "BREAK", "CLASS", "CONTINUE", "DEF", "DELETE", "ELIF", "EXCEPT", "FINALLY", "FROM", "FOR", "GLOBAL", "IF", "IMPORT", "IN", "IS", "LAMBDA", "NOT", "OR", "ORELSE", "PASS", "RAISE", "RETURN", "TRY", "WHILE", "WITH", "YIELD", "NONE", "TRUE", "FALSE", "NONLOCAL", "AT", "LPAREN", "RPAREN", "COLON", "ASSIGN", "COMMA", "STAR", "DOUBLESTAR", "SEMI", "PLUSEQUAL", "MINUSEQUAL", "STAREQUAL", "SLASHEQUAL", "PERCENTEQUAL", "AMPEREQUAL", "VBAREQUAL", "CIRCUMFLEXEQUAL", "LEFTSHIFTEQUAL", "RIGHTSHIFTEQUAL", "DOUBLESTAREQUAL", "DOUBLESLASHEQUAL", "RIGHTSHIFT", "EXEC", "LESS", "GREATER", "EQUAL", "GREATEREQUAL", "LESSEQUAL", "NOTEQUAL", "VBAR", "CIRCUMFLEX", "AMPER", "LEFTSHIFT", "PLUS", "MINUS", "SLASH", "PERCENT", "DOUBLESLASH", "TILDE", "LBRACK", "RBRACK", "LCURLY", "RCURLY", "BACKQUOTE", "INT", "FLOAT", "COMPLEX", "STRING", "DIGITS", "Exponent", "TRIAPOS", "TRIQUOTE", "ESC", "COMMENT", "CONTINUED_LINE", "WS"
    };
    public static final int SLASHEQUAL=57;
    public static final int BACKQUOTE=88;
    public static final int STAR=51;
    public static final int CIRCUMFLEXEQUAL=61;
    public static final int WHILE=38;
    public static final int TRIAPOS=95;
    public static final int ORELSE=33;
    public static final int GREATEREQUAL=71;
    public static final int COMPLEX=91;
    public static final int NOT=31;
    public static final int EXCEPT=21;
    public static final int EOF=-1;
    public static final int BREAK=15;
    public static final int PASS=34;
    public static final int LEADING_WS=8;
    public static final int NOTEQUAL=73;
    public static final int MINUSEQUAL=55;
    public static final int VBAR=74;
    public static final int RPAREN=47;
    public static final int IMPORT=27;
    public static final int NAME=9;
    public static final int GREATER=69;
    public static final int DOUBLESTAREQUAL=64;
    public static final int RETURN=36;
    public static final int LESS=68;
    public static final int RAISE=35;
    public static final int COMMENT=98;
    public static final int RBRACK=85;
    public static final int NONLOCAL=44;
    public static final int LCURLY=86;
    public static final int INT=89;
    public static final int DELETE=19;
    public static final int RIGHTSHIFT=66;
    public static final int ASSERT=14;
    public static final int TRY=37;
    public static final int DOUBLESLASHEQUAL=65;
    public static final int ELIF=20;
    public static final int WS=100;
    public static final int NONE=41;
    public static final int VBAREQUAL=60;
    public static final int OR=32;
    public static final int FROM=23;
    public static final int FALSE=43;
    public static final int PERCENTEQUAL=58;
    public static final int LESSEQUAL=72;
    public static final int DOUBLESLASH=82;
    public static final int CLASS=16;
    public static final int CONTINUED_LINE=99;
    public static final int LBRACK=84;
    public static final int DEF=18;
    public static final int DOUBLESTAR=52;
    public static final int ESC=97;
    public static final int DIGITS=93;
    public static final int Exponent=94;
    public static final int FOR=24;
    public static final int DEDENT=5;
    public static final int FLOAT=90;
    public static final int AND=12;
    public static final int RIGHTSHIFTEQUAL=63;
    public static final int INDENT=4;
    public static final int LPAREN=46;
    public static final int IF=26;
    public static final int PLUSEQUAL=54;
    public static final int AT=45;
    public static final int AS=13;
    public static final int SLASH=80;
    public static final int IN=28;
    public static final int CONTINUE=17;
    public static final int COMMA=50;
    public static final int IS=29;
    public static final int AMPER=76;
    public static final int EQUAL=70;
    public static final int YIELD=40;
    public static final int TILDE=83;
    public static final int LEFTSHIFTEQUAL=62;
    public static final int LEFTSHIFT=77;
    public static final int PLUS=78;
    public static final int LAMBDA=30;
    public static final int DOT=10;
    public static final int WITH=39;
    public static final int PERCENT=81;
    public static final int EXEC=67;
    public static final int MINUS=79;
    public static final int TRUE=42;
    public static final int SEMI=53;
    public static final int PRINT=11;
    public static final int TRIQUOTE=96;
    public static final int COLON=48;
    public static final int TRAILBACKSLASH=6;
    public static final int NEWLINE=7;
    public static final int AMPEREQUAL=59;
    public static final int FINALLY=22;
    public static final int RCURLY=87;
    public static final int ASSIGN=49;
    public static final int GLOBAL=25;
    public static final int STAREQUAL=56;
    public static final int CIRCUMFLEX=75;
    public static final int STRING=92;

    // delegates
    // delegators


        public TruffleParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public TruffleParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return TruffleParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g"; }


        private ErrorHandler errorHandler;

        private GrammarActions actions = new GrammarActions();

        private String encoding;

        private boolean printFunction = true;
        private boolean unicodeLiterals = false;
        
        public void setErrorHandler(ErrorHandler eh) {
            this.errorHandler = eh;
            actions.setErrorHandler(eh);
        }

        protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow)
            throws RecognitionException {

            Object o = errorHandler.recoverFromMismatchedToken(this, input, ttype, follow);
            if (o != null) {
                return o;
            }
            return super.recoverFromMismatchedToken(input, ttype, follow);
        }

        public TruffleParser(TokenStream input, String encoding) {
            this(input);
            this.encoding = encoding;
        }

        @Override
        public void reportError(RecognitionException e) {
          // Update syntax error count and output error.
          super.reportError(e);
          errorHandler.reportError(this, e);
        }

        @Override
        public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
            //Do nothing. We will handle error display elsewhere.
        }


    public static class single_input_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "single_input"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:302:1: single_input : ( ( NEWLINE )* EOF | simple_stmt ( NEWLINE )* EOF | compound_stmt ( NEWLINE )+ EOF );
    public final TruffleParser.single_input_return single_input() throws RecognitionException {
        TruffleParser.single_input_return retval = new TruffleParser.single_input_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token NEWLINE1=null;
        Token EOF2=null;
        Token NEWLINE4=null;
        Token EOF5=null;
        Token NEWLINE7=null;
        Token EOF8=null;
        TruffleParser.simple_stmt_return simple_stmt3 = null;

        TruffleParser.compound_stmt_return compound_stmt6 = null;


        PythonTree NEWLINE1_tree=null;
        PythonTree EOF2_tree=null;
        PythonTree NEWLINE4_tree=null;
        PythonTree EOF5_tree=null;
        PythonTree NEWLINE7_tree=null;
        PythonTree EOF8_tree=null;


            mod mtype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:309:5: ( ( NEWLINE )* EOF | simple_stmt ( NEWLINE )* EOF | compound_stmt ( NEWLINE )+ EOF )
            int alt4=3;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==EOF||LA4_0==NEWLINE) ) {
                alt4=1;
            }
            else if ( (LA4_0==NAME||LA4_0==NOT||LA4_0==LPAREN||(LA4_0>=PLUS && LA4_0<=MINUS)||(LA4_0>=TILDE && LA4_0<=LBRACK)||LA4_0==LCURLY||LA4_0==BACKQUOTE) ) {
                alt4=2;
            }
            else if ( (LA4_0==PRINT) && (((!printFunction)||(printFunction)))) {
                alt4=2;
            }
            else if ( ((LA4_0>=ASSERT && LA4_0<=BREAK)||LA4_0==CONTINUE||LA4_0==DELETE||LA4_0==FROM||LA4_0==GLOBAL||LA4_0==IMPORT||LA4_0==LAMBDA||(LA4_0>=PASS && LA4_0<=RETURN)||(LA4_0>=YIELD && LA4_0<=NONLOCAL)||(LA4_0>=INT && LA4_0<=STRING)) ) {
                alt4=2;
            }
            else if ( (LA4_0==CLASS||LA4_0==DEF||LA4_0==FOR||LA4_0==IF||(LA4_0>=TRY && LA4_0<=WITH)||LA4_0==AT) ) {
                alt4=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:309:7: ( NEWLINE )* EOF
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:309:7: ( NEWLINE )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==NEWLINE) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:309:7: NEWLINE
                    	    {
                    	    NEWLINE1=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_single_input118); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    NEWLINE1_tree = (PythonTree)adaptor.create(NEWLINE1);
                    	    adaptor.addChild(root_0, NEWLINE1_tree);
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);

                    EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_single_input121); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EOF2_tree = (PythonTree)adaptor.create(EOF2);
                    adaptor.addChild(root_0, EOF2_tree);
                    }
                    if ( state.backtracking==0 ) {

                              mtype = new Interactive(((Token)retval.start), new ArrayList<stmt>());
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:313:7: simple_stmt ( NEWLINE )* EOF
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_simple_stmt_in_single_input137);
                    simple_stmt3=simple_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_stmt3.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:313:19: ( NEWLINE )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( (LA2_0==NEWLINE) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:313:19: NEWLINE
                    	    {
                    	    NEWLINE4=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_single_input139); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    NEWLINE4_tree = (PythonTree)adaptor.create(NEWLINE4);
                    	    adaptor.addChild(root_0, NEWLINE4_tree);
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);

                    EOF5=(Token)match(input,EOF,FOLLOW_EOF_in_single_input142); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EOF5_tree = (PythonTree)adaptor.create(EOF5);
                    adaptor.addChild(root_0, EOF5_tree);
                    }
                    if ( state.backtracking==0 ) {

                              mtype = new Interactive(((Token)retval.start), actions.castStmts((simple_stmt3!=null?simple_stmt3.stypes:null)));
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:317:7: compound_stmt ( NEWLINE )+ EOF
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_compound_stmt_in_single_input158);
                    compound_stmt6=compound_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, compound_stmt6.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:317:21: ( NEWLINE )+
                    int cnt3=0;
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==NEWLINE) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:317:21: NEWLINE
                    	    {
                    	    NEWLINE7=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_single_input160); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    NEWLINE7_tree = (PythonTree)adaptor.create(NEWLINE7);
                    	    adaptor.addChild(root_0, NEWLINE7_tree);
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt3 >= 1 ) break loop3;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(3, input);
                                throw eee;
                        }
                        cnt3++;
                    } while (true);

                    EOF8=(Token)match(input,EOF,FOLLOW_EOF_in_single_input163); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EOF8_tree = (PythonTree)adaptor.create(EOF8);
                    adaptor.addChild(root_0, EOF8_tree);
                    }
                    if ( state.backtracking==0 ) {

                              mtype = new Interactive(((Token)retval.start), actions.castStmts((compound_stmt6!=null?((PythonTree)compound_stmt6.tree):null)));
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = mtype;

            }
        }
        catch (RecognitionException re) {

                    reportError(re);
                    errorHandler.recover(this, input,re);
                    PythonTree badNode = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
                    retval.tree = new ErrorMod(badNode);
                
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "single_input"

    public static class file_input_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "file_input"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:331:1: file_input : ( NEWLINE | stmt )* EOF ;
    public final TruffleParser.file_input_return file_input() throws RecognitionException {
        TruffleParser.file_input_return retval = new TruffleParser.file_input_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token NEWLINE9=null;
        Token EOF11=null;
        TruffleParser.stmt_return stmt10 = null;


        PythonTree NEWLINE9_tree=null;
        PythonTree EOF11_tree=null;


            mod mtype = null;
            List stypes = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:349:5: ( ( NEWLINE | stmt )* EOF )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:349:7: ( NEWLINE | stmt )* EOF
            {
            root_0 = (PythonTree)adaptor.nil();

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:349:7: ( NEWLINE | stmt )*
            loop5:
            do {
                int alt5=3;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==NEWLINE) ) {
                    alt5=1;
                }
                else if ( (LA5_0==NAME||LA5_0==NOT||LA5_0==LPAREN||(LA5_0>=PLUS && LA5_0<=MINUS)||(LA5_0>=TILDE && LA5_0<=LBRACK)||LA5_0==LCURLY||LA5_0==BACKQUOTE) ) {
                    alt5=2;
                }
                else if ( (LA5_0==PRINT) && (((!printFunction)||(printFunction)))) {
                    alt5=2;
                }
                else if ( ((LA5_0>=ASSERT && LA5_0<=DELETE)||(LA5_0>=FROM && LA5_0<=IMPORT)||LA5_0==LAMBDA||(LA5_0>=PASS && LA5_0<=AT)||(LA5_0>=INT && LA5_0<=STRING)) ) {
                    alt5=2;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:349:8: NEWLINE
            	    {
            	    NEWLINE9=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_file_input215); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    NEWLINE9_tree = (PythonTree)adaptor.create(NEWLINE9);
            	    adaptor.addChild(root_0, NEWLINE9_tree);
            	    }

            	    }
            	    break;
            	case 2 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:350:9: stmt
            	    {
            	    pushFollow(FOLLOW_stmt_in_file_input225);
            	    stmt10=stmt();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, stmt10.getTree());
            	    if ( state.backtracking==0 ) {

            	                if ((stmt10!=null?stmt10.stypes:null) != null) {
            	                    stypes.addAll((stmt10!=null?stmt10.stypes:null));
            	                }
            	            
            	    }

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            EOF11=(Token)match(input,EOF,FOLLOW_EOF_in_file_input244); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EOF11_tree = (PythonTree)adaptor.create(EOF11);
            adaptor.addChild(root_0, EOF11_tree);
            }
            if ( state.backtracking==0 ) {

                           mtype = new Module(((Token)retval.start), actions.castStmts(stypes));
                       
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (!stypes.isEmpty()) {
                      //The EOF token messes up the end offsets, so set them manually.
                      //XXX: this may no longer be true now that PythonTokenSource is
                      //     adjusting EOF offsets -- but needs testing before I remove
                      //     this.
                      PythonTree stop = (PythonTree)stypes.get(stypes.size() -1);
                      mtype.setCharStopIndex(stop.getCharStopIndex());
                      mtype.setTokenStopIndex(stop.getTokenStopIndex());
                  }

                  retval.tree = mtype;

            }
        }
        catch (RecognitionException re) {

                    reportError(re);
                    errorHandler.recover(this, input,re);
                    PythonTree badNode = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
                    retval.tree = new ErrorMod(badNode);
                
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "file_input"

    public static class eval_input_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "eval_input"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:370:1: eval_input : ( LEADING_WS )? ( NEWLINE )* testlist[expr_contextType.Load] ( NEWLINE )* EOF ;
    public final TruffleParser.eval_input_return eval_input() throws RecognitionException {
        TruffleParser.eval_input_return retval = new TruffleParser.eval_input_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token LEADING_WS12=null;
        Token NEWLINE13=null;
        Token NEWLINE15=null;
        Token EOF16=null;
        TruffleParser.testlist_return testlist14 = null;


        PythonTree LEADING_WS12_tree=null;
        PythonTree NEWLINE13_tree=null;
        PythonTree NEWLINE15_tree=null;
        PythonTree EOF16_tree=null;


            mod mtype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:377:5: ( ( LEADING_WS )? ( NEWLINE )* testlist[expr_contextType.Load] ( NEWLINE )* EOF )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:377:7: ( LEADING_WS )? ( NEWLINE )* testlist[expr_contextType.Load] ( NEWLINE )* EOF
            {
            root_0 = (PythonTree)adaptor.nil();

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:377:7: ( LEADING_WS )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==LEADING_WS) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:377:7: LEADING_WS
                    {
                    LEADING_WS12=(Token)match(input,LEADING_WS,FOLLOW_LEADING_WS_in_eval_input298); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEADING_WS12_tree = (PythonTree)adaptor.create(LEADING_WS12);
                    adaptor.addChild(root_0, LEADING_WS12_tree);
                    }

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:377:19: ( NEWLINE )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==NEWLINE) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:377:20: NEWLINE
            	    {
            	    NEWLINE13=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_eval_input302); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    NEWLINE13_tree = (PythonTree)adaptor.create(NEWLINE13);
            	    adaptor.addChild(root_0, NEWLINE13_tree);
            	    }

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            pushFollow(FOLLOW_testlist_in_eval_input306);
            testlist14=testlist(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, testlist14.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:377:62: ( NEWLINE )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==NEWLINE) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:377:63: NEWLINE
            	    {
            	    NEWLINE15=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_eval_input310); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    NEWLINE15_tree = (PythonTree)adaptor.create(NEWLINE15);
            	    adaptor.addChild(root_0, NEWLINE15_tree);
            	    }

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            EOF16=(Token)match(input,EOF,FOLLOW_EOF_in_eval_input314); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EOF16_tree = (PythonTree)adaptor.create(EOF16);
            adaptor.addChild(root_0, EOF16_tree);
            }
            if ( state.backtracking==0 ) {

                      mtype = new Expression(((Token)retval.start), actions.castExpr((testlist14!=null?((PythonTree)testlist14.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = mtype;

            }
        }
        catch (RecognitionException re) {

                    reportError(re);
                    errorHandler.recover(this, input,re);
                    PythonTree badNode = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
                    retval.tree = new ErrorMod(badNode);
                
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "eval_input"

    public static class dotted_attr_return extends ParserRuleReturnScope {
        public expr etype;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dotted_attr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:392:1: dotted_attr returns [expr etype] : n1= NAME ( ( DOT n2+= NAME )+ | ) ;
    public final TruffleParser.dotted_attr_return dotted_attr() throws RecognitionException {
        TruffleParser.dotted_attr_return retval = new TruffleParser.dotted_attr_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token n1=null;
        Token DOT17=null;
        Token n2=null;
        List list_n2=null;

        PythonTree n1_tree=null;
        PythonTree DOT17_tree=null;
        PythonTree n2_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:394:5: (n1= NAME ( ( DOT n2+= NAME )+ | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:394:7: n1= NAME ( ( DOT n2+= NAME )+ | )
            {
            root_0 = (PythonTree)adaptor.nil();

            n1=(Token)match(input,NAME,FOLLOW_NAME_in_dotted_attr366); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            n1_tree = (PythonTree)adaptor.create(n1);
            adaptor.addChild(root_0, n1_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:395:7: ( ( DOT n2+= NAME )+ | )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==DOT) ) {
                alt10=1;
            }
            else if ( (LA10_0==NEWLINE||LA10_0==LPAREN) ) {
                alt10=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:395:9: ( DOT n2+= NAME )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:395:9: ( DOT n2+= NAME )+
                    int cnt9=0;
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( (LA9_0==DOT) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:395:10: DOT n2+= NAME
                    	    {
                    	    DOT17=(Token)match(input,DOT,FOLLOW_DOT_in_dotted_attr377); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    DOT17_tree = (PythonTree)adaptor.create(DOT17);
                    	    adaptor.addChild(root_0, DOT17_tree);
                    	    }
                    	    n2=(Token)match(input,NAME,FOLLOW_NAME_in_dotted_attr381); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    n2_tree = (PythonTree)adaptor.create(n2);
                    	    adaptor.addChild(root_0, n2_tree);
                    	    }
                    	    if (list_n2==null) list_n2=new ArrayList();
                    	    list_n2.add(n2);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt9 >= 1 ) break loop9;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(9, input);
                                throw eee;
                        }
                        cnt9++;
                    } while (true);

                    if ( state.backtracking==0 ) {

                                  retval.etype = actions.makeDottedAttr(n1, list_n2);
                              
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:400:9: 
                    {
                    if ( state.backtracking==0 ) {

                                  retval.etype = actions.makeNameNode(n1);
                              
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "dotted_attr"

    public static class name_or_print_return extends ParserRuleReturnScope {
        public Token tok;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "name_or_print"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:408:1: name_or_print returns [Token tok] : ( NAME | {...}? => PRINT );
    public final TruffleParser.name_or_print_return name_or_print() throws RecognitionException {
        TruffleParser.name_or_print_return retval = new TruffleParser.name_or_print_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token NAME18=null;
        Token PRINT19=null;

        PythonTree NAME18_tree=null;
        PythonTree PRINT19_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:410:5: ( NAME | {...}? => PRINT )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==NAME) ) {
                alt11=1;
            }
            else if ( (LA11_0==PRINT) && ((printFunction))) {
                alt11=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:410:7: NAME
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    NAME18=(Token)match(input,NAME,FOLLOW_NAME_in_name_or_print446); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NAME18_tree = (PythonTree)adaptor.create(NAME18);
                    adaptor.addChild(root_0, NAME18_tree);
                    }
                    if ( state.backtracking==0 ) {

                              retval.tok = ((Token)retval.start);
                          
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:413:7: {...}? => PRINT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    if ( !((printFunction)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "name_or_print", "printFunction");
                    }
                    PRINT19=(Token)match(input,PRINT,FOLLOW_PRINT_in_name_or_print460); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PRINT19_tree = (PythonTree)adaptor.create(PRINT19);
                    adaptor.addChild(root_0, PRINT19_tree);
                    }
                    if ( state.backtracking==0 ) {

                              retval.tok = ((Token)retval.start);
                          
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "name_or_print"

    public static class attr_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:422:1: attr : ( NAME | AND | AS | ASSERT | BREAK | CLASS | CONTINUE | DEF | DELETE | ELIF | EXCEPT | FINALLY | FROM | FOR | GLOBAL | IF | IMPORT | IN | IS | LAMBDA | NOT | OR | ORELSE | PASS | PRINT | RAISE | RETURN | TRY | WHILE | WITH | YIELD | NONE | TRUE | FALSE | NONLOCAL );
    public final TruffleParser.attr_return attr() throws RecognitionException {
        TruffleParser.attr_return retval = new TruffleParser.attr_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token set20=null;

        PythonTree set20_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:423:5: ( NAME | AND | AS | ASSERT | BREAK | CLASS | CONTINUE | DEF | DELETE | ELIF | EXCEPT | FINALLY | FROM | FOR | GLOBAL | IF | IMPORT | IN | IS | LAMBDA | NOT | OR | ORELSE | PASS | PRINT | RAISE | RETURN | TRY | WHILE | WITH | YIELD | NONE | TRUE | FALSE | NONLOCAL )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:
            {
            root_0 = (PythonTree)adaptor.nil();

            set20=(Token)input.LT(1);
            if ( input.LA(1)==NAME||(input.LA(1)>=PRINT && input.LA(1)<=NONLOCAL) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (PythonTree)adaptor.create(set20));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "attr"

    public static class decorator_return extends ParserRuleReturnScope {
        public expr etype;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "decorator"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:462:1: decorator returns [expr etype] : AT dotted_attr ( LPAREN ( arglist | ) RPAREN | ) NEWLINE ;
    public final TruffleParser.decorator_return decorator() throws RecognitionException {
        TruffleParser.decorator_return retval = new TruffleParser.decorator_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token AT21=null;
        Token LPAREN23=null;
        Token RPAREN25=null;
        Token NEWLINE26=null;
        TruffleParser.dotted_attr_return dotted_attr22 = null;

        TruffleParser.arglist_return arglist24 = null;


        PythonTree AT21_tree=null;
        PythonTree LPAREN23_tree=null;
        PythonTree RPAREN25_tree=null;
        PythonTree NEWLINE26_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:467:5: ( AT dotted_attr ( LPAREN ( arglist | ) RPAREN | ) NEWLINE )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:467:7: AT dotted_attr ( LPAREN ( arglist | ) RPAREN | ) NEWLINE
            {
            root_0 = (PythonTree)adaptor.nil();

            AT21=(Token)match(input,AT,FOLLOW_AT_in_decorator791); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            AT21_tree = (PythonTree)adaptor.create(AT21);
            adaptor.addChild(root_0, AT21_tree);
            }
            pushFollow(FOLLOW_dotted_attr_in_decorator793);
            dotted_attr22=dotted_attr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dotted_attr22.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:468:5: ( LPAREN ( arglist | ) RPAREN | )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==LPAREN) ) {
                alt13=1;
            }
            else if ( (LA13_0==NEWLINE) ) {
                alt13=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:468:7: LPAREN ( arglist | ) RPAREN
                    {
                    LPAREN23=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_decorator801); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN23_tree = (PythonTree)adaptor.create(LPAREN23);
                    adaptor.addChild(root_0, LPAREN23_tree);
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:469:7: ( arglist | )
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==NAME||LA12_0==NOT||LA12_0==LPAREN||(LA12_0>=PLUS && LA12_0<=MINUS)||(LA12_0>=TILDE && LA12_0<=LBRACK)||LA12_0==LCURLY||LA12_0==BACKQUOTE) ) {
                        alt12=1;
                    }
                    else if ( (LA12_0==PRINT) && ((printFunction))) {
                        alt12=1;
                    }
                    else if ( (LA12_0==LAMBDA||(LA12_0>=NONE && LA12_0<=FALSE)||(LA12_0>=STAR && LA12_0<=DOUBLESTAR)||(LA12_0>=INT && LA12_0<=STRING)) ) {
                        alt12=1;
                    }
                    else if ( (LA12_0==RPAREN) ) {
                        alt12=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 0, input);

                        throw nvae;
                    }
                    switch (alt12) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:469:9: arglist
                            {
                            pushFollow(FOLLOW_arglist_in_decorator811);
                            arglist24=arglist();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, arglist24.getTree());
                            if ( state.backtracking==0 ) {

                                          retval.etype = actions.makeCall(LPAREN23, (dotted_attr22!=null?dotted_attr22.etype:null), (arglist24!=null?arglist24.args:null), (arglist24!=null?arglist24.keywords:null),
                                                   (arglist24!=null?arglist24.starargs:null), (arglist24!=null?arglist24.kwargs:null));
                                      
                            }

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:475:9: 
                            {
                            if ( state.backtracking==0 ) {

                                          retval.etype = actions.makeCall(LPAREN23, (dotted_attr22!=null?dotted_attr22.etype:null));
                                      
                            }

                            }
                            break;

                    }

                    RPAREN25=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_decorator855); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN25_tree = (PythonTree)adaptor.create(RPAREN25);
                    adaptor.addChild(root_0, RPAREN25_tree);
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:481:7: 
                    {
                    if ( state.backtracking==0 ) {

                                retval.etype = (dotted_attr22!=null?dotted_attr22.etype:null);
                            
                    }

                    }
                    break;

            }

            NEWLINE26=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_decorator877); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            NEWLINE26_tree = (PythonTree)adaptor.create(NEWLINE26);
            adaptor.addChild(root_0, NEWLINE26_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = retval.etype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "decorator"

    public static class decorators_return extends ParserRuleReturnScope {
        public List etypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "decorators"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:488:1: decorators returns [List etypes] : (d+= decorator )+ ;
    public final TruffleParser.decorators_return decorators() throws RecognitionException {
        TruffleParser.decorators_return retval = new TruffleParser.decorators_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        List list_d=null;
        TruffleParser.decorator_return d = null;
         d = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:490:5: ( (d+= decorator )+ )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:490:7: (d+= decorator )+
            {
            root_0 = (PythonTree)adaptor.nil();

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:490:8: (d+= decorator )+
            int cnt14=0;
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==AT) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:490:8: d+= decorator
            	    {
            	    pushFollow(FOLLOW_decorator_in_decorators905);
            	    d=decorator();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, d.getTree());
            	    if (list_d==null) list_d=new ArrayList();
            	    list_d.add(d.getTree());


            	    }
            	    break;

            	default :
            	    if ( cnt14 >= 1 ) break loop14;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(14, input);
                        throw eee;
                }
                cnt14++;
            } while (true);

            if ( state.backtracking==0 ) {

                        retval.etypes = list_d;
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "decorators"

    public static class funcdef_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "funcdef"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:497:1: funcdef : ( decorators )? DEF name_or_print parameters COLON suite[false] ;
    public final TruffleParser.funcdef_return funcdef() throws RecognitionException {
        TruffleParser.funcdef_return retval = new TruffleParser.funcdef_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token DEF28=null;
        Token COLON31=null;
        TruffleParser.decorators_return decorators27 = null;

        TruffleParser.name_or_print_return name_or_print29 = null;

        TruffleParser.parameters_return parameters30 = null;

        TruffleParser.suite_return suite32 = null;


        PythonTree DEF28_tree=null;
        PythonTree COLON31_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:505:5: ( ( decorators )? DEF name_or_print parameters COLON suite[false] )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:505:7: ( decorators )? DEF name_or_print parameters COLON suite[false]
            {
            root_0 = (PythonTree)adaptor.nil();

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:505:7: ( decorators )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==AT) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:505:7: decorators
                    {
                    pushFollow(FOLLOW_decorators_in_funcdef943);
                    decorators27=decorators();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, decorators27.getTree());

                    }
                    break;

            }

            DEF28=(Token)match(input,DEF,FOLLOW_DEF_in_funcdef946); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DEF28_tree = (PythonTree)adaptor.create(DEF28);
            adaptor.addChild(root_0, DEF28_tree);
            }
            pushFollow(FOLLOW_name_or_print_in_funcdef948);
            name_or_print29=name_or_print();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, name_or_print29.getTree());
            pushFollow(FOLLOW_parameters_in_funcdef950);
            parameters30=parameters();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, parameters30.getTree());
            COLON31=(Token)match(input,COLON,FOLLOW_COLON_in_funcdef952); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON31_tree = (PythonTree)adaptor.create(COLON31);
            adaptor.addChild(root_0, COLON31_tree);
            }
            pushFollow(FOLLOW_suite_in_funcdef954);
            suite32=suite(false);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, suite32.getTree());
            if ( state.backtracking==0 ) {

                      Token t = DEF28;
                      if ((decorators27!=null?((Token)decorators27.start):null) != null) {
                          t = (decorators27!=null?((Token)decorators27.start):null);
                      }
                      stype = actions.makeFuncdef(t, (name_or_print29!=null?((Token)name_or_print29.start):null), (parameters30!=null?parameters30.args:null), (suite32!=null?suite32.stypes:null), (decorators27!=null?decorators27.etypes:null));
                  
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "funcdef"

    public static class parameters_return extends ParserRuleReturnScope {
        public arguments args;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parameters"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:516:1: parameters returns [arguments args] : LPAREN ( varargslist | ) RPAREN ;
    public final TruffleParser.parameters_return parameters() throws RecognitionException {
        TruffleParser.parameters_return retval = new TruffleParser.parameters_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token LPAREN33=null;
        Token RPAREN35=null;
        TruffleParser.varargslist_return varargslist34 = null;


        PythonTree LPAREN33_tree=null;
        PythonTree RPAREN35_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:518:5: ( LPAREN ( varargslist | ) RPAREN )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:518:7: LPAREN ( varargslist | ) RPAREN
            {
            root_0 = (PythonTree)adaptor.nil();

            LPAREN33=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_parameters987); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN33_tree = (PythonTree)adaptor.create(LPAREN33);
            adaptor.addChild(root_0, LPAREN33_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:519:7: ( varargslist | )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==NAME||LA16_0==LPAREN||(LA16_0>=STAR && LA16_0<=DOUBLESTAR)) ) {
                alt16=1;
            }
            else if ( (LA16_0==RPAREN) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:519:8: varargslist
                    {
                    pushFollow(FOLLOW_varargslist_in_parameters996);
                    varargslist34=varargslist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, varargslist34.getTree());
                    if ( state.backtracking==0 ) {

                                    retval.args = (varargslist34!=null?varargslist34.args:null);
                              
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:524:9: 
                    {
                    if ( state.backtracking==0 ) {

                                  retval.args = new arguments(((Token)retval.start), new ArrayList<expr>(), null, null, new ArrayList<expr>());
                              
                    }

                    }
                    break;

            }

            RPAREN35=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_parameters1040); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN35_tree = (PythonTree)adaptor.create(RPAREN35);
            adaptor.addChild(root_0, RPAREN35_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parameters"

    public static class defparameter_return extends ParserRuleReturnScope {
        public expr etype;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "defparameter"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:532:1: defparameter[List defaults] returns [expr etype] : fpdef[expr_contextType.Param] ( ASSIGN test[expr_contextType.Load] )? ;
    public final TruffleParser.defparameter_return defparameter(List defaults) throws RecognitionException {
        TruffleParser.defparameter_return retval = new TruffleParser.defparameter_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token ASSIGN37=null;
        TruffleParser.fpdef_return fpdef36 = null;

        TruffleParser.test_return test38 = null;


        PythonTree ASSIGN37_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:537:5: ( fpdef[expr_contextType.Param] ( ASSIGN test[expr_contextType.Load] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:537:7: fpdef[expr_contextType.Param] ( ASSIGN test[expr_contextType.Load] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_fpdef_in_defparameter1073);
            fpdef36=fpdef(expr_contextType.Param);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, fpdef36.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:537:37: ( ASSIGN test[expr_contextType.Load] )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==ASSIGN) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:537:38: ASSIGN test[expr_contextType.Load]
                    {
                    ASSIGN37=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_defparameter1077); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ASSIGN37_tree = (PythonTree)adaptor.create(ASSIGN37);
                    adaptor.addChild(root_0, ASSIGN37_tree);
                    }
                    pushFollow(FOLLOW_test_in_defparameter1079);
                    test38=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, test38.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        retval.etype = actions.castExpr((fpdef36!=null?((PythonTree)fpdef36.tree):null));
                        if (ASSIGN37 != null) {
                            defaults.add((test38!=null?((PythonTree)test38.tree):null));
                        } else if (!defaults.isEmpty()) {
                            throw new ParseException("non-default argument follows default argument", (fpdef36!=null?((PythonTree)fpdef36.tree):null));
                        }
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = retval.etype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "defparameter"

    public static class varargslist_return extends ParserRuleReturnScope {
        public arguments args;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "varargslist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:551:1: varargslist returns [arguments args] : (d+= defparameter[defaults] ( options {greedy=true; } : COMMA d+= defparameter[defaults] )* ( COMMA ( STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )? | DOUBLESTAR kwargs= NAME )? )? | STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )? | DOUBLESTAR kwargs= NAME );
    public final TruffleParser.varargslist_return varargslist() throws RecognitionException {
        TruffleParser.varargslist_return retval = new TruffleParser.varargslist_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token starargs=null;
        Token kwargs=null;
        Token COMMA39=null;
        Token COMMA40=null;
        Token STAR41=null;
        Token COMMA42=null;
        Token DOUBLESTAR43=null;
        Token DOUBLESTAR44=null;
        Token STAR45=null;
        Token COMMA46=null;
        Token DOUBLESTAR47=null;
        Token DOUBLESTAR48=null;
        List list_d=null;
        TruffleParser.defparameter_return d = null;
         d = null;
        PythonTree starargs_tree=null;
        PythonTree kwargs_tree=null;
        PythonTree COMMA39_tree=null;
        PythonTree COMMA40_tree=null;
        PythonTree STAR41_tree=null;
        PythonTree COMMA42_tree=null;
        PythonTree DOUBLESTAR43_tree=null;
        PythonTree DOUBLESTAR44_tree=null;
        PythonTree STAR45_tree=null;
        PythonTree COMMA46_tree=null;
        PythonTree DOUBLESTAR47_tree=null;
        PythonTree DOUBLESTAR48_tree=null;


            List defaults = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:556:5: (d+= defparameter[defaults] ( options {greedy=true; } : COMMA d+= defparameter[defaults] )* ( COMMA ( STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )? | DOUBLESTAR kwargs= NAME )? )? | STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )? | DOUBLESTAR kwargs= NAME )
            int alt23=3;
            switch ( input.LA(1) ) {
            case NAME:
            case LPAREN:
                {
                alt23=1;
                }
                break;
            case STAR:
                {
                alt23=2;
                }
                break;
            case DOUBLESTAR:
                {
                alt23=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }

            switch (alt23) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:556:7: d+= defparameter[defaults] ( options {greedy=true; } : COMMA d+= defparameter[defaults] )* ( COMMA ( STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )? | DOUBLESTAR kwargs= NAME )? )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_defparameter_in_varargslist1125);
                    d=defparameter(defaults);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, d.getTree());
                    if (list_d==null) list_d=new ArrayList();
                    list_d.add(d.getTree());

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:556:33: ( options {greedy=true; } : COMMA d+= defparameter[defaults] )*
                    loop18:
                    do {
                        int alt18=2;
                        int LA18_0 = input.LA(1);

                        if ( (LA18_0==COMMA) ) {
                            int LA18_1 = input.LA(2);

                            if ( (LA18_1==NAME||LA18_1==LPAREN) ) {
                                alt18=1;
                            }


                        }


                        switch (alt18) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:556:57: COMMA d+= defparameter[defaults]
                    	    {
                    	    COMMA39=(Token)match(input,COMMA,FOLLOW_COMMA_in_varargslist1136); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA39_tree = (PythonTree)adaptor.create(COMMA39);
                    	    adaptor.addChild(root_0, COMMA39_tree);
                    	    }
                    	    pushFollow(FOLLOW_defparameter_in_varargslist1140);
                    	    d=defparameter(defaults);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, d.getTree());
                    	    if (list_d==null) list_d=new ArrayList();
                    	    list_d.add(d.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop18;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:557:7: ( COMMA ( STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )? | DOUBLESTAR kwargs= NAME )? )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==COMMA) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:557:8: COMMA ( STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )? | DOUBLESTAR kwargs= NAME )?
                            {
                            COMMA40=(Token)match(input,COMMA,FOLLOW_COMMA_in_varargslist1152); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA40_tree = (PythonTree)adaptor.create(COMMA40);
                            adaptor.addChild(root_0, COMMA40_tree);
                            }
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:558:11: ( STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )? | DOUBLESTAR kwargs= NAME )?
                            int alt20=3;
                            int LA20_0 = input.LA(1);

                            if ( (LA20_0==STAR) ) {
                                alt20=1;
                            }
                            else if ( (LA20_0==DOUBLESTAR) ) {
                                alt20=2;
                            }
                            switch (alt20) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:558:12: STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )?
                                    {
                                    STAR41=(Token)match(input,STAR,FOLLOW_STAR_in_varargslist1165); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    STAR41_tree = (PythonTree)adaptor.create(STAR41);
                                    adaptor.addChild(root_0, STAR41_tree);
                                    }
                                    starargs=(Token)match(input,NAME,FOLLOW_NAME_in_varargslist1169); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    starargs_tree = (PythonTree)adaptor.create(starargs);
                                    adaptor.addChild(root_0, starargs_tree);
                                    }
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:558:31: ( COMMA DOUBLESTAR kwargs= NAME )?
                                    int alt19=2;
                                    int LA19_0 = input.LA(1);

                                    if ( (LA19_0==COMMA) ) {
                                        alt19=1;
                                    }
                                    switch (alt19) {
                                        case 1 :
                                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:558:32: COMMA DOUBLESTAR kwargs= NAME
                                            {
                                            COMMA42=(Token)match(input,COMMA,FOLLOW_COMMA_in_varargslist1172); if (state.failed) return retval;
                                            if ( state.backtracking==0 ) {
                                            COMMA42_tree = (PythonTree)adaptor.create(COMMA42);
                                            adaptor.addChild(root_0, COMMA42_tree);
                                            }
                                            DOUBLESTAR43=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_varargslist1174); if (state.failed) return retval;
                                            if ( state.backtracking==0 ) {
                                            DOUBLESTAR43_tree = (PythonTree)adaptor.create(DOUBLESTAR43);
                                            adaptor.addChild(root_0, DOUBLESTAR43_tree);
                                            }
                                            kwargs=(Token)match(input,NAME,FOLLOW_NAME_in_varargslist1178); if (state.failed) return retval;
                                            if ( state.backtracking==0 ) {
                                            kwargs_tree = (PythonTree)adaptor.create(kwargs);
                                            adaptor.addChild(root_0, kwargs_tree);
                                            }

                                            }
                                            break;

                                    }


                                    }
                                    break;
                                case 2 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:559:13: DOUBLESTAR kwargs= NAME
                                    {
                                    DOUBLESTAR44=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_varargslist1194); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    DOUBLESTAR44_tree = (PythonTree)adaptor.create(DOUBLESTAR44);
                                    adaptor.addChild(root_0, DOUBLESTAR44_tree);
                                    }
                                    kwargs=(Token)match(input,NAME,FOLLOW_NAME_in_varargslist1198); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    kwargs_tree = (PythonTree)adaptor.create(kwargs);
                                    adaptor.addChild(root_0, kwargs_tree);
                                    }

                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                retval.args = actions.makeArgumentsType(((Token)retval.start), list_d, starargs, kwargs, defaults);
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:565:7: STAR starargs= NAME ( COMMA DOUBLESTAR kwargs= NAME )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    STAR45=(Token)match(input,STAR,FOLLOW_STAR_in_varargslist1236); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STAR45_tree = (PythonTree)adaptor.create(STAR45);
                    adaptor.addChild(root_0, STAR45_tree);
                    }
                    starargs=(Token)match(input,NAME,FOLLOW_NAME_in_varargslist1240); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    starargs_tree = (PythonTree)adaptor.create(starargs);
                    adaptor.addChild(root_0, starargs_tree);
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:565:26: ( COMMA DOUBLESTAR kwargs= NAME )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==COMMA) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:565:27: COMMA DOUBLESTAR kwargs= NAME
                            {
                            COMMA46=(Token)match(input,COMMA,FOLLOW_COMMA_in_varargslist1243); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA46_tree = (PythonTree)adaptor.create(COMMA46);
                            adaptor.addChild(root_0, COMMA46_tree);
                            }
                            DOUBLESTAR47=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_varargslist1245); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            DOUBLESTAR47_tree = (PythonTree)adaptor.create(DOUBLESTAR47);
                            adaptor.addChild(root_0, DOUBLESTAR47_tree);
                            }
                            kwargs=(Token)match(input,NAME,FOLLOW_NAME_in_varargslist1249); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            kwargs_tree = (PythonTree)adaptor.create(kwargs);
                            adaptor.addChild(root_0, kwargs_tree);
                            }

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                retval.args = actions.makeArgumentsType(((Token)retval.start), list_d, starargs, kwargs, defaults);
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:569:7: DOUBLESTAR kwargs= NAME
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    DOUBLESTAR48=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_varargslist1267); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOUBLESTAR48_tree = (PythonTree)adaptor.create(DOUBLESTAR48);
                    adaptor.addChild(root_0, DOUBLESTAR48_tree);
                    }
                    kwargs=(Token)match(input,NAME,FOLLOW_NAME_in_varargslist1271); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    kwargs_tree = (PythonTree)adaptor.create(kwargs);
                    adaptor.addChild(root_0, kwargs_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.args = actions.makeArgumentsType(((Token)retval.start), list_d, null, kwargs, defaults);
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "varargslist"

    public static class fpdef_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fpdef"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:576:1: fpdef[expr_contextType ctype] : ( NAME | ( LPAREN fpdef[null] COMMA )=> LPAREN fplist RPAREN | LPAREN fplist RPAREN );
    public final TruffleParser.fpdef_return fpdef(expr_contextType ctype) throws RecognitionException {
        TruffleParser.fpdef_return retval = new TruffleParser.fpdef_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token NAME49=null;
        Token LPAREN50=null;
        Token RPAREN52=null;
        Token LPAREN53=null;
        Token RPAREN55=null;
        TruffleParser.fplist_return fplist51 = null;

        TruffleParser.fplist_return fplist54 = null;


        PythonTree NAME49_tree=null;
        PythonTree LPAREN50_tree=null;
        PythonTree RPAREN52_tree=null;
        PythonTree LPAREN53_tree=null;
        PythonTree RPAREN55_tree=null;


            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:586:5: ( NAME | ( LPAREN fpdef[null] COMMA )=> LPAREN fplist RPAREN | LPAREN fplist RPAREN )
            int alt24=3;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==NAME) ) {
                alt24=1;
            }
            else if ( (LA24_0==LPAREN) ) {
                int LA24_2 = input.LA(2);

                if ( (synpred1_Truffle()) ) {
                    alt24=2;
                }
                else if ( (true) ) {
                    alt24=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 24, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:586:7: NAME
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    NAME49=(Token)match(input,NAME,FOLLOW_NAME_in_fpdef1308); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NAME49_tree = (PythonTree)adaptor.create(NAME49);
                    adaptor.addChild(root_0, NAME49_tree);
                    }
                    if ( state.backtracking==0 ) {

                                etype = new Name(NAME49, (NAME49!=null?NAME49.getText():null), ctype);
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:590:7: ( LPAREN fpdef[null] COMMA )=> LPAREN fplist RPAREN
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    LPAREN50=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_fpdef1335); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN50_tree = (PythonTree)adaptor.create(LPAREN50);
                    adaptor.addChild(root_0, LPAREN50_tree);
                    }
                    pushFollow(FOLLOW_fplist_in_fpdef1337);
                    fplist51=fplist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fplist51.getTree());
                    RPAREN52=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_fpdef1339); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN52_tree = (PythonTree)adaptor.create(RPAREN52);
                    adaptor.addChild(root_0, RPAREN52_tree);
                    }
                    if ( state.backtracking==0 ) {

                                etype = new Tuple((fplist51!=null?((Token)fplist51.start):null), actions.castExprs((fplist51!=null?fplist51.etypes:null)), expr_contextType.Store);
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:594:7: LPAREN fplist RPAREN
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    LPAREN53=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_fpdef1355); if (state.failed) return retval;
                    pushFollow(FOLLOW_fplist_in_fpdef1358);
                    fplist54=fplist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fplist54.getTree());
                    RPAREN55=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_fpdef1360); if (state.failed) return retval;

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (etype != null) {
                      retval.tree = etype;
                  }
                  actions.checkAssign(actions.castExpr(((PythonTree)retval.tree)));

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fpdef"

    public static class fplist_return extends ParserRuleReturnScope {
        public List etypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fplist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:598:1: fplist returns [List etypes] : f+= fpdef[expr_contextType.Store] ( options {greedy=true; } : COMMA f+= fpdef[expr_contextType.Store] )* ( COMMA )? ;
    public final TruffleParser.fplist_return fplist() throws RecognitionException {
        TruffleParser.fplist_return retval = new TruffleParser.fplist_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COMMA56=null;
        Token COMMA57=null;
        List list_f=null;
        TruffleParser.fpdef_return f = null;
         f = null;
        PythonTree COMMA56_tree=null;
        PythonTree COMMA57_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:600:5: (f+= fpdef[expr_contextType.Store] ( options {greedy=true; } : COMMA f+= fpdef[expr_contextType.Store] )* ( COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:600:7: f+= fpdef[expr_contextType.Store] ( options {greedy=true; } : COMMA f+= fpdef[expr_contextType.Store] )* ( COMMA )?
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_fpdef_in_fplist1389);
            f=fpdef(expr_contextType.Store);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, f.getTree());
            if (list_f==null) list_f=new ArrayList();
            list_f.add(f.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:601:7: ( options {greedy=true; } : COMMA f+= fpdef[expr_contextType.Store] )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==COMMA) ) {
                    int LA25_1 = input.LA(2);

                    if ( (LA25_1==NAME||LA25_1==LPAREN) ) {
                        alt25=1;
                    }


                }


                switch (alt25) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:601:31: COMMA f+= fpdef[expr_contextType.Store]
            	    {
            	    COMMA56=(Token)match(input,COMMA,FOLLOW_COMMA_in_fplist1406); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA56_tree = (PythonTree)adaptor.create(COMMA56);
            	    adaptor.addChild(root_0, COMMA56_tree);
            	    }
            	    pushFollow(FOLLOW_fpdef_in_fplist1410);
            	    f=fpdef(expr_contextType.Store);

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, f.getTree());
            	    if (list_f==null) list_f=new ArrayList();
            	    list_f.add(f.getTree());


            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:601:72: ( COMMA )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==COMMA) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:601:73: COMMA
                    {
                    COMMA57=(Token)match(input,COMMA,FOLLOW_COMMA_in_fplist1416); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA57_tree = (PythonTree)adaptor.create(COMMA57);
                    adaptor.addChild(root_0, COMMA57_tree);
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        retval.etypes = list_f;
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fplist"

    public static class stmt_return extends ParserRuleReturnScope {
        public List stypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:608:1: stmt returns [List stypes] : ( simple_stmt | compound_stmt );
    public final TruffleParser.stmt_return stmt() throws RecognitionException {
        TruffleParser.stmt_return retval = new TruffleParser.stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        TruffleParser.simple_stmt_return simple_stmt58 = null;

        TruffleParser.compound_stmt_return compound_stmt59 = null;



        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:610:5: ( simple_stmt | compound_stmt )
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==NAME||LA27_0==NOT||LA27_0==LPAREN||(LA27_0>=PLUS && LA27_0<=MINUS)||(LA27_0>=TILDE && LA27_0<=LBRACK)||LA27_0==LCURLY||LA27_0==BACKQUOTE) ) {
                alt27=1;
            }
            else if ( (LA27_0==PRINT) && (((!printFunction)||(printFunction)))) {
                alt27=1;
            }
            else if ( ((LA27_0>=ASSERT && LA27_0<=BREAK)||LA27_0==CONTINUE||LA27_0==DELETE||LA27_0==FROM||LA27_0==GLOBAL||LA27_0==IMPORT||LA27_0==LAMBDA||(LA27_0>=PASS && LA27_0<=RETURN)||(LA27_0>=YIELD && LA27_0<=NONLOCAL)||(LA27_0>=INT && LA27_0<=STRING)) ) {
                alt27=1;
            }
            else if ( (LA27_0==CLASS||LA27_0==DEF||LA27_0==FOR||LA27_0==IF||(LA27_0>=TRY && LA27_0<=WITH)||LA27_0==AT) ) {
                alt27=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:610:7: simple_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_simple_stmt_in_stmt1452);
                    simple_stmt58=simple_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_stmt58.getTree());
                    if ( state.backtracking==0 ) {

                                retval.stypes = (simple_stmt58!=null?simple_stmt58.stypes:null);
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:614:7: compound_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_compound_stmt_in_stmt1468);
                    compound_stmt59=compound_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, compound_stmt59.getTree());
                    if ( state.backtracking==0 ) {

                                retval.stypes = new ArrayList();
                                retval.stypes.add((compound_stmt59!=null?((PythonTree)compound_stmt59.tree):null));
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "stmt"

    public static class simple_stmt_return extends ParserRuleReturnScope {
        public List stypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "simple_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:622:1: simple_stmt returns [List stypes] : s+= small_stmt ( options {greedy=true; } : SEMI s+= small_stmt )* ( SEMI )? NEWLINE ;
    public final TruffleParser.simple_stmt_return simple_stmt() throws RecognitionException {
        TruffleParser.simple_stmt_return retval = new TruffleParser.simple_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token SEMI60=null;
        Token SEMI61=null;
        Token NEWLINE62=null;
        List list_s=null;
        TruffleParser.small_stmt_return s = null;
         s = null;
        PythonTree SEMI60_tree=null;
        PythonTree SEMI61_tree=null;
        PythonTree NEWLINE62_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:624:5: (s+= small_stmt ( options {greedy=true; } : SEMI s+= small_stmt )* ( SEMI )? NEWLINE )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:624:7: s+= small_stmt ( options {greedy=true; } : SEMI s+= small_stmt )* ( SEMI )? NEWLINE
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_small_stmt_in_simple_stmt1504);
            s=small_stmt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, s.getTree());
            if (list_s==null) list_s=new ArrayList();
            list_s.add(s.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:624:21: ( options {greedy=true; } : SEMI s+= small_stmt )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==SEMI) ) {
                    int LA28_1 = input.LA(2);

                    if ( (LA28_1==NAME||LA28_1==PRINT||(LA28_1>=ASSERT && LA28_1<=BREAK)||LA28_1==CONTINUE||LA28_1==DELETE||LA28_1==FROM||LA28_1==GLOBAL||LA28_1==IMPORT||(LA28_1>=LAMBDA && LA28_1<=NOT)||(LA28_1>=PASS && LA28_1<=RETURN)||(LA28_1>=YIELD && LA28_1<=NONLOCAL)||LA28_1==LPAREN||(LA28_1>=PLUS && LA28_1<=MINUS)||(LA28_1>=TILDE && LA28_1<=LBRACK)||LA28_1==LCURLY||(LA28_1>=BACKQUOTE && LA28_1<=STRING)) ) {
                        alt28=1;
                    }


                }


                switch (alt28) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:624:45: SEMI s+= small_stmt
            	    {
            	    SEMI60=(Token)match(input,SEMI,FOLLOW_SEMI_in_simple_stmt1514); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    SEMI60_tree = (PythonTree)adaptor.create(SEMI60);
            	    adaptor.addChild(root_0, SEMI60_tree);
            	    }
            	    pushFollow(FOLLOW_small_stmt_in_simple_stmt1518);
            	    s=small_stmt();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, s.getTree());
            	    if (list_s==null) list_s=new ArrayList();
            	    list_s.add(s.getTree());


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:624:66: ( SEMI )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==SEMI) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:624:67: SEMI
                    {
                    SEMI61=(Token)match(input,SEMI,FOLLOW_SEMI_in_simple_stmt1523); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SEMI61_tree = (PythonTree)adaptor.create(SEMI61);
                    adaptor.addChild(root_0, SEMI61_tree);
                    }

                    }
                    break;

            }

            NEWLINE62=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_simple_stmt1527); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            NEWLINE62_tree = (PythonTree)adaptor.create(NEWLINE62);
            adaptor.addChild(root_0, NEWLINE62_tree);
            }
            if ( state.backtracking==0 ) {

                        retval.stypes = list_s;
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "simple_stmt"

    public static class small_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "small_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:632:1: small_stmt : ( expr_stmt | del_stmt | pass_stmt | flow_stmt | import_stmt | global_stmt | assert_stmt | {...}? => print_stmt | nonlocal_stmt );
    public final TruffleParser.small_stmt_return small_stmt() throws RecognitionException {
        TruffleParser.small_stmt_return retval = new TruffleParser.small_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        TruffleParser.expr_stmt_return expr_stmt63 = null;

        TruffleParser.del_stmt_return del_stmt64 = null;

        TruffleParser.pass_stmt_return pass_stmt65 = null;

        TruffleParser.flow_stmt_return flow_stmt66 = null;

        TruffleParser.import_stmt_return import_stmt67 = null;

        TruffleParser.global_stmt_return global_stmt68 = null;

        TruffleParser.assert_stmt_return assert_stmt69 = null;

        TruffleParser.print_stmt_return print_stmt70 = null;

        TruffleParser.nonlocal_stmt_return nonlocal_stmt71 = null;



        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:632:12: ( expr_stmt | del_stmt | pass_stmt | flow_stmt | import_stmt | global_stmt | assert_stmt | {...}? => print_stmt | nonlocal_stmt )
            int alt30=9;
            alt30 = dfa30.predict(input);
            switch (alt30) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:632:14: expr_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_expr_stmt_in_small_stmt1550);
                    expr_stmt63=expr_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr_stmt63.getTree());

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:633:14: del_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_del_stmt_in_small_stmt1565);
                    del_stmt64=del_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, del_stmt64.getTree());

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:634:14: pass_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_pass_stmt_in_small_stmt1580);
                    pass_stmt65=pass_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, pass_stmt65.getTree());

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:635:14: flow_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_flow_stmt_in_small_stmt1595);
                    flow_stmt66=flow_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, flow_stmt66.getTree());

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:636:14: import_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_import_stmt_in_small_stmt1610);
                    import_stmt67=import_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, import_stmt67.getTree());

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:637:14: global_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_global_stmt_in_small_stmt1625);
                    global_stmt68=global_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, global_stmt68.getTree());

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:639:14: assert_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_assert_stmt_in_small_stmt1652);
                    assert_stmt69=assert_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assert_stmt69.getTree());

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:640:14: {...}? => print_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    if ( !((!printFunction)) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "small_stmt", "!printFunction");
                    }
                    pushFollow(FOLLOW_print_stmt_in_small_stmt1671);
                    print_stmt70=print_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, print_stmt70.getTree());

                    }
                    break;
                case 9 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:641:14: nonlocal_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_nonlocal_stmt_in_small_stmt1686);
                    nonlocal_stmt71=nonlocal_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nonlocal_stmt71.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "small_stmt"

    public static class nonlocal_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nonlocal_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:645:1: nonlocal_stmt : NONLOCAL n+= NAME ( options {k=2; } : COMMA n+= NAME )* ;
    public final TruffleParser.nonlocal_stmt_return nonlocal_stmt() throws RecognitionException {
        TruffleParser.nonlocal_stmt_return retval = new TruffleParser.nonlocal_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token NONLOCAL72=null;
        Token COMMA73=null;
        Token n=null;
        List list_n=null;

        PythonTree NONLOCAL72_tree=null;
        PythonTree COMMA73_tree=null;
        PythonTree n_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:652:5: ( NONLOCAL n+= NAME ( options {k=2; } : COMMA n+= NAME )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:652:7: NONLOCAL n+= NAME ( options {k=2; } : COMMA n+= NAME )*
            {
            root_0 = (PythonTree)adaptor.nil();

            NONLOCAL72=(Token)match(input,NONLOCAL,FOLLOW_NONLOCAL_in_nonlocal_stmt1721); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            NONLOCAL72_tree = (PythonTree)adaptor.create(NONLOCAL72);
            adaptor.addChild(root_0, NONLOCAL72_tree);
            }
            n=(Token)match(input,NAME,FOLLOW_NAME_in_nonlocal_stmt1725); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            n_tree = (PythonTree)adaptor.create(n);
            adaptor.addChild(root_0, n_tree);
            }
            if (list_n==null) list_n=new ArrayList();
            list_n.add(n);

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:652:24: ( options {k=2; } : COMMA n+= NAME )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==COMMA) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:652:41: COMMA n+= NAME
            	    {
            	    COMMA73=(Token)match(input,COMMA,FOLLOW_COMMA_in_nonlocal_stmt1736); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA73_tree = (PythonTree)adaptor.create(COMMA73);
            	    adaptor.addChild(root_0, COMMA73_tree);
            	    }
            	    n=(Token)match(input,NAME,FOLLOW_NAME_in_nonlocal_stmt1740); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    n_tree = (PythonTree)adaptor.create(n);
            	    adaptor.addChild(root_0, n_tree);
            	    }
            	    if (list_n==null) list_n=new ArrayList();
            	    list_n.add(n);


            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);

            if ( state.backtracking==0 ) {

                       stype = new Nonlocal(NONLOCAL72, actions.makeNames(list_n), actions.makeNameNodes(list_n));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nonlocal_stmt"

    public static class expr_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expr_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:660:1: expr_stmt : ( ( testlist[null] augassign )=>lhs= testlist[expr_contextType.AugStore] ( (aay= augassign y1= yield_expr ) | (aat= augassign rhs= testlist[expr_contextType.Load] ) ) | ( testlist[null] ASSIGN )=>lhs= testlist[expr_contextType.Store] ( | ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ ) | ( (ay= ASSIGN y2+= yield_expr )+ ) ) | lhs= testlist[expr_contextType.Load] ) ;
    public final TruffleParser.expr_stmt_return expr_stmt() throws RecognitionException {
        TruffleParser.expr_stmt_return retval = new TruffleParser.expr_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token at=null;
        Token ay=null;
        List list_t=null;
        List list_y2=null;
        TruffleParser.testlist_return lhs = null;

        TruffleParser.augassign_return aay = null;

        TruffleParser.yield_expr_return y1 = null;

        TruffleParser.augassign_return aat = null;

        TruffleParser.testlist_return rhs = null;

        TruffleParser.testlist_return t = null;
         t = null;
        TruffleParser.yield_expr_return y2 = null;
         y2 = null;
        PythonTree at_tree=null;
        PythonTree ay_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:669:5: ( ( ( testlist[null] augassign )=>lhs= testlist[expr_contextType.AugStore] ( (aay= augassign y1= yield_expr ) | (aat= augassign rhs= testlist[expr_contextType.Load] ) ) | ( testlist[null] ASSIGN )=>lhs= testlist[expr_contextType.Store] ( | ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ ) | ( (ay= ASSIGN y2+= yield_expr )+ ) ) | lhs= testlist[expr_contextType.Load] ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:669:7: ( ( testlist[null] augassign )=>lhs= testlist[expr_contextType.AugStore] ( (aay= augassign y1= yield_expr ) | (aat= augassign rhs= testlist[expr_contextType.Load] ) ) | ( testlist[null] ASSIGN )=>lhs= testlist[expr_contextType.Store] ( | ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ ) | ( (ay= ASSIGN y2+= yield_expr )+ ) ) | lhs= testlist[expr_contextType.Load] )
            {
            root_0 = (PythonTree)adaptor.nil();

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:669:7: ( ( testlist[null] augassign )=>lhs= testlist[expr_contextType.AugStore] ( (aay= augassign y1= yield_expr ) | (aat= augassign rhs= testlist[expr_contextType.Load] ) ) | ( testlist[null] ASSIGN )=>lhs= testlist[expr_contextType.Store] ( | ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ ) | ( (ay= ASSIGN y2+= yield_expr )+ ) ) | lhs= testlist[expr_contextType.Load] )
            int alt36=3;
            alt36 = dfa36.predict(input);
            switch (alt36) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:669:8: ( testlist[null] augassign )=>lhs= testlist[expr_contextType.AugStore] ( (aay= augassign y1= yield_expr ) | (aat= augassign rhs= testlist[expr_contextType.Load] ) )
                    {
                    pushFollow(FOLLOW_testlist_in_expr_stmt1791);
                    lhs=testlist(expr_contextType.AugStore);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, lhs.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:670:9: ( (aay= augassign y1= yield_expr ) | (aat= augassign rhs= testlist[expr_contextType.Load] ) )
                    int alt32=2;
                    alt32 = dfa32.predict(input);
                    switch (alt32) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:670:11: (aay= augassign y1= yield_expr )
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:670:11: (aay= augassign y1= yield_expr )
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:670:12: aay= augassign y1= yield_expr
                            {
                            pushFollow(FOLLOW_augassign_in_expr_stmt1807);
                            aay=augassign();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, aay.getTree());
                            pushFollow(FOLLOW_yield_expr_in_expr_stmt1811);
                            y1=yield_expr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, y1.getTree());
                            if ( state.backtracking==0 ) {

                                             actions.checkAugAssign(actions.castExpr((lhs!=null?((PythonTree)lhs.tree):null)));
                                             stype = new AugAssign((lhs!=null?((PythonTree)lhs.tree):null), actions.castExpr((lhs!=null?((PythonTree)lhs.tree):null)), (aay!=null?aay.op:null), actions.castExpr((y1!=null?y1.etype:null)));
                                         
                            }

                            }


                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:676:11: (aat= augassign rhs= testlist[expr_contextType.Load] )
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:676:11: (aat= augassign rhs= testlist[expr_contextType.Load] )
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:676:12: aat= augassign rhs= testlist[expr_contextType.Load]
                            {
                            pushFollow(FOLLOW_augassign_in_expr_stmt1851);
                            aat=augassign();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, aat.getTree());
                            pushFollow(FOLLOW_testlist_in_expr_stmt1855);
                            rhs=testlist(expr_contextType.Load);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, rhs.getTree());
                            if ( state.backtracking==0 ) {

                                             actions.checkAugAssign(actions.castExpr((lhs!=null?((PythonTree)lhs.tree):null)));
                                             stype = new AugAssign((lhs!=null?((PythonTree)lhs.tree):null), actions.castExpr((lhs!=null?((PythonTree)lhs.tree):null)), (aat!=null?aat.op:null), actions.castExpr((rhs!=null?((PythonTree)rhs.tree):null)));
                                         
                            }

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:683:7: ( testlist[null] ASSIGN )=>lhs= testlist[expr_contextType.Store] ( | ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ ) | ( (ay= ASSIGN y2+= yield_expr )+ ) )
                    {
                    pushFollow(FOLLOW_testlist_in_expr_stmt1910);
                    lhs=testlist(expr_contextType.Store);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, lhs.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:684:9: ( | ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ ) | ( (ay= ASSIGN y2+= yield_expr )+ ) )
                    int alt35=3;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==NEWLINE||LA35_0==SEMI) ) {
                        alt35=1;
                    }
                    else if ( (LA35_0==ASSIGN) ) {
                        int LA35_2 = input.LA(2);

                        if ( (LA35_2==YIELD) ) {
                            alt35=3;
                        }
                        else if ( (LA35_2==NAME||LA35_2==PRINT||(LA35_2>=LAMBDA && LA35_2<=NOT)||(LA35_2>=NONE && LA35_2<=FALSE)||LA35_2==LPAREN||(LA35_2>=PLUS && LA35_2<=MINUS)||(LA35_2>=TILDE && LA35_2<=LBRACK)||LA35_2==LCURLY||(LA35_2>=BACKQUOTE && LA35_2<=STRING)) ) {
                            alt35=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return retval;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 35, 2, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 35, 0, input);

                        throw nvae;
                    }
                    switch (alt35) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:685:9: 
                            {
                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:685:11: ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ )
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:685:11: ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ )
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:685:12: (at= ASSIGN t+= testlist[expr_contextType.Store] )+
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:685:12: (at= ASSIGN t+= testlist[expr_contextType.Store] )+
                            int cnt33=0;
                            loop33:
                            do {
                                int alt33=2;
                                int LA33_0 = input.LA(1);

                                if ( (LA33_0==ASSIGN) ) {
                                    alt33=1;
                                }


                                switch (alt33) {
                            	case 1 :
                            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:685:13: at= ASSIGN t+= testlist[expr_contextType.Store]
                            	    {
                            	    at=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_expr_stmt1937); if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) {
                            	    at_tree = (PythonTree)adaptor.create(at);
                            	    adaptor.addChild(root_0, at_tree);
                            	    }
                            	    pushFollow(FOLLOW_testlist_in_expr_stmt1941);
                            	    t=testlist(expr_contextType.Store);

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                            	    if (list_t==null) list_t=new ArrayList();
                            	    list_t.add(t.getTree());


                            	    }
                            	    break;

                            	default :
                            	    if ( cnt33 >= 1 ) break loop33;
                            	    if (state.backtracking>0) {state.failed=true; return retval;}
                                        EarlyExitException eee =
                                            new EarlyExitException(33, input);
                                        throw eee;
                                }
                                cnt33++;
                            } while (true);

                            if ( state.backtracking==0 ) {

                                              stype = new Assign((lhs!=null?((PythonTree)lhs.tree):null), actions.makeAssignTargets(
                                                  actions.castExpr((lhs!=null?((PythonTree)lhs.tree):null)), list_t), actions.makeAssignValue(list_t));
                                          
                            }

                            }


                            }
                            break;
                        case 3 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:691:11: ( (ay= ASSIGN y2+= yield_expr )+ )
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:691:11: ( (ay= ASSIGN y2+= yield_expr )+ )
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:691:12: (ay= ASSIGN y2+= yield_expr )+
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:691:12: (ay= ASSIGN y2+= yield_expr )+
                            int cnt34=0;
                            loop34:
                            do {
                                int alt34=2;
                                int LA34_0 = input.LA(1);

                                if ( (LA34_0==ASSIGN) ) {
                                    alt34=1;
                                }


                                switch (alt34) {
                            	case 1 :
                            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:691:13: ay= ASSIGN y2+= yield_expr
                            	    {
                            	    ay=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_expr_stmt1986); if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) {
                            	    ay_tree = (PythonTree)adaptor.create(ay);
                            	    adaptor.addChild(root_0, ay_tree);
                            	    }
                            	    pushFollow(FOLLOW_yield_expr_in_expr_stmt1990);
                            	    y2=yield_expr();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, y2.getTree());
                            	    if (list_y2==null) list_y2=new ArrayList();
                            	    list_y2.add(y2.getTree());


                            	    }
                            	    break;

                            	default :
                            	    if ( cnt34 >= 1 ) break loop34;
                            	    if (state.backtracking>0) {state.failed=true; return retval;}
                                        EarlyExitException eee =
                                            new EarlyExitException(34, input);
                                        throw eee;
                                }
                                cnt34++;
                            } while (true);

                            if ( state.backtracking==0 ) {

                                              stype = new Assign((lhs!=null?((Token)lhs.start):null), actions.makeAssignTargets(
                                                  actions.castExpr((lhs!=null?((PythonTree)lhs.tree):null)), list_y2), actions.makeAssignValue(list_y2));
                                          
                            }

                            }


                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:698:7: lhs= testlist[expr_contextType.Load]
                    {
                    pushFollow(FOLLOW_testlist_in_expr_stmt2038);
                    lhs=testlist(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, lhs.getTree());
                    if ( state.backtracking==0 ) {

                                stype = new Expr((lhs!=null?((Token)lhs.start):null), actions.castExpr((lhs!=null?((PythonTree)lhs.tree):null)));
                            
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (stype != null) {
                      retval.tree = stype;
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expr_stmt"

    public static class augassign_return extends ParserRuleReturnScope {
        public operatorType op;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "augassign"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:707:1: augassign returns [operatorType op] : ( PLUSEQUAL | MINUSEQUAL | STAREQUAL | SLASHEQUAL | PERCENTEQUAL | AMPEREQUAL | VBAREQUAL | CIRCUMFLEXEQUAL | LEFTSHIFTEQUAL | RIGHTSHIFTEQUAL | DOUBLESTAREQUAL | DOUBLESLASHEQUAL );
    public final TruffleParser.augassign_return augassign() throws RecognitionException {
        TruffleParser.augassign_return retval = new TruffleParser.augassign_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token PLUSEQUAL74=null;
        Token MINUSEQUAL75=null;
        Token STAREQUAL76=null;
        Token SLASHEQUAL77=null;
        Token PERCENTEQUAL78=null;
        Token AMPEREQUAL79=null;
        Token VBAREQUAL80=null;
        Token CIRCUMFLEXEQUAL81=null;
        Token LEFTSHIFTEQUAL82=null;
        Token RIGHTSHIFTEQUAL83=null;
        Token DOUBLESTAREQUAL84=null;
        Token DOUBLESLASHEQUAL85=null;

        PythonTree PLUSEQUAL74_tree=null;
        PythonTree MINUSEQUAL75_tree=null;
        PythonTree STAREQUAL76_tree=null;
        PythonTree SLASHEQUAL77_tree=null;
        PythonTree PERCENTEQUAL78_tree=null;
        PythonTree AMPEREQUAL79_tree=null;
        PythonTree VBAREQUAL80_tree=null;
        PythonTree CIRCUMFLEXEQUAL81_tree=null;
        PythonTree LEFTSHIFTEQUAL82_tree=null;
        PythonTree RIGHTSHIFTEQUAL83_tree=null;
        PythonTree DOUBLESTAREQUAL84_tree=null;
        PythonTree DOUBLESLASHEQUAL85_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:709:5: ( PLUSEQUAL | MINUSEQUAL | STAREQUAL | SLASHEQUAL | PERCENTEQUAL | AMPEREQUAL | VBAREQUAL | CIRCUMFLEXEQUAL | LEFTSHIFTEQUAL | RIGHTSHIFTEQUAL | DOUBLESTAREQUAL | DOUBLESLASHEQUAL )
            int alt37=12;
            switch ( input.LA(1) ) {
            case PLUSEQUAL:
                {
                alt37=1;
                }
                break;
            case MINUSEQUAL:
                {
                alt37=2;
                }
                break;
            case STAREQUAL:
                {
                alt37=3;
                }
                break;
            case SLASHEQUAL:
                {
                alt37=4;
                }
                break;
            case PERCENTEQUAL:
                {
                alt37=5;
                }
                break;
            case AMPEREQUAL:
                {
                alt37=6;
                }
                break;
            case VBAREQUAL:
                {
                alt37=7;
                }
                break;
            case CIRCUMFLEXEQUAL:
                {
                alt37=8;
                }
                break;
            case LEFTSHIFTEQUAL:
                {
                alt37=9;
                }
                break;
            case RIGHTSHIFTEQUAL:
                {
                alt37=10;
                }
                break;
            case DOUBLESTAREQUAL:
                {
                alt37=11;
                }
                break;
            case DOUBLESLASHEQUAL:
                {
                alt37=12;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }

            switch (alt37) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:709:7: PLUSEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    PLUSEQUAL74=(Token)match(input,PLUSEQUAL,FOLLOW_PLUSEQUAL_in_augassign2080); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PLUSEQUAL74_tree = (PythonTree)adaptor.create(PLUSEQUAL74);
                    adaptor.addChild(root_0, PLUSEQUAL74_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.Add;
                              
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:713:7: MINUSEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    MINUSEQUAL75=(Token)match(input,MINUSEQUAL,FOLLOW_MINUSEQUAL_in_augassign2098); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINUSEQUAL75_tree = (PythonTree)adaptor.create(MINUSEQUAL75);
                    adaptor.addChild(root_0, MINUSEQUAL75_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.Sub;
                              
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:717:7: STAREQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    STAREQUAL76=(Token)match(input,STAREQUAL,FOLLOW_STAREQUAL_in_augassign2116); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STAREQUAL76_tree = (PythonTree)adaptor.create(STAREQUAL76);
                    adaptor.addChild(root_0, STAREQUAL76_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.Mult;
                              
                    }

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:721:7: SLASHEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    SLASHEQUAL77=(Token)match(input,SLASHEQUAL,FOLLOW_SLASHEQUAL_in_augassign2134); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SLASHEQUAL77_tree = (PythonTree)adaptor.create(SLASHEQUAL77);
                    adaptor.addChild(root_0, SLASHEQUAL77_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.Div;
                              
                    }

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:725:7: PERCENTEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    PERCENTEQUAL78=(Token)match(input,PERCENTEQUAL,FOLLOW_PERCENTEQUAL_in_augassign2152); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PERCENTEQUAL78_tree = (PythonTree)adaptor.create(PERCENTEQUAL78);
                    adaptor.addChild(root_0, PERCENTEQUAL78_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.Mod;
                              
                    }

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:729:7: AMPEREQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    AMPEREQUAL79=(Token)match(input,AMPEREQUAL,FOLLOW_AMPEREQUAL_in_augassign2170); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AMPEREQUAL79_tree = (PythonTree)adaptor.create(AMPEREQUAL79);
                    adaptor.addChild(root_0, AMPEREQUAL79_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.BitAnd;
                              
                    }

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:733:7: VBAREQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    VBAREQUAL80=(Token)match(input,VBAREQUAL,FOLLOW_VBAREQUAL_in_augassign2188); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    VBAREQUAL80_tree = (PythonTree)adaptor.create(VBAREQUAL80);
                    adaptor.addChild(root_0, VBAREQUAL80_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.BitOr;
                              
                    }

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:737:7: CIRCUMFLEXEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    CIRCUMFLEXEQUAL81=(Token)match(input,CIRCUMFLEXEQUAL,FOLLOW_CIRCUMFLEXEQUAL_in_augassign2206); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    CIRCUMFLEXEQUAL81_tree = (PythonTree)adaptor.create(CIRCUMFLEXEQUAL81);
                    adaptor.addChild(root_0, CIRCUMFLEXEQUAL81_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.BitXor;
                              
                    }

                    }
                    break;
                case 9 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:741:7: LEFTSHIFTEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    LEFTSHIFTEQUAL82=(Token)match(input,LEFTSHIFTEQUAL,FOLLOW_LEFTSHIFTEQUAL_in_augassign2224); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFTSHIFTEQUAL82_tree = (PythonTree)adaptor.create(LEFTSHIFTEQUAL82);
                    adaptor.addChild(root_0, LEFTSHIFTEQUAL82_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.LShift;
                              
                    }

                    }
                    break;
                case 10 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:745:7: RIGHTSHIFTEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    RIGHTSHIFTEQUAL83=(Token)match(input,RIGHTSHIFTEQUAL,FOLLOW_RIGHTSHIFTEQUAL_in_augassign2242); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHTSHIFTEQUAL83_tree = (PythonTree)adaptor.create(RIGHTSHIFTEQUAL83);
                    adaptor.addChild(root_0, RIGHTSHIFTEQUAL83_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.RShift;
                              
                    }

                    }
                    break;
                case 11 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:749:7: DOUBLESTAREQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    DOUBLESTAREQUAL84=(Token)match(input,DOUBLESTAREQUAL,FOLLOW_DOUBLESTAREQUAL_in_augassign2260); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOUBLESTAREQUAL84_tree = (PythonTree)adaptor.create(DOUBLESTAREQUAL84);
                    adaptor.addChild(root_0, DOUBLESTAREQUAL84_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.Pow;
                              
                    }

                    }
                    break;
                case 12 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:753:7: DOUBLESLASHEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    DOUBLESLASHEQUAL85=(Token)match(input,DOUBLESLASHEQUAL,FOLLOW_DOUBLESLASHEQUAL_in_augassign2278); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOUBLESLASHEQUAL85_tree = (PythonTree)adaptor.create(DOUBLESLASHEQUAL85);
                    adaptor.addChild(root_0, DOUBLESLASHEQUAL85_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  retval.op = operatorType.FloorDiv;
                              
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "augassign"

    public static class print_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "print_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:761:1: print_stmt : PRINT (t1= printlist | RIGHTSHIFT t2= printlist2 | ) ;
    public final TruffleParser.print_stmt_return print_stmt() throws RecognitionException {
        TruffleParser.print_stmt_return retval = new TruffleParser.print_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token PRINT86=null;
        Token RIGHTSHIFT87=null;
        TruffleParser.printlist_return t1 = null;

        TruffleParser.printlist2_return t2 = null;


        PythonTree PRINT86_tree=null;
        PythonTree RIGHTSHIFT87_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:769:5: ( PRINT (t1= printlist | RIGHTSHIFT t2= printlist2 | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:769:7: PRINT (t1= printlist | RIGHTSHIFT t2= printlist2 | )
            {
            root_0 = (PythonTree)adaptor.nil();

            PRINT86=(Token)match(input,PRINT,FOLLOW_PRINT_in_print_stmt2318); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            PRINT86_tree = (PythonTree)adaptor.create(PRINT86);
            adaptor.addChild(root_0, PRINT86_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:770:7: (t1= printlist | RIGHTSHIFT t2= printlist2 | )
            int alt38=3;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==NAME||LA38_0==NOT||LA38_0==LPAREN||(LA38_0>=PLUS && LA38_0<=MINUS)||(LA38_0>=TILDE && LA38_0<=LBRACK)||LA38_0==LCURLY||LA38_0==BACKQUOTE) ) {
                alt38=1;
            }
            else if ( (LA38_0==PRINT) && ((printFunction))) {
                alt38=1;
            }
            else if ( (LA38_0==LAMBDA||(LA38_0>=NONE && LA38_0<=FALSE)||(LA38_0>=INT && LA38_0<=STRING)) ) {
                alt38=1;
            }
            else if ( (LA38_0==RIGHTSHIFT) ) {
                alt38=2;
            }
            else if ( (LA38_0==NEWLINE||LA38_0==SEMI) ) {
                alt38=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }
            switch (alt38) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:770:8: t1= printlist
                    {
                    pushFollow(FOLLOW_printlist_in_print_stmt2329);
                    t1=printlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t1.getTree());
                    if ( state.backtracking==0 ) {

                                 stype = new Print(PRINT86, null, actions.castExprs((t1!=null?t1.elts:null)), (t1!=null?t1.newline:false));
                             
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:774:9: RIGHTSHIFT t2= printlist2
                    {
                    RIGHTSHIFT87=(Token)match(input,RIGHTSHIFT,FOLLOW_RIGHTSHIFT_in_print_stmt2348); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHTSHIFT87_tree = (PythonTree)adaptor.create(RIGHTSHIFT87);
                    adaptor.addChild(root_0, RIGHTSHIFT87_tree);
                    }
                    pushFollow(FOLLOW_printlist2_in_print_stmt2352);
                    t2=printlist2();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t2.getTree());
                    if ( state.backtracking==0 ) {

                                 stype = new Print(PRINT86, actions.castExpr((t2!=null?t2.elts:null).get(0)), actions.castExprs((t2!=null?t2.elts:null), 1), (t2!=null?t2.newline:false));
                             
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:779:8: 
                    {
                    if ( state.backtracking==0 ) {

                                 stype = new Print(PRINT86, null, new ArrayList<expr>(), true);
                             
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "print_stmt"

    public static class printlist_return extends ParserRuleReturnScope {
        public boolean newline;
        public List elts;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "printlist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:786:1: printlist returns [boolean newline, List elts] : ( ( test[null] COMMA )=>t+= test[expr_contextType.Load] ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )* (trailcomma= COMMA )? | t+= test[expr_contextType.Load] );
    public final TruffleParser.printlist_return printlist() throws RecognitionException {
        TruffleParser.printlist_return retval = new TruffleParser.printlist_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token trailcomma=null;
        Token COMMA88=null;
        List list_t=null;
        TruffleParser.test_return t = null;
         t = null;
        PythonTree trailcomma_tree=null;
        PythonTree COMMA88_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:788:5: ( ( test[null] COMMA )=>t+= test[expr_contextType.Load] ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )* (trailcomma= COMMA )? | t+= test[expr_contextType.Load] )
            int alt41=2;
            alt41 = dfa41.predict(input);
            switch (alt41) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:788:7: ( test[null] COMMA )=>t+= test[expr_contextType.Load] ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )* (trailcomma= COMMA )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_test_in_printlist2432);
                    t=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    if (list_t==null) list_t=new ArrayList();
                    list_t.add(t.getTree());

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:789:39: ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )*
                    loop39:
                    do {
                        int alt39=2;
                        alt39 = dfa39.predict(input);
                        switch (alt39) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:789:56: COMMA t+= test[expr_contextType.Load]
                    	    {
                    	    COMMA88=(Token)match(input,COMMA,FOLLOW_COMMA_in_printlist2444); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA88_tree = (PythonTree)adaptor.create(COMMA88);
                    	    adaptor.addChild(root_0, COMMA88_tree);
                    	    }
                    	    pushFollow(FOLLOW_test_in_printlist2448);
                    	    t=test(expr_contextType.Load);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    	    if (list_t==null) list_t=new ArrayList();
                    	    list_t.add(t.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop39;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:789:95: (trailcomma= COMMA )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0==COMMA) ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:789:96: trailcomma= COMMA
                            {
                            trailcomma=(Token)match(input,COMMA,FOLLOW_COMMA_in_printlist2456); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            trailcomma_tree = (PythonTree)adaptor.create(trailcomma);
                            adaptor.addChild(root_0, trailcomma_tree);
                            }

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                 retval.elts =list_t;
                                 if (trailcomma == null) {
                                     retval.newline = true;
                                 } else {
                                     retval.newline = false;
                                 }
                             
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:798:7: t+= test[expr_contextType.Load]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_test_in_printlist2477);
                    t=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    if (list_t==null) list_t=new ArrayList();
                    list_t.add(t.getTree());

                    if ( state.backtracking==0 ) {

                                retval.elts =list_t;
                                retval.newline = true;
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "printlist"

    public static class printlist2_return extends ParserRuleReturnScope {
        public boolean newline;
        public List elts;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "printlist2"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:807:1: printlist2 returns [boolean newline, List elts] : ( ( test[null] COMMA test[null] )=>t+= test[expr_contextType.Load] ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )* (trailcomma= COMMA )? | t+= test[expr_contextType.Load] );
    public final TruffleParser.printlist2_return printlist2() throws RecognitionException {
        TruffleParser.printlist2_return retval = new TruffleParser.printlist2_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token trailcomma=null;
        Token COMMA89=null;
        List list_t=null;
        TruffleParser.test_return t = null;
         t = null;
        PythonTree trailcomma_tree=null;
        PythonTree COMMA89_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:809:5: ( ( test[null] COMMA test[null] )=>t+= test[expr_contextType.Load] ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )* (trailcomma= COMMA )? | t+= test[expr_contextType.Load] )
            int alt44=2;
            alt44 = dfa44.predict(input);
            switch (alt44) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:809:7: ( test[null] COMMA test[null] )=>t+= test[expr_contextType.Load] ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )* (trailcomma= COMMA )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_test_in_printlist22534);
                    t=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    if (list_t==null) list_t=new ArrayList();
                    list_t.add(t.getTree());

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:810:39: ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )*
                    loop42:
                    do {
                        int alt42=2;
                        alt42 = dfa42.predict(input);
                        switch (alt42) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:810:56: COMMA t+= test[expr_contextType.Load]
                    	    {
                    	    COMMA89=(Token)match(input,COMMA,FOLLOW_COMMA_in_printlist22546); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA89_tree = (PythonTree)adaptor.create(COMMA89);
                    	    adaptor.addChild(root_0, COMMA89_tree);
                    	    }
                    	    pushFollow(FOLLOW_test_in_printlist22550);
                    	    t=test(expr_contextType.Load);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    	    if (list_t==null) list_t=new ArrayList();
                    	    list_t.add(t.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop42;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:810:95: (trailcomma= COMMA )?
                    int alt43=2;
                    int LA43_0 = input.LA(1);

                    if ( (LA43_0==COMMA) ) {
                        alt43=1;
                    }
                    switch (alt43) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:810:96: trailcomma= COMMA
                            {
                            trailcomma=(Token)match(input,COMMA,FOLLOW_COMMA_in_printlist22558); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            trailcomma_tree = (PythonTree)adaptor.create(trailcomma);
                            adaptor.addChild(root_0, trailcomma_tree);
                            }

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {
                       retval.elts =list_t;
                                 if (trailcomma == null) {
                                     retval.newline = true;
                                 } else {
                                     retval.newline = false;
                                 }
                             
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:818:7: t+= test[expr_contextType.Load]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_test_in_printlist22579);
                    t=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    if (list_t==null) list_t=new ArrayList();
                    list_t.add(t.getTree());

                    if ( state.backtracking==0 ) {

                                retval.elts =list_t;
                                retval.newline = true;
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "printlist2"

    public static class del_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "del_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:826:1: del_stmt : DELETE del_list ;
    public final TruffleParser.del_stmt_return del_stmt() throws RecognitionException {
        TruffleParser.del_stmt_return retval = new TruffleParser.del_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token DELETE90=null;
        TruffleParser.del_list_return del_list91 = null;


        PythonTree DELETE90_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:833:5: ( DELETE del_list )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:833:7: DELETE del_list
            {
            root_0 = (PythonTree)adaptor.nil();

            DELETE90=(Token)match(input,DELETE,FOLLOW_DELETE_in_del_stmt2616); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DELETE90_tree = (PythonTree)adaptor.create(DELETE90);
            adaptor.addChild(root_0, DELETE90_tree);
            }
            pushFollow(FOLLOW_del_list_in_del_stmt2618);
            del_list91=del_list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, del_list91.getTree());
            if ( state.backtracking==0 ) {

                        stype = new Delete(DELETE90, (del_list91!=null?del_list91.etypes:null));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "del_stmt"

    public static class pass_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "pass_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:840:1: pass_stmt : PASS ;
    public final TruffleParser.pass_stmt_return pass_stmt() throws RecognitionException {
        TruffleParser.pass_stmt_return retval = new TruffleParser.pass_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token PASS92=null;

        PythonTree PASS92_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:847:5: ( PASS )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:847:7: PASS
            {
            root_0 = (PythonTree)adaptor.nil();

            PASS92=(Token)match(input,PASS,FOLLOW_PASS_in_pass_stmt2654); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            PASS92_tree = (PythonTree)adaptor.create(PASS92);
            adaptor.addChild(root_0, PASS92_tree);
            }
            if ( state.backtracking==0 ) {

                        stype = new Pass(PASS92);
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "pass_stmt"

    public static class flow_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "flow_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:854:1: flow_stmt : ( break_stmt | continue_stmt | return_stmt | raise_stmt | yield_stmt );
    public final TruffleParser.flow_stmt_return flow_stmt() throws RecognitionException {
        TruffleParser.flow_stmt_return retval = new TruffleParser.flow_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        TruffleParser.break_stmt_return break_stmt93 = null;

        TruffleParser.continue_stmt_return continue_stmt94 = null;

        TruffleParser.return_stmt_return return_stmt95 = null;

        TruffleParser.raise_stmt_return raise_stmt96 = null;

        TruffleParser.yield_stmt_return yield_stmt97 = null;



        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:855:5: ( break_stmt | continue_stmt | return_stmt | raise_stmt | yield_stmt )
            int alt45=5;
            switch ( input.LA(1) ) {
            case BREAK:
                {
                alt45=1;
                }
                break;
            case CONTINUE:
                {
                alt45=2;
                }
                break;
            case RETURN:
                {
                alt45=3;
                }
                break;
            case RAISE:
                {
                alt45=4;
                }
                break;
            case YIELD:
                {
                alt45=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }

            switch (alt45) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:855:7: break_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_break_stmt_in_flow_stmt2680);
                    break_stmt93=break_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, break_stmt93.getTree());

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:856:7: continue_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_continue_stmt_in_flow_stmt2688);
                    continue_stmt94=continue_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, continue_stmt94.getTree());

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:857:7: return_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_return_stmt_in_flow_stmt2696);
                    return_stmt95=return_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, return_stmt95.getTree());

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:858:7: raise_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_raise_stmt_in_flow_stmt2704);
                    raise_stmt96=raise_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, raise_stmt96.getTree());

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:859:7: yield_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_yield_stmt_in_flow_stmt2712);
                    yield_stmt97=yield_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, yield_stmt97.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "flow_stmt"

    public static class break_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "break_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:863:1: break_stmt : BREAK ;
    public final TruffleParser.break_stmt_return break_stmt() throws RecognitionException {
        TruffleParser.break_stmt_return retval = new TruffleParser.break_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token BREAK98=null;

        PythonTree BREAK98_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:870:5: ( BREAK )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:870:7: BREAK
            {
            root_0 = (PythonTree)adaptor.nil();

            BREAK98=(Token)match(input,BREAK,FOLLOW_BREAK_in_break_stmt2740); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            BREAK98_tree = (PythonTree)adaptor.create(BREAK98);
            adaptor.addChild(root_0, BREAK98_tree);
            }
            if ( state.backtracking==0 ) {

                        stype = new Break(BREAK98);
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "break_stmt"

    public static class continue_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "continue_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:877:1: continue_stmt : CONTINUE ;
    public final TruffleParser.continue_stmt_return continue_stmt() throws RecognitionException {
        TruffleParser.continue_stmt_return retval = new TruffleParser.continue_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token CONTINUE99=null;

        PythonTree CONTINUE99_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:884:5: ( CONTINUE )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:884:7: CONTINUE
            {
            root_0 = (PythonTree)adaptor.nil();

            CONTINUE99=(Token)match(input,CONTINUE,FOLLOW_CONTINUE_in_continue_stmt2776); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CONTINUE99_tree = (PythonTree)adaptor.create(CONTINUE99);
            adaptor.addChild(root_0, CONTINUE99_tree);
            }
            if ( state.backtracking==0 ) {

                        if (!suite_stack.isEmpty() && ((suite_scope)suite_stack.peek()).continueIllegal) {
                            errorHandler.error("'continue' not supported inside 'finally' clause", new PythonTree(((Token)retval.start)));
                        }
                        stype = new Continue(CONTINUE99);
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "continue_stmt"

    public static class return_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "return_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:894:1: return_stmt : RETURN ( testlist[expr_contextType.Load] | ) ;
    public final TruffleParser.return_stmt_return return_stmt() throws RecognitionException {
        TruffleParser.return_stmt_return retval = new TruffleParser.return_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token RETURN100=null;
        TruffleParser.testlist_return testlist101 = null;


        PythonTree RETURN100_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:901:5: ( RETURN ( testlist[expr_contextType.Load] | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:901:7: RETURN ( testlist[expr_contextType.Load] | )
            {
            root_0 = (PythonTree)adaptor.nil();

            RETURN100=(Token)match(input,RETURN,FOLLOW_RETURN_in_return_stmt2812); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RETURN100_tree = (PythonTree)adaptor.create(RETURN100);
            adaptor.addChild(root_0, RETURN100_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:902:7: ( testlist[expr_contextType.Load] | )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==NAME||LA46_0==NOT||LA46_0==LPAREN||(LA46_0>=PLUS && LA46_0<=MINUS)||(LA46_0>=TILDE && LA46_0<=LBRACK)||LA46_0==LCURLY||LA46_0==BACKQUOTE) ) {
                alt46=1;
            }
            else if ( (LA46_0==PRINT) && ((printFunction))) {
                alt46=1;
            }
            else if ( (LA46_0==LAMBDA||(LA46_0>=NONE && LA46_0<=FALSE)||(LA46_0>=INT && LA46_0<=STRING)) ) {
                alt46=1;
            }
            else if ( (LA46_0==NEWLINE||LA46_0==SEMI) ) {
                alt46=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:902:8: testlist[expr_contextType.Load]
                    {
                    pushFollow(FOLLOW_testlist_in_return_stmt2821);
                    testlist101=testlist(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, testlist101.getTree());
                    if ( state.backtracking==0 ) {

                                 stype = new Return(RETURN100, actions.castExpr((testlist101!=null?((PythonTree)testlist101.tree):null)));
                             
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:907:8: 
                    {
                    if ( state.backtracking==0 ) {

                                 stype = new Return(RETURN100, null);
                             
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "return_stmt"

    public static class yield_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "yield_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:914:1: yield_stmt : yield_expr ;
    public final TruffleParser.yield_stmt_return yield_stmt() throws RecognitionException {
        TruffleParser.yield_stmt_return retval = new TruffleParser.yield_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        TruffleParser.yield_expr_return yield_expr102 = null;




            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:921:5: ( yield_expr )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:921:7: yield_expr
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_yield_expr_in_yield_stmt2886);
            yield_expr102=yield_expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, yield_expr102.getTree());
            if ( state.backtracking==0 ) {

                      stype = new Expr((yield_expr102!=null?((Token)yield_expr102.start):null), actions.castExpr((yield_expr102!=null?yield_expr102.etype:null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "yield_stmt"

    public static class raise_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "raise_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:928:1: raise_stmt : RAISE (t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] ( COMMA t3= test[expr_contextType.Load] )? )? )? ;
    public final TruffleParser.raise_stmt_return raise_stmt() throws RecognitionException {
        TruffleParser.raise_stmt_return retval = new TruffleParser.raise_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token RAISE103=null;
        Token COMMA104=null;
        Token COMMA105=null;
        TruffleParser.test_return t1 = null;

        TruffleParser.test_return t2 = null;

        TruffleParser.test_return t3 = null;


        PythonTree RAISE103_tree=null;
        PythonTree COMMA104_tree=null;
        PythonTree COMMA105_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:935:5: ( RAISE (t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] ( COMMA t3= test[expr_contextType.Load] )? )? )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:935:7: RAISE (t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] ( COMMA t3= test[expr_contextType.Load] )? )? )?
            {
            root_0 = (PythonTree)adaptor.nil();

            RAISE103=(Token)match(input,RAISE,FOLLOW_RAISE_in_raise_stmt2922); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RAISE103_tree = (PythonTree)adaptor.create(RAISE103);
            adaptor.addChild(root_0, RAISE103_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:935:13: (t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] ( COMMA t3= test[expr_contextType.Load] )? )? )?
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==NAME||LA49_0==NOT||LA49_0==LPAREN||(LA49_0>=PLUS && LA49_0<=MINUS)||(LA49_0>=TILDE && LA49_0<=LBRACK)||LA49_0==LCURLY||LA49_0==BACKQUOTE) ) {
                alt49=1;
            }
            else if ( (LA49_0==PRINT) && ((printFunction))) {
                alt49=1;
            }
            else if ( (LA49_0==LAMBDA||(LA49_0>=NONE && LA49_0<=FALSE)||(LA49_0>=INT && LA49_0<=STRING)) ) {
                alt49=1;
            }
            switch (alt49) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:935:14: t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] ( COMMA t3= test[expr_contextType.Load] )? )?
                    {
                    pushFollow(FOLLOW_test_in_raise_stmt2927);
                    t1=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t1.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:935:45: ( COMMA t2= test[expr_contextType.Load] ( COMMA t3= test[expr_contextType.Load] )? )?
                    int alt48=2;
                    int LA48_0 = input.LA(1);

                    if ( (LA48_0==COMMA) ) {
                        alt48=1;
                    }
                    switch (alt48) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:935:46: COMMA t2= test[expr_contextType.Load] ( COMMA t3= test[expr_contextType.Load] )?
                            {
                            COMMA104=(Token)match(input,COMMA,FOLLOW_COMMA_in_raise_stmt2931); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA104_tree = (PythonTree)adaptor.create(COMMA104);
                            adaptor.addChild(root_0, COMMA104_tree);
                            }
                            pushFollow(FOLLOW_test_in_raise_stmt2935);
                            t2=test(expr_contextType.Load);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, t2.getTree());
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:936:9: ( COMMA t3= test[expr_contextType.Load] )?
                            int alt47=2;
                            int LA47_0 = input.LA(1);

                            if ( (LA47_0==COMMA) ) {
                                alt47=1;
                            }
                            switch (alt47) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:936:10: COMMA t3= test[expr_contextType.Load]
                                    {
                                    COMMA105=(Token)match(input,COMMA,FOLLOW_COMMA_in_raise_stmt2947); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    COMMA105_tree = (PythonTree)adaptor.create(COMMA105);
                                    adaptor.addChild(root_0, COMMA105_tree);
                                    }
                                    pushFollow(FOLLOW_test_in_raise_stmt2951);
                                    t3=test(expr_contextType.Load);

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t3.getTree());

                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        stype = new Raise(RAISE103, actions.castExpr((t1!=null?((PythonTree)t1.tree):null)), actions.castExpr((t2!=null?((PythonTree)t2.tree):null)), actions.castExpr((t3!=null?((PythonTree)t3.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "raise_stmt"

    public static class import_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "import_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:943:1: import_stmt : ( import_name | import_from );
    public final TruffleParser.import_stmt_return import_stmt() throws RecognitionException {
        TruffleParser.import_stmt_return retval = new TruffleParser.import_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        TruffleParser.import_name_return import_name106 = null;

        TruffleParser.import_from_return import_from107 = null;



        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:944:5: ( import_name | import_from )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==IMPORT) ) {
                alt50=1;
            }
            else if ( (LA50_0==FROM) ) {
                alt50=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:944:7: import_name
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_import_name_in_import_stmt2984);
                    import_name106=import_name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, import_name106.getTree());

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:945:7: import_from
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_import_from_in_import_stmt2992);
                    import_from107=import_from();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, import_from107.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "import_stmt"

    public static class import_name_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "import_name"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:949:1: import_name : IMPORT dotted_as_names ;
    public final TruffleParser.import_name_return import_name() throws RecognitionException {
        TruffleParser.import_name_return retval = new TruffleParser.import_name_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token IMPORT108=null;
        TruffleParser.dotted_as_names_return dotted_as_names109 = null;


        PythonTree IMPORT108_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:956:5: ( IMPORT dotted_as_names )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:956:7: IMPORT dotted_as_names
            {
            root_0 = (PythonTree)adaptor.nil();

            IMPORT108=(Token)match(input,IMPORT,FOLLOW_IMPORT_in_import_name3020); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IMPORT108_tree = (PythonTree)adaptor.create(IMPORT108);
            adaptor.addChild(root_0, IMPORT108_tree);
            }
            pushFollow(FOLLOW_dotted_as_names_in_import_name3022);
            dotted_as_names109=dotted_as_names();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dotted_as_names109.getTree());
            if ( state.backtracking==0 ) {

                        stype = new Import(IMPORT108, (dotted_as_names109!=null?dotted_as_names109.atypes:null));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "import_name"

    public static class import_from_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "import_from"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:964:1: import_from : FROM ( (d+= DOT )* dotted_name | (d+= DOT )+ ) IMPORT ( STAR | i1= import_as_names | LPAREN i2= import_as_names ( COMMA )? RPAREN ) ;
    public final TruffleParser.import_from_return import_from() throws RecognitionException {
        TruffleParser.import_from_return retval = new TruffleParser.import_from_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token FROM110=null;
        Token IMPORT112=null;
        Token STAR113=null;
        Token LPAREN114=null;
        Token COMMA115=null;
        Token RPAREN116=null;
        Token d=null;
        List list_d=null;
        TruffleParser.import_as_names_return i1 = null;

        TruffleParser.import_as_names_return i2 = null;

        TruffleParser.dotted_name_return dotted_name111 = null;


        PythonTree FROM110_tree=null;
        PythonTree IMPORT112_tree=null;
        PythonTree STAR113_tree=null;
        PythonTree LPAREN114_tree=null;
        PythonTree COMMA115_tree=null;
        PythonTree RPAREN116_tree=null;
        PythonTree d_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:5: ( FROM ( (d+= DOT )* dotted_name | (d+= DOT )+ ) IMPORT ( STAR | i1= import_as_names | LPAREN i2= import_as_names ( COMMA )? RPAREN ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:7: FROM ( (d+= DOT )* dotted_name | (d+= DOT )+ ) IMPORT ( STAR | i1= import_as_names | LPAREN i2= import_as_names ( COMMA )? RPAREN )
            {
            root_0 = (PythonTree)adaptor.nil();

            FROM110=(Token)match(input,FROM,FOLLOW_FROM_in_import_from3059); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FROM110_tree = (PythonTree)adaptor.create(FROM110);
            adaptor.addChild(root_0, FROM110_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:12: ( (d+= DOT )* dotted_name | (d+= DOT )+ )
            int alt53=2;
            alt53 = dfa53.predict(input);
            switch (alt53) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:13: (d+= DOT )* dotted_name
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:14: (d+= DOT )*
                    loop51:
                    do {
                        int alt51=2;
                        int LA51_0 = input.LA(1);

                        if ( (LA51_0==DOT) ) {
                            alt51=1;
                        }


                        switch (alt51) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:14: d+= DOT
                    	    {
                    	    d=(Token)match(input,DOT,FOLLOW_DOT_in_import_from3064); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    d_tree = (PythonTree)adaptor.create(d);
                    	    adaptor.addChild(root_0, d_tree);
                    	    }
                    	    if (list_d==null) list_d=new ArrayList();
                    	    list_d.add(d);


                    	    }
                    	    break;

                    	default :
                    	    break loop51;
                        }
                    } while (true);

                    pushFollow(FOLLOW_dotted_name_in_import_from3067);
                    dotted_name111=dotted_name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dotted_name111.getTree());

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:35: (d+= DOT )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:36: (d+= DOT )+
                    int cnt52=0;
                    loop52:
                    do {
                        int alt52=2;
                        int LA52_0 = input.LA(1);

                        if ( (LA52_0==DOT) ) {
                            alt52=1;
                        }


                        switch (alt52) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:971:36: d+= DOT
                    	    {
                    	    d=(Token)match(input,DOT,FOLLOW_DOT_in_import_from3073); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    d_tree = (PythonTree)adaptor.create(d);
                    	    adaptor.addChild(root_0, d_tree);
                    	    }
                    	    if (list_d==null) list_d=new ArrayList();
                    	    list_d.add(d);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt52 >= 1 ) break loop52;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(52, input);
                                throw eee;
                        }
                        cnt52++;
                    } while (true);


                    }
                    break;

            }

            IMPORT112=(Token)match(input,IMPORT,FOLLOW_IMPORT_in_import_from3077); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IMPORT112_tree = (PythonTree)adaptor.create(IMPORT112);
            adaptor.addChild(root_0, IMPORT112_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:972:9: ( STAR | i1= import_as_names | LPAREN i2= import_as_names ( COMMA )? RPAREN )
            int alt55=3;
            switch ( input.LA(1) ) {
            case STAR:
                {
                alt55=1;
                }
                break;
            case NAME:
                {
                alt55=2;
                }
                break;
            case LPAREN:
                {
                alt55=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 55, 0, input);

                throw nvae;
            }

            switch (alt55) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:972:10: STAR
                    {
                    STAR113=(Token)match(input,STAR,FOLLOW_STAR_in_import_from3088); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STAR113_tree = (PythonTree)adaptor.create(STAR113);
                    adaptor.addChild(root_0, STAR113_tree);
                    }
                    if ( state.backtracking==0 ) {

                                   stype = new ImportFrom(FROM110, actions.makeFromText(list_d, (dotted_name111!=null?dotted_name111.names:null)),
                                       actions.makeModuleNameNode(list_d, (dotted_name111!=null?dotted_name111.names:null)),
                                       actions.makeStarAlias(STAR113), actions.makeLevel(list_d));
                               
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:978:11: i1= import_as_names
                    {
                    pushFollow(FOLLOW_import_as_names_in_import_from3113);
                    i1=import_as_names();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, i1.getTree());
                    if ( state.backtracking==0 ) {

                                   String dottedText = (dotted_name111!=null?input.toString(dotted_name111.start,dotted_name111.stop):null);
                                   if (dottedText != null && dottedText.equals("__future__")) {
                                       List<alias> aliases = (i1!=null?i1.atypes:null);
                                       for(alias a: aliases) {
                                           if (a != null) {
                                               if (a.getInternalName().equals("print_function")) {
                                                   printFunction = true;
                                               } else if (a.getInternalName().equals("unicode_literals")) {
                                                   unicodeLiterals = true;
                                               }
                                           }
                                       }
                                   }
                                   stype = new ImportFrom(FROM110, actions.makeFromText(list_d, (dotted_name111!=null?dotted_name111.names:null)),
                                       actions.makeModuleNameNode(list_d, (dotted_name111!=null?dotted_name111.names:null)),
                                       actions.makeAliases((i1!=null?i1.atypes:null)), actions.makeLevel(list_d));
                               
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:997:11: LPAREN i2= import_as_names ( COMMA )? RPAREN
                    {
                    LPAREN114=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_import_from3136); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN114_tree = (PythonTree)adaptor.create(LPAREN114);
                    adaptor.addChild(root_0, LPAREN114_tree);
                    }
                    pushFollow(FOLLOW_import_as_names_in_import_from3140);
                    i2=import_as_names();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, i2.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:997:37: ( COMMA )?
                    int alt54=2;
                    int LA54_0 = input.LA(1);

                    if ( (LA54_0==COMMA) ) {
                        alt54=1;
                    }
                    switch (alt54) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:997:37: COMMA
                            {
                            COMMA115=(Token)match(input,COMMA,FOLLOW_COMMA_in_import_from3142); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA115_tree = (PythonTree)adaptor.create(COMMA115);
                            adaptor.addChild(root_0, COMMA115_tree);
                            }

                            }
                            break;

                    }

                    RPAREN116=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_import_from3145); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN116_tree = (PythonTree)adaptor.create(RPAREN116);
                    adaptor.addChild(root_0, RPAREN116_tree);
                    }
                    if ( state.backtracking==0 ) {

                                   //XXX: this is almost a complete C&P of the code above - is there some way
                                   //     to factor it out?
                                   String dottedText = (dotted_name111!=null?input.toString(dotted_name111.start,dotted_name111.stop):null);
                                   if (dottedText != null && dottedText.equals("__future__")) {
                                       List<alias> aliases = (i2!=null?i2.atypes:null);
                                       for(alias a: aliases) {
                                           if (a != null) {
                                               if (a.getInternalName().equals("print_function")) {
                                                   printFunction = true;
                                               } else if (a.getInternalName().equals("unicode_literals")) {
                                                   unicodeLiterals = true;
                                               }
                                           }
                                       }
                                   }
                                   stype = new ImportFrom(FROM110, actions.makeFromText(list_d, (dotted_name111!=null?dotted_name111.names:null)),
                                       actions.makeModuleNameNode(list_d, (dotted_name111!=null?dotted_name111.names:null)),
                                       actions.makeAliases((i2!=null?i2.atypes:null)), actions.makeLevel(list_d));
                               
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "import_from"

    public static class import_as_names_return extends ParserRuleReturnScope {
        public List<alias> atypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "import_as_names"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1022:1: import_as_names returns [List<alias> atypes] : n+= import_as_name ( COMMA n+= import_as_name )* ;
    public final TruffleParser.import_as_names_return import_as_names() throws RecognitionException {
        TruffleParser.import_as_names_return retval = new TruffleParser.import_as_names_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COMMA117=null;
        List list_n=null;
        TruffleParser.import_as_name_return n = null;
         n = null;
        PythonTree COMMA117_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1024:5: (n+= import_as_name ( COMMA n+= import_as_name )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1024:7: n+= import_as_name ( COMMA n+= import_as_name )*
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_import_as_name_in_import_as_names3194);
            n=import_as_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, n.getTree());
            if (list_n==null) list_n=new ArrayList();
            list_n.add(n.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1024:25: ( COMMA n+= import_as_name )*
            loop56:
            do {
                int alt56=2;
                int LA56_0 = input.LA(1);

                if ( (LA56_0==COMMA) ) {
                    int LA56_2 = input.LA(2);

                    if ( (LA56_2==NAME) ) {
                        alt56=1;
                    }


                }


                switch (alt56) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1024:26: COMMA n+= import_as_name
            	    {
            	    COMMA117=(Token)match(input,COMMA,FOLLOW_COMMA_in_import_as_names3197); if (state.failed) return retval;
            	    pushFollow(FOLLOW_import_as_name_in_import_as_names3202);
            	    n=import_as_name();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, n.getTree());
            	    if (list_n==null) list_n=new ArrayList();
            	    list_n.add(n.getTree());


            	    }
            	    break;

            	default :
            	    break loop56;
                }
            } while (true);

            if ( state.backtracking==0 ) {

                      retval.atypes = list_n;
                  
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "import_as_names"

    public static class import_as_name_return extends ParserRuleReturnScope {
        public alias atype;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "import_as_name"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1031:1: import_as_name returns [alias atype] : name= NAME ( AS asname= NAME )? ;
    public final TruffleParser.import_as_name_return import_as_name() throws RecognitionException {
        TruffleParser.import_as_name_return retval = new TruffleParser.import_as_name_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token name=null;
        Token asname=null;
        Token AS118=null;

        PythonTree name_tree=null;
        PythonTree asname_tree=null;
        PythonTree AS118_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1036:5: (name= NAME ( AS asname= NAME )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1036:7: name= NAME ( AS asname= NAME )?
            {
            root_0 = (PythonTree)adaptor.nil();

            name=(Token)match(input,NAME,FOLLOW_NAME_in_import_as_name3243); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            name_tree = (PythonTree)adaptor.create(name);
            adaptor.addChild(root_0, name_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1036:17: ( AS asname= NAME )?
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==AS) ) {
                alt57=1;
            }
            switch (alt57) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1036:18: AS asname= NAME
                    {
                    AS118=(Token)match(input,AS,FOLLOW_AS_in_import_as_name3246); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AS118_tree = (PythonTree)adaptor.create(AS118);
                    adaptor.addChild(root_0, AS118_tree);
                    }
                    asname=(Token)match(input,NAME,FOLLOW_NAME_in_import_as_name3250); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    asname_tree = (PythonTree)adaptor.create(asname);
                    adaptor.addChild(root_0, asname_tree);
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                      retval.atype = new alias(actions.makeNameNode(name), actions.makeNameNode(asname));
                  
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = retval.atype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "import_as_name"

    public static class dotted_as_name_return extends ParserRuleReturnScope {
        public alias atype;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dotted_as_name"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1044:1: dotted_as_name returns [alias atype] : dotted_name ( AS asname= NAME )? ;
    public final TruffleParser.dotted_as_name_return dotted_as_name() throws RecognitionException {
        TruffleParser.dotted_as_name_return retval = new TruffleParser.dotted_as_name_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token asname=null;
        Token AS120=null;
        TruffleParser.dotted_name_return dotted_name119 = null;


        PythonTree asname_tree=null;
        PythonTree AS120_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1049:5: ( dotted_name ( AS asname= NAME )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1049:7: dotted_name ( AS asname= NAME )?
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_dotted_name_in_dotted_as_name3290);
            dotted_name119=dotted_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dotted_name119.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1049:19: ( AS asname= NAME )?
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==AS) ) {
                alt58=1;
            }
            switch (alt58) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1049:20: AS asname= NAME
                    {
                    AS120=(Token)match(input,AS,FOLLOW_AS_in_dotted_as_name3293); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AS120_tree = (PythonTree)adaptor.create(AS120);
                    adaptor.addChild(root_0, AS120_tree);
                    }
                    asname=(Token)match(input,NAME,FOLLOW_NAME_in_dotted_as_name3297); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    asname_tree = (PythonTree)adaptor.create(asname);
                    adaptor.addChild(root_0, asname_tree);
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                      retval.atype = new alias((dotted_name119!=null?dotted_name119.names:null), actions.makeNameNode(asname));
                  
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = retval.atype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "dotted_as_name"

    public static class dotted_as_names_return extends ParserRuleReturnScope {
        public List<alias> atypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dotted_as_names"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1056:1: dotted_as_names returns [List<alias> atypes] : d+= dotted_as_name ( COMMA d+= dotted_as_name )* ;
    public final TruffleParser.dotted_as_names_return dotted_as_names() throws RecognitionException {
        TruffleParser.dotted_as_names_return retval = new TruffleParser.dotted_as_names_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COMMA121=null;
        List list_d=null;
        TruffleParser.dotted_as_name_return d = null;
         d = null;
        PythonTree COMMA121_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1058:5: (d+= dotted_as_name ( COMMA d+= dotted_as_name )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1058:7: d+= dotted_as_name ( COMMA d+= dotted_as_name )*
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_dotted_as_name_in_dotted_as_names3333);
            d=dotted_as_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, d.getTree());
            if (list_d==null) list_d=new ArrayList();
            list_d.add(d.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1058:25: ( COMMA d+= dotted_as_name )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==COMMA) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1058:26: COMMA d+= dotted_as_name
            	    {
            	    COMMA121=(Token)match(input,COMMA,FOLLOW_COMMA_in_dotted_as_names3336); if (state.failed) return retval;
            	    pushFollow(FOLLOW_dotted_as_name_in_dotted_as_names3341);
            	    d=dotted_as_name();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, d.getTree());
            	    if (list_d==null) list_d=new ArrayList();
            	    list_d.add(d.getTree());


            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);

            if ( state.backtracking==0 ) {

                      retval.atypes = list_d;
                  
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "dotted_as_names"

    public static class dotted_name_return extends ParserRuleReturnScope {
        public List<Name> names;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dotted_name"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1065:1: dotted_name returns [List<Name> names] : NAME ( DOT dn+= attr )* ;
    public final TruffleParser.dotted_name_return dotted_name() throws RecognitionException {
        TruffleParser.dotted_name_return retval = new TruffleParser.dotted_name_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token NAME122=null;
        Token DOT123=null;
        List list_dn=null;
        TruffleParser.attr_return dn = null;
         dn = null;
        PythonTree NAME122_tree=null;
        PythonTree DOT123_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1067:5: ( NAME ( DOT dn+= attr )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1067:7: NAME ( DOT dn+= attr )*
            {
            root_0 = (PythonTree)adaptor.nil();

            NAME122=(Token)match(input,NAME,FOLLOW_NAME_in_dotted_name3375); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            NAME122_tree = (PythonTree)adaptor.create(NAME122);
            adaptor.addChild(root_0, NAME122_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1067:12: ( DOT dn+= attr )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( (LA60_0==DOT) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1067:13: DOT dn+= attr
            	    {
            	    DOT123=(Token)match(input,DOT,FOLLOW_DOT_in_dotted_name3378); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    DOT123_tree = (PythonTree)adaptor.create(DOT123);
            	    adaptor.addChild(root_0, DOT123_tree);
            	    }
            	    pushFollow(FOLLOW_attr_in_dotted_name3382);
            	    dn=attr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, dn.getTree());
            	    if (list_dn==null) list_dn=new ArrayList();
            	    list_dn.add(dn.getTree());


            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);

            if ( state.backtracking==0 ) {

                      retval.names = actions.makeDottedName(NAME122, list_dn);
                  
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "dotted_name"

    public static class global_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "global_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1074:1: global_stmt : GLOBAL n+= NAME ( COMMA n+= NAME )* ;
    public final TruffleParser.global_stmt_return global_stmt() throws RecognitionException {
        TruffleParser.global_stmt_return retval = new TruffleParser.global_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token GLOBAL124=null;
        Token COMMA125=null;
        Token n=null;
        List list_n=null;

        PythonTree GLOBAL124_tree=null;
        PythonTree COMMA125_tree=null;
        PythonTree n_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1081:5: ( GLOBAL n+= NAME ( COMMA n+= NAME )* )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1081:7: GLOBAL n+= NAME ( COMMA n+= NAME )*
            {
            root_0 = (PythonTree)adaptor.nil();

            GLOBAL124=(Token)match(input,GLOBAL,FOLLOW_GLOBAL_in_global_stmt3418); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            GLOBAL124_tree = (PythonTree)adaptor.create(GLOBAL124);
            adaptor.addChild(root_0, GLOBAL124_tree);
            }
            n=(Token)match(input,NAME,FOLLOW_NAME_in_global_stmt3422); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            n_tree = (PythonTree)adaptor.create(n);
            adaptor.addChild(root_0, n_tree);
            }
            if (list_n==null) list_n=new ArrayList();
            list_n.add(n);

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1081:22: ( COMMA n+= NAME )*
            loop61:
            do {
                int alt61=2;
                int LA61_0 = input.LA(1);

                if ( (LA61_0==COMMA) ) {
                    alt61=1;
                }


                switch (alt61) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1081:23: COMMA n+= NAME
            	    {
            	    COMMA125=(Token)match(input,COMMA,FOLLOW_COMMA_in_global_stmt3425); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA125_tree = (PythonTree)adaptor.create(COMMA125);
            	    adaptor.addChild(root_0, COMMA125_tree);
            	    }
            	    n=(Token)match(input,NAME,FOLLOW_NAME_in_global_stmt3429); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    n_tree = (PythonTree)adaptor.create(n);
            	    adaptor.addChild(root_0, n_tree);
            	    }
            	    if (list_n==null) list_n=new ArrayList();
            	    list_n.add(n);


            	    }
            	    break;

            	default :
            	    break loop61;
                }
            } while (true);

            if ( state.backtracking==0 ) {

                        stype = new Global(GLOBAL124, actions.makeNames(list_n), actions.makeNameNodes(list_n));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "global_stmt"

    public static class exec_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exec_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1088:1: exec_stmt : EXEC expr[expr_contextType.Load] ( IN t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] )? )? ;
    public final TruffleParser.exec_stmt_return exec_stmt() throws RecognitionException {
        TruffleParser.exec_stmt_return retval = new TruffleParser.exec_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token EXEC126=null;
        Token IN128=null;
        Token COMMA129=null;
        TruffleParser.test_return t1 = null;

        TruffleParser.test_return t2 = null;

        TruffleParser.expr_return expr127 = null;


        PythonTree EXEC126_tree=null;
        PythonTree IN128_tree=null;
        PythonTree COMMA129_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1095:5: ( EXEC expr[expr_contextType.Load] ( IN t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] )? )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1095:7: EXEC expr[expr_contextType.Load] ( IN t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] )? )?
            {
            root_0 = (PythonTree)adaptor.nil();

            EXEC126=(Token)match(input,EXEC,FOLLOW_EXEC_in_exec_stmt3467); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EXEC126_tree = (PythonTree)adaptor.create(EXEC126);
            adaptor.addChild(root_0, EXEC126_tree);
            }
            pushFollow(FOLLOW_expr_in_exec_stmt3469);
            expr127=expr(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, expr127.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1095:40: ( IN t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] )? )?
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==IN) ) {
                alt63=1;
            }
            switch (alt63) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1095:41: IN t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] )?
                    {
                    IN128=(Token)match(input,IN,FOLLOW_IN_in_exec_stmt3473); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IN128_tree = (PythonTree)adaptor.create(IN128);
                    adaptor.addChild(root_0, IN128_tree);
                    }
                    pushFollow(FOLLOW_test_in_exec_stmt3477);
                    t1=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t1.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1095:75: ( COMMA t2= test[expr_contextType.Load] )?
                    int alt62=2;
                    int LA62_0 = input.LA(1);

                    if ( (LA62_0==COMMA) ) {
                        alt62=1;
                    }
                    switch (alt62) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1095:76: COMMA t2= test[expr_contextType.Load]
                            {
                            COMMA129=(Token)match(input,COMMA,FOLLOW_COMMA_in_exec_stmt3481); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA129_tree = (PythonTree)adaptor.create(COMMA129);
                            adaptor.addChild(root_0, COMMA129_tree);
                            }
                            pushFollow(FOLLOW_test_in_exec_stmt3485);
                            t2=test(expr_contextType.Load);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, t2.getTree());

                            }
                            break;

                    }


                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                       stype = new Exec(EXEC126, actions.castExpr((expr127!=null?((PythonTree)expr127.tree):null)), actions.castExpr((t1!=null?((PythonTree)t1.tree):null)), actions.castExpr((t2!=null?((PythonTree)t2.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "exec_stmt"

    public static class assert_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assert_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1102:1: assert_stmt : ASSERT t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] )? ;
    public final TruffleParser.assert_stmt_return assert_stmt() throws RecognitionException {
        TruffleParser.assert_stmt_return retval = new TruffleParser.assert_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token ASSERT130=null;
        Token COMMA131=null;
        TruffleParser.test_return t1 = null;

        TruffleParser.test_return t2 = null;


        PythonTree ASSERT130_tree=null;
        PythonTree COMMA131_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1109:5: ( ASSERT t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1109:7: ASSERT t1= test[expr_contextType.Load] ( COMMA t2= test[expr_contextType.Load] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            ASSERT130=(Token)match(input,ASSERT,FOLLOW_ASSERT_in_assert_stmt3526); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ASSERT130_tree = (PythonTree)adaptor.create(ASSERT130);
            adaptor.addChild(root_0, ASSERT130_tree);
            }
            pushFollow(FOLLOW_test_in_assert_stmt3530);
            t1=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, t1.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1109:45: ( COMMA t2= test[expr_contextType.Load] )?
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==COMMA) ) {
                alt64=1;
            }
            switch (alt64) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1109:46: COMMA t2= test[expr_contextType.Load]
                    {
                    COMMA131=(Token)match(input,COMMA,FOLLOW_COMMA_in_assert_stmt3534); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA131_tree = (PythonTree)adaptor.create(COMMA131);
                    adaptor.addChild(root_0, COMMA131_tree);
                    }
                    pushFollow(FOLLOW_test_in_assert_stmt3538);
                    t2=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t2.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        stype = new Assert(ASSERT130, actions.castExpr((t1!=null?((PythonTree)t1.tree):null)), actions.castExpr((t2!=null?((PythonTree)t2.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assert_stmt"

    public static class compound_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "compound_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1116:1: compound_stmt : ( if_stmt | while_stmt | for_stmt | try_stmt | with_stmt | ( ( decorators )? DEF )=> funcdef | classdef );
    public final TruffleParser.compound_stmt_return compound_stmt() throws RecognitionException {
        TruffleParser.compound_stmt_return retval = new TruffleParser.compound_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        TruffleParser.if_stmt_return if_stmt132 = null;

        TruffleParser.while_stmt_return while_stmt133 = null;

        TruffleParser.for_stmt_return for_stmt134 = null;

        TruffleParser.try_stmt_return try_stmt135 = null;

        TruffleParser.with_stmt_return with_stmt136 = null;

        TruffleParser.funcdef_return funcdef137 = null;

        TruffleParser.classdef_return classdef138 = null;



        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1117:5: ( if_stmt | while_stmt | for_stmt | try_stmt | with_stmt | ( ( decorators )? DEF )=> funcdef | classdef )
            int alt65=7;
            int LA65_0 = input.LA(1);

            if ( (LA65_0==IF) ) {
                alt65=1;
            }
            else if ( (LA65_0==WHILE) ) {
                alt65=2;
            }
            else if ( (LA65_0==FOR) ) {
                alt65=3;
            }
            else if ( (LA65_0==TRY) ) {
                alt65=4;
            }
            else if ( (LA65_0==WITH) ) {
                alt65=5;
            }
            else if ( (LA65_0==AT) ) {
                int LA65_6 = input.LA(2);

                if ( (synpred6_Truffle()) ) {
                    alt65=6;
                }
                else if ( (true) ) {
                    alt65=7;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 65, 6, input);

                    throw nvae;
                }
            }
            else if ( (LA65_0==DEF) && (synpred6_Truffle())) {
                alt65=6;
            }
            else if ( (LA65_0==CLASS) ) {
                alt65=7;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                throw nvae;
            }
            switch (alt65) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1117:7: if_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_if_stmt_in_compound_stmt3567);
                    if_stmt132=if_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, if_stmt132.getTree());

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1118:7: while_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_while_stmt_in_compound_stmt3575);
                    while_stmt133=while_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, while_stmt133.getTree());

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1119:7: for_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_for_stmt_in_compound_stmt3583);
                    for_stmt134=for_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, for_stmt134.getTree());

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1120:7: try_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_try_stmt_in_compound_stmt3591);
                    try_stmt135=try_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, try_stmt135.getTree());

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1121:7: with_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_with_stmt_in_compound_stmt3599);
                    with_stmt136=with_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, with_stmt136.getTree());

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1122:7: ( ( decorators )? DEF )=> funcdef
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_funcdef_in_compound_stmt3616);
                    funcdef137=funcdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, funcdef137.getTree());

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1123:7: classdef
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_classdef_in_compound_stmt3624);
                    classdef138=classdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, classdef138.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "compound_stmt"

    public static class if_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "if_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1127:1: if_stmt : IF test[expr_contextType.Load] COLON ifsuite= suite[false] ( elif_clause )? ;
    public final TruffleParser.if_stmt_return if_stmt() throws RecognitionException {
        TruffleParser.if_stmt_return retval = new TruffleParser.if_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token IF139=null;
        Token COLON141=null;
        TruffleParser.suite_return ifsuite = null;

        TruffleParser.test_return test140 = null;

        TruffleParser.elif_clause_return elif_clause142 = null;


        PythonTree IF139_tree=null;
        PythonTree COLON141_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1134:5: ( IF test[expr_contextType.Load] COLON ifsuite= suite[false] ( elif_clause )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1134:7: IF test[expr_contextType.Load] COLON ifsuite= suite[false] ( elif_clause )?
            {
            root_0 = (PythonTree)adaptor.nil();

            IF139=(Token)match(input,IF,FOLLOW_IF_in_if_stmt3652); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IF139_tree = (PythonTree)adaptor.create(IF139);
            adaptor.addChild(root_0, IF139_tree);
            }
            pushFollow(FOLLOW_test_in_if_stmt3654);
            test140=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, test140.getTree());
            COLON141=(Token)match(input,COLON,FOLLOW_COLON_in_if_stmt3657); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON141_tree = (PythonTree)adaptor.create(COLON141);
            adaptor.addChild(root_0, COLON141_tree);
            }
            pushFollow(FOLLOW_suite_in_if_stmt3661);
            ifsuite=suite(false);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ifsuite.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1134:65: ( elif_clause )?
            int alt66=2;
            int LA66_0 = input.LA(1);

            if ( (LA66_0==ELIF||LA66_0==ORELSE) ) {
                alt66=1;
            }
            switch (alt66) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1134:65: elif_clause
                    {
                    pushFollow(FOLLOW_elif_clause_in_if_stmt3664);
                    elif_clause142=elif_clause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, elif_clause142.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        stype = new If(IF139, actions.castExpr((test140!=null?((PythonTree)test140.tree):null)), actions.castStmts((ifsuite!=null?ifsuite.stypes:null)),
                            actions.makeElse((elif_clause142!=null?elif_clause142.stypes:null), (elif_clause142!=null?((PythonTree)elif_clause142.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "if_stmt"

    public static class elif_clause_return extends ParserRuleReturnScope {
        public List stypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elif_clause"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1142:1: elif_clause returns [List stypes] : ( else_clause | ELIF test[expr_contextType.Load] COLON suite[false] (e2= elif_clause | ) );
    public final TruffleParser.elif_clause_return elif_clause() throws RecognitionException {
        TruffleParser.elif_clause_return retval = new TruffleParser.elif_clause_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token ELIF144=null;
        Token COLON146=null;
        TruffleParser.elif_clause_return e2 = null;

        TruffleParser.else_clause_return else_clause143 = null;

        TruffleParser.test_return test145 = null;

        TruffleParser.suite_return suite147 = null;


        PythonTree ELIF144_tree=null;
        PythonTree COLON146_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1152:5: ( else_clause | ELIF test[expr_contextType.Load] COLON suite[false] (e2= elif_clause | ) )
            int alt68=2;
            int LA68_0 = input.LA(1);

            if ( (LA68_0==ORELSE) ) {
                alt68=1;
            }
            else if ( (LA68_0==ELIF) ) {
                alt68=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 68, 0, input);

                throw nvae;
            }
            switch (alt68) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1152:7: else_clause
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_else_clause_in_elif_clause3709);
                    else_clause143=else_clause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, else_clause143.getTree());
                    if ( state.backtracking==0 ) {

                                retval.stypes = (else_clause143!=null?else_clause143.stypes:null);
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1156:7: ELIF test[expr_contextType.Load] COLON suite[false] (e2= elif_clause | )
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    ELIF144=(Token)match(input,ELIF,FOLLOW_ELIF_in_elif_clause3725); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ELIF144_tree = (PythonTree)adaptor.create(ELIF144);
                    adaptor.addChild(root_0, ELIF144_tree);
                    }
                    pushFollow(FOLLOW_test_in_elif_clause3727);
                    test145=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, test145.getTree());
                    COLON146=(Token)match(input,COLON,FOLLOW_COLON_in_elif_clause3730); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON146_tree = (PythonTree)adaptor.create(COLON146);
                    adaptor.addChild(root_0, COLON146_tree);
                    }
                    pushFollow(FOLLOW_suite_in_elif_clause3732);
                    suite147=suite(false);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, suite147.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1157:7: (e2= elif_clause | )
                    int alt67=2;
                    int LA67_0 = input.LA(1);

                    if ( (LA67_0==ELIF||LA67_0==ORELSE) ) {
                        alt67=1;
                    }
                    else if ( (LA67_0==EOF||LA67_0==DEDENT||LA67_0==NEWLINE||LA67_0==NAME||LA67_0==PRINT||(LA67_0>=ASSERT && LA67_0<=DELETE)||(LA67_0>=FROM && LA67_0<=IMPORT)||(LA67_0>=LAMBDA && LA67_0<=NOT)||(LA67_0>=PASS && LA67_0<=LPAREN)||(LA67_0>=PLUS && LA67_0<=MINUS)||(LA67_0>=TILDE && LA67_0<=LBRACK)||LA67_0==LCURLY||(LA67_0>=BACKQUOTE && LA67_0<=STRING)) ) {
                        alt67=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 67, 0, input);

                        throw nvae;
                    }
                    switch (alt67) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1157:8: e2= elif_clause
                            {
                            pushFollow(FOLLOW_elif_clause_in_elif_clause3744);
                            e2=elif_clause();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                            if ( state.backtracking==0 ) {

                                         stype = new If((test145!=null?((Token)test145.start):null), actions.castExpr((test145!=null?((PythonTree)test145.tree):null)), actions.castStmts((suite147!=null?suite147.stypes:null)), actions.makeElse((e2!=null?e2.stypes:null), (e2!=null?((PythonTree)e2.tree):null)));
                                     
                            }

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1162:8: 
                            {
                            if ( state.backtracking==0 ) {

                                         stype = new If((test145!=null?((Token)test145.start):null), actions.castExpr((test145!=null?((PythonTree)test145.tree):null)), actions.castStmts((suite147!=null?suite147.stypes:null)), new ArrayList<stmt>());
                                     
                            }

                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 if (stype != null) {
                     retval.tree = stype;
                 }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elif_clause"

    public static class else_clause_return extends ParserRuleReturnScope {
        public List stypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "else_clause"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1169:1: else_clause returns [List stypes] : ORELSE COLON elsesuite= suite[false] ;
    public final TruffleParser.else_clause_return else_clause() throws RecognitionException {
        TruffleParser.else_clause_return retval = new TruffleParser.else_clause_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token ORELSE148=null;
        Token COLON149=null;
        TruffleParser.suite_return elsesuite = null;


        PythonTree ORELSE148_tree=null;
        PythonTree COLON149_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1171:5: ( ORELSE COLON elsesuite= suite[false] )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1171:7: ORELSE COLON elsesuite= suite[false]
            {
            root_0 = (PythonTree)adaptor.nil();

            ORELSE148=(Token)match(input,ORELSE,FOLLOW_ORELSE_in_else_clause3804); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ORELSE148_tree = (PythonTree)adaptor.create(ORELSE148);
            adaptor.addChild(root_0, ORELSE148_tree);
            }
            COLON149=(Token)match(input,COLON,FOLLOW_COLON_in_else_clause3806); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON149_tree = (PythonTree)adaptor.create(COLON149);
            adaptor.addChild(root_0, COLON149_tree);
            }
            pushFollow(FOLLOW_suite_in_else_clause3810);
            elsesuite=suite(false);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, elsesuite.getTree());
            if ( state.backtracking==0 ) {

                        retval.stypes = (elsesuite!=null?elsesuite.stypes:null);
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "else_clause"

    public static class while_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "while_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1178:1: while_stmt : WHILE test[expr_contextType.Load] COLON s1= suite[false] ( ORELSE COLON s2= suite[false] )? ;
    public final TruffleParser.while_stmt_return while_stmt() throws RecognitionException {
        TruffleParser.while_stmt_return retval = new TruffleParser.while_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token WHILE150=null;
        Token COLON152=null;
        Token ORELSE153=null;
        Token COLON154=null;
        TruffleParser.suite_return s1 = null;

        TruffleParser.suite_return s2 = null;

        TruffleParser.test_return test151 = null;


        PythonTree WHILE150_tree=null;
        PythonTree COLON152_tree=null;
        PythonTree ORELSE153_tree=null;
        PythonTree COLON154_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1185:5: ( WHILE test[expr_contextType.Load] COLON s1= suite[false] ( ORELSE COLON s2= suite[false] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1185:7: WHILE test[expr_contextType.Load] COLON s1= suite[false] ( ORELSE COLON s2= suite[false] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            WHILE150=(Token)match(input,WHILE,FOLLOW_WHILE_in_while_stmt3847); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            WHILE150_tree = (PythonTree)adaptor.create(WHILE150);
            adaptor.addChild(root_0, WHILE150_tree);
            }
            pushFollow(FOLLOW_test_in_while_stmt3849);
            test151=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, test151.getTree());
            COLON152=(Token)match(input,COLON,FOLLOW_COLON_in_while_stmt3852); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON152_tree = (PythonTree)adaptor.create(COLON152);
            adaptor.addChild(root_0, COLON152_tree);
            }
            pushFollow(FOLLOW_suite_in_while_stmt3856);
            s1=suite(false);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, s1.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1185:63: ( ORELSE COLON s2= suite[false] )?
            int alt69=2;
            int LA69_0 = input.LA(1);

            if ( (LA69_0==ORELSE) ) {
                alt69=1;
            }
            switch (alt69) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1185:64: ORELSE COLON s2= suite[false]
                    {
                    ORELSE153=(Token)match(input,ORELSE,FOLLOW_ORELSE_in_while_stmt3860); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ORELSE153_tree = (PythonTree)adaptor.create(ORELSE153);
                    adaptor.addChild(root_0, ORELSE153_tree);
                    }
                    COLON154=(Token)match(input,COLON,FOLLOW_COLON_in_while_stmt3862); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON154_tree = (PythonTree)adaptor.create(COLON154);
                    adaptor.addChild(root_0, COLON154_tree);
                    }
                    pushFollow(FOLLOW_suite_in_while_stmt3866);
                    s2=suite(false);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s2.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        stype = actions.makeWhile(WHILE150, actions.castExpr((test151!=null?((PythonTree)test151.tree):null)), (s1!=null?s1.stypes:null), (s2!=null?s2.stypes:null));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "while_stmt"

    public static class for_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "for_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1192:1: for_stmt : FOR exprlist[expr_contextType.Store] IN testlist[expr_contextType.Load] COLON s1= suite[false] ( ORELSE COLON s2= suite[false] )? ;
    public final TruffleParser.for_stmt_return for_stmt() throws RecognitionException {
        TruffleParser.for_stmt_return retval = new TruffleParser.for_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token FOR155=null;
        Token IN157=null;
        Token COLON159=null;
        Token ORELSE160=null;
        Token COLON161=null;
        TruffleParser.suite_return s1 = null;

        TruffleParser.suite_return s2 = null;

        TruffleParser.exprlist_return exprlist156 = null;

        TruffleParser.testlist_return testlist158 = null;


        PythonTree FOR155_tree=null;
        PythonTree IN157_tree=null;
        PythonTree COLON159_tree=null;
        PythonTree ORELSE160_tree=null;
        PythonTree COLON161_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1199:5: ( FOR exprlist[expr_contextType.Store] IN testlist[expr_contextType.Load] COLON s1= suite[false] ( ORELSE COLON s2= suite[false] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1199:7: FOR exprlist[expr_contextType.Store] IN testlist[expr_contextType.Load] COLON s1= suite[false] ( ORELSE COLON s2= suite[false] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            FOR155=(Token)match(input,FOR,FOLLOW_FOR_in_for_stmt3905); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FOR155_tree = (PythonTree)adaptor.create(FOR155);
            adaptor.addChild(root_0, FOR155_tree);
            }
            pushFollow(FOLLOW_exprlist_in_for_stmt3907);
            exprlist156=exprlist(expr_contextType.Store);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, exprlist156.getTree());
            IN157=(Token)match(input,IN,FOLLOW_IN_in_for_stmt3910); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IN157_tree = (PythonTree)adaptor.create(IN157);
            adaptor.addChild(root_0, IN157_tree);
            }
            pushFollow(FOLLOW_testlist_in_for_stmt3912);
            testlist158=testlist(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, testlist158.getTree());
            COLON159=(Token)match(input,COLON,FOLLOW_COLON_in_for_stmt3915); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON159_tree = (PythonTree)adaptor.create(COLON159);
            adaptor.addChild(root_0, COLON159_tree);
            }
            pushFollow(FOLLOW_suite_in_for_stmt3919);
            s1=suite(false);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, s1.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1200:9: ( ORELSE COLON s2= suite[false] )?
            int alt70=2;
            int LA70_0 = input.LA(1);

            if ( (LA70_0==ORELSE) ) {
                alt70=1;
            }
            switch (alt70) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1200:10: ORELSE COLON s2= suite[false]
                    {
                    ORELSE160=(Token)match(input,ORELSE,FOLLOW_ORELSE_in_for_stmt3931); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ORELSE160_tree = (PythonTree)adaptor.create(ORELSE160);
                    adaptor.addChild(root_0, ORELSE160_tree);
                    }
                    COLON161=(Token)match(input,COLON,FOLLOW_COLON_in_for_stmt3933); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON161_tree = (PythonTree)adaptor.create(COLON161);
                    adaptor.addChild(root_0, COLON161_tree);
                    }
                    pushFollow(FOLLOW_suite_in_for_stmt3937);
                    s2=suite(false);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s2.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        stype = actions.makeFor(FOR155, (exprlist156!=null?exprlist156.etype:null), actions.castExpr((testlist158!=null?((PythonTree)testlist158.tree):null)), (s1!=null?s1.stypes:null), (s2!=null?s2.stypes:null));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "for_stmt"

    public static class try_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "try_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1211:1: try_stmt : TRY COLON trysuite= suite[!$suite.isEmpty() && $suite::continueIllegal] ( (e+= except_clause )+ ( ORELSE COLON elsesuite= suite[!$suite.isEmpty() && $suite::continueIllegal] )? ( FINALLY COLON finalsuite= suite[true] )? | FINALLY COLON finalsuite= suite[true] ) ;
    public final TruffleParser.try_stmt_return try_stmt() throws RecognitionException {
        TruffleParser.try_stmt_return retval = new TruffleParser.try_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token TRY162=null;
        Token COLON163=null;
        Token ORELSE164=null;
        Token COLON165=null;
        Token FINALLY166=null;
        Token COLON167=null;
        Token FINALLY168=null;
        Token COLON169=null;
        List list_e=null;
        TruffleParser.suite_return trysuite = null;

        TruffleParser.suite_return elsesuite = null;

        TruffleParser.suite_return finalsuite = null;

        TruffleParser.except_clause_return e = null;
         e = null;
        PythonTree TRY162_tree=null;
        PythonTree COLON163_tree=null;
        PythonTree ORELSE164_tree=null;
        PythonTree COLON165_tree=null;
        PythonTree FINALLY166_tree=null;
        PythonTree COLON167_tree=null;
        PythonTree FINALLY168_tree=null;
        PythonTree COLON169_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1218:5: ( TRY COLON trysuite= suite[!$suite.isEmpty() && $suite::continueIllegal] ( (e+= except_clause )+ ( ORELSE COLON elsesuite= suite[!$suite.isEmpty() && $suite::continueIllegal] )? ( FINALLY COLON finalsuite= suite[true] )? | FINALLY COLON finalsuite= suite[true] ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1218:7: TRY COLON trysuite= suite[!$suite.isEmpty() && $suite::continueIllegal] ( (e+= except_clause )+ ( ORELSE COLON elsesuite= suite[!$suite.isEmpty() && $suite::continueIllegal] )? ( FINALLY COLON finalsuite= suite[true] )? | FINALLY COLON finalsuite= suite[true] )
            {
            root_0 = (PythonTree)adaptor.nil();

            TRY162=(Token)match(input,TRY,FOLLOW_TRY_in_try_stmt3980); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            TRY162_tree = (PythonTree)adaptor.create(TRY162);
            adaptor.addChild(root_0, TRY162_tree);
            }
            COLON163=(Token)match(input,COLON,FOLLOW_COLON_in_try_stmt3982); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON163_tree = (PythonTree)adaptor.create(COLON163);
            adaptor.addChild(root_0, COLON163_tree);
            }
            pushFollow(FOLLOW_suite_in_try_stmt3986);
            trysuite=suite(!suite_stack.isEmpty() && ((suite_scope)suite_stack.peek()).continueIllegal);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, trysuite.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1219:7: ( (e+= except_clause )+ ( ORELSE COLON elsesuite= suite[!$suite.isEmpty() && $suite::continueIllegal] )? ( FINALLY COLON finalsuite= suite[true] )? | FINALLY COLON finalsuite= suite[true] )
            int alt74=2;
            int LA74_0 = input.LA(1);

            if ( (LA74_0==EXCEPT) ) {
                alt74=1;
            }
            else if ( (LA74_0==FINALLY) ) {
                alt74=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 74, 0, input);

                throw nvae;
            }
            switch (alt74) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1219:9: (e+= except_clause )+ ( ORELSE COLON elsesuite= suite[!$suite.isEmpty() && $suite::continueIllegal] )? ( FINALLY COLON finalsuite= suite[true] )?
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1219:10: (e+= except_clause )+
                    int cnt71=0;
                    loop71:
                    do {
                        int alt71=2;
                        int LA71_0 = input.LA(1);

                        if ( (LA71_0==EXCEPT) ) {
                            alt71=1;
                        }


                        switch (alt71) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1219:10: e+= except_clause
                    	    {
                    	    pushFollow(FOLLOW_except_clause_in_try_stmt3999);
                    	    e=except_clause();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e.getTree());
                    	    if (list_e==null) list_e=new ArrayList();
                    	    list_e.add(e.getTree());


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt71 >= 1 ) break loop71;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(71, input);
                                throw eee;
                        }
                        cnt71++;
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1219:27: ( ORELSE COLON elsesuite= suite[!$suite.isEmpty() && $suite::continueIllegal] )?
                    int alt72=2;
                    int LA72_0 = input.LA(1);

                    if ( (LA72_0==ORELSE) ) {
                        alt72=1;
                    }
                    switch (alt72) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1219:28: ORELSE COLON elsesuite= suite[!$suite.isEmpty() && $suite::continueIllegal]
                            {
                            ORELSE164=(Token)match(input,ORELSE,FOLLOW_ORELSE_in_try_stmt4003); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            ORELSE164_tree = (PythonTree)adaptor.create(ORELSE164);
                            adaptor.addChild(root_0, ORELSE164_tree);
                            }
                            COLON165=(Token)match(input,COLON,FOLLOW_COLON_in_try_stmt4005); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COLON165_tree = (PythonTree)adaptor.create(COLON165);
                            adaptor.addChild(root_0, COLON165_tree);
                            }
                            pushFollow(FOLLOW_suite_in_try_stmt4009);
                            elsesuite=suite(!suite_stack.isEmpty() && ((suite_scope)suite_stack.peek()).continueIllegal);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, elsesuite.getTree());

                            }
                            break;

                    }

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1219:105: ( FINALLY COLON finalsuite= suite[true] )?
                    int alt73=2;
                    int LA73_0 = input.LA(1);

                    if ( (LA73_0==FINALLY) ) {
                        alt73=1;
                    }
                    switch (alt73) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1219:106: FINALLY COLON finalsuite= suite[true]
                            {
                            FINALLY166=(Token)match(input,FINALLY,FOLLOW_FINALLY_in_try_stmt4015); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            FINALLY166_tree = (PythonTree)adaptor.create(FINALLY166);
                            adaptor.addChild(root_0, FINALLY166_tree);
                            }
                            COLON167=(Token)match(input,COLON,FOLLOW_COLON_in_try_stmt4017); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COLON167_tree = (PythonTree)adaptor.create(COLON167);
                            adaptor.addChild(root_0, COLON167_tree);
                            }
                            pushFollow(FOLLOW_suite_in_try_stmt4021);
                            finalsuite=suite(true);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, finalsuite.getTree());

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                  stype = actions.makeTryExcept(TRY162, (trysuite!=null?trysuite.stypes:null), list_e, (elsesuite!=null?elsesuite.stypes:null), (finalsuite!=null?finalsuite.stypes:null));
                              
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1223:9: FINALLY COLON finalsuite= suite[true]
                    {
                    FINALLY168=(Token)match(input,FINALLY,FOLLOW_FINALLY_in_try_stmt4044); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FINALLY168_tree = (PythonTree)adaptor.create(FINALLY168);
                    adaptor.addChild(root_0, FINALLY168_tree);
                    }
                    COLON169=(Token)match(input,COLON,FOLLOW_COLON_in_try_stmt4046); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON169_tree = (PythonTree)adaptor.create(COLON169);
                    adaptor.addChild(root_0, COLON169_tree);
                    }
                    pushFollow(FOLLOW_suite_in_try_stmt4050);
                    finalsuite=suite(true);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, finalsuite.getTree());
                    if ( state.backtracking==0 ) {

                                  stype = actions.makeTryFinally(TRY162, (trysuite!=null?trysuite.stypes:null), (finalsuite!=null?finalsuite.stypes:null));
                              
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "try_stmt"

    public static class with_stmt_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "with_stmt"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1231:1: with_stmt : WITH w+= with_item ( options {greedy=true; } : COMMA w+= with_item )* COLON suite[false] ;
    public final TruffleParser.with_stmt_return with_stmt() throws RecognitionException {
        TruffleParser.with_stmt_return retval = new TruffleParser.with_stmt_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token WITH170=null;
        Token COMMA171=null;
        Token COLON172=null;
        List list_w=null;
        TruffleParser.suite_return suite173 = null;

        TruffleParser.with_item_return w = null;
         w = null;
        PythonTree WITH170_tree=null;
        PythonTree COMMA171_tree=null;
        PythonTree COLON172_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1238:5: ( WITH w+= with_item ( options {greedy=true; } : COMMA w+= with_item )* COLON suite[false] )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1238:7: WITH w+= with_item ( options {greedy=true; } : COMMA w+= with_item )* COLON suite[false]
            {
            root_0 = (PythonTree)adaptor.nil();

            WITH170=(Token)match(input,WITH,FOLLOW_WITH_in_with_stmt4099); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            WITH170_tree = (PythonTree)adaptor.create(WITH170);
            adaptor.addChild(root_0, WITH170_tree);
            }
            pushFollow(FOLLOW_with_item_in_with_stmt4103);
            w=with_item();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, w.getTree());
            if (list_w==null) list_w=new ArrayList();
            list_w.add(w.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1238:25: ( options {greedy=true; } : COMMA w+= with_item )*
            loop75:
            do {
                int alt75=2;
                int LA75_0 = input.LA(1);

                if ( (LA75_0==COMMA) ) {
                    alt75=1;
                }


                switch (alt75) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1238:49: COMMA w+= with_item
            	    {
            	    COMMA171=(Token)match(input,COMMA,FOLLOW_COMMA_in_with_stmt4113); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA171_tree = (PythonTree)adaptor.create(COMMA171);
            	    adaptor.addChild(root_0, COMMA171_tree);
            	    }
            	    pushFollow(FOLLOW_with_item_in_with_stmt4117);
            	    w=with_item();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, w.getTree());
            	    if (list_w==null) list_w=new ArrayList();
            	    list_w.add(w.getTree());


            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);

            COLON172=(Token)match(input,COLON,FOLLOW_COLON_in_with_stmt4121); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON172_tree = (PythonTree)adaptor.create(COLON172);
            adaptor.addChild(root_0, COLON172_tree);
            }
            pushFollow(FOLLOW_suite_in_with_stmt4123);
            suite173=suite(false);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, suite173.getTree());
            if ( state.backtracking==0 ) {

                        stype = actions.makeWith(WITH170, list_w, (suite173!=null?suite173.stypes:null));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "with_stmt"

    public static class with_item_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "with_item"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1245:1: with_item : test[expr_contextType.Load] ( AS expr[expr_contextType.Store] )? ;
    public final TruffleParser.with_item_return with_item() throws RecognitionException {
        TruffleParser.with_item_return retval = new TruffleParser.with_item_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token AS175=null;
        TruffleParser.test_return test174 = null;

        TruffleParser.expr_return expr176 = null;


        PythonTree AS175_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1252:5: ( test[expr_contextType.Load] ( AS expr[expr_contextType.Store] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1252:7: test[expr_contextType.Load] ( AS expr[expr_contextType.Store] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_test_in_with_item4160);
            test174=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, test174.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1252:35: ( AS expr[expr_contextType.Store] )?
            int alt76=2;
            int LA76_0 = input.LA(1);

            if ( (LA76_0==AS) ) {
                alt76=1;
            }
            switch (alt76) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1252:36: AS expr[expr_contextType.Store]
                    {
                    AS175=(Token)match(input,AS,FOLLOW_AS_in_with_item4164); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    AS175_tree = (PythonTree)adaptor.create(AS175);
                    adaptor.addChild(root_0, AS175_tree);
                    }
                    pushFollow(FOLLOW_expr_in_with_item4166);
                    expr176=expr(expr_contextType.Store);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr176.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        expr item = actions.castExpr((test174!=null?((PythonTree)test174.tree):null));
                        expr var = null;
                        if ((expr176!=null?((Token)expr176.start):null) != null) {
                            var = actions.castExpr((expr176!=null?((PythonTree)expr176.tree):null));
                            actions.checkAssign(var);
                        }
                        stype = new With((test174!=null?((Token)test174.start):null), item, var, null);
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "with_item"

    public static class except_clause_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "except_clause"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1265:1: except_clause : EXCEPT (t1= test[expr_contextType.Load] ( ( COMMA | AS ) t2= test[expr_contextType.Store] )? )? COLON suite[!$suite.isEmpty() && $suite::continueIllegal] ;
    public final TruffleParser.except_clause_return except_clause() throws RecognitionException {
        TruffleParser.except_clause_return retval = new TruffleParser.except_clause_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token EXCEPT177=null;
        Token set178=null;
        Token COLON179=null;
        TruffleParser.test_return t1 = null;

        TruffleParser.test_return t2 = null;

        TruffleParser.suite_return suite180 = null;


        PythonTree EXCEPT177_tree=null;
        PythonTree set178_tree=null;
        PythonTree COLON179_tree=null;


            excepthandler extype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1272:5: ( EXCEPT (t1= test[expr_contextType.Load] ( ( COMMA | AS ) t2= test[expr_contextType.Store] )? )? COLON suite[!$suite.isEmpty() && $suite::continueIllegal] )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1272:7: EXCEPT (t1= test[expr_contextType.Load] ( ( COMMA | AS ) t2= test[expr_contextType.Store] )? )? COLON suite[!$suite.isEmpty() && $suite::continueIllegal]
            {
            root_0 = (PythonTree)adaptor.nil();

            EXCEPT177=(Token)match(input,EXCEPT,FOLLOW_EXCEPT_in_except_clause4205); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EXCEPT177_tree = (PythonTree)adaptor.create(EXCEPT177);
            adaptor.addChild(root_0, EXCEPT177_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1272:14: (t1= test[expr_contextType.Load] ( ( COMMA | AS ) t2= test[expr_contextType.Store] )? )?
            int alt78=2;
            int LA78_0 = input.LA(1);

            if ( (LA78_0==NAME||LA78_0==NOT||LA78_0==LPAREN||(LA78_0>=PLUS && LA78_0<=MINUS)||(LA78_0>=TILDE && LA78_0<=LBRACK)||LA78_0==LCURLY||LA78_0==BACKQUOTE) ) {
                alt78=1;
            }
            else if ( (LA78_0==PRINT) && ((printFunction))) {
                alt78=1;
            }
            else if ( (LA78_0==LAMBDA||(LA78_0>=NONE && LA78_0<=FALSE)||(LA78_0>=INT && LA78_0<=STRING)) ) {
                alt78=1;
            }
            switch (alt78) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1272:15: t1= test[expr_contextType.Load] ( ( COMMA | AS ) t2= test[expr_contextType.Store] )?
                    {
                    pushFollow(FOLLOW_test_in_except_clause4210);
                    t1=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t1.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1272:46: ( ( COMMA | AS ) t2= test[expr_contextType.Store] )?
                    int alt77=2;
                    int LA77_0 = input.LA(1);

                    if ( (LA77_0==AS||LA77_0==COMMA) ) {
                        alt77=1;
                    }
                    switch (alt77) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1272:47: ( COMMA | AS ) t2= test[expr_contextType.Store]
                            {
                            set178=(Token)input.LT(1);
                            if ( input.LA(1)==AS||input.LA(1)==COMMA ) {
                                input.consume();
                                if ( state.backtracking==0 ) adaptor.addChild(root_0, (PythonTree)adaptor.create(set178));
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                throw mse;
                            }

                            pushFollow(FOLLOW_test_in_except_clause4224);
                            t2=test(expr_contextType.Store);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, t2.getTree());

                            }
                            break;

                    }


                    }
                    break;

            }

            COLON179=(Token)match(input,COLON,FOLLOW_COLON_in_except_clause4231); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON179_tree = (PythonTree)adaptor.create(COLON179);
            adaptor.addChild(root_0, COLON179_tree);
            }
            pushFollow(FOLLOW_suite_in_except_clause4233);
            suite180=suite(!suite_stack.isEmpty() && ((suite_scope)suite_stack.peek()).continueIllegal);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, suite180.getTree());
            if ( state.backtracking==0 ) {

                        extype = new ExceptHandler(EXCEPT177, actions.castExpr((t1!=null?((PythonTree)t1.tree):null)), actions.castExpr((t2!=null?((PythonTree)t2.tree):null)),
                            actions.castStmts((suite180!=null?suite180.stypes:null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = extype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "except_clause"

    protected static class suite_scope {
        boolean continueIllegal;
    }
    protected Stack suite_stack = new Stack();

    public static class suite_return extends ParserRuleReturnScope {
        public List stypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "suite"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1280:1: suite[boolean fromFinally] returns [List stypes] : ( simple_stmt | NEWLINE INDENT ( stmt )+ DEDENT );
    public final TruffleParser.suite_return suite(boolean fromFinally) throws RecognitionException {
        suite_stack.push(new suite_scope());
        TruffleParser.suite_return retval = new TruffleParser.suite_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token NEWLINE182=null;
        Token INDENT183=null;
        Token DEDENT185=null;
        TruffleParser.simple_stmt_return simple_stmt181 = null;

        TruffleParser.stmt_return stmt184 = null;


        PythonTree NEWLINE182_tree=null;
        PythonTree INDENT183_tree=null;
        PythonTree DEDENT185_tree=null;


            if (((suite_scope)suite_stack.peek()).continueIllegal || fromFinally) {
                ((suite_scope)suite_stack.peek()).continueIllegal = true;
            } else {
                ((suite_scope)suite_stack.peek()).continueIllegal = false;
            }
            retval.stypes = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1293:5: ( simple_stmt | NEWLINE INDENT ( stmt )+ DEDENT )
            int alt80=2;
            int LA80_0 = input.LA(1);

            if ( (LA80_0==NAME||LA80_0==NOT||LA80_0==LPAREN||(LA80_0>=PLUS && LA80_0<=MINUS)||(LA80_0>=TILDE && LA80_0<=LBRACK)||LA80_0==LCURLY||LA80_0==BACKQUOTE) ) {
                alt80=1;
            }
            else if ( (LA80_0==PRINT) && (((!printFunction)||(printFunction)))) {
                alt80=1;
            }
            else if ( ((LA80_0>=ASSERT && LA80_0<=BREAK)||LA80_0==CONTINUE||LA80_0==DELETE||LA80_0==FROM||LA80_0==GLOBAL||LA80_0==IMPORT||LA80_0==LAMBDA||(LA80_0>=PASS && LA80_0<=RETURN)||(LA80_0>=YIELD && LA80_0<=NONLOCAL)||(LA80_0>=INT && LA80_0<=STRING)) ) {
                alt80=1;
            }
            else if ( (LA80_0==NEWLINE) ) {
                alt80=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 80, 0, input);

                throw nvae;
            }
            switch (alt80) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1293:7: simple_stmt
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_simple_stmt_in_suite4279);
                    simple_stmt181=simple_stmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, simple_stmt181.getTree());
                    if ( state.backtracking==0 ) {

                                retval.stypes = (simple_stmt181!=null?simple_stmt181.stypes:null);
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1297:7: NEWLINE INDENT ( stmt )+ DEDENT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    NEWLINE182=(Token)match(input,NEWLINE,FOLLOW_NEWLINE_in_suite4295); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NEWLINE182_tree = (PythonTree)adaptor.create(NEWLINE182);
                    adaptor.addChild(root_0, NEWLINE182_tree);
                    }
                    INDENT183=(Token)match(input,INDENT,FOLLOW_INDENT_in_suite4297); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INDENT183_tree = (PythonTree)adaptor.create(INDENT183);
                    adaptor.addChild(root_0, INDENT183_tree);
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1298:7: ( stmt )+
                    int cnt79=0;
                    loop79:
                    do {
                        int alt79=2;
                        int LA79_0 = input.LA(1);

                        if ( (LA79_0==NAME||LA79_0==NOT||LA79_0==LPAREN||(LA79_0>=PLUS && LA79_0<=MINUS)||(LA79_0>=TILDE && LA79_0<=LBRACK)||LA79_0==LCURLY||LA79_0==BACKQUOTE) ) {
                            alt79=1;
                        }
                        else if ( (LA79_0==PRINT) && (((!printFunction)||(printFunction)))) {
                            alt79=1;
                        }
                        else if ( ((LA79_0>=ASSERT && LA79_0<=DELETE)||(LA79_0>=FROM && LA79_0<=IMPORT)||LA79_0==LAMBDA||(LA79_0>=PASS && LA79_0<=AT)||(LA79_0>=INT && LA79_0<=STRING)) ) {
                            alt79=1;
                        }


                        switch (alt79) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1298:8: stmt
                    	    {
                    	    pushFollow(FOLLOW_stmt_in_suite4306);
                    	    stmt184=stmt();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, stmt184.getTree());
                    	    if ( state.backtracking==0 ) {

                    	                 if ((stmt184!=null?stmt184.stypes:null) != null) {
                    	                     retval.stypes.addAll((stmt184!=null?stmt184.stypes:null));
                    	                 }
                    	             
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt79 >= 1 ) break loop79;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(79, input);
                                throw eee;
                        }
                        cnt79++;
                    } while (true);

                    DEDENT185=(Token)match(input,DEDENT,FOLLOW_DEDENT_in_suite4326); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DEDENT185_tree = (PythonTree)adaptor.create(DEDENT185);
                    adaptor.addChild(root_0, DEDENT185_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
            suite_stack.pop();
        }
        return retval;
    }
    // $ANTLR end "suite"

    public static class test_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "test"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1308:1: test[expr_contextType ctype] : (o1= or_test[ctype] ( ( IF or_test[null] ORELSE )=> IF o2= or_test[ctype] ORELSE e= test[expr_contextType.Load] | -> or_test ) | lambdef );
    public final TruffleParser.test_return test(expr_contextType ctype) throws RecognitionException {
        TruffleParser.test_return retval = new TruffleParser.test_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token IF186=null;
        Token ORELSE187=null;
        TruffleParser.or_test_return o1 = null;

        TruffleParser.or_test_return o2 = null;

        TruffleParser.test_return e = null;

        TruffleParser.lambdef_return lambdef188 = null;


        PythonTree IF186_tree=null;
        PythonTree ORELSE187_tree=null;
        RewriteRuleTokenStream stream_ORELSE=new RewriteRuleTokenStream(adaptor,"token ORELSE");
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_test=new RewriteRuleSubtreeStream(adaptor,"rule test");
        RewriteRuleSubtreeStream stream_or_test=new RewriteRuleSubtreeStream(adaptor,"rule or_test");

            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1317:5: (o1= or_test[ctype] ( ( IF or_test[null] ORELSE )=> IF o2= or_test[ctype] ORELSE e= test[expr_contextType.Load] | -> or_test ) | lambdef )
            int alt82=2;
            int LA82_0 = input.LA(1);

            if ( (LA82_0==NAME||LA82_0==NOT||LA82_0==LPAREN||(LA82_0>=PLUS && LA82_0<=MINUS)||(LA82_0>=TILDE && LA82_0<=LBRACK)||LA82_0==LCURLY||LA82_0==BACKQUOTE) ) {
                alt82=1;
            }
            else if ( (LA82_0==PRINT) && ((printFunction))) {
                alt82=1;
            }
            else if ( ((LA82_0>=NONE && LA82_0<=FALSE)||(LA82_0>=INT && LA82_0<=STRING)) ) {
                alt82=1;
            }
            else if ( (LA82_0==LAMBDA) ) {
                alt82=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                throw nvae;
            }
            switch (alt82) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1317:6: o1= or_test[ctype] ( ( IF or_test[null] ORELSE )=> IF o2= or_test[ctype] ORELSE e= test[expr_contextType.Load] | -> or_test )
                    {
                    pushFollow(FOLLOW_or_test_in_test4356);
                    o1=or_test(ctype);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_or_test.add(o1.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1318:7: ( ( IF or_test[null] ORELSE )=> IF o2= or_test[ctype] ORELSE e= test[expr_contextType.Load] | -> or_test )
                    int alt81=2;
                    alt81 = dfa81.predict(input);
                    switch (alt81) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1318:9: ( IF or_test[null] ORELSE )=> IF o2= or_test[ctype] ORELSE e= test[expr_contextType.Load]
                            {
                            IF186=(Token)match(input,IF,FOLLOW_IF_in_test4378); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_IF.add(IF186);

                            pushFollow(FOLLOW_or_test_in_test4382);
                            o2=or_test(ctype);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_or_test.add(o2.getTree());
                            ORELSE187=(Token)match(input,ORELSE,FOLLOW_ORELSE_in_test4385); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_ORELSE.add(ORELSE187);

                            pushFollow(FOLLOW_test_in_test4389);
                            e=test(expr_contextType.Load);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_test.add(e.getTree());
                            if ( state.backtracking==0 ) {

                                           etype = new IfExp((o1!=null?((Token)o1.start):null), actions.castExpr((o2!=null?((PythonTree)o2.tree):null)), actions.castExpr((o1!=null?((PythonTree)o1.tree):null)), actions.castExpr((e!=null?((PythonTree)e.tree):null)));
                                       
                            }

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1323:6: 
                            {

                            // AST REWRITE
                            // elements: or_test
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (PythonTree)adaptor.nil();
                            // 1323:6: -> or_test
                            {
                                adaptor.addChild(root_0, stream_or_test.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1325:7: lambdef
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_lambdef_in_test4434);
                    lambdef188=lambdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, lambdef188.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 if (etype != null) {
                     retval.tree = etype;
                 }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "test"

    public static class or_test_return extends ParserRuleReturnScope {
        public Token leftTok;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "or_test"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1329:1: or_test[expr_contextType ctype] returns [Token leftTok] : left= and_test[ctype] ( (or= OR right+= and_test[ctype] )+ | -> $left) ;
    public final TruffleParser.or_test_return or_test(expr_contextType ctype) throws RecognitionException {
        TruffleParser.or_test_return retval = new TruffleParser.or_test_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token or=null;
        List list_right=null;
        TruffleParser.and_test_return left = null;

        TruffleParser.and_test_return right = null;
         right = null;
        PythonTree or_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_and_test=new RewriteRuleSubtreeStream(adaptor,"rule and_test");
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1340:5: (left= and_test[ctype] ( (or= OR right+= and_test[ctype] )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1340:7: left= and_test[ctype] ( (or= OR right+= and_test[ctype] )+ | -> $left)
            {
            pushFollow(FOLLOW_and_test_in_or_test4469);
            left=and_test(ctype);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_and_test.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1341:9: ( (or= OR right+= and_test[ctype] )+ | -> $left)
            int alt84=2;
            int LA84_0 = input.LA(1);

            if ( (LA84_0==OR) ) {
                alt84=1;
            }
            else if ( (LA84_0==EOF||LA84_0==NEWLINE||LA84_0==AS||LA84_0==FOR||LA84_0==IF||LA84_0==ORELSE||(LA84_0>=RPAREN && LA84_0<=COMMA)||(LA84_0>=SEMI && LA84_0<=DOUBLESLASHEQUAL)||LA84_0==RBRACK||(LA84_0>=RCURLY && LA84_0<=BACKQUOTE)) ) {
                alt84=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 84, 0, input);

                throw nvae;
            }
            switch (alt84) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1341:11: (or= OR right+= and_test[ctype] )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1341:11: (or= OR right+= and_test[ctype] )+
                    int cnt83=0;
                    loop83:
                    do {
                        int alt83=2;
                        int LA83_0 = input.LA(1);

                        if ( (LA83_0==OR) ) {
                            alt83=1;
                        }


                        switch (alt83) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1341:12: or= OR right+= and_test[ctype]
                    	    {
                    	    or=(Token)match(input,OR,FOLLOW_OR_in_or_test4485); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_OR.add(or);

                    	    pushFollow(FOLLOW_and_test_in_or_test4489);
                    	    right=and_test(ctype);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_and_test.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt83 >= 1 ) break loop83;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(83, input);
                                throw eee;
                        }
                        cnt83++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1344:8: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1344:8: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (or != null) {
                      Token tok = (left!=null?((Token)left.start):null);
                      if ((left!=null?left.leftTok:null) != null) {
                          tok = (left!=null?left.leftTok:null);
                      }
                      retval.tree = actions.makeBoolOp(tok, (left!=null?((PythonTree)left.tree):null), boolopType.Or, list_right);
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "or_test"

    public static class and_test_return extends ParserRuleReturnScope {
        public Token leftTok;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "and_test"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1349:1: and_test[expr_contextType ctype] returns [Token leftTok] : left= not_test[ctype] ( (and= AND right+= not_test[ctype] )+ | -> $left) ;
    public final TruffleParser.and_test_return and_test(expr_contextType ctype) throws RecognitionException {
        TruffleParser.and_test_return retval = new TruffleParser.and_test_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token and=null;
        List list_right=null;
        TruffleParser.not_test_return left = null;

        TruffleParser.not_test_return right = null;
         right = null;
        PythonTree and_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_not_test=new RewriteRuleSubtreeStream(adaptor,"rule not_test");
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1360:5: (left= not_test[ctype] ( (and= AND right+= not_test[ctype] )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1360:7: left= not_test[ctype] ( (and= AND right+= not_test[ctype] )+ | -> $left)
            {
            pushFollow(FOLLOW_not_test_in_and_test4570);
            left=not_test(ctype);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_not_test.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1361:9: ( (and= AND right+= not_test[ctype] )+ | -> $left)
            int alt86=2;
            int LA86_0 = input.LA(1);

            if ( (LA86_0==AND) ) {
                alt86=1;
            }
            else if ( (LA86_0==EOF||LA86_0==NEWLINE||LA86_0==AS||LA86_0==FOR||LA86_0==IF||(LA86_0>=OR && LA86_0<=ORELSE)||(LA86_0>=RPAREN && LA86_0<=COMMA)||(LA86_0>=SEMI && LA86_0<=DOUBLESLASHEQUAL)||LA86_0==RBRACK||(LA86_0>=RCURLY && LA86_0<=BACKQUOTE)) ) {
                alt86=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 86, 0, input);

                throw nvae;
            }
            switch (alt86) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1361:11: (and= AND right+= not_test[ctype] )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1361:11: (and= AND right+= not_test[ctype] )+
                    int cnt85=0;
                    loop85:
                    do {
                        int alt85=2;
                        int LA85_0 = input.LA(1);

                        if ( (LA85_0==AND) ) {
                            alt85=1;
                        }


                        switch (alt85) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1361:12: and= AND right+= not_test[ctype]
                    	    {
                    	    and=(Token)match(input,AND,FOLLOW_AND_in_and_test4586); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_AND.add(and);

                    	    pushFollow(FOLLOW_not_test_in_and_test4590);
                    	    right=not_test(ctype);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_not_test.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt85 >= 1 ) break loop85;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(85, input);
                                throw eee;
                        }
                        cnt85++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1364:8: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1364:8: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (and != null) {
                      Token tok = (left!=null?((Token)left.start):null);
                      if ((left!=null?left.leftTok:null) != null) {
                          tok = (left!=null?left.leftTok:null);
                      }
                      retval.tree = actions.makeBoolOp(tok, (left!=null?((PythonTree)left.tree):null), boolopType.And, list_right);
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "and_test"

    public static class not_test_return extends ParserRuleReturnScope {
        public Token leftTok;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "not_test"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1369:1: not_test[expr_contextType ctype] returns [Token leftTok] : ( NOT nt= not_test[ctype] | comparison[ctype] );
    public final TruffleParser.not_test_return not_test(expr_contextType ctype) throws RecognitionException {
        TruffleParser.not_test_return retval = new TruffleParser.not_test_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token NOT189=null;
        TruffleParser.not_test_return nt = null;

        TruffleParser.comparison_return comparison190 = null;


        PythonTree NOT189_tree=null;


            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1379:5: ( NOT nt= not_test[ctype] | comparison[ctype] )
            int alt87=2;
            int LA87_0 = input.LA(1);

            if ( (LA87_0==NOT) ) {
                alt87=1;
            }
            else if ( (LA87_0==NAME||LA87_0==LPAREN||(LA87_0>=PLUS && LA87_0<=MINUS)||(LA87_0>=TILDE && LA87_0<=LBRACK)||LA87_0==LCURLY||LA87_0==BACKQUOTE) ) {
                alt87=2;
            }
            else if ( (LA87_0==PRINT) && ((printFunction))) {
                alt87=2;
            }
            else if ( ((LA87_0>=NONE && LA87_0<=FALSE)||(LA87_0>=INT && LA87_0<=STRING)) ) {
                alt87=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                throw nvae;
            }
            switch (alt87) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1379:7: NOT nt= not_test[ctype]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    NOT189=(Token)match(input,NOT,FOLLOW_NOT_in_not_test4674); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOT189_tree = (PythonTree)adaptor.create(NOT189);
                    adaptor.addChild(root_0, NOT189_tree);
                    }
                    pushFollow(FOLLOW_not_test_in_not_test4678);
                    nt=not_test(ctype);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, nt.getTree());
                    if ( state.backtracking==0 ) {

                                etype = new UnaryOp(NOT189, unaryopType.Not, actions.castExpr((nt!=null?((PythonTree)nt.tree):null)));
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1383:7: comparison[ctype]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_comparison_in_not_test4695);
                    comparison190=comparison(ctype);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comparison190.getTree());
                    if ( state.backtracking==0 ) {

                                retval.leftTok = (comparison190!=null?comparison190.leftTok:null);
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 if (etype != null) {
                     retval.tree = etype;
                 }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "not_test"

    public static class comparison_return extends ParserRuleReturnScope {
        public Token leftTok;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comparison"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1390:1: comparison[expr_contextType ctype] returns [Token leftTok] : left= expr[ctype] ( ( comp_op right+= expr[ctype] )+ | -> $left) ;
    public final TruffleParser.comparison_return comparison(expr_contextType ctype) throws RecognitionException {
        TruffleParser.comparison_return retval = new TruffleParser.comparison_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        List list_right=null;
        TruffleParser.expr_return left = null;

        TruffleParser.comp_op_return comp_op191 = null;

        TruffleParser.expr_return right = null;
         right = null;
        RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr");
        RewriteRuleSubtreeStream stream_comp_op=new RewriteRuleSubtreeStream(adaptor,"rule comp_op");

            List cmps = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1402:5: (left= expr[ctype] ( ( comp_op right+= expr[ctype] )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1402:7: left= expr[ctype] ( ( comp_op right+= expr[ctype] )+ | -> $left)
            {
            pushFollow(FOLLOW_expr_in_comparison4744);
            left=expr(ctype);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_expr.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1403:8: ( ( comp_op right+= expr[ctype] )+ | -> $left)
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( ((LA89_0>=IN && LA89_0<=IS)||LA89_0==NOT||(LA89_0>=LESS && LA89_0<=NOTEQUAL)) ) {
                alt89=1;
            }
            else if ( (LA89_0==EOF||LA89_0==NEWLINE||(LA89_0>=AND && LA89_0<=AS)||LA89_0==FOR||LA89_0==IF||(LA89_0>=OR && LA89_0<=ORELSE)||(LA89_0>=RPAREN && LA89_0<=COMMA)||(LA89_0>=SEMI && LA89_0<=DOUBLESLASHEQUAL)||LA89_0==RBRACK||(LA89_0>=RCURLY && LA89_0<=BACKQUOTE)) ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }
            switch (alt89) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1403:10: ( comp_op right+= expr[ctype] )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1403:10: ( comp_op right+= expr[ctype] )+
                    int cnt88=0;
                    loop88:
                    do {
                        int alt88=2;
                        int LA88_0 = input.LA(1);

                        if ( ((LA88_0>=IN && LA88_0<=IS)||LA88_0==NOT||(LA88_0>=LESS && LA88_0<=NOTEQUAL)) ) {
                            alt88=1;
                        }


                        switch (alt88) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1403:12: comp_op right+= expr[ctype]
                    	    {
                    	    pushFollow(FOLLOW_comp_op_in_comparison4758);
                    	    comp_op191=comp_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_comp_op.add(comp_op191.getTree());
                    	    pushFollow(FOLLOW_expr_in_comparison4762);
                    	    right=expr(ctype);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_expr.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());

                    	    if ( state.backtracking==0 ) {

                    	                     cmps.add((comp_op191!=null?comp_op191.op:null));
                    	                 
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt88 >= 1 ) break loop88;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(88, input);
                                throw eee;
                        }
                        cnt88++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1409:7: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1409:7: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.leftTok = (left!=null?left.leftTok:null);
                  if (!cmps.isEmpty()) {
                      retval.tree = new Compare((left!=null?((Token)left.start):null), actions.castExpr((left!=null?((PythonTree)left.tree):null)), actions.makeCmpOps(cmps),
                          actions.castExprs(list_right));
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comparison"

    public static class comp_op_return extends ParserRuleReturnScope {
        public cmpopType op;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp_op"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1414:1: comp_op returns [cmpopType op] : ( LESS | GREATER | EQUAL | GREATEREQUAL | LESSEQUAL | NOTEQUAL | IN | NOT IN | IS | IS NOT );
    public final TruffleParser.comp_op_return comp_op() throws RecognitionException {
        TruffleParser.comp_op_return retval = new TruffleParser.comp_op_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token LESS192=null;
        Token GREATER193=null;
        Token EQUAL194=null;
        Token GREATEREQUAL195=null;
        Token LESSEQUAL196=null;
        Token NOTEQUAL197=null;
        Token IN198=null;
        Token NOT199=null;
        Token IN200=null;
        Token IS201=null;
        Token IS202=null;
        Token NOT203=null;

        PythonTree LESS192_tree=null;
        PythonTree GREATER193_tree=null;
        PythonTree EQUAL194_tree=null;
        PythonTree GREATEREQUAL195_tree=null;
        PythonTree LESSEQUAL196_tree=null;
        PythonTree NOTEQUAL197_tree=null;
        PythonTree IN198_tree=null;
        PythonTree NOT199_tree=null;
        PythonTree IN200_tree=null;
        PythonTree IS201_tree=null;
        PythonTree IS202_tree=null;
        PythonTree NOT203_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1416:5: ( LESS | GREATER | EQUAL | GREATEREQUAL | LESSEQUAL | NOTEQUAL | IN | NOT IN | IS | IS NOT )
            int alt90=10;
            alt90 = dfa90.predict(input);
            switch (alt90) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1416:7: LESS
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    LESS192=(Token)match(input,LESS,FOLLOW_LESS_in_comp_op4843); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LESS192_tree = (PythonTree)adaptor.create(LESS192);
                    adaptor.addChild(root_0, LESS192_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.Lt;
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1420:7: GREATER
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    GREATER193=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp_op4859); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GREATER193_tree = (PythonTree)adaptor.create(GREATER193);
                    adaptor.addChild(root_0, GREATER193_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.Gt;
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1424:7: EQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    EQUAL194=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_comp_op4875); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQUAL194_tree = (PythonTree)adaptor.create(EQUAL194);
                    adaptor.addChild(root_0, EQUAL194_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.Eq;
                            
                    }

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1428:7: GREATEREQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    GREATEREQUAL195=(Token)match(input,GREATEREQUAL,FOLLOW_GREATEREQUAL_in_comp_op4891); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GREATEREQUAL195_tree = (PythonTree)adaptor.create(GREATEREQUAL195);
                    adaptor.addChild(root_0, GREATEREQUAL195_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.GtE;
                            
                    }

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1432:7: LESSEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    LESSEQUAL196=(Token)match(input,LESSEQUAL,FOLLOW_LESSEQUAL_in_comp_op4907); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LESSEQUAL196_tree = (PythonTree)adaptor.create(LESSEQUAL196);
                    adaptor.addChild(root_0, LESSEQUAL196_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.LtE;
                            
                    }

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1440:7: NOTEQUAL
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    NOTEQUAL197=(Token)match(input,NOTEQUAL,FOLLOW_NOTEQUAL_in_comp_op4943); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOTEQUAL197_tree = (PythonTree)adaptor.create(NOTEQUAL197);
                    adaptor.addChild(root_0, NOTEQUAL197_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.NotEq;
                            
                    }

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1444:7: IN
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    IN198=(Token)match(input,IN,FOLLOW_IN_in_comp_op4959); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IN198_tree = (PythonTree)adaptor.create(IN198);
                    adaptor.addChild(root_0, IN198_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.In;
                            
                    }

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1448:7: NOT IN
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    NOT199=(Token)match(input,NOT,FOLLOW_NOT_in_comp_op4975); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOT199_tree = (PythonTree)adaptor.create(NOT199);
                    adaptor.addChild(root_0, NOT199_tree);
                    }
                    IN200=(Token)match(input,IN,FOLLOW_IN_in_comp_op4977); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IN200_tree = (PythonTree)adaptor.create(IN200);
                    adaptor.addChild(root_0, IN200_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.NotIn;
                            
                    }

                    }
                    break;
                case 9 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1452:7: IS
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    IS201=(Token)match(input,IS,FOLLOW_IS_in_comp_op4993); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IS201_tree = (PythonTree)adaptor.create(IS201);
                    adaptor.addChild(root_0, IS201_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.Is;
                            
                    }

                    }
                    break;
                case 10 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1456:7: IS NOT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    IS202=(Token)match(input,IS,FOLLOW_IS_in_comp_op5009); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IS202_tree = (PythonTree)adaptor.create(IS202);
                    adaptor.addChild(root_0, IS202_tree);
                    }
                    NOT203=(Token)match(input,NOT,FOLLOW_NOT_in_comp_op5011); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOT203_tree = (PythonTree)adaptor.create(NOT203);
                    adaptor.addChild(root_0, NOT203_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = cmpopType.IsNot;
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comp_op"

    protected static class expr_scope {
        expr_contextType ctype;
    }
    protected Stack expr_stack = new Stack();

    public static class expr_return extends ParserRuleReturnScope {
        public Token leftTok;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1463:1: expr[expr_contextType ect] returns [Token leftTok] : left= xor_expr ( (op= VBAR right+= xor_expr )+ | -> $left) ;
    public final TruffleParser.expr_return expr(expr_contextType ect) throws RecognitionException {
        expr_stack.push(new expr_scope());
        TruffleParser.expr_return retval = new TruffleParser.expr_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token op=null;
        List list_right=null;
        TruffleParser.xor_expr_return left = null;

        TruffleParser.xor_expr_return right = null;
         right = null;
        PythonTree op_tree=null;
        RewriteRuleTokenStream stream_VBAR=new RewriteRuleTokenStream(adaptor,"token VBAR");
        RewriteRuleSubtreeStream stream_xor_expr=new RewriteRuleSubtreeStream(adaptor,"rule xor_expr");

            ((expr_scope)expr_stack.peek()).ctype = ect;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1481:5: (left= xor_expr ( (op= VBAR right+= xor_expr )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1481:7: left= xor_expr ( (op= VBAR right+= xor_expr )+ | -> $left)
            {
            pushFollow(FOLLOW_xor_expr_in_expr5063);
            left=xor_expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_xor_expr.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1482:9: ( (op= VBAR right+= xor_expr )+ | -> $left)
            int alt92=2;
            int LA92_0 = input.LA(1);

            if ( (LA92_0==VBAR) ) {
                alt92=1;
            }
            else if ( (LA92_0==EOF||LA92_0==NEWLINE||(LA92_0>=AND && LA92_0<=AS)||LA92_0==FOR||LA92_0==IF||(LA92_0>=IN && LA92_0<=IS)||(LA92_0>=NOT && LA92_0<=ORELSE)||(LA92_0>=RPAREN && LA92_0<=COMMA)||(LA92_0>=SEMI && LA92_0<=DOUBLESLASHEQUAL)||(LA92_0>=LESS && LA92_0<=NOTEQUAL)||LA92_0==RBRACK||(LA92_0>=RCURLY && LA92_0<=BACKQUOTE)) ) {
                alt92=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 92, 0, input);

                throw nvae;
            }
            switch (alt92) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1482:11: (op= VBAR right+= xor_expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1482:11: (op= VBAR right+= xor_expr )+
                    int cnt91=0;
                    loop91:
                    do {
                        int alt91=2;
                        int LA91_0 = input.LA(1);

                        if ( (LA91_0==VBAR) ) {
                            alt91=1;
                        }


                        switch (alt91) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1482:12: op= VBAR right+= xor_expr
                    	    {
                    	    op=(Token)match(input,VBAR,FOLLOW_VBAR_in_expr5078); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_VBAR.add(op);

                    	    pushFollow(FOLLOW_xor_expr_in_expr5082);
                    	    right=xor_expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_xor_expr.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt91 >= 1 ) break loop91;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(91, input);
                                throw eee;
                        }
                        cnt91++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1485:8: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1485:8: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.leftTok = (left!=null?left.lparen:null);
                  if (op != null) {
                      Token tok = (left!=null?((Token)left.start):null);
                      if ((left!=null?left.lparen:null) != null) {
                          tok = (left!=null?left.lparen:null);
                      }
                      retval.tree = actions.makeBinOp(tok, (left!=null?((PythonTree)left.tree):null), operatorType.BitOr, list_right);
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
            expr_stack.pop();
        }
        return retval;
    }
    // $ANTLR end "expr"

    public static class xor_expr_return extends ParserRuleReturnScope {
        public Token lparen = null;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xor_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1491:1: xor_expr returns [Token lparen = null] : left= and_expr ( (op= CIRCUMFLEX right+= and_expr )+ | -> $left) ;
    public final TruffleParser.xor_expr_return xor_expr() throws RecognitionException {
        TruffleParser.xor_expr_return retval = new TruffleParser.xor_expr_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token op=null;
        List list_right=null;
        TruffleParser.and_expr_return left = null;

        TruffleParser.and_expr_return right = null;
         right = null;
        PythonTree op_tree=null;
        RewriteRuleTokenStream stream_CIRCUMFLEX=new RewriteRuleTokenStream(adaptor,"token CIRCUMFLEX");
        RewriteRuleSubtreeStream stream_and_expr=new RewriteRuleSubtreeStream(adaptor,"rule and_expr");
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1503:5: (left= and_expr ( (op= CIRCUMFLEX right+= and_expr )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1503:7: left= and_expr ( (op= CIRCUMFLEX right+= and_expr )+ | -> $left)
            {
            pushFollow(FOLLOW_and_expr_in_xor_expr5161);
            left=and_expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_and_expr.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1504:9: ( (op= CIRCUMFLEX right+= and_expr )+ | -> $left)
            int alt94=2;
            int LA94_0 = input.LA(1);

            if ( (LA94_0==CIRCUMFLEX) ) {
                alt94=1;
            }
            else if ( (LA94_0==EOF||LA94_0==NEWLINE||(LA94_0>=AND && LA94_0<=AS)||LA94_0==FOR||LA94_0==IF||(LA94_0>=IN && LA94_0<=IS)||(LA94_0>=NOT && LA94_0<=ORELSE)||(LA94_0>=RPAREN && LA94_0<=COMMA)||(LA94_0>=SEMI && LA94_0<=DOUBLESLASHEQUAL)||(LA94_0>=LESS && LA94_0<=VBAR)||LA94_0==RBRACK||(LA94_0>=RCURLY && LA94_0<=BACKQUOTE)) ) {
                alt94=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 94, 0, input);

                throw nvae;
            }
            switch (alt94) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1504:11: (op= CIRCUMFLEX right+= and_expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1504:11: (op= CIRCUMFLEX right+= and_expr )+
                    int cnt93=0;
                    loop93:
                    do {
                        int alt93=2;
                        int LA93_0 = input.LA(1);

                        if ( (LA93_0==CIRCUMFLEX) ) {
                            alt93=1;
                        }


                        switch (alt93) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1504:12: op= CIRCUMFLEX right+= and_expr
                    	    {
                    	    op=(Token)match(input,CIRCUMFLEX,FOLLOW_CIRCUMFLEX_in_xor_expr5176); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_CIRCUMFLEX.add(op);

                    	    pushFollow(FOLLOW_and_expr_in_xor_expr5180);
                    	    right=and_expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_and_expr.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt93 >= 1 ) break loop93;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(93, input);
                                throw eee;
                        }
                        cnt93++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1507:8: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1507:8: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (op != null) {
                      Token tok = (left!=null?((Token)left.start):null);
                      if ((left!=null?left.lparen:null) != null) {
                          tok = (left!=null?left.lparen:null);
                      }
                      retval.tree = actions.makeBinOp(tok, (left!=null?((PythonTree)left.tree):null), operatorType.BitXor, list_right);
                  }
                  retval.lparen = (left!=null?left.lparen:null);

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "xor_expr"

    public static class and_expr_return extends ParserRuleReturnScope {
        public Token lparen = null;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "and_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1512:1: and_expr returns [Token lparen = null] : left= shift_expr ( (op= AMPER right+= shift_expr )+ | -> $left) ;
    public final TruffleParser.and_expr_return and_expr() throws RecognitionException {
        TruffleParser.and_expr_return retval = new TruffleParser.and_expr_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token op=null;
        List list_right=null;
        TruffleParser.shift_expr_return left = null;

        TruffleParser.shift_expr_return right = null;
         right = null;
        PythonTree op_tree=null;
        RewriteRuleTokenStream stream_AMPER=new RewriteRuleTokenStream(adaptor,"token AMPER");
        RewriteRuleSubtreeStream stream_shift_expr=new RewriteRuleSubtreeStream(adaptor,"rule shift_expr");
        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1524:5: (left= shift_expr ( (op= AMPER right+= shift_expr )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1524:7: left= shift_expr ( (op= AMPER right+= shift_expr )+ | -> $left)
            {
            pushFollow(FOLLOW_shift_expr_in_and_expr5258);
            left=shift_expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift_expr.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1525:9: ( (op= AMPER right+= shift_expr )+ | -> $left)
            int alt96=2;
            int LA96_0 = input.LA(1);

            if ( (LA96_0==AMPER) ) {
                alt96=1;
            }
            else if ( (LA96_0==EOF||LA96_0==NEWLINE||(LA96_0>=AND && LA96_0<=AS)||LA96_0==FOR||LA96_0==IF||(LA96_0>=IN && LA96_0<=IS)||(LA96_0>=NOT && LA96_0<=ORELSE)||(LA96_0>=RPAREN && LA96_0<=COMMA)||(LA96_0>=SEMI && LA96_0<=DOUBLESLASHEQUAL)||(LA96_0>=LESS && LA96_0<=CIRCUMFLEX)||LA96_0==RBRACK||(LA96_0>=RCURLY && LA96_0<=BACKQUOTE)) ) {
                alt96=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 96, 0, input);

                throw nvae;
            }
            switch (alt96) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1525:11: (op= AMPER right+= shift_expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1525:11: (op= AMPER right+= shift_expr )+
                    int cnt95=0;
                    loop95:
                    do {
                        int alt95=2;
                        int LA95_0 = input.LA(1);

                        if ( (LA95_0==AMPER) ) {
                            alt95=1;
                        }


                        switch (alt95) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1525:12: op= AMPER right+= shift_expr
                    	    {
                    	    op=(Token)match(input,AMPER,FOLLOW_AMPER_in_and_expr5273); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_AMPER.add(op);

                    	    pushFollow(FOLLOW_shift_expr_in_and_expr5277);
                    	    right=shift_expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_shift_expr.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt95 >= 1 ) break loop95;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(95, input);
                                throw eee;
                        }
                        cnt95++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1528:8: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1528:8: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (op != null) {
                      Token tok = (left!=null?((Token)left.start):null);
                      if ((left!=null?left.lparen:null) != null) {
                          tok = (left!=null?left.lparen:null);
                      }
                      retval.tree = actions.makeBinOp(tok, (left!=null?((PythonTree)left.tree):null), operatorType.BitAnd, list_right);
                  }
                  retval.lparen = (left!=null?left.lparen:null);

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "and_expr"

    public static class shift_expr_return extends ParserRuleReturnScope {
        public Token lparen = null;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1533:1: shift_expr returns [Token lparen = null] : left= arith_expr ( ( shift_op right+= arith_expr )+ | -> $left) ;
    public final TruffleParser.shift_expr_return shift_expr() throws RecognitionException {
        TruffleParser.shift_expr_return retval = new TruffleParser.shift_expr_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        List list_right=null;
        TruffleParser.arith_expr_return left = null;

        TruffleParser.shift_op_return shift_op204 = null;

        TruffleParser.arith_expr_return right = null;
         right = null;
        RewriteRuleSubtreeStream stream_arith_expr=new RewriteRuleSubtreeStream(adaptor,"rule arith_expr");
        RewriteRuleSubtreeStream stream_shift_op=new RewriteRuleSubtreeStream(adaptor,"rule shift_op");

            List ops = new ArrayList();
            List toks = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1549:5: (left= arith_expr ( ( shift_op right+= arith_expr )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1549:7: left= arith_expr ( ( shift_op right+= arith_expr )+ | -> $left)
            {
            pushFollow(FOLLOW_arith_expr_in_shift_expr5360);
            left=arith_expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arith_expr.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1550:9: ( ( shift_op right+= arith_expr )+ | -> $left)
            int alt98=2;
            int LA98_0 = input.LA(1);

            if ( (LA98_0==RIGHTSHIFT||LA98_0==LEFTSHIFT) ) {
                alt98=1;
            }
            else if ( (LA98_0==EOF||LA98_0==NEWLINE||(LA98_0>=AND && LA98_0<=AS)||LA98_0==FOR||LA98_0==IF||(LA98_0>=IN && LA98_0<=IS)||(LA98_0>=NOT && LA98_0<=ORELSE)||(LA98_0>=RPAREN && LA98_0<=COMMA)||(LA98_0>=SEMI && LA98_0<=DOUBLESLASHEQUAL)||(LA98_0>=LESS && LA98_0<=AMPER)||LA98_0==RBRACK||(LA98_0>=RCURLY && LA98_0<=BACKQUOTE)) ) {
                alt98=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 98, 0, input);

                throw nvae;
            }
            switch (alt98) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1550:11: ( shift_op right+= arith_expr )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1550:11: ( shift_op right+= arith_expr )+
                    int cnt97=0;
                    loop97:
                    do {
                        int alt97=2;
                        int LA97_0 = input.LA(1);

                        if ( (LA97_0==RIGHTSHIFT||LA97_0==LEFTSHIFT) ) {
                            alt97=1;
                        }


                        switch (alt97) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1550:13: shift_op right+= arith_expr
                    	    {
                    	    pushFollow(FOLLOW_shift_op_in_shift_expr5374);
                    	    shift_op204=shift_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_shift_op.add(shift_op204.getTree());
                    	    pushFollow(FOLLOW_arith_expr_in_shift_expr5378);
                    	    right=arith_expr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arith_expr.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());

                    	    if ( state.backtracking==0 ) {

                    	                      ops.add((shift_op204!=null?shift_op204.op:null));
                    	                      toks.add((shift_op204!=null?((Token)shift_op204.start):null));
                    	                  
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt97 >= 1 ) break loop97;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(97, input);
                                throw eee;
                        }
                        cnt97++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1557:8: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1557:8: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (!ops.isEmpty()) {
                      Token tok = (left!=null?((Token)left.start):null);
                      if ((left!=null?left.lparen:null) != null) {
                          tok = (left!=null?left.lparen:null);
                      }
                      retval.tree = actions.makeBinOp(tok, (left!=null?((PythonTree)left.tree):null), ops, list_right, toks);
                  }
                  retval.lparen = (left!=null?left.lparen:null);

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "shift_expr"

    public static class shift_op_return extends ParserRuleReturnScope {
        public operatorType op;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift_op"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1561:1: shift_op returns [operatorType op] : ( LEFTSHIFT | RIGHTSHIFT );
    public final TruffleParser.shift_op_return shift_op() throws RecognitionException {
        TruffleParser.shift_op_return retval = new TruffleParser.shift_op_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token LEFTSHIFT205=null;
        Token RIGHTSHIFT206=null;

        PythonTree LEFTSHIFT205_tree=null;
        PythonTree RIGHTSHIFT206_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1563:5: ( LEFTSHIFT | RIGHTSHIFT )
            int alt99=2;
            int LA99_0 = input.LA(1);

            if ( (LA99_0==LEFTSHIFT) ) {
                alt99=1;
            }
            else if ( (LA99_0==RIGHTSHIFT) ) {
                alt99=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 99, 0, input);

                throw nvae;
            }
            switch (alt99) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1563:7: LEFTSHIFT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    LEFTSHIFT205=(Token)match(input,LEFTSHIFT,FOLLOW_LEFTSHIFT_in_shift_op5462); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LEFTSHIFT205_tree = (PythonTree)adaptor.create(LEFTSHIFT205);
                    adaptor.addChild(root_0, LEFTSHIFT205_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = operatorType.LShift;
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1567:7: RIGHTSHIFT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    RIGHTSHIFT206=(Token)match(input,RIGHTSHIFT,FOLLOW_RIGHTSHIFT_in_shift_op5478); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RIGHTSHIFT206_tree = (PythonTree)adaptor.create(RIGHTSHIFT206);
                    adaptor.addChild(root_0, RIGHTSHIFT206_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = operatorType.RShift;
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "shift_op"

    public static class arith_expr_return extends ParserRuleReturnScope {
        public Token lparen = null;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arith_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1574:1: arith_expr returns [Token lparen = null] : left= term ( ( arith_op right+= term )+ | -> $left) ;
    public final TruffleParser.arith_expr_return arith_expr() throws RecognitionException {
        TruffleParser.arith_expr_return retval = new TruffleParser.arith_expr_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        List list_right=null;
        TruffleParser.term_return left = null;

        TruffleParser.arith_op_return arith_op207 = null;

        TruffleParser.term_return right = null;
         right = null;
        RewriteRuleSubtreeStream stream_arith_op=new RewriteRuleSubtreeStream(adaptor,"rule arith_op");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");

            List ops = new ArrayList();
            List toks = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1590:5: (left= term ( ( arith_op right+= term )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1590:7: left= term ( ( arith_op right+= term )+ | -> $left)
            {
            pushFollow(FOLLOW_term_in_arith_expr5524);
            left=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1591:9: ( ( arith_op right+= term )+ | -> $left)
            int alt101=2;
            int LA101_0 = input.LA(1);

            if ( ((LA101_0>=PLUS && LA101_0<=MINUS)) ) {
                alt101=1;
            }
            else if ( (LA101_0==EOF||LA101_0==NEWLINE||(LA101_0>=AND && LA101_0<=AS)||LA101_0==FOR||LA101_0==IF||(LA101_0>=IN && LA101_0<=IS)||(LA101_0>=NOT && LA101_0<=ORELSE)||(LA101_0>=RPAREN && LA101_0<=COMMA)||(LA101_0>=SEMI && LA101_0<=RIGHTSHIFT)||(LA101_0>=LESS && LA101_0<=LEFTSHIFT)||LA101_0==RBRACK||(LA101_0>=RCURLY && LA101_0<=BACKQUOTE)) ) {
                alt101=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 101, 0, input);

                throw nvae;
            }
            switch (alt101) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1591:11: ( arith_op right+= term )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1591:11: ( arith_op right+= term )+
                    int cnt100=0;
                    loop100:
                    do {
                        int alt100=2;
                        int LA100_0 = input.LA(1);

                        if ( ((LA100_0>=PLUS && LA100_0<=MINUS)) ) {
                            alt100=1;
                        }


                        switch (alt100) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1591:12: arith_op right+= term
                    	    {
                    	    pushFollow(FOLLOW_arith_op_in_arith_expr5537);
                    	    arith_op207=arith_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arith_op.add(arith_op207.getTree());
                    	    pushFollow(FOLLOW_term_in_arith_expr5541);
                    	    right=term();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_term.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());

                    	    if ( state.backtracking==0 ) {

                    	                     ops.add((arith_op207!=null?arith_op207.op:null));
                    	                     toks.add((arith_op207!=null?((Token)arith_op207.start):null));
                    	                 
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt100 >= 1 ) break loop100;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(100, input);
                                throw eee;
                        }
                        cnt100++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1598:8: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1598:8: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (!ops.isEmpty()) {
                      Token tok = (left!=null?((Token)left.start):null);
                      if ((left!=null?left.lparen:null) != null) {
                          tok = (left!=null?left.lparen:null);
                      }
                      retval.tree = actions.makeBinOp(tok, (left!=null?((PythonTree)left.tree):null), ops, list_right, toks);
                  }
                  retval.lparen = (left!=null?left.lparen:null);

            }
        }
        catch (RewriteCardinalityException rce) {

                    PythonTree badNode = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), null);
                    retval.tree = badNode;
                    errorHandler.error("Internal Parser Error", badNode);
                
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arith_expr"

    public static class arith_op_return extends ParserRuleReturnScope {
        public operatorType op;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arith_op"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1610:1: arith_op returns [operatorType op] : ( PLUS | MINUS );
    public final TruffleParser.arith_op_return arith_op() throws RecognitionException {
        TruffleParser.arith_op_return retval = new TruffleParser.arith_op_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token PLUS208=null;
        Token MINUS209=null;

        PythonTree PLUS208_tree=null;
        PythonTree MINUS209_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1612:5: ( PLUS | MINUS )
            int alt102=2;
            int LA102_0 = input.LA(1);

            if ( (LA102_0==PLUS) ) {
                alt102=1;
            }
            else if ( (LA102_0==MINUS) ) {
                alt102=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 102, 0, input);

                throw nvae;
            }
            switch (alt102) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1612:7: PLUS
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    PLUS208=(Token)match(input,PLUS,FOLLOW_PLUS_in_arith_op5649); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PLUS208_tree = (PythonTree)adaptor.create(PLUS208);
                    adaptor.addChild(root_0, PLUS208_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = operatorType.Add;
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1616:7: MINUS
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    MINUS209=(Token)match(input,MINUS,FOLLOW_MINUS_in_arith_op5665); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINUS209_tree = (PythonTree)adaptor.create(MINUS209);
                    adaptor.addChild(root_0, MINUS209_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = operatorType.Sub;
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arith_op"

    public static class term_return extends ParserRuleReturnScope {
        public Token lparen = null;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "term"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1623:1: term returns [Token lparen = null] : left= factor ( ( term_op right+= factor )+ | -> $left) ;
    public final TruffleParser.term_return term() throws RecognitionException {
        TruffleParser.term_return retval = new TruffleParser.term_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        List list_right=null;
        TruffleParser.factor_return left = null;

        TruffleParser.term_op_return term_op210 = null;

        TruffleParser.factor_return right = null;
         right = null;
        RewriteRuleSubtreeStream stream_term_op=new RewriteRuleSubtreeStream(adaptor,"rule term_op");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");

            List ops = new ArrayList();
            List toks = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1639:5: (left= factor ( ( term_op right+= factor )+ | -> $left) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1639:7: left= factor ( ( term_op right+= factor )+ | -> $left)
            {
            pushFollow(FOLLOW_factor_in_term5711);
            left=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(left.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1640:9: ( ( term_op right+= factor )+ | -> $left)
            int alt104=2;
            int LA104_0 = input.LA(1);

            if ( (LA104_0==STAR||(LA104_0>=SLASH && LA104_0<=DOUBLESLASH)) ) {
                alt104=1;
            }
            else if ( (LA104_0==EOF||LA104_0==NEWLINE||(LA104_0>=AND && LA104_0<=AS)||LA104_0==FOR||LA104_0==IF||(LA104_0>=IN && LA104_0<=IS)||(LA104_0>=NOT && LA104_0<=ORELSE)||(LA104_0>=RPAREN && LA104_0<=COMMA)||(LA104_0>=SEMI && LA104_0<=RIGHTSHIFT)||(LA104_0>=LESS && LA104_0<=MINUS)||LA104_0==RBRACK||(LA104_0>=RCURLY && LA104_0<=BACKQUOTE)) ) {
                alt104=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 104, 0, input);

                throw nvae;
            }
            switch (alt104) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1640:11: ( term_op right+= factor )+
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1640:11: ( term_op right+= factor )+
                    int cnt103=0;
                    loop103:
                    do {
                        int alt103=2;
                        int LA103_0 = input.LA(1);

                        if ( (LA103_0==STAR||(LA103_0>=SLASH && LA103_0<=DOUBLESLASH)) ) {
                            alt103=1;
                        }


                        switch (alt103) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1640:12: term_op right+= factor
                    	    {
                    	    pushFollow(FOLLOW_term_op_in_term5724);
                    	    term_op210=term_op();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_term_op.add(term_op210.getTree());
                    	    pushFollow(FOLLOW_factor_in_term5728);
                    	    right=factor();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_factor.add(right.getTree());
                    	    if (list_right==null) list_right=new ArrayList();
                    	    list_right.add(right.getTree());

                    	    if ( state.backtracking==0 ) {

                    	                    ops.add((term_op210!=null?term_op210.op:null));
                    	                    toks.add((term_op210!=null?((Token)term_op210.start):null));
                    	                
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt103 >= 1 ) break loop103;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(103, input);
                                throw eee;
                        }
                        cnt103++;
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1647:8: 
                    {

                    // AST REWRITE
                    // elements: left
                    // token labels: 
                    // rule labels: retval, left
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_left=new RewriteRuleSubtreeStream(adaptor,"rule left",left!=null?left.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1647:8: -> $left
                    {
                        adaptor.addChild(root_0, stream_left.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.lparen = (left!=null?left.lparen:null);
                  if (!ops.isEmpty()) {
                      Token tok = (left!=null?((Token)left.start):null);
                      if ((left!=null?left.lparen:null) != null) {
                          tok = (left!=null?left.lparen:null);
                      }
                      retval.tree = actions.makeBinOp(tok, (left!=null?((PythonTree)left.tree):null), ops, list_right, toks);
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "term"

    public static class term_op_return extends ParserRuleReturnScope {
        public operatorType op;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "term_op"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1651:1: term_op returns [operatorType op] : ( STAR | SLASH | PERCENT | DOUBLESLASH );
    public final TruffleParser.term_op_return term_op() throws RecognitionException {
        TruffleParser.term_op_return retval = new TruffleParser.term_op_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token STAR211=null;
        Token SLASH212=null;
        Token PERCENT213=null;
        Token DOUBLESLASH214=null;

        PythonTree STAR211_tree=null;
        PythonTree SLASH212_tree=null;
        PythonTree PERCENT213_tree=null;
        PythonTree DOUBLESLASH214_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1653:5: ( STAR | SLASH | PERCENT | DOUBLESLASH )
            int alt105=4;
            switch ( input.LA(1) ) {
            case STAR:
                {
                alt105=1;
                }
                break;
            case SLASH:
                {
                alt105=2;
                }
                break;
            case PERCENT:
                {
                alt105=3;
                }
                break;
            case DOUBLESLASH:
                {
                alt105=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 105, 0, input);

                throw nvae;
            }

            switch (alt105) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1653:7: STAR
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    STAR211=(Token)match(input,STAR,FOLLOW_STAR_in_term_op5810); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STAR211_tree = (PythonTree)adaptor.create(STAR211);
                    adaptor.addChild(root_0, STAR211_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = operatorType.Mult;
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1657:7: SLASH
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    SLASH212=(Token)match(input,SLASH,FOLLOW_SLASH_in_term_op5826); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SLASH212_tree = (PythonTree)adaptor.create(SLASH212);
                    adaptor.addChild(root_0, SLASH212_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = operatorType.Div;
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1661:7: PERCENT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    PERCENT213=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_term_op5842); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PERCENT213_tree = (PythonTree)adaptor.create(PERCENT213);
                    adaptor.addChild(root_0, PERCENT213_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = operatorType.Mod;
                            
                    }

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1665:7: DOUBLESLASH
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    DOUBLESLASH214=(Token)match(input,DOUBLESLASH,FOLLOW_DOUBLESLASH_in_term_op5858); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOUBLESLASH214_tree = (PythonTree)adaptor.create(DOUBLESLASH214);
                    adaptor.addChild(root_0, DOUBLESLASH214_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.op = operatorType.FloorDiv;
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "term_op"

    public static class factor_return extends ParserRuleReturnScope {
        public expr etype;
        public Token lparen = null;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "factor"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1672:1: factor returns [expr etype, Token lparen = null] : ( PLUS p= factor | MINUS m= factor | TILDE t= factor | power );
    public final TruffleParser.factor_return factor() throws RecognitionException {
        TruffleParser.factor_return retval = new TruffleParser.factor_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token PLUS215=null;
        Token MINUS216=null;
        Token TILDE217=null;
        TruffleParser.factor_return p = null;

        TruffleParser.factor_return m = null;

        TruffleParser.factor_return t = null;

        TruffleParser.power_return power218 = null;


        PythonTree PLUS215_tree=null;
        PythonTree MINUS216_tree=null;
        PythonTree TILDE217_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1677:5: ( PLUS p= factor | MINUS m= factor | TILDE t= factor | power )
            int alt106=4;
            int LA106_0 = input.LA(1);

            if ( (LA106_0==PLUS) ) {
                alt106=1;
            }
            else if ( (LA106_0==MINUS) ) {
                alt106=2;
            }
            else if ( (LA106_0==TILDE) ) {
                alt106=3;
            }
            else if ( (LA106_0==NAME||LA106_0==LPAREN||LA106_0==LBRACK||LA106_0==LCURLY||LA106_0==BACKQUOTE) ) {
                alt106=4;
            }
            else if ( (LA106_0==PRINT) && ((printFunction))) {
                alt106=4;
            }
            else if ( ((LA106_0>=NONE && LA106_0<=FALSE)||(LA106_0>=INT && LA106_0<=STRING)) ) {
                alt106=4;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 106, 0, input);

                throw nvae;
            }
            switch (alt106) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1677:7: PLUS p= factor
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    PLUS215=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor5897); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PLUS215_tree = (PythonTree)adaptor.create(PLUS215);
                    adaptor.addChild(root_0, PLUS215_tree);
                    }
                    pushFollow(FOLLOW_factor_in_factor5901);
                    p=factor();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, p.getTree());
                    if ( state.backtracking==0 ) {

                      	          retval.etype = new UnaryOp(PLUS215, unaryopType.UAdd, (p!=null?p.etype:null));
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1681:7: MINUS m= factor
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    MINUS216=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor5917); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINUS216_tree = (PythonTree)adaptor.create(MINUS216);
                    adaptor.addChild(root_0, MINUS216_tree);
                    }
                    pushFollow(FOLLOW_factor_in_factor5921);
                    m=factor();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, m.getTree());
                    if ( state.backtracking==0 ) {

                      	          retval.etype = actions.negate(MINUS216, (m!=null?m.etype:null));
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1685:7: TILDE t= factor
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    TILDE217=(Token)match(input,TILDE,FOLLOW_TILDE_in_factor5937); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TILDE217_tree = (PythonTree)adaptor.create(TILDE217);
                    adaptor.addChild(root_0, TILDE217_tree);
                    }
                    pushFollow(FOLLOW_factor_in_factor5941);
                    t=factor();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    if ( state.backtracking==0 ) {

                      	          retval.etype = new UnaryOp(TILDE217, unaryopType.Invert, (t!=null?t.etype:null));
                            
                    }

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1689:7: power
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_power_in_factor5957);
                    power218=power();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, power218.getTree());
                    if ( state.backtracking==0 ) {

                                retval.etype = actions.castExpr((power218!=null?((PythonTree)power218.tree):null));
                                retval.lparen = (power218!=null?power218.lparen:null);
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = retval.etype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "factor"

    public static class power_return extends ParserRuleReturnScope {
        public expr etype;
        public Token lparen = null;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "power"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1697:1: power returns [expr etype, Token lparen = null] : atom (t+= trailer[$atom.start, $atom.tree] )* ( options {greedy=true; } : d= DOUBLESTAR factor )? ;
    public final TruffleParser.power_return power() throws RecognitionException {
        TruffleParser.power_return retval = new TruffleParser.power_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token d=null;
        List list_t=null;
        TruffleParser.atom_return atom219 = null;

        TruffleParser.factor_return factor220 = null;

        TruffleParser.trailer_return t = null;
         t = null;
        PythonTree d_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1702:5: ( atom (t+= trailer[$atom.start, $atom.tree] )* ( options {greedy=true; } : d= DOUBLESTAR factor )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1702:7: atom (t+= trailer[$atom.start, $atom.tree] )* ( options {greedy=true; } : d= DOUBLESTAR factor )?
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_atom_in_power5996);
            atom219=atom();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, atom219.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1702:12: (t+= trailer[$atom.start, $atom.tree] )*
            loop107:
            do {
                int alt107=2;
                int LA107_0 = input.LA(1);

                if ( (LA107_0==DOT||LA107_0==LPAREN||LA107_0==LBRACK) ) {
                    alt107=1;
                }


                switch (alt107) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1702:13: t+= trailer[$atom.start, $atom.tree]
            	    {
            	    pushFollow(FOLLOW_trailer_in_power6001);
            	    t=trailer((atom219!=null?((Token)atom219.start):null), (atom219!=null?((PythonTree)atom219.tree):null));

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
            	    if (list_t==null) list_t=new ArrayList();
            	    list_t.add(t.getTree());


            	    }
            	    break;

            	default :
            	    break loop107;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1702:51: ( options {greedy=true; } : d= DOUBLESTAR factor )?
            int alt108=2;
            int LA108_0 = input.LA(1);

            if ( (LA108_0==DOUBLESTAR) ) {
                alt108=1;
            }
            switch (alt108) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1702:75: d= DOUBLESTAR factor
                    {
                    d=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_power6016); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    d_tree = (PythonTree)adaptor.create(d);
                    adaptor.addChild(root_0, d_tree);
                    }
                    pushFollow(FOLLOW_factor_in_power6018);
                    factor220=factor();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, factor220.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        retval.lparen = (atom219!=null?atom219.lparen:null);
                        //XXX: This could be better.
                        retval.etype = actions.castExpr((atom219!=null?((PythonTree)atom219.tree):null));
                        if (list_t != null) {
                            for(Object o : list_t) {
                                actions.recurseSetContext(retval.etype, expr_contextType.Load);
                                if (o instanceof Call) {
                                    Call c = (Call)o;
                                    c.setFunc((PyObject)retval.etype);
                                    retval.etype = c;
                                } else if (o instanceof Subscript) {
                                    Subscript c = (Subscript)o;
                                    c.setValue((PyObject)retval.etype);
                                    retval.etype = c;
                                } else if (o instanceof Attribute) {
                                    Attribute c = (Attribute)o;
                                    c.setCharStartIndex(retval.etype.getCharStartIndex());
                                    c.setValue((PyObject)retval.etype);
                                    retval.etype = c;
                                }
                            }
                        }
                        if (d != null) {
                            List right = new ArrayList();
                            right.add((factor220!=null?((PythonTree)factor220.tree):null));
                            retval.etype = actions.makeBinOp((atom219!=null?((Token)atom219.start):null), retval.etype, operatorType.Pow, right);
                        }
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = retval.etype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "power"

    public static class atom_return extends ParserRuleReturnScope {
        public Token lparen = null;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atom"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1739:1: atom returns [Token lparen = null] : ( LPAREN ( yield_expr | testlist_gexp -> testlist_gexp | ) RPAREN | LBRACK ( listmaker[$LBRACK] -> listmaker | ) RBRACK | LCURLY ( dictorsetmaker[$LCURLY] -> dictorsetmaker | ) RCURLY | lb= BACKQUOTE testlist[expr_contextType.Load] rb= BACKQUOTE | name_or_print | NONE | TRUE | FALSE | INT | FLOAT | COMPLEX | (S+= STRING )+ );
    public final TruffleParser.atom_return atom() throws RecognitionException {
        TruffleParser.atom_return retval = new TruffleParser.atom_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token lb=null;
        Token rb=null;
        Token LPAREN221=null;
        Token RPAREN224=null;
        Token LBRACK225=null;
        Token RBRACK227=null;
        Token LCURLY228=null;
        Token RCURLY230=null;
        Token NONE233=null;
        Token TRUE234=null;
        Token FALSE235=null;
        Token INT236=null;
        Token FLOAT237=null;
        Token COMPLEX238=null;
        Token S=null;
        List list_S=null;
        TruffleParser.yield_expr_return yield_expr222 = null;

        TruffleParser.testlist_gexp_return testlist_gexp223 = null;

        TruffleParser.listmaker_return listmaker226 = null;

        TruffleParser.dictorsetmaker_return dictorsetmaker229 = null;

        TruffleParser.testlist_return testlist231 = null;

        TruffleParser.name_or_print_return name_or_print232 = null;


        PythonTree lb_tree=null;
        PythonTree rb_tree=null;
        PythonTree LPAREN221_tree=null;
        PythonTree RPAREN224_tree=null;
        PythonTree LBRACK225_tree=null;
        PythonTree RBRACK227_tree=null;
        PythonTree LCURLY228_tree=null;
        PythonTree RCURLY230_tree=null;
        PythonTree NONE233_tree=null;
        PythonTree TRUE234_tree=null;
        PythonTree FALSE235_tree=null;
        PythonTree INT236_tree=null;
        PythonTree FLOAT237_tree=null;
        PythonTree COMPLEX238_tree=null;
        PythonTree S_tree=null;
        RewriteRuleTokenStream stream_RBRACK=new RewriteRuleTokenStream(adaptor,"token RBRACK");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LCURLY=new RewriteRuleTokenStream(adaptor,"token LCURLY");
        RewriteRuleTokenStream stream_LBRACK=new RewriteRuleTokenStream(adaptor,"token LBRACK");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_RCURLY=new RewriteRuleTokenStream(adaptor,"token RCURLY");
        RewriteRuleSubtreeStream stream_testlist_gexp=new RewriteRuleSubtreeStream(adaptor,"rule testlist_gexp");
        RewriteRuleSubtreeStream stream_yield_expr=new RewriteRuleSubtreeStream(adaptor,"rule yield_expr");
        RewriteRuleSubtreeStream stream_listmaker=new RewriteRuleSubtreeStream(adaptor,"rule listmaker");
        RewriteRuleSubtreeStream stream_dictorsetmaker=new RewriteRuleSubtreeStream(adaptor,"rule dictorsetmaker");

            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1749:5: ( LPAREN ( yield_expr | testlist_gexp -> testlist_gexp | ) RPAREN | LBRACK ( listmaker[$LBRACK] -> listmaker | ) RBRACK | LCURLY ( dictorsetmaker[$LCURLY] -> dictorsetmaker | ) RCURLY | lb= BACKQUOTE testlist[expr_contextType.Load] rb= BACKQUOTE | name_or_print | NONE | TRUE | FALSE | INT | FLOAT | COMPLEX | (S+= STRING )+ )
            int alt113=12;
            alt113 = dfa113.predict(input);
            switch (alt113) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1749:7: LPAREN ( yield_expr | testlist_gexp -> testlist_gexp | ) RPAREN
                    {
                    LPAREN221=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom6068); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN221);

                    if ( state.backtracking==0 ) {

                                retval.lparen = LPAREN221;
                            
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1753:7: ( yield_expr | testlist_gexp -> testlist_gexp | )
                    int alt109=3;
                    int LA109_0 = input.LA(1);

                    if ( (LA109_0==YIELD) ) {
                        alt109=1;
                    }
                    else if ( (LA109_0==NAME||LA109_0==NOT||LA109_0==LPAREN||(LA109_0>=PLUS && LA109_0<=MINUS)||(LA109_0>=TILDE && LA109_0<=LBRACK)||LA109_0==LCURLY||LA109_0==BACKQUOTE) ) {
                        alt109=2;
                    }
                    else if ( (LA109_0==PRINT) && ((printFunction))) {
                        alt109=2;
                    }
                    else if ( (LA109_0==LAMBDA||(LA109_0>=NONE && LA109_0<=FALSE)||(LA109_0>=INT && LA109_0<=STRING)) ) {
                        alt109=2;
                    }
                    else if ( (LA109_0==RPAREN) ) {
                        alt109=3;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 109, 0, input);

                        throw nvae;
                    }
                    switch (alt109) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1753:9: yield_expr
                            {
                            pushFollow(FOLLOW_yield_expr_in_atom6086);
                            yield_expr222=yield_expr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_yield_expr.add(yield_expr222.getTree());
                            if ( state.backtracking==0 ) {

                                          etype = (yield_expr222!=null?yield_expr222.etype:null);
                                      
                            }

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1757:9: testlist_gexp
                            {
                            pushFollow(FOLLOW_testlist_gexp_in_atom6106);
                            testlist_gexp223=testlist_gexp();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_testlist_gexp.add(testlist_gexp223.getTree());


                            // AST REWRITE
                            // elements: testlist_gexp
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (PythonTree)adaptor.nil();
                            // 1758:6: -> testlist_gexp
                            {
                                adaptor.addChild(root_0, stream_testlist_gexp.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 3 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1760:9: 
                            {
                            if ( state.backtracking==0 ) {

                                          etype = new Tuple(LPAREN221, new ArrayList<expr>(), ((expr_scope)expr_stack.peek()).ctype);
                                      
                            }

                            }
                            break;

                    }

                    RPAREN224=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom6149); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN224);


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1765:7: LBRACK ( listmaker[$LBRACK] -> listmaker | ) RBRACK
                    {
                    LBRACK225=(Token)match(input,LBRACK,FOLLOW_LBRACK_in_atom6157); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACK.add(LBRACK225);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1766:7: ( listmaker[$LBRACK] -> listmaker | )
                    int alt110=2;
                    int LA110_0 = input.LA(1);

                    if ( (LA110_0==NAME||LA110_0==NOT||LA110_0==LPAREN||(LA110_0>=PLUS && LA110_0<=MINUS)||(LA110_0>=TILDE && LA110_0<=LBRACK)||LA110_0==LCURLY||LA110_0==BACKQUOTE) ) {
                        alt110=1;
                    }
                    else if ( (LA110_0==PRINT) && ((printFunction))) {
                        alt110=1;
                    }
                    else if ( (LA110_0==LAMBDA||(LA110_0>=NONE && LA110_0<=FALSE)||(LA110_0>=INT && LA110_0<=STRING)) ) {
                        alt110=1;
                    }
                    else if ( (LA110_0==RBRACK) ) {
                        alt110=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 110, 0, input);

                        throw nvae;
                    }
                    switch (alt110) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1766:8: listmaker[$LBRACK]
                            {
                            pushFollow(FOLLOW_listmaker_in_atom6166);
                            listmaker226=listmaker(LBRACK225);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_listmaker.add(listmaker226.getTree());


                            // AST REWRITE
                            // elements: listmaker
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (PythonTree)adaptor.nil();
                            // 1767:6: -> listmaker
                            {
                                adaptor.addChild(root_0, stream_listmaker.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1769:8: 
                            {
                            if ( state.backtracking==0 ) {

                                         etype = new org.python.antlr.ast.List(LBRACK225, new ArrayList<expr>(), ((expr_scope)expr_stack.peek()).ctype);
                                     
                            }

                            }
                            break;

                    }

                    RBRACK227=(Token)match(input,RBRACK,FOLLOW_RBRACK_in_atom6209); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACK.add(RBRACK227);


                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1774:7: LCURLY ( dictorsetmaker[$LCURLY] -> dictorsetmaker | ) RCURLY
                    {
                    LCURLY228=(Token)match(input,LCURLY,FOLLOW_LCURLY_in_atom6217); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LCURLY.add(LCURLY228);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1775:7: ( dictorsetmaker[$LCURLY] -> dictorsetmaker | )
                    int alt111=2;
                    int LA111_0 = input.LA(1);

                    if ( (LA111_0==NAME||LA111_0==NOT||LA111_0==LPAREN||(LA111_0>=PLUS && LA111_0<=MINUS)||(LA111_0>=TILDE && LA111_0<=LBRACK)||LA111_0==LCURLY||LA111_0==BACKQUOTE) ) {
                        alt111=1;
                    }
                    else if ( (LA111_0==PRINT) && ((printFunction))) {
                        alt111=1;
                    }
                    else if ( (LA111_0==LAMBDA||(LA111_0>=NONE && LA111_0<=FALSE)||(LA111_0>=INT && LA111_0<=STRING)) ) {
                        alt111=1;
                    }
                    else if ( (LA111_0==RCURLY) ) {
                        alt111=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 111, 0, input);

                        throw nvae;
                    }
                    switch (alt111) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1775:8: dictorsetmaker[$LCURLY]
                            {
                            pushFollow(FOLLOW_dictorsetmaker_in_atom6226);
                            dictorsetmaker229=dictorsetmaker(LCURLY228);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_dictorsetmaker.add(dictorsetmaker229.getTree());


                            // AST REWRITE
                            // elements: dictorsetmaker
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (PythonTree)adaptor.nil();
                            // 1776:6: -> dictorsetmaker
                            {
                                adaptor.addChild(root_0, stream_dictorsetmaker.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1778:8: 
                            {
                            if ( state.backtracking==0 ) {

                                         etype = new Dict(LCURLY228, new ArrayList<expr>(), new ArrayList<expr>());
                                     
                            }

                            }
                            break;

                    }

                    RCURLY230=(Token)match(input,RCURLY,FOLLOW_RCURLY_in_atom6270); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RCURLY.add(RCURLY230);


                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1783:8: lb= BACKQUOTE testlist[expr_contextType.Load] rb= BACKQUOTE
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    lb=(Token)match(input,BACKQUOTE,FOLLOW_BACKQUOTE_in_atom6281); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    lb_tree = (PythonTree)adaptor.create(lb);
                    adaptor.addChild(root_0, lb_tree);
                    }
                    pushFollow(FOLLOW_testlist_in_atom6283);
                    testlist231=testlist(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, testlist231.getTree());
                    rb=(Token)match(input,BACKQUOTE,FOLLOW_BACKQUOTE_in_atom6288); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    rb_tree = (PythonTree)adaptor.create(rb);
                    adaptor.addChild(root_0, rb_tree);
                    }
                    if ( state.backtracking==0 ) {

                                 etype = new Repr(lb, actions.castExpr((testlist231!=null?((PythonTree)testlist231.tree):null)));
                             
                    }

                    }
                    break;
                case 5 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1787:8: name_or_print
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_name_or_print_in_atom6306);
                    name_or_print232=name_or_print();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, name_or_print232.getTree());
                    if ( state.backtracking==0 ) {

                                 etype = new Name((name_or_print232!=null?((Token)name_or_print232.start):null), (name_or_print232!=null?input.toString(name_or_print232.start,name_or_print232.stop):null), ((expr_scope)expr_stack.peek()).ctype);
                           
                    }

                    }
                    break;
                case 6 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1791:8: NONE
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    NONE233=(Token)match(input,NONE,FOLLOW_NONE_in_atom6324); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NONE233_tree = (PythonTree)adaptor.create(NONE233);
                    adaptor.addChild(root_0, NONE233_tree);
                    }
                    if ( state.backtracking==0 ) {

                             	   etype = new None(NONE233);
                             
                    }

                    }
                    break;
                case 7 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1795:8: TRUE
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    TRUE234=(Token)match(input,TRUE,FOLLOW_TRUE_in_atom6343); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TRUE234_tree = (PythonTree)adaptor.create(TRUE234);
                    adaptor.addChild(root_0, TRUE234_tree);
                    }
                    if ( state.backtracking==0 ) {

                             	   etype = new True(TRUE234);
                             
                    }

                    }
                    break;
                case 8 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1799:8: FALSE
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    FALSE235=(Token)match(input,FALSE,FOLLOW_FALSE_in_atom6362); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FALSE235_tree = (PythonTree)adaptor.create(FALSE235);
                    adaptor.addChild(root_0, FALSE235_tree);
                    }
                    if ( state.backtracking==0 ) {

                             	   etype = new False(FALSE235);
                             
                    }

                    }
                    break;
                case 9 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1803:8: INT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    INT236=(Token)match(input,INT,FOLLOW_INT_in_atom6381); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT236_tree = (PythonTree)adaptor.create(INT236);
                    adaptor.addChild(root_0, INT236_tree);
                    }
                    if ( state.backtracking==0 ) {

                      	         etype = new Num(INT236, actions.makeInt(INT236));
                             
                    }

                    }
                    break;
                case 10 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1811:8: FLOAT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    FLOAT237=(Token)match(input,FLOAT,FOLLOW_FLOAT_in_atom6420); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    FLOAT237_tree = (PythonTree)adaptor.create(FLOAT237);
                    adaptor.addChild(root_0, FLOAT237_tree);
                    }
                    if ( state.backtracking==0 ) {

                                 etype = new Num(FLOAT237, actions.makeFloat(FLOAT237));
                             
                    }

                    }
                    break;
                case 11 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1815:8: COMPLEX
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    COMPLEX238=(Token)match(input,COMPLEX,FOLLOW_COMPLEX_in_atom6438); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMPLEX238_tree = (PythonTree)adaptor.create(COMPLEX238);
                    adaptor.addChild(root_0, COMPLEX238_tree);
                    }
                    if ( state.backtracking==0 ) {

                                  etype = new Num(COMPLEX238, actions.makeComplex(COMPLEX238));
                             
                    }

                    }
                    break;
                case 12 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1819:8: (S+= STRING )+
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1819:8: (S+= STRING )+
                    int cnt112=0;
                    loop112:
                    do {
                        int alt112=2;
                        int LA112_0 = input.LA(1);

                        if ( (LA112_0==STRING) ) {
                            alt112=1;
                        }


                        switch (alt112) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1819:9: S+= STRING
                    	    {
                    	    S=(Token)match(input,STRING,FOLLOW_STRING_in_atom6459); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    S_tree = (PythonTree)adaptor.create(S);
                    	    adaptor.addChild(root_0, S_tree);
                    	    }
                    	    if (list_S==null) list_S=new ArrayList();
                    	    list_S.add(S);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt112 >= 1 ) break loop112;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(112, input);
                                throw eee;
                        }
                        cnt112++;
                    } while (true);

                    if ( state.backtracking==0 ) {

                                 etype = new Str(actions.extractStringToken(list_S), actions.extractStrings(list_S, encoding, unicodeLiterals));
                             
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 if (etype != null) {
                     retval.tree = etype;
                 }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "atom"

    public static class listmaker_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listmaker"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1826:1: listmaker[Token lbrack] : t+= test[$expr::ctype] ( list_for[gens] | ( options {greedy=true; } : COMMA t+= test[$expr::ctype] )* ) ( COMMA )? ;
    public final TruffleParser.listmaker_return listmaker(Token lbrack) throws RecognitionException {
        TruffleParser.listmaker_return retval = new TruffleParser.listmaker_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COMMA240=null;
        Token COMMA241=null;
        List list_t=null;
        TruffleParser.list_for_return list_for239 = null;

        TruffleParser.test_return t = null;
         t = null;
        PythonTree COMMA240_tree=null;
        PythonTree COMMA241_tree=null;


            List gens = new ArrayList();
            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1834:5: (t+= test[$expr::ctype] ( list_for[gens] | ( options {greedy=true; } : COMMA t+= test[$expr::ctype] )* ) ( COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1834:7: t+= test[$expr::ctype] ( list_for[gens] | ( options {greedy=true; } : COMMA t+= test[$expr::ctype] )* ) ( COMMA )?
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_test_in_listmaker6502);
            t=test(((expr_scope)expr_stack.peek()).ctype);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
            if (list_t==null) list_t=new ArrayList();
            list_t.add(t.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1835:9: ( list_for[gens] | ( options {greedy=true; } : COMMA t+= test[$expr::ctype] )* )
            int alt115=2;
            int LA115_0 = input.LA(1);

            if ( (LA115_0==FOR) ) {
                alt115=1;
            }
            else if ( (LA115_0==COMMA||LA115_0==RBRACK) ) {
                alt115=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 115, 0, input);

                throw nvae;
            }
            switch (alt115) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1835:10: list_for[gens]
                    {
                    pushFollow(FOLLOW_list_for_in_listmaker6514);
                    list_for239=list_for(gens);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, list_for239.getTree());
                    if ( state.backtracking==0 ) {

                                   Collections.reverse(gens);
                                   List<comprehension> c = gens;
                                   etype = new ListComp(((Token)retval.start), actions.castExpr(list_t.get(0)), c);
                               
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1841:11: ( options {greedy=true; } : COMMA t+= test[$expr::ctype] )*
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1841:11: ( options {greedy=true; } : COMMA t+= test[$expr::ctype] )*
                    loop114:
                    do {
                        int alt114=2;
                        int LA114_0 = input.LA(1);

                        if ( (LA114_0==COMMA) ) {
                            int LA114_1 = input.LA(2);

                            if ( (LA114_1==NAME||LA114_1==PRINT||(LA114_1>=LAMBDA && LA114_1<=NOT)||(LA114_1>=NONE && LA114_1<=FALSE)||LA114_1==LPAREN||(LA114_1>=PLUS && LA114_1<=MINUS)||(LA114_1>=TILDE && LA114_1<=LBRACK)||LA114_1==LCURLY||(LA114_1>=BACKQUOTE && LA114_1<=STRING)) ) {
                                alt114=1;
                            }


                        }


                        switch (alt114) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1841:35: COMMA t+= test[$expr::ctype]
                    	    {
                    	    COMMA240=(Token)match(input,COMMA,FOLLOW_COMMA_in_listmaker6546); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA240_tree = (PythonTree)adaptor.create(COMMA240);
                    	    adaptor.addChild(root_0, COMMA240_tree);
                    	    }
                    	    pushFollow(FOLLOW_test_in_listmaker6550);
                    	    t=test(((expr_scope)expr_stack.peek()).ctype);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    	    if (list_t==null) list_t=new ArrayList();
                    	    list_t.add(t.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop114;
                        }
                    } while (true);

                    if ( state.backtracking==0 ) {

                                     etype = new org.python.antlr.ast.List(lbrack, actions.castExprs(list_t), ((expr_scope)expr_stack.peek()).ctype);
                                 
                    }

                    }
                    break;

            }

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1845:11: ( COMMA )?
            int alt116=2;
            int LA116_0 = input.LA(1);

            if ( (LA116_0==COMMA) ) {
                alt116=1;
            }
            switch (alt116) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1845:12: COMMA
                    {
                    COMMA241=(Token)match(input,COMMA,FOLLOW_COMMA_in_listmaker6579); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA241_tree = (PythonTree)adaptor.create(COMMA241);
                    adaptor.addChild(root_0, COMMA241_tree);
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = etype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listmaker"

    public static class testlist_gexp_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "testlist_gexp"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1849:1: testlist_gexp : t+= test[$expr::ctype] ( ( options {k=2; } : c1= COMMA t+= test[$expr::ctype] )* (c2= COMMA )? {...}? | -> test | ( comp_for[gens] ) ) ;
    public final TruffleParser.testlist_gexp_return testlist_gexp() throws RecognitionException {
        TruffleParser.testlist_gexp_return retval = new TruffleParser.testlist_gexp_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token c1=null;
        Token c2=null;
        List list_t=null;
        TruffleParser.comp_for_return comp_for242 = null;

        TruffleParser.test_return t = null;
         t = null;
        PythonTree c1_tree=null;
        PythonTree c2_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_test=new RewriteRuleSubtreeStream(adaptor,"rule test");
        RewriteRuleSubtreeStream stream_comp_for=new RewriteRuleSubtreeStream(adaptor,"rule comp_for");

            expr etype = null;
            List gens = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1859:5: (t+= test[$expr::ctype] ( ( options {k=2; } : c1= COMMA t+= test[$expr::ctype] )* (c2= COMMA )? {...}? | -> test | ( comp_for[gens] ) ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1859:7: t+= test[$expr::ctype] ( ( options {k=2; } : c1= COMMA t+= test[$expr::ctype] )* (c2= COMMA )? {...}? | -> test | ( comp_for[gens] ) )
            {
            pushFollow(FOLLOW_test_in_testlist_gexp6611);
            t=test(((expr_scope)expr_stack.peek()).ctype);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_test.add(t.getTree());
            if (list_t==null) list_t=new ArrayList();
            list_t.add(t.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1860:9: ( ( options {k=2; } : c1= COMMA t+= test[$expr::ctype] )* (c2= COMMA )? {...}? | -> test | ( comp_for[gens] ) )
            int alt119=3;
            switch ( input.LA(1) ) {
            case COMMA:
                {
                alt119=1;
                }
                break;
            case RPAREN:
                {
                int LA119_2 = input.LA(2);

                if ( (( c1 != null || c2 != null )) ) {
                    alt119=1;
                }
                else if ( (true) ) {
                    alt119=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 119, 2, input);

                    throw nvae;
                }
                }
                break;
            case FOR:
                {
                alt119=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 119, 0, input);

                throw nvae;
            }

            switch (alt119) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1860:11: ( options {k=2; } : c1= COMMA t+= test[$expr::ctype] )* (c2= COMMA )? {...}?
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1860:11: ( options {k=2; } : c1= COMMA t+= test[$expr::ctype] )*
                    loop117:
                    do {
                        int alt117=2;
                        alt117 = dfa117.predict(input);
                        switch (alt117) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1860:28: c1= COMMA t+= test[$expr::ctype]
                    	    {
                    	    c1=(Token)match(input,COMMA,FOLLOW_COMMA_in_testlist_gexp6635); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(c1);

                    	    pushFollow(FOLLOW_test_in_testlist_gexp6639);
                    	    t=test(((expr_scope)expr_stack.peek()).ctype);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_test.add(t.getTree());
                    	    if (list_t==null) list_t=new ArrayList();
                    	    list_t.add(t.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop117;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1860:61: (c2= COMMA )?
                    int alt118=2;
                    int LA118_0 = input.LA(1);

                    if ( (LA118_0==COMMA) ) {
                        alt118=1;
                    }
                    switch (alt118) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1860:62: c2= COMMA
                            {
                            c2=(Token)match(input,COMMA,FOLLOW_COMMA_in_testlist_gexp6647); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(c2);


                            }
                            break;

                    }

                    if ( !(( c1 != null || c2 != null )) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "testlist_gexp", " $c1 != null || $c2 != null ");
                    }
                    if ( state.backtracking==0 ) {

                                     etype = new Tuple(((Token)retval.start), actions.castExprs(list_t), ((expr_scope)expr_stack.peek()).ctype);
                                 
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1865:11: 
                    {

                    // AST REWRITE
                    // elements: test
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1865:11: -> test
                    {
                        adaptor.addChild(root_0, stream_test.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1866:11: ( comp_for[gens] )
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1866:11: ( comp_for[gens] )
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1866:12: comp_for[gens]
                    {
                    pushFollow(FOLLOW_comp_for_in_testlist_gexp6701);
                    comp_for242=comp_for(gens);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp_for.add(comp_for242.getTree());
                    if ( state.backtracking==0 ) {

                                     Collections.reverse(gens);
                                     List<comprehension> c = gens;
                                     expr e = actions.castExpr(list_t.get(0));
                                     if (e instanceof Context) {
                                         ((Context)e).setContext(expr_contextType.Load);
                                     }
                                     etype = new GeneratorExp(((Token)retval.start), actions.castExpr(list_t.get(0)), c);
                                 
                    }

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (etype != null) {
                      retval.tree = etype;
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "testlist_gexp"

    public static class lambdef_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "lambdef"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1881:1: lambdef : LAMBDA ( varargslist )? COLON test[expr_contextType.Load] ;
    public final TruffleParser.lambdef_return lambdef() throws RecognitionException {
        TruffleParser.lambdef_return retval = new TruffleParser.lambdef_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token LAMBDA243=null;
        Token COLON245=null;
        TruffleParser.varargslist_return varargslist244 = null;

        TruffleParser.test_return test246 = null;


        PythonTree LAMBDA243_tree=null;
        PythonTree COLON245_tree=null;


            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1888:5: ( LAMBDA ( varargslist )? COLON test[expr_contextType.Load] )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1888:7: LAMBDA ( varargslist )? COLON test[expr_contextType.Load]
            {
            root_0 = (PythonTree)adaptor.nil();

            LAMBDA243=(Token)match(input,LAMBDA,FOLLOW_LAMBDA_in_lambdef6765); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LAMBDA243_tree = (PythonTree)adaptor.create(LAMBDA243);
            adaptor.addChild(root_0, LAMBDA243_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1888:14: ( varargslist )?
            int alt120=2;
            int LA120_0 = input.LA(1);

            if ( (LA120_0==NAME||LA120_0==LPAREN||(LA120_0>=STAR && LA120_0<=DOUBLESTAR)) ) {
                alt120=1;
            }
            switch (alt120) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1888:15: varargslist
                    {
                    pushFollow(FOLLOW_varargslist_in_lambdef6768);
                    varargslist244=varargslist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, varargslist244.getTree());

                    }
                    break;

            }

            COLON245=(Token)match(input,COLON,FOLLOW_COLON_in_lambdef6772); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON245_tree = (PythonTree)adaptor.create(COLON245);
            adaptor.addChild(root_0, COLON245_tree);
            }
            pushFollow(FOLLOW_test_in_lambdef6774);
            test246=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, test246.getTree());
            if ( state.backtracking==0 ) {

                        arguments a = (varargslist244!=null?varargslist244.args:null);
                        if (a == null) {
                            a = new arguments(LAMBDA243, new ArrayList<expr>(), null, null, new ArrayList<expr>());
                        }
                        etype = new Lambda(LAMBDA243, a, actions.castExpr((test246!=null?((PythonTree)test246.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = etype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "lambdef"

    public static class trailer_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "trailer"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1899:1: trailer[Token begin, PythonTree ptree] : ( LPAREN ( arglist | ) RPAREN | LBRACK subscriptlist[$begin] RBRACK | DOT attr );
    public final TruffleParser.trailer_return trailer(Token begin, PythonTree ptree) throws RecognitionException {
        TruffleParser.trailer_return retval = new TruffleParser.trailer_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token LPAREN247=null;
        Token RPAREN249=null;
        Token LBRACK250=null;
        Token RBRACK252=null;
        Token DOT253=null;
        TruffleParser.arglist_return arglist248 = null;

        TruffleParser.subscriptlist_return subscriptlist251 = null;

        TruffleParser.attr_return attr254 = null;


        PythonTree LPAREN247_tree=null;
        PythonTree RPAREN249_tree=null;
        PythonTree LBRACK250_tree=null;
        PythonTree RBRACK252_tree=null;
        PythonTree DOT253_tree=null;


            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1908:5: ( LPAREN ( arglist | ) RPAREN | LBRACK subscriptlist[$begin] RBRACK | DOT attr )
            int alt122=3;
            switch ( input.LA(1) ) {
            case LPAREN:
                {
                alt122=1;
                }
                break;
            case LBRACK:
                {
                alt122=2;
                }
                break;
            case DOT:
                {
                alt122=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 122, 0, input);

                throw nvae;
            }

            switch (alt122) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1908:7: LPAREN ( arglist | ) RPAREN
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    LPAREN247=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_trailer6813); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN247_tree = (PythonTree)adaptor.create(LPAREN247);
                    adaptor.addChild(root_0, LPAREN247_tree);
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1909:7: ( arglist | )
                    int alt121=2;
                    int LA121_0 = input.LA(1);

                    if ( (LA121_0==NAME||LA121_0==NOT||LA121_0==LPAREN||(LA121_0>=PLUS && LA121_0<=MINUS)||(LA121_0>=TILDE && LA121_0<=LBRACK)||LA121_0==LCURLY||LA121_0==BACKQUOTE) ) {
                        alt121=1;
                    }
                    else if ( (LA121_0==PRINT) && ((printFunction))) {
                        alt121=1;
                    }
                    else if ( (LA121_0==LAMBDA||(LA121_0>=NONE && LA121_0<=FALSE)||(LA121_0>=STAR && LA121_0<=DOUBLESTAR)||(LA121_0>=INT && LA121_0<=STRING)) ) {
                        alt121=1;
                    }
                    else if ( (LA121_0==RPAREN) ) {
                        alt121=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 121, 0, input);

                        throw nvae;
                    }
                    switch (alt121) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1909:8: arglist
                            {
                            pushFollow(FOLLOW_arglist_in_trailer6822);
                            arglist248=arglist();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, arglist248.getTree());
                            if ( state.backtracking==0 ) {

                                         etype = new Call(begin, actions.castExpr(ptree), actions.castExprs((arglist248!=null?arglist248.args:null)),
                                           actions.makeKeywords((arglist248!=null?arglist248.keywords:null)), (arglist248!=null?arglist248.starargs:null), (arglist248!=null?arglist248.kwargs:null));
                                     
                            }

                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1915:8: 
                            {
                            if ( state.backtracking==0 ) {

                                         etype = new Call(begin, actions.castExpr(ptree), new ArrayList<expr>(), new ArrayList<keyword>(), null, null);
                                     
                            }

                            }
                            break;

                    }

                    RPAREN249=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_trailer6864); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN249_tree = (PythonTree)adaptor.create(RPAREN249);
                    adaptor.addChild(root_0, RPAREN249_tree);
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1920:7: LBRACK subscriptlist[$begin] RBRACK
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    LBRACK250=(Token)match(input,LBRACK,FOLLOW_LBRACK_in_trailer6872); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACK250_tree = (PythonTree)adaptor.create(LBRACK250);
                    adaptor.addChild(root_0, LBRACK250_tree);
                    }
                    pushFollow(FOLLOW_subscriptlist_in_trailer6874);
                    subscriptlist251=subscriptlist(begin);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, subscriptlist251.getTree());
                    RBRACK252=(Token)match(input,RBRACK,FOLLOW_RBRACK_in_trailer6877); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACK252_tree = (PythonTree)adaptor.create(RBRACK252);
                    adaptor.addChild(root_0, RBRACK252_tree);
                    }
                    if ( state.backtracking==0 ) {

                                etype = new Subscript(begin, actions.castExpr(ptree), actions.castSlice((subscriptlist251!=null?((PythonTree)subscriptlist251.tree):null)), ((expr_scope)expr_stack.peek()).ctype);
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1924:7: DOT attr
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    DOT253=(Token)match(input,DOT,FOLLOW_DOT_in_trailer6893); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOT253_tree = (PythonTree)adaptor.create(DOT253);
                    adaptor.addChild(root_0, DOT253_tree);
                    }
                    pushFollow(FOLLOW_attr_in_trailer6895);
                    attr254=attr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, attr254.getTree());
                    if ( state.backtracking==0 ) {

                                etype = new Attribute(begin, actions.castExpr(ptree), new Name((attr254!=null?((PythonTree)attr254.tree):null), (attr254!=null?input.toString(attr254.start,attr254.stop):null), expr_contextType.Load), ((expr_scope)expr_stack.peek()).ctype);
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (etype != null) {
                      retval.tree = etype;
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "trailer"

    public static class subscriptlist_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "subscriptlist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1931:1: subscriptlist[Token begin] : sub+= subscript ( options {greedy=true; } : c1= COMMA sub+= subscript )* (c2= COMMA )? ;
    public final TruffleParser.subscriptlist_return subscriptlist(Token begin) throws RecognitionException {
        TruffleParser.subscriptlist_return retval = new TruffleParser.subscriptlist_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token c1=null;
        Token c2=null;
        List list_sub=null;
        TruffleParser.subscript_return sub = null;
         sub = null;
        PythonTree c1_tree=null;
        PythonTree c2_tree=null;


            slice sltype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1938:5: (sub+= subscript ( options {greedy=true; } : c1= COMMA sub+= subscript )* (c2= COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1938:7: sub+= subscript ( options {greedy=true; } : c1= COMMA sub+= subscript )* (c2= COMMA )?
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_subscript_in_subscriptlist6934);
            sub=subscript();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, sub.getTree());
            if (list_sub==null) list_sub=new ArrayList();
            list_sub.add(sub.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1938:22: ( options {greedy=true; } : c1= COMMA sub+= subscript )*
            loop123:
            do {
                int alt123=2;
                int LA123_0 = input.LA(1);

                if ( (LA123_0==COMMA) ) {
                    int LA123_1 = input.LA(2);

                    if ( ((LA123_1>=NAME && LA123_1<=PRINT)||(LA123_1>=LAMBDA && LA123_1<=NOT)||(LA123_1>=NONE && LA123_1<=FALSE)||LA123_1==LPAREN||LA123_1==COLON||(LA123_1>=PLUS && LA123_1<=MINUS)||(LA123_1>=TILDE && LA123_1<=LBRACK)||LA123_1==LCURLY||(LA123_1>=BACKQUOTE && LA123_1<=STRING)) ) {
                        alt123=1;
                    }


                }


                switch (alt123) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1938:46: c1= COMMA sub+= subscript
            	    {
            	    c1=(Token)match(input,COMMA,FOLLOW_COMMA_in_subscriptlist6946); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    c1_tree = (PythonTree)adaptor.create(c1);
            	    adaptor.addChild(root_0, c1_tree);
            	    }
            	    pushFollow(FOLLOW_subscript_in_subscriptlist6950);
            	    sub=subscript();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, sub.getTree());
            	    if (list_sub==null) list_sub=new ArrayList();
            	    list_sub.add(sub.getTree());


            	    }
            	    break;

            	default :
            	    break loop123;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1938:72: (c2= COMMA )?
            int alt124=2;
            int LA124_0 = input.LA(1);

            if ( (LA124_0==COMMA) ) {
                alt124=1;
            }
            switch (alt124) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1938:73: c2= COMMA
                    {
                    c2=(Token)match(input,COMMA,FOLLOW_COMMA_in_subscriptlist6957); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    c2_tree = (PythonTree)adaptor.create(c2);
                    adaptor.addChild(root_0, c2_tree);
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        sltype = actions.makeSliceType(begin, c1, c2, list_sub);
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = sltype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "subscriptlist"

    public static class subscript_return extends ParserRuleReturnScope {
        public slice sltype;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "subscript"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1945:1: subscript returns [slice sltype] : (d1= DOT DOT DOT | ( test[null] COLON )=>lower= test[expr_contextType.Load] (c1= COLON (upper1= test[expr_contextType.Load] )? ( sliceop )? )? | ( COLON )=>c2= COLON (upper2= test[expr_contextType.Load] )? ( sliceop )? | test[expr_contextType.Load] );
    public final TruffleParser.subscript_return subscript() throws RecognitionException {
        TruffleParser.subscript_return retval = new TruffleParser.subscript_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token d1=null;
        Token c1=null;
        Token c2=null;
        Token DOT255=null;
        Token DOT256=null;
        TruffleParser.test_return lower = null;

        TruffleParser.test_return upper1 = null;

        TruffleParser.test_return upper2 = null;

        TruffleParser.sliceop_return sliceop257 = null;

        TruffleParser.sliceop_return sliceop258 = null;

        TruffleParser.test_return test259 = null;


        PythonTree d1_tree=null;
        PythonTree c1_tree=null;
        PythonTree c2_tree=null;
        PythonTree DOT255_tree=null;
        PythonTree DOT256_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1950:5: (d1= DOT DOT DOT | ( test[null] COLON )=>lower= test[expr_contextType.Load] (c1= COLON (upper1= test[expr_contextType.Load] )? ( sliceop )? )? | ( COLON )=>c2= COLON (upper2= test[expr_contextType.Load] )? ( sliceop )? | test[expr_contextType.Load] )
            int alt130=4;
            alt130 = dfa130.predict(input);
            switch (alt130) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1950:7: d1= DOT DOT DOT
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    d1=(Token)match(input,DOT,FOLLOW_DOT_in_subscript7000); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    d1_tree = (PythonTree)adaptor.create(d1);
                    adaptor.addChild(root_0, d1_tree);
                    }
                    DOT255=(Token)match(input,DOT,FOLLOW_DOT_in_subscript7002); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOT255_tree = (PythonTree)adaptor.create(DOT255);
                    adaptor.addChild(root_0, DOT255_tree);
                    }
                    DOT256=(Token)match(input,DOT,FOLLOW_DOT_in_subscript7004); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOT256_tree = (PythonTree)adaptor.create(DOT256);
                    adaptor.addChild(root_0, DOT256_tree);
                    }
                    if ( state.backtracking==0 ) {

                                retval.sltype = new Ellipsis(d1);
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1954:7: ( test[null] COLON )=>lower= test[expr_contextType.Load] (c1= COLON (upper1= test[expr_contextType.Load] )? ( sliceop )? )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_test_in_subscript7034);
                    lower=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, lower.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1955:41: (c1= COLON (upper1= test[expr_contextType.Load] )? ( sliceop )? )?
                    int alt127=2;
                    int LA127_0 = input.LA(1);

                    if ( (LA127_0==COLON) ) {
                        alt127=1;
                    }
                    switch (alt127) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1955:42: c1= COLON (upper1= test[expr_contextType.Load] )? ( sliceop )?
                            {
                            c1=(Token)match(input,COLON,FOLLOW_COLON_in_subscript7040); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            c1_tree = (PythonTree)adaptor.create(c1);
                            adaptor.addChild(root_0, c1_tree);
                            }
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1955:51: (upper1= test[expr_contextType.Load] )?
                            int alt125=2;
                            int LA125_0 = input.LA(1);

                            if ( (LA125_0==NAME||LA125_0==NOT||LA125_0==LPAREN||(LA125_0>=PLUS && LA125_0<=MINUS)||(LA125_0>=TILDE && LA125_0<=LBRACK)||LA125_0==LCURLY||LA125_0==BACKQUOTE) ) {
                                alt125=1;
                            }
                            else if ( (LA125_0==PRINT) && ((printFunction))) {
                                alt125=1;
                            }
                            else if ( (LA125_0==LAMBDA||(LA125_0>=NONE && LA125_0<=FALSE)||(LA125_0>=INT && LA125_0<=STRING)) ) {
                                alt125=1;
                            }
                            switch (alt125) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1955:52: upper1= test[expr_contextType.Load]
                                    {
                                    pushFollow(FOLLOW_test_in_subscript7045);
                                    upper1=test(expr_contextType.Load);

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) adaptor.addChild(root_0, upper1.getTree());

                                    }
                                    break;

                            }

                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1955:89: ( sliceop )?
                            int alt126=2;
                            int LA126_0 = input.LA(1);

                            if ( (LA126_0==COLON) ) {
                                alt126=1;
                            }
                            switch (alt126) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1955:90: sliceop
                                    {
                                    pushFollow(FOLLOW_sliceop_in_subscript7051);
                                    sliceop257=sliceop();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) adaptor.addChild(root_0, sliceop257.getTree());

                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                retval.sltype = actions.makeSubscript((lower!=null?((PythonTree)lower.tree):null), c1, (upper1!=null?((PythonTree)upper1.tree):null), (sliceop257!=null?((PythonTree)sliceop257.tree):null));
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1959:7: ( COLON )=>c2= COLON (upper2= test[expr_contextType.Load] )? ( sliceop )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    c2=(Token)match(input,COLON,FOLLOW_COLON_in_subscript7082); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    c2_tree = (PythonTree)adaptor.create(c2);
                    adaptor.addChild(root_0, c2_tree);
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1960:16: (upper2= test[expr_contextType.Load] )?
                    int alt128=2;
                    int LA128_0 = input.LA(1);

                    if ( (LA128_0==NAME||LA128_0==NOT||LA128_0==LPAREN||(LA128_0>=PLUS && LA128_0<=MINUS)||(LA128_0>=TILDE && LA128_0<=LBRACK)||LA128_0==LCURLY||LA128_0==BACKQUOTE) ) {
                        alt128=1;
                    }
                    else if ( (LA128_0==PRINT) && ((printFunction))) {
                        alt128=1;
                    }
                    else if ( (LA128_0==LAMBDA||(LA128_0>=NONE && LA128_0<=FALSE)||(LA128_0>=INT && LA128_0<=STRING)) ) {
                        alt128=1;
                    }
                    switch (alt128) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1960:17: upper2= test[expr_contextType.Load]
                            {
                            pushFollow(FOLLOW_test_in_subscript7087);
                            upper2=test(expr_contextType.Load);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, upper2.getTree());

                            }
                            break;

                    }

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1960:54: ( sliceop )?
                    int alt129=2;
                    int LA129_0 = input.LA(1);

                    if ( (LA129_0==COLON) ) {
                        alt129=1;
                    }
                    switch (alt129) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1960:55: sliceop
                            {
                            pushFollow(FOLLOW_sliceop_in_subscript7093);
                            sliceop258=sliceop();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, sliceop258.getTree());

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                retval.sltype = actions.makeSubscript(null, c2, (upper2!=null?((PythonTree)upper2.tree):null), (sliceop258!=null?((PythonTree)sliceop258.tree):null));
                            
                    }

                    }
                    break;
                case 4 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1964:7: test[expr_contextType.Load]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_test_in_subscript7111);
                    test259=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, test259.getTree());
                    if ( state.backtracking==0 ) {

                                retval.sltype = new Index((test259!=null?((Token)test259.start):null), actions.castExpr((test259!=null?((PythonTree)test259.tree):null)));
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  retval.tree = retval.sltype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "subscript"

    public static class sliceop_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "sliceop"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1971:1: sliceop : COLON ( test[expr_contextType.Load] -> test | ) ;
    public final TruffleParser.sliceop_return sliceop() throws RecognitionException {
        TruffleParser.sliceop_return retval = new TruffleParser.sliceop_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COLON260=null;
        TruffleParser.test_return test261 = null;


        PythonTree COLON260_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_test=new RewriteRuleSubtreeStream(adaptor,"rule test");

            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1980:5: ( COLON ( test[expr_contextType.Load] -> test | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1980:7: COLON ( test[expr_contextType.Load] -> test | )
            {
            COLON260=(Token)match(input,COLON,FOLLOW_COLON_in_sliceop7148); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON260);

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1981:6: ( test[expr_contextType.Load] -> test | )
            int alt131=2;
            int LA131_0 = input.LA(1);

            if ( (LA131_0==NAME||LA131_0==NOT||LA131_0==LPAREN||(LA131_0>=PLUS && LA131_0<=MINUS)||(LA131_0>=TILDE && LA131_0<=LBRACK)||LA131_0==LCURLY||LA131_0==BACKQUOTE) ) {
                alt131=1;
            }
            else if ( (LA131_0==PRINT) && ((printFunction))) {
                alt131=1;
            }
            else if ( (LA131_0==LAMBDA||(LA131_0>=NONE && LA131_0<=FALSE)||(LA131_0>=INT && LA131_0<=STRING)) ) {
                alt131=1;
            }
            else if ( (LA131_0==COMMA||LA131_0==RBRACK) ) {
                alt131=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 131, 0, input);

                throw nvae;
            }
            switch (alt131) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1981:7: test[expr_contextType.Load]
                    {
                    pushFollow(FOLLOW_test_in_sliceop7156);
                    test261=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_test.add(test261.getTree());


                    // AST REWRITE
                    // elements: test
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (PythonTree)adaptor.nil();
                    // 1982:5: -> test
                    {
                        adaptor.addChild(root_0, stream_test.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1984:8: 
                    {
                    if ( state.backtracking==0 ) {

                                 etype = new Name(COLON260, "None", expr_contextType.Load);
                             
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (etype != null) {
                      retval.tree = etype;
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "sliceop"

    public static class exprlist_return extends ParserRuleReturnScope {
        public expr etype;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exprlist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1991:1: exprlist[expr_contextType ctype] returns [expr etype] : ( ( expr[null] COMMA )=>e+= expr[ctype] ( options {k=2; } : COMMA e+= expr[ctype] )* ( COMMA )? | expr[ctype] );
    public final TruffleParser.exprlist_return exprlist(expr_contextType ctype) throws RecognitionException {
        TruffleParser.exprlist_return retval = new TruffleParser.exprlist_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COMMA262=null;
        Token COMMA263=null;
        List list_e=null;
        TruffleParser.expr_return expr264 = null;

        TruffleParser.expr_return e = null;
         e = null;
        PythonTree COMMA262_tree=null;
        PythonTree COMMA263_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1993:5: ( ( expr[null] COMMA )=>e+= expr[ctype] ( options {k=2; } : COMMA e+= expr[ctype] )* ( COMMA )? | expr[ctype] )
            int alt134=2;
            alt134 = dfa134.predict(input);
            switch (alt134) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1993:7: ( expr[null] COMMA )=>e+= expr[ctype] ( options {k=2; } : COMMA e+= expr[ctype] )* ( COMMA )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_expr_in_exprlist7227);
                    e=expr(ctype);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e.getTree());
                    if (list_e==null) list_e=new ArrayList();
                    list_e.add(e.getTree());

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1993:44: ( options {k=2; } : COMMA e+= expr[ctype] )*
                    loop132:
                    do {
                        int alt132=2;
                        alt132 = dfa132.predict(input);
                        switch (alt132) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1993:61: COMMA e+= expr[ctype]
                    	    {
                    	    COMMA262=(Token)match(input,COMMA,FOLLOW_COMMA_in_exprlist7239); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA262_tree = (PythonTree)adaptor.create(COMMA262);
                    	    adaptor.addChild(root_0, COMMA262_tree);
                    	    }
                    	    pushFollow(FOLLOW_expr_in_exprlist7243);
                    	    e=expr(ctype);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e.getTree());
                    	    if (list_e==null) list_e=new ArrayList();
                    	    list_e.add(e.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop132;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1993:84: ( COMMA )?
                    int alt133=2;
                    int LA133_0 = input.LA(1);

                    if ( (LA133_0==COMMA) ) {
                        alt133=1;
                    }
                    switch (alt133) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1993:85: COMMA
                            {
                            COMMA263=(Token)match(input,COMMA,FOLLOW_COMMA_in_exprlist7249); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA263_tree = (PythonTree)adaptor.create(COMMA263);
                            adaptor.addChild(root_0, COMMA263_tree);
                            }

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                 retval.etype = new Tuple(((Token)retval.start), actions.castExprs(list_e), ctype);
                             
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1997:7: expr[ctype]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_expr_in_exprlist7268);
                    expr264=expr(ctype);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr264.getTree());
                    if ( state.backtracking==0 ) {

                              retval.etype = actions.castExpr((expr264!=null?((PythonTree)expr264.tree):null));
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "exprlist"

    public static class del_list_return extends ParserRuleReturnScope {
        public List<expr> etypes;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "del_list"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2005:1: del_list returns [List<expr> etypes] : e+= expr[expr_contextType.Del] ( options {k=2; } : COMMA e+= expr[expr_contextType.Del] )* ( COMMA )? ;
    public final TruffleParser.del_list_return del_list() throws RecognitionException {
        TruffleParser.del_list_return retval = new TruffleParser.del_list_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COMMA265=null;
        Token COMMA266=null;
        List list_e=null;
        TruffleParser.expr_return e = null;
         e = null;
        PythonTree COMMA265_tree=null;
        PythonTree COMMA266_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2007:5: (e+= expr[expr_contextType.Del] ( options {k=2; } : COMMA e+= expr[expr_contextType.Del] )* ( COMMA )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2007:7: e+= expr[expr_contextType.Del] ( options {k=2; } : COMMA e+= expr[expr_contextType.Del] )* ( COMMA )?
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_expr_in_del_list7306);
            e=expr(expr_contextType.Del);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e.getTree());
            if (list_e==null) list_e=new ArrayList();
            list_e.add(e.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2007:37: ( options {k=2; } : COMMA e+= expr[expr_contextType.Del] )*
            loop135:
            do {
                int alt135=2;
                alt135 = dfa135.predict(input);
                switch (alt135) {
            	case 1 :
            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2007:54: COMMA e+= expr[expr_contextType.Del]
            	    {
            	    COMMA265=(Token)match(input,COMMA,FOLLOW_COMMA_in_del_list7318); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA265_tree = (PythonTree)adaptor.create(COMMA265);
            	    adaptor.addChild(root_0, COMMA265_tree);
            	    }
            	    pushFollow(FOLLOW_expr_in_del_list7322);
            	    e=expr(expr_contextType.Del);

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e.getTree());
            	    if (list_e==null) list_e=new ArrayList();
            	    list_e.add(e.getTree());


            	    }
            	    break;

            	default :
            	    break loop135;
                }
            } while (true);

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2007:92: ( COMMA )?
            int alt136=2;
            int LA136_0 = input.LA(1);

            if ( (LA136_0==COMMA) ) {
                alt136=1;
            }
            switch (alt136) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2007:93: COMMA
                    {
                    COMMA266=(Token)match(input,COMMA,FOLLOW_COMMA_in_del_list7328); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA266_tree = (PythonTree)adaptor.create(COMMA266);
                    adaptor.addChild(root_0, COMMA266_tree);
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        retval.etypes = actions.makeDeleteList(list_e);
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "del_list"

    public static class testlist_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "testlist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2014:1: testlist[expr_contextType ctype] : ( ( test[null] COMMA )=>t+= test[ctype] ( options {k=2; } : COMMA t+= test[ctype] )* ( COMMA )? | test[ctype] );
    public final TruffleParser.testlist_return testlist(expr_contextType ctype) throws RecognitionException {
        TruffleParser.testlist_return retval = new TruffleParser.testlist_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COMMA267=null;
        Token COMMA268=null;
        List list_t=null;
        TruffleParser.test_return test269 = null;

        TruffleParser.test_return t = null;
         t = null;
        PythonTree COMMA267_tree=null;
        PythonTree COMMA268_tree=null;


            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2023:5: ( ( test[null] COMMA )=>t+= test[ctype] ( options {k=2; } : COMMA t+= test[ctype] )* ( COMMA )? | test[ctype] )
            int alt139=2;
            alt139 = dfa139.predict(input);
            switch (alt139) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2023:7: ( test[null] COMMA )=>t+= test[ctype] ( options {k=2; } : COMMA t+= test[ctype] )* ( COMMA )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_test_in_testlist7381);
                    t=test(ctype);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    if (list_t==null) list_t=new ArrayList();
                    list_t.add(t.getTree());

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2024:22: ( options {k=2; } : COMMA t+= test[ctype] )*
                    loop137:
                    do {
                        int alt137=2;
                        alt137 = dfa137.predict(input);
                        switch (alt137) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2024:39: COMMA t+= test[ctype]
                    	    {
                    	    COMMA267=(Token)match(input,COMMA,FOLLOW_COMMA_in_testlist7393); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA267_tree = (PythonTree)adaptor.create(COMMA267);
                    	    adaptor.addChild(root_0, COMMA267_tree);
                    	    }
                    	    pushFollow(FOLLOW_test_in_testlist7397);
                    	    t=test(ctype);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, t.getTree());
                    	    if (list_t==null) list_t=new ArrayList();
                    	    list_t.add(t.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop137;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2024:62: ( COMMA )?
                    int alt138=2;
                    int LA138_0 = input.LA(1);

                    if ( (LA138_0==COMMA) ) {
                        alt138=1;
                    }
                    switch (alt138) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2024:63: COMMA
                            {
                            COMMA268=(Token)match(input,COMMA,FOLLOW_COMMA_in_testlist7403); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA268_tree = (PythonTree)adaptor.create(COMMA268);
                            adaptor.addChild(root_0, COMMA268_tree);
                            }

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                etype = new Tuple(((Token)retval.start), actions.castExprs(list_t), ctype);
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2028:7: test[ctype]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_test_in_testlist7421);
                    test269=test(ctype);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, test269.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (etype != null) {
                      retval.tree = etype;
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "testlist"

    public static class dictorsetmaker_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dictorsetmaker"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2035:1: dictorsetmaker[Token lcurly] : k+= test[expr_contextType.Load] ( ( COLON v+= test[expr_contextType.Load] ( comp_for[gens] | ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )* ) | ( COMMA k+= test[expr_contextType.Load] )* ) ( COMMA )? | comp_for[gens] ) ;
    public final TruffleParser.dictorsetmaker_return dictorsetmaker(Token lcurly) throws RecognitionException {
        TruffleParser.dictorsetmaker_return retval = new TruffleParser.dictorsetmaker_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COLON270=null;
        Token COMMA272=null;
        Token COLON273=null;
        Token COMMA274=null;
        Token COMMA275=null;
        List list_k=null;
        List list_v=null;
        TruffleParser.comp_for_return comp_for271 = null;

        TruffleParser.comp_for_return comp_for276 = null;

        TruffleParser.test_return k = null;
         k = null;
        TruffleParser.test_return v = null;
         v = null;
        PythonTree COLON270_tree=null;
        PythonTree COMMA272_tree=null;
        PythonTree COLON273_tree=null;
        PythonTree COMMA274_tree=null;
        PythonTree COMMA275_tree=null;


            List gens = new ArrayList();
            expr etype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2045:5: (k+= test[expr_contextType.Load] ( ( COLON v+= test[expr_contextType.Load] ( comp_for[gens] | ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )* ) | ( COMMA k+= test[expr_contextType.Load] )* ) ( COMMA )? | comp_for[gens] ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2045:7: k+= test[expr_contextType.Load] ( ( COLON v+= test[expr_contextType.Load] ( comp_for[gens] | ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )* ) | ( COMMA k+= test[expr_contextType.Load] )* ) ( COMMA )? | comp_for[gens] )
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_test_in_dictorsetmaker7456);
            k=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, k.getTree());
            if (list_k==null) list_k=new ArrayList();
            list_k.add(k.getTree());

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2046:10: ( ( COLON v+= test[expr_contextType.Load] ( comp_for[gens] | ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )* ) | ( COMMA k+= test[expr_contextType.Load] )* ) ( COMMA )? | comp_for[gens] )
            int alt145=2;
            int LA145_0 = input.LA(1);

            if ( (LA145_0==COLON||LA145_0==COMMA||LA145_0==RCURLY) ) {
                alt145=1;
            }
            else if ( (LA145_0==FOR) ) {
                alt145=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 145, 0, input);

                throw nvae;
            }
            switch (alt145) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2047:14: ( COLON v+= test[expr_contextType.Load] ( comp_for[gens] | ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )* ) | ( COMMA k+= test[expr_contextType.Load] )* ) ( COMMA )?
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2047:14: ( COLON v+= test[expr_contextType.Load] ( comp_for[gens] | ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )* ) | ( COMMA k+= test[expr_contextType.Load] )* )
                    int alt143=2;
                    int LA143_0 = input.LA(1);

                    if ( (LA143_0==COLON) ) {
                        alt143=1;
                    }
                    else if ( (LA143_0==COMMA||LA143_0==RCURLY) ) {
                        alt143=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 143, 0, input);

                        throw nvae;
                    }
                    switch (alt143) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2047:15: COLON v+= test[expr_contextType.Load] ( comp_for[gens] | ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )* )
                            {
                            COLON270=(Token)match(input,COLON,FOLLOW_COLON_in_dictorsetmaker7484); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COLON270_tree = (PythonTree)adaptor.create(COLON270);
                            adaptor.addChild(root_0, COLON270_tree);
                            }
                            pushFollow(FOLLOW_test_in_dictorsetmaker7488);
                            v=test(expr_contextType.Load);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, v.getTree());
                            if (list_v==null) list_v=new ArrayList();
                            list_v.add(v.getTree());

                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2048:16: ( comp_for[gens] | ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )* )
                            int alt141=2;
                            int LA141_0 = input.LA(1);

                            if ( (LA141_0==FOR) ) {
                                alt141=1;
                            }
                            else if ( (LA141_0==COMMA||LA141_0==RCURLY) ) {
                                alt141=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return retval;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 141, 0, input);

                                throw nvae;
                            }
                            switch (alt141) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2048:18: comp_for[gens]
                                    {
                                    pushFollow(FOLLOW_comp_for_in_dictorsetmaker7508);
                                    comp_for271=comp_for(gens);

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comp_for271.getTree());
                                    if ( state.backtracking==0 ) {

                                                           Collections.reverse(gens);
                                                           List<comprehension> c = gens;
                                                           etype = new DictComp(((Token)retval.start), actions.castExpr(list_k.get(0)), actions.castExpr(list_v.get(0)), c);
                                                       
                                    }

                                    }
                                    break;
                                case 2 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2054:18: ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )*
                                    {
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2054:18: ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )*
                                    loop140:
                                    do {
                                        int alt140=2;
                                        alt140 = dfa140.predict(input);
                                        switch (alt140) {
                                    	case 1 :
                                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2054:34: COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load]
                                    	    {
                                    	    COMMA272=(Token)match(input,COMMA,FOLLOW_COMMA_in_dictorsetmaker7555); if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) {
                                    	    COMMA272_tree = (PythonTree)adaptor.create(COMMA272);
                                    	    adaptor.addChild(root_0, COMMA272_tree);
                                    	    }
                                    	    pushFollow(FOLLOW_test_in_dictorsetmaker7559);
                                    	    k=test(expr_contextType.Load);

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, k.getTree());
                                    	    if (list_k==null) list_k=new ArrayList();
                                    	    list_k.add(k.getTree());

                                    	    COLON273=(Token)match(input,COLON,FOLLOW_COLON_in_dictorsetmaker7562); if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) {
                                    	    COLON273_tree = (PythonTree)adaptor.create(COLON273);
                                    	    adaptor.addChild(root_0, COLON273_tree);
                                    	    }
                                    	    pushFollow(FOLLOW_test_in_dictorsetmaker7566);
                                    	    v=test(expr_contextType.Load);

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, v.getTree());
                                    	    if (list_v==null) list_v=new ArrayList();
                                    	    list_v.add(v.getTree());


                                    	    }
                                    	    break;

                                    	default :
                                    	    break loop140;
                                        }
                                    } while (true);

                                    if ( state.backtracking==0 ) {

                                                           etype = new Dict(lcurly, actions.castExprs(list_k), actions.castExprs(list_v));
                                                       
                                    }

                                    }
                                    break;

                            }


                            }
                            break;
                        case 2 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2059:15: ( COMMA k+= test[expr_contextType.Load] )*
                            {
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2059:15: ( COMMA k+= test[expr_contextType.Load] )*
                            loop142:
                            do {
                                int alt142=2;
                                int LA142_0 = input.LA(1);

                                if ( (LA142_0==COMMA) ) {
                                    int LA142_1 = input.LA(2);

                                    if ( (LA142_1==NAME||LA142_1==PRINT||(LA142_1>=LAMBDA && LA142_1<=NOT)||(LA142_1>=NONE && LA142_1<=FALSE)||LA142_1==LPAREN||(LA142_1>=PLUS && LA142_1<=MINUS)||(LA142_1>=TILDE && LA142_1<=LBRACK)||LA142_1==LCURLY||(LA142_1>=BACKQUOTE && LA142_1<=STRING)) ) {
                                        alt142=1;
                                    }


                                }


                                switch (alt142) {
                            	case 1 :
                            	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2059:16: COMMA k+= test[expr_contextType.Load]
                            	    {
                            	    COMMA274=(Token)match(input,COMMA,FOLLOW_COMMA_in_dictorsetmaker7622); if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) {
                            	    COMMA274_tree = (PythonTree)adaptor.create(COMMA274);
                            	    adaptor.addChild(root_0, COMMA274_tree);
                            	    }
                            	    pushFollow(FOLLOW_test_in_dictorsetmaker7626);
                            	    k=test(expr_contextType.Load);

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, k.getTree());
                            	    if (list_k==null) list_k=new ArrayList();
                            	    list_k.add(k.getTree());


                            	    }
                            	    break;

                            	default :
                            	    break loop142;
                                }
                            } while (true);

                            if ( state.backtracking==0 ) {

                                                etype = new Set(lcurly, actions.castExprs(list_k));
                                            
                            }

                            }
                            break;

                    }

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2064:14: ( COMMA )?
                    int alt144=2;
                    int LA144_0 = input.LA(1);

                    if ( (LA144_0==COMMA) ) {
                        alt144=1;
                    }
                    switch (alt144) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2064:15: COMMA
                            {
                            COMMA275=(Token)match(input,COMMA,FOLLOW_COMMA_in_dictorsetmaker7676); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA275_tree = (PythonTree)adaptor.create(COMMA275);
                            adaptor.addChild(root_0, COMMA275_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2065:12: comp_for[gens]
                    {
                    pushFollow(FOLLOW_comp_for_in_dictorsetmaker7691);
                    comp_for276=comp_for(gens);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comp_for276.getTree());
                    if ( state.backtracking==0 ) {

                                     Collections.reverse(gens);
                                     List<comprehension> c = gens;
                                     expr e = actions.castExpr(list_k.get(0));
                                     if (e instanceof Context) {
                                         ((Context)e).setContext(expr_contextType.Load);
                                     }
                                     etype = new SetComp(lcurly, actions.castExpr(list_k.get(0)), c);
                                 
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  if (etype != null) {
                      retval.tree = etype;
                  }

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "dictorsetmaker"

    public static class classdef_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "classdef"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2079:1: classdef : ( decorators )? CLASS NAME ( LPAREN ( testlist[expr_contextType.Load] )? RPAREN )? COLON suite[false] ;
    public final TruffleParser.classdef_return classdef() throws RecognitionException {
        TruffleParser.classdef_return retval = new TruffleParser.classdef_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token CLASS278=null;
        Token NAME279=null;
        Token LPAREN280=null;
        Token RPAREN282=null;
        Token COLON283=null;
        TruffleParser.decorators_return decorators277 = null;

        TruffleParser.testlist_return testlist281 = null;

        TruffleParser.suite_return suite284 = null;


        PythonTree CLASS278_tree=null;
        PythonTree NAME279_tree=null;
        PythonTree LPAREN280_tree=null;
        PythonTree RPAREN282_tree=null;
        PythonTree COLON283_tree=null;


            stmt stype = null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2086:5: ( ( decorators )? CLASS NAME ( LPAREN ( testlist[expr_contextType.Load] )? RPAREN )? COLON suite[false] )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2086:7: ( decorators )? CLASS NAME ( LPAREN ( testlist[expr_contextType.Load] )? RPAREN )? COLON suite[false]
            {
            root_0 = (PythonTree)adaptor.nil();

            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2086:7: ( decorators )?
            int alt146=2;
            int LA146_0 = input.LA(1);

            if ( (LA146_0==AT) ) {
                alt146=1;
            }
            switch (alt146) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2086:7: decorators
                    {
                    pushFollow(FOLLOW_decorators_in_classdef7744);
                    decorators277=decorators();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, decorators277.getTree());

                    }
                    break;

            }

            CLASS278=(Token)match(input,CLASS,FOLLOW_CLASS_in_classdef7747); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CLASS278_tree = (PythonTree)adaptor.create(CLASS278);
            adaptor.addChild(root_0, CLASS278_tree);
            }
            NAME279=(Token)match(input,NAME,FOLLOW_NAME_in_classdef7749); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            NAME279_tree = (PythonTree)adaptor.create(NAME279);
            adaptor.addChild(root_0, NAME279_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2086:30: ( LPAREN ( testlist[expr_contextType.Load] )? RPAREN )?
            int alt148=2;
            int LA148_0 = input.LA(1);

            if ( (LA148_0==LPAREN) ) {
                alt148=1;
            }
            switch (alt148) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2086:31: LPAREN ( testlist[expr_contextType.Load] )? RPAREN
                    {
                    LPAREN280=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_classdef7752); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN280_tree = (PythonTree)adaptor.create(LPAREN280);
                    adaptor.addChild(root_0, LPAREN280_tree);
                    }
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2086:38: ( testlist[expr_contextType.Load] )?
                    int alt147=2;
                    int LA147_0 = input.LA(1);

                    if ( (LA147_0==NAME||LA147_0==NOT||LA147_0==LPAREN||(LA147_0>=PLUS && LA147_0<=MINUS)||(LA147_0>=TILDE && LA147_0<=LBRACK)||LA147_0==LCURLY||LA147_0==BACKQUOTE) ) {
                        alt147=1;
                    }
                    else if ( (LA147_0==PRINT) && ((printFunction))) {
                        alt147=1;
                    }
                    else if ( (LA147_0==LAMBDA||(LA147_0>=NONE && LA147_0<=FALSE)||(LA147_0>=INT && LA147_0<=STRING)) ) {
                        alt147=1;
                    }
                    switch (alt147) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2086:38: testlist[expr_contextType.Load]
                            {
                            pushFollow(FOLLOW_testlist_in_classdef7754);
                            testlist281=testlist(expr_contextType.Load);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, testlist281.getTree());

                            }
                            break;

                    }

                    RPAREN282=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_classdef7758); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN282_tree = (PythonTree)adaptor.create(RPAREN282);
                    adaptor.addChild(root_0, RPAREN282_tree);
                    }

                    }
                    break;

            }

            COLON283=(Token)match(input,COLON,FOLLOW_COLON_in_classdef7762); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON283_tree = (PythonTree)adaptor.create(COLON283);
            adaptor.addChild(root_0, COLON283_tree);
            }
            pushFollow(FOLLOW_suite_in_classdef7764);
            suite284=suite(false);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, suite284.getTree());
            if ( state.backtracking==0 ) {

                        Token t = CLASS278;
                        if ((decorators277!=null?((Token)decorators277.start):null) != null) {
                            t = (decorators277!=null?((Token)decorators277.start):null);
                        }
                        stype = new ClassDef(t, actions.cantBeNoneName(NAME279),
                            actions.makeBases(actions.castExpr((testlist281!=null?((PythonTree)testlist281.tree):null))),
                            actions.castStmts((suite284!=null?suite284.stypes:null)),
                            actions.castExprs((decorators277!=null?decorators277.etypes:null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                 retval.tree = stype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "classdef"

    public static class arglist_return extends ParserRuleReturnScope {
        public List args;
        public List keywords;
        public expr starargs;
        public expr kwargs;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arglist"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2102:1: arglist returns [List args, List keywords, expr starargs, expr kwargs] : ( argument[arguments, kws, gens, true, false] ( COMMA argument[arguments, kws, gens, false, false] )* ( COMMA ( STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )? | DOUBLESTAR k= test[expr_contextType.Load] )? )? | STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )? | DOUBLESTAR k= test[expr_contextType.Load] );
    public final TruffleParser.arglist_return arglist() throws RecognitionException {
        TruffleParser.arglist_return retval = new TruffleParser.arglist_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token COMMA286=null;
        Token COMMA288=null;
        Token STAR289=null;
        Token COMMA290=null;
        Token COMMA292=null;
        Token DOUBLESTAR293=null;
        Token DOUBLESTAR294=null;
        Token STAR295=null;
        Token COMMA296=null;
        Token COMMA298=null;
        Token DOUBLESTAR299=null;
        Token DOUBLESTAR300=null;
        TruffleParser.test_return s = null;

        TruffleParser.test_return k = null;

        TruffleParser.argument_return argument285 = null;

        TruffleParser.argument_return argument287 = null;

        TruffleParser.argument_return argument291 = null;

        TruffleParser.argument_return argument297 = null;


        PythonTree COMMA286_tree=null;
        PythonTree COMMA288_tree=null;
        PythonTree STAR289_tree=null;
        PythonTree COMMA290_tree=null;
        PythonTree COMMA292_tree=null;
        PythonTree DOUBLESTAR293_tree=null;
        PythonTree DOUBLESTAR294_tree=null;
        PythonTree STAR295_tree=null;
        PythonTree COMMA296_tree=null;
        PythonTree COMMA298_tree=null;
        PythonTree DOUBLESTAR299_tree=null;
        PythonTree DOUBLESTAR300_tree=null;


            List arguments = new ArrayList();
            List kws = new ArrayList();
            List gens = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2109:5: ( argument[arguments, kws, gens, true, false] ( COMMA argument[arguments, kws, gens, false, false] )* ( COMMA ( STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )? | DOUBLESTAR k= test[expr_contextType.Load] )? )? | STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )? | DOUBLESTAR k= test[expr_contextType.Load] )
            int alt156=3;
            int LA156_0 = input.LA(1);

            if ( (LA156_0==NAME||LA156_0==NOT||LA156_0==LPAREN||(LA156_0>=PLUS && LA156_0<=MINUS)||(LA156_0>=TILDE && LA156_0<=LBRACK)||LA156_0==LCURLY||LA156_0==BACKQUOTE) ) {
                alt156=1;
            }
            else if ( (LA156_0==PRINT) && ((printFunction))) {
                alt156=1;
            }
            else if ( (LA156_0==LAMBDA||(LA156_0>=NONE && LA156_0<=FALSE)||(LA156_0>=INT && LA156_0<=STRING)) ) {
                alt156=1;
            }
            else if ( (LA156_0==STAR) ) {
                alt156=2;
            }
            else if ( (LA156_0==DOUBLESTAR) ) {
                alt156=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 156, 0, input);

                throw nvae;
            }
            switch (alt156) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2109:7: argument[arguments, kws, gens, true, false] ( COMMA argument[arguments, kws, gens, false, false] )* ( COMMA ( STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )? | DOUBLESTAR k= test[expr_contextType.Load] )? )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_argument_in_arglist7806);
                    argument285=argument(arguments, kws, gens, true, false);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argument285.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2109:51: ( COMMA argument[arguments, kws, gens, false, false] )*
                    loop149:
                    do {
                        int alt149=2;
                        int LA149_0 = input.LA(1);

                        if ( (LA149_0==COMMA) ) {
                            int LA149_1 = input.LA(2);

                            if ( (LA149_1==NAME||LA149_1==PRINT||(LA149_1>=LAMBDA && LA149_1<=NOT)||(LA149_1>=NONE && LA149_1<=FALSE)||LA149_1==LPAREN||(LA149_1>=PLUS && LA149_1<=MINUS)||(LA149_1>=TILDE && LA149_1<=LBRACK)||LA149_1==LCURLY||(LA149_1>=BACKQUOTE && LA149_1<=STRING)) ) {
                                alt149=1;
                            }


                        }


                        switch (alt149) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2109:52: COMMA argument[arguments, kws, gens, false, false]
                    	    {
                    	    COMMA286=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist7810); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA286_tree = (PythonTree)adaptor.create(COMMA286);
                    	    adaptor.addChild(root_0, COMMA286_tree);
                    	    }
                    	    pushFollow(FOLLOW_argument_in_arglist7812);
                    	    argument287=argument(arguments, kws, gens, false, false);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, argument287.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop149;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2110:11: ( COMMA ( STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )? | DOUBLESTAR k= test[expr_contextType.Load] )? )?
                    int alt153=2;
                    int LA153_0 = input.LA(1);

                    if ( (LA153_0==COMMA) ) {
                        alt153=1;
                    }
                    switch (alt153) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2110:12: COMMA ( STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )? | DOUBLESTAR k= test[expr_contextType.Load] )?
                            {
                            COMMA288=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist7828); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA288_tree = (PythonTree)adaptor.create(COMMA288);
                            adaptor.addChild(root_0, COMMA288_tree);
                            }
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2111:15: ( STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )? | DOUBLESTAR k= test[expr_contextType.Load] )?
                            int alt152=3;
                            int LA152_0 = input.LA(1);

                            if ( (LA152_0==STAR) ) {
                                alt152=1;
                            }
                            else if ( (LA152_0==DOUBLESTAR) ) {
                                alt152=2;
                            }
                            switch (alt152) {
                                case 1 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2111:17: STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )?
                                    {
                                    STAR289=(Token)match(input,STAR,FOLLOW_STAR_in_arglist7846); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    STAR289_tree = (PythonTree)adaptor.create(STAR289);
                                    adaptor.addChild(root_0, STAR289_tree);
                                    }
                                    pushFollow(FOLLOW_test_in_arglist7850);
                                    s=test(expr_contextType.Load);

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s.getTree());
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2111:52: ( COMMA argument[arguments, kws, gens, false, true] )*
                                    loop150:
                                    do {
                                        int alt150=2;
                                        int LA150_0 = input.LA(1);

                                        if ( (LA150_0==COMMA) ) {
                                            int LA150_1 = input.LA(2);

                                            if ( (LA150_1==NAME||LA150_1==PRINT||(LA150_1>=LAMBDA && LA150_1<=NOT)||(LA150_1>=NONE && LA150_1<=FALSE)||LA150_1==LPAREN||(LA150_1>=PLUS && LA150_1<=MINUS)||(LA150_1>=TILDE && LA150_1<=LBRACK)||LA150_1==LCURLY||(LA150_1>=BACKQUOTE && LA150_1<=STRING)) ) {
                                                alt150=1;
                                            }


                                        }


                                        switch (alt150) {
                                    	case 1 :
                                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2111:53: COMMA argument[arguments, kws, gens, false, true]
                                    	    {
                                    	    COMMA290=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist7854); if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) {
                                    	    COMMA290_tree = (PythonTree)adaptor.create(COMMA290);
                                    	    adaptor.addChild(root_0, COMMA290_tree);
                                    	    }
                                    	    pushFollow(FOLLOW_argument_in_arglist7856);
                                    	    argument291=argument(arguments, kws, gens, false, true);

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, argument291.getTree());

                                    	    }
                                    	    break;

                                    	default :
                                    	    break loop150;
                                        }
                                    } while (true);

                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2111:105: ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )?
                                    int alt151=2;
                                    int LA151_0 = input.LA(1);

                                    if ( (LA151_0==COMMA) ) {
                                        alt151=1;
                                    }
                                    switch (alt151) {
                                        case 1 :
                                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2111:106: COMMA DOUBLESTAR k= test[expr_contextType.Load]
                                            {
                                            COMMA292=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist7862); if (state.failed) return retval;
                                            if ( state.backtracking==0 ) {
                                            COMMA292_tree = (PythonTree)adaptor.create(COMMA292);
                                            adaptor.addChild(root_0, COMMA292_tree);
                                            }
                                            DOUBLESTAR293=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_arglist7864); if (state.failed) return retval;
                                            if ( state.backtracking==0 ) {
                                            DOUBLESTAR293_tree = (PythonTree)adaptor.create(DOUBLESTAR293);
                                            adaptor.addChild(root_0, DOUBLESTAR293_tree);
                                            }
                                            pushFollow(FOLLOW_test_in_arglist7868);
                                            k=test(expr_contextType.Load);

                                            state._fsp--;
                                            if (state.failed) return retval;
                                            if ( state.backtracking==0 ) adaptor.addChild(root_0, k.getTree());

                                            }
                                            break;

                                    }


                                    }
                                    break;
                                case 2 :
                                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2112:17: DOUBLESTAR k= test[expr_contextType.Load]
                                    {
                                    DOUBLESTAR294=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_arglist7889); if (state.failed) return retval;
                                    if ( state.backtracking==0 ) {
                                    DOUBLESTAR294_tree = (PythonTree)adaptor.create(DOUBLESTAR294);
                                    adaptor.addChild(root_0, DOUBLESTAR294_tree);
                                    }
                                    pushFollow(FOLLOW_test_in_arglist7893);
                                    k=test(expr_contextType.Load);

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) adaptor.addChild(root_0, k.getTree());

                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                if (arguments.size() > 1 && gens.size() > 0) {
                                    actions.errorGenExpNotSoleArg(new PythonTree(((Token)retval.start)));
                                }
                                retval.args =arguments;
                                retval.keywords =kws;
                                retval.starargs =actions.castExpr((s!=null?((PythonTree)s.tree):null));
                                retval.kwargs =actions.castExpr((k!=null?((PythonTree)k.tree):null));
                            
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2124:7: STAR s= test[expr_contextType.Load] ( COMMA argument[arguments, kws, gens, false, true] )* ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )?
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    STAR295=(Token)match(input,STAR,FOLLOW_STAR_in_arglist7940); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STAR295_tree = (PythonTree)adaptor.create(STAR295);
                    adaptor.addChild(root_0, STAR295_tree);
                    }
                    pushFollow(FOLLOW_test_in_arglist7944);
                    s=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s.getTree());
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2124:42: ( COMMA argument[arguments, kws, gens, false, true] )*
                    loop154:
                    do {
                        int alt154=2;
                        int LA154_0 = input.LA(1);

                        if ( (LA154_0==COMMA) ) {
                            int LA154_1 = input.LA(2);

                            if ( (LA154_1==NAME||LA154_1==PRINT||(LA154_1>=LAMBDA && LA154_1<=NOT)||(LA154_1>=NONE && LA154_1<=FALSE)||LA154_1==LPAREN||(LA154_1>=PLUS && LA154_1<=MINUS)||(LA154_1>=TILDE && LA154_1<=LBRACK)||LA154_1==LCURLY||(LA154_1>=BACKQUOTE && LA154_1<=STRING)) ) {
                                alt154=1;
                            }


                        }


                        switch (alt154) {
                    	case 1 :
                    	    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2124:43: COMMA argument[arguments, kws, gens, false, true]
                    	    {
                    	    COMMA296=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist7948); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA296_tree = (PythonTree)adaptor.create(COMMA296);
                    	    adaptor.addChild(root_0, COMMA296_tree);
                    	    }
                    	    pushFollow(FOLLOW_argument_in_arglist7950);
                    	    argument297=argument(arguments, kws, gens, false, true);

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, argument297.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop154;
                        }
                    } while (true);

                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2124:95: ( COMMA DOUBLESTAR k= test[expr_contextType.Load] )?
                    int alt155=2;
                    int LA155_0 = input.LA(1);

                    if ( (LA155_0==COMMA) ) {
                        alt155=1;
                    }
                    switch (alt155) {
                        case 1 :
                            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2124:96: COMMA DOUBLESTAR k= test[expr_contextType.Load]
                            {
                            COMMA298=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist7956); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA298_tree = (PythonTree)adaptor.create(COMMA298);
                            adaptor.addChild(root_0, COMMA298_tree);
                            }
                            DOUBLESTAR299=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_arglist7958); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            DOUBLESTAR299_tree = (PythonTree)adaptor.create(DOUBLESTAR299);
                            adaptor.addChild(root_0, DOUBLESTAR299_tree);
                            }
                            pushFollow(FOLLOW_test_in_arglist7962);
                            k=test(expr_contextType.Load);

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, k.getTree());

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {

                                retval.starargs =actions.castExpr((s!=null?((PythonTree)s.tree):null));
                                retval.keywords =kws;
                                retval.kwargs =actions.castExpr((k!=null?((PythonTree)k.tree):null));
                            
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2130:7: DOUBLESTAR k= test[expr_contextType.Load]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    DOUBLESTAR300=(Token)match(input,DOUBLESTAR,FOLLOW_DOUBLESTAR_in_arglist7981); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOUBLESTAR300_tree = (PythonTree)adaptor.create(DOUBLESTAR300);
                    adaptor.addChild(root_0, DOUBLESTAR300_tree);
                    }
                    pushFollow(FOLLOW_test_in_arglist7985);
                    k=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, k.getTree());
                    if ( state.backtracking==0 ) {

                                retval.kwargs =actions.castExpr((k!=null?((PythonTree)k.tree):null));
                            
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arglist"

    public static class argument_return extends ParserRuleReturnScope {
        public boolean genarg;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argument"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2137:1: argument[List arguments, List kws, List gens, boolean first, boolean afterStar] returns [boolean genarg] : t1= test[expr_contextType.Load] ( ( ASSIGN t2= test[expr_contextType.Load] ) | comp_for[$gens] | ) ;
    public final TruffleParser.argument_return argument(List arguments, List kws, List gens, boolean first, boolean afterStar) throws RecognitionException {
        TruffleParser.argument_return retval = new TruffleParser.argument_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token ASSIGN301=null;
        TruffleParser.test_return t1 = null;

        TruffleParser.test_return t2 = null;

        TruffleParser.comp_for_return comp_for302 = null;


        PythonTree ASSIGN301_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2139:5: (t1= test[expr_contextType.Load] ( ( ASSIGN t2= test[expr_contextType.Load] ) | comp_for[$gens] | ) )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2139:7: t1= test[expr_contextType.Load] ( ( ASSIGN t2= test[expr_contextType.Load] ) | comp_for[$gens] | )
            {
            root_0 = (PythonTree)adaptor.nil();

            pushFollow(FOLLOW_test_in_argument8024);
            t1=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, t1.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2140:9: ( ( ASSIGN t2= test[expr_contextType.Load] ) | comp_for[$gens] | )
            int alt157=3;
            switch ( input.LA(1) ) {
            case ASSIGN:
                {
                alt157=1;
                }
                break;
            case FOR:
                {
                alt157=2;
                }
                break;
            case RPAREN:
            case COMMA:
                {
                alt157=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 157, 0, input);

                throw nvae;
            }

            switch (alt157) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2140:10: ( ASSIGN t2= test[expr_contextType.Load] )
                    {
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2140:10: ( ASSIGN t2= test[expr_contextType.Load] )
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2140:11: ASSIGN t2= test[expr_contextType.Load]
                    {
                    ASSIGN301=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_argument8037); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ASSIGN301_tree = (PythonTree)adaptor.create(ASSIGN301);
                    adaptor.addChild(root_0, ASSIGN301_tree);
                    }
                    pushFollow(FOLLOW_test_in_argument8041);
                    t2=test(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, t2.getTree());

                    }

                    if ( state.backtracking==0 ) {

                                    expr newkey = actions.castExpr((t1!=null?((PythonTree)t1.tree):null));
                                    //Loop through all current keys and fail on duplicate.
                                    for(Object o: kws) {
                                        List list = (List)o;
                                        Object oldkey = list.get(0);
                                        if (oldkey instanceof Name && newkey instanceof Name) {
                                            if (((Name)oldkey).getId().equals(((Name)newkey).getId())) {
                                                errorHandler.error("keyword arguments repeated", (t1!=null?((PythonTree)t1.tree):null));
                                            }
                                        }
                                    }
                                    List<expr> exprs = new ArrayList<expr>();
                                    exprs.add(newkey);
                                    exprs.add(actions.castExpr((t2!=null?((PythonTree)t2.tree):null)));
                                    kws.add(exprs);
                                
                    }

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2158:11: comp_for[$gens]
                    {
                    pushFollow(FOLLOW_comp_for_in_argument8067);
                    comp_for302=comp_for(gens);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comp_for302.getTree());
                    if ( state.backtracking==0 ) {

                                    if (!first) {
                                        actions.errorGenExpNotSoleArg((comp_for302!=null?((PythonTree)comp_for302.tree):null));
                                    }
                                    retval.genarg = true;
                                    Collections.reverse(gens);
                                    List<comprehension> c = gens;
                                    arguments.add(new GeneratorExp((t1!=null?((Token)t1.start):null), actions.castExpr((t1!=null?((PythonTree)t1.tree):null)), c));
                                
                    }

                    }
                    break;
                case 3 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2169:11: 
                    {
                    if ( state.backtracking==0 ) {

                                    if (kws.size() > 0) {
                                        errorHandler.error("non-keyword arg after keyword arg", (t1!=null?((PythonTree)t1.tree):null));
                                    } else if (afterStar) {
                                        errorHandler.error("only named arguments may follow *expression", (t1!=null?((PythonTree)t1.tree):null));
                                    }
                                    arguments.add((t1!=null?((PythonTree)t1.tree):null));
                                
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argument"

    public static class list_iter_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "list_iter"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2181:1: list_iter[List gens, List ifs] : ( list_for[gens] | list_if[gens, ifs] );
    public final TruffleParser.list_iter_return list_iter(List gens, List ifs) throws RecognitionException {
        TruffleParser.list_iter_return retval = new TruffleParser.list_iter_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        TruffleParser.list_for_return list_for303 = null;

        TruffleParser.list_if_return list_if304 = null;



        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2182:5: ( list_for[gens] | list_if[gens, ifs] )
            int alt158=2;
            int LA158_0 = input.LA(1);

            if ( (LA158_0==FOR) ) {
                alt158=1;
            }
            else if ( (LA158_0==IF) ) {
                alt158=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 158, 0, input);

                throw nvae;
            }
            switch (alt158) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2182:7: list_for[gens]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_list_for_in_list_iter8132);
                    list_for303=list_for(gens);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, list_for303.getTree());

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2183:7: list_if[gens, ifs]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_list_if_in_list_iter8141);
                    list_if304=list_if(gens, ifs);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, list_if304.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "list_iter"

    public static class list_for_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "list_for"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2187:1: list_for[List gens] : FOR exprlist[expr_contextType.Store] IN testlist[expr_contextType.Load] ( list_iter[gens, ifs] )? ;
    public final TruffleParser.list_for_return list_for(List gens) throws RecognitionException {
        TruffleParser.list_for_return retval = new TruffleParser.list_for_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token FOR305=null;
        Token IN307=null;
        TruffleParser.exprlist_return exprlist306 = null;

        TruffleParser.testlist_return testlist308 = null;

        TruffleParser.list_iter_return list_iter309 = null;


        PythonTree FOR305_tree=null;
        PythonTree IN307_tree=null;


            List ifs = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2191:5: ( FOR exprlist[expr_contextType.Store] IN testlist[expr_contextType.Load] ( list_iter[gens, ifs] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2191:7: FOR exprlist[expr_contextType.Store] IN testlist[expr_contextType.Load] ( list_iter[gens, ifs] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            FOR305=(Token)match(input,FOR,FOLLOW_FOR_in_list_for8167); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FOR305_tree = (PythonTree)adaptor.create(FOR305);
            adaptor.addChild(root_0, FOR305_tree);
            }
            pushFollow(FOLLOW_exprlist_in_list_for8169);
            exprlist306=exprlist(expr_contextType.Store);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, exprlist306.getTree());
            IN307=(Token)match(input,IN,FOLLOW_IN_in_list_for8172); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IN307_tree = (PythonTree)adaptor.create(IN307);
            adaptor.addChild(root_0, IN307_tree);
            }
            pushFollow(FOLLOW_testlist_in_list_for8174);
            testlist308=testlist(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, testlist308.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2191:79: ( list_iter[gens, ifs] )?
            int alt159=2;
            int LA159_0 = input.LA(1);

            if ( (LA159_0==FOR||LA159_0==IF) ) {
                alt159=1;
            }
            switch (alt159) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2191:80: list_iter[gens, ifs]
                    {
                    pushFollow(FOLLOW_list_iter_in_list_for8178);
                    list_iter309=list_iter(gens, ifs);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, list_iter309.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        Collections.reverse(ifs);
                        gens.add(new comprehension(FOR305, (exprlist306!=null?exprlist306.etype:null), actions.castExpr((testlist308!=null?((PythonTree)testlist308.tree):null)), ifs));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "list_for"

    public static class list_if_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "list_if"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2199:1: list_if[List gens, List ifs] : IF test[expr_contextType.Load] ( list_iter[gens, ifs] )? ;
    public final TruffleParser.list_if_return list_if(List gens, List ifs) throws RecognitionException {
        TruffleParser.list_if_return retval = new TruffleParser.list_if_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token IF310=null;
        TruffleParser.test_return test311 = null;

        TruffleParser.list_iter_return list_iter312 = null;


        PythonTree IF310_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2200:5: ( IF test[expr_contextType.Load] ( list_iter[gens, ifs] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2200:7: IF test[expr_contextType.Load] ( list_iter[gens, ifs] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            IF310=(Token)match(input,IF,FOLLOW_IF_in_list_if8208); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IF310_tree = (PythonTree)adaptor.create(IF310);
            adaptor.addChild(root_0, IF310_tree);
            }
            pushFollow(FOLLOW_test_in_list_if8210);
            test311=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, test311.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2200:38: ( list_iter[gens, ifs] )?
            int alt160=2;
            int LA160_0 = input.LA(1);

            if ( (LA160_0==FOR||LA160_0==IF) ) {
                alt160=1;
            }
            switch (alt160) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2200:39: list_iter[gens, ifs]
                    {
                    pushFollow(FOLLOW_list_iter_in_list_if8214);
                    list_iter312=list_iter(gens, ifs);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, list_iter312.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                      ifs.add(actions.castExpr((test311!=null?((PythonTree)test311.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "list_if"

    public static class comp_iter_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp_iter"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2207:1: comp_iter[List gens, List ifs] : ( comp_for[gens] | comp_if[gens, ifs] );
    public final TruffleParser.comp_iter_return comp_iter(List gens, List ifs) throws RecognitionException {
        TruffleParser.comp_iter_return retval = new TruffleParser.comp_iter_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        TruffleParser.comp_for_return comp_for313 = null;

        TruffleParser.comp_if_return comp_if314 = null;



        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2208:5: ( comp_for[gens] | comp_if[gens, ifs] )
            int alt161=2;
            int LA161_0 = input.LA(1);

            if ( (LA161_0==FOR) ) {
                alt161=1;
            }
            else if ( (LA161_0==IF) ) {
                alt161=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 161, 0, input);

                throw nvae;
            }
            switch (alt161) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2208:7: comp_for[gens]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_comp_for_in_comp_iter8245);
                    comp_for313=comp_for(gens);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comp_for313.getTree());

                    }
                    break;
                case 2 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2209:7: comp_if[gens, ifs]
                    {
                    root_0 = (PythonTree)adaptor.nil();

                    pushFollow(FOLLOW_comp_if_in_comp_iter8254);
                    comp_if314=comp_if(gens, ifs);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comp_if314.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comp_iter"

    public static class comp_for_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp_for"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2213:1: comp_for[List gens] : FOR exprlist[expr_contextType.Store] IN or_test[expr_contextType.Load] ( comp_iter[gens, ifs] )? ;
    public final TruffleParser.comp_for_return comp_for(List gens) throws RecognitionException {
        TruffleParser.comp_for_return retval = new TruffleParser.comp_for_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token FOR315=null;
        Token IN317=null;
        TruffleParser.exprlist_return exprlist316 = null;

        TruffleParser.or_test_return or_test318 = null;

        TruffleParser.comp_iter_return comp_iter319 = null;


        PythonTree FOR315_tree=null;
        PythonTree IN317_tree=null;


            List ifs = new ArrayList();

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2217:5: ( FOR exprlist[expr_contextType.Store] IN or_test[expr_contextType.Load] ( comp_iter[gens, ifs] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2217:7: FOR exprlist[expr_contextType.Store] IN or_test[expr_contextType.Load] ( comp_iter[gens, ifs] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            FOR315=(Token)match(input,FOR,FOLLOW_FOR_in_comp_for8280); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FOR315_tree = (PythonTree)adaptor.create(FOR315);
            adaptor.addChild(root_0, FOR315_tree);
            }
            pushFollow(FOLLOW_exprlist_in_comp_for8282);
            exprlist316=exprlist(expr_contextType.Store);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, exprlist316.getTree());
            IN317=(Token)match(input,IN,FOLLOW_IN_in_comp_for8285); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IN317_tree = (PythonTree)adaptor.create(IN317);
            adaptor.addChild(root_0, IN317_tree);
            }
            pushFollow(FOLLOW_or_test_in_comp_for8287);
            or_test318=or_test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, or_test318.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2217:78: ( comp_iter[gens, ifs] )?
            int alt162=2;
            int LA162_0 = input.LA(1);

            if ( (LA162_0==FOR||LA162_0==IF) ) {
                alt162=1;
            }
            switch (alt162) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2217:78: comp_iter[gens, ifs]
                    {
                    pushFollow(FOLLOW_comp_iter_in_comp_for8290);
                    comp_iter319=comp_iter(gens, ifs);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comp_iter319.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        Collections.reverse(ifs);
                        gens.add(new comprehension(FOR315, (exprlist316!=null?exprlist316.etype:null), actions.castExpr((or_test318!=null?((PythonTree)or_test318.tree):null)), ifs));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comp_for"

    public static class comp_if_return extends ParserRuleReturnScope {
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp_if"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2225:1: comp_if[List gens, List ifs] : IF test[expr_contextType.Load] ( comp_iter[gens, ifs] )? ;
    public final TruffleParser.comp_if_return comp_if(List gens, List ifs) throws RecognitionException {
        TruffleParser.comp_if_return retval = new TruffleParser.comp_if_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token IF320=null;
        TruffleParser.test_return test321 = null;

        TruffleParser.comp_iter_return comp_iter322 = null;


        PythonTree IF320_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2226:5: ( IF test[expr_contextType.Load] ( comp_iter[gens, ifs] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2226:7: IF test[expr_contextType.Load] ( comp_iter[gens, ifs] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            IF320=(Token)match(input,IF,FOLLOW_IF_in_comp_if8319); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IF320_tree = (PythonTree)adaptor.create(IF320);
            adaptor.addChild(root_0, IF320_tree);
            }
            pushFollow(FOLLOW_test_in_comp_if8321);
            test321=test(expr_contextType.Load);

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, test321.getTree());
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2226:38: ( comp_iter[gens, ifs] )?
            int alt163=2;
            int LA163_0 = input.LA(1);

            if ( (LA163_0==FOR||LA163_0==IF) ) {
                alt163=1;
            }
            switch (alt163) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2226:38: comp_iter[gens, ifs]
                    {
                    pushFollow(FOLLOW_comp_iter_in_comp_if8324);
                    comp_iter322=comp_iter(gens, ifs);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, comp_iter322.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                      ifs.add(actions.castExpr((test321!=null?((PythonTree)test321.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comp_if"

    public static class yield_expr_return extends ParserRuleReturnScope {
        public expr etype;
        PythonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "yield_expr"
    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2233:1: yield_expr returns [expr etype] : YIELD ( testlist[expr_contextType.Load] )? ;
    public final TruffleParser.yield_expr_return yield_expr() throws RecognitionException {
        TruffleParser.yield_expr_return retval = new TruffleParser.yield_expr_return();
        retval.start = input.LT(1);

        PythonTree root_0 = null;

        Token YIELD323=null;
        TruffleParser.testlist_return testlist324 = null;


        PythonTree YIELD323_tree=null;

        try {
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2239:5: ( YIELD ( testlist[expr_contextType.Load] )? )
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2239:7: YIELD ( testlist[expr_contextType.Load] )?
            {
            root_0 = (PythonTree)adaptor.nil();

            YIELD323=(Token)match(input,YIELD,FOLLOW_YIELD_in_yield_expr8365); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            YIELD323_tree = (PythonTree)adaptor.create(YIELD323);
            adaptor.addChild(root_0, YIELD323_tree);
            }
            // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2239:13: ( testlist[expr_contextType.Load] )?
            int alt164=2;
            int LA164_0 = input.LA(1);

            if ( (LA164_0==NAME||LA164_0==NOT||LA164_0==LPAREN||(LA164_0>=PLUS && LA164_0<=MINUS)||(LA164_0>=TILDE && LA164_0<=LBRACK)||LA164_0==LCURLY||LA164_0==BACKQUOTE) ) {
                alt164=1;
            }
            else if ( (LA164_0==PRINT) && ((printFunction))) {
                alt164=1;
            }
            else if ( (LA164_0==LAMBDA||(LA164_0>=NONE && LA164_0<=FALSE)||(LA164_0>=INT && LA164_0<=STRING)) ) {
                alt164=1;
            }
            switch (alt164) {
                case 1 :
                    // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2239:13: testlist[expr_contextType.Load]
                    {
                    pushFollow(FOLLOW_testlist_in_yield_expr8367);
                    testlist324=testlist(expr_contextType.Load);

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, testlist324.getTree());

                    }
                    break;

            }

            if ( state.backtracking==0 ) {

                        retval.etype = new Yield(YIELD323, actions.castExpr((testlist324!=null?((PythonTree)testlist324.tree):null)));
                    
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (PythonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

                  //needed for y2+=yield_expr
                  retval.tree = retval.etype;

            }
        }

        catch (RecognitionException re) {
            reportError(re);
            errorHandler.recover(this, input,re);
            retval.tree = (PythonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "yield_expr"

    // $ANTLR start synpred1_Truffle
    public final void synpred1_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:590:7: ( LPAREN fpdef[null] COMMA )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:590:8: LPAREN fpdef[null] COMMA
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred1_Truffle1325); if (state.failed) return ;
        pushFollow(FOLLOW_fpdef_in_synpred1_Truffle1327);
        fpdef(null);

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred1_Truffle1330); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Truffle

    // $ANTLR start synpred2_Truffle
    public final void synpred2_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:669:8: ( testlist[null] augassign )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:669:9: testlist[null] augassign
        {
        pushFollow(FOLLOW_testlist_in_synpred2_Truffle1781);
        testlist(null);

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_augassign_in_synpred2_Truffle1784);
        augassign();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Truffle

    // $ANTLR start synpred3_Truffle
    public final void synpred3_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:683:7: ( testlist[null] ASSIGN )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:683:8: testlist[null] ASSIGN
        {
        pushFollow(FOLLOW_testlist_in_synpred3_Truffle1900);
        testlist(null);

        state._fsp--;
        if (state.failed) return ;
        match(input,ASSIGN,FOLLOW_ASSIGN_in_synpred3_Truffle1903); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Truffle

    // $ANTLR start synpred4_Truffle
    public final void synpred4_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:788:7: ( test[null] COMMA )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:788:8: test[null] COMMA
        {
        pushFollow(FOLLOW_test_in_synpred4_Truffle2415);
        test(null);

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred4_Truffle2418); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Truffle

    // $ANTLR start synpred5_Truffle
    public final void synpred5_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:809:7: ( test[null] COMMA test[null] )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:809:8: test[null] COMMA test[null]
        {
        pushFollow(FOLLOW_test_in_synpred5_Truffle2514);
        test(null);

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred5_Truffle2517); if (state.failed) return ;
        pushFollow(FOLLOW_test_in_synpred5_Truffle2519);
        test(null);

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Truffle

    // $ANTLR start synpred6_Truffle
    public final void synpred6_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1122:7: ( ( decorators )? DEF )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1122:8: ( decorators )? DEF
        {
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1122:8: ( decorators )?
        int alt165=2;
        int LA165_0 = input.LA(1);

        if ( (LA165_0==AT) ) {
            alt165=1;
        }
        switch (alt165) {
            case 1 :
                // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1122:8: decorators
                {
                pushFollow(FOLLOW_decorators_in_synpred6_Truffle3608);
                decorators();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        match(input,DEF,FOLLOW_DEF_in_synpred6_Truffle3611); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Truffle

    // $ANTLR start synpred7_Truffle
    public final void synpred7_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1318:9: ( IF or_test[null] ORELSE )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1318:10: IF or_test[null] ORELSE
        {
        match(input,IF,FOLLOW_IF_in_synpred7_Truffle4368); if (state.failed) return ;
        pushFollow(FOLLOW_or_test_in_synpred7_Truffle4370);
        or_test(null);

        state._fsp--;
        if (state.failed) return ;
        match(input,ORELSE,FOLLOW_ORELSE_in_synpred7_Truffle4373); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Truffle

    // $ANTLR start synpred8_Truffle
    public final void synpred8_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1954:7: ( test[null] COLON )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1954:8: test[null] COLON
        {
        pushFollow(FOLLOW_test_in_synpred8_Truffle7021);
        test(null);

        state._fsp--;
        if (state.failed) return ;
        match(input,COLON,FOLLOW_COLON_in_synpred8_Truffle7024); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Truffle

    // $ANTLR start synpred9_Truffle
    public final void synpred9_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1959:7: ( COLON )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1959:8: COLON
        {
        match(input,COLON,FOLLOW_COLON_in_synpred9_Truffle7072); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Truffle

    // $ANTLR start synpred10_Truffle
    public final void synpred10_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1993:7: ( expr[null] COMMA )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:1993:8: expr[null] COMMA
        {
        pushFollow(FOLLOW_expr_in_synpred10_Truffle7217);
        expr(null);

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred10_Truffle7220); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Truffle

    // $ANTLR start synpred11_Truffle
    public final void synpred11_Truffle_fragment() throws RecognitionException {   
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2023:7: ( test[null] COMMA )
        // /Users/zwei/Workspace/zippy/zippy/grammar/Truffle.g:2023:8: test[null] COMMA
        {
        pushFollow(FOLLOW_test_in_synpred11_Truffle7368);
        test(null);

        state._fsp--;
        if (state.failed) return ;
        match(input,COMMA,FOLLOW_COMMA_in_synpred11_Truffle7371); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Truffle

    // Delegated rules

    public final boolean synpred4_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred9_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred10_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_Truffle() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_Truffle_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA30 dfa30 = new DFA30(this);
    protected DFA36 dfa36 = new DFA36(this);
    protected DFA32 dfa32 = new DFA32(this);
    protected DFA41 dfa41 = new DFA41(this);
    protected DFA39 dfa39 = new DFA39(this);
    protected DFA44 dfa44 = new DFA44(this);
    protected DFA42 dfa42 = new DFA42(this);
    protected DFA53 dfa53 = new DFA53(this);
    protected DFA81 dfa81 = new DFA81(this);
    protected DFA90 dfa90 = new DFA90(this);
    protected DFA113 dfa113 = new DFA113(this);
    protected DFA117 dfa117 = new DFA117(this);
    protected DFA130 dfa130 = new DFA130(this);
    protected DFA134 dfa134 = new DFA134(this);
    protected DFA132 dfa132 = new DFA132(this);
    protected DFA135 dfa135 = new DFA135(this);
    protected DFA139 dfa139 = new DFA139(this);
    protected DFA137 dfa137 = new DFA137(this);
    protected DFA140 dfa140 = new DFA140(this);
    static final String DFA30_eotS =
        "\13\uffff";
    static final String DFA30_eofS =
        "\13\uffff";
    static final String DFA30_minS =
        "\1\11\1\uffff\1\0\10\uffff";
    static final String DFA30_maxS =
        "\1\134\1\uffff\1\0\10\uffff";
    static final String DFA30_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\11\1\10";
    static final String DFA30_specialS =
        "\1\0\1\uffff\1\1\10\uffff}>";
    static final String[] DFA30_transitionS = {
            "\1\1\1\uffff\1\2\2\uffff\1\10\1\5\1\uffff\1\5\1\uffff\1\3\3"+
            "\uffff\1\6\1\uffff\1\7\1\uffff\1\6\2\uffff\2\1\2\uffff\1\4\2"+
            "\5\3\uffff\1\5\3\1\1\11\1\uffff\1\1\37\uffff\2\1\3\uffff\2\1"+
            "\1\uffff\1\1\1\uffff\5\1",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA30_eot = DFA.unpackEncodedString(DFA30_eotS);
    static final short[] DFA30_eof = DFA.unpackEncodedString(DFA30_eofS);
    static final char[] DFA30_min = DFA.unpackEncodedStringToUnsignedChars(DFA30_minS);
    static final char[] DFA30_max = DFA.unpackEncodedStringToUnsignedChars(DFA30_maxS);
    static final short[] DFA30_accept = DFA.unpackEncodedString(DFA30_acceptS);
    static final short[] DFA30_special = DFA.unpackEncodedString(DFA30_specialS);
    static final short[][] DFA30_transition;

    static {
        int numStates = DFA30_transitionS.length;
        DFA30_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA30_transition[i] = DFA.unpackEncodedString(DFA30_transitionS[i]);
        }
    }

    class DFA30 extends DFA {

        public DFA30(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 30;
            this.eot = DFA30_eot;
            this.eof = DFA30_eof;
            this.min = DFA30_min;
            this.max = DFA30_max;
            this.accept = DFA30_accept;
            this.special = DFA30_special;
            this.transition = DFA30_transition;
        }
        public String getDescription() {
            return "632:1: small_stmt : ( expr_stmt | del_stmt | pass_stmt | flow_stmt | import_stmt | global_stmt | assert_stmt | {...}? => print_stmt | nonlocal_stmt );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA30_0 = input.LA(1);

                         
                        int index30_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA30_0==NAME||(LA30_0>=LAMBDA && LA30_0<=NOT)||(LA30_0>=NONE && LA30_0<=FALSE)||LA30_0==LPAREN||(LA30_0>=PLUS && LA30_0<=MINUS)||(LA30_0>=TILDE && LA30_0<=LBRACK)||LA30_0==LCURLY||(LA30_0>=BACKQUOTE && LA30_0<=STRING)) ) {s = 1;}

                        else if ( (LA30_0==PRINT) && (((!printFunction)||(printFunction)))) {s = 2;}

                        else if ( (LA30_0==DELETE) ) {s = 3;}

                        else if ( (LA30_0==PASS) ) {s = 4;}

                        else if ( (LA30_0==BREAK||LA30_0==CONTINUE||(LA30_0>=RAISE && LA30_0<=RETURN)||LA30_0==YIELD) ) {s = 5;}

                        else if ( (LA30_0==FROM||LA30_0==IMPORT) ) {s = 6;}

                        else if ( (LA30_0==GLOBAL) ) {s = 7;}

                        else if ( (LA30_0==ASSERT) ) {s = 8;}

                        else if ( (LA30_0==NONLOCAL) ) {s = 9;}

                         
                        input.seek(index30_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA30_2 = input.LA(1);

                         
                        int index30_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((printFunction)) ) {s = 1;}

                        else if ( ((!printFunction)) ) {s = 10;}

                         
                        input.seek(index30_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 30, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA36_eotS =
        "\26\uffff";
    static final String DFA36_eofS =
        "\26\uffff";
    static final String DFA36_minS =
        "\1\11\22\0\3\uffff";
    static final String DFA36_maxS =
        "\1\134\22\0\3\uffff";
    static final String DFA36_acceptS =
        "\23\uffff\1\1\1\2\1\3";
    static final String DFA36_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1"+
        "\16\1\17\1\20\1\21\1\22\3\uffff}>";
    static final String[] DFA36_transitionS = {
            "\1\11\1\uffff\1\12\22\uffff\1\22\1\1\11\uffff\1\13\1\14\1\15"+
            "\2\uffff\1\5\37\uffff\1\2\1\3\3\uffff\1\4\1\6\1\uffff\1\7\1"+
            "\uffff\1\10\1\16\1\17\1\20\1\21",
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
            return "669:7: ( ( testlist[null] augassign )=>lhs= testlist[expr_contextType.AugStore] ( (aay= augassign y1= yield_expr ) | (aat= augassign rhs= testlist[expr_contextType.Load] ) ) | ( testlist[null] ASSIGN )=>lhs= testlist[expr_contextType.Store] ( | ( (at= ASSIGN t+= testlist[expr_contextType.Store] )+ ) | ( (ay= ASSIGN y2+= yield_expr )+ ) ) | lhs= testlist[expr_contextType.Load] )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA36_0 = input.LA(1);

                         
                        int index36_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA36_0==NOT) ) {s = 1;}

                        else if ( (LA36_0==PLUS) ) {s = 2;}

                        else if ( (LA36_0==MINUS) ) {s = 3;}

                        else if ( (LA36_0==TILDE) ) {s = 4;}

                        else if ( (LA36_0==LPAREN) ) {s = 5;}

                        else if ( (LA36_0==LBRACK) ) {s = 6;}

                        else if ( (LA36_0==LCURLY) ) {s = 7;}

                        else if ( (LA36_0==BACKQUOTE) ) {s = 8;}

                        else if ( (LA36_0==NAME) ) {s = 9;}

                        else if ( (LA36_0==PRINT) && ((printFunction))) {s = 10;}

                        else if ( (LA36_0==NONE) ) {s = 11;}

                        else if ( (LA36_0==TRUE) ) {s = 12;}

                        else if ( (LA36_0==FALSE) ) {s = 13;}

                        else if ( (LA36_0==INT) ) {s = 14;}

                        else if ( (LA36_0==FLOAT) ) {s = 15;}

                        else if ( (LA36_0==COMPLEX) ) {s = 16;}

                        else if ( (LA36_0==STRING) ) {s = 17;}

                        else if ( (LA36_0==LAMBDA) ) {s = 18;}

                         
                        input.seek(index36_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA36_1 = input.LA(1);

                         
                        int index36_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA36_2 = input.LA(1);

                         
                        int index36_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA36_3 = input.LA(1);

                         
                        int index36_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA36_4 = input.LA(1);

                         
                        int index36_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA36_5 = input.LA(1);

                         
                        int index36_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA36_6 = input.LA(1);

                         
                        int index36_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA36_7 = input.LA(1);

                         
                        int index36_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA36_8 = input.LA(1);

                         
                        int index36_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_8);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA36_9 = input.LA(1);

                         
                        int index36_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_9);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA36_10 = input.LA(1);

                         
                        int index36_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (((synpred2_Truffle()&&(printFunction))||(synpred2_Truffle()&&(printFunction)))) ) {s = 19;}

                        else if ( (((synpred3_Truffle()&&(printFunction))||(synpred3_Truffle()&&(printFunction)))) ) {s = 20;}

                        else if ( ((printFunction)) ) {s = 21;}

                         
                        input.seek(index36_10);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA36_11 = input.LA(1);

                         
                        int index36_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_11);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA36_12 = input.LA(1);

                         
                        int index36_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_12);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA36_13 = input.LA(1);

                         
                        int index36_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_13);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA36_14 = input.LA(1);

                         
                        int index36_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_14);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA36_15 = input.LA(1);

                         
                        int index36_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_15);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA36_16 = input.LA(1);

                         
                        int index36_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_16);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA36_17 = input.LA(1);

                         
                        int index36_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
                        input.seek(index36_17);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA36_18 = input.LA(1);

                         
                        int index36_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Truffle()) ) {s = 19;}

                        else if ( (synpred3_Truffle()) ) {s = 20;}

                        else if ( (true) ) {s = 21;}

                         
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
    static final String DFA32_eotS =
        "\17\uffff";
    static final String DFA32_eofS =
        "\17\uffff";
    static final String DFA32_minS =
        "\1\66\14\11\2\uffff";
    static final String DFA32_maxS =
        "\1\101\14\134\2\uffff";
    static final String DFA32_acceptS =
        "\15\uffff\1\2\1\1";
    static final String DFA32_specialS =
        "\17\uffff}>";
    static final String[] DFA32_transitionS = {
            "\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
            "\1\15\1\uffff\1\15\22\uffff\2\15\10\uffff\1\16\3\15\2\uffff"+
            "\1\15\37\uffff\2\15\3\uffff\2\15\1\uffff\1\15\1\uffff\5\15",
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
            return "670:9: ( (aay= augassign y1= yield_expr ) | (aat= augassign rhs= testlist[expr_contextType.Load] ) )";
        }
    }
    static final String DFA41_eotS =
        "\25\uffff";
    static final String DFA41_eofS =
        "\25\uffff";
    static final String DFA41_minS =
        "\1\11\22\0\2\uffff";
    static final String DFA41_maxS =
        "\1\134\22\0\2\uffff";
    static final String DFA41_acceptS =
        "\23\uffff\1\1\1\2";
    static final String DFA41_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1"+
        "\16\1\17\1\20\1\21\1\22\2\uffff}>";
    static final String[] DFA41_transitionS = {
            "\1\11\1\uffff\1\12\22\uffff\1\22\1\1\11\uffff\1\13\1\14\1\15"+
            "\2\uffff\1\5\37\uffff\1\2\1\3\3\uffff\1\4\1\6\1\uffff\1\7\1"+
            "\uffff\1\10\1\16\1\17\1\20\1\21",
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

    static final short[] DFA41_eot = DFA.unpackEncodedString(DFA41_eotS);
    static final short[] DFA41_eof = DFA.unpackEncodedString(DFA41_eofS);
    static final char[] DFA41_min = DFA.unpackEncodedStringToUnsignedChars(DFA41_minS);
    static final char[] DFA41_max = DFA.unpackEncodedStringToUnsignedChars(DFA41_maxS);
    static final short[] DFA41_accept = DFA.unpackEncodedString(DFA41_acceptS);
    static final short[] DFA41_special = DFA.unpackEncodedString(DFA41_specialS);
    static final short[][] DFA41_transition;

    static {
        int numStates = DFA41_transitionS.length;
        DFA41_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA41_transition[i] = DFA.unpackEncodedString(DFA41_transitionS[i]);
        }
    }

    class DFA41 extends DFA {

        public DFA41(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 41;
            this.eot = DFA41_eot;
            this.eof = DFA41_eof;
            this.min = DFA41_min;
            this.max = DFA41_max;
            this.accept = DFA41_accept;
            this.special = DFA41_special;
            this.transition = DFA41_transition;
        }
        public String getDescription() {
            return "786:1: printlist returns [boolean newline, List elts] : ( ( test[null] COMMA )=>t+= test[expr_contextType.Load] ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )* (trailcomma= COMMA )? | t+= test[expr_contextType.Load] );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA41_0 = input.LA(1);

                         
                        int index41_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA41_0==NOT) ) {s = 1;}

                        else if ( (LA41_0==PLUS) ) {s = 2;}

                        else if ( (LA41_0==MINUS) ) {s = 3;}

                        else if ( (LA41_0==TILDE) ) {s = 4;}

                        else if ( (LA41_0==LPAREN) ) {s = 5;}

                        else if ( (LA41_0==LBRACK) ) {s = 6;}

                        else if ( (LA41_0==LCURLY) ) {s = 7;}

                        else if ( (LA41_0==BACKQUOTE) ) {s = 8;}

                        else if ( (LA41_0==NAME) ) {s = 9;}

                        else if ( (LA41_0==PRINT) && ((printFunction))) {s = 10;}

                        else if ( (LA41_0==NONE) ) {s = 11;}

                        else if ( (LA41_0==TRUE) ) {s = 12;}

                        else if ( (LA41_0==FALSE) ) {s = 13;}

                        else if ( (LA41_0==INT) ) {s = 14;}

                        else if ( (LA41_0==FLOAT) ) {s = 15;}

                        else if ( (LA41_0==COMPLEX) ) {s = 16;}

                        else if ( (LA41_0==STRING) ) {s = 17;}

                        else if ( (LA41_0==LAMBDA) ) {s = 18;}

                         
                        input.seek(index41_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA41_1 = input.LA(1);

                         
                        int index41_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA41_2 = input.LA(1);

                         
                        int index41_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA41_3 = input.LA(1);

                         
                        int index41_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA41_4 = input.LA(1);

                         
                        int index41_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA41_5 = input.LA(1);

                         
                        int index41_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA41_6 = input.LA(1);

                         
                        int index41_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA41_7 = input.LA(1);

                         
                        int index41_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA41_8 = input.LA(1);

                         
                        int index41_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_8);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA41_9 = input.LA(1);

                         
                        int index41_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_9);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA41_10 = input.LA(1);

                         
                        int index41_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred4_Truffle()&&(printFunction))) ) {s = 19;}

                        else if ( ((printFunction)) ) {s = 20;}

                         
                        input.seek(index41_10);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA41_11 = input.LA(1);

                         
                        int index41_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_11);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA41_12 = input.LA(1);

                         
                        int index41_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_12);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA41_13 = input.LA(1);

                         
                        int index41_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_13);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA41_14 = input.LA(1);

                         
                        int index41_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_14);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA41_15 = input.LA(1);

                         
                        int index41_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_15);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA41_16 = input.LA(1);

                         
                        int index41_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_16);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA41_17 = input.LA(1);

                         
                        int index41_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_17);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA41_18 = input.LA(1);

                         
                        int index41_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index41_18);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 41, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA39_eotS =
        "\30\uffff";
    static final String DFA39_eofS =
        "\30\uffff";
    static final String DFA39_minS =
        "\2\7\26\uffff";
    static final String DFA39_maxS =
        "\1\65\1\134\26\uffff";
    static final String DFA39_acceptS =
        "\2\uffff\1\2\3\uffff\1\1\21\uffff";
    static final String DFA39_specialS =
        "\30\uffff}>";
    static final String[] DFA39_transitionS = {
            "\1\2\52\uffff\1\1\2\uffff\1\2",
            "\1\2\1\uffff\1\6\1\uffff\1\6\22\uffff\2\6\11\uffff\3\6\2\uffff"+
            "\1\6\6\uffff\1\2\30\uffff\2\6\3\uffff\2\6\1\uffff\1\6\1\uffff"+
            "\5\6",
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

    static final short[] DFA39_eot = DFA.unpackEncodedString(DFA39_eotS);
    static final short[] DFA39_eof = DFA.unpackEncodedString(DFA39_eofS);
    static final char[] DFA39_min = DFA.unpackEncodedStringToUnsignedChars(DFA39_minS);
    static final char[] DFA39_max = DFA.unpackEncodedStringToUnsignedChars(DFA39_maxS);
    static final short[] DFA39_accept = DFA.unpackEncodedString(DFA39_acceptS);
    static final short[] DFA39_special = DFA.unpackEncodedString(DFA39_specialS);
    static final short[][] DFA39_transition;

    static {
        int numStates = DFA39_transitionS.length;
        DFA39_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA39_transition[i] = DFA.unpackEncodedString(DFA39_transitionS[i]);
        }
    }

    class DFA39 extends DFA {

        public DFA39(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 39;
            this.eot = DFA39_eot;
            this.eof = DFA39_eof;
            this.min = DFA39_min;
            this.max = DFA39_max;
            this.accept = DFA39_accept;
            this.special = DFA39_special;
            this.transition = DFA39_transition;
        }
        public String getDescription() {
            return "()* loopback of 789:39: ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )*";
        }
    }
    static final String DFA44_eotS =
        "\25\uffff";
    static final String DFA44_eofS =
        "\25\uffff";
    static final String DFA44_minS =
        "\1\11\22\0\2\uffff";
    static final String DFA44_maxS =
        "\1\134\22\0\2\uffff";
    static final String DFA44_acceptS =
        "\23\uffff\1\1\1\2";
    static final String DFA44_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1"+
        "\16\1\17\1\20\1\21\1\22\2\uffff}>";
    static final String[] DFA44_transitionS = {
            "\1\11\1\uffff\1\12\22\uffff\1\22\1\1\11\uffff\1\13\1\14\1\15"+
            "\2\uffff\1\5\37\uffff\1\2\1\3\3\uffff\1\4\1\6\1\uffff\1\7\1"+
            "\uffff\1\10\1\16\1\17\1\20\1\21",
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

    static final short[] DFA44_eot = DFA.unpackEncodedString(DFA44_eotS);
    static final short[] DFA44_eof = DFA.unpackEncodedString(DFA44_eofS);
    static final char[] DFA44_min = DFA.unpackEncodedStringToUnsignedChars(DFA44_minS);
    static final char[] DFA44_max = DFA.unpackEncodedStringToUnsignedChars(DFA44_maxS);
    static final short[] DFA44_accept = DFA.unpackEncodedString(DFA44_acceptS);
    static final short[] DFA44_special = DFA.unpackEncodedString(DFA44_specialS);
    static final short[][] DFA44_transition;

    static {
        int numStates = DFA44_transitionS.length;
        DFA44_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA44_transition[i] = DFA.unpackEncodedString(DFA44_transitionS[i]);
        }
    }

    class DFA44 extends DFA {

        public DFA44(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 44;
            this.eot = DFA44_eot;
            this.eof = DFA44_eof;
            this.min = DFA44_min;
            this.max = DFA44_max;
            this.accept = DFA44_accept;
            this.special = DFA44_special;
            this.transition = DFA44_transition;
        }
        public String getDescription() {
            return "807:1: printlist2 returns [boolean newline, List elts] : ( ( test[null] COMMA test[null] )=>t+= test[expr_contextType.Load] ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )* (trailcomma= COMMA )? | t+= test[expr_contextType.Load] );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA44_0 = input.LA(1);

                         
                        int index44_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA44_0==NOT) ) {s = 1;}

                        else if ( (LA44_0==PLUS) ) {s = 2;}

                        else if ( (LA44_0==MINUS) ) {s = 3;}

                        else if ( (LA44_0==TILDE) ) {s = 4;}

                        else if ( (LA44_0==LPAREN) ) {s = 5;}

                        else if ( (LA44_0==LBRACK) ) {s = 6;}

                        else if ( (LA44_0==LCURLY) ) {s = 7;}

                        else if ( (LA44_0==BACKQUOTE) ) {s = 8;}

                        else if ( (LA44_0==NAME) ) {s = 9;}

                        else if ( (LA44_0==PRINT) && ((printFunction))) {s = 10;}

                        else if ( (LA44_0==NONE) ) {s = 11;}

                        else if ( (LA44_0==TRUE) ) {s = 12;}

                        else if ( (LA44_0==FALSE) ) {s = 13;}

                        else if ( (LA44_0==INT) ) {s = 14;}

                        else if ( (LA44_0==FLOAT) ) {s = 15;}

                        else if ( (LA44_0==COMPLEX) ) {s = 16;}

                        else if ( (LA44_0==STRING) ) {s = 17;}

                        else if ( (LA44_0==LAMBDA) ) {s = 18;}

                         
                        input.seek(index44_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA44_1 = input.LA(1);

                         
                        int index44_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA44_2 = input.LA(1);

                         
                        int index44_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA44_3 = input.LA(1);

                         
                        int index44_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA44_4 = input.LA(1);

                         
                        int index44_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA44_5 = input.LA(1);

                         
                        int index44_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA44_6 = input.LA(1);

                         
                        int index44_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA44_7 = input.LA(1);

                         
                        int index44_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA44_8 = input.LA(1);

                         
                        int index44_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_8);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA44_9 = input.LA(1);

                         
                        int index44_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_9);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA44_10 = input.LA(1);

                         
                        int index44_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred5_Truffle()&&(printFunction))) ) {s = 19;}

                        else if ( ((printFunction)) ) {s = 20;}

                         
                        input.seek(index44_10);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA44_11 = input.LA(1);

                         
                        int index44_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_11);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA44_12 = input.LA(1);

                         
                        int index44_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_12);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA44_13 = input.LA(1);

                         
                        int index44_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_13);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA44_14 = input.LA(1);

                         
                        int index44_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_14);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA44_15 = input.LA(1);

                         
                        int index44_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_15);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA44_16 = input.LA(1);

                         
                        int index44_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_16);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA44_17 = input.LA(1);

                         
                        int index44_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_17);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA44_18 = input.LA(1);

                         
                        int index44_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index44_18);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 44, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA42_eotS =
        "\30\uffff";
    static final String DFA42_eofS =
        "\30\uffff";
    static final String DFA42_minS =
        "\2\7\26\uffff";
    static final String DFA42_maxS =
        "\1\65\1\134\26\uffff";
    static final String DFA42_acceptS =
        "\2\uffff\1\2\3\uffff\1\1\21\uffff";
    static final String DFA42_specialS =
        "\30\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\2\52\uffff\1\1\2\uffff\1\2",
            "\1\2\1\uffff\1\6\1\uffff\1\6\22\uffff\2\6\11\uffff\3\6\2\uffff"+
            "\1\6\6\uffff\1\2\30\uffff\2\6\3\uffff\2\6\1\uffff\1\6\1\uffff"+
            "\5\6",
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

    static final short[] DFA42_eot = DFA.unpackEncodedString(DFA42_eotS);
    static final short[] DFA42_eof = DFA.unpackEncodedString(DFA42_eofS);
    static final char[] DFA42_min = DFA.unpackEncodedStringToUnsignedChars(DFA42_minS);
    static final char[] DFA42_max = DFA.unpackEncodedStringToUnsignedChars(DFA42_maxS);
    static final short[] DFA42_accept = DFA.unpackEncodedString(DFA42_acceptS);
    static final short[] DFA42_special = DFA.unpackEncodedString(DFA42_specialS);
    static final short[][] DFA42_transition;

    static {
        int numStates = DFA42_transitionS.length;
        DFA42_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA42_transition[i] = DFA.unpackEncodedString(DFA42_transitionS[i]);
        }
    }

    class DFA42 extends DFA {

        public DFA42(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 42;
            this.eot = DFA42_eot;
            this.eof = DFA42_eof;
            this.min = DFA42_min;
            this.max = DFA42_max;
            this.accept = DFA42_accept;
            this.special = DFA42_special;
            this.transition = DFA42_transition;
        }
        public String getDescription() {
            return "()* loopback of 810:39: ( options {k=2; } : COMMA t+= test[expr_contextType.Load] )*";
        }
    }
    static final String DFA53_eotS =
        "\4\uffff";
    static final String DFA53_eofS =
        "\4\uffff";
    static final String DFA53_minS =
        "\2\11\2\uffff";
    static final String DFA53_maxS =
        "\1\12\1\33\2\uffff";
    static final String DFA53_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA53_specialS =
        "\4\uffff}>";
    static final String[] DFA53_transitionS = {
            "\1\2\1\1",
            "\1\2\1\1\20\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA53_eot = DFA.unpackEncodedString(DFA53_eotS);
    static final short[] DFA53_eof = DFA.unpackEncodedString(DFA53_eofS);
    static final char[] DFA53_min = DFA.unpackEncodedStringToUnsignedChars(DFA53_minS);
    static final char[] DFA53_max = DFA.unpackEncodedStringToUnsignedChars(DFA53_maxS);
    static final short[] DFA53_accept = DFA.unpackEncodedString(DFA53_acceptS);
    static final short[] DFA53_special = DFA.unpackEncodedString(DFA53_specialS);
    static final short[][] DFA53_transition;

    static {
        int numStates = DFA53_transitionS.length;
        DFA53_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA53_transition[i] = DFA.unpackEncodedString(DFA53_transitionS[i]);
        }
    }

    class DFA53 extends DFA {

        public DFA53(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 53;
            this.eot = DFA53_eot;
            this.eof = DFA53_eof;
            this.min = DFA53_min;
            this.max = DFA53_max;
            this.accept = DFA53_accept;
            this.special = DFA53_special;
            this.transition = DFA53_transition;
        }
        public String getDescription() {
            return "971:12: ( (d+= DOT )* dotted_name | (d+= DOT )+ )";
        }
    }
    static final String DFA81_eotS =
        "\33\uffff";
    static final String DFA81_eofS =
        "\1\2\32\uffff";
    static final String DFA81_minS =
        "\1\7\1\0\31\uffff";
    static final String DFA81_maxS =
        "\1\130\1\0\31\uffff";
    static final String DFA81_acceptS =
        "\2\uffff\1\2\27\uffff\1\1";
    static final String DFA81_specialS =
        "\1\uffff\1\0\31\uffff}>";
    static final String[] DFA81_transitionS = {
            "\1\2\5\uffff\1\2\12\uffff\1\2\1\uffff\1\1\24\uffff\4\2\2\uffff"+
            "\15\2\23\uffff\1\2\1\uffff\2\2",
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

    static final short[] DFA81_eot = DFA.unpackEncodedString(DFA81_eotS);
    static final short[] DFA81_eof = DFA.unpackEncodedString(DFA81_eofS);
    static final char[] DFA81_min = DFA.unpackEncodedStringToUnsignedChars(DFA81_minS);
    static final char[] DFA81_max = DFA.unpackEncodedStringToUnsignedChars(DFA81_maxS);
    static final short[] DFA81_accept = DFA.unpackEncodedString(DFA81_acceptS);
    static final short[] DFA81_special = DFA.unpackEncodedString(DFA81_specialS);
    static final short[][] DFA81_transition;

    static {
        int numStates = DFA81_transitionS.length;
        DFA81_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA81_transition[i] = DFA.unpackEncodedString(DFA81_transitionS[i]);
        }
    }

    class DFA81 extends DFA {

        public DFA81(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 81;
            this.eot = DFA81_eot;
            this.eof = DFA81_eof;
            this.min = DFA81_min;
            this.max = DFA81_max;
            this.accept = DFA81_accept;
            this.special = DFA81_special;
            this.transition = DFA81_transition;
        }
        public String getDescription() {
            return "1318:7: ( ( IF or_test[null] ORELSE )=> IF o2= or_test[ctype] ORELSE e= test[expr_contextType.Load] | -> or_test )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA81_1 = input.LA(1);

                         
                        int index81_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Truffle()) ) {s = 26;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index81_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 81, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA90_eotS =
        "\14\uffff";
    static final String DFA90_eofS =
        "\14\uffff";
    static final String DFA90_minS =
        "\1\34\10\uffff\1\11\2\uffff";
    static final String DFA90_maxS =
        "\1\111\10\uffff\1\134\2\uffff";
    static final String DFA90_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\uffff\1\12\1\11";
    static final String DFA90_specialS =
        "\14\uffff}>";
    static final String[] DFA90_transitionS = {
            "\1\7\1\11\1\uffff\1\10\44\uffff\1\1\1\2\1\3\1\4\1\5\1\6",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\13\1\uffff\1\13\23\uffff\1\12\11\uffff\3\13\2\uffff\1\13"+
            "\37\uffff\2\13\3\uffff\2\13\1\uffff\1\13\1\uffff\5\13",
            "",
            ""
    };

    static final short[] DFA90_eot = DFA.unpackEncodedString(DFA90_eotS);
    static final short[] DFA90_eof = DFA.unpackEncodedString(DFA90_eofS);
    static final char[] DFA90_min = DFA.unpackEncodedStringToUnsignedChars(DFA90_minS);
    static final char[] DFA90_max = DFA.unpackEncodedStringToUnsignedChars(DFA90_maxS);
    static final short[] DFA90_accept = DFA.unpackEncodedString(DFA90_acceptS);
    static final short[] DFA90_special = DFA.unpackEncodedString(DFA90_specialS);
    static final short[][] DFA90_transition;

    static {
        int numStates = DFA90_transitionS.length;
        DFA90_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA90_transition[i] = DFA.unpackEncodedString(DFA90_transitionS[i]);
        }
    }

    class DFA90 extends DFA {

        public DFA90(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 90;
            this.eot = DFA90_eot;
            this.eof = DFA90_eof;
            this.min = DFA90_min;
            this.max = DFA90_max;
            this.accept = DFA90_accept;
            this.special = DFA90_special;
            this.transition = DFA90_transition;
        }
        public String getDescription() {
            return "1414:1: comp_op returns [cmpopType op] : ( LESS | GREATER | EQUAL | GREATEREQUAL | LESSEQUAL | NOTEQUAL | IN | NOT IN | IS | IS NOT );";
        }
    }
    static final String DFA113_eotS =
        "\16\uffff";
    static final String DFA113_eofS =
        "\16\uffff";
    static final String DFA113_minS =
        "\1\11\15\uffff";
    static final String DFA113_maxS =
        "\1\134\15\uffff";
    static final String DFA113_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\2\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14";
    static final String DFA113_specialS =
        "\1\0\15\uffff}>";
    static final String[] DFA113_transitionS = {
            "\1\5\1\uffff\1\6\35\uffff\1\7\1\10\1\11\2\uffff\1\1\45\uffff"+
            "\1\2\1\uffff\1\3\1\uffff\1\4\1\12\1\13\1\14\1\15",
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

    static final short[] DFA113_eot = DFA.unpackEncodedString(DFA113_eotS);
    static final short[] DFA113_eof = DFA.unpackEncodedString(DFA113_eofS);
    static final char[] DFA113_min = DFA.unpackEncodedStringToUnsignedChars(DFA113_minS);
    static final char[] DFA113_max = DFA.unpackEncodedStringToUnsignedChars(DFA113_maxS);
    static final short[] DFA113_accept = DFA.unpackEncodedString(DFA113_acceptS);
    static final short[] DFA113_special = DFA.unpackEncodedString(DFA113_specialS);
    static final short[][] DFA113_transition;

    static {
        int numStates = DFA113_transitionS.length;
        DFA113_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA113_transition[i] = DFA.unpackEncodedString(DFA113_transitionS[i]);
        }
    }

    class DFA113 extends DFA {

        public DFA113(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 113;
            this.eot = DFA113_eot;
            this.eof = DFA113_eof;
            this.min = DFA113_min;
            this.max = DFA113_max;
            this.accept = DFA113_accept;
            this.special = DFA113_special;
            this.transition = DFA113_transition;
        }
        public String getDescription() {
            return "1739:1: atom returns [Token lparen = null] : ( LPAREN ( yield_expr | testlist_gexp -> testlist_gexp | ) RPAREN | LBRACK ( listmaker[$LBRACK] -> listmaker | ) RBRACK | LCURLY ( dictorsetmaker[$LCURLY] -> dictorsetmaker | ) RCURLY | lb= BACKQUOTE testlist[expr_contextType.Load] rb= BACKQUOTE | name_or_print | NONE | TRUE | FALSE | INT | FLOAT | COMPLEX | (S+= STRING )+ );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA113_0 = input.LA(1);

                         
                        int index113_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA113_0==LPAREN) ) {s = 1;}

                        else if ( (LA113_0==LBRACK) ) {s = 2;}

                        else if ( (LA113_0==LCURLY) ) {s = 3;}

                        else if ( (LA113_0==BACKQUOTE) ) {s = 4;}

                        else if ( (LA113_0==NAME) ) {s = 5;}

                        else if ( (LA113_0==PRINT) && ((printFunction))) {s = 6;}

                        else if ( (LA113_0==NONE) ) {s = 7;}

                        else if ( (LA113_0==TRUE) ) {s = 8;}

                        else if ( (LA113_0==FALSE) ) {s = 9;}

                        else if ( (LA113_0==INT) ) {s = 10;}

                        else if ( (LA113_0==FLOAT) ) {s = 11;}

                        else if ( (LA113_0==COMPLEX) ) {s = 12;}

                        else if ( (LA113_0==STRING) ) {s = 13;}

                         
                        input.seek(index113_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 113, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA117_eotS =
        "\26\uffff";
    static final String DFA117_eofS =
        "\26\uffff";
    static final String DFA117_minS =
        "\1\57\1\11\24\uffff";
    static final String DFA117_maxS =
        "\1\62\1\134\24\uffff";
    static final String DFA117_acceptS =
        "\2\uffff\1\2\1\1\22\uffff";
    static final String DFA117_specialS =
        "\26\uffff}>";
    static final String[] DFA117_transitionS = {
            "\1\2\2\uffff\1\1",
            "\1\3\1\uffff\1\3\22\uffff\2\3\11\uffff\3\3\2\uffff\1\3\1\2"+
            "\36\uffff\2\3\3\uffff\2\3\1\uffff\1\3\1\uffff\5\3",
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

    static final short[] DFA117_eot = DFA.unpackEncodedString(DFA117_eotS);
    static final short[] DFA117_eof = DFA.unpackEncodedString(DFA117_eofS);
    static final char[] DFA117_min = DFA.unpackEncodedStringToUnsignedChars(DFA117_minS);
    static final char[] DFA117_max = DFA.unpackEncodedStringToUnsignedChars(DFA117_maxS);
    static final short[] DFA117_accept = DFA.unpackEncodedString(DFA117_acceptS);
    static final short[] DFA117_special = DFA.unpackEncodedString(DFA117_specialS);
    static final short[][] DFA117_transition;

    static {
        int numStates = DFA117_transitionS.length;
        DFA117_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA117_transition[i] = DFA.unpackEncodedString(DFA117_transitionS[i]);
        }
    }

    class DFA117 extends DFA {

        public DFA117(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 117;
            this.eot = DFA117_eot;
            this.eof = DFA117_eof;
            this.min = DFA117_min;
            this.max = DFA117_max;
            this.accept = DFA117_accept;
            this.special = DFA117_special;
            this.transition = DFA117_transition;
        }
        public String getDescription() {
            return "()* loopback of 1860:11: ( options {k=2; } : c1= COMMA t+= test[$expr::ctype] )*";
        }
    }
    static final String DFA130_eotS =
        "\27\uffff";
    static final String DFA130_eofS =
        "\27\uffff";
    static final String DFA130_minS =
        "\1\11\1\uffff\22\0\3\uffff";
    static final String DFA130_maxS =
        "\1\134\1\uffff\22\0\3\uffff";
    static final String DFA130_acceptS =
        "\1\uffff\1\1\22\uffff\1\3\1\2\1\4";
    static final String DFA130_specialS =
        "\1\0\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\3\uffff}>";
    static final String[] DFA130_transitionS = {
            "\1\12\1\1\1\13\22\uffff\1\23\1\2\11\uffff\1\14\1\15\1\16\2\uffff"+
            "\1\6\1\uffff\1\24\35\uffff\1\3\1\4\3\uffff\1\5\1\7\1\uffff\1"+
            "\10\1\uffff\1\11\1\17\1\20\1\21\1\22",
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
            return "1945:1: subscript returns [slice sltype] : (d1= DOT DOT DOT | ( test[null] COLON )=>lower= test[expr_contextType.Load] (c1= COLON (upper1= test[expr_contextType.Load] )? ( sliceop )? )? | ( COLON )=>c2= COLON (upper2= test[expr_contextType.Load] )? ( sliceop )? | test[expr_contextType.Load] );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA130_0 = input.LA(1);

                         
                        int index130_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA130_0==DOT) ) {s = 1;}

                        else if ( (LA130_0==NOT) ) {s = 2;}

                        else if ( (LA130_0==PLUS) ) {s = 3;}

                        else if ( (LA130_0==MINUS) ) {s = 4;}

                        else if ( (LA130_0==TILDE) ) {s = 5;}

                        else if ( (LA130_0==LPAREN) ) {s = 6;}

                        else if ( (LA130_0==LBRACK) ) {s = 7;}

                        else if ( (LA130_0==LCURLY) ) {s = 8;}

                        else if ( (LA130_0==BACKQUOTE) ) {s = 9;}

                        else if ( (LA130_0==NAME) ) {s = 10;}

                        else if ( (LA130_0==PRINT) && ((printFunction))) {s = 11;}

                        else if ( (LA130_0==NONE) ) {s = 12;}

                        else if ( (LA130_0==TRUE) ) {s = 13;}

                        else if ( (LA130_0==FALSE) ) {s = 14;}

                        else if ( (LA130_0==INT) ) {s = 15;}

                        else if ( (LA130_0==FLOAT) ) {s = 16;}

                        else if ( (LA130_0==COMPLEX) ) {s = 17;}

                        else if ( (LA130_0==STRING) ) {s = 18;}

                        else if ( (LA130_0==LAMBDA) ) {s = 19;}

                        else if ( (LA130_0==COLON) && (synpred9_Truffle())) {s = 20;}

                         
                        input.seek(index130_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA130_2 = input.LA(1);

                         
                        int index130_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA130_3 = input.LA(1);

                         
                        int index130_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA130_4 = input.LA(1);

                         
                        int index130_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA130_5 = input.LA(1);

                         
                        int index130_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA130_6 = input.LA(1);

                         
                        int index130_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA130_7 = input.LA(1);

                         
                        int index130_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA130_8 = input.LA(1);

                         
                        int index130_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA130_9 = input.LA(1);

                         
                        int index130_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA130_10 = input.LA(1);

                         
                        int index130_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA130_11 = input.LA(1);

                         
                        int index130_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred8_Truffle()&&(printFunction))) ) {s = 21;}

                        else if ( ((printFunction)) ) {s = 22;}

                         
                        input.seek(index130_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA130_12 = input.LA(1);

                         
                        int index130_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA130_13 = input.LA(1);

                         
                        int index130_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA130_14 = input.LA(1);

                         
                        int index130_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA130_15 = input.LA(1);

                         
                        int index130_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA130_16 = input.LA(1);

                         
                        int index130_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA130_17 = input.LA(1);

                         
                        int index130_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA130_18 = input.LA(1);

                         
                        int index130_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA130_19 = input.LA(1);

                         
                        int index130_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Truffle()) ) {s = 21;}

                        else if ( (true) ) {s = 22;}

                         
                        input.seek(index130_19);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 130, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA134_eotS =
        "\23\uffff";
    static final String DFA134_eofS =
        "\23\uffff";
    static final String DFA134_minS =
        "\1\11\20\0\2\uffff";
    static final String DFA134_maxS =
        "\1\134\20\0\2\uffff";
    static final String DFA134_acceptS =
        "\21\uffff\1\1\1\2";
    static final String DFA134_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1"+
        "\16\1\17\1\20\2\uffff}>";
    static final String[] DFA134_transitionS = {
            "\1\10\1\uffff\1\11\35\uffff\1\12\1\13\1\14\2\uffff\1\4\37\uffff"+
            "\1\1\1\2\3\uffff\1\3\1\5\1\uffff\1\6\1\uffff\1\7\1\15\1\16\1"+
            "\17\1\20",
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

    static final short[] DFA134_eot = DFA.unpackEncodedString(DFA134_eotS);
    static final short[] DFA134_eof = DFA.unpackEncodedString(DFA134_eofS);
    static final char[] DFA134_min = DFA.unpackEncodedStringToUnsignedChars(DFA134_minS);
    static final char[] DFA134_max = DFA.unpackEncodedStringToUnsignedChars(DFA134_maxS);
    static final short[] DFA134_accept = DFA.unpackEncodedString(DFA134_acceptS);
    static final short[] DFA134_special = DFA.unpackEncodedString(DFA134_specialS);
    static final short[][] DFA134_transition;

    static {
        int numStates = DFA134_transitionS.length;
        DFA134_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA134_transition[i] = DFA.unpackEncodedString(DFA134_transitionS[i]);
        }
    }

    class DFA134 extends DFA {

        public DFA134(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 134;
            this.eot = DFA134_eot;
            this.eof = DFA134_eof;
            this.min = DFA134_min;
            this.max = DFA134_max;
            this.accept = DFA134_accept;
            this.special = DFA134_special;
            this.transition = DFA134_transition;
        }
        public String getDescription() {
            return "1991:1: exprlist[expr_contextType ctype] returns [expr etype] : ( ( expr[null] COMMA )=>e+= expr[ctype] ( options {k=2; } : COMMA e+= expr[ctype] )* ( COMMA )? | expr[ctype] );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA134_0 = input.LA(1);

                         
                        int index134_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA134_0==PLUS) ) {s = 1;}

                        else if ( (LA134_0==MINUS) ) {s = 2;}

                        else if ( (LA134_0==TILDE) ) {s = 3;}

                        else if ( (LA134_0==LPAREN) ) {s = 4;}

                        else if ( (LA134_0==LBRACK) ) {s = 5;}

                        else if ( (LA134_0==LCURLY) ) {s = 6;}

                        else if ( (LA134_0==BACKQUOTE) ) {s = 7;}

                        else if ( (LA134_0==NAME) ) {s = 8;}

                        else if ( (LA134_0==PRINT) && ((printFunction))) {s = 9;}

                        else if ( (LA134_0==NONE) ) {s = 10;}

                        else if ( (LA134_0==TRUE) ) {s = 11;}

                        else if ( (LA134_0==FALSE) ) {s = 12;}

                        else if ( (LA134_0==INT) ) {s = 13;}

                        else if ( (LA134_0==FLOAT) ) {s = 14;}

                        else if ( (LA134_0==COMPLEX) ) {s = 15;}

                        else if ( (LA134_0==STRING) ) {s = 16;}

                         
                        input.seek(index134_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA134_1 = input.LA(1);

                         
                        int index134_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA134_2 = input.LA(1);

                         
                        int index134_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA134_3 = input.LA(1);

                         
                        int index134_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA134_4 = input.LA(1);

                         
                        int index134_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA134_5 = input.LA(1);

                         
                        int index134_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA134_6 = input.LA(1);

                         
                        int index134_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA134_7 = input.LA(1);

                         
                        int index134_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA134_8 = input.LA(1);

                         
                        int index134_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_8);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA134_9 = input.LA(1);

                         
                        int index134_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred10_Truffle()&&(printFunction))) ) {s = 17;}

                        else if ( ((printFunction)) ) {s = 18;}

                         
                        input.seek(index134_9);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA134_10 = input.LA(1);

                         
                        int index134_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_10);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA134_11 = input.LA(1);

                         
                        int index134_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_11);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA134_12 = input.LA(1);

                         
                        int index134_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_12);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA134_13 = input.LA(1);

                         
                        int index134_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_13);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA134_14 = input.LA(1);

                         
                        int index134_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_14);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA134_15 = input.LA(1);

                         
                        int index134_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_15);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA134_16 = input.LA(1);

                         
                        int index134_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Truffle()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index134_16);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 134, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA132_eotS =
        "\24\uffff";
    static final String DFA132_eofS =
        "\24\uffff";
    static final String DFA132_minS =
        "\1\34\1\11\22\uffff";
    static final String DFA132_maxS =
        "\1\62\1\134\22\uffff";
    static final String DFA132_acceptS =
        "\2\uffff\1\2\1\1\20\uffff";
    static final String DFA132_specialS =
        "\24\uffff}>";
    static final String[] DFA132_transitionS = {
            "\1\2\25\uffff\1\1",
            "\1\3\1\uffff\1\3\20\uffff\1\2\14\uffff\3\3\2\uffff\1\3\37\uffff"+
            "\2\3\3\uffff\2\3\1\uffff\1\3\1\uffff\5\3",
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

    static final short[] DFA132_eot = DFA.unpackEncodedString(DFA132_eotS);
    static final short[] DFA132_eof = DFA.unpackEncodedString(DFA132_eofS);
    static final char[] DFA132_min = DFA.unpackEncodedStringToUnsignedChars(DFA132_minS);
    static final char[] DFA132_max = DFA.unpackEncodedStringToUnsignedChars(DFA132_maxS);
    static final short[] DFA132_accept = DFA.unpackEncodedString(DFA132_acceptS);
    static final short[] DFA132_special = DFA.unpackEncodedString(DFA132_specialS);
    static final short[][] DFA132_transition;

    static {
        int numStates = DFA132_transitionS.length;
        DFA132_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA132_transition[i] = DFA.unpackEncodedString(DFA132_transitionS[i]);
        }
    }

    class DFA132 extends DFA {

        public DFA132(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 132;
            this.eot = DFA132_eot;
            this.eof = DFA132_eof;
            this.min = DFA132_min;
            this.max = DFA132_max;
            this.accept = DFA132_accept;
            this.special = DFA132_special;
            this.transition = DFA132_transition;
        }
        public String getDescription() {
            return "()* loopback of 1993:44: ( options {k=2; } : COMMA e+= expr[ctype] )*";
        }
    }
    static final String DFA135_eotS =
        "\26\uffff";
    static final String DFA135_eofS =
        "\26\uffff";
    static final String DFA135_minS =
        "\2\7\24\uffff";
    static final String DFA135_maxS =
        "\1\65\1\134\24\uffff";
    static final String DFA135_acceptS =
        "\2\uffff\1\2\3\uffff\1\1\17\uffff";
    static final String DFA135_specialS =
        "\26\uffff}>";
    static final String[] DFA135_transitionS = {
            "\1\2\52\uffff\1\1\2\uffff\1\2",
            "\1\2\1\uffff\1\6\1\uffff\1\6\35\uffff\3\6\2\uffff\1\6\6\uffff"+
            "\1\2\30\uffff\2\6\3\uffff\2\6\1\uffff\1\6\1\uffff\5\6",
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

    static final short[] DFA135_eot = DFA.unpackEncodedString(DFA135_eotS);
    static final short[] DFA135_eof = DFA.unpackEncodedString(DFA135_eofS);
    static final char[] DFA135_min = DFA.unpackEncodedStringToUnsignedChars(DFA135_minS);
    static final char[] DFA135_max = DFA.unpackEncodedStringToUnsignedChars(DFA135_maxS);
    static final short[] DFA135_accept = DFA.unpackEncodedString(DFA135_acceptS);
    static final short[] DFA135_special = DFA.unpackEncodedString(DFA135_specialS);
    static final short[][] DFA135_transition;

    static {
        int numStates = DFA135_transitionS.length;
        DFA135_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA135_transition[i] = DFA.unpackEncodedString(DFA135_transitionS[i]);
        }
    }

    class DFA135 extends DFA {

        public DFA135(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 135;
            this.eot = DFA135_eot;
            this.eof = DFA135_eof;
            this.min = DFA135_min;
            this.max = DFA135_max;
            this.accept = DFA135_accept;
            this.special = DFA135_special;
            this.transition = DFA135_transition;
        }
        public String getDescription() {
            return "()* loopback of 2007:37: ( options {k=2; } : COMMA e+= expr[expr_contextType.Del] )*";
        }
    }
    static final String DFA139_eotS =
        "\25\uffff";
    static final String DFA139_eofS =
        "\25\uffff";
    static final String DFA139_minS =
        "\1\11\22\0\2\uffff";
    static final String DFA139_maxS =
        "\1\134\22\0\2\uffff";
    static final String DFA139_acceptS =
        "\23\uffff\1\1\1\2";
    static final String DFA139_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1"+
        "\16\1\17\1\20\1\21\1\22\2\uffff}>";
    static final String[] DFA139_transitionS = {
            "\1\11\1\uffff\1\12\22\uffff\1\22\1\1\11\uffff\1\13\1\14\1\15"+
            "\2\uffff\1\5\37\uffff\1\2\1\3\3\uffff\1\4\1\6\1\uffff\1\7\1"+
            "\uffff\1\10\1\16\1\17\1\20\1\21",
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

    static final short[] DFA139_eot = DFA.unpackEncodedString(DFA139_eotS);
    static final short[] DFA139_eof = DFA.unpackEncodedString(DFA139_eofS);
    static final char[] DFA139_min = DFA.unpackEncodedStringToUnsignedChars(DFA139_minS);
    static final char[] DFA139_max = DFA.unpackEncodedStringToUnsignedChars(DFA139_maxS);
    static final short[] DFA139_accept = DFA.unpackEncodedString(DFA139_acceptS);
    static final short[] DFA139_special = DFA.unpackEncodedString(DFA139_specialS);
    static final short[][] DFA139_transition;

    static {
        int numStates = DFA139_transitionS.length;
        DFA139_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA139_transition[i] = DFA.unpackEncodedString(DFA139_transitionS[i]);
        }
    }

    class DFA139 extends DFA {

        public DFA139(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 139;
            this.eot = DFA139_eot;
            this.eof = DFA139_eof;
            this.min = DFA139_min;
            this.max = DFA139_max;
            this.accept = DFA139_accept;
            this.special = DFA139_special;
            this.transition = DFA139_transition;
        }
        public String getDescription() {
            return "2014:1: testlist[expr_contextType ctype] : ( ( test[null] COMMA )=>t+= test[ctype] ( options {k=2; } : COMMA t+= test[ctype] )* ( COMMA )? | test[ctype] );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA139_0 = input.LA(1);

                         
                        int index139_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA139_0==NOT) ) {s = 1;}

                        else if ( (LA139_0==PLUS) ) {s = 2;}

                        else if ( (LA139_0==MINUS) ) {s = 3;}

                        else if ( (LA139_0==TILDE) ) {s = 4;}

                        else if ( (LA139_0==LPAREN) ) {s = 5;}

                        else if ( (LA139_0==LBRACK) ) {s = 6;}

                        else if ( (LA139_0==LCURLY) ) {s = 7;}

                        else if ( (LA139_0==BACKQUOTE) ) {s = 8;}

                        else if ( (LA139_0==NAME) ) {s = 9;}

                        else if ( (LA139_0==PRINT) && ((printFunction))) {s = 10;}

                        else if ( (LA139_0==NONE) ) {s = 11;}

                        else if ( (LA139_0==TRUE) ) {s = 12;}

                        else if ( (LA139_0==FALSE) ) {s = 13;}

                        else if ( (LA139_0==INT) ) {s = 14;}

                        else if ( (LA139_0==FLOAT) ) {s = 15;}

                        else if ( (LA139_0==COMPLEX) ) {s = 16;}

                        else if ( (LA139_0==STRING) ) {s = 17;}

                        else if ( (LA139_0==LAMBDA) ) {s = 18;}

                         
                        input.seek(index139_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA139_1 = input.LA(1);

                         
                        int index139_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA139_2 = input.LA(1);

                         
                        int index139_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA139_3 = input.LA(1);

                         
                        int index139_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA139_4 = input.LA(1);

                         
                        int index139_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA139_5 = input.LA(1);

                         
                        int index139_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA139_6 = input.LA(1);

                         
                        int index139_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA139_7 = input.LA(1);

                         
                        int index139_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_7);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA139_8 = input.LA(1);

                         
                        int index139_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_8);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA139_9 = input.LA(1);

                         
                        int index139_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_9);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA139_10 = input.LA(1);

                         
                        int index139_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((synpred11_Truffle()&&(printFunction))) ) {s = 19;}

                        else if ( ((printFunction)) ) {s = 20;}

                         
                        input.seek(index139_10);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA139_11 = input.LA(1);

                         
                        int index139_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_11);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA139_12 = input.LA(1);

                         
                        int index139_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_12);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA139_13 = input.LA(1);

                         
                        int index139_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_13);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA139_14 = input.LA(1);

                         
                        int index139_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_14);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA139_15 = input.LA(1);

                         
                        int index139_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_15);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA139_16 = input.LA(1);

                         
                        int index139_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_16);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA139_17 = input.LA(1);

                         
                        int index139_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_17);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA139_18 = input.LA(1);

                         
                        int index139_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Truffle()) ) {s = 19;}

                        else if ( (true) ) {s = 20;}

                         
                        input.seek(index139_18);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 139, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA137_eotS =
        "\100\uffff";
    static final String DFA137_eofS =
        "\2\2\76\uffff";
    static final String DFA137_minS =
        "\2\7\76\uffff";
    static final String DFA137_maxS =
        "\1\130\1\134\76\uffff";
    static final String DFA137_acceptS =
        "\2\uffff\1\2\25\uffff\1\1\6\uffff\1\1\40\uffff";
    static final String DFA137_specialS =
        "\100\uffff}>";
    static final String[] DFA137_transitionS = {
            "\1\2\20\uffff\1\2\1\uffff\1\2\24\uffff\3\2\1\1\2\uffff\15\2"+
            "\23\uffff\1\2\2\uffff\1\2",
            "\1\2\1\uffff\1\30\1\uffff\1\30\14\uffff\1\2\1\uffff\1\2\3\uffff"+
            "\2\30\11\uffff\3\30\2\uffff\1\30\4\2\2\uffff\15\2\14\uffff\2"+
            "\30\3\uffff\2\30\1\2\1\30\1\uffff\1\37\4\30",
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

    static final short[] DFA137_eot = DFA.unpackEncodedString(DFA137_eotS);
    static final short[] DFA137_eof = DFA.unpackEncodedString(DFA137_eofS);
    static final char[] DFA137_min = DFA.unpackEncodedStringToUnsignedChars(DFA137_minS);
    static final char[] DFA137_max = DFA.unpackEncodedStringToUnsignedChars(DFA137_maxS);
    static final short[] DFA137_accept = DFA.unpackEncodedString(DFA137_acceptS);
    static final short[] DFA137_special = DFA.unpackEncodedString(DFA137_specialS);
    static final short[][] DFA137_transition;

    static {
        int numStates = DFA137_transitionS.length;
        DFA137_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA137_transition[i] = DFA.unpackEncodedString(DFA137_transitionS[i]);
        }
    }

    class DFA137 extends DFA {

        public DFA137(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 137;
            this.eot = DFA137_eot;
            this.eof = DFA137_eof;
            this.min = DFA137_min;
            this.max = DFA137_max;
            this.accept = DFA137_accept;
            this.special = DFA137_special;
            this.transition = DFA137_transition;
        }
        public String getDescription() {
            return "()* loopback of 2024:22: ( options {k=2; } : COMMA t+= test[ctype] )*";
        }
    }
    static final String DFA140_eotS =
        "\26\uffff";
    static final String DFA140_eofS =
        "\26\uffff";
    static final String DFA140_minS =
        "\1\62\1\11\24\uffff";
    static final String DFA140_maxS =
        "\1\127\1\134\24\uffff";
    static final String DFA140_acceptS =
        "\2\uffff\1\2\1\1\22\uffff";
    static final String DFA140_specialS =
        "\26\uffff}>";
    static final String[] DFA140_transitionS = {
            "\1\1\44\uffff\1\2",
            "\1\3\1\uffff\1\3\22\uffff\2\3\11\uffff\3\3\2\uffff\1\3\37\uffff"+
            "\2\3\3\uffff\2\3\1\uffff\1\3\1\2\5\3",
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

    static final short[] DFA140_eot = DFA.unpackEncodedString(DFA140_eotS);
    static final short[] DFA140_eof = DFA.unpackEncodedString(DFA140_eofS);
    static final char[] DFA140_min = DFA.unpackEncodedStringToUnsignedChars(DFA140_minS);
    static final char[] DFA140_max = DFA.unpackEncodedStringToUnsignedChars(DFA140_maxS);
    static final short[] DFA140_accept = DFA.unpackEncodedString(DFA140_acceptS);
    static final short[] DFA140_special = DFA.unpackEncodedString(DFA140_specialS);
    static final short[][] DFA140_transition;

    static {
        int numStates = DFA140_transitionS.length;
        DFA140_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA140_transition[i] = DFA.unpackEncodedString(DFA140_transitionS[i]);
        }
    }

    class DFA140 extends DFA {

        public DFA140(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 140;
            this.eot = DFA140_eot;
            this.eof = DFA140_eof;
            this.min = DFA140_min;
            this.max = DFA140_max;
            this.accept = DFA140_accept;
            this.special = DFA140_special;
            this.transition = DFA140_transition;
        }
        public String getDescription() {
            return "()* loopback of 2054:18: ( options {k=2; } : COMMA k+= test[expr_contextType.Load] COLON v+= test[expr_contextType.Load] )*";
        }
    }
 

    public static final BitSet FOLLOW_NEWLINE_in_single_input118 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_EOF_in_single_input121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_stmt_in_single_input137 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_single_input139 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_EOF_in_single_input142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compound_stmt_in_single_input158 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_single_input160 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_EOF_in_single_input163 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEWLINE_in_file_input215 = new BitSet(new long[]{0x00007FFCCF8FCA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_stmt_in_file_input225 = new BitSet(new long[]{0x00007FFCCF8FCA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_EOF_in_file_input244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEADING_WS_in_eval_input298 = new BitSet(new long[]{0x00004E00C0000A80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_NEWLINE_in_eval_input302 = new BitSet(new long[]{0x00004E00C0000A80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_eval_input306 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_eval_input310 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_EOF_in_eval_input314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_dotted_attr366 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_DOT_in_dotted_attr377 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_dotted_attr381 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_NAME_in_name_or_print446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRINT_in_name_or_print460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_attr0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_decorator791 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_dotted_attr_in_decorator793 = new BitSet(new long[]{0x0000400000000080L});
    public static final BitSet FOLLOW_LPAREN_in_decorator801 = new BitSet(new long[]{0x0018CE00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_arglist_in_decorator811 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RPAREN_in_decorator855 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_decorator877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_decorator_in_decorators905 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_decorators_in_funcdef943 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_DEF_in_funcdef946 = new BitSet(new long[]{0x0000000000000A00L});
    public static final BitSet FOLLOW_name_or_print_in_funcdef948 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_parameters_in_funcdef950 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_funcdef952 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_funcdef954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_parameters987 = new BitSet(new long[]{0x0018C00000000200L});
    public static final BitSet FOLLOW_varargslist_in_parameters996 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RPAREN_in_parameters1040 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fpdef_in_defparameter1073 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_defparameter1077 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_defparameter1079 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defparameter_in_varargslist1125 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_varargslist1136 = new BitSet(new long[]{0x0000400000000200L});
    public static final BitSet FOLLOW_defparameter_in_varargslist1140 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_varargslist1152 = new BitSet(new long[]{0x0018000000000002L});
    public static final BitSet FOLLOW_STAR_in_varargslist1165 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist1169 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_varargslist1172 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_varargslist1174 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist1178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_varargslist1194 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist1198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_varargslist1236 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist1240 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_varargslist1243 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_varargslist1245 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist1249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_varargslist1267 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_varargslist1271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_fpdef1308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_fpdef1335 = new BitSet(new long[]{0x0000400000000200L});
    public static final BitSet FOLLOW_fplist_in_fpdef1337 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RPAREN_in_fpdef1339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_fpdef1355 = new BitSet(new long[]{0x0000400000000200L});
    public static final BitSet FOLLOW_fplist_in_fpdef1358 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RPAREN_in_fpdef1360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fpdef_in_fplist1389 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_fplist1406 = new BitSet(new long[]{0x0000400000000200L});
    public static final BitSet FOLLOW_fpdef_in_fplist1410 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_fplist1416 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_stmt_in_stmt1452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compound_stmt_in_stmt1468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_small_stmt_in_simple_stmt1504 = new BitSet(new long[]{0x0020000000000080L});
    public static final BitSet FOLLOW_SEMI_in_simple_stmt1514 = new BitSet(new long[]{0x00005F1CCA8ACA00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_small_stmt_in_simple_stmt1518 = new BitSet(new long[]{0x0020000000000080L});
    public static final BitSet FOLLOW_SEMI_in_simple_stmt1523 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_simple_stmt1527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_stmt_in_small_stmt1550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_del_stmt_in_small_stmt1565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pass_stmt_in_small_stmt1580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_flow_stmt_in_small_stmt1595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_stmt_in_small_stmt1610 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_global_stmt_in_small_stmt1625 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assert_stmt_in_small_stmt1652 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_print_stmt_in_small_stmt1671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonlocal_stmt_in_small_stmt1686 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NONLOCAL_in_nonlocal_stmt1721 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_nonlocal_stmt1725 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_nonlocal_stmt1736 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_nonlocal_stmt1740 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1791 = new BitSet(new long[]{0xFFC0000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_augassign_in_expr_stmt1807 = new BitSet(new long[]{0x0000011800028000L});
    public static final BitSet FOLLOW_yield_expr_in_expr_stmt1811 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_augassign_in_expr_stmt1851 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1910 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_expr_stmt1937 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt1941 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_expr_stmt1986 = new BitSet(new long[]{0x0000011800028000L});
    public static final BitSet FOLLOW_yield_expr_in_expr_stmt1990 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_testlist_in_expr_stmt2038 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUSEQUAL_in_augassign2080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUSEQUAL_in_augassign2098 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAREQUAL_in_augassign2116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SLASHEQUAL_in_augassign2134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERCENTEQUAL_in_augassign2152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMPEREQUAL_in_augassign2170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VBAREQUAL_in_augassign2188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CIRCUMFLEXEQUAL_in_augassign2206 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEFTSHIFTEQUAL_in_augassign2224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHTSHIFTEQUAL_in_augassign2242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAREQUAL_in_augassign2260 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESLASHEQUAL_in_augassign2278 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRINT_in_print_stmt2318 = new BitSet(new long[]{0x00004E00C0000A02L,0x000000001F58C004L});
    public static final BitSet FOLLOW_printlist_in_print_stmt2329 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHTSHIFT_in_print_stmt2348 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_printlist2_in_print_stmt2352 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_printlist2432 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_printlist2444 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_printlist2448 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_printlist2456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_printlist2477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_printlist22534 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_printlist22546 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_printlist22550 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_printlist22558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_printlist22579 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DELETE_in_del_stmt2616 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_del_list_in_del_stmt2618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PASS_in_pass_stmt2654 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_break_stmt_in_flow_stmt2680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_continue_stmt_in_flow_stmt2688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_return_stmt_in_flow_stmt2696 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_raise_stmt_in_flow_stmt2704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_yield_stmt_in_flow_stmt2712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_break_stmt2740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTINUE_in_continue_stmt2776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_return_stmt2812 = new BitSet(new long[]{0x00004E00C0000A02L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_return_stmt2821 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_yield_expr_in_yield_stmt2886 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RAISE_in_raise_stmt2922 = new BitSet(new long[]{0x00004E00C0000A02L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_raise_stmt2927 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_raise_stmt2931 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_raise_stmt2935 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_raise_stmt2947 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_raise_stmt2951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_name_in_import_stmt2984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_from_in_import_stmt2992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_in_import_name3020 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_dotted_as_names_in_import_name3022 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_import_from3059 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_DOT_in_import_from3064 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_dotted_name_in_import_from3067 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_DOT_in_import_from3073 = new BitSet(new long[]{0x0000000008000400L});
    public static final BitSet FOLLOW_IMPORT_in_import_from3077 = new BitSet(new long[]{0x0008400000000200L});
    public static final BitSet FOLLOW_STAR_in_import_from3088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_as_names_in_import_from3113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_import_from3136 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_import_as_names_in_import_from3140 = new BitSet(new long[]{0x0004800000000000L});
    public static final BitSet FOLLOW_COMMA_in_import_from3142 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RPAREN_in_import_from3145 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_as_name_in_import_as_names3194 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_import_as_names3197 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_import_as_name_in_import_as_names3202 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_NAME_in_import_as_name3243 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_AS_in_import_as_name3246 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_import_as_name3250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dotted_name_in_dotted_as_name3290 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_AS_in_dotted_as_name3293 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_dotted_as_name3297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dotted_as_name_in_dotted_as_names3333 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_dotted_as_names3336 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_dotted_as_name_in_dotted_as_names3341 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_NAME_in_dotted_name3375 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_DOT_in_dotted_name3378 = new BitSet(new long[]{0x00001FFFFFFFFA00L});
    public static final BitSet FOLLOW_attr_in_dotted_name3382 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_GLOBAL_in_global_stmt3418 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_global_stmt3422 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_global_stmt3425 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_global_stmt3429 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_EXEC_in_exec_stmt3467 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_expr_in_exec_stmt3469 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_IN_in_exec_stmt3473 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_exec_stmt3477 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_exec_stmt3481 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_exec_stmt3485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSERT_in_assert_stmt3526 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_assert_stmt3530 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_assert_stmt3534 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_assert_stmt3538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_if_stmt_in_compound_stmt3567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_while_stmt_in_compound_stmt3575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_for_stmt_in_compound_stmt3583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_try_stmt_in_compound_stmt3591 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_with_stmt_in_compound_stmt3599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcdef_in_compound_stmt3616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_classdef_in_compound_stmt3624 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_if_stmt3652 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_if_stmt3654 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_if_stmt3657 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_if_stmt3661 = new BitSet(new long[]{0x0000000200100002L});
    public static final BitSet FOLLOW_elif_clause_in_if_stmt3664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_else_clause_in_elif_clause3709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELIF_in_elif_clause3725 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_elif_clause3727 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_elif_clause3730 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_elif_clause3732 = new BitSet(new long[]{0x0000000200100002L});
    public static final BitSet FOLLOW_elif_clause_in_elif_clause3744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ORELSE_in_else_clause3804 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_else_clause3806 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_else_clause3810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_while_stmt3847 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_while_stmt3849 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_while_stmt3852 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_while_stmt3856 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_ORELSE_in_while_stmt3860 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_while_stmt3862 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_while_stmt3866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_for_stmt3905 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_exprlist_in_for_stmt3907 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_IN_in_for_stmt3910 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_for_stmt3912 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_for_stmt3915 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_for_stmt3919 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_ORELSE_in_for_stmt3931 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_for_stmt3933 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_for_stmt3937 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRY_in_try_stmt3980 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_try_stmt3982 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_try_stmt3986 = new BitSet(new long[]{0x0000000000600000L});
    public static final BitSet FOLLOW_except_clause_in_try_stmt3999 = new BitSet(new long[]{0x0000000200600002L});
    public static final BitSet FOLLOW_ORELSE_in_try_stmt4003 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_try_stmt4005 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_try_stmt4009 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_FINALLY_in_try_stmt4015 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_try_stmt4017 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_try_stmt4021 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FINALLY_in_try_stmt4044 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_try_stmt4046 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_try_stmt4050 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WITH_in_with_stmt4099 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_with_item_in_with_stmt4103 = new BitSet(new long[]{0x0005000000000000L});
    public static final BitSet FOLLOW_COMMA_in_with_stmt4113 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_with_item_in_with_stmt4117 = new BitSet(new long[]{0x0005000000000000L});
    public static final BitSet FOLLOW_COLON_in_with_stmt4121 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_with_stmt4123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_with_item4160 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_AS_in_with_item4164 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_expr_in_with_item4166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXCEPT_in_except_clause4205 = new BitSet(new long[]{0x00014E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_except_clause4210 = new BitSet(new long[]{0x0005000000002000L});
    public static final BitSet FOLLOW_set_in_except_clause4214 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_except_clause4224 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_except_clause4231 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_except_clause4233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simple_stmt_in_suite4279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEWLINE_in_suite4295 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_INDENT_in_suite4297 = new BitSet(new long[]{0x00007FFCCF8FCA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_stmt_in_suite4306 = new BitSet(new long[]{0x00007FFCCF8FCAA0L,0x000000001F58C000L});
    public static final BitSet FOLLOW_DEDENT_in_suite4326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_test_in_test4356 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_IF_in_test4378 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_or_test_in_test4382 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_ORELSE_in_test4385 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_test4389 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lambdef_in_test4434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_test_in_or_test4469 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_OR_in_or_test4485 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_and_test_in_or_test4489 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_not_test_in_and_test4570 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_AND_in_and_test4586 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_not_test_in_and_test4590 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_NOT_in_not_test4674 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_not_test_in_not_test4678 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_in_not_test4695 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_comparison4744 = new BitSet(new long[]{0x00000000B0000002L,0x00000000000003F0L});
    public static final BitSet FOLLOW_comp_op_in_comparison4758 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_expr_in_comparison4762 = new BitSet(new long[]{0x00000000B0000002L,0x00000000000003F0L});
    public static final BitSet FOLLOW_LESS_in_comp_op4843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp_op4859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUAL_in_comp_op4875 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATEREQUAL_in_comp_op4891 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESSEQUAL_in_comp_op4907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTEQUAL_in_comp_op4943 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_comp_op4959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_comp_op4975 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_IN_in_comp_op4977 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_comp_op4993 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IS_in_comp_op5009 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_NOT_in_comp_op5011 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xor_expr_in_expr5063 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_VBAR_in_expr5078 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_xor_expr_in_expr5082 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_and_expr_in_xor_expr5161 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L});
    public static final BitSet FOLLOW_CIRCUMFLEX_in_xor_expr5176 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_and_expr_in_xor_expr5180 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L});
    public static final BitSet FOLLOW_shift_expr_in_and_expr5258 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_AMPER_in_and_expr5273 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_shift_expr_in_and_expr5277 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_arith_expr_in_shift_expr5360 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002004L});
    public static final BitSet FOLLOW_shift_op_in_shift_expr5374 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_arith_expr_in_shift_expr5378 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002004L});
    public static final BitSet FOLLOW_LEFTSHIFT_in_shift_op5462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RIGHTSHIFT_in_shift_op5478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_arith_expr5524 = new BitSet(new long[]{0x0000000000000002L,0x000000000000C000L});
    public static final BitSet FOLLOW_arith_op_in_arith_expr5537 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_term_in_arith_expr5541 = new BitSet(new long[]{0x0000000000000002L,0x000000000000C000L});
    public static final BitSet FOLLOW_PLUS_in_arith_op5649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_arith_op5665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_factor_in_term5711 = new BitSet(new long[]{0x0008000000000002L,0x0000000000070000L});
    public static final BitSet FOLLOW_term_op_in_term5724 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_factor_in_term5728 = new BitSet(new long[]{0x0008000000000002L,0x0000000000070000L});
    public static final BitSet FOLLOW_STAR_in_term_op5810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SLASH_in_term_op5826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERCENT_in_term_op5842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESLASH_in_term_op5858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_factor5897 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_factor_in_factor5901 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_factor5917 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_factor_in_factor5921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_factor5937 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_factor_in_factor5941 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_power_in_factor5957 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_power5996 = new BitSet(new long[]{0x0010400000000402L,0x0000000000100000L});
    public static final BitSet FOLLOW_trailer_in_power6001 = new BitSet(new long[]{0x0010400000000402L,0x0000000000100000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_power6016 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_factor_in_power6018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom6068 = new BitSet(new long[]{0x0000CF18C0028A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_yield_expr_in_atom6086 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_testlist_gexp_in_atom6106 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RPAREN_in_atom6149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACK_in_atom6157 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F78C000L});
    public static final BitSet FOLLOW_listmaker_in_atom6166 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACK_in_atom6209 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LCURLY_in_atom6217 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001FD8C000L});
    public static final BitSet FOLLOW_dictorsetmaker_in_atom6226 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_RCURLY_in_atom6270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BACKQUOTE_in_atom6281 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_atom6283 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_BACKQUOTE_in_atom6288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_or_print_in_atom6306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NONE_in_atom6324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_atom6343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_atom6362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_atom6381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_atom6420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMPLEX_in_atom6438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_atom6459 = new BitSet(new long[]{0x0000000000000002L,0x0000000010000000L});
    public static final BitSet FOLLOW_test_in_listmaker6502 = new BitSet(new long[]{0x0004000001000002L});
    public static final BitSet FOLLOW_list_for_in_listmaker6514 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listmaker6546 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_listmaker6550 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listmaker6579 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_testlist_gexp6611 = new BitSet(new long[]{0x0004000001000002L});
    public static final BitSet FOLLOW_COMMA_in_testlist_gexp6635 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_testlist_gexp6639 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_testlist_gexp6647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comp_for_in_testlist_gexp6701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LAMBDA_in_lambdef6765 = new BitSet(new long[]{0x0019400000000200L});
    public static final BitSet FOLLOW_varargslist_in_lambdef6768 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_lambdef6772 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_lambdef6774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_trailer6813 = new BitSet(new long[]{0x0018CE00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_arglist_in_trailer6822 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RPAREN_in_trailer6864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACK_in_trailer6872 = new BitSet(new long[]{0x00014E00C0000E00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_subscriptlist_in_trailer6874 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACK_in_trailer6877 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_trailer6893 = new BitSet(new long[]{0x00001FFFFFFFFA00L});
    public static final BitSet FOLLOW_attr_in_trailer6895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subscript_in_subscriptlist6934 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_subscriptlist6946 = new BitSet(new long[]{0x00014E00C0000E00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_subscript_in_subscriptlist6950 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_subscriptlist6957 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_subscript7000 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_subscript7002 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOT_in_subscript7004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_subscript7034 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COLON_in_subscript7040 = new BitSet(new long[]{0x00014E00C0000A02L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_subscript7045 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_sliceop_in_subscript7051 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_subscript7082 = new BitSet(new long[]{0x00014E00C0000A02L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_subscript7087 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_sliceop_in_subscript7093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_subscript7111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_sliceop7148 = new BitSet(new long[]{0x00004E00C0000A02L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_sliceop7156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_exprlist7227 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_exprlist7239 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_expr_in_exprlist7243 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_exprlist7249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_exprlist7268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_del_list7306 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_del_list7318 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_expr_in_del_list7322 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_del_list7328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_testlist7381 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_testlist7393 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_testlist7397 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_testlist7403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_testlist7421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_dictorsetmaker7456 = new BitSet(new long[]{0x0005000001000002L});
    public static final BitSet FOLLOW_COLON_in_dictorsetmaker7484 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_dictorsetmaker7488 = new BitSet(new long[]{0x0004000001000000L});
    public static final BitSet FOLLOW_comp_for_in_dictorsetmaker7508 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_dictorsetmaker7555 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_dictorsetmaker7559 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_dictorsetmaker7562 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_dictorsetmaker7566 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_dictorsetmaker7622 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_dictorsetmaker7626 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_dictorsetmaker7676 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comp_for_in_dictorsetmaker7691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_decorators_in_classdef7744 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_CLASS_in_classdef7747 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NAME_in_classdef7749 = new BitSet(new long[]{0x0001400000000000L});
    public static final BitSet FOLLOW_LPAREN_in_classdef7752 = new BitSet(new long[]{0x0000CE00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_classdef7754 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RPAREN_in_classdef7758 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_classdef7762 = new BitSet(new long[]{0x00005F1CCA8ACA80L,0x000000001F58C000L});
    public static final BitSet FOLLOW_suite_in_classdef7764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argument_in_arglist7806 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist7810 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_argument_in_arglist7812 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist7828 = new BitSet(new long[]{0x0018000000000002L});
    public static final BitSet FOLLOW_STAR_in_arglist7846 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_arglist7850 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist7854 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_argument_in_arglist7856 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist7862 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_arglist7864 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_arglist7868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_arglist7889 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_arglist7893 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_arglist7940 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_arglist7944 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist7948 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_argument_in_arglist7950 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist7956 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_arglist7958 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_arglist7962 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLESTAR_in_arglist7981 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_arglist7985 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_argument8024 = new BitSet(new long[]{0x0006000001000000L});
    public static final BitSet FOLLOW_ASSIGN_in_argument8037 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_argument8041 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comp_for_in_argument8067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_list_for_in_list_iter8132 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_list_if_in_list_iter8141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_list_for8167 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_exprlist_in_list_for8169 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_IN_in_list_for8172 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_list_for8174 = new BitSet(new long[]{0x0000000005000002L});
    public static final BitSet FOLLOW_list_iter_in_list_for8178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_list_if8208 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_list_if8210 = new BitSet(new long[]{0x0000000005000002L});
    public static final BitSet FOLLOW_list_iter_in_list_if8214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comp_for_in_comp_iter8245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comp_if_in_comp_iter8254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_comp_for8280 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_exprlist_in_comp_for8282 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_IN_in_comp_for8285 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_or_test_in_comp_for8287 = new BitSet(new long[]{0x0004000005000002L});
    public static final BitSet FOLLOW_comp_iter_in_comp_for8290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_comp_if8319 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_comp_if8321 = new BitSet(new long[]{0x0004000005000002L});
    public static final BitSet FOLLOW_comp_iter_in_comp_if8324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_YIELD_in_yield_expr8365 = new BitSet(new long[]{0x00004E00C0000A02L,0x000000001F58C000L});
    public static final BitSet FOLLOW_testlist_in_yield_expr8367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred1_Truffle1325 = new BitSet(new long[]{0x0000400000000200L});
    public static final BitSet FOLLOW_fpdef_in_synpred1_Truffle1327 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred1_Truffle1330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_testlist_in_synpred2_Truffle1781 = new BitSet(new long[]{0xFFC0000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_augassign_in_synpred2_Truffle1784 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_testlist_in_synpred3_Truffle1900 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ASSIGN_in_synpred3_Truffle1903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_synpred4_Truffle2415 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred4_Truffle2418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_synpred5_Truffle2514 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred5_Truffle2517 = new BitSet(new long[]{0x00004E00C0000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_test_in_synpred5_Truffle2519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_decorators_in_synpred6_Truffle3608 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_DEF_in_synpred6_Truffle3611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_synpred7_Truffle4368 = new BitSet(new long[]{0x00004E0080000A00L,0x000000001F58C000L});
    public static final BitSet FOLLOW_or_test_in_synpred7_Truffle4370 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_ORELSE_in_synpred7_Truffle4373 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_synpred8_Truffle7021 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_synpred8_Truffle7024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_synpred9_Truffle7072 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_synpred10_Truffle7217 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred10_Truffle7220 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_test_in_synpred11_Truffle7368 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_COMMA_in_synpred11_Truffle7371 = new BitSet(new long[]{0x0000000000000002L});

}