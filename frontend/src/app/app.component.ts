import { Router, NavigationStart } from '@angular/router';
import { Component } from '@angular/core';
import { AuthService } from './service/auth/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title?: string;

  isLoginPage: boolean = false;

  testString: string = '';

  constructor(
    private router: Router,
    private auth: AuthService) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.isLoginPage = event.url === '/login' || event.url === '/register'; //controllo se è login o register 
      }
    });
  }

  
}
