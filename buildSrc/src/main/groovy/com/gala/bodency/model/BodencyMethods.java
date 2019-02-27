package com.gala.bodency.model;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class BodencyMethods extends TreeMap<String, Long> {

    private static final Comparator<String> ORDER = new Comparator<String>() {
        public int compare(String a, String b) {
            return a.compareTo(b);
        }
    };

    private long totalMethodCount = 0;

    public BodencyMethods() {
        super(ORDER);
    }

    public void increaseMethod(String name) {
        Long count = get(name);
        if (count == null) {
            put(name, 1L);
        } else {
            put(name, ++count);
        }
    }

    public long getClassOrPackageMethodCount(String name) {
        Long count = get(name);
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }

    public long getTotalMethodCount() {
        return totalMethodCount;
    }

    public void setTotalMethodCount(long totalMethodCount) {
        this.totalMethodCount = totalMethodCount;
    }

    public Iterator<String> iterator() {
        return iterator("");
    }

    public Iterator<String> iterator(final String divider) {
        final Iterator<Map.Entry<String, Long>> iterator = entrySet().iterator();
        return new Iterator<String>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public String next() {
                Map.Entry entry = iterator.next();
                StringBuilder sb = new StringBuilder((String) entry.getKey());
                sb.append(divider);
                sb.append(entry.getValue());
                return sb.toString();
            }
        };
    }
}
