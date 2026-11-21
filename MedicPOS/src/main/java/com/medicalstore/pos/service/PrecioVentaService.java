package com.medicalstore.pos.service;

import com.medicalstore.pos.entity.PrecioVenta;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.PrecioVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Precios de venta por medicamento (tabla propia: no altera la tabla medicamento). */
@Service
public class PrecioVentaService {

    private final PrecioVentaRepository repository;

    public PrecioVentaService(PrecioVentaRepository repository) {
        this.repository = repository;
    }

    /** Precio vigente del medicamento; cero si aun no tiene precio. */
    @Transactional(readOnly = true)
    public BigDecimal precioDe(Long medicamentoId) {
        return repository.findById(medicamentoId).map(PrecioVenta::getPrecio).orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> preciosDe(Collection<Long> medicamentoIds) {
        Map<Long, BigDecimal> mapa = new HashMap<>();
        repository.findAllById(medicamentoIds).forEach(p -> mapa.put(p.getMedicamentoId(), p.getPrecio()));
        return mapa;
    }

    /** Crea o actualiza el precio. Si precio es null no hace nada (se conserva el actual). */
    @Transactional
    public void guardar(Long medicamentoId, BigDecimal precio, Usuario user) {
        if (precio == null) {
            return;
        }
        repository.save(PrecioVenta.builder()
                .medicamentoId(medicamentoId)
                .precio(precio.setScale(2, RoundingMode.HALF_UP))
                .updatedBy(user != null ? user.getId() : null)
                .updatedAt(LocalDateTime.now())
                .build());
    }
}
