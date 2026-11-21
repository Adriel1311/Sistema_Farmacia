import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { FmLoadingComponent } from './components/fm-loading/fm-loading.component';
import { FmEmptyComponent } from './components/fm-empty/fm-empty.component';
import { FmModalComponent } from './components/fm-modal/fm-modal.component';
import { FmConfirmComponent } from './components/fm-confirm/fm-confirm.component';
import { FmSemaforoComponent } from './components/fm-semaforo/fm-semaforo.component';
import { CodigoBarrasComponent } from './components/codigo-barras/codigo-barras.component';
import { FmDatePipe, FmDiasRestantesPipe } from './pipes/fm-date.pipe';

const COMPONENTS = [
  FmLoadingComponent,
  FmEmptyComponent,
  FmModalComponent,
  FmConfirmComponent,
  FmSemaforoComponent,
  CodigoBarrasComponent
];

const PIPES = [FmDatePipe, FmDiasRestantesPipe];

@NgModule({
  declarations: [...COMPONENTS, ...PIPES],
  imports: [CommonModule, ReactiveFormsModule],
  exports: [CommonModule, ReactiveFormsModule, ...COMPONENTS, ...PIPES]
})
export class SharedModule {}
