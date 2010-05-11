/*
 * Copyright 2000-2010 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */


# include "incls/_precompiled.incl"
# include "incls/_c1x_Compiler.cpp.incl"


// Initialization
void C1XCompiler::initialize() {
	TRACE_C1X_1("initialize");
}

// Compilation entry point for methods
void C1XCompiler::compile_method(ciEnv* env, ciMethod* target, int entry_bci) {
	VM_ENTRY_MARK;
	
	ResourceMark rm;
	HandleMark hm;

	TRACE_C1X_1("compile_method");

	methodHandle method(Thread::current(), (methodOop)target->get_oop());
	TRACE_C1X_1("name = %s", method->name_and_sig_as_C_string());

	JavaValue result(T_VOID);
	symbolHandle syKlass = oopFactory::new_symbol("com/sun/hotspot/c1x/VMExits", CHECK);


	Handle nullh;
	KlassHandle k = SystemDictionary::resolve_or_null(syKlass, nullh, nullh, CHECK);
	if (k.is_null()) {
		tty->print_cr("not found");
		return;//fatal("Could not find class com.sun.hotspot.c1x.VMExits");
	}
	symbolHandle syName = oopFactory::new_symbol("compileMethod", CHECK);
	symbolHandle sySig = oopFactory::new_symbol("(Lcom/sun/cri/ri/RiMethod;I)V", CHECK);
	JavaCallArguments args;
	args.push_oop(method());
	args.push_int(entry_bci);
	JavaCalls::call_static(&result, k, syName, sySig, &args, CHECK);
}

// Print compilation timers and statistics
void C1XCompiler::print_timers() {
	TRACE_C1X_1("print_timers");
}