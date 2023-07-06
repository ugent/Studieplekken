package blok2.extensions.helpers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64String {

    public static String base64Encode(String str) {
        return new String(Base64.getEncoder().encode(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static String base64Decode(String str) {
        return new String(Base64.getDecoder().decode(str));
    }
}
