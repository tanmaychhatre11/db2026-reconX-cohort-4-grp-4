// TICKET-ADV106 / ADV107 — EventSource live feed with prepend + slide-in animation.
(function () {
  const FEED_EL  = document.getElementById('trade-feed');
  const status = document.getElementById('sse-status');
  if (!FEED_EL) return;

  // Hardcoded demo events for the static dashboard (no backend required).
  // Replace with: const sse = new EventSource('/api/v1/trades/stream');
  
  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function prependTradeRow(trade) {
    const row  = document.createElement('article');
    row .className = 'trade-card trade-card--' + trade.status.toLowerCase()+ ' trade-card--new';
    const formatter = new Intl.NumberFormat();
    row .innerHTML = `
      <strong>${escapeHtml(trade.tradeRef)}</strong>
      <span> ${escapeHtml(trade.symbol)} </span>
      <span> qty=${formatter.format(trade.qty)} </span>
      <span> price=${formatter.format(trade.price)} </span>
      <span> [${escapeHtml(trade.status)}]</span>`;
      FEED_EL.prepend(row);

      setTimeout(() => {
        row.classList.remove('trade-card--new');
      }, 500);

      while (FEED_EL.children.length > 50) {
        FEED_EL.lastElementChild.remove();
      }
    }

    const sse = new EventSource('/api/v1/trades/stream');

  sse.onopen = function () {
    if (status) {
      status.textContent = 'Live';
    }
  };

  sse.onmessage = function (event) {
    const trade = JSON.parse(event.data);
    prependTradeRow(trade);
  };

  sse.onerror = function () {
    if (status) {
      status.textContent = 'Reconnecting…';
    }
  };

  window.addEventListener('beforeunload', function () {
    sse?.close();
  });
})();
