package com.medicalstore.pos.validator;

import com.medicalstore.pos.repository.PacienteRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueDniValidator implements ConstraintValidator<UniqueDni, String> {

    private final PacienteRepository pacienteRepository;

    @Override
    public boolean isValid(String dni, ConstraintValidatorContext context) {
        if (dni == null || dni.isBlank()) {
            return true;
        }
        if (!dni.matches("^\\d{8}$")) {
            return true; // Deja que @Pattern valide el formato
        }
        return !pacienteRepository.existsByDni(dni);
    }
}