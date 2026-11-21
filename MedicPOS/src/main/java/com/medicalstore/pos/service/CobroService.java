package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.PagarCobroRequest;
import com.medicalstore.pos.dto.response.CobroResponse;
import com.medicalstore.pos.entity.Cobro;
import com.medicalstore.pos.entity.DetalleDispensacion;
import com.medicalstore.pos.entity.Dispensacion;
import com.medicalstore.pos.entity.EstadoCobro;
import com.medicalstore.pos.entity.MedicamentoPrecio;
import com.medicalstore.pos.entity.MetodoPago;
import com.medicalstore.pos.entity.Receta;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.exception.BusinessRuleException;
import com.medicalstore.pos.exception.ResourceNotFoundException;
import com.medicalstore.pos.repository.CobroRepository;
import com.medicalstore.pos.repository.MedicamentoPrecioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CobroService {

    public static final String CLAVE_DESCUENTO_SIS = "cobro.descuento.sis.porcentaje";
    public static final String CLAVE_DESCUENTO_ASEGURADO = "cobro.descuento.asegurado.porcentaje";

    private final CobroRepository cobroRepository;
    private final MedicamentoPrecioRepository precioRepository;
    private final MedicamentoPrecioService precioService;
    private final ConfiguracionService configuracionService;
    private final AuditoriaService auditoriaService;

    public CobroService(CobroRepository cobroRepository,
                        MedicamentoPrecioRepository precioRepository,
                        MedicamentoPrecioService precioService,
                        ConfiguracionService configuracionService,
                        AuditoriaService auditoriaService) {
        this.cobroRepository = cobroRepository;
        this.precioRepository = precioRepository;
        this.precioService = precioService;
        this.configuracionService = configuracionService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Cobro calcularYCrear(Dispensacion dispensacion, Usuario user, HttpServletRequest httpRequest) {
        Receta receta = dispensacion.getReceta();
        String tipoReceta = receta.getTipoReceta().name();

        // ASEGURADO no genera cobro (total = 0)
        if ("ASEGURADO".equals(tipoReceta)) {
            return null;
        }

        // Verificar si ya existe cobro para esta dispensación
        if (cobroRepository.existsByDispensacionId(dispensacion.getId())) {
            throw new BusinessRuleException("COBRO_YA_EXISTE", "Ya existe un cobro para esta dispensación");
        }

        // Obtener % descuento según tipo de receta
        BigDecimal descuentoPorcentaje = obtenerDescuentoPorTipo(tipoReceta);

        // Calcular subtotal sumando (cantidad × precio_venta) de cada item
        BigDecimal subtotal = BigDecimal.ZERO;
        StringBuilder detalleCalculo = new StringBuilder();

        for (DetalleDispensacion item : dispensacion.getDetalles()) {
            Long medicamentoId = item.getMedicamento().getId();
            Integer cantidad = item.getCantidad();

            // Buscar precio vigente al momento de la dispensación
            LocalDate fechaDisp = dispensacion.getFechaHora() != null
                    ? dispensacion.getFechaHora().toLocalDate()
                    : LocalDate.now();

            BigDecimal precioVenta = precioRepository.findVigentesByMedicamentoYFecha(medicamentoId, fechaDisp)
                    .stream()
                    .findFirst()
                    .map(MedicamentoPrecio::getPrecioVenta)
                    .orElse(null);

            // Validar precio obligatorio
            if (precioVenta == null && precioService.esPrecioObligatorioParaTipo(tipoReceta)) {
                throw new BusinessRuleException("PRECIO_NO_CONFIGURADO",
                        "No hay precio de venta vigente para: " + item.getMedicamento().getNombre()
                                + ". Configure un precio en Módulo Precios.");
            }

            if (precioVenta != null) {
                BigDecimal subtotalItem = precioVenta.multiply(BigDecimal.valueOf(cantidad));
                subtotal = subtotal.add(subtotalItem);
                detalleCalculo.append(item.getMedicamento().getNombre())
                        .append(" x").append(cantidad)
                        .append(" @ S/ ").append(precioVenta)
                        .append(" = S/ ").append(subtotalItem.setScale(2, RoundingMode.HALF_UP))
                        .append("; ");
            }
        }

        // Calcular descuento y total
        BigDecimal descuentoMonto = subtotal.multiply(descuentoPorcentaje).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(descuentoMonto);

        // Crear registro de cobro
        Cobro cobro = Cobro.builder()
                .dispensacion(dispensacion)
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .descuentoPorcentaje(descuentoPorcentaje.setScale(2, RoundingMode.HALF_UP))
                .descuentoMonto(descuentoMonto.setScale(2, RoundingMode.HALF_UP))
                .total(total.setScale(2, RoundingMode.HALF_UP))
                .estado(EstadoCobro.PENDIENTE)
                .createdBy(user != null ? user.getId() : null)
                .build();

        cobro = cobroRepository.save(cobro);

        // Auditoría
        String descAuditoria = String.format("Cobro generado: Subtotal S/ %.2f | Desc. %.0f%% (S/ %.2f) | Total S/ %.2f | Tipo: %s",
                subtotal, descuentoPorcentaje, descuentoMonto, total, tipoReceta);

        auditoriaService.log(
                com.medicalstore.pos.entity.Auditoria.Accion.COBRO_CREADO,
                user, "Cobro", cobro.getId().toString(),
                descAuditoria,
                null, cobro.toString(), httpRequest);

        return cobro;
    }

    private BigDecimal obtenerDescuentoPorTipo(String tipoReceta) {
        switch (tipoReceta) {
            case "ASEGURADO":
                return configuracionService.getBigDecimal(CLAVE_DESCUENTO_ASEGURADO, BigDecimal.valueOf(100));
            case "SIS":
                return configuracionService.getBigDecimal(CLAVE_DESCUENTO_SIS, BigDecimal.valueOf(50));
            case "PARTICULAR":
            default:
                return BigDecimal.ZERO;
        }
    }

    @Transactional
    public CobroResponse pagar(Long cobroId, PagarCobroRequest request, Usuario user, HttpServletRequest httpRequest) {
        Cobro cobro = cobroRepository.findById(cobroId)
                .orElseThrow(() -> new ResourceNotFoundException("Cobro no encontrado con id: " + cobroId));

        if (cobro.getEstado() == EstadoCobro.PAGADO) {
            throw new BusinessRuleException("COBRO_YA_PAGADO", "El cobro ya está pagado");
        }
        if (cobro.getEstado() == EstadoCobro.ANULADO) {
            throw new BusinessRuleException("COBRO_ANULADO", "No se puede pagar un cobro anulado");
        }

        String valorAnterior = cobro.toString();
        cobro.marcarPagado(user, request.getMetodoPago(), request.getObservacion());
        cobro = cobroRepository.save(cobro);

        auditoriaService.log(
                com.medicalstore.pos.entity.Auditoria.Accion.COBRO_ACTUALIZADO,
                user, "Cobro", cobro.getId().toString(),
                "Cobro pagado: " + request.getMetodoPago(),
                valorAnterior, cobro.toString(), httpRequest);

        return mapToResponse(cobro);
    }

    @Transactional
    public Cobro anularPorDispensacion(Long dispensacionId, Usuario user, HttpServletRequest httpRequest) {
        Cobro cobro = cobroRepository.findByDispensacionId(dispensacionId)
                .orElse(null);

        if (cobro != null && cobro.getEstado() == EstadoCobro.PENDIENTE) {
            String valorAnterior = cobro.toString();
            cobro.anular(user);
            cobro = cobroRepository.save(cobro);

            auditoriaService.log(
                    com.medicalstore.pos.entity.Auditoria.Accion.COBRO_ANULADO,
                    user, "Cobro", cobro.getId().toString(),
                    "Cobro anulado por anulación de dispensación",
                    valorAnterior, cobro.toString(), httpRequest);
        }
        return cobro;
    }

    @Transactional(readOnly = true)
    public List<CobroResponse> getAll() {
        return cobroRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CobroResponse getById(Long id) {
        Cobro cobro = cobroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cobro no encontrado con id: " + id));
        return mapToResponse(cobro);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<CobroResponse> getByDispensacionId(Long dispensacionId) {
        return cobroRepository.findByDispensacionId(dispensacionId)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<CobroResponse> getByRangoFechas(java.time.LocalDateTime desde, java.time.LocalDateTime hasta) {
        return cobroRepository.findByRangoFechas(desde, hasta).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CobroResponse mapToResponse(Cobro c) {
        return CobroResponse.builder()
                .id(c.getId())
                .dispensacionId(c.getDispensacion().getId())
                .numeroTicket(c.getDispensacion().getNumeroTicket())
                .tipoReceta(c.getDispensacion().getReceta().getTipoReceta().name())
                .pacienteNombre(c.getDispensacion().getReceta().getPaciente() != null
                        ? c.getDispensacion().getReceta().getPaciente().getNombreCompleto() : null)
                .subtotal(c.getSubtotal())
                .descuentoPorcentaje(c.getDescuentoPorcentaje())
                .descuentoMonto(c.getDescuentoMonto())
                .total(c.getTotal())
                .estado(c.getEstado())
                .metodoPago(c.getMetodoPago())
                .observacion(c.getObservacion())
                .pagadoEn(c.getPagadoEn())
                .pagadoPorNombre(c.getPagadoPor() != null ? c.getPagadoPor().getNombreCompleto() : null)
                .createdAt(c.getCreatedAt())
                .build();
    }
}