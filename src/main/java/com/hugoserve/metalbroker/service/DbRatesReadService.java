package com.hugoserve.metalbroker.service;

import java.time.Instant;
import java.time.LocalDate;

public interface DbRatesReadService {

    String latest(String metal);

    String intraday(String metal, Instant from, Instant to);

    String history(String metal, LocalDate fromDay, LocalDate toDay);

    String intradayPage(
            String metal,
            Instant from,
            Instant to,
            int page,
            int size,
            String sort,
            String dir
    );

    String historyPage(
            String metal,
            LocalDate fromDay,
            LocalDate toDay,
            int page,
            int size,
            String sort,
            String dir
    );
}
