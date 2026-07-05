package com.hugoserve.metalbroker.domain.dao;

public final class SQLQueryConstant {

    private SQLQueryConstant() {}

    public static final String UPSERT_METAL_DAILY_HISTORY = """
        INSERT INTO metal_daily_history
        (metal_id, day_utc, open, high, low, close, ma50, ma200, source)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
          open = VALUES(open),
          high = VALUES(high),
          low = VALUES(low),
          close = VALUES(close),
          ma50 = VALUES(ma50),
          ma200 = VALUES(ma200),
          source = VALUES(source)
        """;

    public static final String FETCH_BETWEEN = """
        SELECT metal_id, day_utc, open, high, low, close, ma50, ma200, source, create_ts, update_ts
        FROM metal_daily_history
        WHERE metal_id = ?
          AND day_utc BETWEEN ? AND ?
        ORDER BY day_utc ASC
        """;

    public static final String FETCH_MAX_DAY = """
        SELECT MAX(day_utc) FROM metal_daily_history WHERE metal_id = ?
        """;
}

