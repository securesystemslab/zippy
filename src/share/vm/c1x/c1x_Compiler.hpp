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

class C1XCompiler : public AbstractCompiler {

public:

	virtual const char* name() { return "C1X"; }

	// Native / OSR not supported
	virtual bool supports_native()                 { return false; }
	virtual bool supports_osr   ()                 { return false; }

	// Pretend to be C1
	bool is_c1   ()                                { return true; }
	bool is_c2   ()                                { return false; }

	// Initialization
	virtual void initialize();

	// Compilation entry point for methods
	virtual void compile_method(ciEnv* env, ciMethod* target, int entry_bci);

	// Print compilation timers and statistics
	virtual void print_timers();

  static oop get_RiMethod(ciMethod *ciMethod);
  static oop get_RiField(ciField *ciField);
  static oop get_RiType(ciType *klass);
  static oop get_RiType(klassOop klass);
  static oop get_RiMethod(methodOop method);
  static oop get_RiType(symbolOop klass, klassOop accessingType);
  static oop get_unresolved_RiType(symbolOop klass, klassOop accessingType);
  static oop get_RiConstantPool(constantPoolOop cpOop);
};


// Tracing macros

#define IF_TRACE_C1X_1 if (TraceC1X >= 1) 
#define IF_TRACE_C1X_2 if (TraceC1X >= 2) 
#define IF_TRACE_C1X_3 if (TraceC1X >= 3) 
#define IF_TRACE_C1X_4 if (TraceC1X >= 4) 
#define IF_TRACE_C1X_5 if (TraceC1X >= 5) 

#define TRACE_C1X_1 if (TraceC1X >= 1) tty->print("TraceC1X-1: "); if (TraceC1X >= 1) tty->print_cr
#define TRACE_C1X_2 if (TraceC1X >= 2) tty->print("   TraceC1X-2: "); if (TraceC1X >= 2) tty->print_cr
#define TRACE_C1X_3 if (TraceC1X >= 3) tty->print("      TraceC1X-3: "); if (TraceC1X >= 3) tty->print_cr
#define TRACE_C1X_4 if (TraceC1X >= 4) tty->print("         TraceC1X-4: "); if (TraceC1X >= 4) tty->print_cr
#define TRACE_C1X_5 if (TraceC1X >= 5) tty->print("            TraceC1X-5: "); if (TraceC1X >= 5) tty->print_cr