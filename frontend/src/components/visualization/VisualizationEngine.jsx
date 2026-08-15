import React from 'react';
import { useAnimation } from '../../context/AnimationContext';
import ArrayVisualizer from './components/ArrayVisualizer';
import StackVisualizer from './components/StackVisualizer';
import QueueVisualizer from './components/QueueVisualizer';
import LinkedListVisualizer from './components/LinkedListVisualizer';
import StringVisualizer from './components/StringVisualizer';
import '../../styles/visualization.css';

const VisualizationEngine = ({ className }) => {
  const { stepInfo } = useAnimation();

  if (!stepInfo) {
    return (
      <div className="visualization-container empty">
        <p>Waiting for execution trace...</p>
      </div>
    );
  }

  // Determine which visualizer to render based on metadata dataStructure priority or className heuristic
  let VisualizerComponent = null;
  const ds = stepInfo.metadata?.dataStructure;

  if (ds) {
    if (ds === 'STACK') {
      VisualizerComponent = StackVisualizer;
    } else if (ds === 'QUEUE') {
      VisualizerComponent = QueueVisualizer;
    } else if (ds === 'LINKED_LIST') {
      VisualizerComponent = LinkedListVisualizer;
    } else if (ds === 'STRING') {
      VisualizerComponent = StringVisualizer;
    } else if (ds === 'ARRAY') {
      VisualizerComponent = ArrayVisualizer;
    }
  }

  if (!VisualizerComponent) {
    if (className.toLowerCase().includes('array')) {
      VisualizerComponent = ArrayVisualizer;
    } else if (className.toLowerCase().includes('stack') || className.toLowerCase().includes('recursion')) {
      VisualizerComponent = StackVisualizer;
    } else if (className.toLowerCase().includes('queue')) {
      VisualizerComponent = QueueVisualizer;
    } else if (className.toLowerCase().includes('string') || className.toLowerCase().includes('char')) {
      VisualizerComponent = StringVisualizer;
    } else if (className.toLowerCase().includes('object') || className.toLowerCase().includes('linked')) {
      VisualizerComponent = LinkedListVisualizer;
    }
  }

  return (
    <div className="visualization-engine">
      <div className="visualization-header">
        <h3>Visualization Panel</h3>
        <div className="complexity-panel">
          {stepInfo.complexity && (
            <>
              <span className="complexity-badge time">Time: {stepInfo.complexity.time || 'O(1)'}</span>
              <span className="complexity-badge space">Space: {stepInfo.complexity.space || 'O(1)'}</span>
            </>
          )}
        </div>
      </div>
      <div className="visualization-content">
        {VisualizerComponent ? (
          <VisualizerComponent stepInfo={stepInfo} />
        ) : (
          <div className="visualization-placeholder">
            <p>No specific visualizer available for <strong>{className}</strong>.</p>
            <p>Select a supported data structure template.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default VisualizationEngine;
