import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  getRecentBuilds(): Observable<any> {
    return this.http.get(`${this.baseUrl}/builds/recent`);
  }

  getRepoBuilds(repoId: number, page: number, size: number = 20): Observable<any> {
    return this.http.get(`${this.baseUrl}/builds/repo/${repoId}?page=${page}&size=${size}`);
  }

  getRepos(): Observable<any> {
    return this.http.get(`${this.baseUrl}/repos`);
  }

  addRepo(repo: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/repos`, repo);
  }

  updateRepo(id: number, data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/repos/${id}`, data);
  }
}
