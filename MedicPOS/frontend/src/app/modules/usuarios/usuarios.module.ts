import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { UsuariosRoutingModule } from './usuarios-routing.module';
import { UsuariosListComponent } from './usuarios-list.component';

@NgModule({
  declarations: [UsuariosListComponent],
  imports: [SharedModule, SvgIconComponent, UsuariosRoutingModule]
})
export class UsuariosModule {}
