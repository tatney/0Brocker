import { defineConfig, devices } from '@playwright/test';
export default defineConfig({
  testDir:'./e2e', fullyParallel:true, workers:4,
  reporter:process.env.CI?'dot':'list',
  use:{baseURL:'http://127.0.0.1:4183',trace:'on-first-retry'},
  projects:[{name:'chromium',use:{...devices['Desktop Chrome']}}],
  webServer:{
    command:'vite build --mode test --outDir dist-test && vite preview --host 127.0.0.1 --outDir dist-test --port 4183 --strictPort',
    url:'http://127.0.0.1:4183', reuseExistingServer:false,timeout:120000,
    env:{VITE_SUPABASE_URL:'',VITE_SUPABASE_ANON_KEY:''},
  },
});
