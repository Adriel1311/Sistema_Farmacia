package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.ReporteBajasResponse;
import com.medicalstore.pos.dto.response.ReporteConsumoResponse;
import com.medicalstore.pos.dto.response.ReporteInventarioResponse;
import com.medicalstore.pos.dto.response.ReporteOrdenesResponse;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.ReporteExportService;
import com.medicalstore.pos.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "M08 Reportes", description = "Consumo, inventario, bajas y ordenes de compra (RF-12). Solo lectura")
public class ReporteController extends BaseController {
    
    private final ReporteService reporteService;
    private final ReporteExportService exportService;
    
    public ReporteController(ReporteService reporteService, ReporteExportService exportService,
                             UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.reporteService = reporteService;
        this.exportService = exportService;
    }
    
    @GetMapping("/consumo")
    @Operation(summary = "Consumo por medicamento en un rango")
    public ResponseEntity<ReporteConsumoResponse> consumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        return ResponseEntity.ok(reporteService.reporteConsumo(inicio, fin));
    }
    
    @GetMapping("/inventario")
    @Operation(summary = "Inventario vigente y vencido", description = "RN06: el vencido se reporta aparte, no suma al vigente")
    public ResponseEntity<ReporteInventarioResponse> inventario() {
        return ResponseEntity.ok(reporteService.reporteInventario());
    }
    
    @GetMapping("/bajas")
    @Operation(summary = "Bajas de inventario en un rango")
    public ResponseEntity<ReporteBajasResponse> bajas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        return ResponseEntity.ok(reporteService.reporteBajas(inicio, fin));
    }
    
    @GetMapping("/ordenes")
    @Operation(summary = "Historial de ordenes de compra en un rango")
    public ResponseEntity<ReporteOrdenesResponse> ordenes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        return ResponseEntity.ok(reporteService.reporteOrdenes(inicio, fin));
    }
    
    @GetMapping("/{tipo}/pdf")
    @Operation(summary = "Exporta el reporte en PDF",
            description = "Tipos: consumo, inventario, bajas, ordenes")
    public ResponseEntity<byte[]> pdf(
            @PathVariable String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        String tipoNormalizado = validarTipo(tipo);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        Usuario usuario = currentUser();
        byte[] pdf = exportService.pdf(tipoNormalizado, inicio, fin,
                usuario != null ? usuario.getUsuario() : "sistema",
                usuario != null ? usuario.getNombreRol() : null);
        return downloadable(pdf, "application/pdf", exportService.nombreArchivo(tipoNormalizado, "pdf"));
    }
    
    @GetMapping("/{tipo}/xlsx")
    @Operation(summary = "Exporta el reporte en XLSX (Excel)",
            description = "Tipos: consumo, inventario, bajas, ordenes")
    public ResponseEntity<byte[]> xlsx(
            @PathVariable String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        String tipoNormalizado = validarTipo(tipo);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        Usuario usuario = currentUser();
        byte[] archivo = exportService.xlsx(tipoNormalizado, inicio, fin,
                usuario != null ? usuario.getUsuario() : "sistema",
                usuario != null ? usuario.getNombreRol() : null);
        return downloadable(archivo,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                exportService.nombreArchivo(tipoNormalizado, "xlsx"));
    }
    
    private String validarTipo(String tipo) {
        String normalizado = tipo.toLowerCase();
        if (!ReporteExportService.TIPOS.contains(normalizado)) {
            throw new RuntimeException("Reporte no soportado: " + tipo
                    + ". Use uno de: " + String.join(", ", ReporteExportService.TIPOS));
        }
        return normalizado;
    }
    
    private ResponseEntity<byte[]> downloadable(byte[] contenido, String contentType, String nombreArchivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .body(contenido);
    }
}
