package com.mineclay.tclite;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NbtUtil {
    public static String dumpItemNbt(ItemStack item) {
        return dumpItemNbt(item, 0);
    }

    public static NbtBase<?> getDeep(NbtCompound compound, String path) {
        String[] keys = path.split("\\.");
        NbtBase<?> base = compound;
        for (String key : keys) {
            if (base.getType() != NbtType.TAG_COMPOUND) return null;
            base = ((NbtCompound) base).getValue(key);
            if (base == null) return null;
        }
        return base;
    }

    public static boolean putDeep(NbtCompound compound, String path, NbtBase<?> value) {
        String[] keys = path.split("\\.");
        NbtBase<?> base = compound;
        for (int i = 0; i < keys.length - 1; i++) {
            if (base.getType() != NbtType.TAG_COMPOUND) return false;
            NbtCompound preserve = ((NbtCompound) base);
            base = ((NbtCompound) base).getValue(keys[i]);
            if (base == null) {
                base = NbtFactory.ofCompound("");
                preserve.put(keys[i], base);
                base = preserve.getValue(keys[i]);
            }
        }
        if (base.getType() != NbtType.TAG_COMPOUND) return false;
        ((NbtCompound) base).put(keys[keys.length - 1], value);
        return true;
    }

    public static String dumpItemNbt(ItemStack item, int indent) {
        NbtCompound tag = (NbtCompound) NbtFactory.fromItemTag(MinecraftReflection.getBukkitItemStack(item));
        StringBuilder sb = new StringBuilder();
        dumpCompound(indent, tag, sb);
        return sb.toString();
    }

    public static void printValue(NbtBase<Object> value, StringBuilder writer) {
        switch (value.getType()) {
            case TAG_BYTE:
                writer.append("<BYTE> ");
                writer.append(((byte) value.getValue()));
                break;
            case TAG_SHORT:
                writer.append("<SHORT> ");
                writer.append(((short) value.getValue()));
                break;
            case TAG_INT:
                writer.append("<INT> ");
                writer.append(((int) value.getValue()));
                break;
            case TAG_LONG:
                writer.append("<LONG> ");
                writer.append(((long) value.getValue()));
                break;
            case TAG_FLOAT:
                writer.append("<FLOAT> ");
                writer.append(((float) value.getValue()));
                break;
            case TAG_DOUBLE:
                writer.append("<DOUBLE> ");
                writer.append(((double) value.getValue()));
                break;
            case TAG_BYTE_ARRAY:
                writer.append("<BYTE ARRAY> ");
                writer.append(Arrays.toString(((byte[]) value.getValue())));
                break;
            case TAG_INT_ARRAY:
                writer.append("<INT ARRAY> ");
                writer.append(Arrays.toString(((int[]) value.getValue())));
                break;
            case TAG_STRING:
                writer.append("<STRING> ");
                writer.append(value.getValue());
                break;
            case TAG_LONG_ARRAY:
                writer.append("<LONG ARRAY> ");
                writer.append(Arrays.toString(((long[]) value.getValue())));
                break;
        }
    }

    public static void dumpList(int indent, NbtList<Object> list, StringBuilder writer) {
        writer.append("<LIST>\n");
        for (NbtBase<Object> base : list.getValue()) {
            printIndent(indent, writer);
            writer.append("- ");
            if (base.getType() == NbtType.TAG_COMPOUND) {
                dumpCompound(indent + 1, (NbtCompound) ((NbtBase<?>) base), writer);
            } else if (base.getType() == NbtType.TAG_LIST) {
                //noinspection unchecked
                dumpList(indent + 1, ((NbtList<Object>) (NbtBase<?>) base), writer);
            } else {
                printValue(base, writer);
            }
            writer.append("\n");
        }
        writer.deleteCharAt(writer.length() - 1);
    }

    public static void dumpCompound(int indent, NbtCompound compound, StringBuilder writer) {
        writer.append("<COMPOUND>\n");
        for (String compoundKey : compound.getKeys()) {
            printIndent(indent, writer);
            writer.append(compoundKey);
            writer.append(": ");
            NbtBase<Object> value = compound.getValue(compoundKey);
            NbtType type = value.getType();
            if (type == NbtType.TAG_COMPOUND) {
                dumpCompound(indent + 1, compound.getCompound(compoundKey), writer);
            } else if (type == NbtType.TAG_LIST) {
                dumpList(indent + 1, compound.getList(compoundKey), writer);
            } else {
                printValue(value, writer);
            }
            writer.append("\n");
        }
        writer.deleteCharAt(writer.length() - 1);
    }

    private static void printIndent(int indent, StringBuilder writer) {
        for (int i = 0; i < indent; i++) {
            writer.append("  ");
        }
    }

    public static NbtBase<?> parseFromDump(String desc) {
        return parseFromDump(new StringBuilder(desc), 0);
    }

    private static NbtBase<?> parseFromDump(StringBuilder sb, int indent) {
        skipConstant(sb, "<");
        StringBuilder type = new StringBuilder();
        while (sb.charAt(0) != '>') {
            type.append(sb.charAt(0));
            sb.deleteCharAt(0);
        }
        skipConstant(sb, ">");
        if (type.toString().equals("COMPOUND")) {
            return parseCompound(sb, indent);
        } else if (type.toString().equals("LIST")) {
            return parseList(sb, indent);
        } else {
            return parseValue(sb, type.toString());
        }
    }

    private final static Map<String, Function<String, NbtBase<?>>> PRIMITIVE_EXTRACTORS;

    static {
        Map<String, Function<String, NbtBase<?>>> extractors = new HashMap<>();

        for (NbtType t : NbtType.values()) {
            switch (t) {
                case TAG_BYTE:
                    extractors.put("BYTE", s -> NbtFactory.of("", Byte.parseByte(s)));
                    break;
                case TAG_SHORT:
                    extractors.put("SHORT", s -> NbtFactory.of("", Short.parseShort(s)));
                    break;
                case TAG_INT:
                    extractors.put("INT", s -> NbtFactory.of("", Integer.parseInt(s)));
                    break;
                case TAG_LONG:
                    extractors.put("LONG", s -> NbtFactory.of("", Long.parseLong(s)));
                    break;
                case TAG_FLOAT:
                    extractors.put("FLOAT", s -> NbtFactory.of("", Float.parseFloat(s)));
                    break;
                case TAG_DOUBLE:
                    extractors.put("DOUBLE", s -> NbtFactory.of("", Double.parseDouble(s)));
                    break;
                case TAG_BYTE_ARRAY:
                    extractors.put("BYTE ARRAY", s -> {
                        List<Byte> l = new LinkedList<>();
                        parseArray(s, Byte::parseByte, l::add);
                        byte[] arr = new byte[l.size()];
                        for (int i = 0; i < l.size(); i++) arr[i] = l.get(i);
                        return NbtFactory.of("", arr);
                    });
                    break;
                case TAG_INT_ARRAY:
                    extractors.put("INT ARRAY", s -> {
                        List<Integer> l = new LinkedList<>();
                        parseArray(s, Integer::parseInt, l::add);
                        int[] arr = new int[l.size()];
                        for (int i = 0; i < l.size(); i++) arr[i] = l.get(i);
                        return NbtFactory.of("", arr);
                    });
                    break;
                case TAG_STRING:
                    extractors.put("STRING", s -> NbtFactory.of("", s));
                    break;
                case TAG_LONG_ARRAY:
                    // not supported
                    break;
            }
        }
        PRIMITIVE_EXTRACTORS = Collections.unmodifiableMap(extractors);
    }

    private static <T> void parseArray(String array, Function<String, T> parser, Consumer<T> acceptor) {
        StringTokenizer tokenizer = new StringTokenizer(array, "[, ]");
        while (tokenizer.hasMoreTokens()) {
            acceptor.accept(parser.apply(tokenizer.nextToken()));
        }
    }

    private static NbtBase<?> parseValue(StringBuilder sb, String type) {
        skipConstant(sb, " ");
        StringBuilder a = new StringBuilder();
        while (sb.length() > 0 && sb.charAt(0) != '\n') {
            a.append(sb.charAt(0));
            sb.deleteCharAt(0);
        }
        skipLn(sb);
        Function<String, NbtBase<?>> extractor = PRIMITIVE_EXTRACTORS.get(type);
        if (extractor == null) throw new IllegalArgumentException("unknown tag type: " + type);
        return extractor.apply(a.toString());
    }

    public static NbtCompound parseCompound(StringBuilder sb, int indent) {
        skipLn(sb);
        NbtCompound compound = NbtFactory.ofCompound("");
        while (skipIndents(sb, indent)) {
            int c = sb.indexOf(":");
            if (c == -1)
                throw new IllegalStateException("corrupted compound");
            String key = sb.substring(0, c);
            sb.delete(0, c + 2);
            compound.put(key, parseFromDump(sb, indent + 1));
        }
        skipLn(sb);
        return compound;
    }

    public static NbtList<?> parseList(StringBuilder sb, int indent) {
        skipLn(sb);
        NbtList<Object> list = NbtFactory.ofList("");
        while (skipIndents(sb, indent)) {
            skipConstant(sb, "- ");
            //noinspection unchecked
            list.add((NbtBase<Object>) parseFromDump(sb, indent + 1));
        }
        skipLn(sb);
        return list;
    }

    private static void skipLn(StringBuilder sb) {
        if (sb.length() != 0 && sb.charAt(0) == '\n') sb.deleteCharAt(0);
    }

    private static boolean skipIndents(StringBuilder sb, int indents) {
        if (sb.length() == 0) return false;
        for (int i = 0; i < indents * 2; i++) {
            if (sb.charAt(i) != ' ') return false;
        }
        sb.delete(0, indents * 2);
        return true;
    }

    private static void skipConstant(StringBuilder sb, String constant) {
        for (int i = 0; i < constant.length(); i++) {
            if (sb.charAt(0) != constant.charAt(i))
                throw new IllegalStateException("expecting constant: " + constant);
            sb.deleteCharAt(0);
        }
    }
}
