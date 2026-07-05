package com.hugoserve.metalbroker.controller;

import com.hugoserve.metalbroker.service.DbMetalReadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/db/metals")
public class DbMetalController {

    private final DbMetalReadService service;

    public DbMetalController(DbMetalReadService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping(
            value = "/{code}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }
}

