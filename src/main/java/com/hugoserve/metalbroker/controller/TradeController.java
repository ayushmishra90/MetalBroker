package com.hugoserve.metalbroker.controller;

import com.hugoserve.metalbroker.proto.MetalRatesProto;
import com.hugoserve.metalbroker.service.TradeService;
import com.hugoserve.metalbroker.utils.ProtoJson;
import com.hugoserve.metalbroker.utils.ProtoJsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<String> trade(@RequestBody String body) {
        MetalRatesProto.TradeRequest req =
                ProtoJsonParser.parse(body, MetalRatesProto.TradeRequest.newBuilder()).build();

        return ResponseEntity.ok(
                ProtoJson.print(tradeService.trade(req))
        );
    }

    @GetMapping("/history")
    public ResponseEntity<String> history() {
        return ResponseEntity.ok(tradeService.tradeHistory());
    }
}
