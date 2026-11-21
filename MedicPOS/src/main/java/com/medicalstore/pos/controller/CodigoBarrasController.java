package com.medicalstore.pos.controller;

import com.medicalstore.pos.entity.Dispensacion;
import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.Paciente;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.DispensacionRepository;
import com.medicalstore.pos.repository.LoteRepository;
import com.medicalstore.pos.repository.MedicamentoRepository;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.CodigoBarrasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Genera las imagenas de codigo de barras y codigo QR que se usan en las
 * etiquetas de medicamentos y lotes y en el comprobante de entrega.
 */
@RestController
@RequestMapping("/api/codigos-barras")
@Tag(name = "Codigos de barras", description = "Etiquetas CODE 128 y QR de medicamentos, lotes y comprobantes de entrega")
public class CodigoBarrasController extends BaseController {
    
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final CodigoBarrasService codigoBarrasService;
    private final MedicamentoRepository medicamentoRepository;
    private final LoteRepository loteRepository;
    private final DispensacionRepository dispensacionRepository;
    
    public CodigoBarrasController(CodigoBarrasService codigoBarrasService,
                                  UsuarioRepository usuarioRepository,
                                  MedicamentoRepository medicamentoRepository,
                                  LoteRepository loteRepository,
                                  DispensacionRepository dispensacionRepository) {
        super(usuarioRepository);
        this.codigoBarrasService = codigoBarrasService;
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.dispensacionRepository = dispensacionRepository;
    }
    
    @GetMapping("/medicamento/{id}")
    @Operation(summary = "Etiqueta de medicamento con codigo de barras")
    public ResponseEntity<byte[]> medicamento(@PathVariable Long id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicamento not found with id: " + id));
        byte[] png = codigoBarrasService.codigoBarras(medicamento.getCodigo());
        return png(png, "medicamento-" + medicamento.getCodigo() + ".png");
    }
    
    @GetMapping("/lote/{id}")
    @Operation(summary = "Etiqueta de lote con codigo de barras")
    public ResponseEntity<byte[]> lote(@PathVariable Long id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote not found with id: " + id));
        byte[] png = codigoBarrasService.codigoBarras(lote.getNumeroLote());
        return png(png, "lote-" + lote.getNumeroLote() + ".png");
    }
    
    @GetMapping("/dispensacion/{id}")
    @Operation(summary = "Codigo QR del comprobante de entrega")
    public ResponseEntity<byte[]> dispensacion(@PathVariable Long id) {
        Dispensacion dispensacion = dispensacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dispensacion not found with id: " + id));
        byte[] png = codigoBarrasService.codigoQr(contenidoQr(dispensacion));
        return png(png, "comprobante-" + dispensacion.getNumeroTicket() + ".png");
    }
    
    /**
     * Texto que lleva el QR del comprobante: numero de ticket, paciente,
     * fecha y el detalle entregado.
     */
    private String contenidoQr(Dispensacion dispensacion) {
        StringBuilder sb = new StringBuilder();
        sb.append("Comprobante FarmaMed\n");
        sb.append("Ticket: ").append(dispensacion.getNumeroTicket()).append('\n');
        Paciente paciente = dispensacion.getReceta() != null ? dispensacion.getReceta().getPaciente() : null;
        if (paciente != null) {
            sb.append("Paciente: ").append(paciente.getNombreCompleto())
                    .append(" (").append(paciente.getDni()).append(")\n");
        }
        if (dispensacion.getFechaHora() != null) {
            sb.append("Fecha: ").append(dispensacion.getFechaHora().format(FECHA_HORA)).append('\n');
        }
        sb.append("Estado: ").append(dispensacion.getEstado() != null ? dispensacion.getEstado().name() : "").append('\n');
        sb.append("Detalle:\n");
        for (var detalle : dispensacion.getDetalles()) {
            sb.append("- ").append(detalle.getMedicamento().getNombre())
                    .append(" x").append(detalle.getCantidad())
                    .append(" (lote ").append(detalle.getLote().getNumeroLote())
                    .append(", vence ").append(detalle.getLote().getFechaVencimiento())
                    .append(")\n");
        }
        return sb.toString();
    }
    
    private ResponseEntity<byte[]> png(byte[] contenido, String nombreArchivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .header("Content-Disposition", "inline; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }
}
