package com.hugoserve.metalbroker.service;

import java.time.LocalDate;

public interface DbHistoryAdminService {

    String upsertOne(String json);

    String upsertBulk(String json);

    String deleteOne(String metalCode, LocalDate dayUtc);

    String deleteRange(String metalCode, LocalDate fromDay, LocalDate toDay);
}

