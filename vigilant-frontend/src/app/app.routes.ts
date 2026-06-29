import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ConfigComponent } from './config/config.component';

export const routes: Routes = [
    { path: '', component: DashboardComponent },
    { path: 'config', component: ConfigComponent },
    { path: '**', redirectTo: '' }
];
