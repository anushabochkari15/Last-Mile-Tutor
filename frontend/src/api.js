export const API = 'http://localhost:8080';

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

export async function getJson(path) {
  const res = await fetch(`${API}${path}`);
  if (!res.ok) {
    throw new ApiError(res.status, `Something went wrong (HTTP ${res.status}). Please try again.`);
  }
  return res.json();
}

export async function postJson(path, body) {
  const res = await fetch(`${API}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    throw new ApiError(res.status, `Something went wrong (HTTP ${res.status}). Please try again.`);
  }
  return res.json();
}

// Turns any thrown value into a short message for the user.
export function errorText(err) {
  if (err instanceof ApiError) return err.message;
  if (err instanceof TypeError) return 'Could not reach the server. Check that the backend is running.';
  return (err && err.message) || 'Something went wrong. Please try again.';
}
