import { useState } from 'react';
import { postJson, getJson, errorText, DEMO_LEARNER, seedDemoProgress } from './api.js';
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
  const [demoLoading, setDemoLoading] = useState(false);
  const [signInEmail, setSignInEmail] = useState('');
  const [signingIn, setSigningIn] = useState(false);

  async function handleDemoLearner() {
    setSetupError('');
    setDemoLoading(true);
    try {
      let data;
      try {
        data = await postJson('/api/learners', DEMO_LEARNER);
      } catch {
        setSetupError('Could not start the demo. Check that the backend is running on port 8080.');
        return;
      }
      if (!data || data.id == null) {
        setSetupError('Could not start the demo. Check that the backend is running on port 8080.');
        return;
      }

      // Seed progress only if the demo learner has no answers yet.
      let progress = null;
      try {
        progress = await getJson(`/api/learners/${encodeURIComponent(data.id)}/progress`);
      } catch {
        progress = null;
      }
      if (!progress || !progress.overallAttempts) {
        try {
          await seedDemoProgress(data.id);
        } catch {
          // Ignore seeding errors; the demo still works with less progress.
        }
      }

      localStorage.setItem(LEARNER_KEY, JSON.stringify(data));
      setLearner(data);
      setName('');
      setEmail('');
      setView('dashboard');
    } finally {
      setDemoLoading(false);
    }
  }

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

  async function signIn(e) {
    e.preventDefault();
    setSetupError('');
    setSigningIn(true);
    try {
      const data = await getJson(
        `/api/learners/by-email?email=${encodeURIComponent(signInEmail.trim())}`
      );
      if (!data || data.id == null) {
        throw new Error('The server returned an unexpected learner.');
      }
      localStorage.setItem(LEARNER_KEY, JSON.stringify(data));
      setLearner(data);
      setName('');
      setEmail('');
      setSignInEmail('');
      setView('dashboard');
    } catch (err) {
      setSetupError(errorText(err));
    } finally {
      setSigningIn(false);
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
        <h1>Last-Mile Tutor</h1>
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
            <button
              type="button"
              className="btn-secondary"
              onClick={handleDemoLearner}
              disabled={demoLoading}
            >
              {demoLoading ? 'Starting demo...' : 'Use demo learner'}
            </button>
          </form>
          <h2>Already registered? Sign in</h2>
          <form onSubmit={signIn}>
            <div className="field">
              <label>
                Email to sign in:{' '}
                <input
                  type="email"
                  value={signInEmail}
                  onChange={(e) => setSignInEmail(e.target.value)}
                  required
                />
              </label>
            </div>
            {setupError && <p className="feedback-error">{setupError}</p>}
            <button type="submit" className="btn-primary" disabled={signingIn}>
              {signingIn ? 'Signing in…' : 'Sign in'}
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
            path={`/api/quiz/practice?learnerId=${learner.id}&topic=${encodeURIComponent(topic)}`}
            learnerId={learner.id}
            practice
          />
        </section>
      )}

      {view === 'lesson' && learner && topic && (
        <Lesson key={topic} topic={topic} learnerId={learner.id} />
      )}
    </div>
  );
}
