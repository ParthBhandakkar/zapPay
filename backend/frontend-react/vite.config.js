import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // Listen on all network interfaces
    port: 5173,
    cors: true,
    allowedHosts: [
      'localhost',
      '127.0.0.1',
      '.ngrok-free.app', // Allow all ngrok free domains
      '.ngrok.io',       // Allow all ngrok domains
      '.ngrok.app',      // Allow all ngrok app domains
    ],
    headers: {
      // Cache busting headers
      'Cache-Control': 'no-cache, no-store, must-revalidate',
      'Pragma': 'no-cache',
      'Expires': '0'
    },
    // Ensure CSS and assets load properly on hard refresh
    fs: {
      strict: true
    }
  },
  build: {
    rollupOptions: {
      output: {
        // Add hash to filenames for cache busting
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    }
  },
  css: {
    // Ensure CSS is processed consistently
    devSourcemap: true,
    modules: {
      localsConvention: 'camelCase'
    }
  }
})
