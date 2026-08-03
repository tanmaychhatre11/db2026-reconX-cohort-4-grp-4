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
  createTrade: (req) => request("POST", "/v1/trades", req),
  updateStatus: (id, status) => request("PATCH", `/v1/trades/${id}/status`, { status }),
  deleteTrade: (id) => request("DELETE", `/v1/trades/${id}`),
  runRecon: (req) => request("POST", "/v1/recon/run", req),
  reconResults: (jobId, params = {}, options = {}) => {
    const query = toQueryString(params);
    return request("GET", `/v1/recon/jobs/${jobId}/results${query}`, undefined, options);
  },
  audit: (tradeRef, options = {}) => request("GET", `/v1/audit/trades/${tradeRef}`, undefined, options),
};
