import React, { useMemo } from 'react';

const QueueVisualizer = ({ stepInfo }) => {
  const queueData = useMemo(() => {
    if (!stepInfo || !stepInfo.variables) return [];
    
    let elements = [];
    let queueName = null;
    
    stepInfo.variables.forEach(v => {
      if (v.type && v.type.includes('Queue') && v.value && v.value.startsWith('[')) {
        try {
          const parsed = JSON.parse(v.value);
          if (Array.isArray(parsed)) {
            queueName = v.name;
            elements = parsed;
          }
        } catch (e) {
          // ignore
        }
      }
    });
    
    return { name: queueName, elements };
  }, [stepInfo]);

  if (!queueData.elements || queueData.elements.length === 0) {
    return (
      <div className="visualization-placeholder">
        <p>No active Queue found.</p>
      </div>
    );
  }

  return (
    <div className="queue-visualizer">
      {queueData.name && <h4 style={{ marginBottom: '1rem', color: 'var(--text-secondary)' }}>Queue: {queueData.name}</h4>}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>Front &larr;</span>
        <div className="queue-container">
          {queueData.elements.map((el, i) => (
            <div key={i} className="queue-element">
              {el}
            </div>
          ))}
        </div>
        <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>&larr; Rear</span>
      </div>
    </div>
  );
};

export default QueueVisualizer;
