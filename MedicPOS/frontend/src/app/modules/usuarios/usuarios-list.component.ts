import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { SharedModule } from '../../shared/shared.module';
import { SvgIconComponent } from '../../core/components/svg-icon/svg-icon.component';
import { UsuarioService } from '../../core/services/usuario.service';
import { CreateUsuarioRequest, Rol, UpdateUsuarioRequest, Usuario } from '../../core/models/user.model';

type ModoFormulario = 'alta' | 'edicion';

function coincidenPasswords(control: AbstractControl): ValidationErrors | null {
  const nuevo = String(control.get('passwordNuevo')?.value ?? '');
  const repetir = String(control.get('repetirPassword')?.value ?? '');
  if (!nuevo || !repetir) {
    return null;
  }
  return nuevo === repetir ? null : { passwordsNoCoinciden: true };
}

@Component({
  selector: 'app-usuarios-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './usuarios-list.component.html',
  styleUrls: ['./usuarios-list.component.scss']
})
export class UsuariosListComponent {
  private readonly fb = inject(FormBuilder);
  private readonly servicio = inject(UsuarioService);
  private readonly destroyRef = inject(DestroyRef);

  readonly usuarios = signal<Usuario[]>([]);
  readonly roles = signal<Rol[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly success = signal('');

  readonly modalAbierto = signal(false);
  readonly modo = signal<ModoFormulario>('alta');
  readonly guardando = signal(false);
  readonly formError = signal('');
  readonly edicion = signal<Usuario | null>(null);
  readonly tituloModal = computed(() => (this.modo() === 'alta' ? 'Nuevo usuario' : 'Editar usuario'));

  readonly objetivo = signal<Usuario | null>(null);
  readonly passwordModalAbierto = signal(false);
  readonly passwordObjetivo = signal<Usuario | null>(null);
  readonly passwordError = signal('');

  readonly form = this.fb.nonNullable.group({
    usuario: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9_]+$/), Validators.maxLength(30)]],
    nombres: ['', [Validators.required, Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.maxLength(100)]],
    correo: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    rolId: [0, [Validators.required, Validators.min(1)]]
  });

  readonly passwordForm = this.fb.nonNullable.group(
    {
      passwordActual: ['', [Validators.required]],
      passwordNuevo: ['', [Validators.required, Validators.minLength(8)]],
      repetirPassword: ['', [Validators.required]]
    },
    { validators: coincidenPasswords }
  );

  constructor() {
    this.cargar();
  }

  refrescar(): void {
    this.success.set('');
    this.error.set('');
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.error.set('');

    forkJoin({
      usuarios: this.servicio.getUsuarios(),
      roles: this.servicio.getRoles()
    })
      .pipe(
        catchError((err: Error) => {
          this.error.set(err.message || 'No se pudieron cargar los usuarios.');
          return of({ usuarios: [] as Usuario[], roles: [] as Rol[] });
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(({ usuarios, roles }) => {
        this.usuarios.set(usuarios);
        this.roles.set(roles);
      });
  }

  abrirAlta(): void {
    this.modo.set('alta');
    this.edicion.set(null);
    this.formError.set('');
    this.configurarCredenciales(true);
    this.form.reset({ usuario: '', nombres: '', apellidos: '', correo: '', password: '', rolId: 0 });
    this.modalAbierto.set(true);
  }

  abrirEdicion(usuario: Usuario): void {
    this.modo.set('edicion');
    this.edicion.set(usuario);
    this.formError.set('');
    this.configurarCredenciales(false);
    this.form.reset({
      usuario: usuario.usuario,
      nombres: usuario.nombres,
      apellidos: usuario.apellidos,
      correo: usuario.correo,
      password: '',
      rolId: usuario.rolId
    });
    this.modalAbierto.set(true);
  }

  private configurarCredenciales(requeridas: boolean): void {
    const usuario = this.form.controls.usuario;
    const password = this.form.controls.password;

    if (requeridas) {
      usuario.setValidators([Validators.required, Validators.pattern(/^[A-Za-z0-9_]+$/), Validators.maxLength(30)]);
      password.setValidators([Validators.required, Validators.minLength(8)]);
    } else {
      usuario.clearValidators();
      password.clearValidators();
    }

    usuario.updateValueAndValidity();
    password.updateValueAndValidity();
  }

  cerrarModal(): void {
    this.modalAbierto.set(false);
    this.formError.set('');
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const original = this.edicion();
    const esEdicion = this.modo() === 'edicion' && original !== null;

    this.guardando.set(true);
    this.formError.set('');

    if (esEdicion && original) {
      const payload: UpdateUsuarioRequest = {
        nombres: valor.nombres.trim(),
        apellidos: valor.apellidos.trim(),
        correo: valor.correo.trim(),
        rolId: Number(valor.rolId),
        activo: original.activo
      };

      this.servicio
        .update(original.id, payload)
        .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: actualizado => {
            this.cerrarModal();
            this.success.set(`Usuario ${actualizado.usuario} actualizado correctamente.`);
            this.cargar();
          },
          error: (err: Error) => this.formError.set(err.message || 'No se pudo guardar el usuario.')
        });
      return;
    }

    const payload: CreateUsuarioRequest = {
      usuario: valor.usuario.trim(),
      nombres: valor.nombres.trim(),
      apellidos: valor.apellidos.trim(),
      correo: valor.correo.trim(),
      password: valor.password,
      rolId: Number(valor.rolId)
    };

    this.servicio
      .create(payload)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: creado => {
          this.cerrarModal();
          this.success.set(`Usuario ${creado.usuario} creado correctamente.`);
          this.cargar();
        },
        error: (err: Error) => this.formError.set(err.message || 'No se pudo crear el usuario.')
      });
  }

  pedirToggle(usuario: Usuario): void {
    this.objetivo.set(usuario);
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
    this.servicio
      .toggleActivo(objetivo.id)
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: actualizado => {
          this.objetivo.set(null);
          this.success.set(
            `Usuario ${actualizado.usuario} ${actualizado.activo ? 'activado' : 'desactivado'} correctamente.`
          );
          this.cargar();
        },
        error: (err: Error) => {
          this.objetivo.set(null);
          this.error.set(err.message || 'No se pudo cambiar el estado del usuario.');
        }
      });
  }

  abrirPassword(usuario: Usuario): void {
    this.passwordObjetivo.set(usuario);
    this.passwordError.set('');
    this.passwordForm.reset({ passwordActual: '', passwordNuevo: '', repetirPassword: '' });
    this.passwordModalAbierto.set(true);
  }

  cerrarPassword(): void {
    this.passwordModalAbierto.set(false);
    this.passwordError.set('');
  }

  guardarPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const objetivo = this.passwordObjetivo();
    if (!objetivo) {
      return;
    }

    const valor = this.passwordForm.getRawValue();
    this.guardando.set(true);
    this.passwordError.set('');

    this.servicio
      .changePassword(objetivo.id, { passwordActual: valor.passwordActual, passwordNuevo: valor.passwordNuevo })
      .pipe(finalize(() => this.guardando.set(false)), takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.cerrarPassword();
          this.success.set(`Contrasena de ${objetivo.usuario} actualizada correctamente.`);
        },
        error: (err: Error) => this.passwordError.set(err.message || 'No se pudo actualizar la contrasena.')
      });
  }

  trackById(_index: number, usuario: Usuario): number {
    return usuario.id;
  }
}
