import { useState } from 'react';
import { postJson, errorText } from './api.js';
import Dashboard from './components/Dashboard.jsx';
import Lesson from './components/Lesson.jsx';
import { QuizLoader } from './components/Quiz.jsx';

const LEARNER_KEY = 'maths.learner';

function readStoredLearner() {
  try {
    const raw = localStorage.getItem(LEARNER_KEY);
    const parsed = raw ? JSON.parse(raw) : null;
    return parsed && parsed.id != null ? parsed : null;
  } catch {
    return null;
  }
}

export default function App() {
  const [learner, setLearner] = useState(readStoredLearner);
  // 'setup' | 'dashboard' | 'diagnostic' | 'practice' | 'lesson'
  const [view, setView] = useState(() => (learner ? 'dashboard' : 'setup'));
  const [topic, setTopic] = useState('');

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [setupError, setSetupError] = useState('');
  const [saving, setSaving] = useState(false);

  async function createLearner(e) {
    e.preventDefault();
    setSetupError('');
    setSaving(true);
    try {
      const data = await postJson('/api/learners', {
        name: name.trim(),
        email: email.trim(),
      });
      if (!data || data.id == null) {
        throw new Error('The server returned an unexpected learner.');
      }
      localStorage.setItem(LEARNER_KEY, JSON.stringify(data));
      setLearner(data);
      setName('');
      setEmail('');
      setView('dashboard');
    } catch (err) {
      setSetupError(errorText(err));
    } finally {
      setSaving(false);
    }
  }

  function changeLearner() {
    localStorage.removeItem(LEARNER_KEY);
    setLearner(null);
    setTopic('');
    setSetupError('');
    setView('setup');
  }

  return (
    <div className="page">
      <header className="card header-card">
        <h1>Maths Practice</h1>
        {learner && (
          <div className="header-actions">
            <button className="btn-primary" onClick={() => setView('dashboard')}>
              Dashboard
            </button>
            <button className="btn-secondary" onClick={changeLearner}>
              Change learner
            </button>
          </div>
        )}
      </header>

      {view === 'setup' && (
        <section className="card">
          <h2>Create your learner</h2>
          <form onSubmit={createLearner}>
            <div className="field">
              <label>
                Name:{' '}
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </label>
            </div>
            <div className="field">
              <label>
                Email:{' '}
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </label>
            </div>
            {setupError && <p className="feedback-error">{setupError}</p>}
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Saving…' : 'Start learning'}
            </button>
          </form>
        </section>
      )}

      {view === 'dashboard' && learner && (
        <Dashboard
          learnerId={learner.id}
          onStartDiagnostic={() => setView('diagnostic')}
          onLearn={(t) => {
            setTopic(t);
            setView('lesson');
          }}
          onPractise={(t) => {
            setTopic(t);
            setView('practice');
          }}
        />
      )}

      {view === 'diagnostic' && learner && (
        <section className="card">
          <h2>Diagnostic quiz</h2>
          <QuizLoader
            path="/api/quiz/diagnostic"
            learnerId={learner.id}
            onBack={() => setView('dashboard')}
          />
        </section>
      )}

      {view === 'practice' && learner && topic && (
        <section className="card">
          <h2>Practise: {topic}</h2>
          <QuizLoader
            key={topic}
            path={`/api/questions?topic=${encodeURIComponent(topic)}`}
            learnerId={learner.id}
          />
        </section>
      )}

      {view === 'lesson' && learner && topic && (
        <Lesson key={topic} topic={topic} learnerId={learner.id} />
      )}
    </div>
  );
}
