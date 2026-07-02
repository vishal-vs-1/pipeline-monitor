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
  private subs = new Subscription();

  constructor(private wsService: WebsocketService) {}

  ngOnInit() {
    // Other subscriptions can go here if needed in the future
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
  }
}
