import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import LinkedListVisualizer from '../components/visualization/components/LinkedListVisualizer';
import { LINKED_LIST_OPERATIONS, COMPARISON_TEMPLATES } from '../utils/linkedListTemplates';
import '../styles/arraysPage.css'; // Reuse core layouts
import '../styles/dsaModules.css';

const LINKED_LIST_QUIZ_QUESTIONS = [
  {
    type: 'MCQ',
    question: 'What is the time complexity of searching for an element in a Singly Linked List of size N in the worst case?',
    options: ['O(1)', 'O(log N)', 'O(N)', 'O(N log N)'],
    answer: 'O(N)',
    explanation: 'Unlike arrays, linked lists do not support direct indexing. To locate an element, you must traverse from the head node sequentially, yielding a worst-case time complexity of O(N).'
  },
  {
    type: 'MCQ',
    question: 'Which operation is O(1) in a Singly Linked List (given only the head pointer)?',
    options: [
      'Inserting a node at the beginning.',
      'Inserting a node at the end.',
      'Deleting the last node.',
      'Finding the middle node.'
    ],
    answer: 'Inserting a node at the beginning.',
    explanation: 'Inserting at the beginning only requires pointing the new node\'s next field to the head and updating head, which takes O(1) constant time.'
  },
  {
    type: 'PREDICTION',
    question: 'Given a Singly Linked List: 10 -> 20 -> 30. What does this code snippet result in?\nNode temp = head.next;\nhead.next = head.next.next;\ntemp.next = null;',
    options: [
      'The list becomes 10 -> 30, and a detached node 20 is created.',
      'The list becomes 10 -> 20 -> null.',
      'The list becomes 20 -> 30 -> null.',
      'A NullPointerException is thrown.'
    ],
    answer: 'The list becomes 10 -> 30, and a detached node 20 is created.',
    explanation: 'head.next originally points to 20. head.next.next points to 30. Setting head.next = head.next.next bypasses 20, linking 10 directly to 30. Setting temp.next = null detaches 20 completely.'
  },
  {
    type: 'MCQ',
    question: 'In Floyd\'s Cycle Detection (Tortoise and Hare), if the slow pointer moves 1 node per step, how many nodes does the fast pointer move?',
    options: ['1 node', '2 nodes', '3 nodes', 'It varies randomly'],
    answer: '2 nodes',
    explanation: 'The fast pointer (Hare) moves at twice the speed of the slow pointer (Tortoise). Thus, it advances 2 nodes per step.'
  },
  {
    type: 'DRYRUN',
    question: 'If slow and fast pointers enter a cycle of length C, what is the maximum number of steps slow will take inside the cycle before they meet?',
    options: [
      'C steps',
      '2 * C steps',
      'Log(C) steps',
      'Infinite steps, they may never meet'
    ],
    answer: 'C steps',
    explanation: 'Once the slow pointer enters the cycle, the fast pointer (which is already in the cycle) reduces the distance between them by 1 node per step. Thus, they will meet in at most C steps.'
  }
];

const LinkedListPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // Navigation State
  const [activeTab, setActiveTab] = useState(() => localStorage.getItem('linkedlist_selected_tab') || 'theory');

  useEffect(() => {
    localStorage.setItem('linkedlist_selected_tab', activeTab);
  }, [activeTab]);

  // Visualizer State
  const [selectedAlgoKey, setSelectedAlgoKey] = useState('singlyList');
  
  const currentTemplate = useMemo(() => {
    return LINKED_LIST_OPERATIONS[selectedAlgoKey];
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
  const [speedMs, setSpeedMs] = useState(() => parseInt(localStorage.getItem('linkedlist_speed') || '1200', 10));
  const [compileError, setCompileError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    localStorage.setItem('linkedlist_speed', speedMs.toString());
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
      algoA: { name: 'List Reversal', template: COMPARISON_TEMPLATES.reverse, time: 'O(N)', space: 'O(1)' },
      algoB: { name: 'Cycle Detection', template: COMPARISON_TEMPLATES.cycle, time: 'O(N)', space: 'O(1)' }
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
      
      const probList = probRes.data.data.filter(p => p.category === 'LINKED_LIST');
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
      const filtered = response.data.data.filter(q => q.quizTitle.toLowerCase().includes('linked'));
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
    if (quizIdx < LINKED_LIST_QUIZ_QUESTIONS.length - 1) {
      setQuizIdx(quizIdx + 1);
    } else {
      setQuizFinished(true);
      // Submit score
      const score = LINKED_LIST_QUIZ_QUESTIONS.filter((q, idx) => quizAnswers[idx] === q.answer).length;
      try {
        await api.post('/quiz/submit', {
          quizTitle: 'Linked Lists Core Quiz',
          score: score,
          totalQuestions: LINKED_LIST_QUIZ_QUESTIONS.length
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
          <span style={{ fontSize: '1.2rem', fontWeight: 'bold', color: 'var(--text-primary)' }}>🔗 Linked Lists Module</span>
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
              <h2>Dynamic Node Internals</h2>
              <p>A Linked List is a linear data structure where elements are not stored in contiguous memory locations.</p>
              <p>Instead, each element (called a <strong>Node</strong>) allocates dynamic Heap memory on creation and links to its neighbors via reference addresses (pointers).</p>
              <table className="complexity-table">
                <thead>
                  <tr>
                    <th>Operation</th>
                    <th>Linked List</th>
                    <th>Array (Static)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>Access / Search</td>
                    <td>O(N)</td>
                    <td>O(1) / O(N)</td>
                  </tr>
                  <tr>
                    <td>Insert / Delete (at head)</td>
                    <td>O(1)</td>
                    <td>O(N)</td>
                  </tr>
                  <tr>
                    <td>Insert / Delete (at end)</td>
                    <td>O(1) with tail / O(N)</td>
                    <td>O(1) / O(N)</td>
                  </tr>
                </tbody>
              </table>
            </section>
            <section className="theory-card">
              <h2>Types of Linked Lists</h2>
              <p><strong>Singly Linked List:</strong> Each node contains a single reference pointer pointing to the <code>next</code> node.</p>
              <p><strong>Doubly Linked List:</strong> Each node contains two pointers: <code>next</code> pointing forward, and <code>prev</code> pointing backward. This enables bidirectional traversal but increases memory footprint.</p>
              <p><strong>Circular Linked List:</strong> The last node\'s pointer points back to the head node instead of referencing <code>null</code>, creating a closed loop.</p>
            </section>
          </div>
        )}

        {activeTab === 'visualizer' && (
          <div className="visualizer-grid" style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '1.5rem' }}>
            <aside className="visualizer-sidebar" style={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '16px', padding: '1.25rem' }}>
              <h3 style={{ margin: '0 0 1rem 0', fontSize: '1rem', fontWeight: 600 }}>Algorithms</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {Object.keys(LINKED_LIST_OPERATIONS).map(key => (
                  <button
                    key={key}
                    className={`algo-select-btn ${selectedAlgoKey === key ? 'active' : ''}`}
                    onClick={() => { setSelectedAlgoKey(key); resetPlayback(); }}
                    style={{ textAlignment: 'left', background: selectedAlgoKey === key ? 'var(--accent-bg)' : 'transparent', border: `1px solid ${selectedAlgoKey === key ? 'var(--accent)' : 'var(--border)'}`, color: selectedAlgoKey === key ? '#fff' : 'var(--text-secondary)', padding: '0.6rem 1rem', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontWeight: selectedAlgoKey === key ? 600 : 400 }}
                  >
                    {LINKED_LIST_OPERATIONS[key].name}
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
                <LinkedListVisualizer stepInfo={currentStep} />
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
                {compIsLoading ? 'Comparing...' : '📊 Compare Reversal vs Cycle'}
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
                  <LinkedListVisualizer stepInfo={activeStepA} />
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
                  <LinkedListVisualizer stepInfo={activeStepB} />
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
                <h2 style={{ marginTop: '1rem', fontSize: '1.5rem' }}>Linked Lists Core Quiz</h2>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                  Test your understanding of linked list structures, node allocations, cycle detection, and complexity differences from arrays.
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
                  <span>Question {quizIdx + 1} of {LINKED_LIST_QUIZ_QUESTIONS.length}</span>
                  <span style={{ fontWeight: 600, color: 'var(--accent)' }}>Type: {LINKED_LIST_QUIZ_QUESTIONS[quizIdx].type}</span>
                </div>

                <h3 style={{ margin: '0 0 1.5rem 0', fontWeight: 600, fontSize: '1.2rem', whiteSpace: 'pre-wrap' }}>
                  {LINKED_LIST_QUIZ_QUESTIONS[quizIdx].question}
                </h3>

                <div className="quiz-options-list" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.5rem' }}>
                  {LINKED_LIST_QUIZ_QUESTIONS[quizIdx].options.map((opt, i) => {
                    const isSelected = quizAnswers[quizIdx] === opt;
                    const isCorrect = opt === LINKED_LIST_QUIZ_QUESTIONS[quizIdx].answer;
                    
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
                    💡 <strong>Explanation:</strong> {LINKED_LIST_QUIZ_QUESTIONS[quizIdx].explanation}
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
                      {quizIdx < LINKED_LIST_QUIZ_QUESTIONS.length - 1 ? 'Next Question' : 'Finish Quiz'}
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
                  You have completed the Linked Lists Core Quiz. Your final score is:
                </p>
                <div style={{ fontSize: '3rem', fontWeight: 800, color: 'var(--accent)', margin: '1.5rem 0' }}>
                  {LINKED_LIST_QUIZ_QUESTIONS.filter((q, idx) => quizAnswers[idx] === q.answer).length} / {LINKED_LIST_QUIZ_QUESTIONS.length}
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

export default LinkedListPage;
