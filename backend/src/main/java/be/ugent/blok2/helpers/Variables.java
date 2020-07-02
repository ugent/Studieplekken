package be.ugent.blok2.helpers;

import be.ugent.blok2.helpers.date.CustomDate;

public class Variables {
    public static final int thresholdPenaltyPoints = Integer.parseInt(Resources.blokatugentConf.getString("maxAllowedPoints"));
    public static final CustomDate maxCancelDate = CustomDate.parseString(Resources.blokatugentConf.getString("maxCancelDate"));
}
