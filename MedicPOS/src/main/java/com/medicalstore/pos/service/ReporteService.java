package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.response.BajaInventarioResponse;
import com.medicalstore.pos.dto.response.OrdenCompraResponse;
import com.medicalstore.pos.dto.response.ReporteBajasResponse;
import com.medicalstore.pos.dto.response.ReporteConsumoResponse;
import com.medicalstore.pos.dto.response.ReporteInventarioResponse;
import com.medicalstore.pos.dto.response.ReporteOrdenesResponse;
import com.medicalstore.pos.entity.BajaInventario;
import com.medicalstore.pos.entity.DetalleDispensacion;
import com.medicalstore.pos.entity.Dispensacion;
import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.OrdenCompra;
import com.medicalstore.pos.entity.Proveedor;
import com.medicalstore.pos.repository.BajaInventarioRepository;
import com.medicalstore.pos.repository.DispensacionRepository;
import com.medicalstore.pos.repository.LoteRepository;
import com.medicalstore.pos.repository.MedicamentoRepository;
import com.medicalstore.pos.repository.OrdenCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * M08 Reportes (RF-12). Solo lectura: los reportes no modifican informacion.
 */
@Service
public class ReporteService {
    
    private final DispensacionRepository dispensacionRepository;
    private final LoteRepository loteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final BajaInventarioRepository bajaRepository;
    private final OrdenCompraRepository ordenRepository;
    private final StockService stockService;
    private final BajaInventarioService bajaInventarioService;
    private final OrdenCompraService ordenCompraService;
    
    public ReporteService(DispensacionRepository dispensacionRepository,
                          LoteRepository loteRepository,
                          MedicamentoRepository medicamentoRepository,
                          BajaInventarioRepository bajaRepository,
                          OrdenCompraRepository ordenRepository,
                          StockService stockService,
                          BajaInventarioService bajaInventarioService,
                          OrdenCompraService ordenCompraService) {
        this.dispensacionRepository = dispensacionRepository;
        this.loteRepository = loteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.bajaRepository = bajaRepository;
        this.ordenRepository = ordenRepository;
        this.stockService = stockService;
        this.bajaInventarioService = bajaInventarioService;
        this.ordenCompraService = ordenCompraService;
    }
    
    /**
     * Consumo por medicamento en el rango indicado.
     */
    @Transactional(readOnly = true)
    public ReporteConsumoResponse reporteConsumo(LocalDate desde, LocalDate hasta) {
        List<Dispensacion> dispensaciones = dispensacionRepository.findByRangoFechas(
                desde.atStartOfDay(), hasta.atTime(23, 59, 59));
        
        Map<Long, Acumulado> porMedicamento = new LinkedHashMap<>();
        long dispensacionesValidas = 0;
        
        for (Dispensacion dispensacion : dispensaciones) {
            if (dispensacion.getEstado() == Dispensacion.Estado.ANULADA) {
                continue;
            }
            dispensacionesValidas++;
            for (DetalleDispensacion detalle : dispensacion.getDetalles()) {
                Medicamento medicamento = detalle.getMedicamento();
                Acumulado acumulado = porMedicamento.computeIfAbsent(medicamento.getId(),
                        k -> new Acumulado(medicamento));
                acumulado.unidades += detalle.getCantidad();
                acumulado.dispensaciones++;
            }
        }
        
        List<ReporteConsumoResponse.ItemConsumoResponse> items = porMedicamento.values().stream()
                .map(a -> ReporteConsumoResponse.ItemConsumoResponse.builder()
                        .medicamentoId(a.medicamento.getId())
                        .codigo(a.medicamento.getCodigo())
                        .nombre(a.medicamento.getNombre())
                        .unidadMedida(a.medicamento.getUnidadMedida())
                        .unidadesConsumidas(a.unidades)
                        .dispensaciones(a.dispensaciones)
                        .promedioPorDispensacion(a.dispensaciones > 0 ? a.unidades / a.dispensaciones : 0)
                        .build())
                .sorted((a, b) -> b.getUnidadesConsumidas().compareTo(a.getUnidadesConsumidas()))
                .collect(Collectors.toList());
        
        int totalUnidades = items.stream()
                .mapToInt(ReporteConsumoResponse.ItemConsumoResponse::getUnidadesConsumidas)
                .sum();
        
        return ReporteConsumoResponse.builder()
                .desde(desde)
                .hasta(hasta)
                .items(items)
                .totalMedicamentos(items.size())
                .totalUnidades(totalUnidades)
                .totalDispensaciones(dispensacionesValidas)
                .diasReporte((int) ChronoUnit.DAYS.between(desde, hasta) + 1)
                .build();
    }
    
