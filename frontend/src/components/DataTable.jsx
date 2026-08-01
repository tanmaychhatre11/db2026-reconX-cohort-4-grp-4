// TICKET-ADV114 — Compound <DataTable> with Header / Body / Pagination subcomponents.
import React, { createContext, useContext } from 'react';

const DataTableContext = createContext(null);

function useDataTable() {
  const context = useContext(DataTableContext);

  if (!context) {
    throw new Error('useDataTable() used outside <DataTable>');
  }

  return context;
}

export default function DataTable({ children, data, sort, page = 0, size = 20, onSortChange }) {
  return (
    <DataTableContext.Provider value={{ data, sort, page, size, onSortChange }}>
      <div className="data-table">{children}</div>
    </DataTableContext.Provider>
  );
}

DataTable.Header = function Header({ columns }) {
  const { sort, onSortChange } = useDataTable();
  return (
    <div className="data-table__header" role="row">
      {columns.map((column) => (
        <button
        key={column.key}
        className={sort?.column === column.key ? 'active' : ''}
        aria-sort={
          sort?.column === column.key
              ? sort.direction
              : 'none'
        }
        onClick={() =>
          onSortChange({ 
            column: column.key,
            direction:
              sort?.column === column.key &&
              sort.direction === 'ascending'
                ? 'descending'
                : 'ascending'
          })
        }
        >
          {column.label}
          {sort?.column === column.key &&
            (sort.direction === 'ascending' ? ' ▲' : ' ▼')}
        </button>
      ))}
    </div>
  );
};

DataTable.Body = function Body({ rows, render }) {
  return (
    <div className="data-table__body">
      {rows.map((row) => ( 
        <div
          className="data-table__row"
          key={row.id}
        >
          {render(row)}
      </div>
    ))}
  </div>
  );
      };


DataTable.Pagination = function Pagination({ page, totalPages, onChange }) {
  // TODO(TICKET-ADV114): render prev / next buttons that call onChange(page±1).
  //                     Disable prev at page === 0, next at page === totalPages-1.
  return (
    <nav className="data-table__pagination" aria-label="Pagination">
      {/* TODO(TICKET-ADV114): ‹ {page+1} / {totalPages} › */}
      <button
        disabled={page === 0}
        onClick={() => onChange(page - 1)}
      >
        ‹
      </button>

      <span>
        {page + 1} / {totalPages} 
      </span>
      <button
        disabled={page === totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        ›
      </button>
    </nav>
  );
};
