package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.AtenderAlertaRequest;
import com.medicalstore.pos.dto.response.AlertaResponse;
import com.medicalstore.pos.dto.response.SugerenciaCompraResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.LoteRepository;
import com.medicalstore.pos.repository.MedicamentoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * M05 Alertas (RF-11, RN05.1, RN05.2, RN05.3).
 *
 * Las alertas NO se persisten: se calculan cada vez que se abre una pantalla.
 * "Atendida" significa que existe un registro de auditoria con accion ALERTA_ATENDIDA
 * cuya clave coincide con la de la alerta (RN05.3).
 */
@Service
public class AlertaService {
    
    // Claves estables: se usan como identificador en la auditoria al atender
    public static final String VENCIMIENTO_CRITICO = "VENCIMIENTO_CRITICO";
    public static final String VENCIMIENTO_PROXIMO = "VENCIMIENTO_PROXIMO";
    public static final String LOTE_VENCIDO_ESTANTE = "LOTE_VENCIDO_ESTANTE";
    public static final String STOCK_CRITICO = "STOCK_CRITICO";
    public static final String ORDEN_SUGERIDA = "ORDEN_SUGERIDA";
    
    private final LoteRepository loteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ConfiguracionService configuracionService;
    private final OrdenCompraService ordenCompraService;
    private final AuditoriaService auditoriaService;
    
    public AlertaService(LoteRepository loteRepository,
                         MedicamentoRepository medicamentoRepository,
                         ConfiguracionService configuracionService,
                         OrdenCompraService ordenCompraService,
                         AuditoriaService auditoriaService) {
        this.loteRepository = loteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.configuracionService = configuracionService;
        this.ordenCompraService = ordenCompraService;
        this.auditoriaService = auditoriaService;
    }
    
    /**
     * RN05.1: los avisos que el sistema calcula al abrir cada pantalla.
     */
    @Transactional(readOnly = true)
    public List<AlertaResponse> getAll() {
        LocalDate hoy = LocalDate.now();
        int diasCritico = configuracionService.getDiasCritico();
        int diasProximo = configuracionService.getDiasProximo();
        
        List<AlertaResponse> alertas = new ArrayList<>();
        alertas.addAll(alertasVencimiento(hoy, diasCritico, diasProximo));
        alertas.addAll(alertasVencidoEstante(hoy));
        alertas.addAll(alertasStockCritico(hoy));
        alertas.addAll(alertasOrdenSugerida());
        
        marcarAtendidas(alertas);
        
        return alertas.stream()
                .sorted((a, b) -> {
                    int p = b.getPrioridad().compareTo(a.getPrioridad());
                    return p != 0 ? p : a.getClave().compareTo(b.getClave());
                })
                .collect(Collectors.toList());
    }
    
    /**
     * RN04: los lotes que vencen dentro de la ventana se muestran en el semaforo,
     * pero un lote nunca se marca vencido antes de tiempo.
     */
    private List<AlertaResponse> alertasVencimiento(LocalDate hoy, int diasCritico, int diasProximo) {
        List<AlertaResponse> alertas = new ArrayList<>();
        
        List<Lote> criticos = loteRepository.findVenciendo(hoy, hoy.plusDays(diasCritico));
        if (!criticos.isEmpty()) {
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("cantidadLotes", criticos.size());
            datos.put("diasUmbral", diasCritico);
            datos.put("loteMasCercano", criticos.get(0).getNumeroLote());
            datos.put("diasRestantes", criticos.get(0).getDiasRestantes());
            
            alertas.add(AlertaResponse.builder()
                    .clave(VENCIMIENTO_CRITICO)
                    .tipo(AlertaResponse.TipoAlerta.PROXIMO_VENCER)
                    .prioridad(AlertaResponse.Prioridad.ALTA)
                    .titulo("Lotes por vencer en " + diasCritico + " dias o menos")
                    .mensaje(criticos.size() + " lote(s) vencen dentro de " + diasCritico
                            + " dias. Revise la dispensacion o prepare la baja.")
                    .referenciaTipo("Lote")
                    .datos(datos)
                    .build());
        }
        
        List<Lote> proximos = loteRepository.findVenciendo(hoy.plusDays(diasCritico), hoy.plusDays(diasProximo));
        if (!proximos.isEmpty()) {
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("cantidadLotes", proximos.size());
            datos.put("desde", diasCritico + 1);
            datos.put("hasta", diasProximo);
            
            alertas.add(AlertaResponse.builder()
                    .clave(VENCIMIENTO_PROXIMO)
                    .tipo(AlertaResponse.TipoAlerta.PROXIMO_VENCER)
                    .prioridad(AlertaResponse.Prioridad.MEDIA)
                    .titulo("Lotes proximos a vencer")
                    .mensaje(proximos.size() + " lote(s) vencen entre " + (diasCritico + 1)
                            + " y " + diasProximo + " dias.")
                    .referenciaTipo("Lote")
                    .datos(datos)
                    .build());
        }
        
        return alertas;
    }
    
