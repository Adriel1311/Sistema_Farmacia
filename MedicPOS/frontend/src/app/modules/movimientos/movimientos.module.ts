import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { MovimientosRoutingModule } from './movimientos-routing.module';
import { MovimientosListComponent } from './movimientos-list.component';

@NgModule({
  declarations: [MovimientosListComponent],
  imports: [SharedModule, SvgIconComponent, MovimientosRoutingModule]
})
export class MovimientosModule {}
