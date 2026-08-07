import React, { useMemo } from 'react';

const LinkedListVisualizer = ({ stepInfo }) => {
  const nodes = useMemo(() => {
    if (!stepInfo || !stepInfo.variables) return [];
    
    const heapObjects = [];
    const seen = new Set();
    
    // Find all objects
    stepInfo.variables.forEach(v => {
      const isPrimitive = ['int', 'double', 'float', 'boolean', 'char', 'long', 'short', 'byte'].includes(v.type);
      if (!isPrimitive && v.memoryAddress && !seen.has(v.memoryAddress)) {
        seen.add(v.memoryAddress);
        heapObjects.push({
          address: v.memoryAddress,
          type: v.type,
          value: v.value,
          name: v.name
        });
      }
    });

    return heapObjects;
  }, [stepInfo]);

  if (!nodes || nodes.length === 0) {
    return (
      <div className="visualization-placeholder">
        <p>No objects/nodes found on the heap.</p>
      </div>
    );
  }

  return (
    <div className="linkedlist-visualizer">
      <h4 style={{ marginBottom: '1rem', color: 'var(--text-secondary)' }}>Objects & References</h4>
      <div className="linkedlist-container">
        {nodes.map((node, i) => (
          <React.Fragment key={node.address}>
            <div className="ll-node">
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{node.name || node.type}</span>
                <div className="ll-box">
                  <div className="ll-value">
                    {/* Extract value if it matches Node{value=X...} pattern, else show raw value */}
                    {node.value.match(/value=([^,]+)/) ? node.value.match(/value=([^,]+)/)[1] : 'Obj'}
                  </div>
                  <div className="ll-next">
                    {node.value.match(/next=([^,}]+)/) ? '•' : 'null'}
                  </div>
                </div>
                <span style={{ fontSize: '0.7rem', color: 'var(--primary)', fontFamily: 'JetBrains Mono' }}>{node.address}</span>
              </div>
            </div>
            {i < nodes.length - 1 && (
              <div className="ll-pointer">
                &rarr;
              </div>
            )}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
};

export default LinkedListVisualizer;
