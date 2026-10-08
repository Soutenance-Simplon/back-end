// Déclaration du package Java : `com.diamyaraam.medecin.init`
package com.diamyaraam.medecin.init;

// Import de la classe `OnmsReferenceRepository` (paquet com.diamyaraam.medecin.repository)
import com.diamyaraam.medecin.repository.OnmsReferenceRepository;
// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `CommandLineRunner` (paquet org.springframework.boot)
import org.springframework.boot.CommandLineRunner;
// Import de la classe `ClassPathResource` (paquet org.springframework.core.io)
import org.springframework.core.io.ClassPathResource;
// Import de la classe `ResourceDatabasePopulator` (paquet org.springframework.jdbc.datasource.init)
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
// Import de la classe `Component` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Component;

// Import de la classe `DataSource` (paquet javax.sql)
import javax.sql.DataSource;

/**
 * Initialiseur automatique de la base de référence ONMS à partir de data-onms.sql (3059 médecins)
 */
@Component
// Déclaration de la classe `OnmsDataInitializer`, et implémente CommandLineRunner
public class OnmsDataInitializer implements CommandLineRunner {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(OnmsDataInitializer.class)`
    private static final Logger log = LoggerFactory.getLogger(OnmsDataInitializer.class);

    // Attribut `onmsRepository` de type OnmsReferenceRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final OnmsReferenceRepository onmsRepository;
    // Attribut `dataSource` de type DataSource — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final DataSource dataSource;

    // Constructeur de `OnmsDataInitializer` — paramètres : `onmsRepository` (OnmsReferenceRepository), `dataSource` (DataSource) (injection des dépendances par Spring)
    public OnmsDataInitializer(OnmsReferenceRepository onmsRepository, DataSource dataSource) {
        // Initialise l'attribut `onmsRepository` avec la valeur de onmsRepository
        this.onmsRepository = onmsRepository;
        // Initialise l'attribut `dataSource` avec la valeur de dataSource
        this.dataSource = dataSource;
    }

    // Redéfinit une méthode héritée de la classe parente ou de l'interface
    @Override
    // Méthode `run` (publique) — paramètres : `args` (String...) ; retourne : aucune valeur
    public void run(String... args) throws Exception {
        // Déclare la variable `count` (entier long) initialisée avec le résultat de la requête count exécutée via onmsRepository
        long count = onmsRepository.count();
        // Écrit un message informatif dans les journaux : "Vérification de la base ONMS de référence : {} médecins enregistrés.…
        log.info("Vérification de la base ONMS de référence : {} médecins enregistrés.", count);

        // Condition : exécute le bloc suivant seulement si `count == 0`
        if (count == 0) {
            // Écrit un message informatif dans les journaux : "Base ONMS vide. Démarrage de l'importation automatique des 3059 méde…
            log.info("Base ONMS vide. Démarrage de l'importation automatique des 3059 médecins de référence...");
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Déclare la variable `populator` (ResourceDatabasePopulator) initialisée avec une nouvelle instance de ResourceDatabasePopulator
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                // Appelle la méthode `addScript` sur `populator` : populator.addScript(new ClassPathResource("data-onms.sql"));
                populator.addScript(new ClassPathResource("data-onms.sql"));
                // Renseigne la propriété ContinueOnError de `populator` avec le booléen vrai
                populator.setContinueOnError(true);
                // Appelle la méthode `execute` sur `populator` : populator.execute(dataSource);
                populator.execute(dataSource);
                // Écrit un message informatif dans les journaux : "Importation terminée avec succès ! Nombre de médecins ONMS désormais…
                log.info("Importation terminée avec succès ! Nombre de médecins ONMS désormais enregistrés : {}", onmsRepository.count());
            // Interception de l'exception Exception e
            } catch (Exception e) {
                // Écrit un message d'erreur dans les journaux : "Erreur lors de l'importation de data-onms.sql : {}", e.getMessage(),…
                log.error("Erreur lors de l'importation de data-onms.sql : {}", e.getMessage(), e);
            }
        }
    }
}
