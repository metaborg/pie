package mb.pie.store.lmdb;

import java.util.Arrays;

class SerializedAndHashed {
    final byte[] serialized;
    final byte[] hashed;

    SerializedAndHashed(byte[] serialized, byte[] hashed) {
        this.serialized = serialized;
        this.hashed = hashed;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SerializedAndHashed that = (SerializedAndHashed) o;
        return Arrays.equals(serialized, that.serialized) && Arrays.equals(hashed, that.hashed);
    }

    @Override public int hashCode() {
        int result = Arrays.hashCode(serialized);
        result = 31 * result + Arrays.hashCode(hashed);
        return result;
    }
}
