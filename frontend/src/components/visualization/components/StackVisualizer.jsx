import React from 'react';

const StackVisualizer = ({ stepInfo }) => {
  const metadata = stepInfo?.metadata || {};
  const stackSnapshot = metadata.stackSnapshot || [];
  const topIndex = metadata.topIndex !== undefined ? metadata.topIndex : -1;
  const stackVariableName = metadata.stackVariableName || 'stack';
  const operation = metadata.operation || 'STACK_UPDATE';
  const pointers = metadata.pointers || {};

  // Check if we have active indices or pointers
  const activeIndices = metadata.indices || [];

  // Determine styling color based on operation
  const getOpBadgeStyle = () => {
    switch (operation) {
      case 'PUSH':
        return { background: 'rgba(76,175,80,0.1)', color: '#4caf50', border: '1px solid rgba(76,175,80,0.2)' };
      case 'POP':
        return { background: 'rgba(244,67,54,0.1)', color: '#f44336', border: '1px solid rgba(244,67,54,0.2)' };
      case 'PEEK':
        return { background: 'rgba(255,152,0,0.1)', color: '#ff9800', border: '1px solid rgba(255,152,0,0.2)' };
      default:
        return { background: 'rgba(62,198,224,0.1)', color: '#3ec6e0', border: '1px solid rgba(62,198,224,0.2)' };
    }
  };

  if (stackSnapshot.length === 0) {
    return (
      <div className="visualization-placeholder" style={{ padding: '2rem', textAlign: 'center', color: '#8b90a8' }}>
        <p>Stack is empty.</p>
        <p style={{ fontSize: '0.85rem', marginTop: '0.5rem' }}>Push elements to visualize the DSA Stack.</p>
      </div>
    );
  }

  return (
    <div className="stack-visualizer-outer" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%', gap: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center', maxWidth: '400px' }}>
        <h4 style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Stack: <span style={{ fontFamily: 'monospace', color: 'var(--accent)' }}>{stackVariableName}</span>
        </h4>
        <span style={{ fontSize: '0.75rem', padding: '4px 8px', borderRadius: '4px', fontWeight: 'bold', ...getOpBadgeStyle() }}>
          {operation}
        </span>
      </div>

      <div style={{ display: 'flex', gap: '2rem', alignItems: 'center', justifyContent: 'center', minHeight: '260px' }}>
        {/* Pointer Labels column on the left */}
        <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'flex-end', height: '200px', gap: '0.5rem', paddingBottom: '0.5rem' }}>
          {stackSnapshot.map((_, idx) => {
            const isTop = idx === 0;
            return (
              <div 
                key={idx} 
                style={{ 
                  height: '38px', 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'flex-end',
                  fontSize: '0.8rem',
                  fontWeight: 'bold',
                  color: isTop ? 'var(--accent)' : 'var(--text-secondary)',
                  opacity: isTop ? 1 : 0.6,
                  paddingRight: '8px'
                }}
              >
                {isTop ? 'TOP →' : ''}
              </div>
            );
          })}
        </div>

        {/* Stack Cup container */}
        <div 
          className="stack-container" 
          style={{ 
            display: 'flex', 
            flexDirection: 'column', 
            justifyContent: 'flex-end', 
            border: '3px solid var(--border)', 
            borderTop: 'none', 
            width: '140px', 
            height: '220px', 
            padding: '8px', 
            gap: '8px', 
            borderRadius: '0 0 12px 12px',
            background: 'rgba(255,255,255,0.01)',
            boxShadow: 'inset 0 -10px 20px rgba(0,0,0,0.2)'
          }}
        >
          {stackSnapshot.map((val, idx) => {
            const isTop = idx === 0;
            // Determine active/highlight status
            const isActive = isTop && (operation === 'PUSH' || operation === 'POP' || operation === 'PEEK');
            
            return (
              <div 
                key={idx} 
                className={`stack-element ${isActive ? 'active' : ''}`}
                style={{ 
                  height: '38px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: isActive 
                    ? 'linear-gradient(135deg, var(--accent) 0%, #ffc107 100%)' 
                    : 'linear-gradient(135deg, var(--primary) 0%, #1976d2 100%)',
                  color: isActive ? '#000' : '#fff',
                  border: isActive ? '1px solid #ffe082' : '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '6px',
                  fontFamily: "'JetBrains Mono', monospace",
                  fontWeight: 'bold',
                  boxShadow: isActive ? '0 4px 12px rgba(255,193,7,0.3)' : '0 2px 4px rgba(0,0,0,0.1)',
                  transition: 'all 0.3s ease',
                  fontSize: '0.95rem'
                }}
              >
                {val}
              </div>
            );
          })}
        </div>
      </div>
      
      {pointers.top !== undefined && (
        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          Pointer: <span style={{ color: 'var(--accent)' }}>top = {pointers.top}</span>
        </div>
      )}
    </div>
  );
};

export default StackVisualizer;
