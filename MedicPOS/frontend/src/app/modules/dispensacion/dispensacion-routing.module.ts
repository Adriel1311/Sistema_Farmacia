import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DispensacionComponent } from './dispensacion.component';

const routes: Routes = [{ path: '', component: DispensacionComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DispensacionRoutingModule {}
