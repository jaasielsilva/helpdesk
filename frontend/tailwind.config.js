/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      colors: {
        brand: {
          50:  '#f5f3ff',
          100: '#ede9fe',
          200: '#ddd6fe',
          300: '#c084fc',
          400: '#a855f7',
          500: '#8b5cf6',
          600: '#7c3aed',
          700: '#6d28d9',
          800: '#5b21b6',
          900: '#4c1d95',
          purple: '#764ba2',
          indigo: '#667eea',
        },
        primary: {
          50:  '#f5f3ff',
          100: '#ede9fe',
          500: '#8b5cf6',
          600: '#7c3aed',
          700: '#6d28d9',
        },
      },
      boxShadow: {
        'premium': '0 8px 30px rgba(15, 23, 42, 0.03)',
        'glow': '0 10px 25px rgba(102, 126, 234, 0.25)',
      },
      keyframes: {
        slideDown: {
          'from': { opacity: '0', transform: 'translateY(-8px)' },
          'to': { opacity: '1', transform: 'translateY(0)' },
        }
      },
      animation: {
        slideDown: 'slideDown 0.2s ease-out forwards',
      }
    },
  },
  plugins: [],
}
