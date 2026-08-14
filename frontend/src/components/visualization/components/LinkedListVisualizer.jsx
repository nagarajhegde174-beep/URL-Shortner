import React, { useMemo } from 'react';

const LinkedListVisualizer = ({ stepInfo }) => {
  const metadata = stepInfo?.metadata || {};
  const nodeSnapshot = metadata.nodeSnapshot || [];
  const nodePointers = metadata.nodePointers || {};
  const activeNodeId = metadata.nodeId;
  const operation = metadata.operation || 'TRAVERSE';

  // Reconstruct the traversal chain to render nodes in logical order
  const { chain, cycleNodeId } = useMemo(() => {
    if (nodeSnapshot.length === 0) return { chain: [], cycleNodeId: null };

    const nodeMap = new Map();
    nodeSnapshot.forEach(node => {
      nodeMap.set(node.nodeId, node);
    });

    // 1. Determine starting node
    let startNodeId = nodePointers.head || nodePointers.current || nodePointers.curr;
    
    // If no active start pointer exists, look for a root node (no incoming nextNodeId references)
    if (!startNodeId || !nodeMap.has(startNodeId)) {
      const incoming = new Set(nodeSnapshot.map(n => n.nextNodeId).filter(Boolean));
      const roots = nodeSnapshot.filter(n => !incoming.has(n.nodeId));
      if (roots.length > 0) {
        startNodeId = roots[0].nodeId;
      } else {
        startNodeId = nodeSnapshot[0].nodeId;
      }
    }

    // 2. Traverse the list chain
    const nodeChain = [];
    const visited = new Set();
    let currentId = startNodeId;
    let cycleId = null;

    while (currentId && nodeMap.has(currentId)) {
      if (visited.has(currentId)) {
        cycleId = currentId; // Cycle detected
        break;
      }
      visited.add(currentId);
      const node = nodeMap.get(currentId);
      nodeChain.push(node);
      currentId = node.nextNodeId;
    }

    // 3. Add any disconnected/new nodes to show them on the canvas
    nodeSnapshot.forEach(node => {
      if (!visited.has(node.nodeId)) {
        nodeChain.push(node);
      }
    });

    return { chain: nodeChain, cycleNodeId: cycleId };
  }, [nodeSnapshot, nodePointers]);

  if (nodeSnapshot.length === 0) {
    return (
      <div className="visualization-placeholder" style={{ padding: '2rem', textAlign: 'center', color: '#8b90a8' }}>
        <p>No active linked list nodes found in scope.</p>
        <p style={{ fontSize: '0.85rem', marginTop: '0.5rem' }}>Select an algorithm and click Run to begin tracing.</p>
      </div>
    );
  }

  // Check if this is a doubly linked list by checking for presence of prevNodeId in snapshot
  const isDoubly = nodeSnapshot.some(node => 'prevNodeId' in node);

  return (
    <div className="linkedlist-visualizer">
      <div className="linkedlist-container">
        {chain.map((node, i) => {
          const isNodeActive = activeNodeId === node.nodeId;
          const activeLabels = Object.keys(nodePointers).filter(name => nodePointers[name] === node.nodeId);
          
          // Determine operation highlights
          let boxClass = 'll-box';
          if (isNodeActive) {
            boxClass += ' active';
            if (operation === 'NODE_CREATE') boxClass += ' create-op';
            if (operation === 'NODE_DELETE') boxClass += ' delete-op';
          }

          return (
            <React.Fragment key={node.nodeId}>
              <div className="ll-node-wrapper">
                
                {/* 1. Pointer Labels above the Node */}
                <div style={{ height: '24px', display: 'flex', flexDirection: 'column-reverse', alignItems: 'center', marginBottom: '8px' }}>
                  {activeLabels.map((label, labelIdx) => (
                    <div key={labelIdx} style={{ fontSize: '0.75rem', fontWeight: 'bold', color: label === 'head' ? 'var(--success)' : label === 'curr' || label === 'current' ? 'var(--accent)' : 'var(--text-secondary)', background: 'rgba(255,255,255,0.05)', padding: '1px 5px', borderRadius: '4px', border: '1px solid rgba(255,255,255,0.08)', whiteSpace: 'nowrap' }}>
                      ↓ {label}
                    </div>
                  ))}
                </div>

                {/* 2. Node representation block */}
                <div className={boxClass}>
                  
                  {/* Prev field if doubly linked list */}
                  {isDoubly && (
                    <div className="ll-prev-field">
                      {node.prevNodeId ? '•' : 'null'}
                    </div>
                  )}

                  {/* Data field */}
                  <div className="ll-data-field">
                    {node.data}
                  </div>

                  {/* Next field */}
                  <div className="ll-next-field">
                    {node.nextNodeId ? '•' : 'null'}
                  </div>
                </div>

                {/* 3. Memory Address indicator underneath */}
                <span style={{ fontSize: '0.7rem', color: 'rgba(255, 255, 255, 0.25)', fontFamily: 'var(--mono)', marginTop: '6px' }}>
                  {node.nodeId}
                </span>

                {/* 4. Cycle badge if cycle returns here */}
                {cycleNodeId === node.nodeId && (
                  <span className="ll-cycle-badge">CYCLE START</span>
                )}
              </div>

              {/* Node arrow connector */}
              {i < chain.length - 1 ? (
                <div className="ll-arrow-wrapper">
                  <div className="ll-arrow">
                    {isDoubly ? '⇄' : '➔'}
                  </div>
                </div>
              ) : (
                // Draw final null terminator or cycle pointer representation
                <div className="ll-arrow-wrapper">
                  {cycleNodeId ? (
                    <div style={{ color: 'var(--error)', fontSize: '0.8rem', fontWeight: 'bold', border: '1px dashed var(--error)', padding: '2px 6px', borderRadius: '4px' }}>
                      ⟲ Cycle
                    </div>
                  ) : (
                    <div style={{ color: 'rgba(255, 255, 255, 0.15)', fontSize: '0.85rem', fontFamily: 'var(--mono)', fontWeight: 'bold' }}>
                      ✖ NULL
                    </div>
                  )}
                </div>
              )}
            </React.Fragment>
          );
        })}
      </div>
    </div>
  );
};

export default LinkedListVisualizer;
