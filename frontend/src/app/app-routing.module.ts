import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './view/_LOGIN/login/login.component';
import { DashboardComponent } from './view/_DASHBOARD/dashboard/dashboard.component';
import { ContactComponent } from './view/_STATIC/contact/contact.component';

import { DocumentiComponent } from './view/_DOCUMENTI/documenti/documenti.component';
import { DipendentiComponent } from './view/_DIPENDENTI/dipendenti/dipendenti.component';
import { DipendentiDettagliComponent } from './view/_DIPENDENTI/dipendenti-dettagli/dipendenti-dettagli.component';
import { AuthGuard } from './auth.guard';
import { AziendeComponent } from './view/_AZIENDE/aziende/aziende.component';
import { RicercaComponent } from './view/_RICERCA/ricerca/ricerca.component';
import { NotFoundComponent } from './view/GENERIC/not-found/not-found.component';
import { NotAuthorizedComponent } from './view/GENERIC/not-authorized/not-authorized.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },

  { path: '', component: DashboardComponent, canActivate: [AuthGuard] },

  { path: 'aziende', component: AziendeComponent, canActivate: [AuthGuard] },

  { path: 'documenti', component: DocumentiComponent, canActivate: [AuthGuard] },

  { path: 'dipendenti', component: DipendentiComponent, canActivate: [AuthGuard] },
  { path: 'dipendenti/:id', component: DipendentiDettagliComponent, canActivate: [AuthGuard] },

  { path: 'contact', component: ContactComponent, canActivate: [AuthGuard] },

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
