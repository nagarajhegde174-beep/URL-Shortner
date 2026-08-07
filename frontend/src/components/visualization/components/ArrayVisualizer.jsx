import React, { useMemo } from 'react';

const ArrayVisualizer = ({ stepInfo }) => {
  // Extract array data from stepInfo.
  // Assuming the backend sends array elements as individual variables like "numbers[0]", "numbers[1]"
  // Or there's a specific array object.
  const arrayData = useMemo(() => {
    if (!stepInfo || !stepInfo.variables) return [];
    
    const elements = [];
    let arrayName = null;
    
    // Simple heuristic: find variables with brackets
    stepInfo.variables.forEach(v => {
      const match = v.name.match(/^([a-zA-Z0-9_]+)\[(\d+)\]$/);
      if (match) {
        arrayName = match[1];
        const index = parseInt(match[2], 10);
        elements[index] = { ...v, index };
      }
    });
    
    // If we didn't find "numbers[0]" format, maybe the array itself is serialized in a 'value' string
    // e.g., "[10, 20, 30]"
    if (elements.length === 0) {
      stepInfo.variables.forEach(v => {
        if (v.type && v.type.includes('[]') && v.value && v.value.startsWith('[')) {
          try {
            const parsed = JSON.parse(v.value);
            if (Array.isArray(parsed)) {
              arrayName = v.name;
              parsed.forEach((val, idx) => {
                elements[idx] = { value: val, index: idx, status: v.status };
              });
            }
          } catch (e) {
            // ignore parse errors
          }
        }
      });
    }
    
    return { name: arrayName, elements };
  }, [stepInfo]);

  if (!arrayData.elements || arrayData.elements.length === 0) {
    return (
      <div className="visualization-placeholder">
        <p>No active array found in the current scope.</p>
      </div>
    );
  }

  return (
    <div className="array-visualizer">
      {arrayData.name && <h4 style={{ marginBottom: '1rem', color: 'var(--text-secondary)' }}>Array: {arrayData.name}</h4>}
      <div className="array-container">
        {arrayData.elements.map((el, i) => (
          <div key={i} className="array-element">
            <div className={`array-value ${el?.status === 'UPDATED' ? 'active' : ''}`}>
              {el ? el.value : 'null'}
            </div>
            <div className="array-index">{i}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ArrayVisualizer;
