package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateMedicamentoPrecioRequest;
import com.medicalstore.pos.dto.request.UpdateMedicamentoPrecioRequest;
import com.medicalstore.pos.dto.response.MedicamentoPrecioResponse;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.MedicamentoPrecio;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.exception.BusinessRuleException;
import com.medicalstore.pos.exception.ResourceNotFoundException;
import com.medicalstore.pos.repository.MedicamentoPrecioRepository;
import com.medicalstore.pos.repository.MedicamentoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicamentoPrecioService {

    public static final String CLAVE_PRECIO_OBLIGATORIO_PARTICULAR = "cobro.precio.obligatorio.particular";
    public static final String CLAVE_PRECIO_OBLIGATORIO_SIS = "cobro.precio.obligatorio.sis";

    private final MedicamentoPrecioRepository precioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ConfiguracionService configuracionService;
    private final AuditoriaService auditoriaService;

    public MedicamentoPrecioService(MedicamentoPrecioRepository precioRepository,
                                     MedicamentoRepository medicamentoRepository,
                                     ConfiguracionService configuracionService,
                                     AuditoriaService auditoriaService) {
        this.precioRepository = precioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.configuracionService = configuracionService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerPrecioVigente(Long medicamentoId, LocalDate fecha) {
        List<MedicamentoPrecio> precios = precioRepository.findVigentesByMedicamentoYFecha(medicamentoId, fecha);
        if (precios.isEmpty()) {
            return null;
        }
        return precios.get(0).getPrecioVenta();
    }

    @Transactional(readOnly = true)
    public boolean tienePrecioVigente(Long medicamentoId, LocalDate fecha) {
        return obtenerPrecioVigente(medicamentoId, fecha) != null;
    }

    @Transactional(readOnly = true)
    public List<MedicamentoPrecioResponse> getAll() {
        return precioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicamentoPrecioResponse> getByMedicamento(Long medicamentoId) {
        return precioRepository.findByMedicamentoIdOrderByVigenteDesdeDesc(medicamentoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicamentoPrecioResponse crear(CreateMedicamentoPrecioRequest request, Usuario user, HttpServletRequest httpRequest) {
        Medicamento medicamento = medicamentoRepository.findById(request.getMedicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado con id: " + request.getMedicamentoId()));

        // Validar que no exista precio con misma vigencia
        if (precioRepository.existsByMedicamentoIdAndVigenteDesde(request.getMedicamentoId(), request.getVigenteDesde())) {
            throw new BusinessRuleException("PRECIO_DUPLICADO", "Ya existe un precio para este medicamento con la misma fecha de inicio");
        }

        // Cerrar precio anterior si existe y se solapa
        List<MedicamentoPrecio> preciosActuales = precioRepository.findVigentesByMedicamentoYFecha(request.getMedicamentoId(), request.getVigenteDesde());
        for (MedicamentoPrecio p : preciosActuales) {
            if (p.getVigenteHasta() == null || p.getVigenteHasta().isAfter(request.getVigenteDesde().minusDays(1))) {
                p.setVigenteHasta(request.getVigenteDesde().minusDays(1));
                precioRepository.save(p);
            }
        }

        MedicamentoPrecio precio = MedicamentoPrecio.builder()
                .medicamento(medicamento)
                .precioVenta(request.getPrecioVenta())
                .vigenteDesde(request.getVigenteDesde())
                .vigenteHasta(request.getVigenteHasta())
                .createdBy(user != null ? user.getId() : null)
                .build();

        precio = precioRepository.save(precio);

        auditoriaService.log(
                com.medicalstore.pos.entity.Auditoria.Accion.PRECIO_CREADO,
                user, "MedicamentoPrecio", precio.getId().toString(),
                "Precio creado: " + medicamento.getNombre() + " = S/ " + request.getPrecioVenta(),
                null, precio.toString(), httpRequest);

        return mapToResponse(precio);
    }

    @Transactional
    public MedicamentoPrecioResponse actualizar(Long id, UpdateMedicamentoPrecioRequest request, Usuario user, HttpServletRequest httpRequest) {
        MedicamentoPrecio precio = precioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Precio no encontrado con id: " + id));

        String valorAnterior = precio.toString();

        if (request.getPrecioVenta() != null) {
            precio.setPrecioVenta(request.getPrecioVenta());
        }
        if (request.getVigenteDesde() != null) {
            precio.setVigenteDesde(request.getVigenteDesde());
        }
        if (request.getVigenteHasta() != null) {
            precio.setVigenteHasta(request.getVigenteHasta());
        }
        precio.setUpdatedBy(user != null ? user.getId() : null);

        precio = precioRepository.save(precio);

        auditoriaService.log(
                com.medicalstore.pos.entity.Auditoria.Accion.PRECIO_ACTUALIZADO,
                user, "MedicamentoPrecio", precio.getId().toString(),
                "Precio actualizado: " + precio.getMedicamento().getNombre(),
                valorAnterior, precio.toString(), httpRequest);

        return mapToResponse(precio);
    }

    @Transactional
    public void eliminar(Long id, Usuario user, HttpServletRequest httpRequest) {
        MedicamentoPrecio precio = precioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Precio no encontrado con id: " + id));

        String valorAnterior = precio.toString();
        precioRepository.delete(precio);

        auditoriaService.log(
                com.medicalstore.pos.entity.Auditoria.Accion.PRECIO_ELIMINADO,
                user, "MedicamentoPrecio", id.toString(),
                "Precio eliminado: " + precio.getMedicamento().getNombre(),
                valorAnterior, null, httpRequest);
    }

    @Transactional(readOnly = true)
    public boolean esPrecioObligatorioParaTipo(String tipoReceta) {
        if ("PARTICULAR".equals(tipoReceta)) {
            return configuracionService.getBoolean(CLAVE_PRECIO_OBLIGATORIO_PARTICULAR, true);
        }
        if ("SIS".equals(tipoReceta)) {
            return configuracionService.getBoolean(CLAVE_PRECIO_OBLIGATORIO_SIS, true);
        }
        return false; // ASEGURADO no requiere precio
    }

    private MedicamentoPrecioResponse mapToResponse(MedicamentoPrecio p) {
        return MedicamentoPrecioResponse.builder()
                .id(p.getId())
                .medicamentoId(p.getMedicamento().getId())
                .medicamentoCodigo(p.getMedicamento().getCodigo())
                .medicamentoNombre(p.getMedicamento().getNombre())
                .precioVenta(p.getPrecioVenta())
                .vigenteDesde(p.getVigenteDesde())
                .vigenteHasta(p.getVigenteHasta())
                .createdAt(p.getCreatedAt())
                .build();
    }
}