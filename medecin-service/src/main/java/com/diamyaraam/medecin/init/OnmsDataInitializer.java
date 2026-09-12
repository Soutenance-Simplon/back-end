package com.diamyaraam.medecin.init;

import com.diamyaraam.medecin.repository.OnmsReferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Initialiseur automatique de la base de référence ONMS à partir de data-onms.sql (3059 médecins)
 */
@Component
public class OnmsDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OnmsDataInitializer.class);

    private final OnmsReferenceRepository onmsRepository;
    private final DataSource dataSource;

    public OnmsDataInitializer(OnmsReferenceRepository onmsRepository, DataSource dataSource) {
        this.onmsRepository = onmsRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        long count = onmsRepository.count();
        log.info("Vérification de la base ONMS de référence : {} médecins enregistrés.", count);

        if (count == 0) {
            log.info("Base ONMS vide. Démarrage de l'importation automatique des 3059 médecins de référence...");
            try {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("data-onms.sql"));
                populator.setContinueOnError(true);
                populator.execute(dataSource);
                log.info("Importation terminée avec succès ! Nombre de médecins ONMS désormais enregistrés : {}", onmsRepository.count());
            } catch (Exception e) {
                log.error("Erreur lors de l'importation de data-onms.sql : {}", e.getMessage(), e);
            }
        }
    }
}
