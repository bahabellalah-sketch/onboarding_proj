package com.onboarding.entity;

public enum StatutChecklist {
    EN_ATTENTE("En attente"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    SAUTE("Sauté"),
    BLOQUE("Bloqué");
    
    private final String displayName;
    
    StatutChecklist(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
