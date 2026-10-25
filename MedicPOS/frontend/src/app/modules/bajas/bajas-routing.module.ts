import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BajasListComponent } from './bajas-list.component';

const routes: Routes = [{ path: '', component: BajasListComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BajasRoutingModule {}
