import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-config',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="p-6 max-w-4xl mx-auto">
      <h1 class="text-3xl font-bold mb-6 text-primary">Configuration</h1>
      
      <div class="card bg-base-100 shadow-xl mb-8 border border-base-300">
        <div class="card-body">
          <h2 class="card-title mb-4">Add Tracked Repository</h2>
          <form [formGroup]="repoForm" (ngSubmit)="onSubmit()" class="flex flex-col gap-4">
            
            <label class="form-control w-full">
              <div class="label"><span class="label-text">Repository Name (owner/repo)</span></div>
              <input type="text" formControlName="repoName" placeholder="e.g. facebook/react" class="input input-bordered w-full" />
            </label>

            <label class="form-control w-full">
              <div class="label"><span class="label-text">GitHub Personal Access Token</span></div>
              <input type="password" formControlName="githubToken" placeholder="ghp_..." class="input input-bordered w-full" />
            </label>

            <label class="form-control w-full">
              <div class="label"><span class="label-text">Branch (default: main)</span></div>
              <input type="text" formControlName="branch" placeholder="main" class="input input-bordered w-full" />
            </label>

            <div class="card-actions justify-end mt-4">
              <button type="submit" class="btn btn-primary" [disabled]="!repoForm.valid">Add Repository</button>
            </div>
          </form>
        </div>
      </div>

      <h2 class="text-2xl font-bold mb-4">Tracked Repositories</h2>
      <div class="overflow-x-auto rounded-box border border-base-300">
        <table class="table table-zebra w-full bg-base-100">
          <thead>
            <tr>
              <th>ID</th>
              <th>Repository</th>
              <th>Branch</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            @for (repo of repos; track repo.id) {
              <tr>
                <td>{{ repo.id }}</td>
                <td class="font-semibold">{{ repo.repoName }}</td>
                <td><div class="badge badge-outline">{{ repo.branch }}</div></td>
                <td>
                  <div class="badge" [ngClass]="repo.isActive ? 'badge-success' : 'badge-error'">
                    {{ repo.isActive ? 'Active' : 'Inactive' }}
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: []
})
export class ConfigComponent implements OnInit {
  repoForm: FormGroup;
  repos: any[] = [];

  constructor(private fb: FormBuilder, private apiService: ApiService, private cdr: ChangeDetectorRef) {
    this.repoForm = this.fb.group({
      repoName: ['', Validators.required],
      githubToken: ['', Validators.required],
      branch: ['main']
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
      },
      error: (err) => {
        console.error('Failed to load repos', err);
      }
    });
  }

  onSubmit() {
    if (this.repoForm.valid) {
      this.apiService.addRepo(this.repoForm.value).subscribe({
        next: (res) => {
          this.repos.push(res);
          this.repoForm.reset({branch: 'main'});
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err)
      });
    }
  }
}
