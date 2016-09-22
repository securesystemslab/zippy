package edu.uci.python.ast;

import edu.uci.python.nodes.EmptyNode;
import edu.uci.python.nodes.NoneNode;
import edu.uci.python.nodes.argument.ArgumentsNode;
import edu.uci.python.nodes.argument.ReadDefaultArgumentNode;
import edu.uci.python.nodes.argument.ReadIndexedArgumentNode;
import edu.uci.python.nodes.argument.ReadKeywordNode;
import edu.uci.python.nodes.argument.ReadVarArgsNode;
import edu.uci.python.nodes.argument.ReadVarKeywordsNode;
import edu.uci.python.nodes.call.PythonCallNode;
import edu.uci.python.nodes.control.BlockNode;
import edu.uci.python.nodes.control.BreakNode;
import edu.uci.python.nodes.control.BreakTargetNode;
import edu.uci.python.nodes.control.ContinueNode;
import edu.uci.python.nodes.control.ContinueTargetNode;
import edu.uci.python.nodes.control.ElseNode;
import edu.uci.python.nodes.control.ForNode;
import edu.uci.python.nodes.control.GetIteratorNode;
import edu.uci.python.nodes.control.IfNode;
import edu.uci.python.nodes.control.ReturnNode.FrameReturnNode;
import edu.uci.python.nodes.control.ReturnTargetNode;
import edu.uci.python.nodes.control.StopIterationTargetNode;
import edu.uci.python.nodes.control.WhileNode;
import edu.uci.python.nodes.expression.AndNode;
import edu.uci.python.nodes.expression.BinaryArithmeticNode;
import edu.uci.python.nodes.expression.BinaryBitwiseNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode;
import edu.uci.python.nodes.expression.CastToBooleanNode;
import edu.uci.python.nodes.expression.OrNode;
import edu.uci.python.nodes.expression.UnaryArithmeticNode;
import edu.uci.python.nodes.frame.FrameSlotNode;
import edu.uci.python.nodes.frame.ReadGlobalNode;
import edu.uci.python.nodes.frame.ReadLevelVariableNode;
import edu.uci.python.nodes.frame.ReadLocalVariableNode;
import edu.uci.python.nodes.frame.ReadVariableNode;
import edu.uci.python.nodes.frame.WriteLocalVariableNode;
import edu.uci.python.nodes.function.PythonBuiltinNode;
import edu.uci.python.nodes.generator.ComprehensionNode;
import edu.uci.python.nodes.generator.FrameSwappingNode;
import edu.uci.python.nodes.generator.FrameTransferNode;
import edu.uci.python.nodes.generator.GeneratorForNode;
import edu.uci.python.nodes.generator.GeneratorReturnTargetNode;
import edu.uci.python.nodes.generator.GeneratorWhileNode;
import edu.uci.python.nodes.generator.ListAppendNode;
import edu.uci.python.nodes.generator.ReadGeneratorFrameVariableNode;
import edu.uci.python.nodes.generator.WriteGeneratorFrameVariableNode;
import edu.uci.python.nodes.generator.YieldNode;
import edu.uci.python.nodes.literal.BigIntegerLiteralNode;
import edu.uci.python.nodes.literal.BooleanLiteralNode;
import edu.uci.python.nodes.literal.ComplexLiteralNode;
import edu.uci.python.nodes.literal.DictLiteralNode;
import edu.uci.python.nodes.literal.DoubleLiteralNode;
import edu.uci.python.nodes.literal.IntegerLiteralNode;
import edu.uci.python.nodes.literal.KeywordLiteralNode;
import edu.uci.python.nodes.literal.ListLiteralNode;
import edu.uci.python.nodes.literal.ObjectLiteralNode;
import edu.uci.python.nodes.literal.SetLiteralNode;
import edu.uci.python.nodes.literal.StringLiteralNode;
import edu.uci.python.nodes.literal.TupleLiteralNode;
import edu.uci.python.nodes.object.GetAttributeNode;
import edu.uci.python.nodes.object.SetAttributeNode;
import edu.uci.python.nodes.optimize.PeeledGeneratorLoopNode;
import edu.uci.python.nodes.statement.AssertNode;
import edu.uci.python.nodes.statement.ClassDefinitionNode;
import edu.uci.python.nodes.statement.DefaultParametersNode;
import edu.uci.python.nodes.statement.ExceptNode;
import edu.uci.python.nodes.statement.ImportFromNode;
import edu.uci.python.nodes.statement.ImportNode;
import edu.uci.python.nodes.statement.ImportStarNode;
import edu.uci.python.nodes.statement.PrintNode;
import edu.uci.python.nodes.statement.RaiseNode;
import edu.uci.python.nodes.statement.TryExceptNode;
import edu.uci.python.nodes.statement.TryFinallyNode;
import edu.uci.python.nodes.statement.WithNode;
import edu.uci.python.nodes.subscript.IndexNode;
import edu.uci.python.nodes.subscript.SliceNode;
import edu.uci.python.nodes.subscript.SubscriptLoadIndexNode;
import edu.uci.python.nodes.subscript.SubscriptLoadSliceNode;
import edu.uci.python.nodes.subscript.SubscriptStoreIndexNode;
import edu.uci.python.nodes.subscript.SubscriptStoreSliceNode;

