package com.onboarding.entity;

public enum StatutAssignment {
    EN_ATTENTE("En attente"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    EN_RETARD("En retard"),
    EN_PAUSE("En pause"),
    ANNULE("Annulé");
    
    private final String displayName;
    
    StatutAssignment(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
