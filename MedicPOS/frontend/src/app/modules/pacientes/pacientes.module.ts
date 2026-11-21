import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { PacientesRoutingModule } from './pacientes-routing.module';
import { PacientesListComponent } from './pacientes-list.component';

@NgModule({
  declarations: [PacientesListComponent],
  imports: [SharedModule, SvgIconComponent, PacientesRoutingModule]
})
export class PacientesModule {}
