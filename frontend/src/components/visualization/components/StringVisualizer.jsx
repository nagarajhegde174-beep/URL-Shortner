import React, { useMemo } from 'react';

const StringVisualizer = ({ stepInfo }) => {
  // Extract all string-like variables in scope
  const strings = useMemo(() => {
    if (!stepInfo || !stepInfo.variables) return [];

    const foundStrings = [];

    stepInfo.variables.forEach(v => {
      // Handle string variables and character arrays
      if (v.type && (v.type.toLowerCase().includes('string') || v.type.toLowerCase().includes('char[]'))) {
        let rawVal = v.value;
        if (rawVal && rawVal !== 'null') {
          // Strip double quotes if present
          if (rawVal.startsWith('"') && rawVal.endsWith('"')) {
            rawVal = rawVal.substring(1, rawVal.length - 1);
          }
          // Parse array representation if present
          if (rawVal.startsWith('[') && rawVal.endsWith(']')) {
            try {
              const parsed = JSON.parse(rawVal.replace(/'/g, '"'));
              if (Array.isArray(parsed)) {
                rawVal = parsed.join('');
              }
            } catch (e) {
              rawVal = rawVal.substring(1, rawVal.length - 1)
                .split(',')
                .map(s => s.trim().replace(/'/g, ''))
                .join('');
            }
          }

          foundStrings.push({
            name: v.name,
            value: rawVal,
            elements: rawVal.split('').map((char, idx) => ({
              value: char,
              index: idx,
              status: v.status
            }))
          });
        }
      }
    });

    return foundStrings;
  }, [stepInfo]);

  if (strings.length === 0) {
    return (
      <div className="visualization-placeholder" style={{ padding: '2rem', textAlign: 'center', color: '#8b90a8' }}>
        <p>No active string found in the current scope.</p>
        <p style={{ fontSize: '0.85rem', marginTop: '0.5rem' }}>Select an algorithm and click Run to begin tracing.</p>
      </div>
    );
  }

  // Extract metadata properties
  const metadata = stepInfo.metadata || {};
  const activeIndices = metadata.indices || [];
  const pointers = metadata.pointers || {};
  const operation = metadata.operation || 'READ';
  const characterStates = metadata.characterStates || {};
  const lpsArray = metadata.lpsArray || null;
  const pattern = metadata.pattern ? metadata.pattern.replace(/^"|"$/g, '') : null;
  const patternOffset = metadata.patternOffset !== null && metadata.patternOffset !== undefined ? metadata.patternOffset : 0;
  const rollingHash = metadata.rollingHash || null;

  return (
    <div className="string-visualizer-container" style={{ display: 'flex', flexDirection: 'column', gap: '2rem', width: '100%' }}>
      
      {/* Rabin-Karp Rolling Hash Badge */}
      {rollingHash !== null && (
        <div className="hash-indicator" style={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
          <div style={{ background: 'var(--accent-bg)', border: '1px solid var(--accent)', padding: '0.5rem 1rem', borderRadius: '8px', fontSize: '0.9rem', color: 'var(--text-primary)' }}>
            Rolling Hash Value: <strong style={{ color: 'var(--accent)', fontFamily: 'var(--mono)' }}>{rollingHash}</strong>
          </div>
        </div>
      )}

      {strings.map((arr, arrIdx) => {
        // Skip drawing pattern helper row on the pattern variable itself
        const isPatternVar = pattern && arr.name.toLowerCase() === 'pattern';
        const showPatternOverlay = pattern && !isPatternVar;

        return (
          <div key={arrIdx} className="string-track" style={{ position: 'relative', padding: '2rem 1rem', background: 'rgba(255,255,255,0.01)', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.03)' }}>
            <h4 style={{ margin: '0 0 1.25rem 0', color: 'var(--text-secondary)', fontSize: '0.9rem', fontWeight: 600, display: 'flex', justifyContent: 'space-between' }}>
              <span>Variable: <code style={{ color: 'var(--accent)', background: 'transparent', padding: 0 }}>{arr.name}</code></span>
              <span style={{ fontSize: '0.8rem', color: 'rgba(255,255,255,0.2)' }}>Length: {arr.elements.length}</span>
            </h4>

            {/* String Character Grid */}
            <div className="string-grid" style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', justifyContent: 'center', alignItems: 'center', position: 'relative' }}>
              {arr.elements.map((el, i) => {
                if (!el) return null;

                const isActive = activeIndices.includes(i);
                const charState = characterStates[i] || 'ACTIVE';
                const activePointers = Object.keys(pointers).filter(pName => pointers[pName] === i);

                // Styling configuration
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
                  boxShadow: 'none',
                };

                // Highlight based on state tokens
                if (charState === 'COMPARE') {
                  customStyle.borderColor = '#ffb340'; // gold
                  customStyle.background = 'rgba(255, 179, 64, 0.1)';
                  customStyle.boxShadow = '0 0 15px rgba(255, 179, 64, 0.3)';
                } else if (charState === 'MATCH') {
                  customStyle.borderColor = 'var(--success)'; // green
                  customStyle.background = 'rgba(46, 202, 127, 0.15)';
                  customStyle.boxShadow = '0 0 15px rgba(46, 202, 127, 0.4)';
                } else if (charState === 'MISMATCH') {
                  customStyle.borderColor = 'var(--error)'; // red
                  customStyle.background = 'rgba(255, 92, 108, 0.15)';
                  customStyle.boxShadow = '0 0 15px rgba(255, 92, 108, 0.4)';
                } else if (charState === 'WINDOW') {
                  customStyle.borderColor = 'var(--accent)'; // blue
                  customStyle.background = 'rgba(74, 144, 226, 0.1)';
                } else if (isActive) {
                  customStyle.borderColor = 'var(--accent)';
                }

                return (
                  <div key={i} className="string-element" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative' }}>
                    
                    {/* Top Pointers Annotation */}
                    <div className="pointer-labels-top" style={{ height: '24px', display: 'flex', flexDirection: 'column-reverse', alignItems: 'center', marginBottom: '4px' }}>
                      {activePointers.map((pName, pIdx) => (
                        <div key={pIdx} style={{ fontSize: '0.75rem', fontWeight: 'bold', color: 'var(--accent)', background: 'rgba(255,255,255,0.05)', padding: '1px 5px', borderRadius: '4px', border: '1px solid rgba(255,255,255,0.08)' }}>
                          ↓ {pName}
                        </div>
                      ))}
                    </div>

                    {/* Character Cell */}
                    <div className="char-cell" style={customStyle}>
                      {el.value === ' ' ? '␣' : el.value}
                      
                      {el.status === 'UPDATED' && (
                        <span style={{ position: 'absolute', top: '-4px', right: '-4px', width: '10px', height: '10px', background: 'var(--success)', borderRadius: '50%', border: '2px solid var(--bg-primary)' }}></span>
                      )}
                    </div>

                    {/* Index */}
                    <div className="char-index" style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: 'var(--text-secondary)', fontFamily: 'var(--mono)' }}>
                      {i}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Pattern Overlay Alignment Row */}
            {showPatternOverlay && (
              <div className="pattern-overlay" style={{ display: 'flex', flexDirection: 'column', marginTop: '1.5rem', borderTop: '1px solid rgba(255,255,255,0.03)', paddingTop: '1.5rem' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '0.75rem', textAlign: 'left', paddingLeft: '1rem' }}>
                  Pattern Align: <span style={{ color: 'var(--accent)' }}>"{pattern}"</span> (offset: {patternOffset})
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', justifyContent: 'center', alignItems: 'center' }}>
                  {arr.elements.map((_, i) => {
                    const charIdx = i - patternOffset;
                    const hasChar = charIdx >= 0 && charIdx < pattern.length;

                    return (
                      <div key={i} style={{ width: '54px', height: '54px', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                        {hasChar ? (
                          <div style={{
                            width: '54px',
                            height: '54px',
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            background: 'rgba(255, 255, 255, 0.02)',
                            border: '2px dashed var(--accent)',
                            borderRadius: '10px',
                            fontWeight: '700',
                            fontFamily: 'var(--mono)',
                            fontSize: '1.2rem',
                            color: 'var(--accent)',
                          }}>
                            {pattern.charAt(charIdx)}
                          </div>
                        ) : (
                          <div style={{ width: '54px', height: '54px' }} />
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* LPS Array Table (KMP Search specific) */}
            {lpsArray && !isPatternVar && (
              <div className="lps-array-container" style={{ marginTop: '2rem', borderTop: '1px solid rgba(255,255,255,0.03)', paddingTop: '1.5rem', textAlign: 'center' }}>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>
                  KMP Longest Prefix Suffix (LPS) Array:
                </div>
                <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                  {lpsArray.map((val, idx) => (
                    <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                      <div style={{ border: '1px solid var(--border)', background: 'var(--bg-secondary)', width: '36px', height: '36px', display: 'flex', justifyContent: 'center', alignItems: 'center', borderRadius: '6px', fontSize: '0.9rem', color: 'var(--text-primary)', fontFamily: 'var(--mono)', fontWeight: 'bold' }}>
                        {val}
                      </div>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)', marginTop: '0.25rem', fontFamily: 'var(--mono)' }}>{idx}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

          </div>
        );
      })}
    </div>
  );
};

export default StringVisualizer;
