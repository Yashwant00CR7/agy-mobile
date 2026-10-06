import esbuild from 'esbuild';
import fs from 'node:fs';
import path from 'node:path';

const isDev = process.argv.includes('--dev');

async function build() {
  const startTime = Date.now();
  console.log('Building agy-mobile...');

  // Ensure dist directories exist
  fs.mkdirSync('dist/public', { recursive: true });

  // 1. Build Server (Zero-dependency bundled ESM for Node.js)
  await esbuild.build({
    entryPoints: ['src/server/index.ts'],
    outfile: 'dist/server.js',
    bundle: true,
    platform: 'node',
    format: 'esm',
    target: 'node22',
    sourcemap: isDev,
    minify: !isDev,
    external: [
      // Only keep native node built-ins external
      'node:*',
      'fs',
      'path',
      'os',
      'child_process',
      'http',
      'https',
      'stream',
      'url',
      'crypto',
      'events',
      'readline',
      'util',
      'buffer',
      'net'
    ],
    banner: {
      js: "import { createRequire } from 'module'; const require = createRequire(import.meta.url);"
    }
  });

  // 2. Build Client (Browser ESM bundle)
  await esbuild.build({
    entryPoints: ['src/client/app.ts'],
    outfile: 'dist/public/app.js',
    bundle: true,
    platform: 'browser',
    format: 'esm',
    target: 'es2022',
    sourcemap: isDev,
    minify: !isDev
  });

  // 3. Build Service Worker
  await esbuild.build({
    entryPoints: ['src/client/sw.ts'],
    outfile: 'dist/public/sw.js',
    bundle: true,
    platform: 'browser',
    format: 'esm',
    target: 'es2022',
    sourcemap: isDev,
    minify: !isDev
  });

  // 4. Copy static assets if they exist in src/client
  const clientDir = 'src/client';
  const publicDir = 'dist/public';
  const staticFiles = ['index.html', 'style.css', 'manifest.json', 'icon.svg', 'favicon.ico'];

  for (const file of staticFiles) {
    const srcPath = path.join(clientDir, file);
    if (fs.existsSync(srcPath)) {
      fs.copyFileSync(srcPath, path.join(publicDir, file));
    }
  }

  console.log(`Build completed in ${Date.now() - startTime}ms.`);
}

build().catch((err) => {
  console.error('Build failed:', err);
  process.exit(1);
});
