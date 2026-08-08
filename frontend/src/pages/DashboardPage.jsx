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
          <p className="coming-soon">Arrays Module Live — Visualize, Practice &amp; Quiz!</p>
        </div>
        <div className="dashboard-cards">
          <div className="dash-card" onClick={() => navigate('/debugger')}>
            <span className="card-icon">🧑‍💻</span>
            <h3>Code Playground</h3>
            <p>Write &amp; execute Java code step-by-step</p>
          </div>
          <div className="dash-card" onClick={() => navigate('/debugger')}>
            <span className="card-icon">🔬</span>
            <h3>Debugger</h3>
            <p>Line-by-line debugging with variable inspection</p>
          </div>
          <div className="dash-card" onClick={() => navigate('/arrays')}>
            <span className="card-icon">🧠</span>
            <h3>DSA Modules</h3>
            <p>Arrays — Visualize 16 operations &amp; patterns</p>
          </div>
          <div className="dash-card" onClick={() => navigate('/arrays')}>
            <span className="card-icon">📊</span>
            <h3>Practice</h3>
            <p>Solve 6 array problems, track your progress</p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardPage;
