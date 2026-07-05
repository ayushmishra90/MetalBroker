package com.hugoserve.metalbroker.service;

import com.hugoserve.metalbroker.proto.MetalRatesProto;

import java.time.Instant;

public interface DbRateAnalysisService {

    String analyze(
            MetalRatesProto.Metal metal,
            Instant from,
            Instant to
    );
}
