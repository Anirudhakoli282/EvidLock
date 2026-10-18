// Vercel Serverless Function: /api/keepalive
// Called every 5 minutes by Vercel cron to keep the Render backend awake.
// Render free tier sleeps after 15 min of inactivity — this prevents that.
// Hardcoded backend URL so this always runs even without RENDER_API_URL env var.

const BACKEND_URL = process.env.RENDER_API_URL
  ? process.env.RENDER_API_URL.replace(/\/$/, '')
  : 'https://evidlock-backend.onrender.com';

export default async function handler(req, res) {
  const pingUrl = BACKEND_URL + '/';   // ping root — always responds

  try {
    const response = await fetch(pingUrl, {
      method: 'GET',
      signal: AbortSignal.timeout(25000)  // 25s — enough for cold start
    });

    console.log(`[keepalive] Pinged ${pingUrl} → ${response.status}`);
    return res.status(200).json({ status: 'ok', backendStatus: response.status });
  } catch (err) {
    console.error('[keepalive] Ping failed:', err.message);
    return res.status(200).json({ status: 'error', message: err.message });
  }
}
