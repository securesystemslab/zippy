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
import java.util.Arrays;
import java.util.List;

import edu.uci.python.runtime.datatypes.*;
import edu.uci.python.runtime.modules.annotations.*;

public class BisectModule extends PModule {

    public BisectModule() {
        super("bisect");
        addBuiltInMethods();
    }

    @ModuleMethod
    public int bisect(Object[] args, Object[] keywords) {
        if (args.length == 2) {
            return bisect(args[0], args[1]);
        } else if (args.length == 3 && args[0] instanceof PList) {
            PList slice = (PList) ((PList) args[0]).getSlice((int) args[2], ((PList) args[0]).len(), 1, ((PList) args[0]).len());
            ArrayList<Object> tempList = new ArrayList<Object>(slice.getList());
            tempList.add(args[1]);
            Object[] tempArray = tempList.toArray();
            Arrays.sort(tempArray);
            int index = Arrays.binarySearch(tempArray, args[1]);

            do {
                index++;
            } while (index < tempArray.length && tempArray[index].equals(args[1]));

            return index + (int) args[2] - 1;
        } else if (args.length == 4 && args[0] instanceof PList) {
            PList slice = (PList) ((PList) args[0]).getSlice((int) args[2], (int) args[3], 1, ((PList) args[0]).len());
            ArrayList<Object> tempList = new ArrayList<Object>(slice.getList());
            tempList.add(args[1]);
            Object[] tempArray = tempList.toArray();
            Arrays.sort(tempArray);
            int index = Arrays.binarySearch(tempArray, args[1]);

            do {
                index++;
            } while (index < tempArray.length && tempArray[index].equals(args[1]));

            return index + (int) args[2] - 1;
        } else {
            throw new RuntimeException("wrong number of arguments for bisect() ");
        }
    }

    public int bisect(Object arg0, Object arg1) {
        if (arg0 instanceof PList) {
            List<Object> list = ((PList) arg0).getList();

            if (list.size() == 0) {
                return 0;
            }

            return getIndexRight(list, arg1);
        } else {
            throw new RuntimeException("invalid arguments number for bisect() ");
        }
    }

    public int bisect(Object arg) {
        throw new RuntimeException("wrong number of arguments for bisect() ");
    }

    // Checkstyle: stop method name check
    @ModuleMethod
    public int bisect_right(Object[] args, Object[] keywords) {
        return bisect(args, keywords);
    }

    public int bisect_right(Object arg0, Object arg1) {
        return bisect(arg0, arg1);
    }

    public int bisect_right(Object arg) {
        return bisect(arg);
    }

    @ModuleMethod
    public int bisect_left(Object[] args, Object[] keywords) {
        if (args.length == 2) {
            return bisect_left(args[0], args[1]);
        } else if (args.length == 3 && args[0] instanceof PList) {
            PList slice = (PList) ((PList) args[0]).getSlice((int) args[2], ((PList) args[0]).len(), 1, ((PList) args[0]).len());
            ArrayList<Object> tempList = new ArrayList<Object>(slice.getList());
            tempList.add(args[1]);
            Object[] tempArray = tempList.toArray();
            Arrays.sort(tempArray);
            int index = Arrays.binarySearch(tempArray, args[1]);

            do {
                index--;
            } while (index >= 0 && tempArray[index].equals(args[1]));

            return index + 1 + (int) args[2];
        } else if (args.length == 4 && args[0] instanceof PList) {
            PList slice = (PList) ((PList) args[0]).getSlice((int) args[2], (int) args[3], 1, ((PList) args[0]).len());
            ArrayList<Object> tempList = new ArrayList<Object>(slice.getList());
            tempList.add(args[1]);
            Object[] tempArray = tempList.toArray();
            Arrays.sort(tempArray);
            int index = Arrays.binarySearch(tempArray, args[1]);

            do {
                index--;
            } while (index >= 0 && tempArray[index].equals(args[1]));

            return index + 1 + (int) args[2];
        } else {
            throw new RuntimeException("wrong number of arguments for bisect_left() ");
        }
    }

    public int bisect_left(Object arg0, Object arg1) {
        if (arg0 instanceof PList) {
            List<Object> list = ((PList) arg0).getList();

            if (list.size() == 0) {
                return 0;
            }

            return getIndexLeft(list, arg1);
        } else {
            throw new RuntimeException("invalid arguments number for bisect_left() ");
        }
    }

    public int bisect_left(Object arg) {
        throw new RuntimeException("wrong number of arguments for bisect_left() ");
    }

    @ModuleMethod
    public void insort(Object[] args, Object[] keywords) {
        if (args.length > 1 && args[0] instanceof PList) {
            ((PList) args[0]).addItem(bisect(args, keywords), args[1]);
        } else {
            throw new RuntimeException("wrong number of arguments for insort() ");
        }
    }

