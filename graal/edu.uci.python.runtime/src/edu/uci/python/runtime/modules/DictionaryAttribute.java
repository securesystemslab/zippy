/*
 * Copyright (c) 2013, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.runtime.modules;

import java.util.ArrayList;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.annotations.*;

public class DictionaryAttribute extends PythonModule {

    public DictionaryAttribute() {
        super("dict");
        try {
            addAttributeMethods();
        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @ModuleMethod
    public Object setDefalut(Object[] args, Object self) {
        if (args.length == 1) {
            return setDefalut(args[0], self);
        } else if (args.length == 2) {
            return setDefalut(args[0], args[1], self);
        } else {
            throw new RuntimeException("wrong number of arguments for setdefault()");
        }
    }

    public Object setDefalut(Object arg, Object self) {
        PDictionary dict = (PDictionary) self;

        if (dict.getMap().containsKey(arg)) {
            return dict.getMap().get(arg);
        } else {
            dict.getMap().put(arg, null);
            return null;
        }
    }

    public Object setDefalut(Object arg0, Object arg1, Object self) {
        PDictionary dict = (PDictionary) self;

        if (dict.getMap().containsKey(arg0)) {
            return dict.getMap().get(arg0);
        } else {
            dict.getMap().put(arg0, arg1);
            return arg1;
        }
    }

    @ModuleMethod
    public Object pop(Object[] args, Object self) {
        if (args.length == 1) {
            return pop(args[0], self);
        } else if (args.length == 2) {
            return pop(args[0], args[1], self);
        } else {
            throw new RuntimeException("wrong number of arguments for pop()");
        }
    }

    public Object pop(Object arg, Object self) {
        PDictionary dict = (PDictionary) self;

        Object retVal = dict.getMap().get(arg);
        if (retVal != null) {
            dict.getMap().remove(arg);
            return retVal;
        } else {
            throw new RuntimeException("invalid key for pop()");
        }
    }

    public Object pop(Object arg0, Object arg1, Object self) {
        PDictionary dict = (PDictionary) self;

        Object retVal = dict.getMap().get(arg0);
        if (retVal != null) {
            dict.getMap().remove(arg0);
            return retVal;
        } else {
            return arg1;
        }
    }

    @ModuleMethod
    public PList keys(Object[] args, Object self) {
        PDictionary dict = (PDictionary) self;

        if (args.length == 0) {
            return new PList(new ArrayList<Object>(dict.getMap().keySet()));
        } else {
            throw new RuntimeException("wrong number of arguments for keys()");
        }
    }

    public PList keys(Object arg, Object self) {
        throw new RuntimeException("wrong number of arguments for keys()");
    }

    public PList keys(Object arg0, Object arg1, Object self) {
        throw new RuntimeException("wrong number of arguments for keys()");
    }

    @ModuleMethod
    public PList items(Object[] args, Object self) {
        PDictionary dict = (PDictionary) self;

        if (args.length == 0) {
            return new PList(dict.getMap().entrySet());
        } else {
            throw new RuntimeException("wrong number of arguments for items()");
        }
    }

    public PList items(Object arg, Object self) {
        throw new RuntimeException("wrong number of arguments for items()");
    }

    public PList items(Object arg0, Object arg1, Object self) {
        throw new RuntimeException("wrong number of arguments for items()");
    }

    @ModuleMethod
    public boolean hasKey(Object[] args, Object self) {
        if (args.length == 1) {
            return hasKey(args[0], self);
        } else {
            throw new RuntimeException("wrong number of arguments for has_key()");
        }
    }

    public boolean hasKey(Object arg, Object self) {
        PDictionary dict = (PDictionary) self;

        return dict.getMap().containsKey(arg);
    }

    public boolean hasKey(Object arg0, Object arg1, Object self) {
        throw new RuntimeException("wrong number of arguments for has_key()");
    }

    @ModuleMethod
    public Object get(Object[] args, Object self) {
        PDictionary dict = (PDictionary) self;

        if (args.length == 1) {
            return dict.getMap().get(args[0]);
        } else if (args.length == 2) {
            if (dict.getMap().get(args[0]) != null) {
                return dict.getMap().get(args[0]);
            } else {
                return args[1];
            }
        } else {
            throw new RuntimeException("wrong number of arguments for get()");
        }
    }

    public Object get(Object arg, Object self) {
        PDictionary dict = (PDictionary) self;

        return dict.getMap().get(arg);
    }

    public Object get(Object arg0, Object arg1, Object self) {
        PDictionary dict = (PDictionary) self;

        if (dict.getMap().get(arg0) != null) {
            return dict.getMap().get(arg0);
        } else {
            return arg1;
        }
    }

    @ModuleMethod
    public PDictionary copy(Object[] args, Object self) {
        PDictionary dict = (PDictionary) self;

        if (args.length == 0) {
            return new PDictionary(dict.getMap());
        } else {
            throw new RuntimeException("wrong number of arguments for copy()");
        }
    }

    public PDictionary copy(Object arg, Object self) {
        throw new RuntimeException("wrong number of arguments for copy()");
    }

    public PDictionary copy(Object arg0, Object arg1, Object self) {
        throw new RuntimeException("wrong number of arguments for copy()");
    }

    @ModuleMethod
    public PDictionary clear(Object[] args, Object self) {
        PDictionary dict = (PDictionary) self;

        if (args.length == 0) {
            dict.getMap().clear();
            return dict;
        } else {
            throw new RuntimeException("wrong number of arguments for clear()");
        }
    }

    public PDictionary clear(Object arg, Object self) {
        throw new RuntimeException("wrong number of arguments for clear()");
    }

    public PDictionary clear(Object arg0, Object arg1, Object self) {
        throw new RuntimeException("wrong number of arguments for clear()");
    }

    @ModuleMethod
    public PList values(Object[] args, Object self) {
        PDictionary dict = (PDictionary) self;

        if (args.length == 0) {
            return new PList(new ArrayList<Object>(dict.getMap().values()));
        } else {
            throw new RuntimeException("wrong number of arguments for values()");
        }
    }

    public PDictionary values(Object arg, Object self) {
        throw new RuntimeException("wrong number of arguments for values()");
    }

    public PDictionary values(Object arg0, Object arg1, Object self) {
        throw new RuntimeException("wrong number of arguments for values()");
    }
}
