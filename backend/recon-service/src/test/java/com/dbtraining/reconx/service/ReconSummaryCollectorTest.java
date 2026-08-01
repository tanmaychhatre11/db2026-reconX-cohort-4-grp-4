package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.ReconSummary;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV038 — ReconSummaryCollector: total/matched/broken counts,
 * verified serial vs parallel for combiner correctness.
 */
class ReconSummaryCollectorTest {

    @Test
    void collectsCountsAcrossMatchedAndBrokenResults() {
        // given
        List<ReconResult> results = List.of(
                ReconResult.matched("TRD-1"),
                ReconResult.matched("TRD-2"),
                ReconResult.breakResult("TRD-3", "VALUE_MISMATCH", "price mismatch"),
                ReconResult.breakResult("TRD-4", "MISSING_EXTERNAL", "no external trade")
        );

        // when
        ReconSummary summary = results.stream().collect(new ReconSummaryCollector());

        // then
        assertThat(summary.total()).isEqualTo(4);
        assertThat(summary.matched()).isEqualTo(2);
        assertThat(summary.broken()).isEqualTo(2);
    }

    @Test
    void emptyStreamProducesZeroedSummary() {
        ReconSummary summary = List.<ReconResult>of().stream().collect(new ReconSummaryCollector());

        assertThat(summary.total()).isZero();
        assertThat(summary.matched()).isZero();
        assertThat(summary.broken()).isZero();
    }

    @Test
    void serialAndParallelStreamsProduceIdenticalSummary_10kResults() {
        // given — 10k results, alternating matched/broken so both branches exercise the combiner
        List<ReconResult> results = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            results.add(i % 2 == 0
                    ? ReconResult.matched("TRD-" + i)
                    : ReconResult.breakResult("TRD-" + i, "VALUE_MISMATCH", "mismatch " + i));
        }

        // when
        ReconSummary serial = results.stream().collect(new ReconSummaryCollector());
        ReconSummary parallel = results.parallelStream().collect(new ReconSummaryCollector());

        // then
        assertThat(parallel).isEqualTo(serial);
        assertThat(serial.total()).isEqualTo(10_000);
        assertThat(serial.matched()).isEqualTo(5_000);
        assertThat(serial.broken()).isEqualTo(5_000);
    }
}