import { Router, NavigationStart } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { AuthService } from './service/auth/auth.service';
import { HttpClient } from '@angular/common/http';
import { Settings } from './Settings';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title?: string;
  isLoginPage: boolean = false;
  testString: string = '';

  constructor(
    private router: Router,
    private auth: AuthService,
    private http: HttpClient
  ) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.isLoginPage = event.url === '/login' || event.url === '/register';
      }
    });
  }

  ngOnInit(): void {
    // Wake up the Render backend immediately on app load.
    // Render free tier sleeps after 15 min — this silent ping ensures
    // the backend is ready before the user tries to log in.
    this.wakeBackend();
  }

  private wakeBackend(): void {
    const wakeUrl = Settings.API_ENDPOINT.replace(/\/$/, '') + '/api/health';
    this.http.get(wakeUrl, { responseType: 'text' }).subscribe({
      next: () => console.log('[EvidLock] Backend is awake ✓'),
      error: () => {
        // Backend may be cold-starting — retry after 20 seconds
        console.log('[EvidLock] Backend waking up, retry in 20s...');
        setTimeout(() => {
          this.http.get(wakeUrl, { responseType: 'text' }).subscribe({
            next: () => console.log('[EvidLock] Backend awake after retry ✓'),
            error: () => console.log('[EvidLock] Backend still starting up...')
          });
        }, 20000);
      }
    });
  }
}
