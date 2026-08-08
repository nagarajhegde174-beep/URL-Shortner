import React, { useMemo } from 'react';

const ArrayVisualizer = ({ stepInfo }) => {
  // Extract all arrays in scope
  const arrays = useMemo(() => {
    if (!stepInfo || !stepInfo.variables) return [];

    const foundArrays = [];

    // Check for variables containing serialized arrays like "[10, 20, 30]"
    stepInfo.variables.forEach(v => {
      if (v.type && v.type.includes('[]') && v.value && v.value.startsWith('[')) {
        try {
          const parsed = JSON.parse(v.value);
          if (Array.isArray(parsed)) {
            foundArrays.push({
              name: v.name,
              elements: parsed.map((val, idx) => ({
                value: val,
                index: idx,
                status: v.status
              }))
            });
          }
        } catch (e) {
          // ignore parsing error
        }
      }
    });

    // Fallback: simple brackets heuristic if serialized arrays aren't present
    if (foundArrays.length === 0) {
      const tempElements = {};
      stepInfo.variables.forEach(v => {
        const match = v.name.match(/^([a-zA-Z0-9_]+)\[(\d+)\]$/);
        if (match) {
          const arrName = match[1];
          const index = parseInt(match[2], 10);
          if (!tempElements[arrName]) {
            tempElements[arrName] = [];
          }
          tempElements[arrName][index] = { value: v.value, index, status: v.status };
        }
      });

      Object.keys(tempElements).forEach(name => {
        foundArrays.push({
          name: name,
          elements: tempElements[name]
        });
      });
    }

    return foundArrays;
  }, [stepInfo]);

  if (arrays.length === 0) {
    return (
      <div className="visualization-placeholder" style={{ padding: '2rem', textAlign: 'center', color: '#8b90a8' }}>
        <p>No active array found in the current scope.</p>
        <p style={{ fontSize: '0.85rem', marginTop: '0.5rem' }}>Select an algorithm and click Run to begin tracing.</p>
      </div>
    );
  }

  // Extract metadata properties
  const metadata = stepInfo.metadata || {};
  const activeIndices = metadata.indices || [];
  const pointers = metadata.pointers || {};
  const operation = metadata.operation || 'READ';

  // Binary search dimming boundary
  const low = pointers.low !== undefined ? pointers.low : (pointers.start !== undefined ? pointers.start : -1);
  const high = pointers.high !== undefined ? pointers.high : (pointers.end !== undefined ? pointers.end : -1);
  const mid = pointers.mid !== undefined ? pointers.mid : -1;
  const isBinarySearch = pointers.low !== undefined || pointers.high !== undefined;

  // Sliding window boundary
  const windowStart = pointers.windowStart !== undefined ? pointers.windowStart : (pointers.left !== undefined && pointers.right !== undefined ? pointers.left : -1);
  const windowEnd = pointers.windowEnd !== undefined ? pointers.windowEnd : (pointers.left !== undefined && pointers.right !== undefined ? pointers.right : -1);
  const hasWindow = windowStart !== -1 && windowEnd !== -1 && !isBinarySearch;

  // Maximum / Minimum leader index marker
  let maxIdx = -1;
  let minIdx = -1;
  stepInfo.variables.forEach(v => {
    if (v.name.equals && (v.name === 'maxIdx' || v.name === 'minIdx')) {
      try {
        if (v.name === 'maxIdx') maxIdx = Integer.parseInt(v.value);
        if (v.name === 'minIdx') minIdx = Integer.parseInt(v.value);
      } catch (e) {}
    }
  });

  return (
    <div className="array-visualizer-container" style={{ display: 'flex', flexDirection: 'column', gap: '2rem', width: '100%' }}>
      {arrays.map((arr, arrIdx) => {
        return (
          <div key={arrIdx} className="array-track" style={{ position: 'relative', padding: '2rem 1rem', background: 'rgba(255,255,255,0.01)', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.03)' }}>
            <h4 style={{ margin: '0 0 1.25rem 0', color: 'var(--text-secondary)', fontSize: '0.9rem', fontWeight: 600, display: 'flex', justifyContent: 'space-between' }}>
              <span>Array: <code style={{ color: 'var(--accent)', background: 'transparent', padding: 0 }}>{arr.name}</code></span>
              <span style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.2)' }}>Length: {arr.elements.length}</span>
            </h4>

            <div className="array-container" style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', justifyContent: 'center', alignItems: 'center', position: 'relative' }}>
              {arr.elements.map((el, i) => {
                if (!el) return null;

                // 1. Determine cell states
                const isActive = activeIndices.includes(i);
                const isMid = isBinarySearch && i === mid;
                
                // Dimming state for Binary Search
                let isDimmed = false;
                if (isBinarySearch && low !== -1 && high !== -1) {
                  isDimmed = i < low || i > high;
                }

                // Window position checks
                const isInWindow = hasWindow && i >= windowStart && i <= windowEnd;
                const isWindowBoundary = hasWindow && (i === windowStart || i === windowEnd);

                // Styling configuration
                let cellClass = 'array-value';
                let customStyle = {
                  width: '54px',
                  height: '54px',
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  background: 'var(--bg-secondary)',
                  border: '2px solid var(--border)',
                  borderRadius: '10px',
                  fontWeight: '700',
                  fontFamily: 'var(--mono)',
                  fontSize: '1.2rem',
                  color: 'var(--text-primary)',
                  position: 'relative',
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  opacity: isDimmed ? 0.25 : 1,
                  boxShadow: 'none',
                };

                // Operation coloring
                if (isActive) {
                  if (operation === 'COMPARE') {
                    customStyle.borderColor = '#ffb340'; // gold
                    customStyle.background = 'rgba(255, 179, 64, 0.1)';
                    customStyle.boxShadow = '0 0 15px rgba(255, 179, 64, 0.3)';
                    customStyle.transform = 'translateY(-4px)';
                  } else if (operation === 'SWAP') {
                    customStyle.borderColor = 'var(--error)'; // red
                    customStyle.background = 'rgba(255, 92, 108, 0.15)';
                    customStyle.boxShadow = '0 0 15px rgba(255, 92, 108, 0.4)';
                    customStyle.transform = 'scale(1.08) translateY(-4px)';
                  } else if (operation === 'WRITE') {
                    customStyle.borderColor = 'var(--success)'; // green
                    customStyle.background = 'rgba(46, 202, 127, 0.15)';
                    customStyle.boxShadow = '0 0 15px rgba(46, 202, 127, 0.4)';
                  } else {
                    customStyle.borderColor = 'var(--accent)';
                    customStyle.background = 'var(--accent-bg)';
                  }
                }

                if (isMid) {
                  customStyle.borderColor = '#3ec6e0';
                  customStyle.background = 'rgba(62, 198, 224, 0.1)';
                  customStyle.boxShadow = '0 0 12px rgba(62, 198, 224, 0.3)';
                }

                if (isInWindow) {
                  customStyle.borderColor = 'var(--accent)';
                  if (isWindowBoundary) {
                    customStyle.borderWidth = '3px';
                  }
                }

                // Check pointers mapping to this index
                const activePointers = Object.keys(pointers).filter(pName => pointers[pName] === i);

                return (
                  <div key={i} className="array-element" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative' }}>
                    
                    {/* Top Pointers Annotation */}
                    <div className="pointer-labels-top" style={{ height: '24px', display: 'flex', flexDirection: 'column-reverse', alignItems: 'center', marginBottom: '4px' }}>
                      {activePointers.map((pName, pIdx) => (
                        <div key={pIdx} style={{ fontSize: '0.75rem', fontWeight: 'bold', color: pName === 'low' ? '#ff5c6c' : pName === 'high' ? '#2eca7f' : pName === 'mid' ? '#3ec6e0' : 'var(--accent)', background: 'rgba(255,255,255,0.05)', padding: '1px 5px', borderRadius: '4px', border: '1px solid rgba(255,255,255,0.08)' }}>
                          ↓ {pName}
                        </div>
                      ))}
                    </div>

                    {/* Array Cell */}
                    <div className={cellClass} style={customStyle}>
                      {el.value}
                      
                      {/* Sub-badge indicating status updates */}
                      {el.status === 'UPDATED' && (
                        <span style={{ position: 'absolute', top: '-4px', right: '-4px', width: '10px', height: '10px', background: 'var(--success)', borderRadius: '50%', border: '2px solid var(--bg-primary)' }}></span>
                      )}
                    </div>

                    {/* Array Index */}
                    <div className="array-index" style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: 'var(--text-secondary)', fontFamily: 'var(--mono)' }}>
                      {i}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Sliding Window Outline Overlay */}
            {hasWindow && (
              <div className="window-overlay-indicator" style={{ textAlign: 'center', marginTop: '1.25rem', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                Sliding Window: <strong style={{ color: 'var(--accent)' }}>[{windowStart} ... {windowEnd}]</strong>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
};

export default ArrayVisualizer;
