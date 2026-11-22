// Backend base URL. Set VITE_API_URL in frontend/.env (see .env.example) to override.
// Falls back to the local backend when the variable is not set.
export const API = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '');

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

// Friendly text for a non-ok HTTP status; other statuses keep the generic message.
function httpErrorMessage(status) {
  if (status === 400) return 'Please check the details you entered and try again.';
  if (status === 404) return 'That item could not be found.';
  return `Something went wrong (HTTP ${status}). Please try again.`;
}

export async function getJson(path) {
  const res = await fetch(`${API}${path}`);
  if (!res.ok) {
    throw new ApiError(res.status, httpErrorMessage(res.status));
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
    throw new ApiError(res.status, httpErrorMessage(res.status));
  }
  return res.json();
}

// Turns any thrown value into a short message for the user.
export function errorText(err) {
  if (err instanceof ApiError) return err.message;
  if (err instanceof TypeError) return 'Could not reach the server. Check that the backend is running.';
  return (err && err.message) || 'Something went wrong. Please try again.';
}


// Account used by the "Use demo learner" button on the setup screen.
export const DEMO_LEARNER = { name: 'Demo learner', email: 'demo@example.com' };

// Gives the demo learner a few answers in every topic so the dashboard has progress.
// Each failing request is skipped so one error does not stop the rest.
export async function seedDemoProgress(learnerId) {
  let topics;
  try {
    topics = await getJson('/api/topics');
  } catch {
    return;
  }
  if (!Array.isArray(topics)) return;

  for (const topic of topics) {
    let questions;
    try {
      questions = await getJson(`/api/questions?topic=${encodeURIComponent(topic)}`);
    } catch {
      continue;
    }
    const picked = Array.isArray(questions) ? questions.slice(0, 2) : [];
    for (let i = 0; i < picked.length; i++) {
      try {
        await postJson(`/api/learners/${encodeURIComponent(learnerId)}/answers`, {
          questionId: picked[i].id,
          selectedIndex: i, // first question answered 0, second answered 1
        });
      } catch {
        // Skip this answer and carry on.
      }
    }
  }
}