public interface VisitorIF<T> {

    // @formatter:off
    /*- control */
    public T visitBlockNode(BlockNode node) throws Exception;

    public T visitContinueTargetNode(ContinueTargetNode node) throws Exception;

    public T visitWhileNode(WhileNode node) throws Exception;

    public T visitGetIteratorNode(GetIteratorNode node) throws Exception;

    public T visitReturnTargetNode(ReturnTargetNode node) throws Exception;

    public T visitFrameReturnNode(FrameReturnNode node) throws Exception;

    public T visitStopIterationTargetNode(StopIterationTargetNode node) throws Exception;

    public T visitBreakNode(BreakNode node) throws Exception;

    public T visitBreakTargetNode(BreakTargetNode node) throws Exception;

    public T visitForNode(ForNode node) throws Exception;

    public T visitElseNode(ElseNode node) throws Exception;

    public T visitContinueNode(ContinueNode node) throws Exception;

    public T visitIfNode(IfNode node) throws Exception;

    /*- function */

    public T visitPythonBuiltinNode(PythonBuiltinNode node) throws Exception;

    /*- expression */
    public T visitBinaryBitwiseNode(BinaryBitwiseNode node) throws Exception; //

    public T visitCastToBooleanNode(CastToBooleanNode node) throws Exception; //

    public T visitOrNode(OrNode node) throws Exception;

    public T visitBinaryComparisonNode(BinaryComparisonNode node) throws Exception; //

    public T visitUnaryArithmeticNode(UnaryArithmeticNode node) throws Exception; //

    public T visitBinaryArithmeticNode(BinaryArithmeticNode node) throws Exception; //

    public T visitAndNode(AndNode node) throws Exception; //

    /*- generator */
    public T visitReadGeneratorFrameVariableNode(ReadGeneratorFrameVariableNode node) throws Exception;

    public T visitFrameSwappingNode(FrameSwappingNode node) throws Exception;

//    public T visitGeneratorContinueNode(GeneratorContinueNode node) throws Exception;

    public T visitComprehensionNode(ComprehensionNode node) throws Exception;

    public T visitGeneratorReturnTargetNode(GeneratorReturnTargetNode node) throws Exception;

    public T visitGeneratorWhileNode(GeneratorWhileNode node) throws Exception;

    public T visitListAppendNode(ListAppendNode node) throws Exception;

    public T visitWriteGeneratorFrameVariableNode(WriteGeneratorFrameVariableNode node) throws Exception;

    public T visitGeneratorForNode(GeneratorForNode node) throws Exception;

    public T visitFrameTransferNode(FrameTransferNode node) throws Exception;

    public T visitYieldNode(YieldNode node) throws Exception;

    /*- literal */
    public T visitIntegerLiteralNode(IntegerLiteralNode node) throws Exception; //

