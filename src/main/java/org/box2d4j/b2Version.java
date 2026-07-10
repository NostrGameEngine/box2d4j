package org.box2d4j;

/** Version numbering scheme. Mirrors Box2D's {@code b2Version}. */
public class b2Version {
    public int major;
    public int minor;
    public int revision;

    public b2Version() {
    }

    public b2Version(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }
}
