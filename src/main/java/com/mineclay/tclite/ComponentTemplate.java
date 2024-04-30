package com.mineclay.tclite;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class ComponentTemplate {
    private final List<Object> list = new ArrayList<>();

    public ComponentTemplate(String template) {
        this.list.add(template);
    }

    public void replace(String target, BaseComponent[] components) {
        ListIterator<Object> listIterator = list.listIterator();

        while (listIterator.hasNext()) {
            Object obj = listIterator.next();
            if (obj == null) continue;

            if (obj instanceof String) {
                String str = (String) obj;
                int idx = str.indexOf(target);
                if (idx == -1) continue;

                String pre = str.substring(0, idx);
                listIterator.set(pre);
                for (BaseComponent component : components) {
                    listIterator.add(component);
                }
                String post = str.substring(idx + target.length());
                if (!post.isEmpty()) listIterator.add(post);
            }
        }
    }

    public BaseComponent[] create() {
        list.removeIf(Objects::isNull);
        List<BaseComponent> finalList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof String) {
                finalList.add(new TextComponent(obj.toString()));
            } else {
                finalList.add(((BaseComponent) obj));
            }
        }
        return finalList.toArray(new BaseComponent[finalList.size()]);
    }
}
