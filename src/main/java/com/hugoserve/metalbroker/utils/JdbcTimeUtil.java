package com.hugoserve.metalbroker.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;
import java.sql.Timestamp;

public final class JdbcTimeUtil {

    private JdbcTimeUtil() {}

    public static Instant getInstantUtc(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(
                column,
                Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        );
        return ts == null ? null : ts.toInstant();
    }

    public static LocalDate toLocalDate(Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }
}
