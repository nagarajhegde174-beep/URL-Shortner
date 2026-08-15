import React, { useMemo } from 'react';

const QueueVisualizer = ({ stepInfo }) => {
  const metadata = stepInfo?.metadata || {};
  const queueSnapshot = metadata.queueSnapshot || [];
  const frontIndex = metadata.frontIndex !== undefined ? metadata.frontIndex : 0;
  const rearIndex = metadata.rearIndex !== undefined ? metadata.rearIndex : -1;
  const queueVariableName = metadata.queueVariableName || 'queue';
  const operation = metadata.operation || 'QUEUE_UPDATE';
  const isPriorityQueue = metadata.isPriorityQueue || false;
  const pointers = metadata.pointers || {};

  // Operation badge colors
  const getOpBadgeStyle = () => {
    switch (operation) {
      case 'ENQUEUE':
        return { background: 'rgba(76,175,80,0.1)', color: '#4caf50', border: '1px solid rgba(76,175,80,0.2)' };
      case 'DEQUEUE':
        return { background: 'rgba(244,67,54,0.1)', color: '#f44336', border: '1px solid rgba(244,67,54,0.2)' };
      case 'PEEK':
        return { background: 'rgba(255,152,0,0.1)', color: '#ff9800', border: '1px solid rgba(255,152,0,0.2)' };
      case 'FRONT_MOVE':
      case 'REAR_MOVE':
        return { background: 'rgba(156,39,176,0.1)', color: '#9c27b0', border: '1px solid rgba(156,39,176,0.2)' };
      default:
        return { background: 'rgba(62,198,224,0.1)', color: '#3ec6e0', border: '1px solid rgba(62,198,224,0.2)' };
    }
  };

  // Find if there is a backing array (e.g. for CircularQueue or QueueArray)
  const backingArray = useMemo(() => {
    if (!stepInfo || !stepInfo.variables) return null;
    const arrayVar = stepInfo.variables.find(
      v => (v.name === 'arr' || v.name === 'queue') && v.type && v.type.includes('[]') && v.value && v.value.startsWith('[')
    );
    if (arrayVar) {
      try {
        const parsed = JSON.parse(arrayVar.value);
        if (Array.isArray(parsed)) return parsed;
      } catch (e) {
        return null;
      }
    }
    return null;
  }, [stepInfo]);

  // Determine if it is circular
  const isCircular = stepInfo?.className?.toLowerCase()?.includes('circular') || false;

  // Render circular queue or array queue layout
  const renderArrayBasedQueue = (arr) => {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1.5rem', width: '100%' }}>
        <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', padding: '1rem', background: 'rgba(255,255,255,0.01)', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.04)' }}>
          {arr.map((val, idx) => {
            const isFront = idx === frontIndex;
            const isRear = idx === rearIndex;
            const hasValue = val !== 0 && val !== '0' && val !== null;
            
            // Highlight cells inside the queue range
            let inRange = false;
            if (frontIndex <= rearIndex) {
              inRange = idx >= frontIndex && idx <= rearIndex;
            } else if (isCircular) {
              // Wrapped range
              inRange = idx >= frontIndex || idx <= rearIndex;
            }

            return (
              <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>[{idx}]</span>
                
                <div 
                  style={{
                    width: '50px',
                    height: '50px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: inRange 
                      ? 'linear-gradient(135deg, var(--accent) 0%, #ffb300 100%)' 
                      : 'rgba(255,255,255,0.02)',
                    color: inRange ? '#000' : 'var(--text-secondary)',
                    border: inRange ? '2px solid var(--accent)' : '1px dashed rgba(255,255,255,0.1)',
                    borderRadius: '8px',
                    fontFamily: "'JetBrains Mono', monospace",
                    fontWeight: 'bold',
                    boxShadow: inRange ? '0 4px 12px rgba(255,179,0,0.2)' : 'none',
                    transition: 'all 0.3s ease'
                  }}
                >
                  {hasValue ? val : ''}
                </div>

                <div style={{ height: '35px', display: 'flex', flexDirection: 'column', alignItems: 'center', fontSize: '0.75rem', fontWeight: 'bold' }}>
                  {isFront && <span style={{ color: '#4caf50' }}>▲ FRONT</span>}
                  {isRear && <span style={{ color: '#f44336' }}>▲ REAR</span>}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  if (queueSnapshot.length === 0 && !backingArray) {
    return (
      <div className="visualization-placeholder" style={{ padding: '2rem', textAlign: 'center', color: '#8b90a8' }}>
        <p>Queue is empty.</p>
        <p style={{ fontSize: '0.85rem', marginTop: '0.5rem' }}>Enqueue elements to visualize the DSA Queue.</p>
      </div>
    );
  }

  return (
    <div className="queue-visualizer-outer" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%', gap: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center', maxWidth: '500px' }}>
        <h4 style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Queue: <span style={{ fontFamily: 'monospace', color: 'var(--accent)' }}>{queueVariableName}</span>
          {isPriorityQueue && <span style={{ fontSize: '0.75rem', color: '#ffb340', marginLeft: '0.5rem' }}>(PriorityQueue)</span>}
        </h4>
        <span style={{ fontSize: '0.75rem', padding: '4px 8px', borderRadius: '4px', fontWeight: 'bold', ...getOpBadgeStyle() }}>
          {operation}
        </span>
      </div>

      {backingArray ? (
        // Render backing array layout for Circular/Array Queues
        renderArrayBasedQueue(backingArray)
      ) : (
        // Standard horizontal FIFO/Priority Queue layout
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', gap: '1rem', overflowX: 'auto', padding: '1rem' }}>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <span style={{ color: '#4caf50', fontSize: '0.8rem', fontWeight: 'bold', marginBottom: '4px' }}>FRONT</span>
            <span style={{ color: 'var(--text-secondary)', fontSize: '1.2rem' }}>&larr;</span>
          </div>

          <div 
            className="queue-container"
            style={{
              display: 'flex',
              alignItems: 'center',
              border: '3px solid var(--border)',
              borderLeft: 'none',
              borderRight: 'none',
              height: '76px',
              padding: '8px',
              gap: '8px',
              background: 'rgba(255,255,255,0.01)',
              borderRadius: '4px'
            }}
          >
            {queueSnapshot.map((val, idx) => {
              const isFront = idx === 0;
              const isRear = idx === queueSnapshot.length - 1;
              const isMinVal = isPriorityQueue && isFront;

              return (
                <div
                  key={idx}
                  className="queue-element"
                  style={{
                    height: '42px',
                    minWidth: '50px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    padding: '0 12px',
                    background: isMinVal 
                      ? 'linear-gradient(135deg, #ffb300 0%, #ff8f00 100%)'
                      : isFront 
                        ? 'linear-gradient(135deg, var(--accent) 0%, #00e676 100%)' 
                        : 'linear-gradient(135deg, var(--primary) 0%, #0288d1 100%)',
                    color: (isMinVal || isFront) ? '#000' : '#fff',
                    border: '1px solid rgba(255,255,255,0.1)',
                    borderRadius: '6px',
                    fontFamily: "'JetBrains Mono', monospace",
                    fontWeight: 'bold',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                    transition: 'all 0.3s ease',
                    fontSize: '0.9rem',
                    position: 'relative'
                  }}
                >
                  {val}
                  {isMinVal && (
                    <span style={{ position: 'absolute', top: '-6px', background: '#ff3d00', color: '#fff', fontSize: '0.55rem', padding: '1px 3px', borderRadius: '3px', fontWeight: 'bold' }}>
                      MIN
                    </span>
                  )}
                </div>
              );
            })}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <span style={{ color: '#f44336', fontSize: '0.8rem', fontWeight: 'bold', marginBottom: '4px' }}>REAR</span>
            <span style={{ color: 'var(--text-secondary)', fontSize: '1.2rem' }}>&larr;</span>
          </div>
        </div>
      )}

      {pointers.size !== undefined && (
        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          Size: <span style={{ color: 'var(--accent)' }}>{pointers.size}</span>
        </div>
      )}
    </div>
  );
};

export default QueueVisualizer;
