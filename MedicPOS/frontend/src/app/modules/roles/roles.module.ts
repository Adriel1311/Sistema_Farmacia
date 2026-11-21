import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { RolesRoutingModule } from './roles-routing.module';
import { RolesListComponent } from './roles-list.component';

@NgModule({
  declarations: [RolesListComponent],
  imports: [SharedModule, SvgIconComponent, RolesRoutingModule]
})
export class RolesModule {}
