package com.hugoserve.metalbroker.controller.internal;

import com.hugoserve.metalbroker.service.DbMetalAdminService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/db/admin/metals")
public class DbMetalAdminController {

    private final DbMetalAdminService service;

    public DbMetalAdminController(DbMetalAdminService service) {
        this.service = service;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> create(@RequestBody String json) {
        return ResponseEntity.ok(service.create(json));
    }

    @PutMapping(
            value = "/{code}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> update(
            @PathVariable String code,
            @RequestBody String json
    ) {
        return ResponseEntity.ok(service.update(code, json));
    }

    @DeleteMapping(
            value = "/{code}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> deactivate(@PathVariable String code) {
        return ResponseEntity.ok(service.deactivate(code));
    }
}
