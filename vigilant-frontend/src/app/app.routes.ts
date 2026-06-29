import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ConfigComponent } from './components/config/config.component';

export const routes: Routes = [
    { path: '', component: DashboardComponent },
    { path: 'config', component: ConfigComponent },
    { path: '**', redirectTo: '' }
];
