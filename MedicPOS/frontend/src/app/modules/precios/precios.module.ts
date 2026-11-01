import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { PreciosRoutingModule } from './precios-routing.module';
import { PreciosListComponent } from './precios-list.component';

@NgModule({
  declarations: [PreciosListComponent],
  imports: [SharedModule, SvgIconComponent, PreciosRoutingModule]
})
export class PreciosModule {}