import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import StringVisualizer from '../components/visualization/components/StringVisualizer';
import { STRING_OPERATIONS, COMPARISON_TEMPLATES } from '../utils/stringTemplates';
import '../styles/arraysPage.css'; // Reuse array visual layouts

const STRING_QUIZ_QUESTIONS = [
  {
    type: 'MCQ',
    question: 'Where are string literals stored in Java memory heap?',
    options: ['Stack Memory', 'String Constant Pool', 'Metaspace', 'Garbage Collector register'],
    answer: 'String Constant Pool',
    explanation: 'Java stores string literals in a specialized memory area within the Heap known as the String Constant Pool (or String Pool) to optimize memory allocations.'
  },
  {
    type: 'MCQ',
    question: 'Which of the following is NOT a reason why Strings are designed to be immutable in Java?',
    options: [
      'To allow sharing of strings in the String Constant Pool safely.',
      'To ensure security when passing strings as database URLs or file paths.',
      'To allow fast indexing of characters using native pointers.',
      'To support thread-safe operations on string objects without synchronization.'
    ],
    answer: 'To allow fast indexing of characters using native pointers.',
    explanation: 'Immutability ensures security, thread safety, and pool sharing. String characters are indexed using charAt() bounds checks, not raw pointer arithmetic.'
  },
  {
    type: 'PREDICTION',
    question: 'What does this print?\nString s1 = "java";\nString s2 = new String("java");\nSystem.out.print((s1 == s2) + " " + s1.equals(s2));',
    options: ['true true', 'false true', 'true false', 'false false'],
    answer: 'false true',
    explanation: '== compares reference identities (s2 is on heap, s1 is in pool, so false). equals() compares character values (both are "java", so true).'
  },
  {
    type: 'MCQ',
    question: 'What is the worst-case time complexity of the Knuth-Morris-Pratt (KMP) string search algorithm for text size N and pattern size M?',
    options: ['O(N * M)', 'O(N + M)', 'O(N log M)', 'O(N^2)'],
    answer: 'O(N + M)',
    explanation: 'KMP precomputes an LPS array of size M and searches the text in O(N) steps without backtracking, yielding a total worst-case time of O(N + M).'
  },
  {
    type: 'DRYRUN',
    question: 'For the pattern "AABAAB", what is its computed KMP LPS (Longest Prefix Suffix) array?',
    options: [
      '[0, 1, 0, 1, 2, 3]',
      '[0, 1, 2, 0, 1, 2]',
      '[0, 1, 0, 1, 2, 0]',
      '[0, 1, 2, 3, 4, 5]'
    ],
    answer: '[0, 1, 0, 1, 2, 3]',
    explanation: 'The LPS matches prefixes that are also suffixes: "A" matches at index 1 -> 1. "AA" prefix does not match "AAB". For "AABAAB", suffix "AAB" at the end matches prefix "AAB" (length 3), resulting in [0, 1, 0, 1, 2, 3].'
  }
];

const StringsPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // Navigation State
  const [activeTab, setActiveTab] = useState(() => localStorage.getItem('strings_selected_tab') || 'theory');

  useEffect(() => {
    localStorage.setItem('strings_selected_tab', activeTab);
  }, [activeTab]);

  // Visualizer State
  const [selectedAlgoKey, setSelectedAlgoKey] = useState('traversal');
  
  const currentTemplate = useMemo(() => {
    return STRING_OPERATIONS[selectedAlgoKey];
  }, [selectedAlgoKey]);

  const [editorCode, setEditorCode] = useState('');
  useEffect(() => {
    if (currentTemplate) {
      setEditorCode(currentTemplate.javaCode);
      resetPlayback();
    }
  }, [currentTemplate]);

  // Tracing State
  const [sessionId, setSessionId] = useState(null);
  const [trace, setTrace] = useState([]);
  const [stepIdx, setStepIdx] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [speedMs, setSpeedMs] = useState(() => parseInt(localStorage.getItem('strings_speed') || '1200', 10));
  const [compileError, setCompileError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    localStorage.setItem('strings_speed', speedMs.toString());
  }, [speedMs]);

  const editorRef = useRef(null);
  const monacoRef = useRef(null);
  const decorationsRef = useRef([]);
  const playIntervalRef = useRef(null);

  const currentStep = trace[stepIdx] || null;

  useEffect(() => {
    return () => stopPlayTimer();
  }, []);

  const stopPlayTimer = () => {
    setIsPlaying(false);
    if (playIntervalRef.current) {
      clearInterval(playIntervalRef.current);
      playIntervalRef.current = null;
    }
  };

  const startPlayTimer = () => {
    setIsPlaying(true);
    playIntervalRef.current = setInterval(() => {
      setStepIdx(prev => {
        if (prev < trace.length - 1) {
          return prev + 1;
        } else {
          stopPlayTimer();
          return prev;
        }
      });
    }, speedMs);
  };

  useEffect(() => {
    if (isPlaying) {
      stopPlayTimer();
      startPlayTimer();
    }
  }, [speedMs]);

  const togglePlay = () => {
    if (isPlaying) {
      stopPlayTimer();
    } else {
      if (stepIdx >= trace.length - 1) {
        setStepIdx(0);
      }
      startPlayTimer();
    }
  };

  const resetPlayback = () => {
    stopPlayTimer();
    setTrace([]);
    setStepIdx(0);
    setSessionId(null);
    setCompileError(null);
  };

  const handleCompileAndRun = async () => {
    setIsLoading(true);
    setCompileError(null);
    stopPlayTimer();

    try {
      const response = await api.post('/execution/start', {
        className: currentTemplate.className,
        code: editorCode,
        input: ''
      });
      const data = response.data.data;
      
      if (data.firstStep && data.firstStep.exceptionName === 'CompilationException') {
        setCompileError(data.firstStep.exceptionMessage);
        setTrace([data.firstStep]);
        setStepIdx(0);
      } else {
        setSessionId(data.sessionId);
        const traceRes = await api.get(`/execution/${data.sessionId}/trace`);
        setTrace(traceRes.data.data);
        setStepIdx(0);
      }
    } catch (err) {
      console.error(err);
      setCompileError(err.response?.data?.message || 'Error occurred starting execution session.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStepForward = () => {
    stopPlayTimer();
    if (stepIdx < trace.length - 1) {
      setStepIdx(stepIdx + 1);
    }
  };

  const handleStepBackward = () => {
    stopPlayTimer();
    if (stepIdx > 0) {
      setStepIdx(stepIdx - 1);
    }
  };

  const handleRestart = () => {
    stopPlayTimer();
    setStepIdx(0);
  };

  const handleEditorDidMount = (editor, monaco) => {
    editorRef.current = editor;
    monacoRef.current = monaco;
  };

  useEffect(() => {
    if (editorRef.current && monacoRef.current && currentStep && currentStep.lineNumber) {
      const monaco = monacoRef.current;
      const line = currentStep.lineNumber;

      decorationsRef.current = editorRef.current.deltaDecorations(
        decorationsRef.current,
        [
          {
            range: new monaco.Range(line, 1, line, 1),
            options: {
              isWholeLine: true,
              className: 'monaco-line-highlight',
              marginClassName: 'monaco-line-highlight',
            },
          },
        ]
      );
      editorRef.current.revealLineInCenter(line);
    } else if (editorRef.current) {
      decorationsRef.current = editorRef.current.deltaDecorations(decorationsRef.current, []);
    }
  }, [currentStep]);

  const metrics = useMemo(() => {
    if (!currentStep || !currentStep.variables) {
      return { iterations: 0, comparisons: 0, swaps: 0, index: -1, pointer: -1 };
    }
    let it = 0, comp = 0, sw = 0, idx = -1, ptr = -1;
    currentStep.variables.forEach(v => {
      const name = v.name.toLowerCase();
      if (name === 'iterations' || name === 'iteration') it = parseInt(v.value, 10) || 0;
      if (name === 'comparisons' || name === 'comparison') comp = parseInt(v.value, 10) || 0;
      if (name === 'swaps' || name === 'swap') sw = parseInt(v.value, 10) || 0;
      if (name === 'i' || name === 'idx' || name === 'index') idx = parseInt(v.value, 10) || 0;
      if (name === 'left' || name === 'low' || name === 'mid') ptr = parseInt(v.value, 10) || 0;
    });
    return { iterations: it, comparisons: comp, swaps: sw, index: idx, pointer: ptr };
  }, [currentStep]);

  const handleCompleteTopic = async () => {
    try {
      const response = await api.get('/learning-progress');
      const progressList = response.data.data;
      const target = progressList.find(p => p.name.toLowerCase() === currentTemplate.name.toLowerCase());
      
      if (target) {
        await api.post('/learning-progress/update', {
          algorithmId: target.algorithmId,
          completionPercentage: 100,
          completed: true
        });
        alert(`Success! Marked "${currentTemplate.name}" as fully completed.`);
      }
    } catch (e) {
      console.error(e);
      alert('Could not update learning progress in MySQL.');
    }
  };

  // ==========================================
  // COMPARISON TAB LOGIC
  // ==========================================
  const [compTraceA, setCompTraceA] = useState([]);
  const [compTraceB, setCompTraceB] = useState([]);
  const [compIdx, setCompIdx] = useState(0);
  const [compIsPlaying, setCompIsPlaying] = useState(false);
  const [compIsLoading, setCompIsLoading] = useState(false);
  
  const compPair = useMemo(() => {
    return {
      algoA: { name: 'Naive Matching', template: COMPARISON_TEMPLATES.naiveMatch, time: 'O(N * M)', space: 'O(1)' },
      algoB: { name: 'KMP Search', template: COMPARISON_TEMPLATES.kmp, time: 'O(N + M)', space: 'O(M)' }
    };
  }, []);

  const compPlayIntervalRef = useRef(null);

  const stopCompPlay = () => {
    setCompIsPlaying(false);
    if (compPlayIntervalRef.current) {
      clearInterval(compPlayIntervalRef.current);
      compPlayIntervalRef.current = null;
    }
  };

  const handleStartComparison = async () => {
    setCompIsLoading(true);
    stopCompPlay();
    setCompTraceA([]);
    setCompTraceB([]);
    setCompIdx(0);

    try {
      const resA = await api.post('/execution/start', {
        className: compPair.algoA.template.className,
        code: compPair.algoA.template.javaCode,
        input: ''
      });
      const dataA = resA.data.data;
      const traceResA = await api.get(`/execution/${dataA.sessionId}/trace`);
      
      const resB = await api.post('/execution/start', {
        className: compPair.algoB.template.className,
        code: compPair.algoB.template.javaCode,
        input: ''
      });
      const dataB = resB.data.data;
      const traceResB = await api.get(`/execution/${dataB.sessionId}/trace`);

      setCompTraceA(traceResA.data.data);
      setCompTraceB(traceResB.data.data);
    } catch (err) {
      console.error(err);
      alert('Error fetching comparison traces.');
    } finally {
      setCompIsLoading(false);
    }
  };

  const toggleCompPlay = () => {
    if (compIsPlaying) {
      stopCompPlay();
    } else {
      const maxLen = Math.max(compTraceA.length, compTraceB.length);
      if (compIdx >= maxLen - 1) {
        setCompIdx(0);
      }
      setCompIsPlaying(true);
      compPlayIntervalRef.current = setInterval(() => {
        setCompIdx(prev => {
          if (prev < maxLen - 1) {
            return prev + 1;
          } else {
            setCompIsPlaying(false);
            if (compPlayIntervalRef.current) clearInterval(compPlayIntervalRef.current);
            return prev;
          }
        });
      }, speedMs);
    }
  };

  const stepCompForward = () => {
    stopCompPlay();
    const maxLen = Math.max(compTraceA.length, compTraceB.length);
    if (compIdx < maxLen - 1) {
      setCompIdx(compIdx + 1);
    }
  };

  const stepCompBackward = () => {
    stopCompPlay();
    if (compIdx > 0) {
      setCompIdx(compIdx - 1);
    }
  };

  const activeStepA = compTraceA[Math.min(compIdx, compTraceA.length - 1)] || null;
  const activeStepB = compTraceB[Math.min(compIdx, compTraceB.length - 1)] || null;

  const extractCompMetrics = (step) => {
    if (!step || !step.variables) return { iterations: 0, comparisons: 0, swaps: 0 };
    let it = 0, comp = 0, sw = 0;
    step.variables.forEach(v => {
      const name = v.name.toLowerCase();
      if (name === 'iterations' || name === 'iteration') it = parseInt(v.value, 10) || 0;
      if (name === 'comparisons' || name === 'comparison') comp = parseInt(v.value, 10) || 0;
      if (name === 'swaps' || name === 'swap') sw = parseInt(v.value, 10) || 0;
    });
    return { iterations: it, comparisons: comp, swaps: sw };
  };

  const metricsA = extractCompMetrics(activeStepA);
  const metricsB = extractCompMetrics(activeStepB);

  // ==========================================
  // PRACTICE ARENA STATE & LOGIC
  // ==========================================
  const [problems, setProblems] = useState([]);
  const [problemProgress, setProblemProgress] = useState([]);
  const [selectedProblem, setSelectedProblem] = useState(null);
  const [practiceCode, setPracticeCode] = useState('');
  const [practiceFeedback, setPracticeFeedback] = useState(null);
  const [isPracticeSubmitting, setIsPracticeSubmitting] = useState(false);

  const fetchPracticeData = async () => {
    try {
      const probRes = await api.get('/practice/problems');
      const progressRes = await api.get('/practice/progress');
      
      const probList = probRes.data.data.filter(p => p.category === 'STRING');
      setProblems(probList);
      setProblemProgress(progressRes.data.data);

      if (probList.length > 0 && !selectedProblem) {
        setSelectedProblem(probList[0]);
        setPracticeCode(probList[0].starterCode);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    if (activeTab === 'practice') {
      fetchPracticeData();
    }
  }, [activeTab]);

  const handleSelectProblem = (prob) => {
    setSelectedProblem(prob);
    setPracticeCode(prob.starterCode);
    setPracticeFeedback(null);
  };

  const handlePracticeSubmit = async () => {
    if (!selectedProblem) return;
    setIsPracticeSubmitting(true);
    setPracticeFeedback(null);

    try {
      const response = await api.post(`/practice/problems/${selectedProblem.id}/submit`, {
        code: practiceCode
      });
      const data = response.data.data;
      setPracticeFeedback(data);
      const progressRes = await api.get('/practice/progress');
      setProblemProgress(progressRes.data.data);
    } catch (err) {
      console.error(err);
      setPracticeFeedback({
        success: false,
        feedback: 'Submission Failed',
        compileError: err.response?.data?.message || 'Server error occurred during verification.'
      });
    } finally {
      setIsPracticeSubmitting(false);
    }
  };

  const practiceStats = useMemo(() => {
    const solved = problems.filter(p => problemProgress.find(pr => pr.problemId === p.id && pr.solved)).length;
    const attempts = problemProgress.filter(pr => problems.some(p => p.id === pr.problemId)).reduce((sum, pr) => sum + pr.attempts, 0);
    return { solved, attempts, accuracy: attempts > 0 ? Math.round((solved / attempts) * 100) : 0 };
  }, [problems, problemProgress]);

  // ==========================================
  // QUIZ ARENA STATE & LOGIC
  // ==========================================
  const [quizStarted, setQuizStarted] = useState(false);
  const [quizIdx, setQuizIdx] = useState(0);
  const [quizAnswers, setQuizAnswers] = useState({});
  const [quizChecked, setQuizChecked] = useState(false);
  const [quizFinished, setQuizFinished] = useState(false);
  const [quizProgressList, setQuizProgressList] = useState([]);

  const fetchQuizProgress = async () => {
    try {
      const response = await api.get('/quiz/progress');
      const filtered = response.data.data.filter(q => q.quizTitle.toLowerCase().includes('string'));
      setQuizProgressList(filtered);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    if (activeTab === 'quiz') {
      fetchQuizProgress();
    }
  }, [activeTab]);

  const handleStartQuiz = () => {
    setQuizStarted(true);
    setQuizIdx(0);
    setQuizAnswers({});
    setQuizChecked(false);
    setQuizFinished(false);
  };

  const handleSelectOption = (opt) => {
    if (quizChecked) return;
    setQuizAnswers({ ...quizAnswers, [quizIdx]: opt });
  };

  const handleNextQuizQuestion = async () => {
    setQuizChecked(false);
    if (quizIdx < STRING_QUIZ_QUESTIONS.length - 1) {
      setQuizIdx(quizIdx + 1);
    } else {
      setQuizFinished(true);
      // Submit score
      const score = STRING_QUIZ_QUESTIONS.filter((q, idx) => quizAnswers[idx] === q.answer).length;
      try {
        await api.post('/quiz/submit', {
          quizTitle: 'Strings Core Quiz',
          score: score,
          totalQuestions: STRING_QUIZ_QUESTIONS.length
        });
        fetchQuizProgress();
      } catch (e) {
        console.error(e);
      }
    }
  };

  return (
    <div className="dsa-page">
      <header className="dsa-header">
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <button onClick={() => navigate('/dashboard')} style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '1.1rem' }}>&larr; Back</button>
          <span style={{ fontSize: '1.2rem', fontWeight: 'bold', color: 'var(--text-primary)' }}>🔤 Strings Module</span>
        </div>
      </header>

      <nav className="tabs-navigation">
        <button onClick={() => setActiveTab('theory')} className={`tab-btn ${activeTab === 'theory' ? 'active' : ''}`}>Theory</button>
        <button onClick={() => setActiveTab('visualizer')} className={`tab-btn ${activeTab === 'visualizer' ? 'active' : ''}`}>Visualizer</button>
        <button onClick={() => setActiveTab('comparison')} className={`tab-btn ${activeTab === 'comparison' ? 'active' : ''}`}>Comparisons</button>
        <button onClick={() => setActiveTab('practice')} className={`tab-btn ${activeTab === 'practice' ? 'active' : ''}`}>Practice</button>
        <button onClick={() => setActiveTab('quiz')} className={`tab-btn ${activeTab === 'quiz' ? 'active' : ''}`}>Quiz</button>
      </nav>

      <main className="dsa-main">
        {activeTab === 'theory' && (
          <div className="theory-grid">
            <section className="theory-card">
              <h2>Internals & String Pool</h2>
              <p>In Java, Strings are immutable object sequences representing 16-bit char sequences.</p>
              <p>To reduce Heap memory allocations, Java stores all literal string instances in the <strong>String Constant Pool</strong>. When a new literal string is defined, the JVM first checks the pool. If found, it returns the reference, preventing duplicate heap allocation.</p>
              <table className="complexity-table">
                <thead>
                  <tr>
                    <th>Operation</th>
                    <th>Complexity</th>
                    <th>Explanation</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>Access (`charAt`)</td>
                    <td>O(1)</td>
                    <td>Direct array lookup.</td>
                  </tr>
                  <tr>
                    <td>Search (KMP)</td>
                    <td>O(N + M)</td>
                    <td>Linear search skip.</td>
                  </tr>
                  <tr>
                    <td>Search (Naive)</td>
                    <td>O(N * M)</td>
                    <td>Double nested scan.</td>
                  </tr>
                </tbody>
              </table>
            </section>
            <section className="theory-card">
              <h2>Key Patterns & Immutability</h2>
              <p><strong>Immutability Reasons:</strong> Immutability allows strings to be shared concurrently across threads. It also guarantees secure inputs since string arguments (like network sockets or file names) cannot be changed dynamically during execution.</p>
              <p>For operations involving extensive modifications (concatenations, replacements), it is highly recommended to use <code>StringBuilder</code> or <code>StringBuffer</code> to prevent garbage collection strain.</p>
            </section>
          </div>
        )}

        {activeTab === 'visualizer' && (
          <div className="visualizer-grid" style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '1.5rem' }}>
            <aside className="visualizer-sidebar" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem' }}>
              <h3 style={{ margin: '0 0 1rem 0', fontSize: '1rem', fontWeight: 600 }}>Algorithms</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {Object.keys(STRING_OPERATIONS).map(key => (
                  <button
                    key={key}
                    className={`algo-select-btn ${selectedAlgoKey === key ? 'active' : ''}`}
                    onClick={() => { setSelectedAlgoKey(key); resetPlayback(); }}
                    style={{ textAlignment: 'left', background: selectedAlgoKey === key ? 'var(--accent-bg)' : 'transparent', border: `1px solid ${selectedAlgoKey === key ? 'var(--accent)' : 'var(--border)'}`, color: selectedAlgoKey === key ? '#fff' : 'var(--text-secondary)', padding: '0.6rem 1rem', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontWeight: selectedAlgoKey === key ? 600 : 400 }}
                  >
                    {STRING_OPERATIONS[key].name}
                  </button>
                ))}
              </div>
              
              <div style={{ marginTop: '2rem', borderTop: '1px solid var(--border)', paddingTop: '1.5rem' }}>
                <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Complexity</h4>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-primary)' }}>Time: <strong style={{ color: 'var(--accent)' }}>{currentTemplate?.timeComplexity}</strong></div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-primary)', marginTop: '0.25rem' }}>Space: <strong>{currentTemplate?.spaceComplexity}</strong></div>
              </div>
            </aside>

            <section className="visualizer-workspace" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div className="visualizer-canvas-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.5rem', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '260px' }}>
                <StringVisualizer stepInfo={currentStep} />
              </div>

              <div className="visualizer-control-card" style={{ display: 'flex', gap: '1rem', alignItems: 'center', background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '12px', padding: '0.75rem 1.5rem', justifyContent: 'space-between', flexWrap: 'wrap' }}>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button onClick={handleCompileAndRun} disabled={isLoading} className="ctrl-btn active" style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.5rem 1.25rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                    {isLoading ? 'Compiling...' : '⚡ Run Code'}
                  </button>
                  {trace.length > 0 && (
                    <>
                      <button onClick={togglePlay} className="ctrl-btn" style={{ padding: '0.5rem 1rem' }}>{isPlaying ? '⏸️ Pause' : '▶️ Play'}</button>
                      <button onClick={handleStepBackward} disabled={stepIdx === 0} className="ctrl-btn" style={{ padding: '0.5rem' }}>&larr; Step</button>
                      <button onClick={handleStepForward} disabled={stepIdx >= trace.length - 1} className="ctrl-btn" style={{ padding: '0.5rem' }}>Step &rarr;</button>
                      <button onClick={handleRestart} className="ctrl-btn" style={{ padding: '0.5rem' }}>⟲ Restart</button>
                    </>
                  )}
                </div>

                {trace.length > 0 && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flex: 1, minWidth: '200px' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', whiteSpace: 'nowrap' }}>Step: {stepIdx + 1} / {trace.length}</span>
                    <input
                      type="range"
                      min="0"
                      max={trace.length - 1}
                      value={stepIdx}
                      onChange={(e) => { stopPlayTimer(); setStepIdx(parseInt(e.target.value, 10)); }}
                      style={{ flex: 1, accentColor: 'var(--accent)' }}
                    />
                  </div>
                )}
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 320px', gap: '1.5rem', minHeight: '360px' }}>
                <div className="editor-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem', alignItems: 'center' }}>
                    <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Java Source Editor</span>
                    <button onClick={handleCompleteTopic} className="complete-btn" style={{ background: 'transparent', border: '1px solid var(--accent)', color: 'var(--accent)', padding: '0.25rem 0.75rem', borderRadius: '6px', fontSize: '0.75rem', cursor: 'pointer' }}>Mark Completed</button>
                  </div>
                  <div style={{ flex: 1, borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--border)' }}>
                    <Editor
                      height="100%"
                      language="java"
                      theme="vs-dark"
                      value={editorCode}
                      onChange={(val) => setEditorCode(val)}
                      onMount={handleEditorDidMount}
                      options={{ minimap: { enabled: false }, fontSize: 13, scrollBeyondLastLine: false }}
                    />
                  </div>
                  {compileError && (
                    <div style={{ background: 'rgba(255,92,108,0.1)', border: '1px solid var(--error)', color: 'var(--error)', borderRadius: '8px', padding: '0.75rem', marginTop: '1rem', fontSize: '0.85rem', fontFamily: 'var(--mono)', overflow: 'auto', maxHeight: '120px' }}>{compileError}</div>
                  )}
                </div>

                <div className="inspector-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                  <div>
                    <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Execution Summary</h4>
                    <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-primary)', lineHeight: '1.5' }}>{currentStep?.explanation}</p>
                  </div>

                  <div>
                    <h4 style={{ margin: '0 0 0.75rem 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Execution Metrics</h4>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                      <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.5rem', borderRadius: '8px', textAlign: 'center' }}>
                        <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>Iterations</span>
                        <div style={{ fontSize: '1.1rem', fontWeight: 'bold', color: 'var(--text-primary)', marginTop: '2px' }}>{metrics.iterations}</div>
                      </div>
                      <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.5rem', borderRadius: '8px', textAlign: 'center' }}>
                        <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>Comparisons</span>
                        <div style={{ fontSize: '1.1rem', fontWeight: 'bold', color: 'var(--text-primary)', marginTop: '2px' }}>{metrics.comparisons}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </div>
        )}

        {activeTab === 'comparison' && (
          <div>
            <div className="visualizer-control-card" style={{ display: 'flex', gap: '1rem', alignItems: 'center', background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '12px', padding: '0.75rem 1.5rem' }}>
              <button onClick={handleStartComparison} disabled={compIsLoading} className="ctrl-btn active" style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.4rem 1.25rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                {compIsLoading ? 'Comparing...' : '📊 Compare Naive vs KMP'}
              </button>
              {compTraceA.length > 0 && (
                <>
                  <button onClick={toggleCompPlay} className="ctrl-btn">{compIsPlaying ? 'Pause' : 'Resume'}</button>
                  <button onClick={stepCompBackward} disabled={compIdx === 0} className="ctrl-btn">&larr;</button>
                  <button onClick={stepCompForward} disabled={compIdx >= Math.max(compTraceA.length, compTraceB.length) - 1} className="ctrl-btn">&rarr;</button>
                  <input
                    type="range"
                    min="0"
                    max={Math.max(compTraceA.length, compTraceB.length) - 1}
                    value={compIdx}
                    onChange={(e) => { stopCompPlay(); setCompIdx(parseInt(e.target.value, 10)); }}
                    style={{ flex: 1, accentColor: 'var(--accent)' }}
                  />
                </>
              )}
            </div>

            <div className="comparison-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginTop: '1.5rem' }}>
              <div className="comparison-column" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3 style={{ margin: 0, fontWeight: 700, color: 'var(--accent)', fontSize: '1.1rem', display: 'flex', justifyContent: 'space-between' }}>
                  <span>{compPair.algoA.name}</span>
                  <span style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.2)' }}>Time: {compPair.algoA.time}</span>
                </h3>
                <div style={{ height: '140px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.1)', borderRadius: '10px' }}>
                  <StringVisualizer stepInfo={activeStepA} />
                </div>
                <div style={{ height: '220px', borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--border)' }}>
                  <Editor height="100%" language="java" theme="vs-dark" value={compPair.algoA.template.javaCode} options={{ readOnly: true, minimap: { enabled: false }, fontSize: 11 }} />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.5rem', borderRadius: '8px', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>Iterations</span>
                    <div style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>{metricsA.iterations}</div>
                  </div>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.5rem', borderRadius: '8px', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>Comparisons</span>
                    <div style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>{metricsA.comparisons}</div>
                  </div>
                </div>
              </div>

              <div className="comparison-column" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3 style={{ margin: 0, fontWeight: 700, color: '#3ec6e0', fontSize: '1.1rem', display: 'flex', justifyContent: 'space-between' }}>
                  <span>{compPair.algoB.name}</span>
                  <span style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.2)' }}>Time: {compPair.algoB.time}</span>
                </h3>
                <div style={{ height: '140px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.1)', borderRadius: '10px' }}>
                  <StringVisualizer stepInfo={activeStepB} />
                </div>
                <div style={{ height: '220px', borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--border)' }}>
                  <Editor height="100%" language="java" theme="vs-dark" value={compPair.algoB.template.javaCode} options={{ readOnly: true, minimap: { enabled: false }, fontSize: 11 }} />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.5rem', borderRadius: '8px', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>Iterations</span>
                    <div style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>{metricsB.iterations}</div>
                  </div>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '0.5rem', borderRadius: '8px', textAlign: 'center' }}>
                    <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>Comparisons</span>
                    <div style={{ fontSize: '1.1rem', fontWeight: 'bold' }}>{metricsB.comparisons}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'practice' && (
          <div>
            <div className="practice-stats" style={{ display: 'flex', gap: '1.5rem', marginBottom: '1.5rem' }}>
              <div className="stat-box" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '12px', padding: '1rem', flex: 1, textAlign: 'center' }}>
                <div className="title" style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Solved Problems</div>
                <div className="number" style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--text-primary)', marginTop: '0.25rem' }}>{practiceStats.solved} / {problems.length}</div>
              </div>
              <div className="stat-box" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '12px', padding: '1rem', flex: 1, textAlign: 'center' }}>
                <div className="title" style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Total Attempts</div>
                <div className="number" style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--text-primary)', marginTop: '0.25rem' }}>{practiceStats.attempts}</div>
              </div>
            </div>

            <div className="practice-grid" style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '1.5rem' }}>
              <aside className="problems-sidebar" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1rem', fontWeight: 600 }}>Problems</h3>
                {problems.map(prob => {
                  const progress = problemProgress.find(p => p.problemId === prob.id);
                  return (
                    <button
                      key={prob.id}
                      className={`problem-item-btn ${selectedProblem?.id === prob.id ? 'active' : ''}`}
                      onClick={() => handleSelectProblem(prob)}
                      style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: selectedProblem?.id === prob.id ? 'var(--accent-bg)' : 'transparent', border: `1px solid ${selectedProblem?.id === prob.id ? 'var(--accent)' : 'var(--border)'}`, color: selectedProblem?.id === prob.id ? '#fff' : 'var(--text-secondary)', padding: '0.75rem 1rem', borderRadius: '8px', cursor: 'pointer', textAlign: 'left' }}
                    >
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', alignItems: 'flex-start' }}>
                        <span style={{ fontSize: '0.9rem', fontWeight: 600 }}>{prob.title}</span>
                        <span className={`difficulty-badge ${prob.difficulty.toLowerCase()}`} style={{ fontSize: '0.7rem', padding: '1px 6px', borderRadius: '4px', background: prob.difficulty === 'EASY' ? 'rgba(46,202,127,0.1)' : 'rgba(255,179,64,0.1)', color: prob.difficulty === 'EASY' ? 'var(--success)' : 'var(--accent)' }}>{prob.difficulty}</span>
                      </div>
                      {progress?.solved && <span style={{ color: 'var(--success)', fontWeight: 'bold' }}>✔</span>}
                    </button>
                  );
                })}
              </aside>

              {selectedProblem && (
                <section className="visualizer-workspace" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                  <div className="workspace-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                      <span style={{ fontWeight: 'bold', fontSize: '1rem' }}>{selectedProblem.title}</span>
                      <span style={{ fontSize: '0.8rem', padding: '2px 8px', borderRadius: '4px', background: 'rgba(255,255,255,0.05)' }}>{selectedProblem.difficulty}</span>
                    </div>
                    <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: '1.6', whiteSpace: 'pre-wrap' }}>{selectedProblem.description}</p>
                    <div style={{ marginTop: '0.75rem', padding: '0.5rem 0.75rem', background: 'rgba(0,0,0,0.15)', borderRadius: '6px', fontSize: '0.85rem' }}>
                      <strong>Expected Output:</strong> <code style={{ color: 'var(--accent)', fontFamily: 'var(--mono)' }}>{selectedProblem.expectedOutput}</code>
                    </div>
                  </div>

                  <div className="workspace-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontWeight: 600 }}>Java Solution Editor</span>
                      <button onClick={handlePracticeSubmit} disabled={isPracticeSubmitting} className="ctrl-btn active" style={{ background: 'var(--success)', color: '#fff', border: 'none', padding: '0.4rem 1.25rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                        {isPracticeSubmitting ? 'Evaluating...' : '🚀 Submit Solution'}
                      </button>
                    </div>
                    <div style={{ height: '360px', borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--border)' }}>
                      <Editor height="100%" language="java" theme="vs-dark" value={practiceCode} onChange={(val) => setPracticeCode(val)} options={{ minimap: { enabled: false }, fontSize: 13, scrollBeyondLastLine: false }} />
                    </div>

                    {practiceFeedback && (
                      <div style={{ padding: '1rem', borderRadius: '8px', background: practiceFeedback.success ? 'rgba(46, 202, 127, 0.08)' : 'rgba(255, 92, 108, 0.08)', border: `1px solid ${practiceFeedback.success ? 'var(--success)' : 'var(--error)'}` }}>
                        <h4 style={{ margin: '0 0 0.5rem 0', color: practiceFeedback.success ? 'var(--success)' : 'var(--error)' }}>
                          {practiceFeedback.success ? '🎉 Solution Verified Successfully!' : '❌ Evaluation Failed'}
                        </h4>
                        <p style={{ margin: 0, fontSize: '0.85rem' }}>{practiceFeedback.feedback}</p>
                        {practiceFeedback.compileError && (
                          <pre style={{ marginTop: '0.75rem', padding: '0.5rem', background: '#000', borderRadius: '4px', color: 'var(--error)', fontSize: '0.8rem', fontFamily: 'var(--mono)', overflowX: 'auto', whiteSpace: 'pre-wrap' }}>{practiceFeedback.compileError}</pre>
                        )}
                        {practiceFeedback.output && (
                          <div style={{ marginTop: '0.75rem' }}>
                            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Console Output:</span>
                            <pre style={{ margin: '0.25rem 0 0 0', padding: '0.5rem', background: '#000', borderRadius: '4px', color: '#fff', fontSize: '0.8rem', fontFamily: 'var(--mono)' }}>{practiceFeedback.output}</pre>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </section>
              )}
            </div>
          </div>
        )}

        {activeTab === 'quiz' && (
          <div>
            {!quizStarted && (
              <div className="theory-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '2rem', textAlign: 'center' }}>
                <span style={{ fontSize: '3rem' }}>🧠</span>
                <h2 style={{ marginTop: '1rem', fontSize: '1.5rem' }}>Strings Core Quiz</h2>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                  Test your understanding of String Constant Pool, immutability, and pattern matching string search algorithms.
                </p>

                {quizProgressList.length > 0 && (
                  <div style={{ marginBottom: '2rem', padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '8px', border: '1px solid var(--border)', textAlign: 'left', maxWidth: '600px', margin: '0 auto 2rem auto' }}>
                    <h4 style={{ margin: '0 0 0.5rem 0' }}>Previous Attempt Scores</h4>
                    {quizProgressList.map((att, idx) => (
                      <div key={idx} style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', display: 'flex', justifyContent: 'space-between', padding: '0.25rem 0' }}>
                        <span>{att.quizTitle}</span>
                        <strong>Score: {att.score}/{att.totalQuestions} ({att.percentage.toFixed(0)}%)</strong>
                      </div>
                    ))}
                  </div>
                )}

                <button onClick={handleStartQuiz} className="ctrl-btn active" style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.75rem 2rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600, fontSize: '1rem' }}>
                  Start Quiz
                </button>
              </div>
            )}

            {quizStarted && !quizFinished && (
              <div className="quiz-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '2rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1rem' }}>
                  <span>Question {quizIdx + 1} of {STRING_QUIZ_QUESTIONS.length}</span>
                  <span style={{ fontWeight: 600, color: 'var(--accent)' }}>Type: {STRING_QUIZ_QUESTIONS[quizIdx].type}</span>
                </div>

                <h3 style={{ margin: '0 0 1.5rem 0', fontWeight: 600, fontSize: '1.2rem', whiteSpace: 'pre-wrap' }}>
                  {STRING_QUIZ_QUESTIONS[quizIdx].question}
                </h3>

                <div className="quiz-options-list" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.5rem' }}>
                  {STRING_QUIZ_QUESTIONS[quizIdx].options.map((opt, i) => {
                    const isSelected = quizAnswers[quizIdx] === opt;
                    const isCorrect = opt === STRING_QUIZ_QUESTIONS[quizIdx].answer;
                    
                    let btnStyle = {
                      textAlign: 'left',
                      background: 'rgba(255,255,255,0.02)',
                      border: '1px solid var(--border)',
                      color: 'var(--text-primary)',
                      padding: '1rem',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      transition: 'all 0.2s'
                    };

                    if (quizChecked) {
                      if (isCorrect) {
                        btnStyle.borderColor = 'var(--success)';
                        btnStyle.background = 'rgba(46, 202, 127, 0.08)';
                      } else if (isSelected) {
                        btnStyle.borderColor = 'var(--error)';
                        btnStyle.background = 'rgba(255, 92, 108, 0.08)';
                      }
                    } else if (isSelected) {
                      btnStyle.borderColor = 'var(--accent)';
                      btnStyle.background = 'var(--accent-bg)';
                    }

                    return (
                      <button key={i} style={btnStyle} onClick={() => handleSelectOption(opt)}>
                        {opt}
                      </button>
                    );
                  })}
                </div>

                {quizChecked && (
                  <div style={{ background: 'rgba(108, 99, 255, 0.08)', borderLeft: '4px solid var(--accent)', borderRadius: '4px', padding: '1rem', marginBottom: '1.5rem', fontSize: '0.9rem', color: 'var(--text-primary)', textAlign: 'left' }}>
                    💡 <strong>Explanation:</strong> {STRING_QUIZ_QUESTIONS[quizIdx].explanation}
                  </div>
                )}

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                  {!quizChecked ? (
                    <button
                      onClick={() => setQuizChecked(true)}
                      disabled={!quizAnswers[quizIdx]}
                      className="ctrl-btn active"
                      style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.5rem 1.5rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}
                    >
                      Check Answer
                    </button>
                  ) : (
                    <button
                      onClick={handleNextQuizQuestion}
                      className="ctrl-btn active"
                      style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.5rem 1.5rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}
                    >
                      {quizIdx < STRING_QUIZ_QUESTIONS.length - 1 ? 'Next Question' : 'Finish Quiz'}
                    </button>
                  )}
                </div>
              </div>
            )}

            {quizStarted && quizFinished && (
              <div className="theory-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '2rem', textAlign: 'center' }}>
                <span style={{ fontSize: '3rem' }}>🎉</span>
                <h2 style={{ marginTop: '1rem' }}>Quiz Completed!</h2>
                <p style={{ color: 'var(--text-secondary)' }}>
                  You have completed the Strings Core Quiz. Your final score is:
                </p>
                <div style={{ fontSize: '3rem', fontWeight: 800, color: 'var(--accent)', margin: '1.5rem 0' }}>
                  {STRING_QUIZ_QUESTIONS.filter((q, idx) => quizAnswers[idx] === q.answer).length} / {STRING_QUIZ_QUESTIONS.length}
                </div>
                <button onClick={handleStartQuiz} className="ctrl-btn" style={{ padding: '0.6rem 1.5rem', borderRadius: '8px', cursor: 'pointer', marginRight: '1rem' }}>
                  Retry Quiz
                </button>
                <button onClick={() => setQuizStarted(false)} className="ctrl-btn active" style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.6rem 1.5rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                  Back to Quiz Home
                </button>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
};

export default StringsPage;
