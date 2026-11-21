package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.AbrirCajaRequest;
import com.medicalstore.pos.dto.request.CerrarCajaRequest;
import com.medicalstore.pos.dto.request.SalidaCajaRequest;
import com.medicalstore.pos.dto.response.CajaResponse;
import com.medicalstore.pos.dto.response.MovimientoCajaResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Caja;
import com.medicalstore.pos.entity.DetalleDispensacion;
import com.medicalstore.pos.entity.Dispensacion;
import com.medicalstore.pos.entity.MovimientoCaja;
import com.medicalstore.pos.entity.MovimientoCajaItem;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.exception.BusinessRuleException;
import com.medicalstore.pos.exception.ResourceNotFoundException;
import com.medicalstore.pos.repository.CajaRepository;
import com.medicalstore.pos.repository.MovimientoCajaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * M11 Caja.
 *
 * Reglas aplicadas:
 * - Solo hay una caja abierta a la vez (indice unico parcial en la BD).
 * - Cada receta PARTICULAR con total mayor a cero se cobra en la caja abierta; sin caja abierta
 *   no se puede dispensar esa receta.
 * - Una venta queda enlazada a su boleta (dispensacion) y se cobra una sola vez.
 * - Anular la boleta devuelve el dinero con un movimiento DEVOLUCION en la caja abierta.
 * - Las salidas (retiros) exigen concepto y no pueden superar el saldo de la caja.
 * - Todo movimiento queda en el libro de la caja y en auditoria.
 */
@Service
public class CajaService {

    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoRepository;
    private final AuditoriaService auditoriaService;

