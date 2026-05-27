package com.hiberadar.common.config;

import com.hiberadar.domain.eligibility.entity.EligibilityRule;
import com.hiberadar.domain.eligibility.repository.EligibilityRuleRepository;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.repository.GrantRepository;
import com.hiberadar.domain.source.entity.Source;
import com.hiberadar.domain.source.entity.enums.SourceCategory;
import com.hiberadar.domain.source.entity.enums.SourceScope;
import com.hiberadar.domain.source.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

@Component
public class DemoGrantDataSeeder implements CommandLineRunner {

    private final GrantRepository grantRepository;
    private final SourceRepository sourceRepository;
    private final EligibilityRuleRepository eligibilityRuleRepository;

    @Value("${app.seed-demo-data:true}")
    private boolean seedDemoData;

    public DemoGrantDataSeeder(GrantRepository grantRepository,
            SourceRepository sourceRepository,
            EligibilityRuleRepository eligibilityRuleRepository) {
        this.grantRepository = grantRepository;
        this.sourceRepository = sourceRepository;
        this.eligibilityRuleRepository = eligibilityRuleRepository;
    }

    @Override
    public void run(String... args) {
        if (!seedDemoData) {
            return;
        }

        Source source = sourceRepository.findByOfficialUrlIgnoreCase("https://example.org/demo-grants")
                .orElseGet(() -> {
                    Source s = new Source();
                    s.setName("Demo Public Grants");
                    s.setCategory(SourceCategory.GOV_PORTAL);
                    s.setScope(SourceScope.NATIONAL);
                    s.setCountryCode("TR");
                    s.setOfficialUrl("https://example.org/demo-grants");
                    s.setActive(true);
                    return sourceRepository.save(s);
                });

        Grant kosgebDigital = upsertGrant(
                source,
                "DEMO-TR-001",
                "KOBI Dijital Donusum Destegi 2026",
                "KOSGEB",
                "Dijital Donusum Programi",
                "Yazilim ve otomasyon yatirimi yapan KOBI'ler icin destek cagrisi.",
                GrantStatus.PUBLISHED,
                InstitutionScope.NATIONAL,
                "TR",
                "62.01",
                "TRY",
                "500000",
                "5000000",
                LocalDate.now().minusDays(20),
                LocalDate.now().plusDays(35),
                "https://example.org/grants/demo-tr-001");

        Grant tubitak1501 = upsertGrant(
                source,
                "DEMO-TR-003",
                "TEYDEB 1501 Sanayi Ar-Ge Destegi",
                "TUBITAK",
                "TEYDEB 1501",
                "Yuksek katma degerli urun gelistiren firmalara Ar-Ge destegi.",
                GrantStatus.PUBLISHED,
                InstitutionScope.NATIONAL,
                "TR",
                "72.19",
                "TRY",
                "750000",
                "6000000",
                LocalDate.now().minusDays(12),
                LocalDate.now().plusDays(40),
                "https://example.org/grants/demo-tr-003");

        Grant kosgebYatirim = upsertGrant(
                source,
                "DEMO-TR-004",
                "KOBI Uretim ve Kapasite Artis Destegi",
                "KOSGEB",
                "KOBI Yatirim Programi",
                "Uretim kapasitesi artisi ve dijital ekipman alinmasi icin destek.",
                GrantStatus.PUBLISHED,
                InstitutionScope.NATIONAL,
                "TR",
                "28.99",
                "TRY",
                "300000",
                "2500000",
                LocalDate.now().minusDays(8),
                LocalDate.now().plusDays(28),
                "https://example.org/grants/demo-tr-004");

        Grant ticaretEIhracat = upsertGrant(
                source,
                "DEMO-TR-005",
                "E-Ihracat Gelistirme Destegi",
                "Ticaret Bakanligi",
                "E-Ihracat Gelistirme",
                "Yazilim tabanli e-ihracat altyapisini gelistiren KOBI'lere finansman.",
                GrantStatus.PUBLISHED,
                InstitutionScope.NATIONAL,
                "TR",
                "62.02",
                "TRY",
                "200000",
                "1800000",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(55),
                "https://example.org/grants/demo-tr-005");

        Grant horizon = upsertGrant(
                source,
                "DEMO-EU-006",
                "Horizon Europe Cluster 4 Digital",
                "European Commission",
                "Horizon Europe",
                "Avrupa dijital teknolojiler ve veri odakli inovasyon cagrisi.",
                GrantStatus.PUBLISHED,
                InstitutionScope.INTERNATIONAL,
                "DE",
                "62.01",
                "EUR",
                "1000000",
                "8000000",
                LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(70),
                "https://example.org/grants/demo-eu-006");

        upsertGrant(
                source,
                "DEMO-EU-002",
                "AB Yesil Donusum Pilot Cagrisi 2025",
                "European Commission",
                "Green Transition Call",
                "Surdurulebilir uretim donusumu icin uluslararasi pilot cagri.",
                GrantStatus.CLOSED,
                InstitutionScope.INTERNATIONAL,
                "DE",
                "28.99",
                "EUR",
                "250000",
                "2500000",
                LocalDate.now().minusMonths(6),
                LocalDate.now().minusDays(10),
                "https://example.org/grants/demo-eu-002");

        ensureEligibility(kosgebDigital, 10, 400, "1000000", "20000000");
        ensureEligibility(tubitak1501, 5, 600, "500000", "80000000");
        ensureEligibility(kosgebYatirim, 3, 300, "250000", "15000000");
        ensureEligibility(ticaretEIhracat, 2, 200, "150000", "12000000");
        ensureEligibility(horizon, 50, 2000, "5000000", "500000000");
    }

