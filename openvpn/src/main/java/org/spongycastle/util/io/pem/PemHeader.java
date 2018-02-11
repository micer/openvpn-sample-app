package org.spongycastle.util.io.pem;

public class PemHeader {
    private String name;
    private String value;

    public PemHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int hashCode() {
        return getHashCode(this.name) + 31 * getHashCode(this.value);
    }

    public boolean equals(Object o) {
        if (!(o instanceof PemHeader)) {
            return false;
        }

        PemHeader other = (PemHeader) o;

        return other == this || (isEqual(this.name, other.name) && isEqual(this.value, other.value));
    }

    private int getHashCode(String s) {
        if (s == null) {
            return 1;
        }

        return s.hashCode();
    }

    private boolean isEqual(String s1, String s2) {
        return s1.equals(s2) || s2 != null && s1.equals(s2);
    }
}
