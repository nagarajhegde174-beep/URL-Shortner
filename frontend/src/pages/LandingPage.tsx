import React from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { Link2, ArrowRight, Zap, BarChart3, Sparkles } from 'lucide-react'

export const LandingPage: React.FC = () => {
  const { isAuthenticated } = useAuthStore()

  // Redirect to dashboard if already logged in
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  return (
    <div className="bg-white min-vh-100 d-flex flex-column">
      {/* Navigation */}
      <header className="navbar navbar-expand-lg navbar-light border-bottom py-3">
        <div className="container">
          <Link to="/" className="navbar-brand d-flex align-items-center gap-2 fw-bold fs-4">
            <Link2 className="text-info" size={28} />
            <span>ZipLink</span>
          </Link>
          <div className="d-flex align-items-center gap-3">
            <Link to="/login" className="btn btn-link text-decoration-none text-secondary fw-semibold">Sign In</Link>
            <Link to="/register" className="btn btn-primary-custom px-4 py-2 rounded-pill">Get Started</Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-grow-1">
        <section className="py-5 text-center bg-light border-bottom">
          <div className="container py-5">
            <div className="row justify-content-center">
              <div className="col-lg-8 col-md-10">
                <span className="badge bg-info-subtle text-info px-3 py-2 rounded-pill fw-semibold mb-3">
                  <Sparkles size={14} className="me-1 align-middle" />
                  Experience lightning fast URL shortening
                </span>
                <h1 className="display-4 fw-extrabold text-dark mb-4">
                  Shorten, Track, and Optimize Your Links
                </h1>
                <p className="lead text-muted mb-5">
                  ZipLink is a complete link management platform. Generate short URLs, track performance with real-time analytics, and create custom branded alias codes.
                </p>
                <div className="d-flex justify-content-center gap-3 flex-column flex-sm-row">
                  <Link to="/register" className="btn btn-primary-custom btn-lg px-5 py-3 rounded-pill d-flex align-items-center justify-content-center gap-2">
                    Get Started Free
                    <ArrowRight size={20} />
                  </Link>
                  <Link to="/login" className="btn btn-outline-secondary btn-lg px-5 py-3 rounded-pill">
                    View Pricing
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section className="py-5">
          <div className="container py-5">
            <h2 className="text-center fw-bold mb-5 fs-2">Everything you need to share smarter</h2>
            <div className="row g-4 justify-content-center">
              <div className="col-lg-4 col-md-6">
                <div className="card h-100 p-4 border-0 shadow-sm rounded-3">
                  <div className="bg-info-subtle p-3 rounded-circle d-inline-flex mb-3 align-self-start">
                    <Zap className="text-info" size={24} />
                  </div>
                  <h5 className="card-title fw-bold">Instant Shortening</h5>
                  <p className="card-text text-muted">
                    Generate secure, short URLs instantly. Your links are cached in Redis to guarantee single-digit millisecond redirection latency.
                  </p>
                </div>
              </div>
              <div className="col-lg-4 col-md-6">
                <div className="card h-100 p-4 border-0 shadow-sm rounded-3">
                  <div className="bg-success-subtle p-3 rounded-circle d-inline-flex mb-3 align-self-start">
                    <BarChart3 className="text-success" size={24} />
                  </div>
                  <h5 className="card-title fw-bold">Real-time Analytics</h5>
                  <p className="card-text text-muted">
                    Track link performance including device types, operating systems, browsers, referrers, and click histories over time.
                  </p>
                </div>
              </div>
              <div className="col-lg-4 col-md-6">
                <div className="card h-100 p-4 border-0 shadow-sm rounded-3">
                  <div className="bg-warning-subtle p-3 rounded-circle d-inline-flex mb-3 align-self-start">
                    <Sparkles className="text-warning" size={24} />
                  </div>
                  <h5 className="card-title fw-bold">Branded Short Codes</h5>
                  <p className="card-text text-muted">
                    Upgrade to PRO to customize your short code aliases. Match your brand name and boost click-through rates by up to 34%.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-dark text-light py-4 border-top border-secondary">
        <div className="container text-center">
          <div className="d-flex align-items-center justify-content-center gap-2 mb-2">
            <Link2 className="text-info" size={20} />
            <span className="fw-bold">ZipLink</span>
          </div>
          <p className="small text-muted mb-0">
            &copy; {new Date().getFullYear()} ZipLink Inc. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  )
}
