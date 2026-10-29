import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="shell">
      <!-- left panel -->
      <aside class="panel-left">
        <div class="brand">
          <span class="brand-mark">S</span>
          <span class="brand-name">SaaSPlatform</span>
        </div>
        <div class="panel-copy">
          <h1>Welcome<br>back.</h1>
          <p>Sign in to access your workspace and manage your organisation.</p>
        </div>
        <div class="panel-footer">Multi-tenant SaaS Platform &copy; 2026</div>
      </aside>

      <!-- right panel -->
      <main class="panel-right">
        <div class="form-card">
          <h2>Sign in</h2>
          <p class="subtitle">Enter your credentials to continue</p>

          <form [formGroup]="form" (ngSubmit)="submit()" novalidate>

            <div class="field">
              <label>Username or Email</label>
              <input formControlName="usernameOrEmail" type="text"
                     placeholder="you@company.com"
                     [class.invalid]="isTouched('usernameOrEmail')" />
              @if (isTouched('usernameOrEmail')) {
                <span class="err">This field is required</span>
              }
            </div>

            <div class="field">
              <label>
                Password
                <a href="#" class="forgot">Forgot?</a>
              </label>
              <div class="pw-wrap">
                <input formControlName="password"
                       [type]="showPw() ? 'text' : 'password'"
                       placeholder="••••••••"
                       [class.invalid]="isTouched('password')" />
                <button type="button" class="pw-toggle" (click)="showPw.set(!showPw())">
                  {{ showPw() ? '🙈' : '👁' }}
                </button>
              </div>
              @if (isTouched('password')) {
                <span class="err">This field is required</span>
              }
            </div>

            @if (errorMsg()) {
              <div class="alert-error">{{ errorMsg() }}</div>
            }

            <button class="btn-primary" type="submit" [disabled]="loading()">
              @if (loading()) { <span class="spinner"></span> } @else { Sign in }
            </button>
          </form>

          <div class="divider"><span>or</span></div>

          <div class="links">
            <span>New to the platform?</span>
            <a routerLink="/onboarding">Request access</a>
          </div>
        </div>
      </main>
    </div>
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=Fraunces:ital,wght@0,300;0,600;1,300&family=DM+Sans:wght@300;400;500&display=swap');

    :host { display:block; height:100vh; }

    .shell {
      display: grid;
      grid-template-columns: 42% 1fr;
      height: 100vh;
      font-family: 'DM Sans', sans-serif;
    }

    /* ── left panel ── */
    .panel-left {
      background: #0f0f11;
      color: #fff;
      display: flex;
      flex-direction: column;
      padding: 3rem;
      position: relative;
      overflow: hidden;
    }
    .panel-left::before {
      content: '';
      position: absolute;
      width: 500px; height: 500px;
      border-radius: 50%;
      background: radial-gradient(circle, #4f46e510 0%, transparent 70%);
      bottom: -100px; left: -100px;
      pointer-events: none;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: .6rem;
      font-weight: 500;
      font-size: .9rem;
      letter-spacing: .04em;
    }
    .brand-mark {
      width: 32px; height: 32px;
      background: #4f46e5;
      color: #fff;
      border-radius: 8px;
      display: grid;
      place-items: center;
      font-weight: 700;
      font-size: 1rem;
    }
    .brand-name { color: #e5e5e5; }

    .panel-copy {
      margin-top: auto;
      margin-bottom: 3rem;
    }
    .panel-copy h1 {
      font-family: 'Fraunces', serif;
      font-size: clamp(3rem, 5vw, 4.5rem);
      font-weight: 300;
      line-height: 1.05;
      margin: 0 0 1.2rem;
      letter-spacing: -.02em;
    }
    .panel-copy p {
      color: #a0a0b0;
      font-size: .95rem;
      line-height: 1.65;
      max-width: 300px;
    }
    .panel-footer {
      color: #3a3a4a;
      font-size: .75rem;
    }

    /* ── right panel ── */
    .panel-right {
      background: #fafafa;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
    }
    .form-card {
      width: 100%;
      max-width: 420px;
    }
    h2 {
      font-family: 'Fraunces', serif;
      font-size: 2rem;
      font-weight: 600;
      color: #0f0f11;
      margin: 0 0 .4rem;
    }
    .subtitle {
      color: #71717a;
      font-size: .9rem;
      margin: 0 0 2rem;
    }

    /* ── form fields ── */
    .field { margin-bottom: 1.25rem; }
    label {
      display: flex;
      justify-content: space-between;
      font-size: .8rem;
      font-weight: 500;
      color: #3f3f46;
      margin-bottom: .45rem;
    }
    input {
      width: 100%;
      padding: .7rem 1rem;
      border: 1.5px solid #e4e4e7;
      border-radius: 10px;
      font-size: .9rem;
      font-family: inherit;
      background: #fff;
      color: #0f0f11;
      transition: border-color .2s, box-shadow .2s;
      box-sizing: border-box;
      outline: none;
    }
    input:focus { border-color: #4f46e5; box-shadow: 0 0 0 3px #4f46e520; }
    input.invalid { border-color: #ef4444; }
    .err { font-size: .75rem; color: #ef4444; margin-top: .3rem; display: block; }
    .forgot { color: #4f46e5; font-weight: 400; text-decoration: none; font-size: .8rem; }

    .pw-wrap { position: relative; }
    .pw-wrap input { padding-right: 3rem; }
    .pw-toggle {
      position: absolute; right: .75rem; top: 50%;
      transform: translateY(-50%);
      background: none; border: none; cursor: pointer; font-size: 1rem; padding: 0;
    }

    .alert-error {
      background: #fef2f2;
      border: 1px solid #fecaca;
      color: #dc2626;
      border-radius: 8px;
      padding: .7rem 1rem;
      font-size: .85rem;
      margin-bottom: 1rem;
    }

    .btn-primary {
      width: 100%;
      padding: .8rem;
      background: #0f0f11;
      color: #fff;
      border: none;
      border-radius: 10px;
      font-size: .95rem;
      font-family: inherit;
      font-weight: 500;
      cursor: pointer;
      transition: background .2s, transform .1s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: .5rem;
      min-height: 46px;
    }
    .btn-primary:hover:not(:disabled) { background: #27272a; }
    .btn-primary:active { transform: scale(.98); }
    .btn-primary:disabled { opacity: .5; cursor: not-allowed; }

    .spinner {
      width: 18px; height: 18px;
      border: 2px solid #fff3;
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin .7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .divider {
      text-align: center;
      position: relative;
      margin: 1.5rem 0;
      color: #d4d4d8;
      font-size: .8rem;
    }
    .divider::before, .divider::after {
      content: '';
      position: absolute;
      top: 50%;
      width: calc(50% - 20px);
      height: 1px;
      background: #e4e4e7;
    }
    .divider::before { left: 0; }
    .divider::after  { right: 0; }

    .links {
      text-align: center;
      font-size: .85rem;
      color: #71717a;
      display: flex;
      gap: .5rem;
      justify-content: center;
      flex-wrap: wrap;
    }
    .links a { color: #4f46e5; text-decoration: none; font-weight: 500; }

    @media (max-width: 700px) {
      .shell { grid-template-columns: 1fr; }
      .panel-left { display: none; }
    }
  `]
})
export class LoginComponent {
  private fb   = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    usernameOrEmail: ['', Validators.required],
    password:        ['', Validators.required],
  });

  loading  = signal(false);
  errorMsg = signal('');
  showPw   = signal(false);

  isTouched(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c.touched);
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');

    this.auth.login(this.form.value as any).subscribe({
      next: () => {
        this.loading.set(false);
        // SUPER_ADMIN → full dashboard; ADMIN → users page only
        const dest = this.auth.isSuperAdmin() ? '/dashboard' : '/users';
        this.router.navigate([dest]);
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'Invalid credentials. Please try again.');
      }
    });
  }
}
