package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.response.DashboardResponse;
import com.medicalstore.pos.dto.response.LoteResponse;
import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.OrdenCompra;
import com.medicalstore.pos.repository.LoteRepository;
import com.medicalstore.pos.repository.MedicamentoRepository;
import com.medicalstore.pos.repository.DispensacionRepository;
import com.medicalstore.pos.repository.OrdenCompraRepository;
import com.medicalstore.pos.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * M12 Dashboard (RF-13): indicadores del panel operativo.
 * Solo lectura; reutiliza los calculos de alertas, lotes y pedidos sugeridos.
 */
@Service
public class DashboardService {
    
    private final LoteRepository loteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ConfiguracionService configuracionService;
    private final StockService stockService;
    private final LoteService loteService;
    private final AlertaService alertaService;
    private final OrdenCompraService ordenCompraService;
    private final DispensacionRepository dispensacionRepository;
    private final OrdenCompraRepository ordenRepository;
    
    public DashboardService(LoteRepository loteRepository,
                            MedicamentoRepository medicamentoRepository,
                            ProveedorRepository proveedorRepository,
                            DispensacionRepository dispensacionRepository,
                            OrdenCompraRepository ordenRepository,
                            ConfiguracionService configuracionService,
                            StockService stockService,
                            LoteService loteService,
                            AlertaService alertaService,
                            OrdenCompraService ordenCompraService) {
        this.loteRepository = loteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.proveedorRepository = proveedorRepository;
        this.dispensacionRepository = dispensacionRepository;
        this.ordenRepository = ordenRepository;
        this.configuracionService = configuracionService;
        this.stockService = stockService;
        this.loteService = loteService;
        this.alertaService = alertaService;
        this.ordenCompraService = ordenCompraService;
    }
    
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate hoy = LocalDate.now();
        int diasCritico = configuracionService.getDiasCritico();
        int diasProximo = configuracionService.getDiasProximo();
        
        List<Lote> criticos = loteRepository.findVenciendo(hoy, hoy.plusDays(diasCritico));
        List<Lote> proximos = loteRepository.findVenciendo(
                hoy.plusDays(diasCritico), hoy.plusDays(diasProximo));
        
        List<Medicamento> medicamentos = medicamentoRepository.findAllActivosWithProveedor();
        long medicamentosStockCritico = medicamentos.stream()
                .filter(m -> m.isStockCritico(stockService.getStockVigente(m.getId(), hoy)))
                .count();
        
        return DashboardResponse.builder()
                .lotesCriticos((long) criticos.size())
                .lotesProximosAVencer((long) proximos.size())
                .lotesVencidos(loteRepository.countVencidosConStock(hoy))
                .medicamentosStockCritico(medicamentosStockCritico)
                .ordenesSugeridas(ordenRepository.countByEstado(OrdenCompra.Estado.SUGERIDA))
                .dispensacionesHoy(dispensacionRepository.countCompletadasDesde(hoy.atStartOfDay()))
                .medicamentosActivos(medicamentoRepository.countByActivoTrue())
                .proveedoresActivos(proveedorRepository.countByActivoTrue())
                .alertasTotales(alertaService.countTotal())
                .alertasAltaPrioridad(alertaService.countAltaPrioridad())
                .diasCritico(diasCritico)
                .diasProximo(diasProximo)
                .proximosAVencer(criticos.stream()
                        .limit(10)
                        .map(l -> loteService.getById(l.getId()))
                        .collect(Collectors.toList()))
                .suggestions(ordenCompraService.getSugerencias().stream()
                        .limit(10)
                        .collect(Collectors.toList()))
                .generadoEn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}
