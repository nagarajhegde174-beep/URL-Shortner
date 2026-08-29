import React, { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import { useAuthStore } from './store/authStore'
import api from './lib/axios'
import { PrivateRoute } from './components/layout/PrivateRoute'
import { Sidebar } from './components/layout/Sidebar'
import { LandingPage } from './pages/LandingPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { DashboardPage } from './pages/DashboardPage'
import { LinkAnalyticsPage } from './pages/LinkAnalyticsPage'
import { UpgradePage } from './pages/UpgradePage'
import { Link2, Menu } from 'lucide-react'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: false,
    },
  },
})

// Layout wrapper for authenticated pages
const PrivateLayout: React.FC = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="app-layout">
      {/* Mobile Navbar Header */}
      <header className="mobile-navbar d-md-none">
        <div className="d-flex align-items-center gap-2">
          <Link2 className="text-info" size={24} />
          <span className="fw-bold text-white fs-5">ZipLink</span>
        </div>
        <button 
          className="btn text-white p-0" 
          onClick={() => setSidebarOpen(true)}
          aria-label="Open sidebar"
        >
          <Menu size={24} />
        </button>
      </header>

      {/* Sidebar Navigation */}
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      {/* Main Page Content */}
      <main className="main-content w-100">
        <Outlet />
      </main>
    </div>
  )
}

export const App: React.FC = () => {
  const { setAuth, clearAuth, setIsLoading, isLoading } = useAuthStore()

  useEffect(() => {
    // Attempt silent refresh on app load
    const checkSession = async () => {
      try {
        const response = await api.post('/api/auth/refresh')
        const { accessToken, user } = response.data
        setAuth(accessToken, user)
      } catch (err) {
        clearAuth()
      } finally {
        setIsLoading(false)
      }
    }
    checkSession()
  }, [setAuth, clearAuth, setIsLoading])

  if (isLoading) {
    return (
      <div className="d-flex justify-content-center align-items-center min-vh-100 bg-light">
        <div className="spinner-border text-info" role="status" style={{ width: '3rem', height: '3rem' }}>
          <span className="visually-hidden">Loading session...</span>
        </div>
      </div>
    )
  }

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected Routes */}
          <Route element={<PrivateRoute><PrivateLayout /></PrivateRoute>}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/dashboard/analytics/:id" element={<LinkAnalyticsPage />} />
            <Route path="/upgrade" element={<UpgradePage />} />
          </Route>

          {/* Fallback redirect */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      
      {/* Toast popup manager */}
      <Toaster position="top-right" toastOptions={{ duration: 4000 }} />
    </QueryClientProvider>
  )
}

export default App
