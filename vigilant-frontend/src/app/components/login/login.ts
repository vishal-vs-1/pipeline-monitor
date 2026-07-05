import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login implements OnInit {
  loginForm: FormGroup;
  error: string = '';
  loading = false;
  isRegistering = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }

    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      name: ['']
    });
  }

  ngOnInit() {
  }

  get f() { return this.loginForm.controls; }

  toggleMode() {
    this.isRegistering = !this.isRegistering;
    this.error = '';
    if (this.isRegistering) {
      this.loginForm.get('name')?.setValidators([Validators.required]);
    } else {
      this.loginForm.get('name')?.clearValidators();
    }
    this.loginForm.get('name')?.updateValueAndValidity();
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    const authObs = this.isRegistering 
      ? this.authService.register(this.loginForm.value)
      : this.authService.login({ email: this.f['email'].value, password: this.f['password'].value });

    authObs.subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        this.error = err.error?.message || 'Authentication failed';
        this.loading = false;
      }
    });
  }

  loginWithGithub() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/github';
  }

  loginWithGoogle() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  }
}
