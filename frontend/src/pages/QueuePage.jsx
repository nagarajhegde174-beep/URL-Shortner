import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import QueueVisualizer from '../components/visualization/components/QueueVisualizer';
import { QUEUE_OPERATIONS, COMPARISON_TEMPLATES, QUEUE_QUIZ_QUESTIONS } from '../utils/queueTemplates';
import '../styles/arraysPage.css'; // Reuse core layouts
import '../styles/dsaModules.css';

const QueuePage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // Navigation State
  const [activeTab, setActiveTab] = useState(() => localStorage.getItem('queue_selected_tab') || 'theory');

  useEffect(() => {
    localStorage.setItem('queue_selected_tab', activeTab);
  }, [activeTab]);

  // Operations State
  const [selectedAlgoKey, setSelectedAlgoKey] = useState('queueArray');

  const currentTemplate = useMemo(() => {
    return QUEUE_OPERATIONS[selectedAlgoKey];
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
  const [speedMs, setSpeedMs] = useState(() => parseInt(localStorage.getItem('queue_speed') || '1200', 10));
  const [compileError, setCompileError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    localStorage.setItem('queue_speed', speedMs.toString());
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
      return { iterations: 0, comparisons: 0, swaps: 0 };
    }
    let it = 0, comp = 0, sw = 0;
    currentStep.variables.forEach(v => {
      const name = v.name.toLowerCase();
      if (name === 'iterations' || name === 'iteration') it = parseInt(v.value, 10) || 0;
      if (name === 'comparisons' || name === 'comparison') comp = parseInt(v.value, 10) || 0;
      if (name === 'swaps' || name === 'swap') sw = parseInt(v.value, 10) || 0;
    });
    return { iterations: it, comparisons: comp, swaps: sw };
  }, [currentStep]);

  const resetPlayback = () => {
    stopPlayTimer();
    setTrace([]);
    setStepIdx(0);
    setSessionId(null);
    setCompileError(null);
  };

  const handleCompileAndRun = async () => {
    resetPlayback();
    setIsLoading(true);
    try {
      const startRes = await api.post('/execution/start', {
        className: currentTemplate.className,
        code: editorCode,
        input: ''
      });
      const data = startRes.data.data;
      if (data.sessionId) {
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
      alert('Could not update learning progress.');
    }
  };

  // ==========================================
  // COMPARISON TAB STATE & LOGIC
  // ==========================================
  const [compTraceA, setCompTraceA] = useState([]);
  const [compTraceB, setCompTraceB] = useState([]);
  const [compIdx, setCompIdx] = useState(0);
  const [compIsPlaying, setCompIsPlaying] = useState(false);
  const [compIsLoading, setCompIsLoading] = useState(false);

  const compPair = useMemo(() => {
    return {
      algoA: { name: 'Array Queue', template: COMPARISON_TEMPLATES.queueArray, time: 'O(1)', space: 'O(N)' },
      algoB: { name: 'Java Queue', template: COMPARISON_TEMPLATES.javaQueue, time: 'O(1)', space: 'O(N)' }
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
            stopCompPlay();
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

      const probList = probRes.data.data.filter(p => p.category === 'QUEUE');
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
        consoleOutput: 'Error occurred during server request.'
      });
    } finally {
      setIsPracticeSubmitting(false);
    }
  };

  const practiceStats = useMemo(() => {
    if (problems.length === 0) return { solved: 0, attempts: 0 };
    let solved = 0;
    let attempts = 0;
    problems.forEach(p => {
      const progress = problemProgress.find(pr => pr.problemId === p.id);
      if (progress) {
        if (progress.solved) solved++;
        attempts += progress.attemptsCount || 0;
      }
    });
    return { solved, attempts };
  }, [problems, problemProgress]);

  // ==========================================
  // QUIZ STATE & LOGIC
  // ==========================================
  const [quizAnswers, setQuizAnswers] = useState({});
  const [quizSubmitted, setQuizSubmitted] = useState(false);
  const [quizScore, setQuizScore] = useState(0);

  const handleOptionSelect = (qIdx, opt) => {
    if (quizSubmitted) return;
    setQuizAnswers(prev => ({ ...prev, [qIdx]: opt }));
  };

  const handleQuizSubmit = async () => {
    let score = 0;
    QUEUE_QUIZ_QUESTIONS.forEach((q, idx) => {
      if (quizAnswers[idx] === q.answer) score++;
    });
    setQuizScore(score);
    setQuizSubmitted(true);

    try {
      await api.post('/quiz/submit', {
        category: 'QUEUE',
        score: score,
        totalQuestions: QUEUE_QUIZ_QUESTIONS.length
      });
    } catch (e) {
      console.error('Failed to submit quiz progress:', e);
    }
  };

  const handleResetQuiz = () => {
    setQuizAnswers({});
    setQuizSubmitted(false);
    setQuizScore(0);
  };

  return (
    <div className="dsa-page">
      {/* Upper Navigation & Branding Banner */}
      <header className="dsa-header">
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <button onClick={() => navigate('/dashboard')} style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '1.1rem' }}>&larr; Back</button>
          <span style={{ fontSize: '1.2rem', fontWeight: 'bold', color: 'var(--text-primary)' }}>👥 Queues Module</span>
        </div>
      </header>

      <nav className="tabs-navigation">
        <button onClick={() => setActiveTab('theory')} className={`tab-btn ${activeTab === 'theory' ? 'active' : ''}`}>Theory</button>
        <button onClick={() => setActiveTab('visualizer')} className={`tab-btn ${activeTab === 'visualizer' ? 'active' : ''}`}>Visualizer</button>
        <button onClick={() => setActiveTab('comparison')} className={`tab-btn ${activeTab === 'comparison' ? 'active' : ''}`}>Comparisons</button>
        <button onClick={() => setActiveTab('practice')} className={`tab-btn ${activeTab === 'practice' ? 'active' : ''}`}>Practice</button>
        <button onClick={() => setActiveTab('quiz')} className={`tab-btn ${activeTab === 'quiz' ? 'active' : ''}`}>Quiz</button>
      </nav>

      {/* Main Workspace Frame */}
      <main className="dsa-main">
        {activeTab === 'theory' && (
          <div className="theory-scroll-pane" style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '2rem', padding: '1rem' }}>
            <section className="theory-markdown-content" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '20px', padding: '2rem' }}>
              <div>
                <h1 style={{ fontWeight: 800, margin: '0 0 1rem 0' }}>The Queue Data Structure</h1>
                <p style={{ fontSize: '1rem', color: 'var(--text-secondary)', lineHeight: '1.7' }}>
                  A Queue is a linear data structure that follows the <strong>FIFO (First In First Out)</strong> principle.
                  Imagine a line of people waiting for a movie ticket: the person who gets in line first is served first.
                </p>
              </div>

              <div style={{ borderLeft: '4px solid var(--accent)', paddingLeft: '1.25rem', margin: '0.5rem 0' }}>
                <h3 style={{ margin: '0 0 0.5rem 0' }}>Key Characteristics</h3>
                <ul style={{ paddingLeft: '1.25rem', color: 'var(--text-secondary)', lineHeight: '1.6' }}>
                  <li><strong>Two Access Points:</strong> Insert at the Rear, remove from the Front.</li>
                  <li><strong>FIFO Property:</strong> The first element enqueued is the first dequeued.</li>
                  <li><strong>Modulus Wrapping:</strong> Circular queues reuse empty slots at the beginning of the array.</li>
                </ul>
              </div>

              <div>
                <h2 style={{ fontWeight: 700, margin: '1.5rem 0 1rem 0' }}>Core Operations</h2>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '10px', border: '1px solid var(--border)' }}>
                    <h4 style={{ margin: '0 0 0.25rem 0', color: 'var(--accent)' }}>Enqueue</h4>
                    <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Adds an element to the rear pointer. Increment with modulo in circular queues.</p>
                  </div>
                  <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '10px', border: '1px solid var(--border)' }}>
                    <h4 style={{ margin: '0 0 0.25rem 0', color: 'var(--accent)' }}>Dequeue</h4>
                    <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Removes and returns the front element.</p>
                  </div>
                  <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '10px', border: '1px solid var(--border)' }}>
                    <h4 style={{ margin: '0 0 0.25rem 0', color: 'var(--accent)' }}>Peek</h4>
                    <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Returns the front element without removing it.</p>
                  </div>
                  <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '10px', border: '1px solid var(--border)' }}>
                    <h4 style={{ margin: '0 0 0.25rem 0', color: 'var(--accent)' }}>PriorityQueue</h4>
                    <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Orders elements by value/priority using a heap internally.</p>
                  </div>
                </div>
              </div>
            </section>

            <aside className="theory-sidebar-topics" style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem' }}>
                <h3 style={{ margin: '0 0 1rem 0', fontSize: '1rem' }}>Syllabus topics</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {Object.keys(QUEUE_OPERATIONS).map(key => (
                    <button
                      key={key}
                      onClick={() => { setSelectedAlgoKey(key); setActiveTab('visualizer'); }}
                      style={{ textAlign: 'left', background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border)', color: 'var(--text-primary)', padding: '0.75rem 1rem', borderRadius: '8px', cursor: 'pointer', fontSize: '0.85rem', transition: 'all 0.2s' }}
                    >
                      {QUEUE_OPERATIONS[key].name}
                    </button>
                  ))}
                </div>
              </div>
            </aside>
          </div>
        )}

        {activeTab === 'visualizer' && (
          <div className="visualizer-tab-content" style={{ display: 'grid', gridTemplateColumns: '260px 1fr', gap: '1.5rem', height: '100%' }}>
            <aside className="visualizer-sidebar-topics" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <h3 style={{ margin: 0, fontSize: '1rem', fontWeight: 700 }}>Select Algorithm</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', overflowY: 'auto' }}>
                {Object.keys(QUEUE_OPERATIONS).map(key => (
                  <button
                    key={key}
                    className={`topic-select-btn ${selectedAlgoKey === key ? 'active' : ''}`}
                    onClick={() => { setSelectedAlgoKey(key); resetPlayback(); }}
                    style={{ textAlign: 'left', display: 'block', width: '100%', padding: '0.75rem 1rem', borderRadius: '8px', border: `1px solid ${selectedAlgoKey === key ? 'var(--accent)' : 'var(--border)'}`, background: selectedAlgoKey === key ? 'var(--accent-bg)' : 'transparent', color: selectedAlgoKey === key ? '#fff' : 'var(--text-secondary)', cursor: 'pointer', transition: 'all 0.2s' }}
                  >
                    {QUEUE_OPERATIONS[key].name}
                  </button>
                ))}
              </div>

              <div style={{ marginTop: 'auto', borderTop: '1px solid var(--border)', paddingTop: '1.5rem' }}>
                <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Complexity</h4>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-primary)' }}>Time: <strong style={{ color: 'var(--accent)' }}>{currentTemplate?.timeComplexity}</strong></div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-primary)', marginTop: '0.25rem' }}>Space: <strong>{currentTemplate?.spaceComplexity}</strong></div>
              </div>
            </aside>

            <section className="visualizer-workspace" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              <div className="visualizer-canvas-card" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.5rem', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '260px' }}>
                <QueueVisualizer stepInfo={currentStep} />
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
            <div className="visualizer-control-card" style={{ display: 'flex', gap: '1rem', alignItems: 'center', background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '12px', padding: '0.75rem 1.5rem', marginBottom: '1.5rem' }}>
              <button onClick={handleStartComparison} disabled={compIsLoading} className="ctrl-btn active" style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.4rem 1.25rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                {compIsLoading ? 'Comparing...' : '📊 Run Queue Comparison'}
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

            <div className="comparison-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div className="comparison-column" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3 style={{ margin: 0, fontWeight: 700, color: 'var(--accent)', fontSize: '1.1rem', display: 'flex', justifyContent: 'space-between' }}>
                  <span>{compPair.algoA.name}</span>
                  <span style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.2)' }}>Time: {compPair.algoA.time}</span>
                </h3>
                <div style={{ height: '220px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.1)', borderRadius: '10px' }}>
                  <QueueVisualizer stepInfo={activeStepA} />
                </div>
                <div style={{ height: '220px', borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--border)' }}>
                  <Editor height="100%" language="java" theme="vs-dark" value={compPair.algoA.template.javaCode} options={{ readOnly: true, minimap: { enabled: false }, fontSize: 11 }} />
                </div>
              </div>

              <div className="comparison-column" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3 style={{ margin: 0, fontWeight: 700, color: '#3ec6e0', fontSize: '1.1rem', display: 'flex', justifyContent: 'space-between' }}>
                  <span>{compPair.algoB.name}</span>
                  <span style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.2)' }}>Time: {compPair.algoB.time}</span>
                </h3>
                <div style={{ height: '220px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.1)', borderRadius: '10px' }}>
                  <QueueVisualizer stepInfo={activeStepB} />
                </div>
                <div style={{ height: '220px', borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--border)' }}>
                  <Editor height="100%" language="java" theme="vs-dark" value={compPair.algoB.template.javaCode} options={{ readOnly: true, minimap: { enabled: false }, fontSize: 11 }} />
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
                    <h3 style={{ margin: '0 0 0.5rem 0', fontWeight: 700 }}>{selectedProblem.title}</h3>
                    <p style={{ margin: '0 0 1.25rem 0', fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: '1.5' }}>{selectedProblem.description}</p>
                    
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
                      <div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Input Constraints</span>
                        <div style={{ fontFamily: 'var(--mono)', fontSize: '0.8rem', marginTop: '2px' }}>Expected Class Name: <strong>{selectedProblem.expectedClassName}</strong></div>
                      </div>
                      <div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Expected Output</span>
                        <pre style={{ margin: '4px 0 0 0', background: 'rgba(0,0,0,0.2)', padding: '6px 12px', borderRadius: '6px', fontSize: '0.8rem', fontFamily: 'var(--mono)', width: 'fit-content' }}>{selectedProblem.expectedOutput}</pre>
                      </div>
                    </div>
                  </div>

                  <div style={{ height: '360px', borderRadius: '16px', overflow: 'hidden', border: '1px solid var(--border)', background: 'var(--bg-card)', padding: '1.25rem', display: 'flex', flexDirection: 'column' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.75rem', alignItems: 'center' }}>
                      <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Solution Source Editor</span>
                      <button onClick={handlePracticeSubmit} disabled={isPracticeSubmitting} className="ctrl-btn active" style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.4rem 1.25rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                        {isPracticeSubmitting ? 'Submitting...' : '🚀 Submit Code'}
                      </button>
                    </div>
                    <div style={{ flex: 1, borderRadius: '8px', overflow: 'hidden', border: '1px solid var(--border)' }}>
                      <Editor height="100%" language="java" theme="vs-dark" value={practiceCode} onChange={(val) => setPracticeCode(val)} />
                    </div>
                  </div>

                  {practiceFeedback && (
                    <div style={{ background: 'var(--bg-card)', border: `1px solid ${practiceFeedback.success ? 'var(--success)' : 'var(--error)'}`, borderRadius: '16px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                      <h4 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem', color: practiceFeedback.success ? 'var(--success)' : 'var(--error)' }}>
                        {practiceFeedback.success ? '🟢 Solution Correct!' : '🔴 Incorrect Output'}
                      </h4>
                      <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-primary)' }}>{practiceFeedback.feedback}</p>
                      {practiceFeedback.consoleOutput && (
                        <div>
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Console Output:</span>
                          <pre style={{ margin: '4px 0 0 0', background: 'rgba(0,0,0,0.2)', padding: '8px 12px', borderRadius: '6px', fontSize: '0.8rem', fontFamily: 'var(--mono)', overflow: 'auto', maxHeight: '100px' }}>{practiceFeedback.consoleOutput}</pre>
                        </div>
                      )}
                    </div>
                  )}
                </section>
              )}
            </div>
          </div>
        )}

        {activeTab === 'quiz' && (
          <div className="quiz-pane" style={{ maxWidth: '800px', margin: '0 auto', padding: '1rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '20px', padding: '2rem' }}>
              <h2 style={{ margin: '0 0 0.5rem 0', fontWeight: 800 }}>Queue Knowledge Check</h2>
              <p style={{ margin: '0 0 1.5rem 0', color: 'var(--text-secondary)' }}>Verify your understanding of FIFO ordering, circular queues, and PriorityQueue heaps.</p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
                {QUEUE_QUIZ_QUESTIONS.map((q, qIdx) => {
                  const selectedOpt = quizAnswers[qIdx];
                  return (
                    <div key={qIdx} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', borderBottom: '1px solid rgba(255,255,255,0.03)', paddingBottom: '1.5rem' }}>
                      <h4 style={{ margin: 0, display: 'flex', gap: '0.5rem', fontSize: '0.95rem' }}>
                        <span>{qIdx + 1}.</span>
                        <span>{q.question}</span>
                      </h4>
                      
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        {q.options.map((opt, optIdx) => {
                          const isSelected = selectedOpt === opt;
                          const isCorrect = q.answer === opt;
                          let btnStyle = {
                            background: 'rgba(255,255,255,0.02)',
                            border: '1px solid var(--border)',
                            color: 'var(--text-secondary)'
                          };
                          
                          if (isSelected) {
                            if (quizSubmitted) {
                              btnStyle = {
                                background: isCorrect ? 'rgba(46,202,127,0.1)' : 'rgba(255,92,108,0.1)',
                                border: `1px solid ${isCorrect ? 'var(--success)' : 'var(--error)'}`,
                                color: isCorrect ? 'var(--success)' : 'var(--error)'
                              };
                            } else {
                              btnStyle = {
                                background: 'var(--accent-bg)',
                                border: '1px solid var(--accent)',
                                color: '#fff'
                              };
                            }
                          } else if (quizSubmitted && isCorrect) {
                            // Highlight the correct answer if not selected
                            btnStyle = {
                              background: 'rgba(46,202,127,0.05)',
                              border: '1px dashed var(--success)',
                              color: 'var(--success)'
                            };
                          }

                          return (
                            <button
                              key={optIdx}
                              onClick={() => handleOptionSelect(qIdx, opt)}
                              style={{ 
                                textAlign: 'left', 
                                padding: '0.75rem 1rem', 
                                borderRadius: '8px', 
                                cursor: 'pointer', 
                                fontSize: '0.85rem',
                                transition: 'all 0.2s',
                                ...btnStyle
                              }}
                            >
                              {opt}
                            </button>
                          );
                        })}
                      </div>

                      {quizSubmitted && (
                        <div style={{ background: 'rgba(255,255,255,0.01)', padding: '0.75rem 1rem', borderRadius: '8px', fontSize: '0.8rem', color: 'var(--text-secondary)', borderLeft: '3px solid var(--accent)' }}>
                          <strong>Explanation:</strong> {q.explanation}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              <div style={{ marginTop: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                {!quizSubmitted ? (
                  <button 
                    onClick={handleQuizSubmit} 
                    disabled={Object.keys(quizAnswers).length < QUEUE_QUIZ_QUESTIONS.length}
                    style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.75rem 1.75rem', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
                  >
                    Submit Quiz
                  </button>
                ) : (
                  <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center', width: '100%', justifyContent: 'space-between' }}>
                    <div style={{ fontSize: '1rem', fontWeight: 'bold' }}>
                      Your Score: <span style={{ color: quizScore >= QUEUE_QUIZ_QUESTIONS.length / 2 ? 'var(--success)' : 'var(--error)' }}>{quizScore} / {QUEUE_QUIZ_QUESTIONS.length}</span>
                    </div>
                    <button 
                      onClick={handleResetQuiz} 
                      style={{ background: 'transparent', border: '1px solid var(--border)', color: 'var(--text-primary)', padding: '0.5rem 1.25rem', borderRadius: '8px', cursor: 'pointer' }}
                    >
                      Reset Quiz
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
};

export default QueuePage;
