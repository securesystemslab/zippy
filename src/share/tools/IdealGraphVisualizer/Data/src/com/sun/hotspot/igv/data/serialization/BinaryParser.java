/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.hotspot.igv.data.serialization;

import com.sun.hotspot.igv.data.*;
import com.sun.hotspot.igv.data.services.GroupCallback;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 *
 * @author gd
 */
public class BinaryParser {
    private static final int BEGIN_GROUP = 0x00;
    private static final int BEGIN_GRAPH = 0x01;
    private static final int CLOSE_GROUP = 0x02;

    private static final int POOL_NULL = -1;
    private static final int POOL_NEW = 0x00;
    private static final int POOL_STRING = 0x01;
    private static final int POOL_ENUM = 0x02;
    private static final int POOL_CLASS = 0x03;
    private static final int POOL_METHOD = 0x04;
    
    private static final int KLASS = 0x00;
    private static final int ENUM_KLASS = 0x01;
    
    private static final int PROPERTY_POOL = 0x00;
    private static final int PROPERTY_INT = 0x01;
    private static final int PROPERTY_LONG = 0x02;
    private static final int PROPERTY_DOUBLE = 0x03;
    private static final int PROPERTY_FLOAT = 0x04;
    private static final int PROPERTY_TRUE = 0x05;
    private static final int PROPERTY_FALSE = 0x06;
    private static final int PROPERTY_ARRAY = 0x07;
    
    private static final String NO_BLOCK = "noBlock";
    
    private GroupCallback callback;
    private List<Object> constantPool;
    private int maxConstant;
    private final ByteBuffer buffer;
    private final ReadableByteChannel channel;
    private Deque<Folder> folderStack;
    
    private static class Klass {
        public String name;
        public Klass(String name) {
            this.name = name;
        }
    }
    
    private static class EnumKlass extends Klass {
        public String[] values;
        public EnumKlass(String name, String[] values) {
            super(name);
            this.values = values;
        }
    }
    
    private static class EnumValue {
        public EnumKlass enumKlass;
        public int ordinal;
        public EnumValue(EnumKlass enumKlass, int ordinal) {
            this.enumKlass = enumKlass;
            this.ordinal = ordinal;
        }
    }

    public BinaryParser(GroupCallback callback, ReadableByteChannel channel) {
        this.callback = callback;
        constantPool = new ArrayList<>();
        buffer = ByteBuffer.allocateDirect(64 * 1024);
        buffer.flip();
        this.channel = channel;
        folderStack = new LinkedList<>();
    }
    
    private void fill() throws IOException {
        buffer.compact();
        channel.read(buffer);
        buffer.flip();
    }
    
    private void ensureAvailable(int i) throws IOException {
        while (buffer.remaining() < i) {
            fill();
        }
    }
    
    private int readByte() throws IOException {
        ensureAvailable(1);
        return ((int)buffer.get()) & 0xff;
    }

    private int readInt() throws IOException {
        ensureAvailable(4);
        return buffer.getInt();
    }
    
    private char readShort() throws IOException {
        ensureAvailable(2);
        return buffer.getChar();
    }
    
    private long readLong() throws IOException {
        ensureAvailable(8);
        return buffer.getLong();
    }
    
    private double readDouble() throws IOException {
        ensureAvailable(8);
        return buffer.getDouble();
    }
    
    private float readFloat() throws IOException {
        ensureAvailable(4);
        return buffer.getFloat();
    }

