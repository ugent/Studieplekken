package blok2.helpers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.stereotype.Service;
import org.threeten.extra.YearWeek;

import java.io.IOException;

@Service
public class YearWeekDeserializer extends StdDeserializer<YearWeek> {

    public YearWeekDeserializer() {
        this(null);
    }

    public YearWeekDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public YearWeek deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        int year = (Integer) node.get("year").numberValue();
        int week = (Integer) node.get("week").numberValue();

        return YearWeek.of(year, week);
    }
}
