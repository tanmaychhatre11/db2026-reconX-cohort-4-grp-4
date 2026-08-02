// TICKET-ADV104 / TICKET-ADV105 — EventSource live feed with prepend + slide-in animation.
(function () {
  const FEED_EL  = document.getElementById('trade-feed');
  const status = document.getElementById('sse-status');
  if (!FEED_EL) return;

  const STREAM_URL = 'http://localhost:8080/api/v1/trades/stream';
  console.log("Connecting to", STREAM_URL);
  let sse = null;

  function updateConnectionBadge(text, variant) {
    const badge = document.getElementById('sse-status');
    if (!badge) return;
    badge.textContent = text;
    badge.className = 'sse-status sse-status--' + variant;
  }

function prepend(trade) {
  const el = document.createElement("article");

  el.className = "trade-card trade-card--" + trade.status.toLowerCase();

  el.innerHTML = `
    <strong>${trade.tradeRef}</strong>
    <span>${trade.instrument?.symbol ?? "-"}</span>
    <span>Qty: ${trade.quantity}</span>
    <span>Price: ${trade.price}</span>
    <span>[${trade.status}]</span>
  `;

  feed.prepend(el);
  
  while (feed.children.length > 20) {
    feed.removeChild(feed.lastElementChild);
  }
}

function connect() {
  sse = new EventSource(STREAM_URL);
  sse.onopen = () => updateConnectionBadge('Live', 'live');
  sse.addEventListener("trade", (event) => {
    console.log("Raw event:", event.data);
    try {
      const trade = JSON.parse(event.data);
      console.log("Trade received:", trade);
      prepend(trade);
    } catch (err) {
      console.error('Malformed SSE payload', err);
    }
  });
  // Never reconnect manually here — EventSource retries with backoff on its own.
  sse.onerror = () => updateConnectionBadge('Reconnecting…', 'reconnecting');
}
  
  window.addEventListener('beforeunload', () => sse?.close());
  console.log("Connecting to SSE stream…");
  connect();
})();