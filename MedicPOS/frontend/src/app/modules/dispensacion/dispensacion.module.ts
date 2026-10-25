import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { DispensacionRoutingModule } from './dispensacion-routing.module';
import { DispensacionComponent } from './dispensacion.component';

@NgModule({
  declarations: [DispensacionComponent],
  imports: [SharedModule, SvgIconComponent, DispensacionRoutingModule]
})
export class DispensacionModule {}
