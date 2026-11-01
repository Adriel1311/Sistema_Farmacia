package com.medicalstore.pos.entity;

public enum TipoCobertura {
    ASEGURADO,   // 100% descuento = no cobra (cargo a seguro)
    SIS,         // 50% descuento
    PARTICULAR   // 0% descuento = precio completo
}