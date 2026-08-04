import { useEffect, useState } from 'react';

export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);

  useEffect(() => {
    let sse;

    async function initialise() {
      try {
        const response = await fetch('/api/v1/trades?size=5000');

        if (!response.ok) {
          throw new Error('Failed to load trades');
        }

        const page = await response.json();
        console.log(page.totalElements);
        console.log(page.items.length);
        console.log(page.items[0]);

        setTrades(page.items ?? []);
      } catch (err) {
        console.error('Initial trade load failed', err);
      }

      sse = new EventSource(url);

      sse.onopen = () => {
        setConnected(true);
      };

      sse.addEventListener('trade', (event) => {
        try {
          const trade = JSON.parse(event.data);

          setTrades(prev => {
            const withoutCurrent = prev.filter(t => t.id !== trade.id);
            return [trade, ...withoutCurrent];
          });

        } catch (err) {
          console.error(err);
        }
      });

      sse.onerror = () => {
        setConnected(false);
      };
    }

    initialise();

    return () => {
      sse?.close();
    };
  }, [url]);

  return {
    trades,
    isConnected
  };
}