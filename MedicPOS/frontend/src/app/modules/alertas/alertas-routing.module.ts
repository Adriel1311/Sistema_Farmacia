import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AlertasListComponent } from './alertas-list.component';

const routes: Routes = [{ path: '', component: AlertasListComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AlertasRoutingModule {}
