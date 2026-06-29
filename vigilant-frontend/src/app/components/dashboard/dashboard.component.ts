import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebsocketService } from '../../services/websocket.service';
import { ApiService } from '../../services/api.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styles: []
})
export class DashboardComponent implements OnInit, OnDestroy {
  builds: any = {};
  repoKeys: string[] = [];
  private subs: Subscription = new Subscription();

  constructor(
    private wsService: WebsocketService, 
    private apiService: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    console.log("Fetching recent builds from API...");
    this.apiService.getRecentBuilds().subscribe({
      next: (data: any) => {
        console.log("SUCCESS! Received builds data:", data);
        this.builds = data;
        this.repoKeys = Object.keys(data);
        console.log("Repo keys parsed:", this.repoKeys);
        this.cdr.detectChanges(); // Force UI to update
      },
      error: (err: any) => {
        console.error('ERROR! Failed to load recent builds', err);
      }
    });

    this.subs.add(
      this.wsService.buildUpdates$.subscribe((update: any) => {
        console.log("WebSocket received update:", update);
        const repoName = update.repoName;
        if (!this.builds[repoName]) {
          this.builds[repoName] = [];
          this.repoKeys.push(repoName);
        }
        
        // Update existing or add new
        const existingIdx = this.builds[repoName].findIndex((b: any) => b.runId === update.runId);
        if (existingIdx !== -1) {
          this.builds[repoName][existingIdx] = update;
        } else {
          this.builds[repoName].unshift(update);
          if (this.builds[repoName].length > 5) {
            this.builds[repoName].pop();
          }
        }
        this.cdr.detectChanges(); // Force UI to update
      })
    );
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
  }

  getBadgeClass(conclusion: string): string {
    if (!conclusion) return 'badge-warning';
    if (conclusion.toLowerCase() === 'success') return 'badge-success';
    if (conclusion.toLowerCase() === 'failure') return 'badge-error';
    return 'badge-ghost';
  }
}
