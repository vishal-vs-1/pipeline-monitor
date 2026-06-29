import { Component, OnInit, OnDestroy, signal, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WebsocketService } from './services/websocket.service';
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
  alerts: any[] = [];
  private subs = new Subscription();

  constructor(private wsService: WebsocketService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.subs.add(
      this.wsService.alerts$.subscribe(alert => {
        console.log("Received alert from WebSocket!", alert);
        this.alerts.push(alert);
        this.cdr.detectChanges();
        // Toasts will remain on screen until manually dismissed by the user
      })
    );
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
  }

  removeAlert(alert: any) {
    this.alerts = this.alerts.filter(a => a !== alert);
    this.cdr.detectChanges();
  }
}
