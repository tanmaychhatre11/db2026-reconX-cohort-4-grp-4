// TICKET-ADV104 / TICKET-ADV105 — EventSource live feed with prepend + slide-in animation.
(function () {
  const FEED_EL = document.getElementById('trade-feed');
  if (!FEED_EL) return;

  const STREAM_URL = 'http://localhost:8080/api/v1/trades/stream';
  let sse = null;

  const STATUS_CLASS = {
    MATCHED: 'trade-card--matched',
    UNMATCHED: 'trade-card--break',
    PENDING: 'trade-card--pending',
    DISPUTED: 'trade-card--pending',
    CANCELLED: 'trade-card--pending',
  };

  const numberFmt = new Intl.NumberFormat();

  function updateConnectionBadge(text, variant) {
    const badge = document.getElementById('sse-status');
    if (!badge) return;
    badge.textContent = text;
    badge.className = 'sse-status sse-status--' + variant;
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function prepend(trade) {
    const el = document.createElement('article');
    const statusClass = STATUS_CLASS[trade.status] ?? 'trade-card--pending';
    el.className = 'trade-card ' + statusClass + ' trade-card--new';

    const symbol = escapeHtml(trade.instrument?.symbol ?? '-');
    const tradeRef = escapeHtml(trade.tradeRef);
    const status = escapeHtml(trade.status);

    el.innerHTML = `
      <strong>${tradeRef}</strong>
      <span>${symbol}</span>
      <span>Qty: ${numberFmt.format(trade.quantity)}</span>
      <span>Price: ${numberFmt.format(trade.price)}</span>
      <span>[${status}]</span>
    `;

    FEED_EL.prepend(el);

    setTimeout(() => el.classList.remove('trade-card--new'), 500);

    while (FEED_EL.children.length > 50) {
      FEED_EL.removeChild(FEED_EL.lastElementChild);
    }
  }

  function connect() {
    sse = new EventSource(STREAM_URL);

    sse.onopen = () => updateConnectionBadge('Live', 'live');

    sse.addEventListener('trade', (event) => {
      try {
        const trade = JSON.parse(event.data);
        prepend(trade);
      } catch (err) {
        console.error('Malformed SSE payload', err);
      }
    });

    // Never reconnect manually here — EventSource retries with backoff on its own.
    sse.onerror = () => updateConnectionBadge('Reconnecting…', 'reconnecting');
  }

  window.addEventListener('beforeunload', () => sse?.close());

  connect();
})();