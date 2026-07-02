import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { WebsocketService } from '../../services/websocket.service';
import { ApiService } from '../../services/api.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styles: []
})
export class DashboardComponent implements OnInit, OnDestroy {
  repoBuilds: any[] = [];
  private subs: Subscription = new Subscription();

  constructor(
    private wsService: WebsocketService, 
    private apiService: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    console.log("Fetching recent builds from API...");
    this.apiService.getRecentBuilds().subscribe({
      next: (data: any[]) => {
        console.log("SUCCESS! Received builds data:", data);
        this.repoBuilds = data || [];
        this.cdr.detectChanges(); // Force UI to update
      },
      error: (err: any) => {
        console.error('ERROR! Failed to load recent builds', err);
      }
    });

    this.subs.add(
      this.wsService.buildUpdates$.subscribe((update: any) => {
        console.log("WebSocket received update:", update);
        
        let existingRepo = this.repoBuilds.find(r => r.repoId === update.repoId);
        
        if (!existingRepo) {
           // Basic parsing for new repos from WebSocket event
           const branchMatch = update.repoName.match(/\((.*?)\)/);
           const branch = branchMatch ? branchMatch[1] : '';
           const name = update.repoName.split(' ')[0];
           
           existingRepo = { repoId: update.repoId, repoName: name, branch: branch, builds: [] };
           this.repoBuilds.push(existingRepo);
        }
        
        // Update existing or add new
        const existingIdx = existingRepo.builds.findIndex((b: any) => b.runId === update.runId);
        if (existingIdx !== -1) {
          existingRepo.builds[existingIdx] = update;
        } else {
          existingRepo.builds.unshift(update);
          if (existingRepo.builds.length > 5) {
            existingRepo.builds.pop();
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
