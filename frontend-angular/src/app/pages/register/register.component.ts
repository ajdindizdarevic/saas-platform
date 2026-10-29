import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

function passwordStrength(c: AbstractControl): ValidationErrors | null {
  const v = c.value as string;
  if (!v) return null;
  if (v.length < 8)                            return { weak: 'Min 8 characters' };
  if (!/[A-Za-z]/.test(v) || !/\d/.test(v))   return { weak: 'Must contain letters and numbers' };
  return null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="card">
        <!-- header -->
        <div class="card-header">
          <a routerLink="/login" class="back-link">← Sign in</a>
          <div class="logo"><span>S</span></div>
          <h1>Create account</h1>
          <p>Join your team on SaaSPlatform</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="submit()">

          <!-- row -->
          <div class="row-2">
            <div class="field">
              <label>First name</label>
              <input formControlName="firstName" placeholder="Jane" [class.err]="t('firstName')" />
              @if (t('firstName')) { <span class="errmsg">Required</span> }
            </div>
            <div class="field">
              <label>Last name</label>
              <input formControlName="lastName" placeholder="Smith" [class.err]="t('lastName')" />
              @if (t('lastName')) { <span class="errmsg">Required</span> }
            </div>
          </div>

          <div class="field">
            <label>Email</label>
            <input formControlName="email" type="email" placeholder="jane@company.com" [class.err]="t('email')" />
            @if (t('email')) { <span class="errmsg">Valid email required</span> }
          </div>

          <div class="field">
            <label>Username</label>
            <input formControlName="username" placeholder="jane_smith" [class.err]="t('username')" />
            @if (t('username')) { <span class="errmsg">3–50 chars, letters/digits/._-</span> }
          </div>

          <div class="field">
            <label>Password</label>
            <div class="pw-wrap">
              <input formControlName="password"
                     [type]="showPw() ? 'text' : 'password'"
                     placeholder="Min 8 chars"
                     [class.err]="t('password')" />
              <button type="button" class="eye" (click)="showPw.set(!showPw())">
                {{ showPw() ? '🙈' : '👁' }}
              </button>
            </div>
            @if (t('password')) {
              <span class="errmsg">{{ form.get('password')?.errors?.['weak'] ?? 'Required' }}</span>
            }
            <!-- strength bar -->
            @if (form.get('password')?.value) {
              <div class="strength-bar">
                <div class="bar" [style.width]="strength().pct" [style.background]="strength().color"></div>
              </div>
              <span class="strength-label" [style.color]="strength().color">{{ strength().label }}</span>
            }
          </div>

          <div class="field">
            <label>Phone <span class="opt">(optional)</span></label>
            <input formControlName="phoneNumber" placeholder="+1 555 000 0000" />
          </div>

          @if (errorMsg()) {
            <div class="alert">{{ errorMsg() }}</div>
          }
          @if (successMsg()) {
            <div class="alert success">{{ successMsg() }}</div>
          }

          <button class="btn" type="submit" [disabled]="loading()">
            @if (loading()) { <span class="spin"></span> } @else { Create account }
          </button>

        </form>

        <p class="login-link">Already have an account? <a routerLink="/login">Sign in</a></p>
      </div>
    </div>
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=Fraunces:wght@300;600&family=DM+Sans:wght@300;400;500&display=swap');

    :host { display:block; }

    .page {
      min-height: 100vh;
      background: #f4f4f5;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem 1rem;
      font-family: 'DM Sans', sans-serif;
    }

    .card {
      background: #fff;
      border-radius: 20px;
      padding: 2.5rem 2.5rem 2rem;
      width: 100%;
      max-width: 480px;
      box-shadow: 0 2px 40px #0000000a;
    }

    .card-header { text-align: center; margin-bottom: 1.75rem; }
    .back-link { font-size: .82rem; color: #71717a; text-decoration: none; display: block; margin-bottom: 1.25rem; text-align: left; }
    .back-link:hover { color: #4f46e5; }

    .logo {
      width: 44px; height: 44px;
      background: #0f0f11;
      color: #fff;
      border-radius: 12px;
      display: grid;
      place-items: center;
      font-weight: 700;
      font-size: 1.2rem;
      margin: 0 auto 1rem;
    }

    h1 {
      font-family: 'Fraunces', serif;
      font-size: 1.7rem;
      font-weight: 600;
      color: #0f0f11;
      margin: 0 0 .3rem;
    }
    p { color: #71717a; font-size: .88rem; margin: 0; }

    .row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }

    .field { margin-bottom: 1.1rem; }
    label {
      display: block;
      font-size: .8rem;
      font-weight: 500;
      color: #3f3f46;
      margin-bottom: .4rem;
    }
    .opt { font-weight: 400; color: #a1a1aa; }
    input {
      width: 100%;
      padding: .65rem .9rem;
      border: 1.5px solid #e4e4e7;
      border-radius: 9px;
      font-size: .88rem;
      font-family: inherit;
      color: #0f0f11;
      background: #fff;
      outline: none;
      transition: border-color .2s, box-shadow .2s;
      box-sizing: border-box;
    }
    input:focus { border-color: #4f46e5; box-shadow: 0 0 0 3px #4f46e51a; }
    input.err { border-color: #ef4444; }
    .errmsg { font-size: .73rem; color: #ef4444; margin-top: .25rem; display: block; }

    .pw-wrap { position: relative; }
    .pw-wrap input { padding-right: 2.8rem; }
    .eye { position: absolute; right: .7rem; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; padding: 0; font-size: .95rem; }

    .strength-bar {
      height: 3px; background: #e4e4e7; border-radius: 4px; margin-top: .5rem; overflow: hidden;
    }
    .bar { height: 100%; border-radius: 4px; transition: width .3s, background .3s; }
    .strength-label { font-size: .72rem; font-weight: 500; margin-top: .2rem; display: block; }

    .alert {
      background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;
      border-radius: 8px; padding: .65rem .9rem; font-size: .83rem; margin-bottom: 1rem;
    }
    .alert.success { background: #f0fdf4; border-color: #bbf7d0; color: #15803d; }

    .btn {
      width: 100%; padding: .78rem; background: #0f0f11; color: #fff; border: none;
      border-radius: 10px; font-size: .93rem; font-family: inherit; font-weight: 500;
      cursor: pointer; display: flex; align-items: center; justify-content: center;
      gap: .5rem; min-height: 44px; transition: background .2s;
    }
    .btn:hover:not(:disabled) { background: #27272a; }
    .btn:disabled { opacity: .5; cursor: not-allowed; }

    .spin {
      width: 16px; height: 16px; border: 2px solid #fff3; border-top-color: #fff;
      border-radius: 50%; animation: spin .7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .login-link { text-align: center; font-size: .84rem; color: #71717a; margin-top: 1.25rem; }
    .login-link a { color: #4f46e5; text-decoration: none; font-weight: 500; }
  `]
})
export class RegisterComponent {
  private fb     = inject(FormBuilder);
  private auth   = inject(AuthService);
  private router = inject(Router);

  showPw   = signal(false);
  loading  = signal(false);
  errorMsg = signal('');
  successMsg = signal('');

  form = this.fb.group({
    firstName:   ['', [Validators.required, Validators.minLength(2)]],
    lastName:    ['', [Validators.required, Validators.minLength(2)]],
    email:       ['', [Validators.required, Validators.email]],
    username:    ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z0-9._-]+$/)]],
    password:    ['', [Validators.required, passwordStrength]],
    phoneNumber: [''],
  });

  t(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c.touched);
  }

  strength(): { pct: string; color: string; label: string } {
    const v = this.form.get('password')?.value ?? '';
    if (v.length < 6)  return { pct: '25%',  color: '#ef4444', label: 'Weak' };
    if (v.length < 10) return { pct: '55%',  color: '#f59e0b', label: 'Fair' };
    if (!/[^a-zA-Z0-9]/.test(v)) return { pct: '75%', color: '#3b82f6', label: 'Good' };
    return { pct: '100%', color: '#22c55e', label: 'Strong' };
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');

    this.auth.register(this.form.value as any).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMsg.set('Account created! Redirecting to login…');
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'Registration failed. Please try again.');
      }
    });
  }
}
