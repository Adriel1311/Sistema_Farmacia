import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { ReportesRoutingModule } from './reportes-routing.module';
import { ReportesComponent } from './reportes.component';

@NgModule({
  declarations: [ReportesComponent],
  imports: [SharedModule, SvgIconComponent, ReportesRoutingModule]
})
export class ReportesModule {}
