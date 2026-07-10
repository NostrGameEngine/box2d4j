package org.box2d4j;

public class b2SetItem {
    public long key;
    public int hash;

    public b2SetItem() {
    }

    public b2SetItem(b2SetItem other) {
        this.key = other.key;
        this.hash = other.hash;
    }
}
