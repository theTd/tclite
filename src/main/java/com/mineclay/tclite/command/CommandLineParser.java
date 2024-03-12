package com.mineclay.tclite.command;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Consumer;

public class CommandLineParser {

    Map<String, String> value = new HashMap<>();

    public CommandLineParser() {
    }

    private CommandLineParser(Map<String, String> value) {
        this.value = value;
    }

    /**
     * copilot generated
     */
    public List<String> complete(String[] args) {
        Map<String, String> value = new HashMap<>(this.value);
        value.entrySet().forEach(e -> e.setValue(null));

        List<String> list = new ArrayList<>();
        Iterator<String> ite = Arrays.stream(args).iterator();
        Consumer<String> set = null;
        StringBuilder buffer = new StringBuilder();
        while (ite.hasNext()) {
            String next = ite.next();
            if (next.startsWith("--")) {
                String key = next.substring(2);
                if (value.containsKey(key)) {
                    if (set != null) {
                        set.accept(buffer + "");
                        buffer = new StringBuilder();
                    }
                    set = s -> value.put(key, s);
                    continue;
                }
            }
            buffer.append(buffer.length() == 0 ? "" : " ").append(next);
        }
        if (set != null) set.accept(buffer + "");
        for (Map.Entry<String, String> entry : value.entrySet()) {
            if (entry.getValue() == null) list.add("--" + entry.getKey());
        }
        return list;
    }

    public CommandLineParser accepts(String name) {
        value.put(name, null);
        return this;
    }

    public CommandLineParser accepts(String name, String description) {
        value.put(name, description);
        return this;
    }

    public void showHelp(StringWriter writer) {
        writer.write("Usage: \n");
        value.forEach((k, v) -> writer.write("--" + k + " " + (v == null ? "" : v) + "\n"));
    }

    public CommandLineParser parse(String[] args) {
        Map<String, String> container = new LinkedHashMap<>();
        value.forEach((k, v) -> container.put(k, null));
        CommandLineParser p = this;
        return new CommandLineParser(container) {
            @Override
            public void showHelp(StringWriter writer) {
                p.showHelp(writer);
            }
        }._parse(args);
    }

    CommandLineParser _parse(String[] args) {
        Iterator<String> ite = Arrays.stream(args).iterator();
        Consumer<String> set = null;
        StringBuilder buffer = new StringBuilder();
        while (ite.hasNext()) {
            String next = ite.next();
            if (next.startsWith("--")) {
                String key = next.substring(2);
                if (value.containsKey(key)) {
                    if (set != null) {
                        set.accept(buffer + "");
                        buffer = new StringBuilder();
                    }
                    set = s -> value.put(key, s);
                    continue;
                }
            }
            buffer.append(buffer.length() == 0 ? "" : " ").append(next);
        }
        if (set != null) set.accept(buffer + "");
        return this;
    }

    public String valueOf(String name) {
        return value.get(name);
    }

    public boolean has(String name) {
        return value.containsKey(name);
    }
}
