import { create } from 'zustand'

export interface User {
  id: number
  email: string
  role: string
  plan: string
  isPro?: boolean
  subscriptionExpiresAt?: string | null
}

interface AuthState {
  accessToken: string | null
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  setAuth: (accessToken: string, user: User) => void
  clearAuth: () => void
  setIsLoading: (isLoading: boolean) => void
  updateUser: (updatedUser: Partial<User>) => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  isAuthenticated: false,
  isLoading: true, // starts true to check silent refresh on mount
  setAuth: (accessToken, user) => set({
    accessToken,
    user,
    isAuthenticated: true,
    isLoading: false
  }),
  clearAuth: () => set({
    accessToken: null,
    user: null,
    isAuthenticated: false,
    isLoading: false
  }),
  setIsLoading: (isLoading) => set({ isLoading }),
  updateUser: (updatedUser) => set((state) => ({
    user: state.user ? { ...state.user, ...updatedUser } : null
  }))
}))
