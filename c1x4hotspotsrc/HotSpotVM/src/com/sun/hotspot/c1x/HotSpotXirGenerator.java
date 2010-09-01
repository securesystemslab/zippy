/*
 * Copyright (c) 2009-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * Sun Microsystems, Inc. has intellectual property rights relating to technology embodied in the product that is
 * described in this document. In particular, and without limitation, these intellectual property rights may include one
 * or more of the U.S. patents listed at http://www.sun.com/patents and one or more additional patents or pending patent
 * applications in the U.S. and in other countries.
 *
 * U.S. Government Rights - Commercial software. Government users are subject to the Sun Microsystems, Inc. standard
 * license agreement and applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms. Sun, Sun Microsystems, the Sun logo, Java and Solaris are trademarks or registered
 * trademarks of Sun Microsystems, Inc. in the U.S. and other countries. All SPARC trademarks are used under license and
 * are trademarks or registered trademarks of SPARC International, Inc. in the U.S. and other countries.
 *
 * UNIX is a registered trademark in the U.S. and other countries, exclusively licensed through X/Open Company, Ltd.
 */
package com.sun.hotspot.c1x;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import com.sun.c1x.target.amd64.*;
import com.sun.cri.ci.CiAddress.Scale;
import com.sun.cri.ci.*;
import com.sun.cri.ri.*;
import com.sun.cri.ri.RiType.Representation;
import com.sun.cri.xir.*;
import com.sun.cri.xir.CiXirAssembler.XirOperand;
import com.sun.cri.xir.CiXirAssembler.*;

