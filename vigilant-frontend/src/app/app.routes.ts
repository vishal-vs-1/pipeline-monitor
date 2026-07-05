import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ConfigComponent } from './components/config/config.component';
import { RepoDetailComponent } from './components/repo-detail/repo-detail.component';
import { Login } from './components/login/login';
import { Oauth2Callback } from './components/oauth2-callback/oauth2-callback';
import { AuthGuard } from './services/auth.guard';

export const routes: Routes = [
    { path: 'login', component: Login },
    { path: 'oauth2/callback', component: Oauth2Callback },
    { path: '', component: DashboardComponent, canActivate: [AuthGuard] },
    { path: 'config', component: ConfigComponent, canActivate: [AuthGuard] },
    { path: 'repo/:id', component: RepoDetailComponent, canActivate: [AuthGuard] },
    { path: '**', redirectTo: '' }
];
