package fr.saintmichel.sirene;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Une ligne de la table "mytable" (créée par le script SQL fourni)
// Champs publics : pas besoin de getters/setters
@Entity
@Table(name = "mytable")
public class Entreprise {

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // le SIREN vient de l'URL, jamais du JSON
    public Integer siren;

    @Column(name = "date_creation_unite_legale")
    public LocalDate dateCreation;

    @Size(max = 20)
    @Column(name = "prenom_unite_legale")
    public String prenom;

    @Size(max = 29)
    @Column(name = "nom_unite_legale")
    public String nom;

    @Size(max = 115)
    @Column(name = "denomination_unite_legale")
    public String denomination;

    @Size(max = 6)
    @Column(name = "activite_principale_unite_legale")
    public String activitePrincipale;

    @NotBlank
    @Size(max = 2)
    @Column(name = "tranche_effectifs_unite_legale")
    public String trancheEffectifs;

    @NotBlank
    @Pattern(regexp = "[AC]", message = "doit valoir A (active) ou C (cessée)")
    @Column(name = "etat_administratif_unite_legale")
    public String etatAdministratif;
}
