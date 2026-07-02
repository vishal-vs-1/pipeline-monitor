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

  constructor(
    private fb: FormBuilder, 
    private apiService: ApiService, 
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {
    this.repoForm = this.fb.group({
      repoName: ['', Validators.required],
      githubToken: ['', Validators.required],
      branch: ['main'],
      anomalyMultiplier: [1.5, [Validators.required, Validators.min(1.2), Validators.max(5.0)]],
      anomalyWindowSize: [10, [Validators.required, Validators.min(5), Validators.max(20)]],
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
    this.editingRepoId = repo.id;
    this.repoForm.patchValue({
      repoName: repo.repoName,
      githubToken: '', // Keep empty so we don't accidentally send a dummy token
      branch: repo.branch,
      anomalyMultiplier: repo.anomalyMultiplier,
      anomalyWindowSize: repo.anomalyWindowSize,
      isActive: repo.isActive
    });
    // githubToken is optional during update
    this.repoForm.get('githubToken')?.clearValidators();
    this.repoForm.get('githubToken')?.updateValueAndValidity();
    this.cdr.detectChanges();
  }

  cancelEdit() {
    this.editingRepoId = null;
    this.repoForm.reset({
      branch: 'main',
      anomalyMultiplier: 1.5,
      anomalyWindowSize: 10,
      isActive: true
    });
    this.repoForm.get('githubToken')?.setValidators([Validators.required]);
    this.repoForm.get('githubToken')?.updateValueAndValidity();
    this.cdr.detectChanges();
  }

  onSubmit() {
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
          error: (err: any) => console.error(err)
        });
      } else {
        this.apiService.addRepo(this.repoForm.value).subscribe({
          next: (res: any) => {
            this.repos.push(res);
            this.cancelEdit();
          },
          error: (err: any) => console.error(err)
        });
      }
    }
  }
}
