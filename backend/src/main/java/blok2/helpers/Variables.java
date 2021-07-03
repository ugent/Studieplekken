package blok2.helpers;

import java.time.LocalDateTime;

public class Variables {
    public static final int thresholdPenaltyPoints = Integer.parseInt(Resources.blokatugentConf.getString("maxAllowedPoints"));
    public static final LocalDateTime maxCancelDate = LocalDateTime.parse(Resources.blokatugentConf.getString("maxCancelDate"));
}
