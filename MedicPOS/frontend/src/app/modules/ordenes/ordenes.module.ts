import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { OrdenesRoutingModule } from './ordenes-routing.module';
import { OrdenesComponent } from './ordenes.component';

@NgModule({
  declarations: [OrdenesComponent],
  imports: [SharedModule, SvgIconComponent, OrdenesRoutingModule]
})
export class OrdenesModule {}