/**
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class HotSpotXirGenerator implements RiXirGenerator {

    // this needs to correspond to c1x_CodeInstaller.hpp
    // @formatter:off
    private static final Integer MARK_VERIFIED_ENTRY            = 0x0001;
    private static final Integer MARK_UNVERIFIED_ENTRY          = 0x0002;
    private static final Integer MARK_OSR_ENTRY                 = 0x0003;
    private static final Integer MARK_UNWIND_ENTRY              = 0x0004;
    private static final Integer MARK_EXCEPTION_HANDLER_ENTRY   = 0x0005;

    private static final Integer MARK_STATIC_CALL_STUB          = 0x1000;

    private static final Integer MARK_INVOKE_INVALID            = 0x2000;
    private static final Integer MARK_INVOKEINTERFACE           = 0x2001;
    private static final Integer MARK_INVOKESTATIC              = 0x2002;
    private static final Integer MARK_INVOKESPECIAL             = 0x2003;
    private static final Integer MARK_INVOKEVIRTUAL             = 0x2004;

    private static final Integer MARK_IMPLICIT_NULL             = 0x3000;

    private static final Integer MARK_KLASS_PATCHING            = 0x4000;
    private static final Integer MARK_DUMMY_OOP_RELOCATION      = 0x4001;
    // @formatter:on

    private final HotSpotVMConfig config;
    private final CiTarget target;
    private final RiRegisterConfig registerConfig;

    private CiXirAssembler asm;

    private XirTemplate[] emptyTemplates = new XirTemplate[CiKind.values().length];
    private XirTemplate[] arrayLoadTemplates = new XirTemplate[CiKind.values().length];
    private XirTemplate[] arrayStoreTemplates = new XirTemplate[CiKind.values().length];
    private XirTemplate[] arrayLoadTemplatesWithLength = new XirTemplate[CiKind.values().length];
    private XirTemplate[] arrayStoreTemplatesWithLength = new XirTemplate[CiKind.values().length];
    private XirTemplate prologueTemplate;
    private XirTemplate staticPrologueTemplate;
    private XirTemplate epilogueTemplate;
    private XirTemplate arrayLengthTemplate;
    private XirTemplate safepointTemplate;
    private XirTemplate exceptionObjectTemplate;
    private XirTemplate invokeStaticTemplate;
    private XirTemplate invokeSpecialTemplate;
    private XirTemplate invokeInterfaceTemplate;
    private XirTemplate invokeVirtualTemplate;

    private XirTemplate newInstanceUnresolvedTemplate;
    private XirPair newObjectArrayTemplate;
    private XirTemplate newTypeArrayTemplate;
    private XirPair resolveClassTemplate;
    private XirPair checkCastTemplate;

    static class XirPair {

        final XirTemplate resolved;
        final XirTemplate unresolved;

        XirPair(XirTemplate resolved, XirTemplate unresolved) {
            this.resolved = resolved;
            this.unresolved = unresolved;
        }
    }

    private XirPair[] putFieldTemplates = new XirPair[CiKind.values().length];
    private XirPair[] getFieldTemplates = new XirPair[CiKind.values().length];
    private XirPair[] putStaticTemplates = new XirPair[CiKind.values().length];
    private XirPair[] getStaticTemplates = new XirPair[CiKind.values().length];
    private XirPair instanceofTemplate;
    private XirPair instanceofTemplateNonnull;

    public HotSpotXirGenerator(HotSpotVMConfig config, CiTarget target, RiRegisterConfig registerConfig) {
        this.config = config;
        this.target = target;
        this.registerConfig = registerConfig;
    }

    @Override
    public List<XirTemplate> buildTemplates(CiXirAssembler asm) {
        this.asm = asm;

        List<XirTemplate> templates = new ArrayList<XirTemplate>();
        for (CiKind kind : CiKind.JAVA_VALUES) {
            int index = kind.ordinal();

            if (kind == CiKind.Void) {
                asm.restart(CiKind.values()[index]);
                emptyTemplates[index] = asm.finishTemplate("empty-" + kind);
            } else {
                asm.restart();
                XirOperand result = asm.createTemp("result", kind);
                emptyTemplates[index] = asm.finishTemplate(result, "empty-" + kind);

                putFieldTemplates[index] = buildPutFieldTemplate(kind, kind == CiKind.Object);
                getFieldTemplates[index] = buildGetFieldTemplate(kind);
                putStaticTemplates[index] = buildPutStaticTemplate(kind, kind == CiKind.Object);
                getStaticTemplates[index] = buildGetStaticTemplate(kind);
                arrayLoadTemplates[index] = buildArrayLoad(kind, asm, true, false);
                arrayStoreTemplates[index] = buildArrayStore(kind, asm, true, kind == CiKind.Object, kind == CiKind.Object, false);
                arrayLoadTemplatesWithLength[index] = buildArrayLoad(kind, asm, true, true);
                arrayStoreTemplatesWithLength[index] = buildArrayStore(kind, asm, true, kind == CiKind.Object, kind == CiKind.Object, true);
                // newArrayTemplates[index] = buildNewArray(kind);
            }
            // templates.add(emptyTemplates[index]);
        }

        asm.restart();
        XirOperand result = asm.createTemp("result", CiKind.Word);
        emptyTemplates[CiKind.Word.ordinal()] = asm.finishTemplate(result, "empty-Word");

        prologueTemplate = buildPrologue(false);
        staticPrologueTemplate = buildPrologue(true);
        epilogueTemplate = buildEpilogue();
        arrayLengthTemplate = buildArrayLength();
        exceptionObjectTemplate = buildExceptionObject();
        safepointTemplate = buildSafepoint();
        instanceofTemplate = buildInstanceof(false);
        instanceofTemplateNonnull = buildInstanceof(true);
        invokeStaticTemplate = buildInvokeStatic();
        invokeSpecialTemplate = buildInvokeSpecial();
        invokeInterfaceTemplate = buildInvokeInterface();
        invokeVirtualTemplate = buildInvokeVirtual();
        newInstanceUnresolvedTemplate = buildNewInstanceUnresolved();
        newObjectArrayTemplate = new XirPair(buildNewObjectArray(true), buildNewObjectArray(false));
        newTypeArrayTemplate = buildNewTypeArray();
        resolveClassTemplate = new XirPair(buildResolveClass(true), buildResolveClass(false));
        checkCastTemplate = buildCheckCast();

        return templates;
    }

    private final OndemandTemplates<XirTemplate> newInstanceTemplates = new OndemandTemplates<XirTemplate>() {

        @Override
        protected XirTemplate create(CiXirAssembler asm, int size) {
            return buildNewInstance(asm, size);
        }
    };

    private final OndemandTemplates<XirPair> multiNewArrayTemplate = new OndemandTemplates<HotSpotXirGenerator.XirPair>() {

        @Override
        protected XirPair create(CiXirAssembler asm, int dimensions) {
            return new XirPair(buildMultiNewArray(dimensions, true), buildMultiNewArray(dimensions, false));
        }
    };

    private XirTemplate buildPrologue(boolean staticMethod) {
        asm.restart(CiKind.Void);
        XirOperand temp = asm.createRegister("temp (rax)", CiKind.Int, AMD64.rax);
        XirOperand framePointer = asm.createRegister("frame pointer", CiKind.Word, AMD64.rbp);
        XirOperand stackPointer = asm.createRegister("stack pointer", CiKind.Word, AMD64.rsp);

        asm.align(config.codeEntryAlignment);
        asm.mark(MARK_OSR_ENTRY);
        asm.mark(MARK_UNVERIFIED_ENTRY);
        if (!staticMethod) {
            // TODO do some checking...
            asm.add(temp, temp, asm.i(1));
            asm.sub(temp, temp, asm.i(1));
            asm.shouldNotReachHere();

            asm.align(config.codeEntryAlignment);
        }
        asm.mark(MARK_VERIFIED_ENTRY);
        asm.push(framePointer);
        asm.mov(framePointer, stackPointer);
        asm.pushFrame();

        // -- out of line -------------------------------------------------------
        XirOperand thread = asm.createRegister("thread", CiKind.Word, AMD64.r15);
        XirOperand exceptionOop = asm.createTemp("exception oop", CiKind.Object);
        XirLabel unwind = asm.createOutOfLineLabel("unwind");
        asm.bindOutOfLine(unwind);

        asm.mark(MARK_UNWIND_ENTRY);
        //asm.popFrame();
        //asm.pop(framePointer);

        // TODO synchronized methods / monitors

        asm.pload(CiKind.Object, exceptionOop, thread, asm.i(config.threadExceptionOopOffset), false);
        asm.pstore(CiKind.Object, thread, asm.i(config.threadExceptionOopOffset), asm.createConstant(CiConstant.NULL_OBJECT), false);
        asm.pstore(CiKind.Long, thread, asm.i(config.threadExceptionPcOffset), asm.l(0), false);

        asm.callRuntime(config.unwindExceptionStub, null, exceptionOop);
        asm.shouldNotReachHere();

        asm.mark(MARK_EXCEPTION_HANDLER_ENTRY);
        asm.callRuntime(config.handleExceptionStub, null);
        asm.shouldNotReachHere();

        return asm.finishTemplate(staticMethod ? "static prologue" : "prologue");
    }

    private XirTemplate buildEpilogue() {
        asm.restart(CiKind.Void);
        XirOperand framePointer = asm.createRegister("frame pointer", CiKind.Word, AMD64.rbp);

        asm.popFrame();
        asm.pop(framePointer);
        // TODO safepoint check

        return asm.finishTemplate("epilogue");
    }

    private XirTemplate buildArrayLength() {
        XirOperand result = asm.restart(CiKind.Int);
        XirParameter object = asm.createInputParameter("object", CiKind.Object);
        asm.nop(1);
        asm.mark(MARK_IMPLICIT_NULL);
        asm.pload(CiKind.Int, result, object, asm.i(config.arrayLengthOffset), true);
        return asm.finishTemplate("arrayLength");
    }

    private XirTemplate buildExceptionObject() {
        XirOperand result = asm.restart(CiKind.Object);
        XirOperand thread = asm.createRegister("thread", CiKind.Word, AMD64.r15);

        asm.pload(CiKind.Object, result, thread, asm.i(config.threadExceptionOopOffset), false);
        asm.pstore(CiKind.Object, thread, asm.i(config.threadExceptionOopOffset), asm.o(null), false);
        asm.pstore(CiKind.Long, thread, asm.i(config.threadExceptionPcOffset), asm.l(0), false);

        return asm.finishTemplate("exception object");
    }

    private XirTemplate buildSafepoint() {
        asm.restart(CiKind.Void);

        // TODO safepoint

        return asm.finishTemplate("safepoint");
    }

    private XirPair buildGetFieldTemplate(CiKind kind) {
        final XirTemplate resolved;
        final XirTemplate unresolved;
        {
            // resolved case
            XirOperand result = asm.restart(kind);
            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter fieldOffset = asm.createConstantInputParameter("fieldOffset", CiKind.Int);
            asm.nop(1);
            asm.mark(MARK_IMPLICIT_NULL);
            asm.pload(kind, result, object, fieldOffset, true);
            resolved = asm.finishTemplate("getfield<" + kind + ">");
        }
        unresolved = null;
        return new XirPair(resolved, unresolved);
    }

    private XirPair buildPutFieldTemplate(CiKind kind, boolean genWriteBarrier) {
        final XirTemplate resolved;
        final XirTemplate unresolved;
        {
            // resolved case
            asm.restart(CiKind.Void);
            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter value = asm.createInputParameter("value", kind);
            XirParameter fieldOffset = asm.createConstantInputParameter("fieldOffset", CiKind.Int);
            asm.nop(1);
            asm.mark(MARK_IMPLICIT_NULL);
            asm.pstore(kind, object, fieldOffset, value, true);
            if (genWriteBarrier) {
                // TODO write barrier
                // addWriteBarrier(asm, object, value);
            }
            resolved = asm.finishTemplate("putfield<" + kind + ", " + genWriteBarrier + ">");
        }
        unresolved = null;
        return new XirPair(resolved, unresolved);
    }

    private XirPair buildGetStaticTemplate(CiKind kind) {
        final XirTemplate resolved;
        final XirTemplate unresolved;
        {
            // resolved case
            XirOperand result = asm.restart(kind);
            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter fieldOffset = asm.createConstantInputParameter("fieldOffset", CiKind.Int);
            asm.nop(1);
            asm.mark(MARK_IMPLICIT_NULL);
            asm.pload(kind, result, object, fieldOffset, true);
            resolved = asm.finishTemplate("getfield<" + kind + ">");
        }
        unresolved = null;
        return new XirPair(resolved, unresolved);
    }

    private XirPair buildPutStaticTemplate(CiKind kind, boolean genWriteBarrier) {
        final XirTemplate resolved;
        final XirTemplate unresolved;
        {
            // resolved case
            asm.restart(CiKind.Void);
            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter value = asm.createInputParameter("value", kind);
            XirParameter fieldOffset = asm.createConstantInputParameter("fieldOffset", CiKind.Int);
            asm.nop(1);
            asm.mark(MARK_IMPLICIT_NULL);
            asm.pstore(kind, object, fieldOffset, value, true);
            if (genWriteBarrier) {
                // TODO write barrier
                // addWriteBarrier(asm, object, value);
            }
            resolved = asm.finishTemplate("putfield<" + kind + ", " + genWriteBarrier + ">");
        }
        unresolved = null;
        return new XirPair(resolved, unresolved);
    }

    private XirPair buildInstanceof(boolean nonnull) {
        final XirTemplate resolved;
        final XirTemplate unresolved;
        {
            XirOperand result = asm.restart(CiKind.Boolean);

            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter hub = asm.createConstantInputParameter("hub", CiKind.Object);
            XirOperand objHub = asm.createTemp("objHub", CiKind.Object);

            XirLabel end = asm.createInlineLabel("end");
            XirLabel slow_path = asm.createOutOfLineLabel("slow path");

            if (!nonnull) {
                // null isn't "instanceof" anything
                asm.mov(result, asm.b(false));
                asm.jeq(end, object, asm.o(null));
            }
            asm.pload(CiKind.Object, objHub, object, asm.i(config.hubOffset), false);
            // if we get an exact match: succeed immediately
            asm.mov(result, asm.b(true));
            asm.jneq(slow_path, objHub, hub);
            asm.bindInline(end);

            // -- out of line -------------------------------------------------------
            asm.bindOutOfLine(slow_path);
            checkSubtype(result, objHub, hub);
            asm.jmp(end);

            resolved = asm.finishTemplate("instanceof-leaf<" + nonnull + ">");
        }
        {
            XirOperand result = asm.restart(CiKind.Boolean);

            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirOperand hub = asm.createTemp("hub", CiKind.Object);
            XirOperand objHub = asm.createTemp("objHub", CiKind.Object);

            XirLabel end = asm.createInlineLabel("end");
            XirLabel slow_path = asm.createOutOfLineLabel("slow path");

            // insert the patching code for class resolving - the hub will end up in "hub"
            UnresolvedClassPatching patching = new UnresolvedClassPatching(asm, hub, config);
            patching.emitInline();

            if (!nonnull) {
                // null isn't "instanceof" anything
                asm.mov(result, asm.b(false));
                asm.jeq(end, object, asm.o(null));
            }
            asm.pload(CiKind.Object, objHub, object, asm.i(config.hubOffset), false);
            // if we get an exact match: succeed immediately
            asm.mov(result, asm.b(true));
            asm.jneq(slow_path, objHub, hub);
            asm.bindInline(end);

            // -- out of line -------------------------------------------------------
            asm.bindOutOfLine(slow_path);
            checkSubtype(result, objHub, hub);
            asm.jmp(end);

            patching.emitOutOfLine();

            unresolved = asm.finishTemplate("instanceof-leaf<" + nonnull + ">");
        }
        return new XirPair(resolved, unresolved);
    }

    private XirPair buildCheckCast() {
        final XirTemplate resolved;
        final XirTemplate unresolved;
        {
            asm.restart();

            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter hub = asm.createConstantInputParameter("hub", CiKind.Object);
            XirOperand objHub = asm.createTemp("objHub", CiKind.Object);

            XirLabel end = asm.createInlineLabel("end");
            XirLabel slow_path = asm.createOutOfLineLabel("slow path");

            // null can be cast to anything
            asm.jeq(end, object, asm.o(null));

            asm.pload(CiKind.Object, objHub, object, asm.i(config.hubOffset), false);
            // if we get an exact match: succeed immediately
            asm.jneq(slow_path, objHub, hub);
            asm.bindInline(end);

            // -- out of line -------------------------------------------------------
            asm.bindOutOfLine(slow_path);
            checkSubtype(objHub, objHub, hub);
            asm.jneq(end, objHub, asm.o(null));
            XirOperand scratch = asm.createRegister("scratch", CiKind.Object, AMD64.r10);
            asm.mov(scratch, object);
            asm.callRuntime(config.throwClassCastException, null);
            asm.shouldNotReachHere();

            resolved = asm.finishTemplate(object, "check cast");
        }
        {
            asm.restart();

            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirOperand hub = asm.createTemp("hub", CiKind.Object);
            XirOperand objHub = asm.createTemp("objHub", CiKind.Object);

            XirLabel end = asm.createInlineLabel("end");
            XirLabel slow_path = asm.createOutOfLineLabel("slow path");

            // insert the patching code for class resolving - the hub will end up in "hub"
            UnresolvedClassPatching patching = new UnresolvedClassPatching(asm, hub, config);
            patching.emitInline();

            // null can be cast to anything
            asm.jeq(end, object, asm.o(null));

            asm.pload(CiKind.Object, objHub, object, asm.i(config.hubOffset), false);
            // if we get an exact match: succeed immediately
            asm.jneq(slow_path, objHub, hub);
            asm.bindInline(end);

            // -- out of line -------------------------------------------------------
            asm.bindOutOfLine(slow_path);
            checkSubtype(objHub, objHub, hub);
            asm.jneq(end, objHub, asm.o(null));
            XirOperand scratch = asm.createRegister("scratch", CiKind.Object, AMD64.r10);
            asm.mov(scratch, object);
            asm.callRuntime(config.throwClassCastException, null);
            asm.shouldNotReachHere();

            patching.emitOutOfLine();

            unresolved = asm.finishTemplate(object, "check cast");
        }
        return new XirPair(resolved, unresolved);
    }

    private XirTemplate buildArrayStore(CiKind kind, CiXirAssembler asm, boolean genBoundsCheck, boolean genStoreCheck, boolean genWriteBarrier, boolean withLength) {
        asm.restart(CiKind.Void);
        XirParameter array = asm.createInputParameter("array", CiKind.Object);
        XirParameter index = asm.createInputParameter("index", CiKind.Int);
        XirParameter value = asm.createInputParameter("value", kind);
        XirOperand temp = asm.createTemp("temp", CiKind.Word);
        XirOperand valueHub = null;
        XirOperand compHub = null;
        XirLabel store = asm.createInlineLabel("store");
        XirLabel failBoundsCheck = null;
        XirLabel slowStoreCheck = null;
        // if the length is known the array cannot be null
        boolean implicitNullException = !withLength;

        if (genBoundsCheck) {
            // load the array length and check the index
            failBoundsCheck = asm.createOutOfLineLabel("failBoundsCheck");
            XirOperand length;
            if (withLength) {
                length = asm.createInputParameter("length", CiKind.Int);
            } else {
                length = asm.createTemp("length", CiKind.Int);
                asm.nop(1);
                asm.mark(MARK_IMPLICIT_NULL);
                asm.pload(CiKind.Int, length, array, asm.i(config.arrayLengthOffset), true);
                implicitNullException = false;
            }
            asm.jugteq(failBoundsCheck, index, length);

        }
        if (genStoreCheck) {
            slowStoreCheck = asm.createOutOfLineLabel("slowStoreCheck");
            asm.jeq(store, value, asm.o(null)); // first check if value is null
            valueHub = asm.createTemp("valueHub", CiKind.Object);
            compHub = asm.createTemp("compHub", CiKind.Object);
            if (implicitNullException) {
                asm.mark(MARK_IMPLICIT_NULL);
            }
            asm.pload(CiKind.Object, compHub, array, asm.i(config.hubOffset), implicitNullException);
            asm.pload(CiKind.Object, compHub, compHub, asm.i(config.arrayClassElementOffset), false);
            asm.pload(CiKind.Object, valueHub, value, asm.i(config.hubOffset), false);
            asm.jneq(slowStoreCheck, compHub, valueHub); // then check component hub matches value hub

            implicitNullException = false;
        }
        asm.bindInline(store);
        int elemSize = target.sizeInBytes(kind);

        if (implicitNullException) {
            asm.mark(MARK_IMPLICIT_NULL);
        }
        asm.pstore(kind, array, index, value, config.getArrayOffset(kind), Scale.fromInt(elemSize), implicitNullException);
        if (genWriteBarrier) {
            // addWriteBarrier(asm, array, value);
        }

        // -- out of line -------------------------------------------------------
        if (genBoundsCheck) {
            asm.bindOutOfLine(failBoundsCheck);
            asm.callRuntime(config.throwArrayIndexException, null);
            asm.shouldNotReachHere();
        }
        if (genStoreCheck) {
            asm.bindOutOfLine(slowStoreCheck);
            checkSubtype(temp, valueHub, compHub);
            asm.jneq(store, temp, asm.w(0));
            asm.callRuntime(config.throwArrayStoreException, null);
            asm.jmp(store);
        }
        return asm.finishTemplate("arraystore<" + kind + ">");
    }

    private XirTemplate buildArrayLoad(CiKind kind, CiXirAssembler asm, boolean genBoundsCheck, boolean withLength) {
        XirOperand result = asm.restart(kind);
        XirParameter array = asm.createInputParameter("array", CiKind.Object);
        XirParameter index = asm.createInputParameter("index", CiKind.Int);
        XirLabel failBoundsCheck = null;
        // if the length is known the array cannot be null
        boolean implicitNullException = !withLength;

        if (genBoundsCheck) {
            // load the array length and check the index
            failBoundsCheck = asm.createOutOfLineLabel("failBoundsCheck");
            XirOperand length;
            if (withLength) {
                length = asm.createInputParameter("length", CiKind.Int);
            } else {
                length = asm.createTemp("length", CiKind.Int);
                asm.nop(1);
                asm.mark(MARK_IMPLICIT_NULL);
                asm.pload(CiKind.Int, length, array, asm.i(config.arrayLengthOffset), true);
                implicitNullException = false;
            }
            asm.jugteq(failBoundsCheck, index, length);
            implicitNullException = false;
        }
        int elemSize = target.sizeInBytes(kind);
        if (implicitNullException) {
            asm.nop(1);
            asm.mark(MARK_IMPLICIT_NULL);
        }
        asm.pload(kind, result, array, index, config.getArrayOffset(kind), Scale.fromInt(elemSize), implicitNullException);
        if (genBoundsCheck) {
            asm.bindOutOfLine(failBoundsCheck);
            asm.callRuntime(config.throwArrayIndexException, null);
            asm.shouldNotReachHere();
        }
        return asm.finishTemplate("arrayload<" + kind + ">");
    }

    private XirTemplate buildInvokeStatic() {
        asm.restart();
        XirParameter addr = asm.createConstantInputParameter("addr", CiKind.Word);

        XirLabel stub = asm.createOutOfLineLabel("call stub");
        asm.mark(MARK_INVOKESTATIC);

        // -- out of line -------------------------------------------------------
        asm.bindOutOfLine(stub);
        XirOperand method = asm.createRegister("method", CiKind.Word, AMD64.rbx);
        asm.mark(MARK_STATIC_CALL_STUB, XirMark.CALLSITE);
        asm.mov(method, asm.w(0l));
        XirLabel dummy = asm.createOutOfLineLabel("dummy");
        asm.jmp(dummy);
        asm.bindOutOfLine(dummy);

        return asm.finishTemplate(addr, "invokestatic");
    }

    private XirTemplate buildInvokeSpecial() {
        asm.restart();
        XirParameter receiver = asm.createInputParameter("receiver", CiKind.Object);
        XirParameter addr = asm.createConstantInputParameter("addr", CiKind.Word);
        XirOperand temp = asm.createRegister("temp", CiKind.Word, AMD64.rax);
        XirLabel stub = asm.createOutOfLineLabel("call stub");

        asm.nop(1);
        asm.mark(MARK_IMPLICIT_NULL);
        asm.pload(CiKind.Word, temp, receiver, true);
        asm.mark(MARK_INVOKESPECIAL);

        // -- out of line -------------------------------------------------------
        asm.bindOutOfLine(stub);
        XirOperand method = asm.createRegister("method", CiKind.Word, AMD64.rbx);
        asm.mark(MARK_STATIC_CALL_STUB, XirMark.CALLSITE);
        asm.mov(method, asm.w(0l));
        XirLabel dummy = asm.createOutOfLineLabel("dummy");
        asm.jmp(dummy);
        asm.bindOutOfLine(dummy);

        return asm.finishTemplate(addr, "invokespecial");
    }

    private XirTemplate buildInvokeInterface() {
        asm.restart();
        XirParameter receiver = asm.createInputParameter("receiver", CiKind.Object);
        XirParameter addr = asm.createConstantInputParameter("addr", CiKind.Word);
        XirOperand method = asm.createRegister("method", CiKind.Object, AMD64.rbx);
        XirOperand temp = asm.createRegister("temp", CiKind.Word, AMD64.rax);
        XirLabel stub = asm.createOutOfLineLabel("call stub");

        asm.nop(1);
        asm.mark(MARK_IMPLICIT_NULL);
        asm.pload(CiKind.Word, temp, receiver, true);
        asm.mark(MARK_INVOKEINTERFACE);
        asm.mov(method, asm.createConstant(CiConstant.forObject(HotSpotProxy.DUMMY_CONSTANT_OBJ)));

        // -- out of line -------------------------------------------------------
        asm.bindOutOfLine(stub);
        asm.mark(MARK_STATIC_CALL_STUB, XirMark.CALLSITE);
        asm.mov(method, asm.w(0l));
        XirLabel dummy = asm.createOutOfLineLabel("dummy");
        asm.jmp(dummy);
        asm.bindOutOfLine(dummy);

        return asm.finishTemplate(addr, "invokespecial");
    }

    private XirTemplate buildInvokeVirtual() {
        asm.restart();
        XirParameter receiver = asm.createInputParameter("receiver", CiKind.Object);
        XirParameter addr = asm.createConstantInputParameter("addr", CiKind.Word);
        XirOperand method = asm.createRegister("method", CiKind.Object, AMD64.rbx);
        XirOperand temp = asm.createRegister("temp", CiKind.Word, AMD64.rax);
        XirLabel stub = asm.createOutOfLineLabel("call stub");

        asm.nop(1);
        asm.mark(MARK_IMPLICIT_NULL);
        asm.pload(CiKind.Word, temp, receiver, true);
        asm.mark(MARK_INVOKEVIRTUAL);
        asm.mov(method, asm.createConstant(CiConstant.forObject(HotSpotProxy.DUMMY_CONSTANT_OBJ)));

        // -- out of line -------------------------------------------------------
        asm.bindOutOfLine(stub);
        asm.mark(MARK_STATIC_CALL_STUB, XirMark.CALLSITE);
        asm.mov(method, asm.w(0l));
        XirLabel dummy = asm.createOutOfLineLabel("dummy");
        asm.jmp(dummy);
        asm.bindOutOfLine(dummy);

        return asm.finishTemplate(addr, "invokespecial");
    }

    private XirTemplate buildNewInstance(CiXirAssembler asm, int size) {
        XirOperand result = asm.restart(CiKind.Word);
        XirOperand type = asm.createInputParameter("type", CiKind.Object);

        XirOperand thread = asm.createRegister("thread", CiKind.Word, AMD64.r15);
        XirOperand temp1 = asm.createRegister("temp1", CiKind.Word, AMD64.rcx);
        XirOperand temp2 = asm.createRegister("temp2", CiKind.Word, AMD64.rbx);
        useRegisters(asm, AMD64.rsi);
        XirLabel tlabFull = asm.createOutOfLineLabel("tlab full");
        XirLabel resume = asm.createInlineLabel("resume");

        asm.pload(CiKind.Word, result, thread, asm.i(config.threadTlabTopOffset), false);
        asm.add(temp1, result, asm.w(size));
        asm.pload(CiKind.Word, temp2, thread, asm.i(config.threadTlabEndOffset), false);

        asm.jgt(tlabFull, temp1, temp2);
        asm.pstore(CiKind.Word, thread, asm.i(config.threadTlabTopOffset), temp1, false);
        asm.bindInline(resume);

        asm.pload(CiKind.Word, temp1, type, asm.i(config.instanceHeaderPrototypeOffset), false);
        asm.pstore(CiKind.Word, result, temp1, false);
        asm.pstore(CiKind.Object, result, asm.i(config.hubOffset), type, false);

        if (size > 2 * target.wordSize) {
            asm.mov(temp1, asm.w(0));
            for (int offset = 2 * target.wordSize; offset < size; offset += target.wordSize) {
                asm.pstore(CiKind.Word, result, asm.i(offset), temp1, false);
            }
        }

        // -- out of line -------------------------------------------------------
        asm.bindOutOfLine(tlabFull);
        XirOperand arg = asm.createRegister("runtime call argument", CiKind.Object, AMD64.rdx);
        asm.mov(arg, type);
        asm.callRuntime(config.newInstanceStub, result);
        asm.jmp(resume);

        return asm.finishTemplate("new instance");
    }

    private XirTemplate buildNewInstanceUnresolved() {
        XirOperand result = asm.restart(CiKind.Word);
        XirOperand arg = asm.createRegister("runtime call argument", CiKind.Object, AMD64.rdx);

        UnresolvedClassPatching patching = new UnresolvedClassPatching(asm, arg, config);

        patching.emitInline();
        useRegisters(AMD64.rbx, AMD64.rcx, AMD64.rsi);
        asm.callRuntime(config.newInstanceStub, result);

        // -- out of line -------------------------------------------------------
        patching.emitOutOfLine();

        return asm.finishTemplate("new instance");
    }

    private XirTemplate buildNewObjectArray(boolean resolved) {
        XirOperand result = asm.restart(CiKind.Object);

        XirParameter lengthParam = asm.createInputParameter("length", CiKind.Int);

        XirOperand length = asm.createRegister("length", CiKind.Int, AMD64.rbx);
        XirOperand hub = asm.createRegister("hub", CiKind.Object, AMD64.rdx);

        UnresolvedClassPatching patching = null;
        if (resolved) {
            asm.mov(hub, asm.createConstantInputParameter("hub", CiKind.Object));
        } else {
            // insert the patching code for class resolving - the hub will end up in "hub"
            patching = new UnresolvedClassPatching(asm, hub, config);
            patching.emitInline();
        }

        asm.mov(length, lengthParam);
        useRegisters(AMD64.rsi, AMD64.rcx, AMD64.rdi);
        asm.callRuntime(config.newObjectArrayStub, result);
        if (!resolved) {
            patching.emitOutOfLine();
        }
        return asm.finishTemplate(resolved ? "newObjectArray" : "newObjectArray (unresolved)");
    }

    private XirTemplate buildMultiNewArray(int dimensions, boolean resolved) {
        XirOperand result = asm.restart(CiKind.Object);

        XirOperand hub = asm.createRegister("hub", CiKind.Object, AMD64.rax);
        XirOperand rank = asm.createRegister("rank", CiKind.Int, AMD64.rbx);
        XirOperand sizes = asm.createRegister("sizes", CiKind.Long, AMD64.rcx);
        XirOperand thread = asm.createRegister("thread", CiKind.Long, AMD64.r15);
        asm.add(sizes, thread, asm.l(config.threadMultiNewArrayStorage));
        for (int i = 0; i < dimensions; i++) {
            XirParameter length = asm.createInputParameter("length" + i, CiKind.Int);
            asm.pstore(CiKind.Int, sizes, asm.i(i * target.sizeInBytes(CiKind.Int)), length, false);
        }

        UnresolvedClassPatching patching = null;
        if (resolved) {
            asm.mov(hub, asm.createConstantInputParameter("hub", CiKind.Object));
        } else {
            // insert the patching code for class resolving - the hub will end up in "hub"
            patching = new UnresolvedClassPatching(asm, hub, config);
            patching.emitInline();
        }

        asm.mov(rank, asm.i(dimensions));
        asm.callRuntime(config.newMultiArrayStub, result);
        if (!resolved) {
            patching.emitOutOfLine();
        }
        return asm.finishTemplate(resolved ? "multiNewArray" + dimensions : "multiNewArray" + dimensions + " (unresolved)");
    }

    private XirTemplate buildNewTypeArray() {
        XirOperand result = asm.restart(CiKind.Object);

        XirParameter lengthParam = asm.createInputParameter("length", CiKind.Int);
        XirParameter hubParam = asm.createConstantInputParameter("hub", CiKind.Object);

        XirOperand length = asm.createRegister("length", CiKind.Int, AMD64.rbx);
        XirOperand hub = asm.createRegister("hub", CiKind.Object, AMD64.rdx);

        asm.mov(hub, hubParam);
        asm.mov(length, lengthParam);
        useRegisters(AMD64.rsi, AMD64.rcx, AMD64.rdi);
        asm.callRuntime(config.newTypeArrayStub, result);

        return asm.finishTemplate("newObjectArray");
    }

    private XirTemplate buildResolveClass(boolean resolved) {
        XirOperand result = asm.restart(CiKind.Word);
        if (resolved) {
            XirOperand type = asm.createConstantInputParameter("type", CiKind.Object);

            asm.mov(result, type);
        } else {
            UnresolvedClassPatching patching = new UnresolvedClassPatching(asm, result, config);
            patching.emitInline();
            // -- out of line -------------------------------------------------------
            patching.emitOutOfLine();
        }
        return asm.finishTemplate(resolved ? "resolve class" : "resolve class (unresolved)");
    }

    @Override
    public XirSnippet genArrayLength(XirSite site, XirArgument array) {
        return new XirSnippet(arrayLengthTemplate, array);
    }

    @Override
    public XirSnippet genArrayLoad(XirSite site, XirArgument array, XirArgument index, XirArgument length, CiKind elementKind, RiType elementType) {
        if (length == null) {
            return new XirSnippet(arrayLoadTemplates[elementKind.ordinal()], array, index);
        }
        return new XirSnippet(arrayLoadTemplatesWithLength[elementKind.ordinal()], array, index, length);
    }

    @Override
    public XirSnippet genArrayStore(XirSite site, XirArgument array, XirArgument index, XirArgument length, XirArgument value, CiKind elementKind, RiType elementType) {
        if (length == null) {
            return new XirSnippet(arrayStoreTemplates[elementKind.ordinal()], array, index, value);
        }
        return new XirSnippet(arrayStoreTemplatesWithLength[elementKind.ordinal()], array, index, value, length);
    }

    @Override
    public XirSnippet genCheckCast(XirSite site, XirArgument receiver, XirArgument hub, RiType type) {
        if (type.isResolved()) {
            return new XirSnippet(checkCastTemplate.resolved, receiver, hub);
        }
        return new XirSnippet(checkCastTemplate.unresolved, receiver);
    }

    @Override
    public XirSnippet genPrologue(XirSite site, RiMethod method) {
        boolean staticMethod = Modifier.isStatic(method.accessFlags());
        return new XirSnippet(staticMethod ? staticPrologueTemplate : prologueTemplate);
    }

    @Override
    public XirSnippet genEpilogue(XirSite site, RiMethod method) {
        return new XirSnippet(epilogueTemplate);
    }

    @Override
    public XirSnippet genGetField(XirSite site, XirArgument receiver, RiField field) {
        XirPair pair = getFieldTemplates[field.kind().ordinal()];
        if (field.isResolved()) {
            return new XirSnippet(pair.resolved, receiver, XirArgument.forInt(((HotSpotField) field).offset()));
        }
        return new XirSnippet(pair.unresolved, receiver);
    }

    @Override
    public XirSnippet genGetStatic(XirSite site, XirArgument staticTuple, RiField field) {
        XirPair pair = getStaticTemplates[field.kind().ordinal()];
        if (field.isResolved()) {
            return new XirSnippet(pair.resolved, staticTuple, XirArgument.forInt(((HotSpotField) field).offset()));
        }
        return new XirSnippet(pair.unresolved, staticTuple, null);
    }

    @Override
    public XirSnippet genPutField(XirSite site, XirArgument receiver, RiField field, XirArgument value) {
        XirPair pair = putFieldTemplates[field.kind().ordinal()];
        if (field.isResolved()) {
            return new XirSnippet(pair.resolved, receiver, value, XirArgument.forInt(((HotSpotField) field).offset()));
        }
        return new XirSnippet(pair.unresolved, receiver, value);
    }

    @Override
    public XirSnippet genPutStatic(XirSite site, XirArgument staticTuple, RiField field, XirArgument value) {
        XirPair pair = putStaticTemplates[field.kind().ordinal()];
        if (field.isResolved()) {
            return new XirSnippet(pair.resolved, staticTuple, value, XirArgument.forInt(((HotSpotField) field).offset()));
        }
        return new XirSnippet(pair.unresolved, staticTuple, value);
    }

    @Override
    public XirSnippet genInstanceOf(XirSite site, XirArgument receiver, XirArgument hub, RiType type) {
        if (type.isResolved()) {
            return new XirSnippet(instanceofTemplate.resolved, receiver, hub);
        }
        return new XirSnippet(instanceofTemplate.unresolved, receiver);
    }

    @Override
    public XirSnippet genIntrinsic(XirSite site, XirArgument[] arguments, RiMethod method) {
        return null;
    }

    @Override
    public XirSnippet genInvokeInterface(XirSite site, XirArgument receiver, RiMethod method) {
        return new XirSnippet(invokeInterfaceTemplate, receiver, XirArgument.forWord(0));
    }

    @Override
    public XirSnippet genInvokeSpecial(XirSite site, XirArgument receiver, RiMethod method) {
        return new XirSnippet(invokeSpecialTemplate, receiver, XirArgument.forWord(0));
    }

    @Override
    public XirSnippet genInvokeStatic(XirSite site, RiMethod method) {
        return new XirSnippet(invokeStaticTemplate, XirArgument.forWord(0));
    }

    @Override
    public XirSnippet genInvokeVirtual(XirSite site, XirArgument receiver, RiMethod method) {
        return new XirSnippet(invokeVirtualTemplate, receiver, XirArgument.forWord(0));
    }

    @Override
    public XirSnippet genMonitorEnter(XirSite site, XirArgument receiver) {
        return new XirSnippet(emptyTemplates[CiKind.Void.ordinal()]);
    }

    @Override
    public XirSnippet genMonitorExit(XirSite site, XirArgument receiver) {
        return new XirSnippet(emptyTemplates[CiKind.Void.ordinal()]);
    }

    @Override
    public XirSnippet genNewArray(XirSite site, XirArgument length, CiKind elementKind, RiType componentType, RiType arrayType) {
        if (elementKind == CiKind.Object) {
            if (arrayType instanceof HotSpotTypeResolved) {
                return new XirSnippet(newObjectArrayTemplate.resolved, length, XirArgument.forObject(arrayType));
            }
            return new XirSnippet(newObjectArrayTemplate.unresolved, length);
        }
        assert arrayType == null;
        arrayType = Compiler.getVMEntries().getPrimitiveArrayType(elementKind);
        return new XirSnippet(newTypeArrayTemplate, length, XirArgument.forObject(arrayType));
    }

    @Override
    public XirSnippet genNewInstance(XirSite site, RiType type) {
        if (type instanceof HotSpotTypeResolved) {
            int instanceSize = ((HotSpotTypeResolved) type).instanceSize();
            return new XirSnippet(newInstanceTemplates.get(instanceSize), XirArgument.forObject(type));
        }
        return new XirSnippet(newInstanceUnresolvedTemplate);
    }

    @Override
    public XirSnippet genNewMultiArray(XirSite site, XirArgument[] lengths, RiType type) {
        if (type instanceof HotSpotTypeResolved) {
            XirArgument[] params = Arrays.copyOf(lengths, lengths.length + 1);
            params[lengths.length] = XirArgument.forObject(type);
            return new XirSnippet(multiNewArrayTemplate.get(lengths.length).resolved, params);
        }
        return new XirSnippet(multiNewArrayTemplate.get(lengths.length).unresolved, lengths);
    }

    @Override
    public XirSnippet genResolveClass(XirSite site, RiType type, Representation representation) {
        assert representation == Representation.ObjectHub : "unexpected representation: " + representation;
        if (type instanceof HotSpotTypeResolved) {
            return new XirSnippet(resolveClassTemplate.resolved, XirArgument.forObject(type));
        }
        return new XirSnippet(resolveClassTemplate.unresolved);
    }

    @Override
    public XirSnippet genSafepoint(XirSite site) {
        return new XirSnippet(safepointTemplate);
    }

    @Override
    public XirSnippet genExceptionObject(XirSite site) {
        return new XirSnippet(exceptionObjectTemplate);
    }

    private static class UnresolvedClassPatching {

        private final XirLabel patchSite;
        private final XirLabel replacement;
        private final XirLabel patchStub;
        private final CiXirAssembler asm;
        private final HotSpotVMConfig config;
        private final XirOperand arg;
        private State state;

        private enum State {
            New, Inline, Finished
        }

        public UnresolvedClassPatching(CiXirAssembler asm, XirOperand arg, HotSpotVMConfig config) {
            this.asm = asm;
            this.arg = arg;
            this.config = config;
            patchSite = asm.createInlineLabel("patch site");
            replacement = asm.createOutOfLineLabel("replacement");
            patchStub = asm.createOutOfLineLabel("patch stub");

            state = State.New;
        }

        public void emitInline() {
            assert state == State.New;

            asm.bindInline(patchSite);
            asm.mark(MARK_DUMMY_OOP_RELOCATION);
            asm.jmp(patchStub);

            // TODO: make this more generic & safe - this is needed to create space for patching
            asm.nop(5);

            state = State.Inline;
        }

        public void emitOutOfLine() {
            assert state == State.Inline;

            asm.bindOutOfLine(replacement);
            XirMark begin = asm.mark(null);
            asm.mov(arg, asm.createConstant(CiConstant.forObject(null)));
            XirMark end = asm.mark(null);
            // make this piece of data look like an instruction
            asm.rawBytes(new byte[] { (byte) 0xb8, 0, 0, 0x05, 0});
            asm.mark(MARK_KLASS_PATCHING, begin, end);
            asm.bindOutOfLine(patchStub);
            asm.callRuntime(config.loadKlassStub, null);
            asm.jmp(patchSite);

            state = State.Finished;
        }
    }

    private void checkSubtype(XirOperand result, XirOperand objHub, XirOperand hub) {
        asm.push(objHub);
        asm.push(hub);
        asm.callRuntime(config.instanceofStub, null);
        asm.pop(result);
        asm.pop(result);
    }

    private void useRegisters(CiXirAssembler asm, CiRegister... registers) {
        if (registers != null) {
            for (CiRegister register : registers) {
                asm.createRegister("reg", CiKind.Illegal, register);
            }
        }
    }

    private void useRegisters(CiRegister... registers) {
        useRegisters(asm, registers);
    }

    private abstract class OndemandTemplates<T> {

        private ConcurrentHashMap<Integer, T> templates = new ConcurrentHashMap<Integer, T>();

        protected abstract T create(CiXirAssembler asm, int index);

        public T get(int index) {
            T template = templates.get(index);
            if (template == null) {
                template = create(asm.copy(), index);
                templates.put(index, template);
            }
            return template;
        }
    }
}
