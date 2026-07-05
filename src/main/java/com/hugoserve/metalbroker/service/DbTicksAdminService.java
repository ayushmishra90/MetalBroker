package com.hugoserve.metalbroker.service;

import java.time.Instant;

public interface DbTicksAdminService {

    String upsertOne(String json);

    String upsertBulk(String json);

    String deleteById(long id);

    String deleteRange(String metalCode, Instant from, Instant to);
}