/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                primary: '#0f766e',
                'primary-hover': '#115e59',
                'accent-pink': '#cbd5e1',
                'accent-high': '#f59e0b',
            },
            fontFamily: {
                sans: ['Manrope', 'Inter', 'sans-serif'],
            },
            boxShadow: {
                'soft-diffusion': '0 22px 40px -18px rgba(15, 23, 42, 0.14), 0 10px 20px -14px rgba(15, 23, 42, 0.08)',
            }
        },
    },
    plugins: [],
}
