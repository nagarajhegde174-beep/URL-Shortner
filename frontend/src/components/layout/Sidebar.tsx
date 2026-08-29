import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { LayoutDashboard, Sparkles, LogOut, Link2, X } from 'lucide-react'
import api from '../../lib/axios'
import toast from 'react-hot-toast'

interface SidebarProps {
  isOpen: boolean
  onClose: () => void
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const { user, clearAuth } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try {
      await api.post('/api/auth/logout')
      clearAuth()
      toast.success('Logged out successfully')
      navigate('/login')
    } catch (error) {
      // In case of error, still clear local auth state
      clearAuth()
      navigate('/login')
    }
  }

  const isPro = user?.plan === 'PRO' || user?.isPro

  return (
    <div className={`sidebar ${isOpen ? 'show' : ''}`}>
      {/* Sidebar Header */}
      <div className="p-4 d-flex align-items-center justify-content-between">
        <div className="d-flex align-items-center gap-2">
          <Link2 className="text-info" size={28} />
          <span className="fs-4 fw-bold text-white tracking-wide">ZipLink</span>
        </div>
        <button 
          className="btn text-white d-md-none p-0" 
          onClick={onClose}
          aria-label="Close sidebar"
        >
          <X size={24} />
        </button>
      </div>

      {/* Navigation Links */}
      <div className="flex-grow-1 px-2 py-3">
        <NavLink 
          to="/dashboard" 
          end
          className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
          onClick={onClose}
        >
          <LayoutDashboard size={20} />
          <span>Dashboard</span>
        </NavLink>
        <NavLink 
          to="/upgrade" 
          className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
          onClick={onClose}
        >
          <Sparkles size={20} />
          <span>Upgrade to PRO</span>
        </NavLink>
      </div>

      {/* Upgrade Banner for FREE users */}
      {!isPro && (
        <div className="mx-3 my-3 p-3 rounded theme-rose border border-warning border-opacity-25 animate-fade-in">
          <h6 className="fw-bold text-dark mb-1 d-flex align-items-center gap-1">
            <Sparkles size={16} className="text-warning" />
            Go Premium
          </h6>
          <p className="text-muted small mb-2" style={{ fontSize: '0.8rem' }}>
            Unlock custom short codes, detailed analytics and unlimited link generation.
          </p>
          <button 
            className="btn btn-sm btn-primary-custom w-100 py-1"
            onClick={() => {
              onClose()
              navigate('/upgrade')
            }}
          >
            Upgrade Plan
          </button>
        </div>
      )}

      {/* User Information & Logout */}
      <div className="mt-auto p-3 border-top border-secondary border-opacity-20 bg-dark bg-opacity-25">
        <div className="d-flex align-items-center gap-2 mb-3">
          <div className="flex-grow-1 overflow-hidden">
            <div className="text-white text-truncate fw-medium small">{user?.email}</div>
            <div className="mt-1">
              {isPro ? (
                <span className="badge bg-warning text-dark fw-bold px-2 py-1" style={{ fontSize: '0.7rem' }}>PRO USER</span>
              ) : (
                <span className="badge bg-secondary text-light px-2 py-1" style={{ fontSize: '0.7rem' }}>FREE PLAN</span>
              )}
            </div>
          </div>
        </div>
        <button 
          className="btn btn-outline-danger btn-sm w-100 d-flex align-items-center justify-content-center gap-2 py-2"
          onClick={handleLogout}
        >
          <LogOut size={16} />
          <span>Logout</span>
        </button>
      </div>
    </div>
  )
}
