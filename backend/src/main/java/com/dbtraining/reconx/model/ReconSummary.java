package com.dbtraining.reconx.model;

/**
 * TICKET-ADV038 — Summary result produced by ReconSummaryCollector.
 * Holds total, matched, and broken reconciliation counts.
 */
public record ReconSummary(
        long total,
        long matched,
        long broken
) {

    /**
     * Returns an empty summary with all counts set to zero.
     */
    public static ReconSummary empty() {
        return new ReconSummary(0, 0, 0);
    }
}