package com.mineclay.tclite;

import java.util.*;

public class RandomUtil {
    public static <T> T pickObjectByWeightMap(Map<T, Integer> weightMap) {
        if (weightMap.isEmpty()) return null;
        int sum = 0;
        for (int i : weightMap.values()) {
            sum += i;
        }
        double rand = Math.random() * sum;

        double temp = 0;
        Iterator<Map.Entry<T, Integer>> ite = weightMap.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<T, Integer> en = ite.next();
            if ((temp = temp + en.getValue()) >= rand) return en.getKey();
            if (!ite.hasNext()) return en.getKey();
        }
        throw new RuntimeException("impossible reaching here!");
    }

    private static <T> List<T> pickElementsFromTemplate(ElementTemplate<T> template) {
        Integer groupAmount = pickObjectByWeightMap(template.groupAmountWeightMap);
        if (groupAmount == null) return new ArrayList<>();

        int currentGroupAmount = 0;
        Map<ElementGroup<T>, Integer> groupAmountMap = new HashMap<>();

        // fill freely, max occur cautious
        Map<ElementGroup<T>, Integer> itemGroupWeightMap = template.getElementGroupWeightMap();
        while (currentGroupAmount < groupAmount && itemGroupWeightMap.size() != 0) {
            ElementGroup<T> group = pickObjectByWeightMap(itemGroupWeightMap);
            if (group == null) break;

            Integer thisGroupAmount = groupAmountMap.get(group);
            if (thisGroupAmount == null) thisGroupAmount = 0;
            if (group.maxOccur != -1 && group.maxOccur <= thisGroupAmount) {
                itemGroupWeightMap.remove(group);
                continue;
            }

            groupAmountMap.put(group, thisGroupAmount + 1);
            currentGroupAmount++;
        }

        // min occur fix, remove other element with inverted weight map
        Map<ElementGroup<T>, Integer> invertedItemGroupWeightMap = template.getInvertedElementGroupWeightMap();

        Map<ElementGroup<T>, Integer> minOccurMap = new HashMap<>();
        for (ElementGroup<T> group : template.groups) {
            if (group.minOccur != -1) minOccurMap.put(group, group.minOccur);
        }

        for (ElementGroup<T> group : minOccurMap.keySet()) {
            Integer amount = groupAmountMap.get(group);
            if (amount == null) amount = 0;
            int leak = group.minOccur - amount;
            for (int i = 0; i < leak; i++) {
                amount = amount + 1;
                groupAmountMap.put(group, amount);
                Map<ElementGroup<T>, Integer> m = new HashMap<>(invertedItemGroupWeightMap);
                // remove groups with least amount or not exists in groupAmountMap OR is it self
                m.entrySet().removeIf(en -> {
                    if (en.getKey() == group) return true;

                    Integer minOccur = minOccurMap.get(en.getKey());
                    Integer currentAmount = groupAmountMap.get(en.getKey());
                    return currentAmount == null || (minOccur != null && currentAmount <= minOccur);
                });

                // decrease a random picked element group
                ElementGroup<T> toDecrease = pickObjectByWeightMap(m);
                if (toDecrease != null) {
                    groupAmountMap.put(toDecrease, groupAmountMap.get(toDecrease) - 1);
                }
            }
        }

        List<T> output = new ArrayList<>();

        for (Map.Entry<ElementGroup<T>, Integer> en : groupAmountMap.entrySet()) {
            for (int i = 0; i < en.getValue(); i++) {
                T element = en.getKey().pickElementByWeight();
                if (element == null) continue;
                output.add(element);
            }
        }
        return output;
    }

    public static class ElementTemplate<T> {
        private Map<Integer, Integer> groupAmountWeightMap = new HashMap<>();
        private Set<ElementGroup<T>> groups = new HashSet<>();

        public Map<Integer, Integer> getGroupAmountWeightMap() {
            return groupAmountWeightMap;
        }

        public void setGroupAmountWeight(int groupAmount, int weight) {
            Map<Integer, Integer> map = new HashMap<>(groupAmountWeightMap);
            map.put(groupAmount, weight);
            this.groupAmountWeightMap = Collections.unmodifiableMap(map);
        }

        public Set<ElementGroup<T>> getGroups() {
            return groups;
        }

        public void addGroup(ElementGroup<T> group) {
            Set<ElementGroup<T>> groups = new HashSet<>(this.groups);
            groups.add(group);
            this.groups = Collections.unmodifiableSet(groups);
        }

        public List<T> pick() {
            return pickElementsFromTemplate(this);
        }

        Map<ElementGroup<T>, Integer> getElementGroupWeightMap() {
            Map<ElementGroup<T>, Integer> map = new HashMap<>();
            for (ElementGroup<T> group : groups) {
                map.put(group, group.weight);
            }
            return map;
        }

        Map<ElementGroup<T>, Integer> getInvertedElementGroupWeightMap() {
            Map<ElementGroup<T>, Integer> map = new HashMap<>();
            for (ElementGroup<T> group : groups) {
                map.put(group, group.weight);
            }
            Map<ElementGroup<T>, Integer> finalMap = new HashMap<>();

            while (!map.isEmpty()) {
                if (map.size() == 1) {
                    Map.Entry<ElementGroup<T>, Integer> lastEntry = map.entrySet().iterator().next();
                    finalMap.put(lastEntry.getKey(), lastEntry.getValue());
                    break;
                }

                // put with highest and lowest swapped
                ElementGroup<T> highest = null;
                int highestWeight = -1;
                ElementGroup<T> lowest = null;
                int lowestWeight = -1;

                for (Map.Entry<ElementGroup<T>, Integer> en : map.entrySet()) {
                    if (highest == null || highestWeight < en.getValue()) {
                        highest = en.getKey();
                        highestWeight = en.getValue();
                    }

                    if (lowest == null || lowestWeight >= en.getValue()) {
                        lowest = en.getKey();
                        lowestWeight = en.getValue();
                    }
                }
                finalMap.put(highest, lowestWeight);
                finalMap.put(lowest, highestWeight);
                map.remove(highest);
                map.remove(lowest);
            }
            return finalMap;
        }
    }

    public static class ElementGroup<T> {
        private int weight = 1;
        private int maxOccur = -1;
        private int minOccur = -1;
        private Map<T, Integer> elements = new HashMap<>();

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public void setMaxOccur(int maxOccur) {
            this.maxOccur = maxOccur;
        }

        public void setMinOccur(int minOccur) {
            this.minOccur = minOccur;
        }

        public void addElement(T element, int weight) {
            Map<T, Integer> elements = new HashMap<>(this.elements);
            elements.put(element, weight);
            this.elements = Collections.unmodifiableMap(elements);
        }

        T pickElementByWeight() {
            return pickObjectByWeightMap(elements);
        }
    }
}
