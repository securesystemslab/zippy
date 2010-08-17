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

import com.sun.c1x.target.amd64.*;
import com.sun.cri.ci.CiAddress.Scale;
import com.sun.cri.ci.*;
import com.sun.cri.ri.*;
import com.sun.cri.ri.RiType.Representation;
import com.sun.cri.xir.*;
import com.sun.cri.xir.CiXirAssembler.*;

/**
 *
 * @author Thomas Wuerthinger, Lukas Stadler
 */
public class HotSpotXirGenerator implements RiXirGenerator {

    // this needs to correspond to c1x_Compiler.hpp
    private static final Integer MARK_VERIFIED_ENTRY = 1;
    private static final Integer MARK_UNVERIFIED_ENTRY = 2;
    private static final Integer MARK_STATIC_CALL_STUB = 1000;

    private final HotSpotVMConfig config;
    private final CiTarget target;
    private final RiRegisterConfig registerConfig;

    private CiXirAssembler asm;

    private XirTemplate[] emptyTemplates = new XirTemplate[CiKind.values().length];
    private XirTemplate[] arrayLoadTemplates = new XirTemplate[CiKind.values().length];
    private XirTemplate[] arrayStoreTemplates = new XirTemplate[CiKind.values().length];
    private XirTemplate prologueTemplate;
    private XirTemplate staticPrologueTemplate;
    private XirTemplate epilogueTemplate;
    private XirTemplate arrayLengthTemplate;
    private XirTemplate exceptionObjectTemplate;
    private XirTemplate invokeStaticTemplate;

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
    private XirPair[] putStaticFieldTemplates = new XirPair[CiKind.values().length];
    private XirPair[] getStaticFieldTemplates = new XirPair[CiKind.values().length];
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

