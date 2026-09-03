import React, { useState } from 'react'
import { Link, useNavigate, Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { Link2, Mail, Lock, Loader2 } from 'lucide-react'
import api from '../lib/axios'
import toast from 'react-hot-toast'

export const LoginPage: React.FC = () => {
  const { isAuthenticated, setAuth } = useAuthStore()
  const navigate = useNavigate()
  
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  // Redirect if already authenticated
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!email || !password) {
      toast.error('Please fill in all fields')
      return
    }

    setLoading(true)
    try {
      const response = await api.post('/api/auth/login', { email, password })
      const { accessToken, user } = response.data
      
      // Update store
      setAuth(accessToken, user)
      
      toast.success('Successfully logged in')
      navigate('/dashboard')
    } catch (error: any) {
      const errMsg = error.response?.data?.message || 'Login failed. Please check your credentials.'
      toast.error(errMsg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light px-3">
      <div className="card shadow border-0 rounded-3 w-100" style={{ maxWidth: '420px' }}>
        <div className="card-body p-4 p-sm-5">
          {/* Logo */}
          <div className="text-center mb-4">
            <Link to="/" className="d-inline-flex align-items-center gap-2 fw-bold text-decoration-none fs-3 text-dark">
              <Link2 className="text-info" size={28} />
              <span>ZipLink</span>
            </Link>
            <p className="text-muted mt-2 small">Sign in to your ZipLink account</p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label htmlFor="emailInput" className="form-label small fw-semibold text-secondary">Email Address</label>
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0 text-muted">
                  <Mail size={18} />
                </span>
                <input
                  id="emailInput"
                  type="email"
                  className="form-control border-start-0 custom-input"
                  placeholder="name@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={loading}
                  required
                />
              </div>
            </div>

            <div className="mb-4">
              <div className="d-flex justify-content-between align-items-center mb-1">
                <label htmlFor="passwordInput" className="form-label mb-0 small fw-semibold text-secondary">Password</label>
                <Link to="/forgot-password" className="small text-info fw-semibold text-decoration-none">
                  Forgot Password?
                </Link>
              </div>
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0 text-muted">
                  <Lock size={18} />
                </span>
                <input
                  id="passwordInput"
                  type="password"
                  className="form-control border-start-0 custom-input"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={loading}
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary-custom w-100 py-2 d-flex align-items-center justify-content-center gap-2"
              disabled={loading}
            >
              {loading ? (
                <>
                  <Loader2 className="spinner-border spinner-border-sm border-0" size={16} style={{ animation: 'spin 1s linear infinite' }} />
                  <span>Logging in...</span>
                </>
              ) : (
                <span>Sign In</span>
              )}
            </button>
          </form>

          {/* Redirect Register */}
          <div className="text-center mt-4">
            <span className="text-muted small">Don't have an account? </span>
            <Link to="/register" className="small text-info fw-semibold text-decoration-none">Sign up</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
