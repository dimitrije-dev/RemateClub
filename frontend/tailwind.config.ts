import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        remate: {
          navy: '#0E1A2B',
          navy2: '#1F2A3D',
          bg: '#E7EBEF',
          green: '#4FD09C',
          greenLight: '#A7F3D0',
          muted: '#64748B',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 18px 50px rgba(14, 26, 43, 0.12)',
      },
      borderRadius: {
        remate: '18px',
      },
    },
  },
  plugins: [],
} satisfies Config;

