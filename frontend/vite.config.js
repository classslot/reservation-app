import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 開発中は /api をバックエンド(8080)へ転送する（Cookieが同一オリジン扱いになりCORS不要）
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080' },
  },
});
