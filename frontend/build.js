#!/usr/bin/env node
/**
 * Vercel Build Script for EvidLock Frontend
 *
 * This script:
 * 1. Reads RENDER_API_URL from environment (set in Vercel project settings)
 * 2. Injects it into environment.prod.ts, replacing the __API_URL__ placeholder
 * 3. Runs the Angular production build
 *
 * Set this as your Vercel "Build Command":
 *   node build.js
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// Read the backend URL from Vercel's environment variables (fallback to live Render backend)
const apiUrl = process.env.RENDER_API_URL || 'https://evidlock.onrender.com/';

// Ensure the URL ends with a trailing slash
const normalizedApiUrl = apiUrl.endsWith('/') ? apiUrl : apiUrl + '/';
console.log(`Using backend API URL: ${normalizedApiUrl}`);

// Patch the environment.prod.ts file
const envProdPath = path.join(__dirname, 'src', 'environments', 'environment.prod.ts');
let content = fs.readFileSync(envProdPath, 'utf8');
content = content.replace('__API_URL__', normalizedApiUrl);
fs.writeFileSync(envProdPath, content);
console.log('Patched environment.prod.ts');

// Run Angular production build
console.log('Running Angular production build...');
execSync('npm run build -- --configuration production', { stdio: 'inherit' });
console.log('Build complete!');
