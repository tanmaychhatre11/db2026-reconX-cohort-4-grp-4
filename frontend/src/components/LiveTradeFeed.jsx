import React from 'react';

const STATUS_CLASS = {
  MATCHED: 'trade-card--matched',
  UNMATCHED: 'trade-card--break',
  PENDING: 'trade-card--pending',
  DISPUTED: 'trade-card--pending',
  CANCELLED: 'trade-card--pending'
};

const numberFmt = new Intl.NumberFormat();

export default function LiveTradeFeed({ trades }) {
  return (
    <section>
      <h2>Live Trade Feed</h2>
      <div className="trade-feed">
        {trades.map(trade => (
            <article
                key={trade.id}
                className={`trade-card trade-card--new ${STATUS_CLASS[trade.status] ?? 'trade-card--pending'}`}
            >
            <strong>{trade.tradeRef}</strong>
            <span>{trade.instrumentSymbol ?? trade.instrument?.symbol ?? "-"}</span>
            <span>
              Qty: {numberFmt.format(Number(trade.quantity))}
            </span>
            <span>
              Price: {numberFmt.format(Number(trade.price))}
            </span>
            <span>
              [{trade.status}]
            </span>
          </article>
        ))}
      </div>
    </section>
  );
}