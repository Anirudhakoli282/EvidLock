// Vercel Serverless Function: /api/keepalive
// Called by Vercel cron to keep the Render backend awake.
// Fallback to https://evidlock.onrender.com ensures it always pings even if env var is missing.

export default async function handler(req, res) {
  const rawUrl = process.env.RENDER_API_URL || 'https://evidlock.onrender.com';
  const pingUrl = rawUrl.replace(/\/$/, '') + '/api/health';

  try {
    const response = await fetch(pingUrl, {
      method: 'GET',
      signal: AbortSignal.timeout(15000)
    });

    const text = await response.text();
    console.log(`[keepalive] Pinged ${pingUrl} → ${response.status}`);

    return res.status(200).json({
      status: 'ok',
      backendStatus: response.status,
      response: text
    });
  } catch (err) {
    console.error('[keepalive] Ping failed:', err.message);
    return res.status(200).json({ status: 'error', message: err.message });
  }
}
