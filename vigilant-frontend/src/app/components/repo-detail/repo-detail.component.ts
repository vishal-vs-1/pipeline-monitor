import { Component, OnInit, HostListener, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-repo-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './repo-detail.component.html',
  styles: []
})
export class RepoDetailComponent implements OnInit {
  repoId!: number;
  builds: any[] = [];
  page = 0;
  size = 20;
  loading = false;
  hasMore = true;

  constructor(private route: ActivatedRoute, private apiService: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.repoId = +idParam;
        this.loadBuilds();
      }
    });
  }

  loadBuilds() {
    console.log('loadBuilds() called! loading:', this.loading, 'hasMore:', this.hasMore, 'page:', this.page);
    if (this.loading || !this.hasMore) return;
    this.loading = true;

    console.log('Making API call to getRepoBuilds for repo:', this.repoId);
    this.apiService.getRepoBuilds(this.repoId, this.page, this.size).subscribe({
      next: (data: any) => {
        console.log('API call succeeded! Data received:', data);
        try {
          if (data && data.content) {
            console.log('Appending', data.content.length, 'builds');
            this.builds = [...this.builds, ...data.content];
            // Determine if there are more pages based on the 'page' metadata block
            if (data.page) {
               this.hasMore = data.page.number < (data.page.totalPages - 1);
            } else {
               // Fallback if structured differently
               this.hasMore = data.content.length === this.size;
            }
            this.page++;
          } else {
            console.log('No data.content found in response.');
            this.hasMore = false;
          }
        } catch (e) {
          console.error('Error processing API response:', e);
        } finally {
          this.loading = false;
          console.log('Loading set to false (success/finally).');
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Error loading repo builds! API call failed:', err);
        this.loading = false;
        console.log('Loading set to false (error).');
        this.cdr.detectChanges();
      }
    });
  }

  @HostListener('window:scroll')
  onScroll() {
    // Detect when user is near bottom of the page
    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
      this.loadBuilds();
    }
  }

  getBadgeClass(conclusion: string): string {
    if (!conclusion) return 'badge-warning';
    if (conclusion.toLowerCase() === 'success') return 'badge-success';
    if (conclusion.toLowerCase() === 'failure') return 'badge-error';
    return 'badge-ghost';
  }
}
