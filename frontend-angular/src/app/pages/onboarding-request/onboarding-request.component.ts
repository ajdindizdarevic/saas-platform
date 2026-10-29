import { Component, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { OnboardingService } from '../../core/services/onboarding.service';
import { OnboardingResponse } from '../../core/models';

type Step = 1 | 2 | 3;

@Component({
  selector: 'app-onboarding-request',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <!-- Hero strip -->
      <header class="hero">
        <div class="hero-inner">
          <div class="brand"><span class="mark">S</span> SaaSPlatform</div>
          <a routerLink="/login" class="signin-link">Already have access? Sign in →</a>
        </div>
      </header>

      <main class="main">
        <div class="left-col">
          <h1>Request<br/><em>tenant access.</em></h1>
          <p>Fill in your organisation details. Our team reviews each request within one business day.</p>

          <ul class="perks">
            @for (p of perks; track p.icon) {
              <li>
                <span class="perk-icon">{{ p.icon }}</span>
                <span>{{ p.text }}</span>
              </li>
            }
          </ul>
        </div>

        <div class="right-col">
          <!-- Stepper -->
          <div class="stepper">
            @for (s of steps; track s.n) {
              <div class="step" [class.active]="currentStep() === s.n" [class.done]="currentStep() > s.n">
                <div class="step-circle">
                  @if (currentStep() > s.n) { ✓ } @else { {{ s.n }} }
                </div>
                <span>{{ s.label }}</span>
              </div>
              @if (!$last) { <div class="step-line" [class.done]="currentStep() > s.n"></div> }
            }
          </div>

          <!-- Step 1 – Company -->
          @if (currentStep() === 1) {
            <div class="form-section" @fadeIn>
              <h2>Company details</h2>
              <p class="step-desc">Tell us about your organisation.</p>

              <form [formGroup]="companyForm">
                <div class="field">
                  <label>Company name <span class="req">*</span></label>
                  <input formControlName="companyName" placeholder="Acme Corporation"
                         [class.err]="t(companyForm, 'companyName')" />
                  @if (t(companyForm, 'companyName')) { <span class="errmsg">Min 2 characters required</span> }
                </div>

                <div class="row-2">
                  <div class="field">
                    <label>Business type</label>
                    <select formControlName="businessType">
                      <option value="">Select type…</option>
                      @for (bt of businessTypes; track bt) { <option>{{ bt }}</option> }
                    </select>
                  </div>
                  <div class="field">
                    <label>Expected users</label>
                    <input formControlName="expectedUsers" type="number" min="1" placeholder="50" />
                  </div>
                </div>

                <div class="field">
                  <label>Additional notes</label>
                  <textarea formControlName="notes" rows="3"
                            placeholder="Tell us about your use case…"></textarea>
                </div>
              </form>
            </div>
          }

          <!-- Step 2 – Contact -->
          @if (currentStep() === 2) {
            <div class="form-section">
              <h2>Contact information</h2>
              <p class="step-desc">Who should we reach out to?</p>

              <form [formGroup]="contactForm">
                <div class="field">
                  <label>Full name <span class="req">*</span></label>
                  <input formControlName="contactName" placeholder="Jane Smith"
                         [class.err]="t(contactForm, 'contactName')" />
                  @if (t(contactForm, 'contactName')) { <span class="errmsg">Required</span> }
                </div>

                <div class="field">
                  <label>Work email <span class="req">*</span></label>
                  <input formControlName="contactEmail" type="email" placeholder="jane@acme.com"
                         [class.err]="t(contactForm, 'contactEmail')" />
                  @if (t(contactForm, 'contactEmail')) { <span class="errmsg">Valid email required</span> }
                </div>

                <div class="field">
                  <label>Phone number</label>
                  <input formControlName="phoneNumber" placeholder="+1 555 000 0000" />
                </div>
              </form>
            </div>
          }

          <!-- Step 3 – Review -->
          @if (currentStep() === 3 && !submitted()) {
            <div class="form-section">
              <h2>Review & submit</h2>
              <p class="step-desc">Confirm your details before submitting.</p>

              <div class="review-card">
                <div class="review-group">
                  <span class="review-label">Company</span>
                  <span class="review-value">{{ companyForm.value.companyName }}</span>
                </div>
                @if (companyForm.value.businessType) {
                  <div class="review-group">
                    <span class="review-label">Business type</span>
                    <span class="review-value">{{ companyForm.value.businessType }}</span>
                  </div>
                }
                @if (companyForm.value.expectedUsers) {
                  <div class="review-group">
                    <span class="review-label">Expected users</span>
                    <span class="review-value">{{ companyForm.value.expectedUsers }}</span>
                  </div>
                }
                <div class="divider-line"></div>
                <div class="review-group">
                  <span class="review-label">Contact name</span>
                  <span class="review-value">{{ contactForm.value.contactName }}</span>
                </div>
                <div class="review-group">
                  <span class="review-label">Email</span>
                  <span class="review-value">{{ contactForm.value.contactEmail }}</span>
                </div>
                @if (contactForm.value.phoneNumber) {
                  <div class="review-group">
                    <span class="review-label">Phone</span>
                    <span class="review-value">{{ contactForm.value.phoneNumber }}</span>
                  </div>
                }
              </div>

              @if (errorMsg()) {
                <div class="alert-err">{{ errorMsg() }}</div>
              }
            </div>
          }

          <!-- Success state -->
          @if (submitted()) {
            <div class="success-state">
              <div class="success-icon">✓</div>
              <h2>Request submitted!</h2>
              <p>We've received your request for <strong>{{ submittedCompany() }}</strong>.<br/>
                 Our team will review it and contact you at <strong>{{ submittedEmail() }}</strong>.</p>
              <div class="request-id">
                Request ID: <code>{{ requestId() }}</code>
              </div>
              <a routerLink="/login" class="btn-ghost">Back to sign in</a>
            </div>
          }

          <!-- Navigation -->
          @if (!submitted()) {
            <div class="nav-buttons">
              @if (currentStep() > 1) {
                <button class="btn-back" (click)="prevStep()">← Back</button>
              } @else {
                <div></div>
              }

              @if (currentStep() < 3) {
                <button class="btn-next" (click)="nextStep()">
                  Continue →
                </button>
              } @else {
                <button class="btn-submit" (click)="submit()" [disabled]="loading()">
                  @if (loading()) { <span class="spin"></span> Submitting… }
                  @else { Submit request }
                </button>
              }
            </div>
          }
        </div>
      </main>
    </div>
  `,
  styles: [`
    @import url('https://fonts.googleapis.com/css2?family=Fraunces:ital,wght@0,300;0,600;1,400&family=DM+Sans:wght@300;400;500&display=swap');

    :host { display: block; }

    .page {
      min-height: 100vh;
      background: #f8f8f9;
      font-family: 'DM Sans', sans-serif;
    }

    /* ── hero ──────────────────────────────────────────────────────────────── */
    .hero {
      background: #0f0f11;
      padding: 0 2rem;
    }
    .hero-inner {
      max-width: 1100px;
      margin: 0 auto;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: .55rem;
      color: #fff;
      font-weight: 500;
      font-size: .9rem;
    }
    .mark {
      width: 28px; height: 28px;
      background: #4f46e5;
      border-radius: 7px;
      display: grid; place-items: center;
      font-weight: 700; font-size: .9rem; color: #fff;
    }
    .signin-link {
      color: #a1a1aa; font-size: .82rem; text-decoration: none;
      transition: color .2s;
    }
    .signin-link:hover { color: #fff; }

    /* ── main layout ───────────────────────────────────────────────────────── */
    .main {
      max-width: 1100px;
      margin: 0 auto;
      padding: 4rem 2rem;
      display: grid;
      grid-template-columns: 1fr 1.4fr;
      gap: 4rem;
      align-items: start;
    }

    /* ── left col ──────────────────────────────────────────────────────────── */
    .left-col h1 {
      font-family: 'Fraunces', serif;
      font-size: clamp(2.4rem, 4vw, 3.4rem);
      font-weight: 300;
      line-height: 1.1;
      color: #0f0f11;
      margin: 0 0 1rem;
      letter-spacing: -.02em;
    }
    .left-col h1 em { font-style: italic; color: #4f46e5; }
    .left-col p { color: #71717a; line-height: 1.65; margin: 0 0 2.5rem; font-size: .95rem; }

    .perks { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: .9rem; }
    .perks li { display: flex; align-items: center; gap: .75rem; font-size: .88rem; color: #3f3f46; }
    .perk-icon { font-size: 1.1rem; }

    /* ── right col (form card) ─────────────────────────────────────────────── */
    .right-col {
      background: #fff;
      border-radius: 20px;
      padding: 2.5rem;
      box-shadow: 0 2px 40px #00000009;
    }

    /* ── stepper ───────────────────────────────────────────────────────────── */
    .stepper {
      display: flex;
      align-items: center;
      margin-bottom: 2.5rem;
    }
    .step {
      display: flex;
      align-items: center;
      gap: .5rem;
      font-size: .8rem;
      font-weight: 500;
      color: #a1a1aa;
      white-space: nowrap;
    }
    .step.active { color: #0f0f11; }
    .step.done   { color: #4f46e5; }
    .step-circle {
      width: 28px; height: 28px;
      border-radius: 50%;
      border: 2px solid #e4e4e7;
      display: grid; place-items: center;
      font-size: .8rem; font-weight: 600;
      flex-shrink: 0;
      transition: all .25s;
    }
    .step.active .step-circle { border-color: #0f0f11; background: #0f0f11; color: #fff; }
    .step.done   .step-circle { border-color: #4f46e5; background: #4f46e5; color: #fff; }
    .step-line { flex: 1; height: 2px; background: #e4e4e7; margin: 0 .5rem; transition: background .25s; }
    .step-line.done { background: #4f46e5; }

    /* ── form section ──────────────────────────────────────────────────────── */
    .form-section h2 {
      font-family: 'Fraunces', serif;
      font-size: 1.5rem;
      font-weight: 600;
      color: #0f0f11;
      margin: 0 0 .3rem;
    }
    .step-desc { color: #71717a; font-size: .87rem; margin: 0 0 1.75rem; }

    .field { margin-bottom: 1.1rem; }
    .row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }

    label {
      display: block;
      font-size: .79rem;
      font-weight: 500;
      color: #3f3f46;
      margin-bottom: .4rem;
    }
    .req { color: #ef4444; }

    input, select, textarea {
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
      resize: vertical;
    }
    input:focus, select:focus, textarea:focus {
      border-color: #4f46e5;
      box-shadow: 0 0 0 3px #4f46e51a;
    }
    input.err { border-color: #ef4444; }
    .errmsg { font-size: .73rem; color: #ef4444; margin-top: .25rem; display: block; }

    /* ── review card ───────────────────────────────────────────────────────── */
    .review-card {
      background: #fafafa;
      border: 1px solid #e4e4e7;
      border-radius: 12px;
      padding: 1.25rem 1.5rem;
      margin-bottom: 1.5rem;
    }
    .review-group {
      display: flex;
      justify-content: space-between;
      padding: .5rem 0;
      font-size: .87rem;
    }
    .review-label { color: #71717a; }
    .review-value { font-weight: 500; color: #0f0f11; }
    .divider-line { height: 1px; background: #e4e4e7; margin: .5rem 0; }

    .alert-err {
      background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;
      border-radius: 8px; padding: .7rem 1rem; font-size: .83rem; margin-bottom: 1rem;
    }

    /* ── navigation buttons ────────────────────────────────────────────────── */
    .nav-buttons {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 1.75rem;
      padding-top: 1.5rem;
      border-top: 1px solid #f4f4f5;
    }
    .btn-back {
      background: none; border: 1.5px solid #e4e4e7; color: #3f3f46;
      padding: .65rem 1.25rem; border-radius: 9px; font-family: inherit;
      font-size: .88rem; cursor: pointer; transition: border-color .2s, color .2s;
    }
    .btn-back:hover { border-color: #a1a1aa; color: #0f0f11; }

    .btn-next, .btn-submit {
      background: #0f0f11; color: #fff; border: none;
      padding: .68rem 1.75rem; border-radius: 9px; font-family: inherit;
      font-size: .9rem; font-weight: 500; cursor: pointer;
      display: flex; align-items: center; gap: .4rem;
      transition: background .2s;
    }
    .btn-next:hover, .btn-submit:hover:not(:disabled) { background: #27272a; }
    .btn-submit:disabled { opacity: .5; cursor: not-allowed; }
    .spin {
      width: 15px; height: 15px; border: 2px solid #fff4; border-top-color: #fff;
      border-radius: 50%; animation: spin .7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    /* ── success state ─────────────────────────────────────────────────────── */
    .success-state {
      text-align: center;
      padding: 1rem 0 .5rem;
    }
    .success-icon {
      width: 60px; height: 60px;
      background: #f0fdf4; border: 2px solid #bbf7d0;
      color: #22c55e; border-radius: 50%;
      font-size: 1.5rem; font-weight: 700;
      display: grid; place-items: center;
      margin: 0 auto 1.25rem;
    }
    .success-state h2 {
      font-family: 'Fraunces', serif;
      font-size: 1.75rem; font-weight: 600;
      color: #0f0f11; margin: 0 0 .75rem;
    }
    .success-state p { color: #52525b; font-size: .9rem; line-height: 1.6; margin: 0 0 1.25rem; }
    .request-id {
      background: #fafafa; border: 1px solid #e4e4e7; border-radius: 8px;
      padding: .65rem 1rem; font-size: .82rem; color: #71717a; margin-bottom: 1.5rem;
    }
    .request-id code { color: #4f46e5; font-weight: 600; }
    .btn-ghost {
      display: inline-block; padding: .65rem 1.5rem;
      border: 1.5px solid #e4e4e7; border-radius: 9px;
      color: #3f3f46; text-decoration: none; font-size: .88rem;
      transition: border-color .2s;
    }
    .btn-ghost:hover { border-color: #a1a1aa; }

    @media (max-width: 800px) {
      .main { grid-template-columns: 1fr; gap: 2rem; }
      .left-col h1 { font-size: 2rem; }
    }
  `]
})
export class OnboardingRequestComponent {
  private fb  = inject(FormBuilder);
  private svc = inject(OnboardingService);

  currentStep = signal<Step>(1);
  loading     = signal(false);
  errorMsg    = signal('');
  submitted   = signal(false);
  submittedCompany = signal('');
  submittedEmail   = signal('');
  requestId        = signal('');

  steps = [
    { n: 1, label: 'Company' },
    { n: 2, label: 'Contact' },
    { n: 3, label: 'Review'  },
  ];

  perks = [
    { icon: '🔒', text: 'Isolated PostgreSQL schema per tenant' },
    { icon: '⚡', text: 'Provisioned within minutes of approval' },
    { icon: '👥', text: 'Unlimited users on the Enterprise plan' },
    { icon: '🛟', text: 'Dedicated support from day one'         },
  ];

  businessTypes = ['SaaS', 'E-commerce', 'Healthcare', 'Education', 'Finance', 'Logistics', 'Other'];

  companyForm = this.fb.group({
    companyName:   ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
    businessType:  [''],
    expectedUsers: [null as number | null],
    notes:         [''],
  });

  contactForm = this.fb.group({
    contactName:  ['', [Validators.required, Validators.minLength(2)]],
    contactEmail: ['', [Validators.required, Validators.email]],
    phoneNumber:  [''],
  });

  t(form: any, field: string): boolean {
    const c = form.get(field);
    return !!(c?.invalid && c.touched);
  }

  nextStep(): void {
    if (this.currentStep() === 1) {
      this.companyForm.markAllAsTouched();
      if (this.companyForm.invalid) return;
    }
    if (this.currentStep() === 2) {
      this.contactForm.markAllAsTouched();
      if (this.contactForm.invalid) return;
    }
    this.currentStep.set((this.currentStep() + 1) as Step);
  }

  prevStep(): void {
    this.currentStep.set((this.currentStep() - 1) as Step);
  }

  submit(): void {
    if (this.companyForm.invalid || this.contactForm.invalid) return;

    this.loading.set(true);
    this.errorMsg.set('');

    const payload = {
      companyName:   this.companyForm.value.companyName!,
      contactEmail:  this.contactForm.value.contactEmail!,
      contactName:   this.contactForm.value.contactName!,
      phoneNumber:   this.contactForm.value.phoneNumber || undefined,
      businessType:  this.companyForm.value.businessType || undefined,
      expectedUsers: this.companyForm.value.expectedUsers || undefined,
      notes:         this.companyForm.value.notes || undefined,
    };

    this.svc.submitRequest(payload).subscribe({
      next: res => {
        this.loading.set(false);
        this.submittedCompany.set(res.data.companyName);
        this.submittedEmail.set(res.data.contactEmail);
        this.requestId.set(res.data.id);
        this.submitted.set(true);
      },
      error: err => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'Submission failed. Please try again.');
      }
    });
  }
}
