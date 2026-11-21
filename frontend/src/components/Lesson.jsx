import { useEffect, useRef, useState } from 'react';
import { getJson, errorText } from '../api.js';

const LANGS = [
  { code: 'en', label: 'English' },
  { code: 'te', label: 'Telugu' },
  { code: 'hi', label: 'Hindi' },
];

export default function Lesson({ topic, learnerId }) {
  const [lang, setLang] = useState('en');
  const [lesson, setLesson] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const alive = useRef(false);
  const latestRequest = useRef(0);

  useEffect(() => {
    alive.current = true;
    return () => {
      alive.current = false;
    };
  }, []);

  async function load(nextLang) {
    const requestId = ++latestRequest.current;
    setLoading(true);
    setError('');
    try {
      const url =
        `/api/lesson?topic=${encodeURIComponent(topic)}` +
        `&lang=${encodeURIComponent(nextLang)}` +
        `&learnerId=${encodeURIComponent(learnerId)}`;
      const data = await getJson(url);
      if (alive.current && requestId === latestRequest.current) setLesson(data);
    } catch (err) {
      if (alive.current && requestId === latestRequest.current) {
        setLesson(null);
        setError(errorText(err));
      }
    } finally {
      if (alive.current && requestId === latestRequest.current) setLoading(false);
    }
  }

  function changeLang(e) {
    const next = e.target.value;
    setLang(next);
    if (lesson) load(next);
  }

  const lessonLang = lesson ? lesson.lang || lang : lang;

  return (
    <>
      <section className="card">
        <h2>Learn: {topic}</h2>
        <div className="field">
          <label>
            Language:{' '}
            <select value={lang} onChange={changeLang}>
              {LANGS.map((l) => (
                <option key={l.code} value={l.code}>
                  {l.label}
                </option>
              ))}
            </select>
          </label>
        </div>
        <button className="btn-primary" onClick={() => load(lang)} disabled={loading}>
          {loading ? 'Loading…' : 'Get lesson'}
        </button>
      </section>

      {loading && <p className="muted">Loading lesson…</p>}
      {error && <p className="feedback-error">{error}</p>}

      {lesson && !loading && (
        <section className="card">
          {lesson.simplified && (
            <p className="muted">Simpler explanation based on your score</p>
          )}
          {lesson.source === 'stored' && (
            <p className="muted">
              Showing the built-in explanation
              {lessonLang !== 'en' ? ' (it is in English)' : ''}
            </p>
          )}
          <p className="lesson-text">{lesson.text}</p>
        </section>
      )}
    </>
  );
}
