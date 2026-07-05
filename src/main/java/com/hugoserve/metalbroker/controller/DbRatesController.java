package com.hugoserve.metalbroker.controller;

import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.DbRatesReadService;
import com.hugoserve.metalbroker.service.DbRateAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/db/rates")
public class DbRatesController {

    private final DbRatesReadService service;
    private final DbRateAnalysisService dbRateAnalysisService;

    public DbRatesController(
            DbRatesReadService service,
            DbRateAnalysisService dbRateAnalysisService
    ) {
        this.service = service;
        this.dbRateAnalysisService = dbRateAnalysisService;
    }

    @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> latest(@RequestParam String metal) {
        return ResponseEntity.ok(service.latest(metal));
    }

    @GetMapping(value = "/intraday", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> intraday(
            @RequestParam String metal,
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return ResponseEntity.ok(service.intraday(metal, from, to));
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> history(
            @RequestParam String metal,
            @RequestParam LocalDate fromDay,
            @RequestParam LocalDate toDay
    ) {
        return ResponseEntity.ok(service.history(metal, fromDay, toDay));
    }

    @GetMapping(value = "/intraday/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> intradayPage(
            @RequestParam String metal,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        return ResponseEntity.ok(
                service.intradayPage(metal, from, to, page, size, sort, dir)
        );
    }

    @GetMapping(value = "/history/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> historyPage(
            @RequestParam String metal,
            @RequestParam LocalDate fromDay,
            @RequestParam LocalDate toDay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        return ResponseEntity.ok(
                service.historyPage(metal, fromDay, toDay, page, size, sort, dir)
        );
    }

    @GetMapping("/intraday/analysis")
    public ResponseEntity<String> analyze(
            @RequestParam MetalRatesProto.Metal metal,
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return ResponseEntity.ok(dbRateAnalysisService.analyze(metal, from, to));
    }
}

