package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.service.InstrumentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    // @GetMapping("/{id}")
    // public Instrument getById(@PathVariable Long id) {
    //     return instrumentService.findById(id);
    // }

    @GetMapping("/{symbol}")
    public Instrument getBySymbol(@PathVariable String symbol) {
        return instrumentService.findBySymbol(symbol);
    }
}