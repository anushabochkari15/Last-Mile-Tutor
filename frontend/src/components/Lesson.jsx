import { Fragment, useEffect, useRef, useState } from 'react';
import { getJson, errorText } from '../api.js';

const LANGS = [
  { code: 'en', label: 'English' },
  { code: 'te', label: 'Telugu' },
  { code: 'hi', label: 'Hindi' },
];

const HEADINGS = {
  en: ['What is it?', 'Key rules', 'Worked examples', 'Common mistake'],
  te: ['ఇది ఏమిటి?', 'ముఖ్య నియమాలు', 'ఉదాహరణలు', 'సాధారణ తప్పు'],
  hi: ['यह क्या है?', 'मुख्य नियम', 'उदाहरण', 'आम गलती'],
};
const UI = {
  en: {
    title: 'Learn',
    language: 'Language:',
    getLesson: 'Get lesson',
    loadingButton: 'Loading…',
    loadingLesson: 'Loading lesson…',
    simplified: 'Simpler explanation based on your score',
    builtIn: 'Showing the built-in explanation',
    inEnglish: ' (it is in English)',
    aiUnavailable: 'AI lesson unavailable right now. Showing the built-in explanation',
  },
  te: {
    title: 'నేర్చుకోండి',
    language: 'భాష:',
    getLesson: 'పాఠం పొందండి',
    loadingButton: 'లోడ్ అవుతోంది…',
    loadingLesson: 'పాఠం లోడ్ అవుతోంది…',
    simplified: 'మీ స్కోరు ఆధారంగా సరళమైన వివరణ',
    builtIn: 'అంతర్నిర్మిత వివరణ చూపిస్తోంది',
    inEnglish: ' (ఇది ఇంగ్లీష్‌లో ఉంది)',
    aiUnavailable: 'AI పాఠం ప్రస్తుతం అందుబాటులో లేదు. అంతర్నిర్మిత వివరణ చూపిస్తోంది',
  },
  hi: {
    title: 'सीखें',
    language: 'भाषा:',
    getLesson: 'पाठ लें',
    loadingButton: 'लोड हो रहा है…',
    loadingLesson: 'पाठ लोड हो रहा है…',
    simplified: 'आपके स्कोर के आधार पर सरल व्याख्या',
    builtIn: 'अंतर्निहित व्याख्या दिखाई जा रही है',
    inEnglish: ' (यह अंग्रेज़ी में है)',
    aiUnavailable: 'AI पाठ अभी उपलब्ध नहीं है। अंतर्निहित व्याख्या दिखाई जा रही है',
  },
};
const ALL_HEADINGS = [...HEADINGS.en, ...HEADINGS.te, ...HEADINGS.hi];
const WORKED_HEADINGS = [HEADINGS.en[2], HEADINGS.te[2], HEADINGS.hi[2]];
const EXAMPLE_RE = /^(Example|ఉదాహరణ|उदाहरण) \d+:/;
const ANSWER_RE = /^(Answer|సమాధానం|उत्तर):\s*(.*)$/;

// Splits plain lesson text into sections at known headings.
// Text before the first heading goes into a section with heading null.
function parseLesson(text) {
  const sections = [];
  let current = { heading: null, lines: [] };
  let hasHeadings = false;
  for (const raw of String(text ?? '').split(/\r?\n/)) {
    const line = raw.trim();
    if (ALL_HEADINGS.includes(line)) {
      hasHeadings = true;
      if (hasContent(current)) sections.push(current);
      current = { heading: line, lines: [] };
    } else {
      current.lines.push(line);
    }
  }
  if (hasContent(current)) sections.push(current);
  return { sections, hasHeadings };
}

function hasContent(section) {
  return section.heading !== null || section.lines.some((l) => l !== '');
}

function renderSection(section, sIdx) {
  const isWorked = WORKED_HEADINGS.includes(section.heading);
  const out = [];
  let para = [];
  let list = [];
  let example = null;

  const key = () => `${sIdx}-${out.length}`;
  const flushPara = () => {
    if (para.length) {
      out.push(
        <p key={key()} className="lesson-para">
          {para.join(' ')}
        </p>
      );
      para = [];
    }
  };
  const flushList = () => {
    if (list.length) {
      out.push(
        <ul key={key()} className="lesson-list">
          {list.map((item, i) => (
            <li key={i}>{item}</li>
          ))}
        </ul>
      );
      list = [];
    }
  };
  const flushExample = () => {
    if (example) {
      out.push(
        <div key={key()} className="example-box">
          <h4 className="example-title">{example.title}</h4>
          {example.steps.length > 0 && (
            <ol className="lesson-steps">
              {example.steps.map((s, i) => (
                <li key={i}>{s}</li>
              ))}
            </ol>
          )}
          {example.answer && (
            <p className="example-answer">
              <strong>{example.answerLabel}</strong> {example.answer}
            </p>
          )}
        </div>
      );
      example = null;
    }
  };

  for (const line of section.lines) {
    if (line === '') {
      flushPara();
      flushList();
      continue;
    }
    if (isWorked && EXAMPLE_RE.test(line)) {
      flushPara();
      flushList();
      flushExample();
      example = { title: line, steps: [], answer: '', answerLabel: '' };
      continue;
    }
    if (isWorked && example) {
      const step = line.match(/^\d+\.\s+(.*)$/);
      if (step) {
        example.steps.push(step[1]);
        continue;
      }
      const answer = line.match(ANSWER_RE);
      if (answer) {
        example.answerLabel = answer[1] + ':';
        example.answer = answer[2];
        continue;
      }
    }
    if (line.startsWith('- ')) {
      flushPara();
      flushExample();
      list.push(line.slice(2));
    } else {
      flushList();
      flushExample();
      para.push(line);
    }
  }
  flushPara();
  flushList();
  flushExample();

  return (
    <Fragment key={`s-${sIdx}`}>
      {section.heading !== null && <h3 className="lesson-heading">{section.heading}</h3>}
      {out}
    </Fragment>
  );
}

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
  const ui = UI[lang] || UI.en;

  const parsed = lesson ? parseLesson(lesson.text) : null;
  const plainBlocks = lesson
    ? String(lesson.text ?? '')
        .split(/\n\s*\n/)
        .map((b) => b.trim())
        .filter(Boolean)
    : [];

  return (
    <>
      <section className="card">
        <h2>{ui.title}: {topic}</h2>
        <div className="field">
          <label>
            {ui.language}{' '}
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
          {loading ? ui.loadingButton : ui.getLesson}
        </button>
      </section>

      {loading && <p className="muted">{ui.loadingLesson}</p>}
      {error && <p className="feedback-error">{error}</p>}

      {lesson && !loading && (
        <section className="card">
          {lesson.simplified && (
            <p className="muted">{ui.simplified}</p>
          )}
          {lesson.source === 'stored' && (
            <p className="muted">
              {ui.aiUnavailable}
              {lesson.lang !== lang ? ui.inEnglish : ''}
            </p>
          )}
          {parsed.hasHeadings
            ? parsed.sections.map((s, i) => renderSection(s, i))
            : plainBlocks.map((b, i) => (
                <p key={i} className="lesson-para" style={{ whiteSpace: 'pre-line' }}>
                  {b}
                </p>
              ))}
        </section>
      )}
    </>
  );
}
