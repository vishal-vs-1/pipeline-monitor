import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebsocketService } from '../services/websocket.service';
import { ApiService } from '../services/api.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-6">
      <h1 class="text-3xl font-bold mb-6 text-primary">Pipeline Dashboard</h1>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        @for (repo of repoKeys; track repo) {
          <div class="card bg-base-100 shadow-xl border border-base-300 hover:shadow-2xl transition-all duration-300">
            <div class="card-body">
              <h2 class="card-title text-accent">{{ repo }}</h2>
              <div class="divider my-0"></div>
              
              <div class="flex flex-col gap-2 mt-2">
                @for (build of builds[repo]; track build.runId) {
                  <div class="flex justify-between items-center p-2 rounded-lg bg-base-200">
                    <span class="font-mono text-sm opacity-70">#{{ build.runId }}</span>
                    <span class="badge" [ngClass]="getBadgeClass(build.conclusion)">
                      {{ build.conclusion || build.status }}
                    </span>
                    <span class="text-sm opacity-50">{{ build.durationSeconds ? build.durationSeconds + 's' : '-' }}</span>
                  </div>
                }
              </div>
              
            </div>
          </div>
        }
        @empty {
          <div class="col-span-full flex justify-center items-center p-12 bg-base-200 rounded-xl border border-dashed border-base-300">
            <p class="text-lg opacity-60">No repositories are currently being tracked. Add one in Configuration!</p>
          </div>
        }
      </div>
    </div>
  `,
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
      error: (err) => {
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
