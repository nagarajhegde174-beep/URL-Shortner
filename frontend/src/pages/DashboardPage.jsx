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
          <p className="coming-soon">Arrays, Strings, Linked Lists, Stacks, and Queues Live — Visualize, Practice &amp; Quiz!</p>
        </div>
        <div className="dashboard-cards">
          <div className="dash-card" onClick={() => navigate('/debugger')}>
            <span className="card-icon">🧑‍💻</span>
            <h3>Debugger Playground</h3>
            <p>Write &amp; trace Java code step-by-step</p>
          </div>
          <div className="dash-card" onClick={() => navigate('/arrays')}>
            <span className="card-icon">🧠</span>
            <h3>Arrays Module</h3>
            <p>16 operations &amp; patterns visualization</p>
          </div>
          <div className="dash-card" onClick={() => navigate('/strings')}>
            <span className="card-icon">🔤</span>
            <h3>Strings Module</h3>
            <p>Naive/KMP matching, Palindromes, Anagrams</p>
          </div>
          <div className="dash-card" onClick={() => navigate('/linked-list')}>
            <span className="card-icon">🔗</span>
            <h3>Linked Lists</h3>
            <p>Singly, Doubly, and Circular list structures</p>
          </div>
          <div className="dash-card" onClick={() => navigate('/stack')}>
            <span className="card-icon">🥞</span>
            <h3>Stacks Module</h3>
            <p>LIFO structures, NGE, Balanced Parentheses</p>
          </div>
          <div className="dash-card" onClick={() => navigate('/queue')}>
            <span className="card-icon">👥</span>
            <h3>Queues Module</h3>
            <p>FIFO queues, Circular Queue, PriorityQueue</p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default DashboardPage;
