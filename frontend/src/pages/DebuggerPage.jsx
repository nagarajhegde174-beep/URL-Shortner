import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import api from '../services/api';
import '../styles/debugger.css';

const CODE_TEMPLATES = {
  helloWorld: {
    name: 'Hello World',
    className: 'HelloWorld',
    code: `public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, DSA Studio!");
        System.out.println("Welcome to Phase 2 Execution Engine.");
    }
}`
  },
  loops: {
    name: 'Loops & Counters',
    className: 'LoopDemo',
    code: `public class LoopDemo {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 1; i <= 5; i++) {
            sum += i;
            System.out.println("i = " + i + ", sum = " + sum);
        }
    }
}`
  },
  arrays: {
    name: 'Array Operations',
    className: 'ArrayDemo',
    code: `public class ArrayDemo {
    public static void main(String[] args) {
        int[] numbers = new int[3];
        numbers[0] = 10;
        numbers[1] = 20;
        numbers[2] = 30;
        
        int total = 0;
        for (int x : numbers) {
            total += x;
        }
        System.out.println("Total: " + total);
    }
}`
  },
  objects: {
    name: 'Object Instantiation (Heap)',
    className: 'ObjectDemo',
    code: `public class ObjectDemo {
    static class Node {
        int value;
        Node next;
        Node(int val) {
            this.value = val;
        }
    }

    public static void main(String[] args) {
        Node first = new Node(100);
        Node second = new Node(200);
        first.next = second;
        System.out.println("Nodes linked successfully.");
    }
}`
  },
  recursion: {
    name: 'Recursive Factorial (Stack)',
    className: 'RecursionDemo',
    code: `public class RecursionDemo {
    public static void main(String[] args) {
        int result = factorial(4);
        System.out.println("Factorial of 4 is: " + result);
    }

    public static int factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }
}`
  },
  exceptions: {
    name: 'Exceptions Demo',
    className: 'ExceptionDemo',
    code: `public class ExceptionDemo {
    public static void main(String[] args) {
        try {
            String str = null;
            int length = str.length(); // Throws NPE
        } catch (NullPointerException e) {
            System.out.println("Caught exception: " + e.toString());
        }
    }
}`
  }
};

