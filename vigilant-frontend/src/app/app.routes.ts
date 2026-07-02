import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ConfigComponent } from './components/config/config.component';
import { RepoDetailComponent } from './components/repo-detail/repo-detail.component';

export const routes: Routes = [
    { path: '', component: DashboardComponent },
    { path: 'config', component: ConfigComponent },
    { path: 'repo/:id', component: RepoDetailComponent },
    { path: '**', redirectTo: '' }
];
