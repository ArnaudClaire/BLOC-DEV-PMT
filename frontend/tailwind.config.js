/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        ink: '#0f172a',
        mist: '#eef2ff',
        sand: '#fff7ed',
        ember: '#ea580c',
        lagoon: '#0f766e',
      },
      boxShadow: {
        soft: '0 24px 80px rgba(15, 23, 42, 0.10)',
      },
      fontFamily: {
        display: ['Georgia', 'Times New Roman', 'serif'],
        body: ['Segoe UI', 'Tahoma', 'Geneva', 'Verdana', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
