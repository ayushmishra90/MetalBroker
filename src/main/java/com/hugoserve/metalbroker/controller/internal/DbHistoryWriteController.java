package com.hugoserve.metalbroker.controller.internal;

import com.hugoserve.metalbroker.service.DbHistoryAdminService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/db/admin/history")
public class DbHistoryWriteController {

    private final DbHistoryAdminService service;

    public DbHistoryWriteController(DbHistoryAdminService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/upsert-one",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> upsertOne(@RequestBody String json) {
        return ResponseEntity.ok(service.upsertOne(json));
    }

    @PostMapping(
            value = "/upsert-bulk",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> upsertBulk(@RequestBody String json) {
        return ResponseEntity.ok(service.upsertBulk(json));
    }

    @DeleteMapping(
            value = "/delete-one",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> deleteOne(
            @RequestParam String metal,
            @RequestParam LocalDate day) {
        return ResponseEntity.ok(service.deleteOne(metal, day));
    }

    @DeleteMapping(
            value = "/delete-range",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> deleteRange(
            @RequestParam String metal,
            @RequestParam LocalDate fromDay,
            @RequestParam LocalDate toDay) {
        return ResponseEntity.ok(service.deleteRange(metal, fromDay, toDay));
    }
}
