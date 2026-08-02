// TICKET-ADV116 — useTradeStream() — SSE subscription returning live trades.
import { useEffect, useState } from 'react';

const MAX_BUFFER = 200;

export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);

  useEffect(() => {
    const sse = new EventSource(url);

    sse.onopen = () => {
      setConnected(true);
    };

    sse.addEventListener("trade", (event) => {
      console.log("Trade event received", event);

      try {
        const trade = JSON.parse(event.data);
        console.log("Parsed trade", trade);

        setTrades((prev) => [trade, ...prev].slice(0, MAX_BUFFER));
      } catch (e) {
        console.error(e);
      }
    });

    sse.onerror = () => {
      setConnected(false);
    };

    return () => {
      sse.close();
    };
  }, [url]);

  return { trades, isConnected };
}
