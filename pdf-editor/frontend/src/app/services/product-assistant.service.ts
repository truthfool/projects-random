import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AskResponse {
  allowed: boolean;
  answer?: string;
  reason?: string;
}

@Injectable({ providedIn: 'root' })
export class ProductAssistantService {
  private readonly baseUrl = 'http://localhost:8085/api';

  constructor(private http: HttpClient) {}

  ask(question: string): Observable<AskResponse> {
    return this.http.post<AskResponse>(`${this.baseUrl}/ask`, { question });
  }
}


