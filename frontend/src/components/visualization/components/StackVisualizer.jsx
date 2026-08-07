import React from 'react';

const StackVisualizer = ({ stepInfo }) => {
  if (!stepInfo || !stepInfo.callStack || stepInfo.callStack.length === 0) {
    return (
      <div className="visualization-placeholder">
        <p>Call stack is empty.</p>
      </div>
    );
  }

  // Reverse call stack to show bottom-up
  const stackFrames = [...stepInfo.callStack].reverse();

  return (
    <div className="stack-visualizer">
      <h4 style={{ marginBottom: '1rem', color: 'var(--text-secondary)', textAlign: 'center' }}>Call Stack</h4>
      <div className="stack-container">
        {stackFrames.map((frame, idx) => (
          <div key={idx} className="stack-element">
            <div style={{ fontSize: '0.9rem' }}>{frame.methodName}()</div>
            <div style={{ fontSize: '0.7rem', color: 'rgba(255,255,255,0.7)' }}>line {frame.lineNumber}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default StackVisualizer;
