import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-config',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './config.component.html',
  styles: []
})
export class ConfigComponent implements OnInit {
  repoForm: FormGroup;
  repos: any[] = [];
  editingRepoId: number | null = null;
  apiErrors: string[] = [];

  constructor(
    private fb: FormBuilder, 
    private apiService: ApiService, 
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {
    this.repoForm = this.fb.group({
      repoName: ['', Validators.required],
      branch: ['main', Validators.required],
      anomalyMultiplier: [1.5, [Validators.required, Validators.min(1.0)]],
      anomalyWindowSize: [10, [Validators.required, Validators.min(1)]],
      isActive: [true]
    });
  }

  ngOnInit() {
    this.loadRepos();
  }

  loadRepos() {
    this.apiService.getRepos().subscribe({
      next: (data: any) => {
        this.repos = data;
        this.cdr.detectChanges();
        
        // Auto-select repo if query param is present
        this.route.queryParams.subscribe(params => {
          if (params['editRepoId']) {
            const id = +params['editRepoId'];
            const repo = this.repos.find(r => r.id === id);
            if (repo) {
              this.editRepo(repo);
            }
          }
        });
      },
      error: (err: any) => {
        console.error('Failed to load repos', err);
      }
    });
  }

  editRepo(repo: any) {
    this.apiErrors = [];
    this.editingRepoId = repo.id;
    this.repoForm.patchValue({
      repoName: repo.repoName,
      branch: repo.branch,
      anomalyMultiplier: repo.anomalyMultiplier,
      anomalyWindowSize: repo.anomalyWindowSize,
      isActive: repo.isActive
    });
    this.cdr.detectChanges();
  }

  cancelEdit() {
    this.editingRepoId = null;
    this.apiErrors = [];
    this.repoForm.reset({
      branch: 'main',
      anomalyMultiplier: 1.5,
      anomalyWindowSize: 10,
      isActive: true
    });
    this.cdr.detectChanges();
  }

  dismissError() {
    this.apiErrors = [];
    this.cdr.detectChanges();
  }

  onSubmit() {
    this.apiErrors = [];
    if (this.repoForm.valid) {
      if (this.editingRepoId) {
        this.apiService.updateRepo(this.editingRepoId, this.repoForm.value).subscribe({
          next: (res: any) => {
            const index = this.repos.findIndex(r => r.id === this.editingRepoId);
            if (index !== -1) {
              this.repos[index] = res;
            }
            this.cancelEdit();
          },
          error: (err: any) => {
            console.error(err);
            if (err.error && err.error.errors) {
              this.apiErrors = err.error.errors;
            } else {
              this.apiErrors = ['An unexpected error occurred while updating the repository.'];
            }
            this.cdr.detectChanges();
          }
        });
      } else {
        this.apiService.addRepo(this.repoForm.value).subscribe({
          next: (res: any) => {
            this.repos.push(res);
            this.cancelEdit();
          },
          error: (err: any) => {
            console.error(err);
            if (err.error && err.error.errors) {
              this.apiErrors = err.error.errors;
            } else {
              this.apiErrors = ['An unexpected error occurred while adding the repository.'];
            }
            this.cdr.detectChanges();
          }
        });
      }
    }
  }

  deleteRepo(id: number) {
    this.apiErrors = [];
    if (confirm('Are you sure you want to delete this repository? This will also delete all associated metrics and states.')) {
      this.apiService.deleteRepo(id).subscribe({
        next: () => {
          this.repos = this.repos.filter(r => r.id !== id);
          if (this.editingRepoId === id) {
            this.cancelEdit();
          }
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          console.error('Failed to delete repo', err);
          alert('Failed to delete repository. Check console for details.');
        }
      });
    }
  }
}
