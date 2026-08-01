// TICKET-ADV106 — Sortable, resizable, frozen header table
(function () {
    const table = document.getElementById('trades-table');
    const tbody = document.getElementById('trades-body');
  
    if (!table || !tbody) return;
  
    let rows = [
      { tradeRef: 'EQU-20260603-0001', symbol: 'SAP.DE', qty: 1000, price: 125.50, status: 'MATCHED' },
      { tradeRef: 'FX-20260603-0001', symbol: 'EUR/USD', qty: 1000000, price: 1.0852, status: 'PENDING' },
      { tradeRef: 'EQU-20260603-0002', symbol: 'AAPL', qty: 500, price: 178.20, status: 'BREAK' }
    ];
  
    function renderRows() {
      tbody.innerHTML = '';
  
      rows.forEach(trade => {
        const tr = document.createElement('tr');
  
        tr.innerHTML = `
          <td>${trade.tradeRef}</td>
          <td>${trade.symbol}</td>
          <td>${trade.qty}</td>
          <td>${trade.price}</td>
          <td>${trade.status}</td>
        `;
  
        tbody.appendChild(tr);
      });
    }
  
    document.querySelectorAll('#trades-table th').forEach((header, index) => {
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
  
        header.textContent =
          header.textContent.replace(/[▲▼]/g, '') +
          (currentDir === 'asc' ? ' ▲' : ' ▼');
  
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
  
        column = handle.parentElement;
        startX = event.clientX;
        startWidth = column.offsetWidth;
  
        function resize(event) {
          column.style.width =
            startWidth + (event.clientX - startX) + 'px';
        }
  
        function stopResize() {
          document.removeEventListener('mousemove', resize);
          document.removeEventListener('mouseup', stopResize);
        }
  
        document.addEventListener('mousemove', resize);
        document.addEventListener('mouseup', stopResize);
      });
    });
  
    renderRows();
  })();