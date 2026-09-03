import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Link2, Mail, Lock, KeyRound, ArrowLeft, Loader2, CheckCircle2 } from 'lucide-react'
import api from '../lib/axios'
import toast from 'react-hot-toast'

export const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate()

  const [step, setStep] = useState<1 | 2>(1)
  const [email, setEmail] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [generatedToken, setGeneratedToken] = useState<string | null>(null)

  // Step 1: Handle Request Token
  const handleRequestToken = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!email) {
      toast.error('Please enter your email address')
      return
    }

    setLoading(true)
    try {
      const response = await api.post('/api/auth/forgot-password', { email })
      const { message, resetToken: token } = response.data

      toast.success(message || 'Reset token generated successfully')
      if (token) {
        setGeneratedToken(token)
        setResetToken(token)
      }
      setStep(2)
    } catch (error: any) {
      const errMsg = error.response?.data?.message || 'Failed to request password reset token'
      toast.error(errMsg)
    } finally {
      setLoading(false)
    }
  }

  // Step 2: Handle Reset Password
  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!resetToken || !newPassword) {
      toast.error('Please fill in all fields')
      return
    }

    if (newPassword.length < 6) {
      toast.error('New password must be at least 6 characters long')
      return
    }

    if (newPassword !== confirmPassword) {
      toast.error('Passwords do not match')
      return
    }

    setLoading(true)
    try {
      const response = await api.post('/api/auth/reset-password', {
        token: resetToken,
        newPassword
      })

      toast.success(response.data.message || 'Password reset successfully!')
      navigate('/login')
    } catch (error: any) {
      const errMsg = error.response?.data?.message || 'Failed to reset password. Check your reset token.'
      toast.error(errMsg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light px-3 py-5">
      <div className="card shadow border-0 rounded-3 w-100" style={{ maxWidth: '440px' }}>
        <div className="card-body p-4 p-sm-5">
          {/* Logo & Header */}
          <div className="text-center mb-4">
            <Link to="/" className="d-inline-flex align-items-center gap-2 fw-bold text-decoration-none fs-3 text-dark">
              <Link2 className="text-info" size={28} />
              <span>ZipLink</span>
            </Link>
            <h5 className="fw-bold text-dark mt-3 mb-1">
              {step === 1 ? 'Reset Your Password' : 'Enter New Password'}
            </h5>
            <p className="text-muted small">
              {step === 1
                ? 'Enter your registered email address to receive a 6-digit password reset token.'
                : 'Enter your 6-digit reset token and choose a new secure password.'}
            </p>
          </div>

          {/* Generated Token Banner (Demo Helper) */}
          {generatedToken && step === 2 && (
            <div className="alert alert-info border-0 p-3 mb-4 rounded-3 small animate-fade-in">
              <div className="d-flex align-items-center gap-2 mb-1 fw-bold text-dark">
                <CheckCircle2 size={16} className="text-info" />
                <span>Reset Token Issued</span>
              </div>
              <p className="mb-1 text-secondary">
                Your reset token is: <code className="fw-bold text-primary fs-6 ms-1">{generatedToken}</code>
              </p>
              <span className="text-muted" style={{ fontSize: '0.75rem' }}>Valid for 15 minutes. Pre-filled below for convenience.</span>
            </div>
          )}

          {/* Form Step 1: Request Reset Token */}
          {step === 1 && (
            <form onSubmit={handleRequestToken}>
              <div className="mb-4">
                <label htmlFor="resetEmailInput" className="form-label small fw-semibold text-secondary">Email Address</label>
                <div className="input-group">
                  <span className="input-group-text bg-white border-end-0 text-muted">
                    <Mail size={18} />
                  </span>
                  <input
                    id="resetEmailInput"
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

              <button
                type="submit"
                className="btn btn-primary-custom w-100 py-2 d-flex align-items-center justify-content-center gap-2 mb-3"
                disabled={loading}
              >
                {loading ? (
                  <>
                    <Loader2 className="spinner-border spinner-border-sm border-0" size={16} style={{ animation: 'spin 1s linear infinite' }} />
                    <span>Sending Token...</span>
                  </>
                ) : (
                  <span>Send Reset Token</span>
                )}
              </button>
            </form>
          )}

          {/* Form Step 2: Set New Password */}
          {step === 2 && (
            <form onSubmit={handleResetPassword}>
              <div className="mb-3">
                <label htmlFor="tokenInput" className="form-label small fw-semibold text-secondary">6-Digit Reset Token</label>
                <div className="input-group">
                  <span className="input-group-text bg-white border-end-0 text-muted">
                    <KeyRound size={18} />
                  </span>
                  <input
                    id="tokenInput"
                    type="text"
                    className="form-control border-start-0 custom-input fw-semibold tracking-wider"
                    placeholder="123456"
                    value={resetToken}
                    onChange={(e) => setResetToken(e.target.value)}
                    disabled={loading}
                    required
                  />
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="newPasswordInput" className="form-label small fw-semibold text-secondary">New Password</label>
                <div className="input-group">
                  <span className="input-group-text bg-white border-end-0 text-muted">
                    <Lock size={18} />
                  </span>
                  <input
                    id="newPasswordInput"
                    type="password"
                    className="form-control border-start-0 custom-input"
                    placeholder="At least 6 characters"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    disabled={loading}
                    required
                  />
                </div>
              </div>

              <div className="mb-4">
                <label htmlFor="confirmPasswordInput" className="form-label small fw-semibold text-secondary">Confirm New Password</label>
                <div className="input-group">
                  <span className="input-group-text bg-white border-end-0 text-muted">
                    <Lock size={18} />
                  </span>
                  <input
                    id="confirmPasswordInput"
                    type="password"
                    className="form-control border-start-0 custom-input"
                    placeholder="Repeat new password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    disabled={loading}
                    required
                  />
                </div>
              </div>

              <button
                type="submit"
                className="btn btn-primary-custom w-100 py-2 d-flex align-items-center justify-content-center gap-2 mb-3"
                disabled={loading}
              >
                {loading ? (
                  <>
                    <Loader2 className="spinner-border spinner-border-sm border-0" size={16} style={{ animation: 'spin 1s linear infinite' }} />
                    <span>Resetting Password...</span>
                  </>
                ) : (
                  <span>Update & Save Password</span>
                )}
              </button>

              <button
                type="button"
                className="btn btn-link w-100 text-muted text-decoration-none small"
                onClick={() => setStep(1)}
                disabled={loading}
              >
                Change Email Address
              </button>
            </form>
          )}

          {/* Return to Login */}
          <div className="text-center mt-3 pt-3 border-top">
            <Link to="/login" className="small text-secondary text-decoration-none d-inline-flex align-items-center gap-1">
              <ArrowLeft size={14} />
              <span>Back to Sign In</span>
            </Link>
          </div>

        </div>
      </div>
    </div>
  )
}
