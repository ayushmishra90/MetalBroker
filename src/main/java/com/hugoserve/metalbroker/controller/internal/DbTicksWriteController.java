package com.hugoserve.metalbroker.controller.internal;

import com.hugoserve.metalbroker.service.DbTicksAdminService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/db/admin/ticks")
public class DbTicksWriteController {

    private final DbTicksAdminService service;

    public DbTicksWriteController(DbTicksAdminService service) {
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
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> deleteById(@PathVariable long id) {
        return ResponseEntity.ok(service.deleteById(id));
    }

    @DeleteMapping(
            value = "/delete-range",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> deleteRange(
            @RequestParam String metal,
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return ResponseEntity.ok(service.deleteRange(metal, from, to));
    }
}