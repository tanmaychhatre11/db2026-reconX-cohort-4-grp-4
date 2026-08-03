package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.entity.Trade;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TradeStreamService {

    private static final Long TIMEOUT = 30L * 60 * 1000; // 30 min

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.add(emitter);

        // Flush the response immediately so EventSource can report an open connection.
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return emitter;
    }

    public void broadcast(Trade trade) {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("trade")
                        .data(trade, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}
