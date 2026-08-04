import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-logo">
          <span>⚡</span> DSA Studio
        </div>
        <div className="header-user">
          <span>Welcome, <strong>{user?.username}</strong></span>
          <button onClick={handleLogout} className="logout-btn">Sign Out</button>
        </div>
      </header>
      <main className="dashboard-main">
        <div className="dashboard-hero">
          <h1>🚀 DSA Studio</h1>
          <p>Your complete Java learning and debugging platform.</p>
          <p className="coming-soon">Phase 2: Java Execution Engine — Coming Soon!</p>
        </div>
        <div className="dashboard-cards">
          <div className="dash-card">
            <span className="card-icon">🧑‍💻</span>
            <h3>Code Playground</h3>
            <p>Write & execute Java code step-by-step</p>
          </div>
          <div className="dash-card">
            <span className="card-icon">🔬</span>
            <h3>Debugger</h3>
            <p>Line-by-line debugging with variable inspection</p>
          </div>
          <div className="dash-card">
            <span className="card-icon">🧠</span>
            <h3>DSA Modules</h3>
            <p>Arrays, Trees, Graphs, DP and more</p>
          </div>
          <div className="dash-card">
            <span className="card-icon">📊</span>
            <h3>Practice</h3>
            <p>Solve problems, track progress</p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardPage;
