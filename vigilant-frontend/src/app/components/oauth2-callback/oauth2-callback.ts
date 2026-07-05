import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-oauth2-callback',
  standalone: true,
  template: `<div class="min-h-screen flex items-center justify-center bg-base-200"><span class="loading loading-spinner loading-lg"></span></div>`
})
export class Oauth2Callback implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      const refreshToken = params['refreshToken'];
      const userStr = params['user'];

      if (token && refreshToken && userStr) {
        try {
          const user = JSON.parse(decodeURIComponent(userStr));
          this.authService.handleAuthResponse({
            accessToken: token,
            refreshToken: refreshToken,
            user: user
          });
          this.router.navigate(['/dashboard']);
        } catch (e) {
          console.error('Failed to parse user info', e);
          this.router.navigate(['/login']);
        }
      } else {
        this.router.navigate(['/login']);
      }
    });
  }
}
