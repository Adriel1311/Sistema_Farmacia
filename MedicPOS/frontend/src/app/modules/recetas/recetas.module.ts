import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { RecetasRoutingModule } from './recetas-routing.module';
import { RecetasListComponent } from './recetas-list.component';

@NgModule({
  declarations: [RecetasListComponent],
  imports: [SharedModule, SvgIconComponent, RecetasRoutingModule]
})
export class RecetasModule {}
