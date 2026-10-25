import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { BajasRoutingModule } from './bajas-routing.module';
import { BajasListComponent } from './bajas-list.component';

@NgModule({
  declarations: [BajasListComponent],
  imports: [SharedModule, SvgIconComponent, BajasRoutingModule]
})
export class BajasModule {}
