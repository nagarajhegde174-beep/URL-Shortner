import React from 'react'
import { Sun, Moon } from 'lucide-react'
import { useTheme } from '../../context/ThemeContext'

interface ThemeToggleProps {
  /** 'icon' shows icon-only (sidebar), 'full' shows icon + label (navbar) */
  variant?: 'icon' | 'full'
  className?: string
}

export const ThemeToggle: React.FC<ThemeToggleProps> = ({ variant = 'icon', className = '' }) => {
  const { theme, toggleTheme } = useTheme()
  const isDark = theme === 'dark'

  const label = isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'

  if (variant === 'full') {
    return (
      <button
        type="button"
        className={`theme-toggle-btn theme-toggle-full ${className}`}
        onClick={toggleTheme}
        aria-label={label}
        title={label}
      >
        {isDark ? (
          <>
            <Sun size={16} />
            <span>Light</span>
          </>
        ) : (
          <>
            <Moon size={16} />
            <span>Dark</span>
          </>
        )}
      </button>
    )
  }

  return (
    <button
      type="button"
      className={`theme-toggle-btn ${className}`}
      onClick={toggleTheme}
      aria-label={label}
      title={label}
    >
      {isDark ? <Sun size={18} /> : <Moon size={18} />}
    </button>
  )
}