    public T visitListLiteralNode(ListLiteralNode node) throws Exception;

    public T visitComplexLiteralNode(ComplexLiteralNode node) throws Exception; //

    public T visitDictLiteralNode(DictLiteralNode node) throws Exception;

    public T visitTupleLiteralNode(TupleLiteralNode node) throws Exception;

    public T visitDoubleLiteralNode(DoubleLiteralNode node) throws Exception; //

    public T visitSetLiteralNode(SetLiteralNode node) throws Exception;

    public T visitObjectLiteralNode(ObjectLiteralNode node) throws Exception;

    public T visitStringLiteralNode(StringLiteralNode node) throws Exception;

    public T visitBigIntegerLiteralNode(BigIntegerLiteralNode node) throws Exception;

    public T visitBooleanLiteralNode(BooleanLiteralNode node) throws Exception;

    public T visitKeywordLiteralNode(KeywordLiteralNode node) throws Exception;

    /*- statement */
    public T visitTryFinallyNode(TryFinallyNode node) throws Exception;

    public T visitRaiseNode(RaiseNode node) throws Exception;

    public T visitWithNode(WithNode node) throws Exception;

    public T visitPrintNode(PrintNode node) throws Exception;

    public T visitAssertNode(AssertNode node) throws Exception;

    public T visitTryExceptNode(TryExceptNode node) throws Exception;

    public T visitExceptNode(ExceptNode node) throws Exception;

    public T visitClassDefinitionNode(ClassDefinitionNode node) throws Exception;

    public T visitImportStarNode(ImportStarNode node) throws Exception;

    public T visitDefaultParametersNode(DefaultParametersNode node) throws Exception;

    public T visitImportFromNode(ImportFromNode node) throws Exception;

    public T visitImportNode(ImportNode node) throws Exception;

    /*- frame */
    public T visitReadLocalVariableNode(ReadLocalVariableNode node) throws Exception;

    public T visitWriteLocalVariableNode(WriteLocalVariableNode node) throws Exception;

    public T visitReadLevelVariableNode(ReadLevelVariableNode node) throws Exception;

    public T visitReadVariableNode(ReadVariableNode node) throws Exception;

    public T visitReadGlobalNode(ReadGlobalNode node) throws Exception;

    public T visitFrameSlotNode(FrameSlotNode node) throws Exception;

    /*- call */
    public T visitPythonCallNode(PythonCallNode node) throws Exception;

    /*- argument */
    public T visitReadVarKeywordsNode(ReadVarKeywordsNode node) throws Exception;

    public T visitArgumentsNode(ArgumentsNode node) throws Exception;

    public T visitReadKeywordNode(ReadKeywordNode node) throws Exception;

    public T visitReadVarArgsNode(ReadVarArgsNode node) throws Exception;

    public T visitReadIndexedArgumentNode(ReadIndexedArgumentNode node) throws Exception;

    public T visitReadDefaultArgumentNode(ReadDefaultArgumentNode node) throws Exception;

    /*- subscript */

    public T visitIndexNode(IndexNode node) throws Exception;

    public T visitSubscriptLoadIndexNode(SubscriptLoadIndexNode node) throws Exception; //

    public T visitSubscriptStoreIndexNode(SubscriptStoreIndexNode node) throws Exception; //

    public T visitSubscriptStoreSliceNode(SubscriptStoreSliceNode node) throws Exception;

    public T visitSliceNode(SliceNode node) throws Exception;

    public T visitSubscriptLoadSliceNode(SubscriptLoadSliceNode node) throws Exception;

    /*- object */
    public T visitSetAttributeNode(SetAttributeNode node) throws Exception;

    public T visitGetAttributeNode(GetAttributeNode node) throws Exception;

    /*- Other */
    public T visitPeeledGeneratorLoopNode(PeeledGeneratorLoopNode node) throws Exception;

    public T visitEmptyNode(EmptyNode node) throws Exception;

    public T visitNoneNode(NoneNode node) throws Exception;

    // @formatter:on

}
