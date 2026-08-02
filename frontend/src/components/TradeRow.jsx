// frontend/src/components/TradeRow.jsx
import React from 'react';

function TradeRowImpl({ trade, onClick }) {
  return (
    <div className="data-table__row-content" onClick={() => onClick(trade.id)}>
      <span role="cell">{trade.tradeRef}</span>
      <span role="cell">{trade.instrumentSymbol}</span>
      <span role="cell">{trade.quantity}</span>
      <span role="cell">{trade.price}</span>
      <span role="cell">
        <span className={`status-pill ${trade.status.toLowerCase()}`}>{trade.status}</span>
      </span>
    </div>
  );
}

// Custom equality — only the fields we actually render
function areEqual(prev, next) {
  return prev.trade.id      === next.trade.id
      && prev.trade.status  === next.trade.status
      && prev.trade.price   === next.trade.price
      && prev.onClick       === next.onClick;
}

export const TradeRow = React.memo(TradeRowImpl, areEqual);