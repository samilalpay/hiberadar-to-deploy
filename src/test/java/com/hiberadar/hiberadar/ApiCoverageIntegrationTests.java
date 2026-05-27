package com.hiberadar.hiberadar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.user.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiCoverageIntegrationTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Test
        void auth_register_login_and_bearer_access() throws Exception {
                String suffix = unique();
                String username = "auth_" + suffix;
                String password = "secret123";
                String email = username + "@mail.com";

                register(username, email, password);
                String token = loginAndGetToken(username, password);

                mockMvc.perform(get("/api/applications/me")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(token)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        void firm_registration_create_list_get_and_review() throws Exception {
                String suffix = unique();
                String firmUsername = "firmreq_" + suffix;
                String firmEmail = firmUsername + "@mail.com";
                String adminToken = createAdminAndLogin("admin_fr_" + suffix, "admin123");

                MvcResult createReq = mockMvc.perform(post("/api/auth/firm-registrations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username":"%s",
                                                  "email":"%s",
                                                  "password":"secret123",
                                                  "note":"integration test"
                                                }
                                                """.formatted(firmUsername, firmEmail)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andReturn();

                long requestId = objectMapper.readTree(createReq.getResponse().getContentAsString()).get("requestId")
                                .asLong();

                mockMvc.perform(get("/api/admin/firm-registrations")
                                .param("status", "PENDING")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());

                mockMvc.perform(get("/api/admin/firm-registrations/{id}", requestId)
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(requestId));

                mockMvc.perform(patch("/api/admin/firm-registrations/{id}/review", requestId)
                                .param("decidedBy", "admin-reviewer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "decision":"APPROVED",
                                                  "note":"approved in test"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        @Test
        void grant_user_list_detail_and_admin_management() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_grant_" + suffix, "admin123");
                long sourceId = createSource(adminToken, suffix);

                MvcResult createdGrant = mockMvc.perform(post("/api/admin/grants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "sourceId": %d,
                                                  "title": "Grant %s",
                                                  "scope": "NATIONAL",
                                                  "naceCode": "26.11",
                                                  "countryCode": "TR",
                                                  "officialUrl": "https://example.org/grants/%s",
                                                  "referenceCode": "REF-%s"
                                                }
                                                """.formatted(sourceId, suffix, suffix, suffix)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("DRAFT"))
                                .andReturn();

                long grantId = objectMapper.readTree(createdGrant.getResponse().getContentAsString()).get("id")
                                .asLong();

                mockMvc.perform(put("/api/admin/grants/{id}", grantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "title": "Grant Updated %s",
                                                  "scope": "INTERNATIONAL",
                                                  "naceCode": "26.11",
                                                  "countryCode": "DE",
                                                  "officialUrl": "https://example.org/grants/u/%s",
                                                  "referenceCode": "REF-U-%s"
                                                }
                                                """.formatted(suffix, suffix, suffix)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.scope").value("INTERNATIONAL"));

                mockMvc.perform(patch("/api/admin/grants/{id}/status", grantId)
                                .header("Authorization", bearer(adminToken))
                                .param("status", "PUBLISHED"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("PUBLISHED"));

                mockMvc.perform(get("/api/grants")
                                .param("sourceId", String.valueOf(sourceId))
                                .param("nace", "26.11")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"))
                                .andExpect(jsonPath("$.content[0].clickable").value(true));

                mockMvc.perform(patch("/api/admin/grants/{id}/active", grantId)
                                .header("Authorization", bearer(adminToken))
                                .param("active", "false"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/admin/grants/{id}", grantId)
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(grantId));

                mockMvc.perform(get("/api/grants/{id}", grantId)
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(grantId));
        }

        @Test
        void grant_search_matches_provider_name_with_and_without_turkish_chars() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_search_" + suffix, "admin123");
                long sourceId = createSource(adminToken, "searchsrc_" + suffix);

                MvcResult createdGrant = mockMvc.perform(post("/api/admin/grants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "sourceId": %d,
                                                  "title": "Ar-Ge Destegi %s",
                                                  "providerName": "TÜBİTAK - TEYDEB",
                                                  "programName": "1501 Sanayi Ar-Ge",
                                                  "scope": "NATIONAL",
                                                  "countryCode": "TR",
                                                  "officialUrl": "https://example.org/grants/search/%s",
                                                  "referenceCode": "SEARCH-%s"
                                                }
                                                """.formatted(sourceId, suffix, suffix, suffix)))
                                .andExpect(status().isOk())
                                .andReturn();

                long grantId = objectMapper.readTree(createdGrant.getResponse().getContentAsString()).get("id")
                                .asLong();

                mockMvc.perform(patch("/api/admin/grants/{id}/status", grantId)
                                .header("Authorization", bearer(adminToken))
                                .param("status", "PUBLISHED"))
                                .andExpect(status().isOk());

                assertQueryContainsGrant("tubitak", grantId, adminToken);
                assertQueryContainsGrant("tübitak", grantId, adminToken);
                assertQueryContainsGrant("teydeb", grantId, adminToken);
        }

        @Test
        void grant_search_filter_boundaries_and_human_queries() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_filter_" + suffix, "admin123");
                long sourceId = createSource(adminToken, "filtersrc_" + suffix);

                long lowBudgetGrantId = createPublishedGrantWithFields(
                                adminToken,
                                sourceId,
                                "Dusuk Butce Destegi " + suffix,
                                "KOSGEB",
                                "Dijital Donusum Programi",
                                "62.01",
                                100_000,
                                500_000,
                                "LOW-" + suffix);

                long highBudgetGrantId = createPublishedGrantWithFields(
                                adminToken,
                                sourceId,
                                "Yuksek Butce Ar-Ge " + suffix,
                                "TUBITAK - TEYDEB",
                                "1501 Sanayi Ar-Ge",
                                "62.01",
                                2_000_000,
                                12_000_000,
                                "HIGH-" + suffix);

                long otherNaceGrantId = createPublishedGrantWithFields(
                                adminToken,
                                sourceId,
                                "Baska NACE Programi " + suffix,
                                "Ticaret Bakanligi",
                                "Ihracat Gelistirme",
                                "72.19",
                                200_000,
                                900_000,
                                "NACE-" + suffix);

                // Human-like free text should match provider + program tokens regardless of
                // accents.
                assertQueryContainsGrant("tubitak 1501", highBudgetGrantId, adminToken);
                assertQueryContainsGrant("teydeb arge", highBudgetGrantId, adminToken);

                // Min funding boundary: should exclude low budget grant and include high budget
                // grant.
                mockMvc.perform(get("/api/grants")
                                .param("status", "PUBLISHED")
                                .param("minFunding", "1000000")
                                .param("page", "0")
                                .param("size", "50")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.id==%d)]".formatted(highBudgetGrantId)).isNotEmpty())
                                .andExpect(jsonPath("$.content[?(@.id==%d)]".formatted(lowBudgetGrantId)).isEmpty());

                // Max funding boundary: should include low budget grant and exclude high budget
                // grant.
                mockMvc.perform(get("/api/grants")
                                .param("status", "PUBLISHED")
                                .param("maxFunding", "600000")
                                .param("page", "0")
                                .param("size", "50")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.id==%d)]".formatted(lowBudgetGrantId)).isNotEmpty())
                                .andExpect(jsonPath("$.content[?(@.id==%d)]".formatted(highBudgetGrantId)).isEmpty());

                // NACE boundary: exact NACE should include same code and exclude different one.
                mockMvc.perform(get("/api/grants")
                                .param("status", "PUBLISHED")
                                .param("nace", "62.01")
                                .param("page", "0")
                                .param("size", "50")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.id==%d)]".formatted(lowBudgetGrantId)).isNotEmpty())
                                .andExpect(jsonPath("$.content[?(@.id==%d)]".formatted(highBudgetGrantId)).isNotEmpty())
                                .andExpect(jsonPath("$.content[?(@.id==%d)]".formatted(otherNaceGrantId)).isEmpty());
        }

        @Test
        void application_firm_and_admin_endpoints() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_app_" + suffix, "admin123");
                long sourceId = createSource(adminToken, "appsrc_" + suffix);
                long grantId = createAndPublishGrant(adminToken, sourceId, "appgrant_" + suffix);

                String firmUser = "firm_apply_" + suffix;
                String firmPass = "secret123";
                register(firmUser, firmUser + "@mail.com", firmPass);
                String firmToken = loginAndGetToken(firmUser, firmPass);

                MvcResult createdApp = mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(firmToken))
                                .content("{\"grantId\":%d}".formatted(grantId)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                                .andReturn();

                long applicationId = objectMapper.readTree(createdApp.getResponse().getContentAsString()).get("id")
                                .asLong();

                MvcResult createdSlot = mockMvc
                                .perform(post("/api/admin/applications/{id}/meeting-slots", applicationId)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("Authorization", bearer(adminToken))
                                                .content("""
                                                                {
                                                                  "slotAt":"2030-01-10T10:30:00"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.applicationId").value(applicationId))
                                .andExpect(jsonPath("$.available").value(true))
                                .andReturn();
                long slotId = objectMapper.readTree(createdSlot.getResponse().getContentAsString()).get("id").asLong();

                mockMvc.perform(get("/api/applications/{id}/meeting-slots", applicationId)
                                .header("Authorization", bearer(firmToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(slotId))
                                .andExpect(jsonPath("$[0].slotAt").value("2030-01-10T10:30:00"));

                mockMvc.perform(patch("/api/applications/{id}/meeting", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(firmToken))
                                .content("""
                                                {
                                                  "slotId":%d,
                                                  "note":"firma randevu talebi"
                                                }
                                                """.formatted(slotId)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.applicationId").value(applicationId))
                                .andExpect(jsonPath("$.requestedMeetingAt").value("2030-01-10T10:30:00"))
                                .andExpect(jsonPath("$.meetingNote").value("firma randevu talebi"));

                mockMvc.perform(patch("/api/applications/{id}/meeting", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(firmToken))
                                .content("""
                                                {
                                                  "slotId":%d,
                                                  "note":"tekrar deneme"
                                                }
                                                """.formatted(slotId)))
                                .andExpect(status().isConflict());

                mockMvc.perform(patch("/api/admin/applications/{id}/meeting", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "confirmedMeetingAt":"2030-01-11T11:00:00",
                                                  "note":"admin onayli randevu"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.applicationId").value(applicationId))
                                .andExpect(jsonPath("$.confirmedMeetingAt").value("2030-01-11T11:00:00"))
                                .andExpect(jsonPath("$.meetingNote").value("admin onayli randevu"));

                MvcResult notificationsResult = mockMvc.perform(get("/api/notifications/me")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(firmToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].type").value("MEETING_CONFIRMED"))
                                .andExpect(jsonPath("$.content[0].read").value(false))
                                .andReturn();

                long notificationId = objectMapper.readTree(notificationsResult.getResponse().getContentAsString())
                                .get("content").get(0).get("id").asLong();

                mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                                .header("Authorization", bearer(firmToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(notificationId))
                                .andExpect(jsonPath("$.read").value(true));

                mockMvc.perform(get("/api/applications/me")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(firmToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());

                mockMvc.perform(get("/api/admin/applications")
                                .param("status", "SUBMITTED")
                                .param("grantId", String.valueOf(grantId))
                                .param("q", firmUser)
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());

                mockMvc.perform(get("/api/admin/applications/{id}", applicationId)
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(applicationId));

                mockMvc.perform(get("/api/admin/applications/{id}/history", applicationId)
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].toStatus").value("SUBMITTED"));

                mockMvc.perform(patch("/api/admin/applications/{id}/status", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "status":"APPROVED",
                                                  "decisionNote":"approved by integration test"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));
        }

                          @Test
                          void admin_applications_list_supports_firmUsername_param() throws Exception {
                            String suffix = unique();
                            String adminToken = createAdminAndLogin("admin_app_filter_" + suffix, "admin123");
                            long sourceId = createSource(adminToken, "appfilter_" + suffix);
                            long grantId = createAndPublishGrant(adminToken, sourceId, "appfiltergrant_" + suffix);

                            String firmUser = "firm_filter_" + suffix;
                            String firmPass = "secret123";
                            register(firmUser, firmUser + "@mail.com", firmPass);
                            String firmToken = loginAndGetToken(firmUser, firmPass);

                            mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(firmToken))
                                .content("{\"grantId\":%d}".formatted(grantId)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUBMITTED"));

                            mockMvc.perform(get("/api/admin/applications")
                                .param("status", "SUBMITTED")
                                .param("firmUsername", firmUser)
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].status").value("SUBMITTED"));
                          }

        @Test
        void eligibility_upsert_and_get() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_elig_" + suffix, "admin123");
                long sourceId = createSource(adminToken, "eligsrc_" + suffix);
                long grantId = createAndPublishGrant(adminToken, sourceId, "eliggrant_" + suffix);

                mockMvc.perform(put("/api/grants/{grantId}/eligibility", grantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "applicantTypes":["SME","STARTUP"],
                                                  "minCompanyAgeMonths":6,
                                                  "minEmployees":1,
                                                  "maxEmployees":250,
                                                  "requiredCountryCodes":["TR"],
                                                  "cofundingRequired":true,
                                                  "cofundingRate":30,
                                                  "notes":"eligibility integration test"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.grantId").value(grantId));

                mockMvc.perform(get("/api/grants/{grantId}/eligibility", grantId)
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.grantId").value(grantId))
                                .andExpect(jsonPath("$.applicantTypes[0]").value("SME"));
        }

        @Test
        void eligibility_check_rejects_out_of_range_turnover_and_employee_values() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_eligrange_" + suffix, "admin123");
                long sourceId = createSource(adminToken, "eligrange_" + suffix);
                long grantId = createAndPublishGrant(adminToken, sourceId, "eligrangegrant_" + suffix);

                mockMvc.perform(put("/api/grants/{grantId}/eligibility", grantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "applicantTypes":["SME"],
                                                  "minEmployees":10,
                                                  "maxEmployees":100,
                                                  "minTurnover":1000000,
                                                  "maxTurnover":5000000,
                                                  "requiredCountryCodes":["TR"]
                                                }
                                                """))
                                .andExpect(status().isOk());

                mockMvc.perform(post("/api/eligibility/check")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "grantId": %d,
                                                  "applicantType": "SME",
                                                  "employees": 120,
                                                  "turnover": 2000000,
                                                  "countryCode": "TR"
                                                }
                                                """.formatted(grantId)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.eligible").value(false));

                mockMvc.perform(post("/api/eligibility/check")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "grantId": %d,
                                                  "applicantType": "SME",
                                                  "employees": 40,
                                                  "turnover": 9000000,
                                                  "countryCode": "TR"
                                                }
                                                """.formatted(grantId)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.eligible").value(false));
        }

        @Test
        void source_list_create_update() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_src_" + suffix, "admin123");

                MvcResult createdSource = mockMvc.perform(post("/api/admin/sources")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "name":"Source %s",
                                                  "category":"GOV_PORTAL",
                                                  "scope":"NATIONAL",
                                                  "countryCode":"TR",
                                                  "officialUrl":"https://example.org/source/%s",
                                                  "active":true
                                                }
                                                """.formatted(suffix, suffix)))
                                .andExpect(status().isOk())
                                .andReturn();

                long sourceId = objectMapper.readTree(createdSource.getResponse().getContentAsString()).get("id")
                                .asLong();

                mockMvc.perform(get("/api/sources")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());

                mockMvc.perform(put("/api/admin/sources/{id}", sourceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "name":"Source Updated %s",
                                                  "category":"AGENCY",
                                                  "scope":"MIXED",
                                                  "countryCode":"DE",
                                                  "officialUrl":"https://example.org/source/u/%s",
                                                  "active":false
                                                }
                                                """.formatted(suffix, suffix)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.scope").value("MIXED"));
        }

        @Test
        void listed_endpoint_compatibility_check() throws Exception {
                mockMvc.perform(post("/api/firm-registrations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username":"dummy_%s",
                                                  "email":"dummy_%s@mail.com",
                                                  "password":"secret123"
                                                }
                                                """.formatted(unique(), unique())))
                                .andExpect(status().isOk());

                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_mismatch_" + suffix, "admin123");
                mockMvc.perform(post("/api/eligibility/check")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void pre_analysis_flow_create_list_review_completed() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_pre_" + suffix, "admin123");

                String firmUser = "firm_pre_" + suffix;
                String firmPass = "secret123";
                register(firmUser, firmUser + "@mail.com", firmPass);
                String firmToken = loginAndGetToken(firmUser, firmPass);

                MvcResult created = mockMvc.perform(post("/api/pre-analysis")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(firmToken))
                                .content("""
                                                {
                                                  "activityArea":"otomasyon",
                                                  "machinePark":"cnc, robot kolu",
                                                  "investmentPlan":"2027 yeni hat",
                                                  "rdExperience":"3 proje",
                                                  "exportStatus":"AB pazarina ihracat",
                                                  "financialCapacity":"2M EUR butce",
                                                  "note":"on analiz talebi"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                                .andExpect(jsonPath("$.firmUsername").value(firmUser))
                                .andReturn();

                long preId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

                mockMvc.perform(get("/api/pre-analysis/me")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(firmToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());

                mockMvc.perform(get("/api/admin/pre-analysis")
                                .param("status", "SUBMITTED")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());

                mockMvc.perform(patch("/api/admin/pre-analysis/{id}", preId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "status":"COMPLETED",
                                                  "reviewNote":"uygun bulundu",
                                                  "reportSummary":"Firma icin 3 uygun hibe tespit edildi"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(preId))
                                .andExpect(jsonPath("$.status").value("COMPLETED"))
                                .andExpect(jsonPath("$.reportSummary").value("Firma icin 3 uygun hibe tespit edildi"));
        }

        @Test
        void duplicate_application_and_past_meeting_dates_are_rejected() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_guard_" + suffix, "admin123");
                long sourceId = createSource(adminToken, "guardsrc_" + suffix);
                long grantId = createAndPublishGrant(adminToken, sourceId, "guardgrant_" + suffix);

                String firmUser = "firm_guard_" + suffix;
                String firmPass = "secret123";
                register(firmUser, firmUser + "@mail.com", firmPass);
                String firmToken = loginAndGetToken(firmUser, firmPass);

                MvcResult firstApp = mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(firmToken))
                                .content("{\"grantId\":%d}".formatted(grantId)))
                                .andExpect(status().isOk())
                                .andReturn();
                long applicationId = objectMapper.readTree(firstApp.getResponse().getContentAsString()).get("id")
                                .asLong();

                mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(firmToken))
                                .content("{\"grantId\":%d}".formatted(grantId)))
                                .andExpect(status().isConflict());

                mockMvc.perform(post("/api/admin/applications/{id}/meeting-slots", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "slotAt":"2020-01-01T10:00:00"
                                                }
                                                """))
                                .andExpect(status().isBadRequest());

                mockMvc.perform(patch("/api/admin/applications/{id}/meeting", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "confirmedMeetingAt":"2020-01-01T11:00:00",
                                                  "note":"past confirm"
                                                }
                                                """))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void ingest_run_status_jobs_and_metrics() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_ingest_" + suffix, "admin123");

                mockMvc.perform(post("/api/admin/ingest/run")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.connectorCount").value(4))
                                .andExpect(jsonPath("$.fetchedCount").value(4));

                mockMvc.perform(get("/api/admin/ingest/status")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.lastStatus").value("SUCCESS"))
                                .andExpect(jsonPath("$.lastRunAt").exists());

                mockMvc.perform(get("/api/admin/ingest/jobs")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].status").value("SUCCESS"));

                mockMvc.perform(get("/api/admin/ingest/metrics")
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalFetchedCount").isNumber())
                                .andExpect(jsonPath("$.totalFetchedCount")
                                                .value(org.hamcrest.Matchers.greaterThanOrEqualTo(4)))
                                .andExpect(jsonPath("$.totalCreatedCount").isNumber())
                                .andExpect(jsonPath("$.totalUpdatedCount").isNumber())
                                .andExpect(jsonPath("$.errorRate").value(0.0));
        }

        @Test
        void new_matching_grant_triggers_notification_for_firm() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_notify_" + suffix, "admin123");
                long sourceId = createSource(adminToken, "notifsrc_" + suffix);

                String firmUser = "firm_notify_" + suffix;
                String firmPass = "secret123";
                register(firmUser, firmUser + "@mail.com", firmPass);
                String firmToken = loginAndGetToken(firmUser, firmPass);

                AppUser firm = userRepository.findByUsername(firmUser).orElseThrow();
                firm.setRole(UserRole.FIRMA);
                firm.setProfileCompleted(true);
                firm.setApplicantType("SME");
                firm.setCompanyAgeMonths(24);
                firm.setEmployees(30);
                firm.setCountryCode("TR");
                firm.setCofundingAvailable(true);
                firm.setCofundingRate(40);
                firm.setSector("savunma");
                firm.setActivityArea("yazilim");
                firm.setNaceCodes("62.01");
                firm.setTurnover(new java.math.BigDecimal("5000000"));
                userRepository.save(firm);

                MvcResult createdGrant = mockMvc.perform(post("/api/admin/grants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "sourceId": %d,
                                                  "title": "Savunma Yazilim Destegi %s",
                                                  "scope": "NATIONAL",
                                                  "naceCode": "62.01",
                                                  "countryCode": "TR",
                                                  "officialUrl": "https://example.org/grants/notify/%s",
                                                  "referenceCode": "NOTIFY-%s"
                                                }
                                                """.formatted(sourceId, suffix, suffix, suffix)))
                                .andExpect(status().isOk())
                                .andReturn();

                long grantId = objectMapper.readTree(createdGrant.getResponse().getContentAsString()).get("id")
                                .asLong();

                mockMvc.perform(put("/api/grants/{grantId}/eligibility", grantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "applicantTypes":["SME"],
                                                  "minCompanyAgeMonths":6,
                                                  "minEmployees":1,
                                                  "maxEmployees":250,
                                                  "requiredCountryCodes":["TR"],
                                                  "cofundingRequired":true,
                                                  "cofundingRate":20
                                                }
                                                """))
                                .andExpect(status().isOk());

                mockMvc.perform(patch("/api/admin/grants/{id}/status", grantId)
                                .header("Authorization", bearer(adminToken))
                                .param("status", "PUBLISHED"))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/notifications/me")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", bearer(firmToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].type").value("GRANT_MATCHED"))
                                .andExpect(jsonPath("$.content[0].message")
                                                .value(org.hamcrest.Matchers.containsString("#" + grantId)));
        }

        @Test
        void grant_detail_returns_admin_quick_info() throws Exception {
                String suffix = unique();
                String adminToken = createAdminAndLogin("admin_quick_" + suffix, "admin123");
                long sourceId = createSource(adminToken, "quicksrc_" + suffix);

                MvcResult createdGrant = mockMvc.perform(post("/api/admin/grants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "sourceId": %d,
                                                  "title": "Quick Info Grant %s",
                                                  "scope": "NATIONAL",
                                                  "countryCode": "TR",
                                                  "officialUrl": "https://example.org/grants/quick/%s",
                                                  "referenceCode": "QUICK-%s",
                                                  "summaryShort": "Kisa ozet",
                                                  "adminQuickInfo": "Bu hibe ihracat potansiyeli olan KOBI'ler icin uygun"
                                                }
                                                """
                                                .formatted(sourceId, suffix, suffix, suffix)))
                                .andExpect(status().isOk())
                                .andReturn();

                long grantId = objectMapper.readTree(createdGrant.getResponse().getContentAsString()).get("id")
                                .asLong();

                mockMvc.perform(get("/api/admin/grants/{id}", grantId)
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.adminQuickInfo")
                                                .value("Bu hibe ihracat potansiyeli olan KOBI'ler icin uygun"))
                                .andExpect(jsonPath("$.summaryShort").value("Kisa ozet"));

                mockMvc.perform(get("/api/grants/{id}", grantId)
                                .header("Authorization", bearer(adminToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.adminQuickInfo")
                                                .value("Bu hibe ihracat potansiyeli olan KOBI'ler icin uygun"));
        }

        private String unique() {
                return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        private String bearer(String token) {
                return "Bearer " + token;
        }

        private void register(String username, String email, String password) throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username":"%s",
                                                  "email":"%s",
                                                  "password":"%s"
                                                }
                                                """.formatted(username, email, password)))
                                .andExpect(status().isOk());
        }

        private String createAdminAndLogin(String username, String rawPassword) throws Exception {
                AppUser admin = new AppUser();
                admin.setUsername(username);
                admin.setEmail(username + "@mail.com");
                admin.setPasswordHash(passwordEncoder.encode(rawPassword));
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
                return loginAndGetToken(username, rawPassword);
        }

        private String loginAndGetToken(String username, String password) throws Exception {
                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username":"%s",
                                                  "password":"%s"
                                                }
                                                """.formatted(username, password)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andReturn();

                JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
                return body.get("accessToken").asText();
        }

        private long createSource(String token, String suffix) throws Exception {
                MvcResult created = mockMvc.perform(post("/api/sources")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(token))
                                .content("""
                                                {
                                                  "name":"Seed Source %s",
                                                  "category":"GOV_PORTAL",
                                                  "scope":"NATIONAL",
                                                  "countryCode":"TR",
                                                  "officialUrl":"https://example.org/seed/source/%s",
                                                  "active":true
                                                }
                                                """.formatted(suffix, suffix)))
                                .andExpect(status().isOk())
                                .andReturn();
                return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        }

        private long createAndPublishGrant(String adminToken, long sourceId, String suffix) throws Exception {
                MvcResult createdGrant = mockMvc.perform(post("/api/admin/grants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "sourceId": %d,
                                                  "title": "Grant %s",
                                                  "scope": "NATIONAL",
                                                  "countryCode": "TR",
                                                  "officialUrl": "https://example.org/grants/%s",
                                                  "referenceCode": "REF-%s"
                                                }
                                                """.formatted(sourceId, suffix, suffix, suffix)))
                                .andExpect(status().isOk())
                                .andReturn();

                long grantId = objectMapper.readTree(createdGrant.getResponse().getContentAsString()).get("id")
                                .asLong();

                mockMvc.perform(patch("/api/admin/grants/{id}/status", grantId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "status":"PUBLISHED"
                                                }
                                                """))
                                .andExpect(status().isOk());
                return grantId;
        }

        private void assertQueryContainsGrant(String query, long grantId, String token) throws Exception {
                MvcResult result = mockMvc.perform(get("/api/grants")
                                .param("q", query)
                                .param("status", "PUBLISHED")
                                .param("page", "0")
                                .param("size", "20")
                                .header("Authorization", bearer(token)))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
                boolean contains = false;
                for (JsonNode item : content) {
                        if (item.get("id").asLong() == grantId) {
                                contains = true;
                                break;
                        }
                }
                org.junit.jupiter.api.Assertions.assertTrue(contains, "Query should include grant for q=" + query);
        }

        private long createPublishedGrantWithFields(
                        String adminToken,
                        long sourceId,
                        String title,
                        String providerName,
                        String programName,
                        String naceCode,
                        int fundingMin,
                        int fundingMax,
                        String referenceCode) throws Exception {
                MvcResult createdGrant = mockMvc.perform(post("/api/admin/grants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", bearer(adminToken))
                                .content("""
                                                {
                                                  "sourceId": %d,
                                                  "title": "%s",
                                                  "providerName": "%s",
                                                  "programName": "%s",
                                                  "scope": "NATIONAL",
                                                  "naceCode": "%s",
                                                  "countryCode": "TR",
                                                  "fundingMin": %d,
                                                  "fundingMax": %d,
                                                  "officialUrl": "https://example.org/grants/filter/%s",
                                                  "referenceCode": "%s"
                                                }
                                                """.formatted(
                                                sourceId,
                                                title,
                                                providerName,
                                                programName,
                                                naceCode,
                                                fundingMin,
                                                fundingMax,
                                                referenceCode,
                                                referenceCode)))
                                .andExpect(status().isOk())
                                .andReturn();

                long grantId = objectMapper.readTree(createdGrant.getResponse().getContentAsString()).get("id")
                                .asLong();

                mockMvc.perform(patch("/api/admin/grants/{id}/status", grantId)
                                .header("Authorization", bearer(adminToken))
                                .param("status", "PUBLISHED"))
                                .andExpect(status().isOk());

                return grantId;
        }
}
