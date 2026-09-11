import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './view/_LOGIN/login/login.component';
import { DashboardComponent } from './view/_DASHBOARD/dashboard/dashboard.component';
import { ContactComponent } from './view/_STATIC/contact/contact.component';
import { QuestionComponent } from './view/_STATIC/question/question.component';
import { ProfileComponent } from './view/_STATIC/profile/profile.component';
import { CorsiComponent } from './view/_CORSI/corsi/corsi.component';
import { DettaglioCorsoComponent } from './view/_CORSI/dettaglio-corso/dettaglio-corso.component';

import { DocumentiComponent } from './view/_DOCUMENTI/documenti/documenti.component';
import { DipendentiComponent } from './view/_DIPENDENTI/dipendenti/dipendenti.component';
import { DipendentiDettagliComponent } from './view/_DIPENDENTI/dipendenti-dettagli/dipendenti-dettagli.component';
import { AuthGuard } from './auth.guard';
import { AziendeComponent } from './view/_AZIENDE/aziende/aziende.component';
import { RicercaComponent } from './view/_RICERCA/ricerca/ricerca.component';
import { NotFoundComponent } from './view/GENERIC/not-found/not-found.component';
import { NotAuthorizedComponent } from './view/GENERIC/not-authorized/not-authorized.component';
import { CopyrightComponent } from './view/_STATIC/copyright/copyright.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  
  { path: '', component: DashboardComponent, canActivate: [AuthGuard] },

  { path: 'aziende', component: AziendeComponent, canActivate: [AuthGuard] },

  { path: 'documenti', component: DocumentiComponent, canActivate: [AuthGuard] },

  { path: 'dipendenti', component: DipendentiComponent, canActivate: [AuthGuard] },
  { path: 'dipendenti/:id', component: DipendentiDettagliComponent, canActivate: [AuthGuard] },

  { path: 'corsi', component: CorsiComponent, canActivate: [AuthGuard] },
  { path: 'corsi/:id', component:DettaglioCorsoComponent, canActivate: [AuthGuard] },

  { path: 'contact', component: ContactComponent, canActivate: [AuthGuard] },
  { path: 'question', component: QuestionComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'copyright', component: CopyrightComponent, canActivate: [AuthGuard]},

  { path: 'search/:text', component: RicercaComponent, canActivate: [AuthGuard] },

  { path: '401', component: NotAuthorizedComponent },
  { path: '404', component: NotFoundComponent },
  { path: '**', redirectTo: ''}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {scrollPositionRestoration: 'enabled'})],
  exports: [RouterModule]
})

export class AppRoutingModule { }