    private String readString() throws IOException {
        int len = readInt();
        ensureAvailable(len * 2);
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = buffer.getChar();
        }
        return new String(chars);
    }

    private byte[] readBytes() throws IOException {
        int len = readInt();
        if (len < 0) {
            return null;
        }
        ensureAvailable(len);
        byte[] data = new byte[len];
        buffer.get(data);
        return data;
    }
    
    private String readIntsToString() throws IOException {
        int len = readInt();
        if (len < 0) {
            return "null";
        }
        ensureAvailable(len * 4);
        StringBuilder sb = new StringBuilder().append('[');
        for (int i = 0; i < len; i++) {
            sb.append(buffer.getInt());
            if (i < len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }
    
    private String readDoublesToString() throws IOException {
        int len = readInt();
        if (len < 0) {
            return "null";
        }
        ensureAvailable(len * 8);
        StringBuilder sb = new StringBuilder().append('[');
        for (int i = 0; i < len; i++) {
            sb.append(buffer.getDouble());
            if (i < len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }
    
    private String readPoolObjectsToString() throws IOException {
        int len = readInt();
        if (len < 0) {
            return "null";
        }
        StringBuilder sb = new StringBuilder().append('[');
        for (int i = 0; i < len; i++) {
            sb.append(readPoolObject(Object.class));
            if (i < len - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }
    
    private <T> T readPoolObject(Class<T> klass) throws IOException {
        int type = readByte();
        if (type == POOL_NULL) {
            return null;
        }
        if (type == POOL_NEW) {
            return (T) addPoolEntry(klass);
        }
        int index = readInt();
        if (index < 0 || index >= constantPool.size()) {
            throw new IOException("Invalid constant pool index : " + index);
        }
        Object obj = constantPool.get(index);
        return (T) obj;
    }
    
    private boolean assertObjectType(Class<?> klass, int type) {
        switch(type) {
            case POOL_CLASS:
                return klass.isAssignableFrom(Klass.class);
            case POOL_ENUM:
                return klass.isAssignableFrom(EnumValue.class);
            case POOL_METHOD:
                return klass.isAssignableFrom(byte[].class);
            case POOL_STRING:
                return klass.isAssignableFrom(String.class);
            case POOL_NULL:
                return true;
            default:
                return false;
        }
    }

    private Object addPoolEntry(Class<?> klass) throws IOException {
        int index = readInt();
        int type = readByte();
        assert assertObjectType(klass, type) : "Wrong object type : " + klass + " != " + type;
        Object obj;
        switch(type) {
            case POOL_CLASS:
                String name = readString();
                int klasstype = readByte();
                if (klasstype == ENUM_KLASS) {
                    int len = readInt();
                    String[] values = new String[len];
                    for (int i = 0; i < len; i++) {
                        values[i] = readPoolObject(String.class);
                    }
                    obj = new EnumKlass(name, values);
                } else if (klasstype == KLASS) {
                    obj = new Klass(name);
                } else {
                    throw new IOException("unknown klass type");
                }
                break;
            case POOL_ENUM:
                EnumKlass enumClass = readPoolObject(EnumKlass.class);
                int ordinal = readInt();
                obj = new EnumValue(enumClass, ordinal);
                break;
            case POOL_METHOD:
                obj = readBytes();
                break;
            case POOL_STRING:
                obj = readString();
                break;
            default:
                throw new IOException("unknown pool type");
        }
        while (constantPool.size() <= index) {
            constantPool.add(null);
        }
        constantPool.set(index, obj);
        return obj;
    }
    
    private Object readPropertyObject() throws IOException {
        int type = readByte();
        switch (type) {
            case PROPERTY_INT:
                return readInt();
            case PROPERTY_LONG:
                return readLong();
            case PROPERTY_FLOAT:
                return readFloat();
            case PROPERTY_DOUBLE:
                return readDouble();
            case PROPERTY_TRUE:
                return Boolean.TRUE;
            case PROPERTY_FALSE:
                return Boolean.FALSE;
            case PROPERTY_POOL:
                return readPoolObject(Object.class);
            case PROPERTY_ARRAY:
                int subType = readByte();
                switch(subType) {
                    case PROPERTY_INT:
                        return readIntsToString();
                    case PROPERTY_DOUBLE:
                        return readDoublesToString();
                    case PROPERTY_POOL:
                        return readPoolObjectsToString();
                    default:
                        throw new IOException("Unknown type");
                }
            default:
                throw new IOException("Unknown type");
        }
    }

    public void parse() throws IOException {
        folderStack.push(new GraphDocument());
        while(true) {
            parseRoot();
        }
    }

    private void parseRoot() throws IOException {
        int type = readByte();
        switch(type) {
            case BEGIN_GRAPH: {
                final Folder parent = folderStack.peek();
                final InputGraph graph = parseGraph();
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        parent.addElement(graph);
                    }
                });
                break;
            }
            case BEGIN_GROUP: {
                final Folder parent = folderStack.peek();
                final Group group = parseGroup(parent);
                if (callback == null || parent instanceof Group) {
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            parent.addElement(group);
                        }
                    });
                }
                folderStack.push(group);
                if (callback != null && parent instanceof GraphDocument) {
                    callback.started(group);
                }
                break;
            }
            case CLOSE_GROUP: {
                if (folderStack.isEmpty()) {
                    throw new IOException("Unbalanced groups");
                }
                folderStack.pop();
                break;
            }
            default:
                throw new IOException("unknown root : " + type);
        }
    }

    private Group parseGroup(Folder parent) throws IOException {
        String name = readPoolObject(String.class);
        String shortName = readPoolObject(String.class);
        byte[] bytecodes = readPoolObject(byte[].class);
        int bci = readInt();
        Group group = new Group(parent);
        group.getProperties().setProperty("name", name);
        final InputMethod method = new InputMethod(group, name, shortName, bci);
        if (bytecodes != null) {
            method.setBytecodes("TODO");
        }
        group.setMethod(method);
        return group;
    }
    
    private InputGraph parseGraph() throws IOException {
        String title = readPoolObject(String.class);
        InputGraph graph = new InputGraph(title);
        parseNodes(graph);
        parseEdges(graph);
        parseBlocks(graph);
        graph.ensureNodesInBlocks();
        return graph;
    }
    
    private void parseBlocks(InputGraph graph) throws IOException {
        int blockCount = readInt();
        for (int i = 0; i < blockCount; i++) {
            int id = readInt();
            String name = id >= 0 ? Integer.toString(id) : NO_BLOCK;
            InputBlock block = graph.addBlock(name);
            int nodeCount = readInt();
            for (int j = 0; j < nodeCount; j++) {
                block.addNode(readInt());
            }
        }
        int edgeCount = readInt();
        for (int i = 0; i < edgeCount; i++) {
            int from = readInt();
            int to = readInt();
            String fromName = from >= 0 ? Integer.toString(from) : NO_BLOCK;
            String toName = to >= 0 ? Integer.toString(to) : NO_BLOCK;
            graph.addBlockEdge(graph.getBlock(fromName), graph.getBlock(toName));
        }
    }
    
    private void parseEdges(InputGraph graph) throws IOException {
        int count = readInt();
        for (int i = 0; i < count; i++) {
            char fromIndex = readShort();
            int from = readInt();
            char toIndex = readShort();
            int to = readInt();
            String label = readPoolObject(String.class);
            graph.addEdge(new InputEdge(fromIndex, toIndex, from, to, label));
        }
    }

    private void parseNodes(InputGraph graph) throws IOException {
        int count = readInt();
        
        for (int i = 0; i < count; i++) {
            int id = readInt();
            InputNode node = new InputNode(id);
            final Properties properties = node.getProperties();
            String name = readPoolObject(String.class);
            String simpleName = readPoolObject(String.class);
            int block = readInt();
            int preds = readByte();
            properties.setProperty("name", name);
            properties.setProperty("class", simpleName);
            properties.setProperty("predecessorCount", Integer.toString(preds));
            if (block > 0) {
                properties.setProperty("block", Integer.toString(block));
            } else {
                properties.setProperty("block", NO_BLOCK);
            }
            int propCount = readInt();
            for (int j = 0; j < propCount; j++) {
                String key = readPoolObject(String.class);
                Object value = readPropertyObject();
                properties.setProperty(key, value.toString());
            }
            graph.addNode(node);
        }
    }
}
