import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, Validators, AbstractControl, AsyncValidatorFn } from '@angular/forms';
import { Observable, Subject, merge, of, fromEvent } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, startWith, switchMap, tap, filter, take } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { PacienteRecetaService } from '../../core/services/paciente-receta.service';
import { DniService, DniResponse } from '../../core/services/dni.service';
import { CreatePacienteRequest, Paciente } from '../../core/models/receta.model';

@Component({
  selector: 'app-pacientes-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pacientes-list.component.html',
  styleUrls: ['./pacientes-list.component.scss']
})
export class PacientesListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PacienteRecetaService);
  private readonly dniService = inject(DniService);
  private readonly destroyRef = inject(DestroyRef);

  @ViewChild('dniInput') dniInput!: ElementRef<HTMLInputElement>;

  readonly pacientes = signal<Paciente[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly modalAbierto = signal(false);
  readonly guardando = signal(false);
  readonly formError = signal('');
  readonly consultandoDni = signal(false);
  readonly dniConsultadoExitosamente = signal(false);

  readonly searchControl = new FormControl('', { nonNullable: true });

  readonly form = this.fb.nonNullable.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)], [this.dniAsyncValidator()]],
    nombres: ['', [Validators.required, Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.maxLength(100)]]
  });

  private readonly reload$ = new Subject<void>();

  constructor() {
    merge(this.reload$, this.searchControl.valueChanges.pipe(distinctUntilChanged(), debounceTime(300), map(() => undefined)))
      .pipe(startWith(undefined), switchMap(() => this.cargarPacientes()), takeUntilDestroyed(this.destroyRef))
      .subscribe();
  }

  ngAfterViewInit(): void {
    if (this.dniInput) {
      fromEvent(this.dniInput.nativeElement, 'blur')
        .pipe(
          map(() => this.form.get('dni')?.value?.trim()),
          filter((dni): dni is string => !!dni && /^\d{8}$/.test(dni)),
          debounceTime(500),
          distinctUntilChanged(),
          tap(() => {
            if (!this.consultandoDni()) {
              this.consultarDniAuto();
            }
          }),
          takeUntilDestroyed(this.destroyRef)
        )
        .subscribe();
    }
  }

  refrescar(): void {
    this.success.set('');
    this.error.set('');
    this.reload$.next();
  }

  private cargarPacientes(): Observable<Paciente[]> {
    this.loading.set(true);
    this.error.set('');
    const texto = this.searchControl.value.trim();

    const request$ = texto.length > 0 ? this.service.buscarPacientes(texto) : this.service.getPacientes();

    return request$.pipe(
      catchError((err: Error) => {
        this.error.set(err.message || 'No se pudieron cargar los pacientes.');
        return of([] as Paciente[]);
      }),
      finalize(() => this.loading.set(false)),
      tap(lista => this.pacientes.set(lista))
    );
  }

  abrirAlta(): void {
    this.formError.set('');
    this.dniConsultadoExitosamente.set(false);
    this.form.reset({ dni: '', nombres: '', apellidos: '' });
    this.modalAbierto.set(true);
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.formError.set('');
    this.dniConsultadoExitosamente.set(false);
  }

  consultarDniManual(): void {
    const dni = this.form.get('dni')?.value?.trim();
    if (dni && /^\d{8}$/.test(dni)) {
      this.consultarDni(dni);
    }
  }

  private consultarDniAuto(): void {
    const dni = this.form.get('dni')?.value?.trim();
    if (dni && /^\d{8}$/.test(dni)) {
      this.consultarDni(dni);
    }
  }

  private consultarDni(dni: string): void {
    this.consultandoDni.set(true);
    this.formError.set('');

    this.dniService.consultarDni(dni)
      .pipe(
        finalize(() => this.consultandoDni.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (response: DniResponse) => {
          this.form.patchValue({
            nombres: response.nombres || '',
            apellidos: response.apellidos || ''
          });
          this.dniConsultadoExitosamente.set(true);
        },
        error: (err: any) => {
          this.dniConsultadoExitosamente.set(false);
          const status = err?.status;
          if (status === 404) {
            this.formError.set('DNI no encontrado en RENIEC. Ingrese los datos manualmente.');
          } else if (status === 409) {
            this.formError.set('El DNI ya está registrado en el sistema.');
          } else if (status === 502 || status === 504) {
            this.formError.set('Servicio de consulta DNI no disponible. Ingrese los datos manualmente.');
          } else {
            this.formError.set('No se pudo consultar el DNI. Ingrese los datos manualmente.');
          }
        }
      });
  }

  onDniChange(): void {
    const dniControl = this.form.get('dni');
    const dni = dniControl?.value?.trim();
    
    if (!dni || !/^\d{8}$/.test(dni)) {
      this.form.patchValue({ nombres: '', apellidos: '' }, { emitEvent: false });
      this.dniConsultadoExitosamente.set(false);
      this.formError.set('');
    }
  }

  private dniAsyncValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ dniTaken: true } | null> => {
      const dni = control.value?.trim();
      if (!dni || !/^\d{8}$/.test(dni)) {
        return of(null);
      }
      return this.dniService.checkExists(dni).pipe(
        debounceTime(300),
        distinctUntilChanged(),
        map(exists => exists ? { dniTaken: true } : null)
      );
    };
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const payload: CreatePacienteRequest = {
      dni: valor.dni.trim(),
      nombres: valor.nombres.trim(),
      apellidos: valor.apellidos.trim()
    };

    this.guardando.set(true);
    this.formError.set('');

    this.service
      .createPaciente(payload)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: creado => {
          this.cerrarModal();
          this.success.set(`Paciente ${creado.fullName} registrado correctamente.`);
          this.reload$.next();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo registrar el paciente.')
      });
  }

  trackById(_index: number, paciente: Paciente): number {
    return paciente.id;
  }
}