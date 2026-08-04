// useMemo for portfolio-value calc.
// useTradeStream live feed.
import React, { useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';
import { withErrorBoundary } from '@components/withErrorBoundary.jsx';
import LiveTradeFeed from '@components/LiveTradeFeed.jsx';

function StatCard({ label, value }) {
  return (
    <article className="stat-card">
      <h3>{label}</h3>
      <p>{value}</p>
    </article>
  );
}

function Dashboard() {
  const stream = useTradeStream();
  const { trades, isConnected } = useTradeStream();

  const portfolioValue = useMemo(
    () => trades.reduce((sum, t) => sum + (t.quantity * t.price || 0), 0),
    [trades]
  );

  const matched = trades.filter((t) => t.status === 'MATCHED').length;
  const breaks  = trades.filter((t) => ['UNMATCHED','DISPUTED'].includes(t.status)).length;

  console.log("Trades:", trades.length);

  console.log(
    "Latest trade:",
    trades[0]
  );

  console.log(
    "Portfolio:",
    portfolioValue,
    "Matched:",
    matched,
    "Breaks:",
    breaks
  );

  return (
    <section>
      <h2>Dashboard</h2>
      <div className="stat-grid">
        <StatCard label="Portfolio value (USD)" value={portfolioValue.toLocaleString()} />
        <StatCard label="Trades streamed" value={trades.length} />
        <StatCard label="Matched" value={matched} />
        <StatCard label="Open breaks" value={breaks} />
      </div>
      <div role="status" aria-live="polite" style={{ marginBottom: "1rem" }}>
          SSE:{" "}{isConnected? "🟢 Connected": "🔴 Disconnected"}
      </div>
      <LiveTradeFeed trades={trades.slice(0, 10)}/>
    </section>
  );
}

export default withErrorBoundary(withAuth(Dashboard));
