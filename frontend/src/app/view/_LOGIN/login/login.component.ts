import { Component } from '@angular/core';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { AuthService } from '../../../service/auth/auth.service';
import { Router } from '@angular/router';
import { FormCheck } from '../../../FormCheck';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  loginForm: FormGroup;
  model: NgbDateStruct | undefined;

  success: boolean = true;
  serverError: boolean = false;
  loading: boolean = false;
  wakingUp: boolean = false;
  retryCount: number = 0;
  readonly MAX_RETRIES = 3;
  readonly RETRY_DELAY_MS = 15000;

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private auth: AuthService,
  ) {
    this.loginForm = this.fb.group({
      email: ['', Validators.required],
      password: ['', Validators.required],
      rememberMe: [false]
    }, { validators: this.customValidation });
  }

  customValidation(group: FormGroup) {
    const emailControl = group.get('email');
    const passwordControl = group.get('password');

    if (emailControl && passwordControl) {
      const email = emailControl.value;
      if (email && !FormCheck.checkEmail(email)) {
        emailControl.setErrors({ 'invalidEmail': true });
      } else {
        if (emailControl.hasError('invalidEmail')) {
          emailControl.setErrors(null);
        }
      }
    }
  }

  submitForm() {
    this.serverError = false;
    this.success = true;
    this.retryCount = 0;
    this.wakingUp = false;
    this.loading = true;
    this.attemptLogin();
  }

  private attemptLogin(): void {
    const email = this.loginForm.get('email')?.value;
    const password = this.loginForm.get('password')?.value;
    const rememberMe = this.loginForm.get('rememberMe')?.value;

    this.auth.login(email, password, rememberMe).subscribe(
      result => {
        this.loading = false;
        this.wakingUp = false;
        if (result) {
          this.router.navigate(['/']);
        } else {
          this.success = false;
        }
      },
      error => {
        console.error('Login error:', error);
        if (this.retryCount < this.MAX_RETRIES) {
          this.retryCount++;
          this.wakingUp = true;
          this.serverError = false;
          // Auto-retry after delay — wakes Render free-tier backend
          setTimeout(() => this.attemptLogin(), this.RETRY_DELAY_MS);
        } else {
          this.loading = false;
          this.wakingUp = false;
          this.serverError = true;
        }
      }
    );
  }

  selectDemoRole(role: string): void {
    this.serverError = false;
    this.success = true;
    this.wakingUp = false;
    let email = '';
    const password = 'Web2324.';
    switch (role) {
      case 'SUPERVISOR':  email = 'supervisor@evidlock.gov';  break;
      case 'INVESTIGATOR': email = 'investigator@evidlock.gov'; break;
      case 'CUSTODIAN':   email = 'custodian@evidlock.gov';   break;
      case 'AUDITOR':     email = 'auditor@evidlock.gov';     break;
    }
    this.loginForm.patchValue({ email, password });
    this.loginForm.markAsDirty();
  }
}
