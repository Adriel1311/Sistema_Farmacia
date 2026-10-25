import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { MedicamentosRoutingModule } from './medicamentos-routing.module';
import { MedicamentosListComponent } from './medicamentos-list.component';

@NgModule({
  declarations: [MedicamentosListComponent],
  imports: [SharedModule, SvgIconComponent, MedicamentosRoutingModule]
})
export class MedicamentosModule {}
