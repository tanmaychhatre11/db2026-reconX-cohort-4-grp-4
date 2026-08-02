// TICKET-ADV106 — Sortable, resizable, frozen header table
(function () {
  const table = document.getElementById('trades-table');
  const tbody = document.getElementById('trades-body');

  if (!table || !tbody) return;

  let rows = [];

async function loadTrades() {
    try {
      const res = await fetch('http://localhost:8080/api/v1/trades?size=200');
      const data = await res.json();
      rows = data.items || data.content || data;   // <-- updated
      renderRows();
    } catch (err) {
      console.error('Failed to load trades', err);
    }
  }

  function renderRows() {
    tbody.innerHTML = '';

    rows.forEach(trade => {
      const tr = document.createElement('tr');

      tr.innerHTML = `
        <td>${trade.tradeRef}</td>
        <td>${trade.symbol ?? trade.instrument?.symbol ?? '-'}</td>
        <td>${trade.qty ?? trade.quantity}</td>
        <td>${trade.price}</td>
        <td>${trade.status}</td>
      `;

      tbody.appendChild(tr);
    });
  }

  document.querySelectorAll('#trades-table th').forEach(header => {
    header.addEventListener('click', function () {
      const col = header.dataset.col;
      const type = header.dataset.type;

      const currentDir = header.dataset.dir === 'asc' ? 'desc' : 'asc';

      document.querySelectorAll('#trades-table th').forEach(th => {
        th.dataset.dir = 'none';
        th.removeAttribute('aria-sort');
      });

      header.dataset.dir = currentDir;
      header.setAttribute(
        'aria-sort',
        currentDir === 'asc' ? 'ascending' : 'descending'
      );

      rows.sort((a, b) => {
        let first = a[col];
        let second = b[col];

        if (type === 'number') {
          return currentDir === 'asc'
            ? first - second
            : second - first;
        }

        return currentDir === 'asc'
          ? String(first).localeCompare(String(second))
          : String(second).localeCompare(String(first));
      });

      renderRows();
    });
  });

  document.querySelectorAll('.resize-handle').forEach(handle => {
    let startX;
    let startWidth;
    let column;

    handle.addEventListener('mousedown', function (event) {
      event.preventDefault();
      event.stopPropagation(); // don't trigger the header's sort click

      column = handle.parentElement;
      startX = event.clientX;
      startWidth = column.offsetWidth;

      function resize(event) {
        column.style.width =
          Math.max(40, startWidth + (event.clientX - startX)) + 'px';
      }

      function stopResize() {
        document.removeEventListener('mousemove', resize);
        document.removeEventListener('mouseup', stopResize);
      }

      document.addEventListener('mousemove', resize);
      document.addEventListener('mouseup', stopResize);
    });
  });

  loadTrades();
})();