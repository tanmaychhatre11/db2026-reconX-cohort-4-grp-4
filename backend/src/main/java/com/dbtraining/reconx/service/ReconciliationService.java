package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.observability.ReconMetrics;
// import com.dbtraining.reconx.repository.ReconResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReconciliationService {

    private final ReconciliationEngine engine;
    private final ReconMetrics metrics;
    // private final ReconResultRepository repo;

    public ReconciliationService(ReconciliationEngine engine, ReconMetrics metrics) {
        // ReconResultRepository repo
        this.engine = engine;
        this.metrics = metrics;
        // this.repo = repo;
    }

    public List<ReconResult> runRecon(List<TradeType> internal,
                                      List<TradeType> external,
                                      ReconciliationRule rule) {

        // List<ReconResult> results =
        //         engine.reconcile(internal, external, rule);

        // results.forEach(repo::save);

        return metrics.reconciliationTimer().record(() -> engine.reconcile(internal, external, rule));
    }
}