    private Grant upsertGrant(Source source,
            String referenceCode,
            String title,
            String provider,
            String program,
            String summary,
            GrantStatus status,
            InstitutionScope scope,
            String countryCode,
            String naceCode,
            String currency,
            String fundingMin,
            String fundingMax,
            LocalDate publishedAt,
            LocalDate deadlineAt,
            String officialUrl) {
        Grant grant = grantRepository.findBySourceIdAndReferenceCodeIgnoreCase(source.getId(), referenceCode)
                .orElseGet(Grant::new);

        grant.setSource(source);
        grant.setReferenceCode(referenceCode.toUpperCase(Locale.ROOT));
        grant.setTitle(title);
        grant.setProviderName(provider);
        grant.setProgramName(program);
        grant.setSummaryShort(summary);
        grant.setAdminQuickInfo("Demo veri: filtreleme ve eslesme testleri icin kullanilir.");
        grant.setStatus(status);
        grant.setScope(scope);
        grant.setCountryCode(countryCode);
        grant.setNaceCode(naceCode);
        grant.setCurrency(currency);
        grant.setFundingMin(new BigDecimal(fundingMin));
        grant.setFundingMax(new BigDecimal(fundingMax));
        grant.setPublishedAt(publishedAt);
        grant.setDeadlineAt(deadlineAt);
        grant.setOfficialUrl(officialUrl);
        return grantRepository.save(grant);
    }

    private void ensureEligibility(Grant grant, int minEmployees, int maxEmployees, String minTurnover,
            String maxTurnover) {
        EligibilityRule rule = eligibilityRuleRepository.findByGrantId(grant.getId())
                .orElseGet(() -> {
                    EligibilityRule created = new EligibilityRule();
                    created.setGrant(grant);
                    return created;
                });

        rule.setMinEmployees(minEmployees);
        rule.setMaxEmployees(maxEmployees);
        rule.setMinTurnover(new BigDecimal(minTurnover));
        rule.setMaxTurnover(new BigDecimal(maxTurnover));
        rule.setRequiredCountryCodes("TR,DE");
        rule.setCofundingRequired(false);
        rule.setNotes("Demo uygunluk kurali");
        eligibilityRuleRepository.save(rule);
    }
}
