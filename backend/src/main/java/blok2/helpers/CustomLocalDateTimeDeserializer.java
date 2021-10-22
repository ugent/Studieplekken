package blok2.helpers;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends LocalDateTimeDeserializer {
    CustomLocalDateTimeDeserializer() {
        super(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
