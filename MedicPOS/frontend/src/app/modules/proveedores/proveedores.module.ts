import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { ProveedoresRoutingModule } from './proveedores-routing.module';
import { ProveedoresListComponent } from './proveedores-list.component';

@NgModule({
  declarations: [ProveedoresListComponent],
  imports: [SharedModule, SvgIconComponent, ProveedoresRoutingModule]
})
export class ProveedoresModule {}
