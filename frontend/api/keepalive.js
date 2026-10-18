// Vercel Serverless Function: /api/keepalive
// Called every 5 minutes by Vercel cron to keep the Render backend awake.
// Render free tier sleeps after 15 min of inactivity — this prevents that.

export default async function handler(req, res) {
  const backendUrl = process.env.RENDER_API_URL || '';

  if (!backendUrl) {
    return res.status(200).json({ status: 'skipped', reason: 'RENDER_API_URL not set' });
  }

  try {
    const pingUrl = backendUrl.replace(/\/$/, '') + '/api/health';
    const response = await fetch(pingUrl, {
      method: 'GET',
      signal: AbortSignal.timeout(10000) // 10s timeout
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
