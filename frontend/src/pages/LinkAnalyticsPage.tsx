import React from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import api from '../lib/axios'
import { ArrowLeft, BarChart3, Globe, Smartphone, Monitor } from 'lucide-react'
import { Line, Doughnut, Bar } from 'react-chartjs-2'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

interface TimeSeriesPoint {
  date: string
  clicks: number
}

interface BreakdownItem {
  label: string
  count: number
}

interface LinkAnalyticsResponse {
  linkId: number
  shortCode: string
  shortUrl: string
  longUrl: string
  totalClicks: number
  clicksOverTime: TimeSeriesPoint[]
  deviceBreakdown: BreakdownItem[]
  browserBreakdown: BreakdownItem[]
  osBreakdown: BreakdownItem[]
  topReferrers: BreakdownItem[]
}

export const LinkAnalyticsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: analytics, isLoading, error } = useQuery<LinkAnalyticsResponse>({
    queryKey: ['linkAnalytics', id],
    queryFn: async () => {
      const response = await api.get(`/api/analytics/links/${id}`)
      return response.data
    },
    enabled: !!id
  })

  if (isLoading) {
    return (
      <div className="d-flex justify-content-center align-items-center min-vh-100 bg-light">
        <div className="spinner-border text-info" role="status">
          <span className="visually-hidden">Loading Analytics...</span>
        </div>
      </div>
    )
  }

  if (error || !analytics) {
    return (
      <div className="container py-5 text-center">
        <div className="alert alert-danger shadow-sm py-4">
          <h4 className="fw-bold mb-2">Error Loading Analytics</h4>
          <p className="text-secondary small mb-3">
            Unable to fetch data for this link. You may need PRO plan features or the link does not exist.
          </p>
          <button className="btn btn-outline-secondary btn-sm" onClick={() => navigate('/dashboard')}>
            Back to Dashboard
          </button>
        </div>
      </div>
    )
  }

  // ──────────────────────────────────────────────────────────────────────────
  // Prepare Chart Configurations
  // ──────────────────────────────────────────────────────────────────────────

  // Colors
  const skyBlue = '#0ea5e9'
  const green = '#10b981'
  const indigo = '#6366f1'
  const amber = '#f59e0b'
  
  const chartColors = [skyBlue, green, indigo, amber, '#ec4899', '#8b5cf6']

  // 1. Line Chart: Clicks over Time
  const clicksOverTimeData = {
    labels: analytics.clicksOverTime.map(p => p.date),
    datasets: [
      {
        label: 'Clicks',
        data: analytics.clicksOverTime.map(p => p.clicks),
        borderColor: skyBlue,
        backgroundColor: 'rgba(14, 165, 233, 0.1)',
        tension: 0.3,
        fill: true,
        pointBackgroundColor: skyBlue,
        pointHoverRadius: 6
      }
    ]
  }

  const clicksOverTimeOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { precision: 0 }
      }
    }
  }

  // 2. Doughnut Chart: Device Breakdown
  const deviceData = {
    labels: analytics.deviceBreakdown.map(d => d.label || 'Unknown'),
    datasets: [
      {
        data: analytics.deviceBreakdown.map(d => d.count),
        backgroundColor: chartColors.slice(0, analytics.deviceBreakdown.length),
        borderWidth: 1
      }
    ]
  }

  const doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: { boxWidth: 12, padding: 15 }
      }
    }
  }

  // 3. Bar Chart: Browser Breakdown
  const browserData = {
    labels: analytics.browserBreakdown.map(b => b.label || 'Unknown'),
    datasets: [
      {
        label: 'Clicks',
        data: analytics.browserBreakdown.map(b => b.count),
        backgroundColor: green,
        borderRadius: 4
      }
    ]
  }

  const barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: { beginAtZero: true, ticks: { precision: 0 } }
    }
  }

  // 4. Doughnut Chart: OS Breakdown
  const osData = {
    labels: analytics.osBreakdown.map(o => o.label || 'Unknown'),
    datasets: [
      {
        data: analytics.osBreakdown.map(o => o.count),
        backgroundColor: chartColors.slice(2, 2 + analytics.osBreakdown.length),
        borderWidth: 1
      }
    ]
  }

  return (
    <div className="container-fluid py-4">
      {/* Back button & Header */}
      <div className="mb-4">
        <button 
          className="btn btn-light d-inline-flex align-items-center gap-2 border shadow-sm mb-3"
          onClick={() => navigate('/dashboard')}
        >
          <ArrowLeft size={16} />
          <span>Back to Dashboard</span>
        </button>
        <div className="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3">
          <div>
            <h1 className="page-title mb-1">Link Analytics</h1>
            <p className="text-secondary small mb-0">
              Short Code: <strong className="text-primary">{analytics.shortCode}</strong>
            </p>
          </div>
          <div className="bg-white px-3 py-2 border rounded shadow-sm d-flex align-items-center gap-2">
            <BarChart3 className="text-info" size={20} />
            <div>
              <span className="small text-secondary d-block lh-1">Total Clicks</span>
              <strong className="fs-5 text-dark">{analytics.totalClicks}</strong>
            </div>
          </div>
        </div>
      </div>

      {/* URL information panel */}
      <div className="card saas-card border-0 mb-4 shadow-sm">
        <div className="row g-3">
          <div className="col-12 col-md-6">
            <span className="small text-secondary fw-semibold text-uppercase tracking-wider">Short URL</span>
            <div className="mt-1 d-flex align-items-center gap-2">
              <a href={analytics.shortUrl} target="_blank" rel="noopener noreferrer" className="fw-semibold text-info text-decoration-none">
                {analytics.shortUrl}
              </a>
            </div>
          </div>
          <div className="col-12 col-md-6">
            <span className="small text-secondary fw-semibold text-uppercase tracking-wider">Original Long URL</span>
            <div className="mt-1 text-truncate" style={{ maxWidth: '100%' }}>
              <a href={analytics.longUrl} target="_blank" rel="noopener noreferrer" className="text-secondary text-decoration-none">
                {analytics.longUrl}
              </a>
            </div>
          </div>
        </div>
      </div>

      {/* Line Chart: Clicks over time */}
      <div className="card saas-card border-0 mb-4 shadow-sm">
        <h5 className="fw-bold text-dark mb-3">Clicks Over Time</h5>
        <div style={{ height: '300px' }}>
          <Line data={clicksOverTimeData} options={clicksOverTimeOptions} />
        </div>
      </div>

      {/* Grid of details charts */}
      <div className="row g-4 mb-4">
        {/* Device Breakdown */}
        <div className="col-12 col-md-6 col-lg-4">
          <div className="card saas-card border-0 h-100 shadow-sm">
            <h5 className="fw-bold text-dark mb-3 d-flex align-items-center gap-2">
              <Smartphone size={18} className="text-secondary" />
              <span>Devices</span>
            </h5>
            <div style={{ height: '220px' }} className="position-relative">
              {analytics.deviceBreakdown.length > 0 ? (
                <Doughnut data={deviceData} options={doughnutOptions} />
              ) : (
                <div className="position-absolute top-50 start-50 translate-middle text-muted small">No device data</div>
              )}
            </div>
          </div>
        </div>

        {/* Browser Breakdown */}
        <div className="col-12 col-md-6 col-lg-4">
          <div className="card saas-card border-0 h-100 shadow-sm">
            <h5 className="fw-bold text-dark mb-3 d-flex align-items-center gap-2">
              <Globe size={18} className="text-secondary" />
              <span>Browsers</span>
            </h5>
            <div style={{ height: '220px' }} className="position-relative">
              {analytics.browserBreakdown.length > 0 ? (
                <Bar data={browserData} options={barOptions} />
              ) : (
                <div className="position-absolute top-50 start-50 translate-middle text-muted small">No browser data</div>
              )}
            </div>
          </div>
        </div>

        {/* OS Breakdown */}
        <div className="col-12 col-md-6 col-lg-4">
          <div className="card saas-card border-0 h-100 shadow-sm">
            <h5 className="fw-bold text-dark mb-3 d-flex align-items-center gap-2">
              <Monitor size={18} className="text-secondary" />
              <span>Operating Systems</span>
            </h5>
            <div style={{ height: '220px' }} className="position-relative">
              {analytics.osBreakdown.length > 0 ? (
                <Doughnut data={osData} options={doughnutOptions} />
              ) : (
                <div className="position-absolute top-50 start-50 translate-middle text-muted small">No OS data</div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Referrer Table section */}
      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white py-3 border-0">
          <h5 className="mb-0 fw-bold text-dark">Top Referrers</h5>
        </div>
        <div className="table-responsive">
          <table className="table custom-table mb-0">
            <thead>
              <tr>
                <th scope="col">Referrer Source</th>
                <th scope="col" className="text-end">Clicks</th>
              </tr>
            </thead>
            <tbody>
              {analytics.topReferrers.length === 0 ? (
                <tr>
                  <td colSpan={2} className="text-center py-4 text-muted small">
                    No referrer data tracked yet.
                  </td>
                </tr>
              ) : (
                analytics.topReferrers.map((item, index) => (
                  <tr key={index}>
                    <td className="fw-medium text-secondary">
                      {item.label || 'Direct / None'}
                    </td>
                    <td className="text-end fw-bold text-dark">{item.count}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
