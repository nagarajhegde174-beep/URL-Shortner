import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import api from '../lib/axios'
import { useAuthStore } from '../store/authStore'
import { 
  Copy, 
  ExternalLink, 
  BarChart3, 
  Trash2, 
  ToggleLeft, 
  ToggleRight, 
  Plus, 
  Calendar, 
  AlertCircle, 
  Sparkles,
  ChevronLeft,
  ChevronRight
} from 'lucide-react'
import toast from 'react-hot-toast'

interface LinkResponse {
  id: number
  shortCode: string
  longUrl: string
  shortUrl: string
  isCustom: boolean
  isActive: boolean
  expiresAt: string | null
  createdAt: string
  updatedAt: string
  clickCount: number
}

interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  size: number
  number: number
}

interface DashboardStats {
  totalLinks: number
  activeLinks: number
  totalClicks: number
  isPro: boolean
  plan: string
  subscriptionExpiresAt: string | null
}

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user, updateUser } = useAuthStore()

  // Pagination state
  const [page, setPage] = useState(0)
  const size = 10

  // Create link form state
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [longUrl, setLongUrl] = useState('')
  const [customCode, setCustomCode] = useState('')
  const [expiresAt, setExpiresAt] = useState('')

  // Fetch Dashboard Stats
  const { data: stats, isLoading: statsLoading } = useQuery<DashboardStats>({
    queryKey: ['dashboardStats'],
    queryFn: async () => {
      const response = await api.get('/api/analytics/dashboard')
      // Update plan details in authStore if they changed
      if (response.data) {
        updateUser({
          plan: response.data.plan,
          isPro: response.data.isPro,
          subscriptionExpiresAt: response.data.subscriptionExpiresAt
        })
      }
      return response.data
    }
  })

  // Fetch Link list
  const { data: linksPage, isLoading: linksLoading } = useQuery<PageResponse<LinkResponse>>({
    queryKey: ['links', page],
    queryFn: async () => {
      const response = await api.get(`/api/links?page=${page}&size=${size}`)
      return response.data
    }
  })

  // Toggle Link status Mutation
  const toggleMutation = useMutation({
    mutationFn: async (id: number) => {
      const response = await api.patch(`/api/links/${id}/toggle`)
      return response.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['links'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
      toast.success('Link status updated')
    },
    onError: (error: any) => {
      const msg = error.response?.data?.message || 'Failed to update link status'
      toast.error(msg)
    }
  })

  // Delete Link Mutation
  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      await api.delete(`/api/links/${id}`)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['links'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
      toast.success('Link deleted successfully')
    },
    onError: (error: any) => {
      const msg = error.response?.data?.message || 'Failed to delete link'
      toast.error(msg)
    }
  })

  // Create Link Mutation
  const createMutation = useMutation({
    mutationFn: async (payload: { longUrl: string; customCode?: string; expiresAt?: string }) => {
      const response = await api.post('/api/links', payload)
      return response.data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['links'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
      toast.success('Short link created!')
      setShowCreateModal(false)
      // Reset form
      setLongUrl('')
      setCustomCode('')
      setExpiresAt('')
    },
    onError: (error: any) => {
      const msg = error.response?.data?.message || 'Failed to create short link'
      toast.error(msg)
    }
  })

  const isPro = stats?.isPro || user?.isPro || user?.plan === 'PRO'

  const handleCreateLink = (e: React.FormEvent) => {
    e.preventDefault()

    if (!longUrl) {
      toast.error('Long URL is required')
      return
    }

    // UX check: FREE user limit of 5 links
    if (!isPro && stats && stats.totalLinks >= 5) {
      toast.error('Free plan is limited to 5 links. Please upgrade to PRO for unlimited links!')
      navigate('/upgrade')
      return
    }

    const payload: any = { longUrl }
    if (customCode && isPro) {
      payload.customCode = customCode
    }
    if (expiresAt) {
      payload.expiresAt = new Date(expiresAt).toISOString()
    }

    createMutation.mutate(payload)
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
    toast.success('Copied link to clipboard!')
  }

  const formatDateTime = (isoString: string | null) => {
    if (!isoString) return '-'
    return new Date(isoString).toLocaleString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return (
    <div className="container-fluid py-4">
      {/* Upper header block */}
      <div className="d-flex flex-column flex-sm-row justify-content-between align-items-start align-items-sm-center gap-3 mb-4">
        <div>
          <h1 className="page-title mb-1">Link Management</h1>
          <p className="text-muted mb-0 small">Create short URLs, customize codes, and track dynamic analytics.</p>
        </div>
        <button 
          className="btn btn-primary-custom px-4 py-2 d-flex align-items-center gap-2 rounded-3 shadow-sm"
          onClick={() => setShowCreateModal(true)}
          disabled={createMutation.isPending}
        >
          <Plus size={18} />
          <span>Create Link</span>
        </button>
      </div>

      {/* Stats Cards Row */}
      <div className="row g-3 mb-4">
        {/* Total Links Card */}
        <div className="col-12 col-sm-6 col-lg-3">
          <div className="metric-card metric-stripe-skyblue">
            <div className="text-secondary small fw-semibold text-uppercase tracking-wider">Total Links</div>
            <div className="fs-2 fw-bold text-dark mt-1">
              {statsLoading ? '-' : stats?.totalLinks}
            </div>
            {!isPro && (
              <div className="progress mt-2" style={{ height: '6px' }}>
                <div 
                  className="progress-bar bg-info" 
                  role="progressbar" 
                  style={{ width: `${Math.min(((stats?.totalLinks || 0) / 5) * 100, 100)}%` }}
                  aria-valuenow={stats?.totalLinks || 0}
                  aria-valuemin={0}
                  aria-valuemax={5}
                ></div>
              </div>
            )}
            {!isPro && (
              <div className="small text-muted mt-1" style={{ fontSize: '0.75rem' }}>
                {stats?.totalLinks || 0} of 5 links used (FREE plan limit)
              </div>
            )}
          </div>
        </div>

        {/* Total Clicks Card */}
        <div className="col-12 col-sm-6 col-lg-3">
          <div className="metric-card metric-stripe-orange">
            <div className="text-secondary small fw-semibold text-uppercase tracking-wider">Total Clicks</div>
            <div className="fs-2 fw-bold text-dark mt-1">
              {statsLoading ? '-' : stats?.totalClicks}
            </div>
            <div className="small text-muted mt-2" style={{ fontSize: '0.75rem' }}>
              Dynamic link routing clicks tracked
            </div>
          </div>
        </div>

        {/* Active Links Card */}
        <div className="col-12 col-sm-6 col-lg-3">
          <div className="metric-card metric-stripe-green">
            <div className="text-secondary small fw-semibold text-uppercase tracking-wider">Active Links</div>
            <div className="fs-2 fw-bold text-dark mt-1">
              {statsLoading ? '-' : stats?.activeLinks}
            </div>
            <div className="small text-muted mt-2" style={{ fontSize: '0.75rem' }}>
              Redirect cache warm in Redis memory
            </div>
          </div>
        </div>

        {/* Plan status Card */}
        <div className="col-12 col-sm-6 col-lg-3">
          <div className="metric-card metric-stripe-indigo">
            <div className="text-secondary small fw-semibold text-uppercase tracking-wider">Plan Status</div>
            <div className="d-flex align-items-center gap-2 mt-1">
              <span className={`badge ${isPro ? 'bg-warning text-dark' : 'bg-secondary text-light'} fs-5 fw-bold px-3 py-1`}>
                {isPro ? 'PRO' : 'FREE'}
              </span>
            </div>
            {isPro && stats?.subscriptionExpiresAt && (
              <div className="small text-muted mt-2" style={{ fontSize: '0.75rem' }}>
                Expires: {new Date(stats.subscriptionExpiresAt).toLocaleDateString('en-IN')}
              </div>
            )}
            {!isPro && (
              <div className="small text-muted mt-2" style={{ fontSize: '0.75rem' }}>
                Single limits. Custom alias blocked.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Free User Promo Banner */}
      {!isPro && (
        <div className="alert border-0 theme-rose shadow-sm p-3 mb-4 rounded-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 animate-fade-in">
          <div className="d-flex align-items-center gap-2 text-dark">
            <Sparkles className="text-warning" size={24} />
            <div>
              <strong className="d-block">Upgrade to PRO plan for ₹499</strong>
              <span className="small text-secondary">Unlock custom alias links, unlimited generation, and granular click analytics dashboard.</span>
            </div>
          </div>
          <button 
            className="btn btn-warning fw-bold px-4 py-2 rounded-3 text-dark text-nowrap"
            onClick={() => navigate('/upgrade')}
          >
            Upgrade Plan Now
          </button>
        </div>
      )}

      {/* Links Table section */}
      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white py-3 border-0">
          <h5 className="mb-0 fw-bold text-dark">Shortened Links</h5>
        </div>
        <div className="table-responsive">
          <table className="table custom-table mb-0">
            <thead>
              <tr>
                <th scope="col">Short URL</th>
                <th scope="col" className="d-none d-lg-table-cell">Original URL</th>
                <th scope="col" className="text-center">Clicks</th>
                <th scope="col" className="text-center">Status</th>
                <th scope="col" className="d-none d-md-table-cell">Created At</th>
                <th scope="col" className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {linksLoading ? (
                <tr>
                  <td colSpan={6} className="text-center py-5">
                    <div className="spinner-border text-info" role="status">
                      <span className="visually-hidden">Loading...</span>
                    </div>
                  </td>
                </tr>
              ) : !linksPage || linksPage.content.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-5 text-muted">
                    <AlertCircle size={36} className="mb-2 text-secondary" />
                    <p className="mb-0 small fw-medium">No links created yet. Click "Create Link" to begin.</p>
                  </td>
                </tr>
              ) : (
                linksPage.content.map((link) => (
                  <tr key={link.id}>
                    <td>
                      <div className="d-flex align-items-center gap-2">
                        <a 
                          href={link.shortUrl} 
                          target="_blank" 
                          rel="noopener noreferrer" 
                          className="fw-semibold text-primary text-decoration-none hover-underline"
                        >
                          {link.shortCode}
                        </a>
                        <button 
                          className="btn btn-link p-0 text-muted hover-info" 
                          onClick={() => copyToClipboard(link.shortUrl)}
                          aria-label="Copy short link"
                        >
                          <Copy size={14} />
                        </button>
                        <a 
                          href={link.shortUrl} 
                          target="_blank" 
                          rel="noopener noreferrer" 
                          className="text-muted hover-info"
                          aria-label="Open short link"
                        >
                          <ExternalLink size={14} />
                        </a>
                      </div>
                      <div className="small text-muted d-lg-none text-truncate mt-1" style={{ maxWidth: '200px' }}>
                        {link.longUrl}
                      </div>
                    </td>
                    <td className="d-none d-lg-table-cell text-truncate" style={{ maxWidth: '300px' }}>
                      <a href={link.longUrl} target="_blank" rel="noopener noreferrer" className="text-decoration-none text-secondary">
                        {link.longUrl}
                      </a>
                    </td>
                    <td className="text-center fw-bold text-dark">{link.clickCount}</td>
                    <td className="text-center">
                      <button
                        className="btn p-0 border-0"
                        onClick={() => toggleMutation.mutate(link.id)}
                        disabled={toggleMutation.isPending}
                        aria-label={link.isActive ? "Deactivate link" : "Activate link"}
                      >
                        {link.isActive ? (
                          <ToggleRight className="text-success" size={28} />
                        ) : (
                          <ToggleLeft className="text-muted" size={28} />
                        )}
                      </button>
                    </td>
                    <td className="d-none d-md-table-cell text-muted small">
                      {formatDateTime(link.createdAt)}
                    </td>
                    <td className="text-end">
                      <div className="d-flex justify-content-end gap-2">
                        <button
                          className="btn btn-sm btn-outline-info"
                          onClick={() => navigate(`/dashboard/analytics/${link.id}`)}
                          aria-label="View analytics"
                        >
                          <BarChart3 size={15} />
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => {
                            if (window.confirm('Are you sure you want to delete this link?')) {
                              deleteMutation.mutate(link.id)
                            }
                          }}
                          disabled={deleteMutation.isPending}
                          aria-label="Delete link"
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination controls */}
        {linksPage && linksPage.totalPages > 1 && (
          <div className="card-footer bg-white border-0 py-3 d-flex justify-content-between align-items-center">
            <span className="small text-muted">
              Page {linksPage.number + 1} of {linksPage.totalPages}
            </span>
            <div className="d-flex gap-2">
              <button
                className="btn btn-sm btn-outline-secondary px-3 py-1 rounded"
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                <ChevronLeft size={16} />
              </button>
              <button
                className="btn btn-sm btn-outline-secondary px-3 py-1 rounded"
                onClick={() => setPage(p => Math.min(linksPage.totalPages - 1, p + 1))}
                disabled={page === linksPage.totalPages - 1}
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Modal - Create Link */}
      {showCreateModal && (
        <div className="modal show d-block" tabIndex={-1} style={{ backgroundColor: 'rgba(15, 23, 42, 0.6)', backdropFilter: 'blur(4px)' }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow rounded-3">
              <div className="modal-header border-bottom-0 pb-0">
                <h5 className="modal-title fw-bold text-dark">Shorten Long URL</h5>
                <button type="button" className="btn-close" onClick={() => setShowCreateModal(false)} aria-label="Close"></button>
              </div>
              <form onSubmit={handleCreateLink}>
                <div className="modal-body py-3">
                  {/* Long URL */}
                  <div className="mb-3">
                    <label htmlFor="longUrlInput" className="form-label small fw-semibold text-secondary">Destination URL</label>
                    <input
                      id="longUrlInput"
                      type="url"
                      className="form-control custom-input"
                      placeholder="https://example.com/very-long-url-path"
                      value={longUrl}
                      onChange={(e) => setLongUrl(e.target.value)}
                      disabled={createMutation.isPending}
                      required
                    />
                  </div>

                  {/* Custom short code (PRO only) */}
                  <div className="mb-3">
                    <div className="d-flex justify-content-between align-items-center mb-1">
                      <label htmlFor="customCodeInput" className="form-label mb-0 small fw-semibold text-secondary">Custom Code (Optional)</label>
                      {!isPro && (
                        <span className="badge bg-warning-subtle text-warning fw-bold small d-flex align-items-center gap-1" style={{ fontSize: '0.7rem' }}>
                          <Sparkles size={10} /> PRO ONLY
                        </span>
                      )}
                    </div>
                    <div className="input-group">
                      <span className="input-group-text bg-light text-muted small">ziplink/</span>
                      <input
                        id="customCodeInput"
                        type="text"
                        className="form-control custom-input"
                        placeholder={isPro ? "promo-2026" : "Upgrade to unlock"}
                        value={customCode}
                        onChange={(e) => setCustomCode(e.target.value)}
                        disabled={!isPro || createMutation.isPending}
                      />
                    </div>
                    {!isPro && (
                      <div className="form-text small text-muted">
                        Only PRO users can set custom brand names as alias codes.
                      </div>
                    )}
                  </div>

                  {/* Expiry Date */}
                  <div className="mb-3">
                    <label htmlFor="expiresAtInput" className="form-label small fw-semibold text-secondary">Expiry Date (Optional)</label>
                    <div className="input-group">
                      <span className="input-group-text bg-white text-muted">
                        <Calendar size={16} />
                      </span>
                      <input
                        id="expiresAtInput"
                        type="datetime-local"
                        className="form-control custom-input"
                        value={expiresAt}
                        onChange={(e) => setExpiresAt(e.target.value)}
                        disabled={createMutation.isPending}
                      />
                    </div>
                  </div>
                </div>

                <div className="modal-footer border-top-0 pt-0">
                  <button type="button" className="btn btn-light px-4" onClick={() => setShowCreateModal(false)}>Cancel</button>
                  <button 
                    type="submit" 
                    className="btn btn-primary-custom px-4 d-flex align-items-center gap-1"
                    disabled={createMutation.isPending}
                  >
                    {createMutation.isPending ? 'Shortening...' : 'Generate URL'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