                putFieldTemplates[index] = buildPutFieldTemplate(kind, kind == CiKind.Object, false);
                getFieldTemplates[index] = buildGetFieldTemplate(kind, false);
                putStaticFieldTemplates[index] = buildPutFieldTemplate(kind, kind == CiKind.Object, true);
                getStaticFieldTemplates[index] = buildGetFieldTemplate(kind, true);
                arrayLoadTemplates[index] = buildArrayLoad(kind, asm, true);
                arrayStoreTemplates[index] = buildArrayStore(kind, asm, true, kind == CiKind.Object, kind == CiKind.Object);
                // newArrayTemplates[index] = buildNewArray(kind);
            }
            // templates.add(emptyTemplates[index]);
        }
        prologueTemplate = buildPrologue(false);
        staticPrologueTemplate = buildPrologue(true);
        epilogueTemplate = buildEpilogue();
        arrayLengthTemplate = buildArrayLength();
        exceptionObjectTemplate = buildExceptionObject();
        instanceofTemplate = buildInstanceof(false);
        instanceofTemplateNonnull = buildInstanceof(true);
        invokeStaticTemplate = buildInvokeStatic();

        return templates;
    }

    private XirTemplate buildPrologue(boolean staticMethod) {
        asm.restart(CiKind.Void);
        XirOperand sp = asm.createRegister("stack pointer", CiKind.Word, registerConfig.getStackPointerRegister());
        XirOperand temp = asm.createRegister("temp (rax)", CiKind.Int, AMD64.rax);
        XirOperand frame_pointer = asm.createRegister("frame pointer", CiKind.Word, AMD64.rbp);

        asm.align(config.codeEntryAlignment);
        asm.mark(MARK_UNVERIFIED_ENTRY);
        if (!staticMethod) {
            // TODO do some checking...
            asm.add(temp, temp, asm.i(1));
            asm.sub(temp, temp, asm.i(1));
            asm.shouldNotReachHere();

            asm.align(config.codeEntryAlignment);
        }
        asm.mark(MARK_VERIFIED_ENTRY);
        asm.push(frame_pointer);
        asm.pushFrame();

        return asm.finishTemplate(staticMethod ? "static prologue" : "prologue");
    }

    private XirTemplate buildEpilogue() {
        asm.restart(CiKind.Void);
        XirOperand frame_pointer = asm.createRegister("frame pointer", CiKind.Word, AMD64.rbp);
        asm.popFrame();
        asm.pop(frame_pointer);
        // TODO safepoint check
        return asm.finishTemplate("epilogue");
    }

    private XirTemplate buildArrayLength() {
        XirOperand result = asm.restart(CiKind.Int);
        XirParameter object = asm.createInputParameter("object", CiKind.Object);
        asm.pload(CiKind.Int, result, object, asm.i(config.arrayLengthOffset), true);
        return asm.finishTemplate("arrayLength");
    }

    private XirTemplate buildExceptionObject() {
        asm.restart();
        XirOperand temp = asm.createRegister("temp (rax)", CiKind.Object, AMD64.rax);
        return asm.finishTemplate(temp, "exception object");
    }

    private XirPair buildGetFieldTemplate(CiKind kind, boolean isStatic) {
        final XirTemplate resolved;
        final XirTemplate unresolved;
        {
            // resolved case
            XirOperand result = asm.restart(kind);
            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter fieldOffset = asm.createConstantInputParameter("fieldOffset", CiKind.Int);
            asm.pload(kind, result, object, fieldOffset, true);
            resolved = asm.finishTemplate("getfield<" + kind + ">");
        }
        if (isStatic) {
            asm.restart(kind);
            asm.shouldNotReachHere();

// XirParameter object = asm.createInputParameter("object", CiKind.Object);
// XirParameter guard = asm.createInputParameter("guard", CiKind.Object);
// XirOperand fieldOffset = asm.createTemp("fieldOffset", CiKind.Int);
// if (isStatic) {
// callRuntimeThroughStub(asm, "resolveGetStatic", fieldOffset, guard);
// } else {
// callRuntimeThroughStub(asm, "resolveGetField", fieldOffset, guard);
// }
// asm.pload(kind, result, object, fieldOffset, true);

            unresolved = asm.finishTemplate("getfield<" + kind + ">-unresolved");
        } else {
            unresolved = null;
        }
        return new XirPair(resolved, unresolved);
    }

    private XirPair buildPutFieldTemplate(CiKind kind, boolean genWriteBarrier, boolean isStatic) {
        final XirTemplate resolved;
        final XirTemplate unresolved;
        {
            // resolved case
            asm.restart(CiKind.Void);
            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter value = asm.createInputParameter("value", kind);
            XirParameter fieldOffset = asm.createConstantInputParameter("fieldOffset", CiKind.Int);
            asm.pstore(kind, object, fieldOffset, value, true);
            if (genWriteBarrier) {
                // TODO write barrier
                // addWriteBarrier(asm, object, value);
            }
            resolved = asm.finishTemplate("putfield<" + kind + ", " + genWriteBarrier + ">");
        }
        if (isStatic) {
            // unresolved case
            asm.restart(CiKind.Void);
            asm.shouldNotReachHere();

//            XirParameter object = asm.createInputParameter("object", CiKind.Object);
//            XirParameter value = asm.createInputParameter("value", kind);
//            XirParameter guard = asm.createInputParameter("guard", CiKind.Object);
//            XirOperand fieldOffset = asm.createTemp("fieldOffset", CiKind.Int);
//            if (isStatic) {
//                callRuntimeThroughStub(asm, "resolvePutStatic", fieldOffset, guard);
//            } else {
//                callRuntimeThroughStub(asm, "resolvePutField", fieldOffset, guard);
//            }
//            asm.pstore(kind, object, fieldOffset, value, true);
//            if (genWriteBarrier) {
//                addWriteBarrier(asm, object, value);
//            }

            unresolved = asm.finishTemplate("putfield<" + kind + ", " + genWriteBarrier + ">-unresolved");
        } else {
            unresolved = null;
        }
        return new XirPair(resolved, unresolved);
    }

    private XirPair buildInstanceof(boolean nonnull) {
        XirTemplate resolved;
        XirTemplate unresolved;
        {
            XirOperand result = asm.restart(CiKind.Boolean);

            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            XirParameter hub = asm.createConstantInputParameter("hub", CiKind.Object);
            XirOperand temp = asm.createTemp("temp", CiKind.Object);
            XirLabel end = asm.createInlineLabel("end");
            XirLabel slow_path = asm.createOutOfLineLabel("slow path");

            asm.mov(result, asm.b(false));
            if (!nonnull) {
                // first check for null
                asm.jeq(end, object, asm.o(null));
            }
            asm.pload(CiKind.Object, temp, object, asm.i(config.hubOffset), !nonnull);
            asm.mov(result, asm.b(true));
            asm.jneq(slow_path, temp, hub);

            asm.bindInline(end);

            asm.bindOutOfLine(slow_path);
            asm.push(temp);
            asm.push(hub);
            asm.callRuntime(config.instanceofStub, result);
            asm.pop(hub);
            asm.pop(result);
            asm.jmp(end);
            resolved = asm.finishTemplate("instanceof-leaf<" + nonnull + ">");
        }
        {/*
          * // unresolved instanceof unresolved = buildUnresolvedInstanceOf(nonnull);
          */
            asm.restart(CiKind.Boolean);
            XirParameter object = asm.createInputParameter("object", CiKind.Object);
            System.out.println(object);
            asm.shouldNotReachHere();
            unresolved = asm.finishTemplate("instanceof-leaf<" + nonnull + ">");
        }
        return new XirPair(resolved, unresolved);
    }

    private XirTemplate buildArrayStore(CiKind kind, CiXirAssembler asm, boolean genBoundsCheck, boolean genStoreCheck, boolean genWriteBarrier) {
        asm.restart(CiKind.Void);
        XirParameter array = asm.createInputParameter("array", CiKind.Object);
        XirParameter index = asm.createInputParameter("index", CiKind.Int);
        XirParameter value = asm.createInputParameter("value", kind);
        XirOperand length = asm.createTemp("length", CiKind.Int);
        XirOperand temp = asm.createTemp("temp", CiKind.Word);
        XirOperand valueHub = null;
        XirOperand compHub = null;
        XirLabel store = asm.createInlineLabel("store");
        XirLabel failBoundsCheck = null;
        XirLabel slowStoreCheck = null;
        if (genBoundsCheck) {
            // load the array length and check the index
            failBoundsCheck = asm.createOutOfLineLabel("failBoundsCheck");
            asm.pload(CiKind.Int, length, array, asm.i(config.arrayLengthOffset), true);
            asm.jugteq(failBoundsCheck, index, length);
        }
        if (genStoreCheck) {
            slowStoreCheck = asm.createOutOfLineLabel("slowStoreCheck");
            asm.jeq(store, value, asm.o(null)); // first check if value is null
            valueHub = asm.createTemp("valueHub", CiKind.Object);
            compHub = asm.createTemp("compHub", CiKind.Object);
            asm.pload(CiKind.Object, compHub, array, asm.i(config.hubOffset), !genBoundsCheck);
            asm.pload(CiKind.Object, compHub, compHub, asm.i(config.arrayClassElementOffset), false);
            asm.pload(CiKind.Object, valueHub, value, asm.i(config.hubOffset), false);
            asm.jneq(slowStoreCheck, compHub, valueHub); // then check component hub matches value hub
        }
        asm.bindInline(store);
        int elemSize = target.sizeInBytes(kind);
        asm.pstore(kind, array, index, value, config.getArrayOffset(kind), Scale.fromInt(elemSize), !genBoundsCheck && !genStoreCheck);
        if (genWriteBarrier) {
            // addWriteBarrier(asm, array, value);
        }
        if (genBoundsCheck) {
            asm.bindOutOfLine(failBoundsCheck);
            asm.shouldNotReachHere();
            // callRuntimeThroughStub(asm, "throwArrayIndexOutOfBoundsException", null, array, index);
        }
        if (genStoreCheck) {
            asm.bindOutOfLine(slowStoreCheck);
            asm.push(valueHub);
            asm.push(compHub);
            asm.callRuntime(config.instanceofStub, null);
            asm.pop(temp);
            asm.pop(temp);
            asm.jneq(store, temp, asm.w(0));
            asm.shouldNotReachHere();
            asm.jmp(store);
        }
        return asm.finishTemplate("arraystore<" + kind + ">");
    }

    private XirTemplate buildArrayLoad(CiKind kind, CiXirAssembler asm, boolean genBoundsCheck) {
        XirOperand result = asm.restart(kind);
        XirParameter array = asm.createInputParameter("array", CiKind.Object);
        XirParameter index = asm.createInputParameter("index", CiKind.Int);
        XirOperand length = asm.createTemp("length", CiKind.Int);
        XirLabel fail = null;
        if (genBoundsCheck) {
            // load the array length and check the index
            fail = asm.createOutOfLineLabel("fail");
            asm.pload(CiKind.Int, length, array, asm.i(config.arrayLengthOffset), true);
            asm.jugteq(fail, index, length);
        }
        int elemSize = target.sizeInBytes(kind);
        asm.pload(kind, result, array, index, config.getArrayOffset(kind), Scale.fromInt(elemSize), !genBoundsCheck);
        if (genBoundsCheck) {
            asm.bindOutOfLine(fail);
            asm.shouldNotReachHere();
            // callRuntimeThroughStub(asm, "throwArrayIndexOutOfBoundsException", null, array, index);
        }
        return asm.finishTemplate("arrayload<" + kind + ">");
    }

    private XirTemplate buildInvokeStatic() {
        asm.restart();
        XirParameter addr = asm.createConstantInputParameter("addr", CiKind.Word);

        XirLabel stub = asm.createOutOfLineLabel("staticCallStub");

        asm.bindOutOfLine(stub);
        XirOperand method = asm.createRegister("method", CiKind.Word, AMD64.rbx);
        asm.mark(MARK_STATIC_CALL_STUB, XirMark.CALLSITE);
        asm.mov(method, asm.w(0l));
        XirLabel dummy = asm.createOutOfLineLabel("dummy");
        asm.jmp(dummy);
        asm.bindOutOfLine(dummy);

        return asm.finishTemplate(addr, "invokestatic");
    }

    @Override
    public XirSnippet genArrayLength(XirSite site, XirArgument array) {
        return new XirSnippet(arrayLengthTemplate, array);
    }

    @Override
    public XirSnippet genArrayLoad(XirSite site, XirArgument array, XirArgument index, XirArgument length, CiKind elementKind, RiType elementType) {
        // TODO: emit different template if length is present
        return new XirSnippet(arrayLoadTemplates[elementKind.ordinal()], array, index);
    }

    @Override
    public XirSnippet genArrayStore(XirSite site, XirArgument array, XirArgument index, XirArgument length, XirArgument value, CiKind elementKind, RiType elementType) {
        // TODO: emit different template if length is present
        return new XirSnippet(arrayStoreTemplates[elementKind.ordinal()], array, index, value);
    }

    @Override
    public XirSnippet genCheckCast(XirSite site, XirArgument receiver, XirArgument hub, RiType type) {
        return new XirSnippet(emptyTemplates[CiKind.Object.ordinal()]);
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
        XirPair pair = getStaticFieldTemplates[field.kind().ordinal()];
        assert field.isResolved() : "getfield doesn't expect unresolved fields";
        XirArgument offset = XirArgument.forInt(((HotSpotField) field).offset());
        return new XirSnippet(pair.resolved, receiver, offset);
    }

    @Override
    public XirSnippet genGetStatic(XirSite site, XirArgument staticTuple, RiField field) {
        XirPair pair = getStaticFieldTemplates[field.kind().ordinal()];
        if (field.isResolved()) {
            XirArgument offset = XirArgument.forInt(((HotSpotField) field).offset());
            return new XirSnippet(pair.resolved, staticTuple, offset);
        }
        return new XirSnippet(pair.unresolved, staticTuple, null);
    }

    @Override
    public XirSnippet genPutField(XirSite site, XirArgument receiver, RiField field, XirArgument value) {
        XirPair pair = putFieldTemplates[field.kind().ordinal()];
        assert field.isResolved() : "putfield doesn't expect unresolved fields";
        XirArgument offset = XirArgument.forInt(((HotSpotField) field).offset());
        return new XirSnippet(pair.resolved, receiver, value, offset);
    }

    @Override
    public XirSnippet genPutStatic(XirSite site, XirArgument staticTuple, RiField field, XirArgument value) {
        XirPair pair = putFieldTemplates[field.kind().ordinal()];
        if (field.isResolved()) {
            XirArgument offset = XirArgument.forInt(((HotSpotField) field).offset());
            return new XirSnippet(pair.resolved, staticTuple, value, offset);
        }
        return new XirSnippet(pair.unresolved, staticTuple, value);
    }

    @Override
    public XirSnippet genInstanceOf(XirSite site, XirArgument receiver, XirArgument hub, RiType type) {
        if (type.isResolved()) {
            return new XirSnippet(instanceofTemplate.resolved, receiver, hub);
        }
        // XirArgument guard = guardFor(type, ResolveClass.SNIPPET);
        return new XirSnippet(instanceofTemplate.unresolved, receiver);
    }

    @Override
    public XirSnippet genIntrinsic(XirSite site, XirArgument[] arguments, RiMethod method) {
        return null;
    }

    @Override
    public XirSnippet genInvokeInterface(XirSite site, XirArgument receiver, RiMethod method) {
        return new XirSnippet(emptyTemplates[CiKind.Word.ordinal()]);
    }

    @Override
    public XirSnippet genInvokeSpecial(XirSite site, XirArgument receiver, RiMethod method) {
        return new XirSnippet(emptyTemplates[CiKind.Word.ordinal()]);
    }

    @Override
    public XirSnippet genInvokeStatic(XirSite site, RiMethod method) {
        return new XirSnippet(invokeStaticTemplate, XirArgument.forWord(0));
    }

    @Override
    public XirSnippet genInvokeVirtual(XirSite site, XirArgument receiver, RiMethod method) {
        return new XirSnippet(emptyTemplates[CiKind.Word.ordinal()]);
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
        return new XirSnippet(emptyTemplates[CiKind.Object.ordinal()]);
    }

    @Override
    public XirSnippet genNewInstance(XirSite site, RiType type) {
        return new XirSnippet(emptyTemplates[CiKind.Object.ordinal()]);
    }

    @Override
    public XirSnippet genNewMultiArray(XirSite site, XirArgument[] lengths, RiType type) {
        return new XirSnippet(emptyTemplates[CiKind.Object.ordinal()]);
    }

    @Override
    public XirSnippet genResolveClass(XirSite site, RiType type, Representation representation) {
        XirOperand result = asm.restart(CiKind.Object);
        if (type.isResolved()) {
            asm.mov(result, asm.o(type));
            return new XirSnippet(asm.finishTemplate("resolve class"));
        }
        asm.shouldNotReachHere();
        return new XirSnippet(asm.finishTemplate("resolve class"));

    }

    @Override
    public XirSnippet genSafepoint(XirSite site) {
        return new XirSnippet(emptyTemplates[CiKind.Void.ordinal()]);
    }

    @Override
    public XirSnippet genExceptionObject(XirSite site) {
        return new XirSnippet(exceptionObjectTemplate);
    }

}
