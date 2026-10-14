package fr.saintmichel.sirene;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/entreprises")
public class EntrepriseController {

    private final EntrepriseRepository repository;

    public EntrepriseController(EntrepriseRepository repository) {
        this.repository = repository;
    }

    // GET /entreprises?activite=62.01Z&denomination=boulang&page=0&size=20
    @GetMapping
    public Page<Entreprise> search(@RequestParam(required = false) String activite,
                                   @RequestParam(required = false) String denomination,
                                   @PageableDefault(sort = "siren") Pageable pageable) {
        if (activite != null && denomination != null) {
            return repository.findByActivitePrincipaleAndDenominationContainingIgnoreCase(activite, denomination, pageable);
        }
        if (activite != null) {
            return repository.findByActivitePrincipale(activite, pageable);
        }
        if (denomination != null) {
            return repository.findByDenominationContainingIgnoreCase(denomination, pageable);
        }
        return repository.findAll(pageable);
    }

    // GET /entreprises/325175
    @GetMapping("/{siren}")
    public Entreprise get(@PathVariable Integer siren) {
        return findOr404(siren);
    }

    // PUT /entreprises/325175 + JSON
    @PutMapping("/{siren}")
    public Entreprise update(@PathVariable Integer siren, @Valid @RequestBody Entreprise entreprise) {
        findOr404(siren);
        entreprise.siren = siren;
        return repository.save(entreprise);
    }

    // DELETE /entreprises/325175
    @DeleteMapping("/{siren}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer siren) {
        repository.delete(findOr404(siren));
    }

    private Entreprise findOr404(Integer siren) {
        return repository.findById(siren).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune entreprise avec le SIREN " + siren));
    }
}
