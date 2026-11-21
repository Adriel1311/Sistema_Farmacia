import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { ConfiguracionRoutingModule } from './configuracion-routing.module';
import { ConfiguracionListComponent } from './configuracion-list.component';

@NgModule({
  declarations: [ConfiguracionListComponent],
  imports: [SharedModule, SvgIconComponent, ConfiguracionRoutingModule]
})
export class ConfiguracionModule {}
