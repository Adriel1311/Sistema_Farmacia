import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PreciosListComponent } from './precios-list.component';

const routes: Routes = [
  { path: '', component: PreciosListComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PreciosRoutingModule {}