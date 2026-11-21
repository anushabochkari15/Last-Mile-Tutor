import { useEffect, useRef, useState } from 'react';
import { getJson, postJson, errorText } from '../api.js';

// Loads a list of questions from `path` and runs the Quiz on them.
// With `practice`, the Quiz shows a "Next 5 questions" button that refetches `path`.
export function QuizLoader({ path, learnerId, onBack, practice = false }) {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [round, setRound] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    getJson(path)
      .then((data) => {
        if (active) setQuestions(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        if (active) setError(errorText(err));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [path, round]);

  if (loading) return <p className="muted">Loading questions…</p>;
  if (error) return <p className="feedback-error">{error}</p>;
  return (
    <Quiz
      questions={questions}
      learnerId={learnerId}
      onBack={onBack}
      onNext={practice ? () => setRound((r) => r + 1) : undefined}
      emptyText={round > 0 ? 'No more questions for this topic right now.' : undefined}
    />
  );
}

export default function Quiz({ questions, learnerId, onBack, onNext, emptyText }) {
  // questionId -> { selected, submitting, result, error, hint, hintLoading, hintError }
  const [state, setState] = useState({});
  const alive = useRef(false);

  useEffect(() => {
    alive.current = true;
    return () => {
      alive.current = false;
    };
  }, []);

  function patch(id, changes) {
    setState((prev) => ({ ...prev, [id]: { ...prev[id], ...changes } }));
  }

  async function submit(q) {
    const cur = state[q.id] || {};
    if (cur.selected === undefined || cur.submitting || cur.result) return;
    patch(q.id, { submitting: true, error: '' });
    try {
      const data = await postJson(
        `/api/learners/${encodeURIComponent(learnerId)}/answers`,
        { questionId: q.id, selectedIndex: cur.selected }
      );
      if (alive.current) patch(q.id, { submitting: false, result: data });
    } catch (err) {
      if (alive.current) patch(q.id, { submitting: false, error: errorText(err) });
    }
  }

  async function loadHint(q) {
    const cur = state[q.id] || {};
    if (cur.hint || cur.hintLoading) return;
    patch(q.id, { hintLoading: true, hintError: '' });
    try {
      const data = await getJson(`/api/hint/${encodeURIComponent(q.id)}`);
      if (alive.current) {
        patch(q.id, {
          hintLoading: false,
          hint: data.text || 'No hint is available for this question.',
        });
      }
    } catch (err) {
      if (alive.current) patch(q.id, { hintLoading: false, hintError: errorText(err) });
    }
  }

  if (questions.length === 0) {
    return <p>{emptyText || 'No questions available right now.'}</p>;
  }

  const answered = questions.filter((q) => state[q.id]?.result).length;
  const allDone = answered === questions.length;

  return (
    <>
      {questions.map((q, idx) => {
        const cur = state[q.id] || {};
        const locked = Boolean(cur.result);
        return (
          <div key={q.id} className="question">
            <p>
              <strong>
                {idx + 1}. {q.text}
              </strong>
            </p>
            {q.options.map((opt, i) => (
              <label key={i} className="option">
                <input
                  type="radio"
                  name={`question-${q.id}`}
                  checked={cur.selected === i}
                  disabled={locked}
                  onChange={() => patch(q.id, { selected: i, error: '' })}
                />{' '}
                {opt}
              </label>
            ))}

            <div>
              <button
                className="btn-primary"
                disabled={cur.selected === undefined || cur.submitting || locked}
                onClick={() => submit(q)}
              >
                {cur.submitting ? 'Submitting…' : 'Submit answer'}
              </button>
              {!locked && (
                <button
                  className="btn-secondary"
                  disabled={cur.hintLoading || Boolean(cur.hint)}
                  onClick={() => loadHint(q)}
                >
                  {cur.hintLoading ? 'Loading hint…' : 'Hint'}
                </button>
              )}
            </div>

            {!locked && cur.hint && (
              <div className="hint-box">
                <strong>Hint:</strong> {cur.hint}
              </div>
            )}
            {cur.hintError && <p className="feedback-error">{cur.hintError}</p>}
            {cur.error && <p className="feedback-error">{cur.error}</p>}

            {cur.result && (
              <div>
                <p className={cur.result.correct ? 'feedback-success' : 'feedback-error'}>
                  {cur.result.correct ? 'Correct!' : 'Incorrect'}
                </p>
                <p>
                  <strong>Explanation:</strong> {cur.result.explanation}
                </p>
                {!cur.result.correct && cur.result.hint && (
                  <p>
                    <strong>Hint:</strong> {cur.result.hint}
                  </p>
                )}
              </div>
            )}
          </div>
        );
      })}

      {allDone && onNext && (
        <div className="card">
          <button className="btn-primary" onClick={onNext}>
            Next 5 questions
          </button>
        </div>
      )}

      {allDone && onBack && (
        <div className="card">
          <p>You have answered all the questions.</p>
          <button className="btn-primary" onClick={onBack}>
            Back to dashboard
          </button>
        </div>
      )}
    </>
  );
}
