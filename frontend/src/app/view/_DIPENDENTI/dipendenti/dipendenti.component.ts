import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../service/user/user.service';
import { User } from '../../../model/User';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dipendenti',
  templateUrl: './dipendenti.component.html',
  styleUrl: './dipendenti.component.css'
})
export class DipendentiComponent implements OnInit {
  users: User[] = [];
  isInitialized: boolean = false;

  constructor(
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.userService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.isInitialized = true;
      },
      error: (error) => {
        if (error.status == 401) {
          this.router.navigate(['/login']);
        } else {
            console.error("Error fetching users:", error);
        }
      }
    });
  }
}
