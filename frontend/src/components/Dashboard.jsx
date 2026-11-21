import { useEffect, useState } from 'react';
import { getJson, errorText } from '../api.js';

export default function Dashboard({ learnerId, onStartDiagnostic, onLearn, onPractise }) {
  const [progress, setProgress] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Runs on mount, so returning to the dashboard always refetches progress.
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    getJson(`/api/learners/${encodeURIComponent(learnerId)}/progress`)
      .then((data) => {
        if (active) setProgress(data);
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
  }, [learnerId]);

  if (loading) return <p className="muted">Loading your progress…</p>;
  if (error) return <p className="feedback-error">{error}</p>;
  if (!progress) return null;

  const topics = progress.topics || [];

  return (
    <>
      <section className="card">
        <h2>Your progress</h2>
        <div className="stat-row">
          <div className="stat">
            <span className="stat-value">{progress.currentStreak ?? 0} days</span>
            <span className="muted">Streak</span>
          </div>
          <div className="stat">
            <span className="stat-value">{progress.overallScorePercent ?? 0}%</span>
            <span className="muted">Overall score</span>
          </div>
          <div className="stat">
            <span className="stat-value">{progress.overallAttempts ?? 0}</span>
            <span className="muted">Questions answered</span>
          </div>
        </div>
        <p>
          <strong>Weakest topic:</strong>{' '}
          {progress.weakestTopic || 'Answer a few questions to find out'}
        </p>
        <button className="btn-primary" onClick={onStartDiagnostic}>
          Start diagnostic quiz
        </button>
      </section>

      <h2>Topics</h2>
      {topics.length === 0 && <p className="muted">No topics yet.</p>}
      <div className="topic-grid">
        {topics.map((t) => (
          <div key={t.topic} className="card topic-card">
            <h3>{t.topic}</h3>
            <p className="topic-score">{t.scorePercent ?? 0}%</p>
            <p className="muted">
              {t.attempts ?? 0} attempts · {t.correct ?? 0} correct
            </p>
            <div>
              <button className="btn-primary" onClick={() => onLearn(t.topic)}>
                Learn
              </button>
              <button className="btn-secondary" onClick={() => onPractise(t.topic)}>
                Practise
              </button>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
