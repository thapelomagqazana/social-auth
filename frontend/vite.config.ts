import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  // Load environment variables based on the mode (e.g., development, production)
  const env = loadEnv(mode, process.cwd());

  return {
    plugins: [
      react(), // React plugin for Vite
    ],
    server: {
      port: Number(env.VITE_SERVER_PORT) || 5173, // Set the server port using env variables or default
      watch: {
        usePolling: true, // Ensure hot reload works in virtualized environments
      },
    },
    build: {
      outDir: 'dist', // Output directory for build files
      sourcemap: true, // Generate source maps for debugging production builds
      rollupOptions: {
        output: {
          manualChunks: {
            vendor: ['react', 'react-dom'], // Split vendor code into separate chunks
          },
        },
      },
    },
    // Resolve .env variables for use in the app
    define: {
      'process.env': env,
    },
  };
});
