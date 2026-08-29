import React, { useState } from 'react'
import { Link, useNavigate, Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { Link2, Mail, Lock, Loader2 } from 'lucide-react'
import api from '../lib/axios'
import toast from 'react-hot-toast'

export const RegisterPage: React.FC = () => {
  const { isAuthenticated, setAuth } = useAuthStore()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)

  // Redirect if already authenticated
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!email || !password || !confirmPassword) {
      toast.error('Please fill in all fields')
      return
    }

    if (password.length < 6) {
      toast.error('Password must be at least 6 characters long')
      return
    }

    if (password !== confirmPassword) {
      toast.error('Passwords do not match')
      return
    }

    setLoading(true)
    try {
      const response = await api.post('/api/auth/register', { email, password })
      const { accessToken, user } = response.data
      
      setAuth(accessToken, user)
      toast.success('Account created successfully')
      navigate('/dashboard')
    } catch (error: any) {
      const errMsg = error.response?.data?.message || 'Registration failed. Please try again.'
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
            <p className="text-muted mt-2 small">Create a free account to shorten links</p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label htmlFor="registerEmailInput" className="form-label small fw-semibold text-secondary">Email Address</label>
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0 text-muted">
                  <Mail size={18} />
                </span>
                <input
                  id="registerEmailInput"
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

            <div className="mb-3">
              <label htmlFor="registerPasswordInput" className="form-label small fw-semibold text-secondary">Password</label>
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0 text-muted">
                  <Lock size={18} />
                </span>
                <input
                  id="registerPasswordInput"
                  type="password"
                  className="form-control border-start-0 custom-input"
                  placeholder="At least 6 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={loading}
                  required
                />
              </div>
            </div>

            <div className="mb-4">
              <label htmlFor="confirmPasswordInput" className="form-label small fw-semibold text-secondary">Confirm Password</label>
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0 text-muted">
                  <Lock size={18} />
                </span>
                <input
                  id="confirmPasswordInput"
                  type="password"
                  className="form-control border-start-0 custom-input"
                  placeholder="Repeat password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
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
                  <span>Creating Account...</span>
                </>
              ) : (
                <span>Register</span>
              )}
            </button>
          </form>

          {/* Redirect Login */}
          <div className="text-center mt-4">
            <span className="text-muted small">Already have an account? </span>
            <Link to="/login" className="small text-info fw-semibold text-decoration-none">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