    public CajaService(CajaRepository cajaRepository,
                       MovimientoCajaRepository movimientoRepository,
                       AuditoriaService auditoriaService) {
        this.cajaRepository = cajaRepository;
        this.movimientoRepository = movimientoRepository;
        this.auditoriaService = auditoriaService;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Optional<CajaResponse> getActual() {
        return cajaRepository.findByEstado(Caja.Estado.ABIERTA).map(this::mapCaja);
    }

    @Transactional(readOnly = true)
    public List<CajaResponse> getHistorial() {
        return cajaRepository.findAllRecientes().stream()
                .limit(60)
                .map(this::mapCaja)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CajaResponse getById(Long id) {
        return mapCaja(obtener(id));
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaResponse> getMovimientos(Long cajaId) {
        obtener(cajaId);
        List<MovimientoCaja> movimientos = movimientoRepository.findByCajaDetallado(cajaId);
        List<Long> boletas = movimientos.stream()
                .filter(m -> m.getDispensacion() != null)
                .map(m -> m.getDispensacion().getId())
                .distinct()
                .collect(Collectors.toList());
        Map<Long, VentaInfo> ventas = ventasPorDispensacion(boletas);
        return movimientos.stream()
                .map(m -> mapMovimiento(m, ventas))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Operaciones de caja
    // ------------------------------------------------------------------

    @Transactional(rollbackFor = Exception.class)
    public CajaResponse abrir(AbrirCajaRequest request, Usuario user, HttpServletRequest httpRequest) {
        if (cajaRepository.findByEstado(Caja.Estado.ABIERTA).isPresent()) {
            throw new BusinessRuleException("CAJA_YA_ABIERTA", "Ya hay una caja abierta. Cierrala antes de abrir otra.");
        }

        BigDecimal inicial = money(request.getMontoInicial());
        Caja caja = Caja.builder()
                .estado(Caja.Estado.ABIERTA)
                .montoInicial(inicial)
                .usuarioApertura(user)
                .observacionApertura(limpiar(request.getObservacion()))
                .build();
        caja = cajaRepository.save(caja);

        movimientoRepository.save(MovimientoCaja.builder()
                .caja(caja)
                .tipo(MovimientoCaja.Tipo.APERTURA)
                .monto(inicial)
                .concepto("Apertura de caja con monto inicial")
                .usuario(user)
                .build());

        auditoriaService.log(Auditoria.Accion.CAJA_ABIERTA, user, "Caja", caja.getId().toString(),
                "Caja abierta con monto inicial " + soles(inicial), null, soles(inicial), httpRequest);

        return mapCaja(caja);
    }

    @Transactional(rollbackFor = Exception.class)
    public CajaResponse registrarSalida(SalidaCajaRequest request, Usuario user, HttpServletRequest httpRequest) {
        Caja caja = cajaAbiertaBloqueada("No hay una caja abierta. Abre la caja para registrar una salida.");

        BigDecimal monto = money(request.getMonto());
        BigDecimal saldo = saldo(caja);
        if (monto.compareTo(saldo) > 0) {
            throw new BusinessRuleException("SALDO_INSUFICIENTE",
                    "El retiro de " + soles(monto) + " supera el saldo disponible en caja (" + soles(saldo) + ").");
        }

        String concepto = request.getConcepto().trim();
        movimientoRepository.save(MovimientoCaja.builder()
                .caja(caja)
                .tipo(MovimientoCaja.Tipo.SALIDA)
                .monto(monto)
                .concepto(concepto)
                .usuario(user)
                .build());

        auditoriaService.log(Auditoria.Accion.CAJA_SALIDA, user, "Caja", caja.getId().toString(),
                "Salida de caja " + soles(monto) + ": " + recortar(concepto, 150),
                soles(saldo), soles(saldo.subtract(monto)), httpRequest);

        return mapCaja(caja);
    }

    @Transactional(rollbackFor = Exception.class)
    public CajaResponse cerrar(CerrarCajaRequest request, Usuario user, HttpServletRequest httpRequest) {
        Caja caja = cajaAbiertaBloqueada("No hay una caja abierta para cerrar.");

        BigDecimal esperado = saldo(caja);
        BigDecimal contado = money(request.getMontoContado());
        BigDecimal diferencia = contado.subtract(esperado);

        caja.setEstado(Caja.Estado.CERRADA);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setUsuarioCierre(user);
        caja.setMontoEsperado(esperado);
        caja.setMontoContado(contado);
        caja.setDiferencia(diferencia);
        caja.setObservacionCierre(limpiar(request.getObservacion()));
        caja = cajaRepository.save(caja);

        auditoriaService.log(Auditoria.Accion.CAJA_CERRADA, user, "Caja", caja.getId().toString(),
                "Caja cerrada. Esperado " + soles(esperado) + ", contado " + soles(contado)
                        + ", diferencia " + soles(diferencia),
                soles(esperado), soles(contado), httpRequest);

        return mapCaja(caja);
    }

    // ------------------------------------------------------------------
    // Integracion con dispensacion (se ejecutan dentro de su transaccion)
    // ------------------------------------------------------------------

    /** Venta de una boleta: total cobrado y precio unitario por linea de dispensacion. */
    public static class VentaInfo {
        private final BigDecimal total;
        private final Map<Long, BigDecimal> precioPorDetalle;
        private final List<MovimientoCajaItem> items;

        VentaInfo(BigDecimal total, Map<Long, BigDecimal> precioPorDetalle, List<MovimientoCajaItem> items) {
            this.total = total;
            this.precioPorDetalle = precioPorDetalle;
            this.items = items;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public BigDecimal precioDeDetalle(Long detalleId) {
            return precioPorDetalle.getOrDefault(detalleId, BigDecimal.ZERO);
        }

        List<MovimientoCajaItem> getItems() {
            return items;
        }
    }

    /**
     * Cobra la boleta en la caja abierta. El precio de cada medicamento se congela en la linea de
     * venta. Si el total es cero (sin precios) no se cobra. Si no hay caja abierta lanza una
     * excepcion y la transaccion de la dispensacion se revierte completa (el stock no se descuenta).
     */
    @Transactional(rollbackFor = Exception.class)
    public void registrarVenta(Dispensacion dispensacion, Map<Long, BigDecimal> preciosPorMedicamento, Usuario user) {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleDispensacion det : dispensacion.getDetalles()) {
            BigDecimal precio = preciosPorMedicamento.getOrDefault(det.getMedicamento().getId(), BigDecimal.ZERO);
            total = total.add(precio.multiply(BigDecimal.valueOf(det.getCantidad())));
        }
        total = money(total);
        if (total.signum() <= 0) {
            return;
        }

        Caja caja = cajaAbiertaBloqueada(
                "No hay una caja abierta. Abre la caja antes de dispensar una receta particular con cobro.");

        MovimientoCaja venta = MovimientoCaja.builder()
                .caja(caja)
                .tipo(MovimientoCaja.Tipo.VENTA)
                .monto(total)
                .concepto("Venta de receta " + dispensacion.getReceta().getNumeroReceta())
                .dispensacion(dispensacion)
                .usuario(user)
                .build();

        for (DetalleDispensacion det : dispensacion.getDetalles()) {
            BigDecimal precio = money(preciosPorMedicamento.getOrDefault(det.getMedicamento().getId(), BigDecimal.ZERO));
            venta.agregarItem(MovimientoCajaItem.builder()
                    .detalleDispensacionId(det.getId())
                    .medicamentoId(det.getMedicamento().getId())
                    .medicamentoNombre(det.getMedicamento().getNombre())
                    .numeroLote(det.getLote().getNumeroLote())
                    .cantidad(det.getCantidad())
                    .precioUnitario(precio)
                    .build());
        }
        movimientoRepository.save(venta);
    }

    /**
     * Devuelve el dinero de una boleta anulada. Si la boleta nunca se cobro en caja (receta
     * asegurada o sin precios) no hace nada. La devolucion no valida saldo: el dinero se entrego al
     * paciente y la caja debe reflejarlo aunque quede en negativo.
     */
    @Transactional(rollbackFor = Exception.class)
    public void registrarDevolucion(Dispensacion dispensacion, Usuario user) {
        Long id = dispensacion.getId();
        Optional<MovimientoCaja> venta = movimientoRepository.findByTipoAndDispensacionId(MovimientoCaja.Tipo.VENTA, id);
        if (venta.isEmpty() || movimientoRepository.existsByDispensacionIdAndTipo(id, MovimientoCaja.Tipo.DEVOLUCION)) {
            return;
        }

        Caja caja = cajaAbiertaBloqueada(
                "Esta boleta ya fue cobrada. Abre la caja para registrar la devolucion del dinero y poder anularla.");

        movimientoRepository.save(MovimientoCaja.builder()
                .caja(caja)
                .tipo(MovimientoCaja.Tipo.DEVOLUCION)
                .monto(money(venta.get().getMonto()))
                .concepto("Devolucion por anulacion de la receta " + dispensacion.getReceta().getNumeroReceta())
                .dispensacion(dispensacion)
                .usuario(user)
                .build());
    }

    /** Ventas de un conjunto de boletas, indexadas por id de dispensacion (en una sola consulta). */
    @Transactional(readOnly = true)
    public Map<Long, VentaInfo> ventasPorDispensacion(Collection<Long> dispensacionIds) {
        Map<Long, VentaInfo> mapa = new HashMap<>();
        if (dispensacionIds == null || dispensacionIds.isEmpty()) {
            return mapa;
        }
        for (MovimientoCaja venta : movimientoRepository
                .findConItemsByTipoAndDispensacionIds(MovimientoCaja.Tipo.VENTA, dispensacionIds)) {
            Map<Long, BigDecimal> precios = new HashMap<>();
            for (MovimientoCajaItem item : venta.getItems()) {
                if (item.getDetalleDispensacionId() != null) {
                    precios.put(item.getDetalleDispensacionId(), money(item.getPrecioUnitario()));
                }
            }
            mapa.put(venta.getDispensacion().getId(), new VentaInfo(money(venta.getMonto()), precios, venta.getItems()));
        }
        return mapa;
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private Caja obtener(Long id) {
        return cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja not found with id: " + id));
    }

    private Caja cajaAbiertaBloqueada(String mensajeSiCerrada) {
        return cajaRepository.findByEstadoParaActualizar(Caja.Estado.ABIERTA)
                .orElseThrow(() -> new BusinessRuleException("CAJA_CERRADA", mensajeSiCerrada));
    }

    /** Totales por tipo de movimiento: [suma, cantidad]. */
    private Map<MovimientoCaja.Tipo, BigDecimal[]> totales(Long cajaId) {
        Map<MovimientoCaja.Tipo, BigDecimal[]> mapa = new EnumMap<>(MovimientoCaja.Tipo.class);
        for (Object[] fila : movimientoRepository.totalesPorTipo(cajaId)) {
            mapa.put((MovimientoCaja.Tipo) fila[0],
                    new BigDecimal[]{money((BigDecimal) fila[1]), BigDecimal.valueOf(((Number) fila[2]).longValue())});
        }
        return mapa;
    }

    private BigDecimal suma(Map<MovimientoCaja.Tipo, BigDecimal[]> t, MovimientoCaja.Tipo tipo) {
        BigDecimal[] fila = t.get(tipo);
        return fila == null ? money(BigDecimal.ZERO) : fila[0];
    }

    /** Efectivo que debe haber en caja: inicial + ventas - devoluciones - salidas. */
    private BigDecimal saldo(Caja caja) {
        Map<MovimientoCaja.Tipo, BigDecimal[]> t = totales(caja.getId());
        return money(caja.getMontoInicial()
                .add(suma(t, MovimientoCaja.Tipo.VENTA))
                .subtract(suma(t, MovimientoCaja.Tipo.DEVOLUCION))
                .subtract(suma(t, MovimientoCaja.Tipo.SALIDA)));
    }

    private CajaResponse mapCaja(Caja c) {
        Map<MovimientoCaja.Tipo, BigDecimal[]> t = totales(c.getId());
        BigDecimal ventas = suma(t, MovimientoCaja.Tipo.VENTA);
        BigDecimal devoluciones = suma(t, MovimientoCaja.Tipo.DEVOLUCION);
        BigDecimal salidas = suma(t, MovimientoCaja.Tipo.SALIDA);
        BigDecimal[] filaVentas = t.get(MovimientoCaja.Tipo.VENTA);

        return CajaResponse.builder()
                .id(c.getId())
                .estado(c.getEstado())
                .montoInicial(money(c.getMontoInicial()))
                .fechaApertura(c.getFechaApertura())
                .usuarioApertura(c.getUsuarioApertura() != null ? c.getUsuarioApertura().getNombreCompleto() : null)
                .observacionApertura(c.getObservacionApertura())
                .totalVentas(ventas)
                .cantidadVentas(filaVentas == null ? 0 : filaVentas[1].intValue())
                .totalDevoluciones(devoluciones)
                .totalSalidas(salidas)
                .totalRecaudado(money(ventas.subtract(devoluciones)))
                .saldoActual(money(c.getMontoInicial().add(ventas).subtract(devoluciones).subtract(salidas)))
                .fechaCierre(c.getFechaCierre())
                .usuarioCierre(c.getUsuarioCierre() != null ? c.getUsuarioCierre().getNombreCompleto() : null)
                .montoEsperado(c.getMontoEsperado())
                .montoContado(c.getMontoContado())
                .diferencia(c.getDiferencia())
                .observacionCierre(c.getObservacionCierre())
                .build();
    }

    private MovimientoCajaResponse mapMovimiento(MovimientoCaja m, Map<Long, VentaInfo> ventas) {
        Dispensacion d = m.getDispensacion();
        MovimientoCajaResponse.MovimientoCajaResponseBuilder b = MovimientoCajaResponse.builder()
                .id(m.getId())
                .cajaId(m.getCaja().getId())
                .tipo(m.getTipo())
                .monto(money(m.getMonto()))
                .esIngreso(m.esIngreso())
                .concepto(m.getConcepto())
                .fechaHora(m.getFechaHora())
                .usuario(m.getUsuario() != null ? m.getUsuario().getNombreCompleto() : null);

        if (d != null) {
            VentaInfo venta = ventas.get(d.getId());
            b.dispensacionId(d.getId())
                    .numeroTicket(d.getNumeroTicket())
                    .numeroReceta(d.getReceta().getNumeroReceta())
                    .pacienteNombreCompleto(d.getReceta().getPaciente() != null
                            ? d.getReceta().getPaciente().getNombreCompleto() : null)
                    .pacienteDni(d.getReceta().getPaciente() != null ? d.getReceta().getPaciente().getDni() : null)
                    .items(venta == null ? List.of()
                            : venta.getItems().stream().map(this::mapItem).collect(Collectors.toList()));
        }
        return b.build();
    }

    private MovimientoCajaResponse.ItemVenta mapItem(MovimientoCajaItem item) {
        return MovimientoCajaResponse.ItemVenta.builder()
                .medicamentoNombre(item.getMedicamentoNombre())
                .numeroLote(item.getNumeroLote())
                .cantidad(item.getCantidad())
                .precioUnitario(money(item.getPrecioUnitario()))
                .subtotal(money(item.getSubtotal()))
                .build();
    }

    private static BigDecimal money(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static String soles(BigDecimal valor) {
        return "S/ " + money(valor).toPlainString();
    }

    private static String limpiar(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.trim();
        return t.isEmpty() ? null : t;
    }

    private static String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }
}
