package mb.pie.api;

public class StringUtil {
    public static String toShortString(String str, int maxLength) {
        str = str.replace("\r", "\\r").replace("\n", "\\n");
        if(str.length() > maxLength) {
            return str.substring(0, maxLength - 1) + "...";
        } else {
            return str;
        }
    }
}
