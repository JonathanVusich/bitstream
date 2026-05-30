package dev.javax.bitstream;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

final class Utils {

    private static final VarHandle BE_BYTES = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN)
            .withInvokeExactBehavior();
    private static final VarHandle LE_BYTES = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN)
            .withInvokeExactBehavior();

    static byte[] toBeBytes(long val) {
        final var array = new byte[8];
        BE_BYTES.set(array, 0, val);
        return array;
    }

    static byte[] toLeBytes(long val) {
        final var array = new byte[8];
        LE_BYTES.set(array, 0, val);
        return array;
    }

    static long fromBeBytes(byte[] bytes) {
        return (long) BE_BYTES.get(bytes, 0);
    }

    static long fromLeBytes(byte[] bytes) {
        return (long) LE_BYTES.get(bytes, 0);
    }

    static void zero(byte[] bytes) {
        LE_BYTES.set(bytes, 0, 0L);
    }
}
