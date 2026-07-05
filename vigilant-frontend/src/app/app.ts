import { Component, OnInit, OnDestroy, signal, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WebsocketService } from './services/websocket.service';
import { AuthService } from './services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app.html',
  styles: []
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('vigilant-frontend');
  private subs = new Subscription();
  currentUser: any = null;

  constructor(
    private wsService: WebsocketService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.subs.add(
      this.authService.currentUser$.subscribe(user => {
        this.currentUser = user;
      })
    );
  }

  logout() {
    this.authService.logout();
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
  }
}
