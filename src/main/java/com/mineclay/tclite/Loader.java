package com.mineclay.tclite;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Loader {
    protected final List<Loadable> list = new ArrayList<>();
    protected final HashSet<Loadable> enabled = new HashSet<>();

    public void loadAll() throws Exception {
        for (Loadable m : list) {
            if (enabled.contains(m)) continue;
            m.load();
            enabled.add(m);
        }
    }

    public void unloadAll(Logger logger) {
        ListIterator<Loadable> listIterator = list.listIterator(list.size());
        while (listIterator.hasPrevious()) {
            Loadable m = listIterator.previous();
            try {
                m.unload();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "unloading module " + m.getClass().getSimpleName(), e);
            } finally {
                enabled.remove(m);
            }
        }
    }

    public <M extends Loadable> M get(Class<M> clz) {
        for (Loadable m : list)
            if (m.getClass().equals(clz)) //noinspection unchecked
                return (M) m;
        return null;
    }

    public void add(Loadable loadable) {
        Preconditions.checkNotNull(loadable, "module cannot be null");
        list.add(loadable);
    }

    public interface Loadable {
        void load() throws Exception;

        default void unload() {}
    }
}
