# EvidLock Render Backend Keep-Alive Script
# Run this during your presentation day to prevent Render from sleeping!
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " EvidLock Presentation Shield - Keep-Alive Running        " -ForegroundColor Green
Write-Host " Keeps https://evidlock.onrender.com awake every 3 minutes" -ForegroundColor Cyan
Write-Host " Press Ctrl+C in this window when presentation is finished" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

$url = "https://evidlock.onrender.com/health"
$intervalSeconds = 180

while ($true) {
    $timestamp = Get-Date -Format "HH:mm:ss"
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 15
        Write-Host "[$timestamp] Backend Alive! Status: $($response.status) (DB Connected: $($response.dbConnected))" -ForegroundColor Green
    } catch {
        Write-Host "[$timestamp] Ping failed or waking up: $($_.Exception.Message)" -ForegroundColor Red
    }
    Start-Sleep -Seconds $intervalSeconds
}
