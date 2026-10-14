package fr.saintmichel.sirene;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring écrit le SQL tout seul à partir du nom des méthodes
public interface EntrepriseRepository extends JpaRepository<Entreprise, Integer> {

    Page<Entreprise> findByActivitePrincipale(String activite, Pageable pageable);

    Page<Entreprise> findByDenominationContainingIgnoreCase(String denomination, Pageable pageable);

    Page<Entreprise> findByActivitePrincipaleAndDenominationContainingIgnoreCase(String activite, String denomination, Pageable pageable);
}
