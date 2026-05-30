package com.hiberadar.common.init;

import com.hiberadar.domain.grant.entity.Institution;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.repository.InstitutionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InstitutionInitializer implements CommandLineRunner {

    private final InstitutionRepository institutionRepository;

    public InstitutionInitializer(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if institutions already exist
        if (institutionRepository.count() > 0) {
            return;
        }

        // Create initial institutions
        institutionRepository.save(new Institution(
                "Küçük ve Orta Ölçekli İşletmeleri Geliştirme ve Destekleme İdaresi Başkanlığı",
                "KOSGEB",
                "/institution-logos/kosgeb.png",
                InstitutionScope.NATIONAL));

        institutionRepository.save(new Institution(
                "Türkiye Bilimsel ve Teknolojik Araştırma Kurumu",
                "TUBITAK",
                "/institution-logos/tubitak.png",
                InstitutionScope.NATIONAL));

        institutionRepository.save(new Institution(
                "Horizon Europe",
                "HORIZON",
                "/institution-logos/horizon-europe.png",
                InstitutionScope.INTERNATIONAL));

        institutionRepository.save(new Institution(
                "Türkiye Cumhuriyeti Ticaret Bakanlığı",
                "TICARET",
                "/institution-logos/ticaret-bakanligi.png",
                InstitutionScope.NATIONAL));

        institutionRepository.save(new Institution(
                "Avrupa İnovasyonu ve Teknoloji Enstitüsü",
                "EIT",
                "/institution-logos/eit.png",
                InstitutionScope.INTERNATIONAL));
    }
}
