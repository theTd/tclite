package com.mineclay.tclite;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TagTemplate {
    private final Set<String> requiredTags;
    private final Set<String> filteredTags;
    private final Set<String> tags;

    private TagTemplate(Set<String> requiredTags, Set<String> filteredTags) {
        this.requiredTags = requiredTags;
        this.filteredTags = filteredTags;

        this.tags = new HashSet<>();
        this.tags.addAll(requiredTags);
        this.tags.addAll(filteredTags.stream().map(s -> "!" + s).collect(Collectors.toSet()));
    }

    public Set<String> getRequiredTags() {
        return Collections.unmodifiableSet(requiredTags);
    }

    public Set<String> getFilteredTags() {
        return Collections.unmodifiableSet(filteredTags);
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public boolean check(Set<String> objectTags) {
        if (!objectTags.containsAll(requiredTags)) return false;
        for (String f : filteredTags) {
            if (objectTags.contains(f)) return false;
        }
        return true;
    }

    public static TagTemplate create(Collection<String> tags) {
        Set<String> requiredTags = new HashSet<>();
        Set<String> filteredTags = new HashSet<>();

        for (String t : tags) {
            int type = checkTag(t);
            if (checkTag(t) == 0) throw new RuntimeException(String.format("tag %s is not valid", t));
            if (type == 1) requiredTags.add(t);
            else filteredTags.add(t.substring(1));
        }

        return new TagTemplate(requiredTags, filteredTags);
    }

    private final static Pattern TAG_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]*$");

    /**
     * @param tag to check
     * @return -1 if filtered, 0 if error, 1 if required
     */
    public static int checkTag(String tag) {
        if (tag == null || tag.isEmpty()) return 0;
        if (tag.startsWith("!")) {
            tag = tag.substring(1);
            return TAG_PATTERN.matcher(tag).find() ? -1 : 0;
        } else {
            return TAG_PATTERN.matcher(tag).find() ? 1 : 0;
        }
    }
}
