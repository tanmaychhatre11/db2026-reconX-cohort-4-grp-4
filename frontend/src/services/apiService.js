// TICKET-ADV112-related — fetch wrapper that attaches Bearer JWT from sessionStorage.
const BASE = '/api';

function authHeaders() {
  const token = sessionStorage.getItem("reconx-token");

  return token
    ? {
        Authorization: `Bearer ${token}`,
      }
    : {};
}

async function request(method, path, body, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(),
    },
    body: body ? JSON.stringify(body) : undefined,
    signal: options.signal,
  });

  if (!res.ok) {
    let detail = "";

    try {
      detail = await res.text();
    } catch {
      detail = "";
    }

    throw new Error(`HTTP ${res.status}: ${detail}`);
  }

  if (res.status === 204) {
    return null;
  }

  return await res.json();
}

function toQueryString(params = {}) {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, value);
    }
  });

  const qs = search.toString();
  return qs ? `?${qs}` : '';
}

export const api = {
  login: (email, password) =>
    request("POST", "/v1/auth/login", {
        email,
        password,
  }),
  listTrades: (params = {}, options = {}) => {
    const query = new URLSearchParams();

    if (params.page !== undefined) {
      query.set("page", params.page);
    }

    if (params.status) {
      query.set("status", params.status);
    }

    if (params.sortColumn) {
      query.set("sort", `${params.sortColumn},${params.sortDirection}`);
    }

    const suffix = query.toString()
      ? `?${query.toString()}`
      : "";

    return request(
      "GET",
      `/v1/trades${suffix}`,
      undefined,
      options
    );
  },
  createTrade: (req)         => {
    // TODO(TICKET-ADV123): POST /v1/trades with the form payload.
    throw new Error('TICKET-ADV123 not implemented');
  },
  updateStatus: (id, status) => {
    // TODO(TICKET-ADV119): PATCH /v1/trades/{id}/status with { status }.
    throw new Error('TICKET-ADV119 not implemented');
  },
  deleteTrade: (id)          => {
    // TODO(TICKET-ADV119): DELETE /v1/trades/{id}.
    throw new Error('TICKET-ADV119 not implemented');
  },
  runRecon: (req)            => {
    // TODO(TICKET-ADV121): POST /v1/recon/run to enqueue a recon job.
    throw new Error('TICKET-ADV121 not implemented');
  },
  reconResults: (jobId)      => {
    // TODO(TICKET-ADV121): GET /v1/recon/jobs/{jobId}/results.
    throw new Error('TICKET-ADV121 not implemented');
  },
  audit: (tradeRef)          => {
    // TODO(TICKET-ADV121): GET /v1/audit/trades/{tradeRef}.
    throw new Error('TICKET-ADV121 not implemented');
  },
};