    /**
     * Foto actual del inventario: vigente, vencido y stock critico por medicamento.
     */
    @Transactional(readOnly = true)
    public ReporteInventarioResponse reporteInventario() {
        LocalDate hoy = LocalDate.now();
        List<Medicamento> medicamentos = medicamentoRepository.findAllActivosWithProveedor();
        
        Map<Long, Integer> stockVigenteMap = stockService.getStockVigentePorIds(
                medicamentos.stream().map(Medicamento::getId).collect(Collectors.toList()), hoy);
        // El vencido se consulta aparte porque el stock vigente excluye lo vencido (RN06)
        Map<Long, Integer> stockVencidoMap = stockService.getStockVencidoPorIds(
                medicamentos.stream().map(Medicamento::getId).collect(Collectors.toList()), hoy);
        
        List<ReporteInventarioResponse.ItemInventarioResponse> items = new ArrayList<>();
        long totalVigentes = 0;
        long totalVencidas = 0;
        long criticos = 0;
        
        for (Medicamento medicamento : medicamentos) {
            int vigente = stockVigenteMap.getOrDefault(medicamento.getId(), 0);
            int vencido = stockVencidoMap.getOrDefault(medicamento.getId(), 0);
            long lotesActivos = stockService.countLotesActivos(medicamento.getId());
            long lotesVencidos = stockService.countLotesVencidos(medicamento.getId(), hoy);
            boolean critico = medicamento.isStockCritico(vigente);
            
            totalVigentes += vigente;
            totalVencidas += vencido;
            if (critico) {
                criticos++;
            }
            
            items.add(ReporteInventarioResponse.ItemInventarioResponse.builder()
                    .medicamentoId(medicamento.getId())
                    .codigo(medicamento.getCodigo())
                    .nombre(medicamento.getNombre())
                    .unidadMedida(medicamento.getUnidadMedida())
                    .proveedorRazonSocial(medicamento.getProveedor() != null
                            ? medicamento.getProveedor().getRazonSocial() : null)
                    .stockVigente(vigente)
                    .stockVencido(vencido)
                    .stockMinimo(medicamento.getStockMinimo())
                    .stockMaximo(medicamento.getStockMaximo())
                    .stockCritico(critico)
                    .cantidadSugerida(medicamento.getCantidadSugerida(vigente))
                    .lotesActivos((int) lotesActivos)
                    .lotesVencidos((int) lotesVencidos)
                    .build());
        }
        
        return ReporteInventarioResponse.builder()
                .items(items)
                .totalMedicamentos(items.size())
                .totalLotes((long) loteRepository.findAllWithMedicamento().size())
                .totalUnidadesVigentes(totalVigentes)
                .totalUnidadesVencidas(totalVencidas)
                .medicamentosStockCritico(criticos)
                .build();
    }
    
