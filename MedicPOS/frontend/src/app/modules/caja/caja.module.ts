import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { CajaRoutingModule } from './caja-routing.module';
import { CajaComponent } from './caja.component';

@NgModule({
  declarations: [CajaComponent],
  imports: [SharedModule, SvgIconComponent, CajaRoutingModule]
})
export class CajaModule {}
