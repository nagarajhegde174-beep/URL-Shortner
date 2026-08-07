import React, { createContext, useState, useRef, useEffect, useContext } from 'react';
import api from '../services/api';

const AnimationContext = createContext();

export const AnimationProvider = ({ children }) => {
  const [stepInfo, setStepInfo] = useState(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [speedMs, setSpeedMs] = useState(1200);
  const [compileError, setCompileError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  
  const playIntervalRef = useRef(null);

  // Playback Control
  const stopPlayback = () => {
    setIsPlaying(false);
    if (playIntervalRef.current) {
      clearInterval(playIntervalRef.current);
      playIntervalRef.current = null;
    }
  };

  const togglePlayback = () => {
    if (isPlaying) {
      stopPlayback();
    } else {
      setIsPlaying(true);
      playIntervalRef.current = setInterval(() => {
        handleStepForward();
      }, speedMs);
    }
  };

  // Update interval if speed changes while playing
  useEffect(() => {
    if (isPlaying) {
      stopPlayback();
      setIsPlaying(true);
      playIntervalRef.current = setInterval(() => {
        handleStepForward();
      }, speedMs);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [speedMs]);

  // Clean up on unmount
  useEffect(() => {
    return () => stopPlayback();
  }, []);

  // API calls
  const handleCompileAndRun = async (className, code, input = '') => {
    setIsLoading(true);
    setCompileError(null);
    setStepInfo(null);
    stopPlayback();
    
    try {
      const response = await api.post('/execution/run', { className, code, input });
      const data = response.data.data;
      if (data.exceptionName === 'CompilationException') {
        setCompileError(data.exceptionMessage);
      } else {
        setStepInfo(data);
      }
    } catch (err) {
      console.error(err);
      setCompileError(err.response?.data?.message || 'Error occurred during compile & execution.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStepForward = async () => {
    try {
      const response = await api.post('/execution/step?direction=next');
      const data = response.data.data;
      if (data) {
        setStepInfo(data);
      } else {
        stopPlayback(); // End of trace
      }
    } catch (err) {
      console.error(err);
      stopPlayback();
    }
  };

  const handleStepBackward = async () => {
    try {
      const response = await api.post('/execution/step?direction=prev');
      const data = response.data.data;
      if (data) {
        setStepInfo(data);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleRestart = async () => {
    try {
      stopPlayback();
      const response = await api.post('/execution/reset');
      const data = response.data.data;
      if (data) {
        setStepInfo(data);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const resetDebuggerState = () => {
    setStepInfo(null);
    setCompileError(null);
    stopPlayback();
  };

  return (
    <AnimationContext.Provider
      value={{
        stepInfo,
        isPlaying,
        speedMs,
        setSpeedMs,
        compileError,
        isLoading,
        togglePlayback,
        stopPlayback,
        handleCompileAndRun,
        handleStepForward,
        handleStepBackward,
        handleRestart,
        resetDebuggerState
      }}
    >
      {children}
    </AnimationContext.Provider>
  );
};

export const useAnimation = () => useContext(AnimationContext);
