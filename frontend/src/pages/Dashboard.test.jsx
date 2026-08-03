import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { AuthContext } from '@context/AuthContext.jsx';
import { ThemeProvider } from '@context/ThemeContext.jsx';
import Dashboard from './Dashboard.jsx';

vi.mock('@hooks/useTradeStream.js', () => ({
  useTradeStream: () => ({ trades: [], isConnected: false }),
}));

const trades = [
  { id: 1, quantity: 100, price: 250, status: 'MATCHED' },
  { id: 2, quantity: 50, price: 251, status: 'UNMATCHED' },
];

function renderWithProviders(ui) {
  return render(
    <AuthContext.Provider value={{ user: { email: 'trader@db.com' } }}>
      <ThemeProvider>
        <MemoryRouter>{ui}</MemoryRouter>
      </ThemeProvider>
    </AuthContext.Provider>
  );
}

beforeEach(() => {
  const store = {};
  vi.stubGlobal('localStorage', {
    getItem: (key) => store[key] ?? null,
    setItem: (key, value) => { store[key] = String(value); },
    removeItem: (key) => { delete store[key]; },
    clear: () => { for (const k in store) delete store[k]; },
  });

  vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({ matches: false }));
});

describe('<Dashboard />', () => {
  it('shows summary cards', () => {
    renderWithProviders(<Dashboard trades={trades} />);

    expect(screen.getByRole('heading', { name: /portfolio value/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /trades streamed/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /matched/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /open breaks/i })).toBeInTheDocument();
    expect(screen.getByText(/37,550/)).toBeInTheDocument();
  });
});