    public void insort(Object arg0, Object arg1) {
        if (arg0 instanceof PList) {
            ((PList) arg0).addItem(bisect(arg0, arg1), arg1);
        } else {
            throw new RuntimeException("invalid arguments number for insort() ");
        }
    }

    public void insort(Object arg) {
        throw new RuntimeException("wrong number of arguments for insort() ");
    }

    @ModuleMethod
    public void insort_right(Object[] args, Object[] keywords) {
        insort(args, keywords);
    }

    public void insort_right(Object arg0, Object arg1) {
        insort(arg0, arg1);
    }

    public void insort_right(Object arg) {
        insort(arg);
    }

    @ModuleMethod
    public void insort_left(Object[] args, Object[] keywords) {
        if (args.length > 1 && args[0] instanceof PList) {
            ((PList) args[0]).addItem(bisect_left(args, keywords), args[1]);
        } else {
            throw new RuntimeException("wrong number of arguments for insort_left() ");
        }
    }

    public void insort_left(Object arg0, Object arg1) {
        if (arg0 instanceof PList) {
            ((PList) arg0).addItem(bisect_left(arg0, arg1), arg1);
        } else {
            throw new RuntimeException("invalid arguments number for insort_left() ");
        }
    }

    public void insort_left(Object arg) {
        throw new RuntimeException("wrong number of arguments for insort_left() ");
    }

    // Checkstyle: resume method name check

    public int getIndexLeft(List<Object> args, Object key) {
        return binarySearchLeft(args, 0, args.size() - 1, key);
    }

    public int binarySearchLeft(List<Object> args, int start, int stop, Object key) {
        if (start <= stop) {
            int middle = (stop - start) / 2 + start;
            if (((String) args.get(middle)).compareTo((String) key) > 0) {
                if (middle - 1 >= 0 && ((String) args.get(middle - 1)).compareTo((String) key) < 0) {
                    return middle;
                } else if (middle - 1 <= 0) {
                    return 0;
                } else {
                    return binarySearchLeft(args, start, middle - 1, key);
                }
            } else if (((String) args.get(middle)).compareTo((String) key) < 0) {
                if (middle + 1 < args.size() && ((String) args.get(middle + 1)).compareTo((String) key) > 0) {
                    return middle + 1;
                } else if (middle + 1 >= args.size() - 1) {
                    return args.size();
                } else {
                    return binarySearchLeft(args, middle + 1, stop, key);
                }
            } else {
                int i = middle - 1;
                while (((String) args.get(i)).compareTo((String) key) == 0 && i >= 0) {
                    i--;
                }
                return i + 1;
            }
        }
        return -1; // should not happen
    }

    public int getIndexRight(List<Object> args, Object key) {
        if (key instanceof String) {
            return binarySearchRightStr(args, 0, args.size() - 1, (String) key);
        } else {
            return binarySearchRightDouble(args, 0, args.size() - 1, (double) key);
        }
    }

    public int binarySearchRightDouble(List<Object> args, int start, int stop, double key) {
        if (start <= stop) {
            int middle = (stop - start) / 2 + start;
            if (((double) args.get(middle)) > key) {
                if (middle - 1 >= 0 && ((double) args.get(middle - 1)) < key) {
                    return middle;
                } else if (middle - 1 <= 0) {
                    return 0;
                } else {
                    return binarySearchRightDouble(args, start, middle - 1, key);
                }
            } else if (((double) args.get(middle)) < key) {
                if (middle + 1 < args.size() && ((double) args.get(middle + 1)) > key) {
                    return middle + 1;
                } else if (middle + 1 >= args.size() - 1) {
                    return args.size();
                } else {
                    return binarySearchRightDouble(args, middle + 1, stop, key);
                }
            } else {
                int i = middle + 1;
                while (((double) args.get(i)) == key && i < args.size()) {
                    i++;
                }
                return i;
            }
        }
        return -1; // should not happen
    }

    public int binarySearchRightStr(List<Object> args, int start, int stop, String key) {
        if (start <= stop) {
            int middle = (stop - start) / 2 + start;
            if (((String) args.get(middle)).compareTo(key) > 0) {
                if (middle - 1 >= 0 && ((String) args.get(middle - 1)).compareTo(key) < 0) {
                    return middle;
                } else if (middle - 1 <= 0) {
                    return 0;
                } else {
                    return binarySearchRightStr(args, start, middle - 1, key);
                }
            } else if (((String) args.get(middle)).compareTo(key) < 0) {
                if (middle + 1 < args.size() && ((String) args.get(middle + 1)).compareTo(key) > 0) {
                    return middle + 1;
                } else if (middle + 1 >= args.size() - 1) {
                    return args.size();
                } else {
                    return binarySearchRightStr(args, middle + 1, stop, key);
                }
            } else {
                int i = middle + 1;
                while (((String) args.get(i)).compareTo(key) == 0 && i < args.size()) {
                    i++;
                }
                return i;
            }
        }
        return -1; // should not happen
    }

}
