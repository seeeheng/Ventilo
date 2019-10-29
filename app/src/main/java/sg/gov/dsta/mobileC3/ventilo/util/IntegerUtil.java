package sg.gov.dsta.mobileC3.ventilo.util;

public class IntegerUtil {

    public static byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value};
    }

}
