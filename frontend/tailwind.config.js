/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'football': {
          'bg': '#1a1d23',
          'card': '#22252d',
          'card-hover': '#2a2d35',
          'primary': '#2d8c4e',
          'primary-light': '#3cb371',
          'primary-dark': '#1a6b35',
          'gold': '#f0c040',
          'red': '#e05555',
          'green': '#3cb371',
          'blue': '#4a9eff',
        },
        'text': {
          'primary': '#e8eaed',
          'secondary': '#9aa0a6',
          'muted': '#6e7681',
        }
      },
      fontFamily: {
        sans: ['PingFang SC', 'Microsoft YaHei', 'sans-serif'],
      },
      animation: {
        'pulse-vs': 'pulseVs 2s ease-in-out infinite',
        'float-up': 'floatUp 0.3s ease-out',
        'fade-in': 'fadeIn 0.4s ease-out',
      },
      keyframes: {
        pulseVs: {
          '0%, 100%': { transform: 'scale(1)', opacity: '0.8' },
          '50%': { transform: 'scale(1.08)', opacity: '1' },
        },
        floatUp: {
          '0%': { transform: 'translateY(8px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}
