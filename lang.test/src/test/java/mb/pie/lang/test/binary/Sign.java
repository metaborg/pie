package mb.pie.lang.test.binary;

public class Sign {
    private int value;

    public Sign(int value) {
        this.value = value;
    }

    public static Sign createSign(int value) {
        return new Sign(value);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Sign other = (Sign)o;
        return asSign() == other.asSign();
    }

    @Override
    public int hashCode() {
        return asSign();
    }

    private int asSign() {
        if(this.value == 0) {
            return 0;
        } else if(this.value > 0) {
            return 1;
        }
        return -1;
    }

    @Override public String toString() {
        return "Sign with value " + value;
    }
}