const DebuggerPage = () => {
  const navigate = useNavigate();
  const [selectedTemplate, setSelectedTemplate] = useState('loops');
  const [code, setCode] = useState(CODE_TEMPLATES.loops.code);
  const [className, setClassName] = useState(CODE_TEMPLATES.loops.className);
  const [inputData, setInputData] = useState('');
  
  const [isLoading, setIsLoading] = useState(false);
  const [compileError, setCompileError] = useState(null);
  const [stepInfo, setStepInfo] = useState(null);
  
  // Playback Auto-Run controls
  const [isPlaying, setIsPlaying] = useState(false);
  const playIntervalRef = useRef(null);

  // Monaco Refs
  const editorRef = useRef(null);
  const monacoRef = useRef(null);
  const decorationsRef = useRef([]);

  // Handle template change
  const handleTemplateChange = (e) => {
    const key = e.target.value;
    setSelectedTemplate(key);
    setCode(CODE_TEMPLATES[key].code);
    setClassName(CODE_TEMPLATES[key].className);
    resetDebuggerState();
  };

  const resetDebuggerState = () => {
    setStepInfo(null);
    setCompileError(null);
    stopPlayback();
  };

  const handleEditorDidMount = (editor, monaco) => {
    editorRef.current = editor;
    monacoRef.current = monaco;
  };

  // Highlight executing line in Monaco
  useEffect(() => {
    if (editorRef.current && monacoRef.current && stepInfo && stepInfo.lineNumber) {
      const monaco = monacoRef.current;
      const line = stepInfo.lineNumber;

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
  }, [stepInfo]);

  // Clean up auto-play interval
  useEffect(() => {
    return () => stopPlayback();
  }, []);

  const stopPlayback = () => {
    setIsPlaying(false);
    if (playIntervalRef.current) {
      clearInterval(playIntervalRef.current);
      playIntervalRef.current = null;
    }
  };

  // API Calls
  const handleCompileAndRun = async () => {
    setIsLoading(true);
    resetDebuggerState();
    try {
      const response = await api.post('/execution/run', {
        className,
        code,
        input: inputData
      });
      const data = response.data.data;
      if (data.exceptionName === 'CompilationException') {
        setCompileError(data.exceptionMessage);
      } else {
        setStepInfo(data);
      }
    } catch (err) {
      console.error(err);
      setCompileError(err.response?.data?.message || 'Error occurred during compile & execution.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStepForward = async () => {
    if (!stepInfo) return;
    try {
      const response = await api.post('/execution/step?direction=next');
      const data = response.data.data;
      if (data) {
        setStepInfo(data);
      } else {
        stopPlayback();
      }
    } catch (err) {
      console.error(err);
      stopPlayback();
    }
  };

  const handleStepBackward = async () => {
    if (!stepInfo) return;
    try {
      const response = await api.post('/execution/step?direction=prev');
      const data = response.data.data;
      if (data) {
        setStepInfo(data);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleRestart = async () => {
    if (!stepInfo) return;
    try {
      const response = await api.post('/execution/reset');
      const data = response.data.data;
      if (data) {
        setStepInfo(data);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const togglePlayback = () => {
    if (isPlaying) {
      stopPlayback();
    } else {
      setIsPlaying(true);
      playIntervalRef.current = setInterval(() => {
        handleStepForward();
      }, 1200);
    }
  };

  // Parse heap objects out of active variables
  const getHeapObjects = () => {
    if (!stepInfo || !stepInfo.variables) return [];
    const heap = [];
    const seen = new Set();
    
    stepInfo.variables.forEach(v => {
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
  };

  const heapObjects = getHeapObjects();

  return (
    <div className="debugger-page">
      <header className="debugger-header">
        <button className="back-btn" onClick={() => navigate('/dashboard')}>
          &larr; Back to Dashboard
        </button>
        <div className="header-logo">
          ⚡ JDI Execution Engine
        </div>
        <div style={{ width: '80px' }}></div>
      </header>

      <main className="debugger-grid">
        {/* Controls toolbar */}
        <section className="control-panel">
          <div className="editor-info">
            <select value={selectedTemplate} onChange={handleTemplateChange} className="class-name-input">
              {Object.keys(CODE_TEMPLATES).map(k => (
                <option key={k} value={k}>{CODE_TEMPLATES[k].name}</option>
              ))}
            </select>
            <input
              type="text"
              value={className}
              onChange={(e) => setClassName(e.target.value)}
              placeholder="Main Class Name"
              className="class-name-input"
            />
          </div>

          <div className="control-buttons">
            <button
              onClick={handleCompileAndRun}
              className="ctrl-btn primary"
              disabled={isLoading}
            >
              {isLoading ? 'Compiling...' : '⚡ Compile & Run'}
            </button>
            <button
              onClick={handleRestart}
              className="ctrl-btn"
              disabled={!stepInfo}
              title="Restart trace execution"
            >
              🔄 Restart
            </button>
            <button
              onClick={handleStepBackward}
              className="ctrl-btn"
              disabled={!stepInfo}
            >
              &larr; Step Back
            </button>
            <button
              onClick={togglePlayback}
              className="ctrl-btn"
              disabled={!stepInfo}
            >
              {isPlaying ? '⏸️ Pause' : '▶️ Resume'}
            </button>
            <button
              onClick={handleStepForward}
              className="ctrl-btn"
              disabled={!stepInfo}
            >
              Step Forward &rarr;
            </button>
          </div>
        </section>

        {/* Code Editor Pane */}
        <section className="editor-section">
          <div className="section-header">
            <span>Editor (Monaco)</span>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Target: {className}.java
            </span>
          </div>
          <div className="monaco-wrapper">
            <Editor
              height="100%"
              language="java"
              theme="vs-dark"
              value={code}
              onChange={(val) => setCode(val)}
              onMount={handleEditorDidMount}
              options={{
                minimap: { enabled: false },
                fontSize: 14,
                lineNumbers: 'on',
                scrollBeyondLastLine: false,
                automaticLayout: true
              }}
            />
          </div>
        </section>

        {/* Visualizer Panes (Stack & Heap) */}
        <section className="visualizer-section">
          {stepInfo && stepInfo.explanation && (
            <div className="explanation-banner">
              💡 <strong>Explain:</strong> {stepInfo.explanation}
            </div>
          )}

          <div className="memory-visualizer">
            {/* Stack Memory */}
            <div className="memory-pane">
              <div className="section-header">Stack Memory (Call Stack)</div>
              <div className="memory-content">
                {stepInfo && stepInfo.callStack && stepInfo.callStack.length > 0 ? (
                  stepInfo.callStack.map((frame, idx) => (
                    <div key={idx} className={`stack-frame ${idx === 0 ? 'active-frame' : ''}`}>
                      <div className="frame-title">
                        <span>{frame.methodName}()</span>
                        <span className="frame-line">line {frame.lineNumber}</span>
                      </div>
                      {frame.localVariables && frame.localVariables.length > 0 && (
                        <div className="frame-variables">
                          {frame.localVariables.map((v, vIdx) => (
                            <div key={vIdx} className="frame-var">
                              <span style={{ color: 'var(--text-secondary)' }}>{v.name}</span>
                              <span style={{ fontWeight: 600 }}>{v.value}</span>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  ))
                ) : (
                  <span className="empty-state">No active stack frames. Click Compile & Run.</span>
                )}
              </div>
            </div>

            {/* Heap Memory */}
            <div className="memory-pane">
              <div className="section-header">Heap Memory (Objects)</div>
              <div className="memory-content">
                {heapObjects.length > 0 ? (
                  heapObjects.map((obj, idx) => (
                    <div key={idx} className="heap-object">
                      <div className="heap-addr">{obj.address}</div>
                      <div className="heap-type">{obj.type}</div>
                      <div style={{ wordBreak: 'break-all' }}>Value: {obj.value}</div>
                    </div>
                  ))
                ) : (
                  <span className="empty-state">No objects on the heap.</span>
                )}
              </div>
            </div>
          </div>
        </section>

        {/* Compile Error banner */}
        {compileError && (
          <section style={{ gridColumn: '1 / -1' }} className="auth-error">
            <pre style={{ whiteSpace: 'pre-wrap', fontFamily: 'JetBrains Mono', fontSize: '0.85rem' }}>
              {compileError}
            </pre>
          </section>
        )}

        {/* Variables Table */}
        <section className="variables-section">
          <div className="section-header">Variables Inspector</div>
          <div className="variables-table-wrapper">
            <table className="variables-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Type</th>
                  <th>Value</th>
                  <th>Memory Address</th>
                  <th>Scope</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {stepInfo && stepInfo.variables && stepInfo.variables.length > 0 ? (
                  stepInfo.variables.map((v, idx) => (
                    <tr key={idx}>
                      <td style={{ fontFamily: 'JetBrains Mono', fontWeight: 600 }}>{v.name}</td>
                      <td>{v.type}</td>
                      <td style={{ fontFamily: 'JetBrains Mono', color: '#ffb340' }}>{v.value}</td>
                      <td style={{ fontFamily: 'JetBrains Mono', color: '#3ec6e0' }}>{v.memoryAddress}</td>
                      <td>{v.scope}()</td>
                      <td>
                        {v.status === 'NEW' && <span className="status-badge new">NEW</span>}
                        {v.status === 'UPDATED' && <span className="status-badge updated">UPDATED</span>}
                        {v.status === 'UNCHANGED' && <span style={{ color: 'var(--text-secondary)' }}>UNCHANGED</span>}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-secondary)' }}>
                      No variables active.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* Console Terminal */}
        <section className="terminal-section">
          <div className="section-header">Console Output</div>
          <div className="terminal-content">
            {stepInfo && stepInfo.output ? stepInfo.output : 'Console output will appear here...'}
          </div>
        </section>
      </main>
    </div>
  );
};

export default DebuggerPage;
