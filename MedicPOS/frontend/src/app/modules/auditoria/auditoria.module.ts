import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AuditoriaRoutingModule } from './auditoria-routing.module';
import { AuditoriaListComponent } from './auditoria-list.component';

@NgModule({
  declarations: [AuditoriaListComponent],
  imports: [SharedModule, SvgIconComponent, AuditoriaRoutingModule]
})
export class AuditoriaModule {}
