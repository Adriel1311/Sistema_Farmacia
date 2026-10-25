import { Pipe, PipeTransform } from '@angular/core';

const LOCALE = 'es-PE';

/**
 * Formatea fechas de la API FarmaMed. El backend devuelve LocalDate como
 * "yyyy-MM-dd" y LocalDateTime como ISO sin zona; se interpretan como hora
 * local para no correr un dia por el desfase de UTC.
 */
@Pipe({ name: 'fmDate', standalone: false })
export class FmDatePipe implements PipeTransform {
  transform(value: string | Date | null | undefined, formato: 'date' | 'datetime' | 'time' = 'date'): string {
    const date = this.toDate(value);
    if (!date) {
      return '';
    }
    const opciones: Intl.DateTimeFormatOptions =
      formato === 'datetime'
        ? { dateStyle: 'short', timeStyle: 'short' }
        : formato === 'time'
          ? { timeStyle: 'short' }
          : { dateStyle: 'short' };
    return new Intl.DateTimeFormat(LOCALE, opciones).format(date);
  }

  private toDate(value: string | Date | null | undefined): Date | null {
    if (!value) {
      return null;
    }
    if (value instanceof Date) {
      return Number.isNaN(value.getTime()) ? null : value;
    }
    const soloFecha = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
    if (soloFecha) {
      return new Date(Number(soloFecha[1]), Number(soloFecha[2]) - 1, Number(soloFecha[3]));
    }
    const sinZona = value.replace(/Z$/, '').replace(/([+-]\d{2}:\d{2})$/, '');
    const date = new Date(sinZona);
    return Number.isNaN(date.getTime()) ? null : date;
  }
}

/** Dias restantes hasta una fecha; negativo si ya vencio. */
@Pipe({ name: 'fmDiasRestantes', standalone: false })
export class FmDiasRestantesPipe implements PipeTransform {
  transform(value: string | Date | null | undefined): number | null {
    if (!value) {
      return null;
    }
    const target = value instanceof Date ? value : this.parse(value);
    if (!target) {
      return null;
    }
    const hoy = new Date();
    const inicio = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
    const fin = new Date(target.getFullYear(), target.getMonth(), target.getDate());
    return Math.round((fin.getTime() - inicio.getTime()) / 86_400_000);
  }

  private parse(value: string): Date | null {
    const soloFecha = /^(\d{4})-(\d{2})-(\d{2})/.exec(value);
    if (soloFecha) {
      return new Date(Number(soloFecha[1]), Number(soloFecha[2]) - 1, Number(soloFecha[3]));
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date;
  }
}
