import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { AlertasRoutingModule } from './alertas-routing.module';
import { AlertasListComponent } from './alertas-list.component';

@NgModule({
  declarations: [AlertasListComponent],
  imports: [SharedModule, SvgIconComponent, AlertasRoutingModule]
})
export class AlertasModule {}
