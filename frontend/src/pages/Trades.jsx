// TICKET-ADV114 — Compound DataTable.
// TICKET-ADV117 — useDebouncedSearch.
import React, { useEffect, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { api } from '@services/apiService.js';

function Trades() {
  const [search, setSearch] = useState('');
  const debounced = useDebouncedSearch(search, 300);
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState(null);
  const [data, setData] = useState({ items: [], totalPages: 0 });

  // Reset to page 0 whenever the filter changes — otherwise a narrower
  // filter can leave `page` pointing past the new last page.
  useEffect(() => {
    setPage(0);
  }, [debounced]);

  useEffect(() => {
    const controller = new AbortController();

    async function fetchTrades() {
      try {
        const params = {
          page,
          status: debounced || undefined,
          sortColumn: sort?.column,
          sortDirection: sort?.direction,
        };
        const response = await api.listTrades(params, { signal: controller.signal });
        console.log("API RESPONSE", response);
        setData({ items: response.items, totalPages: response.totalPages });
      } catch (err) {
        console.log("ERROR", err);
        if (err.name === 'AbortError') return; // superseded by a newer request
        setData({ items: [], totalPages: 0 });
      }
    }

    fetchTrades();
    return () => controller.abort();
  }, [page, debounced, sort]);

  return (
    <section>
      <h2>Trades</h2>
      <input
        aria-label="Filter by status"
        placeholder="status filter (PENDING/MATCHED/…)"
        value={search}
        onChange={(e) => setSearch(e.target.value.toUpperCase())}
      />
      <DataTable data={data.items} sort={sort} onSortChange={setSort}>
        <DataTable.Header columns={[
          { key: 'tradeRef', label: 'Ref' },
          { key: 'symbol',   label: 'Symbol' },
          { key: 'quantity', label: 'Qty' },
          { key: 'price',    label: 'Price' },
          { key: 'status',   label: 'Status' },
        ]} />
        <DataTable.Body
          rows={data.items}
          render={(trade) => (
            <>
              <span>{trade.tradeRef}</span>
              <span>{trade.instrumentSymbol}</span>
              <span>{trade.quantity}</span>
              <span>{trade.price}</span>
              <span>{trade.status}</span>
            </>
          )}
        />
        <DataTable.Pagination
          page={page}
          totalPages={Math.max(1, data.totalPages)}
          onChange={setPage}
        />
      </DataTable>
    </section>
  );
}

export default withAuth(Trades);