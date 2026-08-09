/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        space: {
          950: '#030712',
          900: '#060d1d',
          850: '#0a142c',
          800: '#0f1c3f',
          700: '#1e293b',
          600: '#334155',
        },
        hud: {
          cyan: '#00f0ff',
          blue: '#3b82f6',
          amber: '#f59e0b',
          orange: '#f97316',
          red: '#ef4444',
          emerald: '#10b981',
          purple: '#a855f7',
        }
      },
      fontFamily: {
        mono: ['"JetBrains Mono"', '"Fira Code"', 'Consolas', 'monospace'],
        sans: ['"Inter"', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        'glow-cyan': '0 0 20px rgba(0, 240, 255, 0.35)',
        'glow-red': '0 0 25px rgba(239, 68, 68, 0.45)',
        'glow-amber': '0 0 20px rgba(245, 158, 11, 0.35)',
        'glow-emerald': '0 0 20px rgba(16, 185, 129, 0.35)',
      },
      animation: {
        'pulse-fast': 'pulse 1s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin-slow': 'spin 30s linear infinite',
        'ping-slow': 'ping 2.5s cubic-bezier(0, 0, 0.2, 1) infinite',
      }
    },
  },
  plugins: [],
}
