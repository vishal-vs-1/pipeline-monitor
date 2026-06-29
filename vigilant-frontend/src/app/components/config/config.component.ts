import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
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
      error: (err: any) => {
        console.error('Failed to load repos', err);
      }
    });
  }

  onSubmit() {
    if (this.repoForm.valid) {
      this.apiService.addRepo(this.repoForm.value).subscribe({
        next: (res: any) => {
          this.repos.push(res);
          this.repoForm.reset({branch: 'main'});
          this.cdr.detectChanges();
        },
        error: (err: any) => console.error(err)
      });
    }
  }
}
