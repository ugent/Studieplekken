package blok2.extensions.orm;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeSerializer extends LocalDateTimeSerializer {

    CustomLocalDateTimeSerializer() {
        super();
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator g, SerializerProvider provider) throws IOException {
        g.writeString(value.atOffset(ZoneOffset.of("Europe/Brussels")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
