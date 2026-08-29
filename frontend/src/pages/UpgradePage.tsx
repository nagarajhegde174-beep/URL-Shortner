import React, { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../store/authStore'
import api from '../lib/axios'
import { CheckCircle2, Sparkles, Shield, Zap, CreditCard, Loader2 } from 'lucide-react'
import toast from 'react-hot-toast'

export const UpgradePage: React.FC = () => {
  const { user, updateUser } = useAuthStore()
  const queryClient = useQueryClient()
  const [loading, setLoading] = useState(false)

  const isPro = user?.plan === 'PRO' || user?.isPro

  const loadRazorpayScript = () => {
    return new Promise((resolve) => {
      // Check if already loaded
      if ((window as any).Razorpay) {
        resolve(true)
        return
      }
      const script = document.createElement('script')
      script.src = 'https://checkout.razorpay.com/v1/checkout.js'
      script.onload = () => resolve(true)
      script.onerror = () => resolve(false)
      document.body.appendChild(script)
    })
  }

  const handleUpgrade = async () => {
    setLoading(true)
    try {
      // Step 1: Load script
      const scriptLoaded = await loadRazorpayScript()
      if (!scriptLoaded) {
        toast.error('Failed to load payment checkout SDK. Check internet connection.')
        setLoading(false)
        return
      }

      // Step 2: Create payment order on backend
      const orderResponse = await api.post('/api/payments/orders', { plan: 'PRO' })
      const { orderId, amount, currency, razorpayKeyId } = orderResponse.data

      // Step 3: Configure Razorpay Checkout options
      const options = {
        key: razorpayKeyId,
        amount: amount, // ₹499 = 49900 paise
        currency: currency,
        name: 'ZipLink Premium',
        description: '30 Days PRO Subscription',
        order_id: orderId,
        handler: async (response: any) => {
          // Success callback from Razorpay
          setLoading(true)
          try {
            const verifyPayload = {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature
            }

            // Step 4: Verify payment on backend
            await api.post('/api/payments/verify', verifyPayload)
            
            // Invalidate queries so stats updates
            queryClient.invalidateQueries({ queryKey: ['dashboardStats'] })
            
            // Re-fetch current user profile details
            const meResponse = await api.get('/api/auth/me')
            const me = meResponse.data
            
            updateUser({
              plan: me.plan,
              isPro: me.isPro,
              subscriptionExpiresAt: me.subscriptionExpiresAt
            })

            toast.success('Upgrade Successful! You are now a PRO user.')
          } catch (verifyError: any) {
            const errMsg = verifyError.response?.data?.message || 'Payment verification failed.'
            toast.error(errMsg)
          } finally {
            setLoading(false)
          }
        },
        prefill: {
          email: user?.email || '',
        },
        theme: {
          color: '#0ea5e9', // Sky blue checkout button theme
        },
        modal: {
          ondismiss: () => {
            setLoading(false)
            toast.error('Payment checkout dismissed')
          }
        }
      }

      // Step 5: Open Razorpay checkout modal
      const rzp = new (window as any).Razorpay(options)
      rzp.open()
      
    } catch (orderError: any) {
      const errMsg = orderError.response?.data?.message || 'Failed to initialize upgrade payment.'
      toast.error(errMsg)
      setLoading(false)
    }
  }

  const formatExpiry = (isoString: string | null | undefined) => {
    if (!isoString) return ''
    return new Date(isoString).toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    })
  }

  return (
    <div className="container-fluid min-vh-100 py-5 px-3 px-md-5" style={{ backgroundColor: '#fff1f2' /* Soft Rose */ }}>
      <div className="row justify-content-center">
        <div className="col-12 col-md-8 col-lg-6">
          
          {/* Header block */}
          <div className="text-center mb-5">
            <span className="badge bg-danger-subtle text-danger px-3 py-2 rounded-pill fw-semibold mb-2">
              <Sparkles size={14} className="me-1 align-middle" />
              SaaS Pro Upgrade
            </span>
            <h1 className="fw-extrabold text-dark display-6 mt-2">ZipLink Premium</h1>
            <p className="text-secondary small">
              Supercharge your link sharing with custom branded aliases and real-time deep analytics.
            </p>
          </div>

          {/* Pricing white Card */}
          <div className="card border-0 shadow-lg rounded-4 p-4 p-md-5 bg-white text-dark mb-4 position-relative overflow-hidden">
            {isPro && (
              <div className="position-absolute top-0 end-0 bg-warning text-dark fw-bold px-4 py-2 rounded-start-pill small">
                ACTIVE PLAN
              </div>
            )}
            
            <div className="d-flex justify-content-between align-items-center mb-4">
              <div>
                <h4 className="fw-bold mb-1">Premium Pass</h4>
                <span className="text-muted small">One-time payment. Valid for 30 days.</span>
              </div>
              <div className="text-end">
                <span className="fs-1 fw-extrabold text-dark">₹499</span>
                <span className="text-muted d-block small">/ 30 Days</span>
              </div>
            </div>

            <hr className="my-4 border-light-subtle" />

            {/* Features list */}
            <div className="mb-4">
              <h6 className="fw-bold mb-3">FEATURES INCLUDED:</h6>
              <ul className="list-unstyled d-flex flex-column gap-3">
                <li className="d-flex align-items-start gap-2">
                  <CheckCircle2 className="text-success mt-1 flex-shrink-0" size={18} />
                  <div>
                    <strong>Unlimited short links</strong>
                    <span className="small text-secondary d-block">Remove the limit of 5 links and generate endlessly.</span>
                  </div>
                </li>
                <li className="d-flex align-items-start gap-2">
                  <CheckCircle2 className="text-success mt-1 flex-shrink-0" size={18} />
                  <div>
                    <strong>Branded Custom Codes</strong>
                    <span className="small text-secondary d-block">Set custom keywords for alias redirection shortcodes.</span>
                  </div>
                </li>
                <li className="d-flex align-items-start gap-2">
                  <CheckCircle2 className="text-success mt-1 flex-shrink-0" size={18} />
                  <div>
                    <strong>Real-time Analytical breakdown</strong>
                    <span className="small text-secondary d-block">Device types, browsers, referrers, and timeline charts.</span>
                  </div>
                </li>
                <li className="d-flex align-items-start gap-2">
                  <CheckCircle2 className="text-success mt-1 flex-shrink-0" size={18} />
                  <div>
                    <strong>Redis performance cache</strong>
                    <span className="small text-secondary d-block">Fast redirect loadtimes warm in memory.</span>
                  </div>
                </li>
              </ul>
            </div>

            {/* Dynamic Buttons depending on plan */}
            {isPro ? (
              <div className="bg-success-subtle p-3 rounded-3 text-success mb-3 d-flex align-items-center gap-2">
                <Zap size={20} />
                <div>
                  <div className="small fw-bold">You are premium upgraded!</div>
                  <div className="small text-secondary" style={{ fontSize: '0.75rem' }}>
                    Valid until: <strong>{formatExpiry(user?.subscriptionExpiresAt)}</strong>
                  </div>
                </div>
              </div>
            ) : null}

            <button
              onClick={handleUpgrade}
              className={`btn w-100 py-3 rounded-pill fw-bold text-uppercase fs-6 shadow-sm d-flex align-items-center justify-content-center gap-2 ${
                isPro 
                  ? 'btn-outline-primary border-primary-custom text-primary' 
                  : 'btn-primary-custom'
              }`}
              disabled={loading}
            >
              {loading ? (
                <>
                  <Loader2 className="spinner-border spinner-border-sm border-0" size={18} style={{ animation: 'spin 1s linear infinite' }} />
                  <span>Processing...</span>
                </>
              ) : isPro ? (
                <>
                  <CreditCard size={18} />
                  <span>Renew Premium Pass</span>
                </>
              ) : (
                <>
                  <Zap size={18} />
                  <span>Upgrade to Premium Pass</span>
                </>
              )}
            </button>
          </div>

          {/* Secure notice info */}
          <div className="text-center text-muted small d-flex align-items-center justify-content-center gap-2">
            <Shield size={16} className="text-secondary" />
            <span>Payments secured via Razorpay. Transactions verified immediately.</span>
          </div>

        </div>
      </div>
    </div>
  )
}