    /**
     * Bajas del periodo, agrupadas por motivo y por medicamento.
     */
    @Transactional(readOnly = true)
    public ReporteBajasResponse reporteBajas(LocalDate desde, LocalDate hasta) {
        List<BajaInventarioResponse> respuestas = bajaInventarioService.getByRango(
                desde.atStartOfDay(), hasta.atTime(23, 59, 59));
        List<BajaInventario> bajas = bajaRepository.findByRangoFechas(
                desde.atStartOfDay(), hasta.atTime(23, 59, 59));
        
        int totalUnidades = bajas.stream().mapToInt(BajaInventario::getCantidad).sum();
        int vencido = bajas.stream()
                .filter(b -> b.getMotivo() == BajaInventario.Motivo.VENCIDO_EN_ESTANTE)
                .mapToInt(BajaInventario::getCantidad).sum();
        int merma = bajas.stream()
                .filter(b -> b.getMotivo() == BajaInventario.Motivo.MERMA)
                .mapToInt(BajaInventario::getCantidad).sum();
        int devuelto = bajas.stream()
                .filter(b -> b.getMotivo() == BajaInventario.Motivo.DEVUELTO)
                .mapToInt(BajaInventario::getCantidad).sum();
        
        Map<Long, ReporteBajasResponse.ResumenPorMedicamento> resumen = new LinkedHashMap<>();
        for (BajaInventario baja : bajas) {
            Medicamento medicamento = baja.getLote().getMedicamento();
            ReporteBajasResponse.ResumenPorMedicamento item = resumen.computeIfAbsent(
                    medicamento.getId(),
                    k -> ReporteBajasResponse.ResumenPorMedicamento.builder()
                            .medicamentoId(medicamento.getId())
                            .codigo(medicamento.getCodigo())
                            .nombre(medicamento.getNombre())
                            .unidadesDadasDeBaja(0)
                            .build());
            item.setUnidadesDadasDeBaja(item.getUnidadesDadasDeBaja() + baja.getCantidad());
        }
        
        return ReporteBajasResponse.builder()
                .desde(desde)
                .hasta(hasta)
                .bajas(respuestas)
                .totalRegistros(bajas.size())
                .totalUnidades(totalUnidades)
                .totalVencidoEnEstante(vencido)
                .totalMerma(merma)
                .totalDevuelto(devuelto)
                .resumenPorMedicamento(new ArrayList<>(resumen.values()))
                .build();
    }
    
    /**
     * Historial de ordenes de compra del periodo.
     */
    @Transactional(readOnly = true)
    public ReporteOrdenesResponse reporteOrdenes(LocalDate desde, LocalDate hasta) {
        List<OrdenCompra> ordenes = ordenRepository.findAllDetallado().stream()
                .filter(o -> o.getFechaHora() != null)
                .filter(o -> !o.getFechaHora().toLocalDate().isBefore(desde))
                .filter(o -> !o.getFechaHora().toLocalDate().isAfter(hasta))
                .collect(Collectors.toList());
        
        List<OrdenCompraResponse> respuestas = ordenes.stream()
                .map(o -> ordenCompraService.getById(o.getId()))
                .collect(Collectors.toList());
        
        int totalUnidades = respuestas.stream()
                .mapToInt(r -> r.getTotalUnidades() != null ? r.getTotalUnidades() : 0)
                .sum();
        
        Map<Long, ReporteOrdenesResponse.ResumenPorProveedor> resumen = new LinkedHashMap<>();
        for (OrdenCompra orden : ordenes) {
            Proveedor proveedor = orden.getProveedor();
            ReporteOrdenesResponse.ResumenPorProveedor item = resumen.computeIfAbsent(
                    proveedor.getId(),
                    k -> ReporteOrdenesResponse.ResumenPorProveedor.builder()
                            .proveedorId(proveedor.getId())
                            .razonSocial(proveedor.getRazonSocial())
                            .ruc(proveedor.getRuc())
                            .ordenes(0)
                            .unidades(0)
                            .build());
            item.setOrdenes(item.getOrdenes() + 1);
            item.setUnidades(item.getUnidades() + orden.getDetalles().stream()
                    .mapToInt(d -> d.getCantidad() != null ? d.getCantidad() : 0).sum());
        }
        
        return ReporteOrdenesResponse.builder()
                .desde(desde)
                .hasta(hasta)
                .ordenes(respuestas)
                .totalOrdenes(ordenes.size())
                .totalUnidades(totalUnidades)
                .totalBorrador(contar(ordenes, OrdenCompra.Estado.BORRADOR))
                .totalSugerida(contar(ordenes, OrdenCompra.Estado.SUGERIDA))
                .totalAprobada(contar(ordenes, OrdenCompra.Estado.APROBADA))
                .totalRechazada(contar(ordenes, OrdenCompra.Estado.RECHAZADA))
                .resumenPorProveedor(new ArrayList<>(resumen.values()))
                .build();
    }
    
    private long contar(List<OrdenCompra> ordenes, OrdenCompra.Estado estado) {
        return ordenes.stream().filter(o -> o.getEstado() == estado).count();
    }
    
    private static class Acumulado {
        private final Medicamento medicamento;
        private int unidades;
        private int dispensaciones;
        
        Acumulado(Medicamento medicamento) {
            this.medicamento = medicamento;
        }
    }
}