    /**
     * RN03: vencido en el estante es el aviso mas urgente, se da de baja de inmediato.
     */
    private List<AlertaResponse> alertasVencidoEstante(LocalDate hoy) {
        List<Lote> vencidos = loteRepository.findVencidosConStock(hoy);
        List<AlertaResponse> alertas = new ArrayList<>();
        
        for (Lote lote : vencidos) {
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("loteId", lote.getId());
            datos.put("numeroLote", lote.getNumeroLote());
            datos.put("medicamento", lote.getMedicamento().getNombre());
            datos.put("cantidad", lote.getCantidadActual());
            datos.put("fechaVencimiento", lote.getFechaVencimiento());
            datos.put("diasVencido", Math.abs(lote.getDiasRestantes()));
            
            alertas.add(AlertaResponse.builder()
                    .clave(LOTE_VENCIDO_ESTANTE + ":" + lote.getId())
                    .tipo(AlertaResponse.TipoAlerta.VENCIDO)
                    .prioridad(AlertaResponse.Prioridad.ALTA)
                    .titulo("Lote vencido en el estante: " + lote.getNumeroLote())
                    .mensaje(lote.getMedicamento().getNombre() + " tiene " + lote.getCantidadActual()
                            + " " + lote.getMedicamento().getUnidadMedida()
                            + " vencido(s). No se puede dispensar y debe darse de baja.")
                    .referenciaId(lote.getId())
                    .referenciaTipo("Lote")
                    .datos(datos)
                    .build());
        }
        
        return alertas;
    }
    
    /**
     * RN06: medicamentos que igualaron o quedaron por debajo de su stock minimo.
     */
    private List<AlertaResponse> alertasStockCritico(LocalDate hoy) {
        List<AlertaResponse> alertas = new ArrayList<>();
        List<Medicamento> medicamentos = medicamentoRepository.findAllActivosWithProveedor();
        
        for (Medicamento medicamento : medicamentos) {
            int stockVigente = loteRepository.getStockVigente(medicamento.getId(), hoy);
            if (!medicamento.isStockCritico(stockVigente)) {
                continue;
            }
            
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("medicamentoId", medicamento.getId());
            datos.put("codigo", medicamento.getCodigo());
            datos.put("stockVigente", stockVigente);
            datos.put("stockMinimo", medicamento.getStockMinimo());
            datos.put("cantidadSugerida", medicamento.getCantidadSugerida(stockVigente));
            datos.put("proveedorRazonSocial", medicamento.getProveedor().getRazonSocial());
            
            alertas.add(AlertaResponse.builder()
                    .clave(STOCK_CRITICO + ":" + medicamento.getId())
                    .tipo(AlertaResponse.TipoAlerta.STOCK_CRITICO)
                    .prioridad(AlertaResponse.Prioridad.ALTA)
                    .titulo("Stock critico: " + medicamento.getNombre())
                    .mensaje(medicamento.getNombre() + " tiene " + stockVigente + " "
                            + medicamento.getUnidadMedida() + " y su minimo es "
                            + medicamento.getStockMinimo() + ".")
                    .referenciaId(medicamento.getId())
                    .referenciaTipo("Medicamento")
                    .datos(datos)
                    .build());
        }
        
        return alertas;
    }
    
