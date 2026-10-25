package com.medicalstore.pos.service;

import com.medicalstore.pos.repository.LoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concentra los calculos de stock derivados de los lotes.
 * Regla central (RN06): solo cuenta lo vigente, lo vencido no suma.
 */
@Service
public class StockService {
    
    private final LoteRepository loteRepository;
    
    public StockService(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }
    
    /**
     * Stock vigente: no vencido, no dado de baja y con existencias.
     */
    @Transactional(readOnly = true)
    public int getStockVigente(Long medicamentoId, LocalDate hoy) {
        Integer stock = loteRepository.getStockVigente(medicamentoId, hoy);
        return stock != null ? stock : 0;
    }
    
    @Transactional(readOnly = true)
    public Map<Long, Integer> getStockVigentePorIds(List<Long> medicamentoIds, LocalDate hoy) {
        if (medicamentoIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, Integer> mapa = new HashMap<>();
        for (Object[] fila : loteRepository.getStockVigentePorIds(medicamentoIds, hoy)) {
            mapa.put(((Number) fila[0]).longValue(), ((Number) fila[1]).intValue());
        }
        return mapa;
    }
    
    /**
     * Stock vencido que aun permanece fisicamente en el estante (RN03).
     */
    @Transactional(readOnly = true)
    public int getStockVencido(Long medicamentoId, LocalDate hoy) {
        return loteRepository.getStockVencido(medicamentoId, hoy);
    }
    
    @Transactional(readOnly = true)
    public Map<Long, Integer> getStockVencidoPorIds(List<Long> medicamentoIds, LocalDate hoy) {
        if (medicamentoIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, Integer> mapa = new HashMap<>();
        for (Object[] fila : loteRepository.getStockVencidoPorIds(medicamentoIds, hoy)) {
            mapa.put(((Number) fila[0]).longValue(), ((Number) fila[1]).intValue());
        }
        return mapa;
    }
    
    @Transactional(readOnly = true)
    public long countLotesActivos(Long medicamentoId) {
        return loteRepository.countByMedicamentoIdAndCantidadActualGreaterThan(medicamentoId, 0);
    }
    
    @Transactional(readOnly = true)
    public long countLotesVencidos(Long medicamentoId, LocalDate hoy) {
        return loteRepository.countLotesVencidos(medicamentoId, hoy);
    }
}
