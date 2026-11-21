import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { InventarioRoutingModule } from './inventario-routing.module';
import { InventarioListComponent } from './inventario-list.component';

@NgModule({
  declarations: [InventarioListComponent],
  imports: [SharedModule, SvgIconComponent, InventarioRoutingModule]
})
export class InventarioModule {}