    /**
     * RF-09: queda pendiente mientras el sistema tenga una propuesta sin convertir en orden.
     */
    private List<AlertaResponse> alertasOrdenSugerida() {
        List<SugerenciaCompraResponse> sugerencias = ordenCompraService.getSugerencias();
        if (sugerencias.isEmpty()) {
            return List.of();
        }
        
        int unidades = sugerencias.stream()
                .mapToInt(s -> s.getCantidadSugerida() != null ? s.getCantidadSugerida() : 0)
                .sum();
        
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("cantidadSugerencias", sugerencias.size());
        datos.put("totalUnidades", unidades);
        
        return List.of(AlertaResponse.builder()
                .clave(ORDEN_SUGERIDA)
                .tipo(AlertaResponse.TipoAlerta.ORDEN_SUGERIDA)
                .prioridad(AlertaResponse.Prioridad.MEDIA)
                .titulo("Pedido sugerido disponible")
                .mensaje("El sistema propone abastecer " + sugerencias.size() + " medicamento(s). "
                        + "Logistica revisa y el Jefe de Farmacia aprueba.")
                .referenciaTipo("OrdenCompra")
                .datos(datos)
                .build());
    }
    
    /**
     * RN05.3: una alerta esta atendida si la auditoria registra ALERTA_ATENDIDA
     * con esa misma clave. No hay tabla de alertas.
     */
    private void marcarAtendidas(List<AlertaResponse> alertas) {
        List<Auditoria> registros = auditoriaService.getAlertasAtendidas();
        Map<String, Auditoria> porClave = new LinkedHashMap<>();
        for (Auditoria auditoria : registros) {
            String clave = claveDe(auditoria);
            if (clave != null) {
                porClave.put(clave, auditoria);
            }
        }
        
        for (AlertaResponse alerta : alertas) {
            Auditoria auditoria = porClave.get(alerta.getClave());
            if (auditoria != null) {
                alerta.setAtendida(true);
                alerta.setAtendidaEn(auditoria.getFechaHora());
                alerta.setAtendidaPor(auditoria.getUsuario() != null
                        ? auditoria.getUsuario().getNombreCompleto() : null);
            } else {
                alerta.setAtendida(false);
            }
        }
    }
    
    /**
     * La clave de la alerta queda en el campo descripcion con el prefijo "CLAVE:".
     */
    private String claveDe(Auditoria auditoria) {
        String descripcion = auditoria.getDescripcion();
        if (descripcion != null && descripcion.startsWith("CLAVE:")) {
            return descripcion.substring("CLAVE:".length()).trim();
        }
        return auditoria.getValorNuevo();
    }
    
    /**
     * RN05.3: marcar como atendida solo escribe en auditoria.
     */
    @Transactional
    public void atender(AtenderAlertaRequest request, Usuario user, HttpServletRequest httpRequest) {
        if (request.getReferenciaId() == null || request.getReferenciaId().isBlank()) {
            throw new RuntimeException("La alerta atendida debe indicar su clave");
        }
        
        String descripcion = "CLAVE:" + request.getReferenciaId().trim();
        if (request.getComentario() != null && !request.getComentario().isBlank()) {
            descripcion += " | " + request.getComentario().trim();
        }
        
        auditoriaService.log(Auditoria.Accion.ALERTA_ATENDIDA, user, "Alerta", request.getReferenciaId(),
                descripcion, null, request.getTipo(), httpRequest);
    }
    
    @Transactional(readOnly = true)
    public long countTotal() {
        return getAll().size();
    }
    
    @Transactional(readOnly = true)
    public long countAltaPrioridad() {
        return getAll().stream()
                .filter(a -> a.getPrioridad() == AlertaResponse.Prioridad.ALTA)
                .filter(a -> !Boolean.TRUE.equals(a.getAtendida()))
                .count();
    }
    
    @Transactional(readOnly = true)
    public List<AlertaResponse> getPendientes() {
        return getAll().stream()
                .filter(a -> !Boolean.TRUE.equals(a.getAtendida()))
                .collect(Collectors.toList());
    }
}
