/*
 * Copyright (c) 2009 Sun Microsystems, Inc. All rights reserved.
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.sun.c1x.target.amd64.*;
import com.sun.cri.ci.CiKind;
import com.sun.cri.ri.RiField;
import com.sun.cri.ri.RiMethod;
import com.sun.cri.ri.RiRegisterConfig;
import com.sun.cri.ri.RiType;
import com.sun.cri.ri.RiType.Representation;
import com.sun.cri.xir.*;
import com.sun.cri.xir.CiXirAssembler.*;

/**
 *
 * @author Thomas Wuerthinger
 *
 */
public class HotSpotXirGenerator extends RiXirGenerator {

    private final HotSpotVMConfig config;
    private CiXirAssembler asm;
    private final RiRegisterConfig registerConfig;

    private XirTemplate[] emptyTemplates = new XirTemplate[CiKind.values().length];
    private XirTemplate prologueTemplate;
    private XirTemplate staticPrologueTemplate;
    private XirTemplate epilogueTemplate;
    private XirTemplate arrayLengthTemplate;
    private XirTemplate exceptionObjectTemplate;

    static class XirPair {

        final XirTemplate resolved;
        final XirTemplate unresolved;

        XirPair(XirTemplate resolved, XirTemplate unresolved) {
            this.resolved = resolved;
            this.unresolved = unresolved;
        }
    }

    private XirPair[] putFieldTemplates;
    private XirPair[] getFieldTemplates;
    private XirPair[] putStaticFieldTemplates;
    private XirPair[] getStaticFieldTemplates;
    private XirPair instanceofTemplate;
    private XirPair instanceofTemplateNonnull;

    public HotSpotXirGenerator(HotSpotVMConfig config, RiRegisterConfig registerConfig) {
        this.config = config;
        this.registerConfig = registerConfig;
    }

    @Override
    public List<XirTemplate> buildTemplates(CiXirAssembler asm) {
        this.asm = asm;

        putFieldTemplates = new XirPair[CiKind.values().length];
        getFieldTemplates = new XirPair[CiKind.values().length];
        putStaticFieldTemplates = new XirPair[CiKind.values().length];
        getStaticFieldTemplates = new XirPair[CiKind.values().length];

        List<XirTemplate> templates = new ArrayList<XirTemplate>();
        for (int index = 0; index < CiKind.values().length; index++) {

            CiKind kind = CiKind.values()[index];

            if (kind == CiKind.Float || kind == CiKind.Double)
                continue;

            if (CiKind.values()[index] == CiKind.Void) {
                asm.restart(CiKind.values()[index]);
                emptyTemplates[index] = asm.finishTemplate("empty-" + CiKind.values()[index]);
            } else {
                asm.restart();
                XirOperand result = asm.createTemp("result", CiKind.values()[index]);
                emptyTemplates[index] = asm.finishTemplate(result, "empty-" + CiKind.values()[index]);

                putFieldTemplates[index] = buildPutFieldTemplate(kind, kind == CiKind.Object, false);
                getFieldTemplates[index] = buildGetFieldTemplate(kind, false);
                putStaticFieldTemplates[index] = buildPutFieldTemplate(kind, kind == CiKind.Object, true);
                getStaticFieldTemplates[index] = buildGetFieldTemplate(kind, true);
// arrayLoadTemplates[index] = buildArrayLoad(kind, asm, true);
// arrayStoreTemplates[index] = buildArrayStore(kind, asm, true, kind == CiKind.Object, kind == CiKind.Object);
// newArrayTemplates[index] = buildNewArray(kind);
            }
            templates.add(emptyTemplates[index]);
        }
        prologueTemplate = buildPrologue(false);
        staticPrologueTemplate = buildPrologue(true);
        epilogueTemplate = buildEpilogue();
        arrayLengthTemplate = buildArrayLength();
        exceptionObjectTemplate = buildExceptionObject();
        instanceofTemplate = buildInstanceof(false);
        instanceofTemplateNonnull = buildInstanceof(true);

        return templates;
    }

    private XirTemplate buildPrologue(boolean staticMethod) {
        asm.restart(CiKind.Void);
        XirOperand sp = asm.createRegister("stack pointer", CiKind.Word, registerConfig.getStackPointerRegister());
        XirOperand temp = asm.createRegister("temp (rax)", CiKind.Int, AMD64.rax);

        asm.align(config.codeEntryAlignment);
        asm.entrypoint(HotSpotRuntime.Entrypoints.UNVERIFIED);
        if (!staticMethod) {
            // TODO do some checking...
            asm.add(temp, temp, asm.i(1));
            asm.sub(temp, temp, asm.i(1));
            asm.shouldNotReachHere();

            asm.align(config.codeEntryAlignment);
        }
        asm.entrypoint(HotSpotRuntime.Entrypoints.VERIFIED);
        // stack banging
        asm.pstore(CiKind.Word, sp, asm.i(-config.stackShadowPages * config.vmPageSize), temp, true);
        asm.pushFrame();

        return asm.finishTemplate(staticMethod ? "static prologue" : "prologue");
    }

    private XirTemplate buildEpilogue() {
        asm.restart(CiKind.Void);
        asm.popFrame();
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
            /*
             * XirParameter object = asm.createInputParameter("object", CiKind.Object); XirParameter guard =
             * asm.createInputParameter("guard", CiKind.Object); XirOperand fieldOffset = asm.createTemp("fieldOffset",
             * CiKind.Int); if (isStatic) { callRuntimeThroughStub(asm, "resolveGetStatic", fieldOffset, guard); } else
             * { callRuntimeThroughStub(asm, "resolveGetField", fieldOffset, guard); } asm.pload(kind, result, object,
             * fieldOffset, true);
             */

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
            /*
             * XirParameter object = asm.createInputParameter("object", CiKind.Object); XirParameter value =
             * asm.createInputParameter("value", kind); XirParameter guard = asm.createInputParameter("guard",
             * CiKind.Object); XirOperand fieldOffset = asm.createTemp("fieldOffset", CiKind.Int); if (isStatic) {
             * callRuntimeThroughStub(asm, "resolvePutStatic", fieldOffset, guard); } else { callRuntimeThroughStub(asm,
             * "resolvePutField", fieldOffset, guard); } asm.pstore(kind, object, fieldOffset, value, true); if
             * (genWriteBarrier) { addWriteBarrier(asm, object, value); }
             */
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
            asm.shouldNotReachHere();
            unresolved = asm.finishTemplate("instanceof-leaf<" + nonnull + ">");
        }
        return new XirPair(resolved, unresolved);
    }

    @Override
    public XirSnippet genArrayLength(XirSite site, XirArgument array) {
        return new XirSnippet(arrayLengthTemplate, array);
    }

    @Override
    public XirSnippet genArrayLoad(XirSite site, XirArgument array, XirArgument index, XirArgument length, CiKind elementKind, RiType elementType) {
        return new XirSnippet(emptyTemplates[elementKind.ordinal()]);
    }

    @Override
    public XirSnippet genArrayStore(XirSite site, XirArgument array, XirArgument index, XirArgument length, XirArgument value, CiKind elementKind, RiType elementType) {
        return new XirSnippet(emptyTemplates[CiKind.Void.ordinal()]);
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
        return new XirSnippet(emptyTemplates[CiKind.Word.ordinal()]);
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
        System.out.println("genResolveClass " + type + ", " + representation);
        XirOperand result = asm.restart(CiKind.Object);
        if (type.isResolved()) {
            System.out.println("resolved");
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
