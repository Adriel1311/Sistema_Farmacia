import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { MedicosRoutingModule } from './medicos-routing.module';
import { MedicosListComponent } from './medicos-list.component';

@NgModule({
  declarations: [MedicosListComponent],
  imports: [SharedModule, SvgIconComponent, MedicosRoutingModule]
})
export class MedicosModule {}
