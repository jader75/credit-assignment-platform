import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, tap } from 'rxjs';

import { LoginRequest, LoginResponse } from './models';

const STORAGE_KEY = 'credit-engine-auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly sessionSubject = new BehaviorSubject<LoginResponse | null>(this.restoreSession());
  private readonly baseUrl = 'http://localhost:8080';

  login(request: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, request).pipe(
      tap((response) => {
        this.sessionSubject.next(response);
        localStorage.setItem(STORAGE_KEY, JSON.stringify(response));
      }),
    );
  }

  logout(): void {
    this.sessionSubject.next(null);
    localStorage.removeItem(STORAGE_KEY);
  }

  session(): LoginResponse | null {
    return this.sessionSubject.value;
  }

  accessToken(): string {
    return this.session()?.accessToken ?? '';
  }

  isAuthenticated(): boolean {
    return Boolean(this.accessToken());
  }

  username(): string {
    return this.session()?.subject ?? '';
  }

  roles(): string[] {
    return this.session()?.roles ?? [];
  }

  private restoreSession(): LoginResponse | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return null;
      }
      const session = JSON.parse(raw) as LoginResponse;
      return session.accessToken ? session : null;
    } catch {
      return null;
    }
  }
}
