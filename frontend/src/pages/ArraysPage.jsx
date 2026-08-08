import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import ArrayVisualizer from '../components/visualization/components/ArrayVisualizer';
import { ARRAY_OPERATIONS, ARRAY_PATTERNS, COMPARISON_TEMPLATES } from '../utils/arrayTemplates';
import '../styles/arraysPage.css';

// Initial quiz content
const ARRAY_QUIZ_QUESTIONS = [
  {
    type: 'MCQ',
    question: 'What is the index of the first element in a Java array?',
    options: ['1', '0', '-1', 'It depends on initialization'],
    answer: '0',
    explanation: 'In Java (and most modern programming languages), array indexing is zero-based, meaning the first element resides at index 0.'
  },
  {
    type: 'MCQ',
    question: 'Which addressing formula calculates the memory location of element A[i] given base address B and element size S?',
    options: [
      'Address = B + i * S',
      'Address = B - i * S',
      'Address = B + (i - 1) * S',
      'Address = B * i + S'
    ],
    answer: 'Address = B + i * S',
    explanation: 'The address of the ith element in contiguous memory is calculated as: BaseAddress + index * SizeOfElement.'
  },
  {
    type: 'PREDICTION',
    question: 'What does this code print?\nint[] arr = {1, 2, 3};\narr[1] = arr[2];\nSystem.out.print(arr[0] + "" + arr[1] + "" + arr[2]);',
    options: ['123', '133', '122', '333'],
    answer: '133',
    explanation: 'arr[2] has value 3. Setting arr[1] = arr[2] updates the array to {1, 3, 3}. Thus, it prints 133.'
  },
  {
    type: 'DRYRUN',
    question: 'In Binary Search on a sorted array of size 10, how many comparisons are made in the worst-case scenario?',
    options: ['10', '1', '4', '5'],
    answer: '4',
    explanation: 'For size N = 10, log2(10) is approximately 3.32. The worst-case search space divisions take at most ceil(log2(10)) = 4 comparisons.'
  },
  {
    type: 'MCQ',
    question: 'Which algorithm is Kadane\'s algorithm used to solve?',
    options: [
      'Finding the shortest path in a graph',
      'Maximum subarray sum problem',
      'Finding duplicates in an array',
      'Sorting elements in linear time'
    ],
    answer: 'Maximum subarray sum problem',
    explanation: 'Kadane\'s Algorithm computes the maximum contiguous subarray sum in O(N) time and O(1) space.'
  }
];

const ArraysPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // Navigation State
  const [activeTab, setActiveTab] = useState(() => localStorage.getItem('arrays_selected_tab') || 'theory');

  // Sync tab choices to localStorage
  useEffect(() => {
    localStorage.setItem('arrays_selected_tab', activeTab);
  }, [activeTab]);

  // Operations & Patterns Selector State
  const [selectedAlgoKey, setSelectedAlgoKey] = useState('traversal');
  const [selectedCategory, setSelectedCategory] = useState('operations'); // 'operations' or 'patterns'

  const currentTemplate = useMemo(() => {
    if (selectedCategory === 'operations') {
      return ARRAY_OPERATIONS[selectedAlgoKey];
    }
    return ARRAY_PATTERNS[selectedAlgoKey];
  }, [selectedCategory, selectedAlgoKey]);

  // Visualizer Monaco State
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
  const [speedMs, setSpeedMs] = useState(() => parseInt(localStorage.getItem('arrays_speed') || '1200', 10));
  const [compileError, setCompileError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // Sync animation speed preference
  useEffect(() => {
    localStorage.setItem('arrays_speed', speedMs.toString());
  }, [speedMs]);

  // Monaco highlight control refs
  const editorRef = useRef(null);
  const monacoRef = useRef(null);
  const decorationsRef = useRef([]);
  const playIntervalRef = useRef(null);

  const currentStep = trace[stepIdx] || null;

  // Cleanup interval on unmount
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

  // Sync speed changes while playing
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

  // Compile & Run execution
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
        // Fetch full trace list
        const traceRes = await api.get(`/execution/${data.sessionId}/trace`);
        const fullTrace = traceRes.data.data;
        setTrace(fullTrace);
        setStepIdx(0);
      }
    } catch (err) {
      console.error(err);
      setCompileError(err.response?.data?.message || 'Error occurred starting execution session.');
    } finally {
      setIsLoading(false);
    }
  };

  // Step Forward/Backward
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

  // Editor decorations mapping
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

  // Extract variables metrics dynamically
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

  // Memory extraction
  const heapObjects = useMemo(() => {
    if (!currentStep || !currentStep.variables) return [];
    const heap = [];
    const seen = new Set();
    currentStep.variables.forEach(v => {
      const isPrimitive = ['int', 'double', 'float', 'boolean', 'char', 'long', 'short', 'byte'].includes(v.type);
      if (!isPrimitive && v.memoryAddress && !seen.has(v.memoryAddress)) {
        seen.add(v.memoryAddress);
        heap.push({
          address: v.memoryAddress,
          type: v.type,
          value: v.value
        });
      }
    });
    return heap;
  }, [currentStep]);

  // Learning progress REST updates
  const [learningPercentage, setLearningPercentage] = useState(0);
  const handleCompleteTopic = async () => {
    try {
      // Find Algorithm ID in backend seed
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
  // COMPARISON TAB STATE & LOGIC
  // ==========================================
  const [selectedCompCategory, setSelectedCompCategory] = useState('search'); // 'search' or 'sort'
  const [compTraceA, setCompTraceA] = useState([]);
  const [compTraceB, setCompTraceB] = useState([]);
  const [compIdx, setCompIdx] = useState(0);
  const [compIsPlaying, setCompIsPlaying] = useState(false);
  const [compIsLoading, setCompIsLoading] = useState(false);
  
  const compPair = useMemo(() => {
    if (selectedCompCategory === 'search') {
      return {
        algoA: { name: 'Linear Search', template: COMPARISON_TEMPLATES.linearSearch, time: 'O(N)', space: 'O(1)' },
        algoB: { name: 'Binary Search', template: COMPARISON_TEMPLATES.binarySearch, time: 'O(log N)', space: 'O(1)' }
      };
    } else {
      return {
        algoA: { name: 'Bubble Sort', template: COMPARISON_TEMPLATES.bubbleSort, time: 'O(N^2)', space: 'O(1)' },
        algoB: { name: 'Selection Sort', template: COMPARISON_TEMPLATES.selectionSort, time: 'O(N^2)', space: 'O(1)' }
      };
    }
  }, [selectedCompCategory]);

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
      // Start Session A
      const resA = await api.post('/execution/start', {
        className: compPair.algoA.template.className,
        code: compPair.algoA.template.javaCode,
        input: ''
      });
      const dataA = resA.data.data;
      const traceResA = await api.get(`/execution/${dataA.sessionId}/trace`);
      
      // Start Session B
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
      
      const probList = probRes.data.data.filter(p => p.category === 'ARRAY');
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
      // Refresh progress metrics
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
    if (problemProgress.length === 0) return { solved: 0, attempts: 0, accuracy: 0.0 };
    const solvedCount = problemProgress.filter(p => p.solved).length;
    const totalAttempts = problemProgress.reduce((sum, p) => sum + p.attempts, 0);
    const avgAccuracy = problemProgress.reduce((sum, p) => sum + p.accuracy, 0) / problemProgress.length;

    return { solved: solvedCount, attempts: totalAttempts, accuracy: avgAccuracy.toFixed(1) };
  }, [problemProgress]);

  // ==========================================
  // QUIZ ARENA STATE & LOGIC
  // ==========================================
  const [quizStarted, setQuizStarted] = useState(false);
  const [quizFinished, setQuizFinished] = useState(false);
  const [quizIdx, setQuizIdx] = useState(0);
  const [quizAnswers, setQuizAnswers] = useState({});
  const [quizChecked, setQuizChecked] = useState(false);
  const [quizProgressList, setQuizProgressList] = useState([]);

  const fetchQuizProgress = async () => {
    try {
      const response = await api.get('/quiz/progress');
      setQuizProgressList(response.data.data);
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
    setQuizFinished(false);
    setQuizIdx(0);
    setQuizAnswers({});
    setQuizChecked(false);
  };

  const handleSelectOption = (opt) => {
    if (quizChecked) return;
    setQuizAnswers(prev => ({ ...prev, [quizIdx]: opt }));
  };

  const handleNextQuizQuestion = () => {
    setQuizChecked(false);
    if (quizIdx < ARRAY_QUIZ_QUESTIONS.length - 1) {
      setQuizIdx(quizIdx + 1);
    } else {
      // Calculate Score and submit
      submitQuizScore();
    }
  };

  const submitQuizScore = async () => {
    let score = 0;
    ARRAY_QUIZ_QUESTIONS.forEach((q, idx) => {
      if (quizAnswers[idx] === q.answer) score++;
    });

    try {
      await api.post('/quiz/submit', {
        quizTitle: 'Arrays Core Quiz',
        category: 'ARRAY',
        score: score,
        totalQuestions: ARRAY_QUIZ_QUESTIONS.length
      });
      setQuizFinished(true);
      fetchQuizProgress();
    } catch (e) {
      console.error(e);
      alert('Could not persist quiz score to database.');
      setQuizFinished(true);
    }
  };

  return (
    <div className="arrays-page">
      <header className="arrays-header">
        <button className="back-btn" onClick={() => navigate('/dashboard')}>
          &larr; Back to Dashboard
        </button>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <span style={{ fontSize: '1.5rem' }}>🚀</span>
          <h2 style={{ margin: 0, fontWeight: 700, fontSize: '1.25rem', letterSpacing: '-0.3px' }}>Arrays Module</h2>
        </div>
        <div style={{ width: '80px' }}></div>
      </header>

      <nav className="tabs-navigation">
        <button className={`tab-btn ${activeTab === 'theory' ? 'active' : ''}`} onClick={() => setActiveTab('theory')}>📚 Theory</button>
        <button className={`tab-btn ${activeTab === 'visualizer' ? 'active' : ''}`} onClick={() => setActiveTab('visualizer')}>🔬 Visualizer</button>
        <button className={`tab-btn ${activeTab === 'comparison' ? 'active' : ''}`} onClick={() => setActiveTab('comparison')}>📊 Comparisons</button>
        <button className={`tab-btn ${activeTab === 'practice' ? 'active' : ''}`} onClick={() => setActiveTab('practice')}>🧑‍💻 Practice Arena</button>
        <button className={`tab-btn ${activeTab === 'quiz' ? 'active' : ''}`} onClick={() => setActiveTab('quiz')}>🧠 Quiz Arena</button>
      </nav>

      <main className="arrays-main">
        {/* ==========================================
            TAB: THEORY & OVERVIEW
            ========================================== */}
        {activeTab === 'theory' && (
          <div className="theory-grid">
            <div className="theory-card">
              <h2>Introduction to Arrays</h2>
              <p>
                An <strong>Array</strong> is a fundamental linear data structure that stores a collection of elements of the same type in 
                contiguous memory locations. This contiguous structure allows elements to be accessed directly using their offset indices.
              </p>
              <p>
                In Java, arrays are objects that are dynamically created on the heap. They are static in size, meaning their length is 
                fixed upon instantiation and cannot change.
              </p>
              <h2 style={{ marginTop: '2rem' }}>Memory Addressing Formula</h2>
              <p>
                Because arrays are contiguous, the memory address of any element <code>A[i]</code> can be computed in constant time:
              </p>
              <div style={{ background: 'rgba(255,255,255,0.02)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border)', fontFamily: 'var(--mono)', textAlign: 'center', margin: '1rem 0' }}>
                Address(A[i]) = Base Address + i &times; Size of Element (Bytes)
              </div>
              <p>
                This lookup time is <code>O(1)</code>, which is the primary advantage of arrays.
              </p>
            </div>

            <div className="theory-card">
              <h2>Time & Space Complexity</h2>
              <table className="complexity-table">
                <thead>
                  <tr>
                    <th>Operation</th>
                    <th>Average Case</th>
                    <th>Worst Case</th>
                    <th>Space Complexity</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>Access by Index</td>
                    <td><code>O(1)</code></td>
                    <td><code>O(1)</code></td>
                    <td><code>O(1)</code></td>
                  </tr>
                  <tr>
                    <td>Search (Unsorted)</td>
                    <td><code>O(N)</code></td>
                    <td><code>O(N)</code></td>
                    <td><code>O(1)</code></td>
                  </tr>
                  <tr>
                    <td>Search (Sorted / Binary)</td>
                    <td><code>O(log N)</code></td>
                    <td><code>O(log N)</code></td>
                    <td><code>O(1)</code></td>
                  </tr>
                  <tr>
                    <td>Insertion</td>
                    <td><code>O(N)</code></td>
                    <td><code>O(N)</code></td>
                    <td><code>O(1)</code></td>
                  </tr>
                  <tr>
                    <td>Deletion</td>
                    <td><code>O(N)</code></td>
                    <td><code>O(N)</code></td>
                    <td><code>O(1)</code></td>
                  </tr>
                </tbody>
              </table>

              <h2 style={{ marginTop: '2rem' }}>Core Interview Tips</h2>
              <ul style={{ color: 'var(--text-secondary)', lineHeight: '1.7', paddingLeft: '1.25rem' }}>
                <li>Use the <strong>Two Pointer</strong> technique (e.g. left and right starting from ends) to solve searching or swapping problems on sorted arrays in linear time.</li>
                <li>Leverage the <strong>Sliding Window</strong> pattern for subarray problems involving sums, lengths, or character matches.</li>
                <li>Utilize <strong>Prefix Sum</strong> buffers to resolve static subarray range queries in constant <code>O(1)</code> time.</li>
                <li>Always handle array boundaries carefully. Keep a strict check on loop bounds to prevent <code>ArrayIndexOutOfBoundsException</code>.</li>
              </ul>
            </div>
          </div>
        )}

        {/* ==========================================
            TAB: INTERACTIVE VISUALIZER
            ========================================== */}
        {activeTab === 'visualizer' && (
          <div className="visualizer-container">
            <aside className="algo-sidebar">
              <div className="sidebar-section-title">Operations</div>
              <div className="algo-list">
                {Object.keys(ARRAY_OPERATIONS).map(key => (
                  <button
                    key={key}
                    className={`algo-item ${selectedAlgoKey === key && selectedCategory === 'operations' ? 'active' : ''}`}
                    onClick={() => {
                      setSelectedAlgoKey(key);
                      setSelectedCategory('operations');
                    }}
                  >
                    {ARRAY_OPERATIONS[key].name}
                  </button>
                ))}
              </div>

              <div className="sidebar-section-title">Patterns</div>
              <div className="algo-list">
                {Object.keys(ARRAY_PATTERNS).map(key => (
                  <button
                    key={key}
                    className={`algo-item ${selectedAlgoKey === key && selectedCategory === 'patterns' ? 'active' : ''}`}
                    onClick={() => {
                      setSelectedAlgoKey(key);
                      setSelectedCategory('patterns');
                    }}
                  >
                    {ARRAY_PATTERNS[key].name}
                  </button>
                ))}
              </div>
            </aside>

            <section className="visualizer-workspace">
              <div className="visualizer-control-card">
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                  <button onClick={handleCompileAndRun} disabled={isLoading} className="ctrl-btn active" style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.5rem 1.25rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                    {isLoading ? 'Compiling...' : '⚡ Compile & Run'}
                  </button>
                  <button onClick={handleRestart} disabled={trace.length === 0} className="ctrl-btn" style={{ padding: '0.5rem', minWidth: '40px' }} title="Restart execution">🔄</button>
                  <button onClick={handleStepBackward} disabled={trace.length === 0 || stepIdx === 0} className="ctrl-btn" style={{ padding: '0.5rem', minWidth: '40px' }}>&larr; Back</button>
                  <button onClick={togglePlay} disabled={trace.length === 0} className="ctrl-btn" style={{ padding: '0.5rem 1rem', fontWeight: 600 }}>
                    {isPlaying ? '⏸️ Pause' : '▶️ Resume'}
                  </button>
                  <button onClick={handleStepForward} disabled={trace.length === 0 || stepIdx >= trace.length - 1} className="ctrl-btn" style={{ padding: '0.5rem', minWidth: '40px' }}>Next &rarr;</button>
                </div>

                <div className="timeline-slider-container">
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', minWidth: '50px' }}>Step {stepIdx + 1}/{trace.length || 1}</span>
                  <input
                    type="range"
                    min="0"
                    max={trace.length > 0 ? trace.length - 1 : 0}
                    value={stepIdx}
                    onChange={(e) => {
                      stopPlayTimer();
                      setStepIdx(parseInt(e.target.value, 10));
                    }}
                    className="timeline-input"
                    disabled={trace.length === 0}
                  />
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Speed</span>
                  <input
                    type="range"
                    min="200"
                    max="2000"
                    step="200"
                    value={speedMs}
                    onChange={(e) => setSpeedMs(parseInt(e.target.value, 10))}
                    style={{ width: '80px', accentColor: 'var(--accent)' }}
                  />
                </div>
              </div>

              <div className="workspace-grid">
                {/* Editor Column */}
                <div className="workspace-card">
                  <div className="header">
                    <span>Editor (Monaco)</span>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{currentTemplate.className}.java</span>
                  </div>
                  <div className="monaco-wrapper" style={{ height: '360px', overflow: 'hidden', borderRadius: '8px', border: '1px solid var(--border)' }}>
                    <Editor
                      height="100%"
                      language="java"
                      theme="vs-dark"
                      value={editorCode}
                      onChange={(val) => setEditorCode(val)}
                      onMount={handleEditorDidMount}
                      options={{
                        minimap: { enabled: false },
                        fontSize: 13,
                        lineNumbers: 'on',
                        scrollBeyondLastLine: false,
                        automaticLayout: true
                      }}
                    />
                  </div>
                  {compileError && (
                    <div style={{ background: 'rgba(255, 92, 108, 0.1)', border: '1px solid var(--error)', color: 'var(--error)', borderRadius: '8px', padding: '0.75rem', marginTop: '1rem', fontSize: '0.8rem', fontFamily: 'var(--mono)', whiteSpace: 'pre-wrap' }}>
                      {compileError}
                    </div>
                  )}
                </div>

                {/* Visualizations Column */}
                <div className="workspace-card" style={{ gap: '1.25rem' }}>
                  <div className="header">
                    <span>Visualization Panel</span>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <span className="complexity-badge time" style={{ background: 'rgba(62,198,224,0.1)', color: '#3ec6e0', padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem' }}>Time: {currentTemplate.timeComplexity}</span>
                      <span className="complexity-badge space" style={{ background: 'rgba(255,179,64,0.1)', color: '#ffb340', padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem' }}>Space: {currentTemplate.spaceComplexity}</span>
                    </div>
                  </div>

                  <div style={{ minHeight: '180px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.1)', borderRadius: '8px', padding: '1rem' }}>
                    <ArrayVisualizer stepInfo={currentStep} />
                  </div>

                  {/* Complexity Panel */}
                  <div>
                    <h5 style={{ margin: '0 0 0.5rem 0', color: 'var(--text-secondary)', fontSize: '0.8rem' }}>Live Complexity Metrics</h5>
                    <div className="complexity-grid">
                      <div className="metric-card">
                        <div className="label">Iterations</div>
                        <div className="value">{metrics.iterations}</div>
                      </div>
                      <div className="metric-card">
                        <div className="label">Comparisons</div>
                        <div className="value">{metrics.comparisons}</div>
                      </div>
                      <div className="metric-card">
                        <div className="label">Swaps/Shifts</div>
                        <div className="value">{metrics.swaps}</div>
                      </div>
                      <div className="metric-card">
                        <div className="label">Active Index</div>
                        <div className="value">{metrics.index !== -1 ? metrics.index : 'N/A'}</div>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Explanation & Memory Row */}
                <div className="workspace-card" style={{ gridColumn: 'span 2' }}>
                  <div className="header">Memory & Trace Inspector</div>
                  
                  {currentStep && currentStep.explanation && (
                    <div className="explanation-banner" style={{ marginBottom: '1.25rem' }}>
                      💡 <strong>Explain:</strong> {currentStep.explanation}
                    </div>
                  )}

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
                    {/* Call Stack */}
                    <div style={{ background: 'rgba(255,255,255,0.01)', border: '1px solid var(--border)', borderRadius: '10px', padding: '1rem' }}>
                      <h5 style={{ margin: '0 0 0.75rem 0', color: 'var(--text-primary)', fontSize: '0.85rem' }}>Stack Memory (Call Stack)</h5>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxHeight: '150px', overflowY: 'auto' }}>
                        {currentStep && currentStep.callStack && currentStep.callStack.length > 0 ? (
                          currentStep.callStack.map((frame, idx) => (
                            <div key={idx} style={{ padding: '0.5rem', borderLeft: '3px solid var(--accent)', background: 'rgba(255,255,255,0.02)', borderRadius: '4px', fontSize: '0.8rem' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 600 }}>
                                <span>{frame.methodName}()</span>
                                <span style={{ color: 'var(--text-secondary)', fontSize: '0.75rem' }}>line {frame.lineNumber}</span>
                              </div>
                              {frame.localVariables && frame.localVariables.length > 0 && (
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.25rem' }}>
                                  {frame.localVariables.map((v, vIdx) => (
                                    <span key={vIdx} style={{ fontSize: '0.75rem', background: 'rgba(255,255,255,0.03)', padding: '1px 4px', borderRadius: '2px' }}>
                                      {v.name}: <strong style={{ color: '#ffb340' }}>{v.value}</strong>
                                    </span>
                                  ))}
                                </div>
                              )}
                            </div>
                          ))
                        ) : (
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontStyle: 'italic' }}>No active stack frames.</div>
                        )}
                      </div>
                    </div>

                    {/* Heap */}
                    <div style={{ background: 'rgba(255,255,255,0.01)', border: '1px solid var(--border)', borderRadius: '10px', padding: '1rem' }}>
                      <h5 style={{ margin: '0 0 0.75rem 0', color: 'var(--text-primary)', fontSize: '0.85rem' }}>Heap Memory (Objects)</h5>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxHeight: '150px', overflowY: 'auto' }}>
                        {heapObjects.length > 0 ? (
                          heapObjects.map((obj, idx) => (
                            <div key={idx} style={{ padding: '0.5rem', background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.05)', borderRadius: '4px', fontSize: '0.8rem' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', fontFamily: 'var(--mono)', fontSize: '0.75rem', color: '#3ec6e0' }}>
                                <span>{obj.address}</span>
                                <span>{obj.type}</span>
                              </div>
                              <div style={{ marginTop: '0.25rem', wordBreak: 'break-all', fontWeight: 600 }}>Value: {obj.value}</div>
                            </div>
                          ))
                        ) : (
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontStyle: 'italic' }}>No heap objects allocated.</div>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Dry Run Table */}
                  <div style={{ marginBottom: '1rem' }}>
                    <h5 style={{ margin: '0 0 0.5rem 0', color: 'var(--text-primary)', fontSize: '0.85rem' }}>Dry Run Table Logs</h5>
                    <div className="dry-run-table-wrapper">
                      <table className="dry-run-table">
                        <thead>
                          <tr>
                            <th style={{ width: '50px' }}>Step</th>
                            <th style={{ width: '70px' }}>Line No</th>
                            <th>Code Statement</th>
                            <th>Local Variables</th>
                            <th>Operation</th>
                            <th>Output</th>
                          </tr>
                        </thead>
                        <tbody>
                          {trace.length > 0 && trace.slice(0, stepIdx + 1).map((tr, idx) => {
                            const lines = editorCode.split('\n');
                            const codeStatement = lines[tr.lineNumber - 1] || '';
                            const varStr = tr.variables && tr.variables.length > 0 ?
                              tr.variables.map(v => `${v.name}=${v.value}`).join(', ') : 'none';
                            const isActiveRow = idx === stepIdx;

                            return (
                              <tr key={idx} className={isActiveRow ? 'active-row' : ''}>
                                <td style={{ fontFamily: 'var(--mono)' }}>{tr.stepNumber}</td>
                                <td style={{ fontFamily: 'var(--mono)', color: 'var(--text-secondary)' }}>{tr.lineNumber}</td>
                                <td style={{ fontFamily: 'var(--mono)', whiteSpace: 'pre', color: 'var(--text-primary)' }}>{codeStatement.trim()}</td>
                                <td style={{ color: '#ffb340', fontFamily: 'var(--mono)' }}>{varStr}</td>
                                <td>
                                  {tr.metadata ? (
                                    <span style={{ fontSize: '0.7rem', fontWeight: 700, padding: '1px 4px', borderRadius: '3px', background: tr.metadata.operation === 'COMPARE' ? 'rgba(255,179,64,0.1)' : tr.metadata.operation === 'SWAP' ? 'rgba(255,92,108,0.1)' : 'rgba(255,255,255,0.05)', color: tr.metadata.operation === 'COMPARE' ? '#ffb340' : tr.metadata.operation === 'SWAP' ? 'var(--error)' : 'var(--text-secondary)' }}>
                                      {tr.metadata.operation}
                                    </span>
                                  ) : 'READ'}
                                </td>
                                <td style={{ fontFamily: 'var(--mono)', color: 'var(--success)' }}>{tr.output ? tr.output.replace(/\n/g, ' \\n ') : ''}</td>
                              </tr>
                            );
                          })}
                          {trace.length === 0 && (
                            <tr>
                              <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-secondary)', fontStyle: 'italic' }}>No steps executed. Click Run.</td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
                    <button onClick={handleCompleteTopic} className="ctrl-btn" style={{ borderColor: 'var(--success)', color: 'var(--success)', padding: '0.5rem 1rem', borderRadius: '8px', cursor: 'pointer', background: 'transparent', border: '1px solid var(--success)', fontWeight: 600 }}>
                      ✔ Mark Concept as Solved
                    </button>
                  </div>
                </div>

                {/* Additional Study Info Tab */}
                <div className="workspace-card" style={{ gridColumn: 'span 2' }}>
                  <div className="header">Learning Resources: {currentTemplate.name}</div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '2rem' }}>
                    <div>
                      <h4 style={{ color: 'var(--text-primary)', margin: '0 0 0.5rem 0' }}>Explanation</h4>
                      <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6', fontSize: '0.9rem' }}>{currentTemplate.explanation}</p>
                      
                      <h4 style={{ color: 'var(--text-primary)', margin: '1.5rem 0 0.5rem 0' }}>Common Pitfalls / Mistakes</h4>
                      <ul style={{ color: 'var(--text-secondary)', lineHeight: '1.6', fontSize: '0.9rem', paddingLeft: '1.25rem' }}>
                        {currentTemplate.commonMistakes.map((m, idx) => (
                          <li key={idx} style={{ marginBottom: '0.5rem' }}>{m}</li>
                        ))}
                      </ul>

                      <h4 style={{ color: 'var(--text-primary)', margin: '1.5rem 0 0.5rem 0' }}>Recommended Interview Questions</h4>
                      <ul style={{ color: 'var(--text-secondary)', lineHeight: '1.6', fontSize: '0.9rem', paddingLeft: '1.25rem' }}>
                        {currentTemplate.interviewQuestions.map((q, idx) => (
                          <li key={idx} style={{ marginBottom: '0.5rem' }}>{q}</li>
                        ))}
                      </ul>
                    </div>

                    <div>
                      <h4 style={{ color: 'var(--text-primary)', margin: '0 0 0.5rem 0' }}>Optimized Implementation</h4>
                      <div style={{ border: '1px solid var(--border)', borderRadius: '8px', height: '300px', overflow: 'hidden' }}>
                        <Editor
                          height="100%"
                          language="java"
                          theme="vs-dark"
                          value={currentTemplate.optimizedVersion}
                          options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            fontSize: 12,
                            lineNumbers: 'on',
                            scrollBeyondLastLine: false,
                            automaticLayout: true
                          }}
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </div>
        )}

        {/* ==========================================
            TAB: ALGORITHM COMPARISONS
            ========================================== */}
        {activeTab === 'comparison' && (
          <div>
            <div className="visualizer-control-card">
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                <select
                  value={selectedCompCategory}
                  onChange={(e) => setSelectedCompCategory(e.target.value)}
                  style={{ background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border)', color: '#fff', padding: '0.4rem 0.8rem', borderRadius: '8px', outline: 'none' }}
                >
                  <option value="search" style={{ background: '#131623' }}>Linear Search vs Binary Search</option>
                  <option value="sort" style={{ background: '#131623' }}>Bubble Sort vs Selection Sort</option>
                </select>

                <button onClick={handleStartComparison} disabled={compIsLoading} className="ctrl-btn active" style={{ background: 'var(--accent)', color: '#fff', border: 'none', padding: '0.4rem 1.25rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}>
                  {compIsLoading ? 'Initializing...' : '📊 Compare Algorithms'}
                </button>
                
                {compTraceA.length > 0 && (
                  <>
                    <button onClick={toggleCompPlay} className="ctrl-btn" style={{ padding: '0.4rem 1rem', fontWeight: 600 }}>
                      {compIsPlaying ? '⏸️ Pause' : '▶️ Resume'}
                    </button>
                    <button onClick={stepCompBackward} disabled={compIdx === 0} className="ctrl-btn" style={{ padding: '0.4rem', minWidth: '40px' }}>&larr;</button>
                    <button onClick={stepCompForward} disabled={compIdx >= Math.max(compTraceA.length, compTraceB.length) - 1} className="ctrl-btn" style={{ padding: '0.4rem', minWidth: '40px' }}>&rarr;</button>
                  </>
                )}
              </div>

              {compTraceA.length > 0 && (
                <div className="timeline-slider-container" style={{ flex: 1 }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Timeline Index: {compIdx}</span>
                  <input
                    type="range"
                    min="0"
                    max={Math.max(compTraceA.length, compTraceB.length) - 1}
                    value={compIdx}
                    onChange={(e) => {
                      stopCompPlay();
                      setCompIdx(parseInt(e.target.value, 10));
                    }}
                    className="timeline-input"
                  />
                </div>
              )}
            </div>

            <div className="comparison-grid">
              {/* Algorithm A Column */}
              <div className="comparison-column">
                <h3 style={{ margin: 0, fontWeight: 700, color: 'var(--accent)', fontSize: '1.15rem', display: 'flex', justifyContent: 'space-between' }}>
                  <span>{compPair.algoA.name}</span>
                  <span style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.2)' }}>Time: {compPair.algoA.time} | Space: {compPair.algoA.space}</span>
                </h3>
                <div style={{ height: '140px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.1)', borderRadius: '10px', padding: '1rem' }}>
                  <ArrayVisualizer stepInfo={activeStepA} />
                </div>
                
                {/* Code snippets */}
                <div style={{ height: '220px', border: '1px solid var(--border)', borderRadius: '8px', overflow: 'hidden' }}>
                  <Editor
                    height="100%"
                    language="java"
                    theme="vs-dark"
                    value={compPair.algoA.template.javaCode}
                    options={{
                      readOnly: true,
                      minimap: { enabled: false },
                      fontSize: 11,
                      lineNumbers: 'on',
                      scrollBeyondLastLine: false,
                      automaticLayout: true
                    }}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.5rem' }}>
                  <div className="metric-card">
                    <div className="label">Iterations</div>
                    <div className="value">{metricsA.iterations}</div>
                  </div>
                  <div className="metric-card">
                    <div className="label">Comparisons</div>
                    <div className="value">{metricsA.comparisons}</div>
                  </div>
                  <div className="metric-card">
                    <div className="label">Swaps</div>
                    <div className="value">{metricsA.swaps}</div>
                  </div>
                </div>
              </div>

              {/* Algorithm B Column */}
              <div className="comparison-column">
                <h3 style={{ margin: 0, fontWeight: 700, color: '#3ec6e0', fontSize: '1.15rem', display: 'flex', justifyContent: 'space-between' }}>
                  <span>{compPair.algoB.name}</span>
                  <span style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.2)' }}>Time: {compPair.algoB.time} | Space: {compPair.algoB.space}</span>
                </h3>
                <div style={{ height: '140px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.1)', borderRadius: '10px', padding: '1rem' }}>
                  <ArrayVisualizer stepInfo={activeStepB} />
                </div>

                <div style={{ height: '220px', border: '1px solid var(--border)', borderRadius: '8px', overflow: 'hidden' }}>
                  <Editor
                    height="100%"
                    language="java"
                    theme="vs-dark"
                    value={compPair.algoB.template.javaCode}
                    options={{
                      readOnly: true,
                      minimap: { enabled: false },
                      fontSize: 11,
                      lineNumbers: 'on',
                      scrollBeyondLastLine: false,
                      automaticLayout: true
                    }}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.5rem' }}>
                  <div className="metric-card">
                    <div className="label">Iterations</div>
                    <div className="value">{metricsB.iterations}</div>
                  </div>
                  <div className="metric-card">
                    <div className="label">Comparisons</div>
                    <div className="value">{metricsB.comparisons}</div>
                  </div>
                  <div className="metric-card">
                    <div className="label">Swaps</div>
                    <div className="value">{metricsB.swaps}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ==========================================
            TAB: PRACTICE ARENA
            ========================================== */}
        {activeTab === 'practice' && (
          <div>
            <div className="practice-stats">
              <div className="stat-box">
                <div className="title">Solved Problems</div>
                <div className="number">{practiceStats.solved} / {problems.length}</div>
              </div>
              <div className="stat-box">
                <div className="title">Total Submissions</div>
                <div className="number">{practiceStats.attempts}</div>
              </div>
              <div className="stat-box">
                <div className="title">Average Accuracy</div>
                <div className="number">{practiceStats.accuracy}%</div>
              </div>
            </div>

            <div className="practice-grid">
              <aside className="problems-sidebar">
                <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1rem', fontWeight: 600 }}>Problems</h3>
                {problems.map((prob) => {
                  const progress = problemProgress.find(p => p.problemId === prob.id);
                  return (
                    <button
                      key={prob.id}
                      className={`problem-item-btn ${selectedProblem?.id === prob.id ? 'active' : ''}`}
                      onClick={() => handleSelectProblem(prob)}
                    >
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                        <span style={{ fontSize: '0.9rem', fontWeight: 600 }}>{prob.title}</span>
                        <span className={`difficulty-badge ${prob.difficulty.toLowerCase()}`} style={{ alignSelf: 'flex-start' }}>{prob.difficulty}</span>
                      </div>
                      {progress?.solved && <span style={{ color: 'var(--success)', fontWeight: 'bold' }}>✔</span>}
                    </button>
                  );
                })}
              </aside>

              {selectedProblem && (
                <section className="visualizer-workspace">
                  <div className="workspace-card">
                    <div className="header">
                      <span>Problem Details: {selectedProblem.title}</span>
                      <span className={`difficulty-badge ${selectedProblem.difficulty.toLowerCase()}`}>{selectedProblem.difficulty}</span>
                    </div>
                    <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6', fontSize: '0.9rem', whiteSpace: 'pre-wrap' }}>
                      {selectedProblem.description}
                    </p>
                    <div style={{ marginTop: '0.5rem', background: 'rgba(255,255,255,0.02)', padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border)' }}>
                      <strong>Expected Output:</strong> <code>{selectedProblem.expectedOutput}</code>
                    </div>
                  </div>

                  <div className="workspace-card">
                    <div className="header">
                      <span>Java Editor Workspace</span>
                      <button
                        onClick={handlePracticeSubmit}
                        disabled={isPracticeSubmitting}
                        className="ctrl-btn active"
                        style={{ background: 'var(--success)', color: '#fff', border: 'none', padding: '0.4rem 1.25rem', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}
                      >
                        {isPracticeSubmitting ? 'Evaluating...' : '🚀 Submit Solution'}
                      </button>
                    </div>

                    <div style={{ height: '360px', border: '1px solid var(--border)', borderRadius: '8px', overflow: 'hidden' }}>
                      <Editor
                        height="100%"
                        language="java"
                        theme="vs-dark"
                        value={practiceCode}
                        onChange={(val) => setPracticeCode(val)}
                        options={{
                          minimap: { enabled: false },
                          fontSize: 13,
                          lineNumbers: 'on',
                          scrollBeyondLastLine: false,
                          automaticLayout: true
                        }}
                      />
                    </div>

                    {practiceFeedback && (
                      <div style={{ marginTop: '1.25rem', padding: '1rem', borderRadius: '8px', background: practiceFeedback.success ? 'rgba(46, 202, 127, 0.1)' : 'rgba(255, 92, 108, 0.1)', border: `1px solid ${practiceFeedback.success ? 'var(--success)' : 'var(--error)'}` }}>
                        <h4 style={{ margin: '0 0 0.5rem 0', color: practiceFeedback.success ? 'var(--success)' : 'var(--error)' }}>
                          {practiceFeedback.success ? '🎉 Challenge Passed!' : '❌ Evaluation Failed'}
                        </h4>
                        <p style={{ margin: 0, fontSize: '0.85rem' }}>{practiceFeedback.feedback}</p>
                        
                        {practiceFeedback.compileError && (
                          <pre style={{ marginTop: '0.75rem', padding: '0.5rem', background: '#000', borderRadius: '4px', color: 'var(--error)', fontFamily: 'var(--mono)', fontSize: '0.8rem', overflowX: 'auto', whiteSpace: 'pre-wrap' }}>
                            {practiceFeedback.compileError}
                          </pre>
                        )}
                        {practiceFeedback.output && (
                          <div style={{ marginTop: '0.75rem' }}>
                            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Console Output:</span>
                            <pre style={{ margin: '0.25rem 0 0 0', padding: '0.5rem', background: '#000', borderRadius: '4px', color: '#fff', fontFamily: 'var(--mono)', fontSize: '0.8rem' }}>
                              {practiceFeedback.output}
                            </pre>
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

        {/* ==========================================
            TAB: QUIZ ARENA
            ========================================== */}
        {activeTab === 'quiz' && (
          <div>
            {!quizStarted && (
              <div className="theory-card quiz-welcome-card">
                <span style={{ fontSize: '3rem' }}>🧠</span>
                <h2 style={{ marginTop: '1rem' }}>Arrays Core Quiz</h2>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                  Test your understanding of array memory allocation, addressing indices, search algorithms, and the sliding window pattern.
                </p>

                {quizProgressList.length > 0 && (
                  <div style={{ marginBottom: '2rem', padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '8px', border: '1px solid var(--border)' }}>
                    <h4 style={{ margin: '0 0 0.5rem 0' }}>Your Previous Attempts</h4>
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
              <div className="quiz-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1rem' }}>
                  <span>Question {quizIdx + 1} of {ARRAY_QUIZ_QUESTIONS.length}</span>
                  <span style={{ fontWeight: 600, color: 'var(--accent)' }}>Type: {ARRAY_QUIZ_QUESTIONS[quizIdx].type}</span>
                </div>

                <h3 style={{ margin: '0 0 1.5rem 0', fontWeight: 600, fontSize: '1.2rem', whiteSpace: 'pre-wrap' }}>
                  {ARRAY_QUIZ_QUESTIONS[quizIdx].question}
                </h3>

                <div className="quiz-options-list">
                  {ARRAY_QUIZ_QUESTIONS[quizIdx].options.map((opt, i) => {
                    const isSelected = quizAnswers[quizIdx] === opt;
                    const isCorrect = opt === ARRAY_QUIZ_QUESTIONS[quizIdx].answer;
                    
                    let optionClass = 'quiz-option-btn';
                    if (quizChecked) {
                      if (isCorrect) optionClass += ' correct';
                      else if (isSelected) optionClass += ' wrong';
                    } else if (isSelected) {
                      optionClass += ' selected';
                    }

                    return (
                      <button
                        key={i}
                        className={optionClass}
                        onClick={() => handleSelectOption(opt)}
                      >
                        {opt}
                      </button>
                    );
                  })}
                </div>

                {quizChecked && (
                  <div style={{ background: 'rgba(108, 99, 255, 0.08)', borderLeft: '4px solid var(--accent)', borderRadius: '4px', padding: '1rem', marginBottom: '1.5rem', fontSize: '0.9rem', color: 'var(--text-primary)' }}>
                    💡 <strong>Explanation:</strong> {ARRAY_QUIZ_QUESTIONS[quizIdx].explanation}
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
                      {quizIdx < ARRAY_QUIZ_QUESTIONS.length - 1 ? 'Next Question' : 'Finish Quiz'}
                    </button>
                  )}
                </div>
              </div>
            )}

            {quizStarted && quizFinished && (
              <div className="theory-card quiz-welcome-card">
                <span style={{ fontSize: '3rem' }}>🎉</span>
                <h2 style={{ marginTop: '1rem' }}>Quiz Completed!</h2>
                <p style={{ color: 'var(--text-secondary)' }}>
                  You have completed the Arrays Core Quiz. Your final score is:
                </p>
                <div style={{ fontSize: '3rem', fontWeight: 800, color: 'var(--accent)', margin: '1.5rem 0' }}>
                  {ARRAY_QUIZ_QUESTIONS.filter((q, idx) => quizAnswers[idx] === q.answer).length} / {ARRAY_QUIZ_QUESTIONS.length}
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

export default ArraysPage;
