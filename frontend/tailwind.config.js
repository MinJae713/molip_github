/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
        "./.storybook/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                "primary": "#3B82F6",
                "primary-dark": "#1D4ED8",
                "indigo-brand": "#4F46E5",
                "indigo-dark": "#3730A3",
                "electric-blue": "#00E5FF",
                "magenta": "#D946EF",
                "dark-navy-900": "#0A0F1D",
                "dark-navy-800": "#111927",
                "dark-navy-700": "#1F2937",
                "light-bg": "#F8FAFC",
                "slate-850": "#151e2e",
            },
            fontFamily: {
                "display": ["Lexend", "Noto Sans KR", "sans-serif"],
                "body": ["Noto Sans KR", "sans-serif"]
            },
            boxShadow: {
                'soft': '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)',
                'card': '0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025)',
            }
        },
    },
    plugins: [],
}