import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, FormControl, ValidationErrors, Validators } from '@angular/forms';
import { Observable, Subject, merge, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, startWith, switchMap, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { CatalogoService } from '../../core/services/catalogo.service';
import { AuthService } from '../../core/services/auth.service';
import { CreateMedicamentoRequest, Medicamento, Proveedor, UpdateMedicamentoRequest } from '../../core/models/catalogo.model';
import { UserRole } from '../../core/models/user.model';

type ModoFormulario = 'alta' | 'edicion';

function rangoStockValido(control: AbstractControl): ValidationErrors | null {
  const minimo = Number(control.get('stockMinimo')?.value ?? 0);
  const maximo = Number(control.get('stockMaximo')?.value ?? 0);
  if (!Number.isFinite(minimo) || !Number.isFinite(maximo)) {
    return null;
  }
  return maximo >= minimo ? null : { stockMaximoMenorAlMinimo: true };
}

@Component({
  selector: 'app-medicamentos-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './medicamentos-list.component.html',
  styleUrls: ['./medicamentos-list.component.scss']
})
export class MedicamentosListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly catalogo = inject(CatalogoService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly unidades = ['UNIDAD', 'CAJA', 'FRASCO', 'BLISTER', 'TUBO', 'FRASCO X ML'];

  readonly puedeEditar = this.auth.hasRole(UserRole.JEFE_FARMACIA);

  readonly medicamentos = signal<Medicamento[]>([]);
  readonly proveedores = signal<Proveedor[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly soloCriticos = signal(false);
  readonly proveedorSeleccionado = signal(0);
  readonly objetivo = signal<Medicamento | null>(null);
  readonly medicamentoCodigo = signal<Medicamento | null>(null);

  readonly modalAbierto = signal(false);
  readonly modo = signal<ModoFormulario>('alta');
  readonly guardando = signal(false);
  readonly formError = signal('');
  readonly edicion = signal<Medicamento | null>(null);
  readonly tituloModal = computed(() => (this.modo() === 'alta' ? 'Nuevo medicamento' : 'Editar medicamento'));

  readonly searchControl = new FormControl('', { nonNullable: true });

  readonly form = this.fb.nonNullable.group(
    {
      codigo: ['', [Validators.required, Validators.maxLength(20)]],
      nombre: ['', [Validators.required, Validators.maxLength(120)]],
      descripcion: ['', [Validators.maxLength(500)]],
      unidadMedida: ['UNIDAD', [Validators.required]],
      proveedorId: [0, [Validators.required, Validators.min(1)]],
      stockMinimo: [0, [Validators.required, Validators.min(0)]],
      stockMaximo: [0, [Validators.required, Validators.min(0)]]
    },
    { validators: rangoStockValido }
  );

  readonly visibles = computed(() => {
    const proveedorId = this.proveedorSeleccionado();
    const lista = this.medicamentos();
    return proveedorId > 0 ? lista.filter(item => item.proveedorId === proveedorId) : lista;
  });

  readonly criticos = computed(() => this.visibles().filter(item => item.stockCritico).length);
  readonly inactivos = computed(() => this.visibles().filter(item => !item.activo).length);

  private readonly reload$ = new Subject<void>();

  constructor() {
    merge(
      this.reload$,
      this.searchControl.valueChanges.pipe(distinctUntilChanged(), debounceTime(300), map(() => undefined))
    )
      .pipe(startWith(undefined), switchMap(() => this.cargarMedicamentos()), takeUntilDestroyed(this.destroyRef))
      .subscribe();

    this.catalogo
      .getProveedores(false)
      .pipe(
        catchError(() => of([] as Proveedor[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(lista => this.proveedores.set(lista));
  }

  refrescar(): void {
    this.limpiarMensajes();
    this.reload$.next();
  }

  private cargarMedicamentos(): Observable<Medicamento[]> {
    this.loading.set(true);
    this.error.set('');
    const texto = this.searchControl.value.trim();
    const soloCriticos = this.soloCriticos();

    const request$ = soloCriticos
      ? this.catalogo.getMedicamentosStockCritico()
      : texto.length > 0
        ? this.catalogo.buscarMedicamentos(texto)
        : this.catalogo.getMedicamentos(false);

    return request$.pipe(
      catchError((err: Error) => {
        this.error.set(err.message || 'No se pudieron cargar los medicamentos.');
        return of([] as Medicamento[]);
      }),
      finalize(() => this.loading.set(false)),
      tap(lista => this.medicamentos.set(lista))
    );
  }

  limpiarMensajes(): void {
    this.success.set('');
    this.error.set('');
  }

  alternarSoloCriticos(): void {
    this.soloCriticos.update(valor => !valor);
    this.refrescar();
  }

  cambiarProveedor(valor: string): void {
    this.proveedorSeleccionado.set(Number(valor) || 0);
  }

  abrirAlta(): void {
    this.modo.set('alta');
    this.edicion.set(null);
    this.formError.set('');
    this.form.reset({
      codigo: '',
      nombre: '',
      descripcion: '',
      unidadMedida: 'UNIDAD',
      proveedorId: 0,
      stockMinimo: 0,
      stockMaximo: 0
    });
    this.modalAbierto.set(true);
  }

  abrirEdicion(medicamento: Medicamento): void {
    if (!this.puedeEditar) {
      return;
    }
    this.modo.set('edicion');
    this.edicion.set(medicamento);
    this.formError.set('');
    this.form.reset({
      codigo: medicamento.codigo,
      nombre: medicamento.nombre,
      descripcion: medicamento.descripcion,
      unidadMedida: medicamento.unidadMedida,
      proveedorId: medicamento.proveedorId,
      stockMinimo: medicamento.stockMinimo,
      stockMaximo: medicamento.stockMaximo
    });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.formError.set('');
  }

  /** Abre la etiqueta con el codigo de barras del medicamento. */
  abrirCodigo(medicamento: Medicamento): void {
    this.medicamentoCodigo.set(medicamento);
  }

  cerrarCodigo(): void {
    this.medicamentoCodigo.set(null);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const original = this.edicion();
    const base: CreateMedicamentoRequest = {
      codigo: valor.codigo.trim(),
      nombre: valor.nombre.trim(),
      descripcion: valor.descripcion.trim(),
      unidadMedida: valor.unidadMedida,
      proveedorId: Number(valor.proveedorId),
      stockMinimo: Number(valor.stockMinimo),
      stockMaximo: Number(valor.stockMaximo)
    };

    const esEdicion = this.modo() === 'edicion' && original !== null;
    const payload: UpdateMedicamentoRequest | CreateMedicamentoRequest = esEdicion
      ? { ...base, activo: original.activo }
      : base;

    this.guardando.set(true);
    this.formError.set('');

    const request$: Observable<Medicamento> = esEdicion && original
      ? this.catalogo.updateMedicamento(original.id, payload as UpdateMedicamentoRequest)
      : this.catalogo.createMedicamento(payload as CreateMedicamentoRequest);

    request$
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: guardado => {
          this.cerrarModal();
          this.success.set(`${guardado.nombre} ${esEdicion ? 'actualizado' : 'registrado'} correctamente.`);
          this.reload$.next();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo guardar el medicamento.')
      });
  }

  pedirToggle(medicamento: Medicamento): void {
    if (!this.puedeEditar) {
      return;
    }
    this.objetivo.set(medicamento);
  }

  cancelarToggle(): void {
    this.objetivo.set(null);
  }

  confirmarToggle(): void {
    const objetivo = this.objetivo();
    if (!objetivo) {
      return;
    }

    this.guardando.set(true);
    this.catalogo
      .toggleMedicamentoActivo(objetivo.id)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: actualizado => {
          this.objetivo.set(null);
          this.success.set(`${actualizado.nombre} ${actualizado.activo ? 'activado' : 'desactivado'} correctamente.`);
          this.reload$.next();
        },
        error: (err: Error) => {
          this.objetivo.set(null);
          this.error.set(err.message || 'No se pudo cambiar el estado del medicamento.');
        }
      });
  }

  trackById(_index: number, medicamento: Medicamento): number {
    return medicamento.id;
  }
}
