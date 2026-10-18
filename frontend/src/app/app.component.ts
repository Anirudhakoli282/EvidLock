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
    // Proactively wake up backend as soon as app loads
    this.wakeBackend();
  }

  private wakeBackend(): void {
    try {
      const wakeUrl = Settings.API_ENDPOINT.replace(/\/$/, '') + '/api/health';
      this.http.get(wakeUrl, { responseType: 'text' }).subscribe({
        next: () => console.log('[EvidLock] Backend is awake ✓'),
        error: () => console.log('[EvidLock] Backend waking up...')
      });
    } catch (e) {
      // Non-critical background ping
    }
  }
}
