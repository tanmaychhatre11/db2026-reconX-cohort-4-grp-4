// TICKET-ADV114 — Compound DataTable.
// TICKET-ADV117 — useDebouncedSearch.
// TICKET-ADV119 — memoised TradeRow.
import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { withAuth } from '@components/withAuth.jsx';
import DataTable from '@components/DataTable.jsx';
import { TradeRow } from '@components/TradeRow.jsx';
import { useDebouncedSearch } from '@hooks/useDebouncedSearch.js';
import { api } from '@services/apiService.js';

function Trades() {
  const navigate = useNavigate();
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
        setData({ items: response.items, totalPages: response.totalPages });
      } catch (err) {
        if (err.name === 'AbortError') return; // superseded by a newer request
        setData({ items: [], totalPages: 0 });
      }
    }

    fetchTrades();
    return () => controller.abort();
  }, [page, debounced, sort]);

  // Stable identity so TradeRow's areEqual (prev.onClick === next.onClick)
  // actually holds — without useCallback this would be a new function
  // every render and defeat the ADV119 memo entirely.
  const handleRowClick = useCallback((tradeId) => {
    navigate(`/trades/${tradeId}`);
  }, [navigate]);

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
          { key: 'instrumentSymbol',   label: 'Symbol' },
          { key: 'quantity', label: 'Qty' },
          { key: 'price',    label: 'Price' },
          { key: 'status',   label: 'Status' },
        ]} />
        <DataTable.Body
          rows={data.items}
          render={(trade) => <TradeRow trade={trade} onClick={handleRowClick} />}
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