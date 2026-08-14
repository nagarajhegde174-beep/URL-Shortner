import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import DebuggerPage from './pages/DebuggerPage';
import ArraysPage from './pages/ArraysPage';
import StringsPage from './pages/StringsPage';
import LinkedListPage from './pages/LinkedListPage';
import './styles/auth.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/debugger"
            element={
              <ProtectedRoute>
                <DebuggerPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/arrays"
            element={
              <ProtectedRoute>
                <ArraysPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/strings"
            element={
              <ProtectedRoute>
                <StringsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/linked-list"
            element={
              <ProtectedRoute>
                <LinkedListPage />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
