package com.hiberadar.hiberadar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.auth.entity.enums.FirmRegistrationStatus;
import com.hiberadar.domain.eligibility.entity.EligibilityRule;
import com.hiberadar.domain.eligibility.repository.EligibilityRuleRepository;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.repository.GrantRepository;
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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class WorkflowIntegrationTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private GrantRepository grantRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private EligibilityRuleRepository eligibilityRuleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Test
        void registerLoginAndListMyApplicationsFlow() throws Exception {
                String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                String username = "wf_user_" + suffix;
                String password = "secret123";
                String email = username + "@mail.com";

                registerUser(username, email, password);
                String token = loginAndGetToken(username, password);

                mockMvc.perform(get("/api/applications/me")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        void createApplicationAndAdminApproveFlow() throws Exception {
                String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                String username = "wf_apply_" + suffix;
                String password = "secret123";
                String email = username + "@mail.com";

                registerUser(username, email, password);
                String userToken = loginAndGetToken(username, password);

                Grant grant = new Grant();
                grant.setTitle("Workflow Grant " + suffix);
                grant.setStatus(GrantStatus.PUBLISHED);
                grant = grantRepository.save(grant);

                MvcResult created = mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + userToken)
                                .content("{\"grantId\":" + grant.getId() + "}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.grantId").value(grant.getId()))
                                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                                .andReturn();

                JsonNode createdBody = objectMapper.readTree(created.getResponse().getContentAsString());
                long applicationId = createdBody.get("id").asLong();

                String adminUsername = "wf_admin_" + suffix;
                String adminPassword = "admin123";
                String adminEmail = adminUsername + "@mail.com";
                createAdminUser(adminUsername, adminEmail, adminPassword);
                String adminToken = loginAndGetToken(adminUsername, adminPassword);

                mockMvc.perform(patch("/api/admin/applications/{id}/status", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + adminToken)
                                .content("{\"status\":\"APPROVED\",\"decisionNote\":\"workflow test\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));

                mockMvc.perform(get("/api/admin/applications/{id}/history", applicationId)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].toStatus").value("SUBMITTED"))
                                .andExpect(jsonPath("$[1].toStatus").value("APPROVED"));
        }

        @Test
        void meetingConfirmationCreatesNotificationFlow() throws Exception {
                String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                int n = Integer.parseInt(suffix.substring(0, 4), 16);
                int year = 2070 + (n % 20);
                int month = ((n / 20) % 12) + 1;
                int day = ((n / 240) % 24) + 1;
                String slotAt = "%04d-%02d-%02dT10:00:00".formatted(year, month, day);
                String confirmedAt = "%04d-%02d-%02dT11:00:00".formatted(year, month, day + 1);
                String username = "wf_meet_" + suffix;
                String password = "secret123";
                String email = username + "@mail.com";
                registerUser(username, email, password);
                String userToken = loginAndGetToken(username, password);

                Grant grant = new Grant();
                grant.setTitle("Meeting Grant " + suffix);
                grant.setStatus(GrantStatus.PUBLISHED);
                grant = grantRepository.save(grant);

                MvcResult created = mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + userToken)
                                .content("{\"grantId\":" + grant.getId() + "}"))
                                .andExpect(status().isOk())
                                .andReturn();
                long applicationId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id")
                                .asLong();

                String adminUsername = "wf_meet_admin_" + suffix;
                String adminPassword = "admin123";
                createAdminUser(adminUsername, adminUsername + "@mail.com", adminPassword);
                String adminToken = loginAndGetToken(adminUsername, adminPassword);

                mockMvc.perform(patch("/api/applications/{id}/meeting", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + userToken)
                                .content("""
                                                {
                                                                                                                                                                                                        "requestedMeetingAt":"%s",
                                                  "note":"workflow meeting request"
                                                }
                                                                                                                                                                                                """
                                                .formatted(slotAt)))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/notifications/me")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[*].type").value(hasItem("MEETING_REQUESTED")));

                mockMvc.perform(patch("/api/admin/applications/{id}/meeting", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + adminToken)
                                .content("""
                                                {
                                                        "confirmedMeetingAt":"%s",
                                                        "note":"workflow meeting confirmed"
                                                }
                                                """.formatted(confirmedAt)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.confirmedMeetingAt").value(confirmedAt));

                mockMvc.perform(get("/api/notifications/me")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].type").value("MEETING_CONFIRMED"))
                                .andExpect(jsonPath("$.content[0].read").value(false));
        }

        @Test
        void meetingCalendarAndSameDayCapacityRulesFlow() throws Exception {
                String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                int n = Integer.parseInt(suffix.substring(4, 8), 16);
                int year = 2080 + (n % 15);
                int month = ((n / 15) % 12) + 1;
                int baseDay = ((n / 180) % 20) + 5;
                String appASlotAt = "%04d-%02d-%02dT10:00:00".formatted(year, month, baseDay);
                String appAConfirmAt = appASlotAt;
                String appBSlotAt = "%04d-%02d-%02dT11:00:00".formatted(year, month, baseDay - 1);
                String appBConflictConfirmAt = "%04d-%02d-%02dT10:00:00".formatted(year, month, baseDay);

                String firmA = "wf_meet_a_" + suffix;
                String firmB = "wf_meet_b_" + suffix;
                registerUser(firmA, firmA + "@mail.com", "secret123");
                registerUser(firmB, firmB + "@mail.com", "secret123");
                String firmAToken = loginAndGetToken(firmA, "secret123");
                String firmBToken = loginAndGetToken(firmB, "secret123");

                String adminUsername = "wf_meet_cap_admin_" + suffix;
                createAdminUser(adminUsername, adminUsername + "@mail.com", "admin123");
                String adminToken = loginAndGetToken(adminUsername, "admin123");

                Grant grant1 = new Grant();
                grant1.setTitle("Capacity Grant A " + suffix);
                grant1.setStatus(GrantStatus.PUBLISHED);
                grant1 = grantRepository.save(grant1);

                Grant grant2 = new Grant();
                grant2.setTitle("Capacity Grant B " + suffix);
                grant2.setStatus(GrantStatus.PUBLISHED);
                grant2 = grantRepository.save(grant2);

                long appA = objectMapper.readTree(mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + firmAToken)
                                .content("{\"grantId\":" + grant1.getId() + "}"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString()).get("id").asLong();

                long appB = objectMapper.readTree(mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + firmBToken)
                                .content("{\"grantId\":" + grant2.getId() + "}"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString()).get("id").asLong();

                mockMvc.perform(patch("/api/applications/{id}/meeting", appA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + firmAToken)
                                .content("""
                                                {
                                                  "requestedMeetingAt":"%s",
                                                  "note":"ilk talep"
                                                }
                                                """.formatted(appASlotAt)))
                                .andExpect(status().isOk());

                mockMvc.perform(patch("/api/admin/applications/{id}/meeting", appA)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + adminToken)
                                .content("""
                                                {
                                                        "confirmedMeetingAt":"%s",
                                                        "note":"onaylandi"
                                                }
                                                """.formatted(appAConfirmAt)))
                                .andExpect(status().isOk());

                mockMvc.perform(patch("/api/applications/{id}/meeting", appB)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + firmBToken)
                                .content("""
                                                {
                                                                                                                                                                                                        "requestedMeetingAt":"%s",
                                                  "note":"ikinci talep"
                                                }
                                                                                                                                                                                                """
                                                .formatted(appBSlotAt)))
                                .andExpect(status().isOk());

                mockMvc.perform(patch("/api/admin/applications/{id}/meeting", appB)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + adminToken)
                                .content("""
                                                {
                                                        "confirmedMeetingAt":"%s",
                                                        "note":"ayni gune ikinci randevu"
                                                }
                                                """.formatted(appBConflictConfirmAt)))
                                .andExpect(status().isConflict());

                mockMvc.perform(get("/api/admin/applications/meetings")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].applicationId").exists());

                mockMvc.perform(get("/api/admin/applications/meetings/unavailable-days")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0]").exists());

                mockMvc.perform(get("/api/applications/meetings/me")
                                .header("Authorization", "Bearer " + firmAToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].applicationId").exists());
        }

                                @Test
                                void maxThreePendingMeetingRequestsPerFirmFlow() throws Exception {
                                                                String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                                                                String username = "wf_limit_" + suffix;
                                                                String password = "secret123";
                                                                registerUser(username, username + "@mail.com", password);
                                                                String token = loginAndGetToken(username, password);

                                                                Grant g1 = new Grant();
                                                                g1.setTitle("Limit Grant 1 " + suffix);
                                                                g1.setStatus(GrantStatus.PUBLISHED);
                                                                g1 = grantRepository.save(g1);

                                                                Grant g2 = new Grant();
                                                                g2.setTitle("Limit Grant 2 " + suffix);
                                                                g2.setStatus(GrantStatus.PUBLISHED);
                                                                g2 = grantRepository.save(g2);

                                                                Grant g3 = new Grant();
                                                                g3.setTitle("Limit Grant 3 " + suffix);
                                                                g3.setStatus(GrantStatus.PUBLISHED);
                                                                g3 = grantRepository.save(g3);

                                                                Grant g4 = new Grant();
                                                                g4.setTitle("Limit Grant 4 " + suffix);
                                                                g4.setStatus(GrantStatus.PUBLISHED);
                                                                g4 = grantRepository.save(g4);

                                                                long app1 = createApplicationAndReturnId(token, g1.getId());
                                                                long app2 = createApplicationAndReturnId(token, g2.getId());
                                                                long app3 = createApplicationAndReturnId(token, g3.getId());
                                                                long app4 = createApplicationAndReturnId(token, g4.getId());

                                                                String t1 = "2091-01-10T10:00:00";
                                                                String t2 = "2091-01-11T11:00:00";
                                                                String t3 = "2091-01-12T12:00:00";
                                                                String t4 = "2091-01-13T13:00:00";

                                                                requestMeetingAndExpectOk(token, app1, t1);
                                                                requestMeetingAndExpectOk(token, app2, t2);
                                                                requestMeetingAndExpectOk(token, app3, t3);

                                                                mockMvc.perform(patch("/api/applications/{id}/meeting", app4)
                                                                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                                                                .header("Authorization", "Bearer " + token)
                                                                                                                                .content("""
                                                                                                                                                                                                {
                                                                                                                                                                                                        "requestedMeetingAt":"%s",
                                                                                                                                                                                                        "note":"dorduncu talep"
                                                                                                                                                                                                }
                                                                                                                                                                                                """.formatted(t4)))
                                                                                                                                .andExpect(status().isTooManyRequests())
                                                                                                                                .andExpect(jsonPath("$.message").value(containsString("En fazla 3")));
                                }

                                @Test
                                void confirmingOneRequestAutoRejectsOtherConflictingPendingFlow() throws Exception {
                                                                String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                                                                String firmA = "wf_conflict_a_" + suffix;
                                                                String firmB = "wf_conflict_b_" + suffix;
                                                                registerUser(firmA, firmA + "@mail.com", "secret123");
                                                                registerUser(firmB, firmB + "@mail.com", "secret123");
                                                                String tokenA = loginAndGetToken(firmA, "secret123");
                                                                String tokenB = loginAndGetToken(firmB, "secret123");

                                                                String adminUsername = "wf_conflict_admin_" + suffix;
                                                                createAdminUser(adminUsername, adminUsername + "@mail.com", "admin123");
                                                                String adminToken = loginAndGetToken(adminUsername, "admin123");

                                                                Grant g1 = new Grant();
                                                                g1.setTitle("Conflict Grant A " + suffix);
                                                                g1.setStatus(GrantStatus.PUBLISHED);
                                                                g1 = grantRepository.save(g1);

                                                                Grant g2 = new Grant();
                                                                g2.setTitle("Conflict Grant B " + suffix);
                                                                g2.setStatus(GrantStatus.PUBLISHED);
                                                                g2 = grantRepository.save(g2);

                                                                long appA = createApplicationAndReturnId(tokenA, g1.getId());
                                                                long appB = createApplicationAndReturnId(tokenB, g2.getId());

                                                                String sameHour = "2092-02-14T11:00:00";
                                                                requestMeetingAndExpectOk(tokenA, appA, sameHour);
                                                                requestMeetingAndExpectOk(tokenB, appB, sameHour);

                                                                mockMvc.perform(patch("/api/admin/applications/{id}/meeting", appA)
                                                                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                                                                .header("Authorization", "Bearer " + adminToken)
                                                                                                                                .content("""
                                                                                                                                                                                                {
                                                                                                                                                                                                        "confirmedMeetingAt":"%s",
                                                                                                                                                                                                        "note":"ilk talep onaylandi"
                                                                                                                                                                                                }
                                                                                                                                                                                                """.formatted(sameHour)))
                                                                                                                                .andExpect(status().isOk());

                                                                mockMvc.perform(get("/api/admin/applications/{id}", appB)
                                                                                                                                .header("Authorization", "Bearer " + adminToken))
                                                                                                                                .andExpect(status().isOk())
                                                                                                                                .andExpect(jsonPath("$.status").value("REJECTED"))
                                                                                                                                .andExpect(jsonPath("$.decisionNote").value(containsString("otomatik reddedildi")));
                                }

        @Test
        void firmRegistrationApprovalProfileAndMatchedGrantsFlow() throws Exception {
                String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                String firmUsername = "wf_firm_req_" + suffix;
                String firmPassword = "secret123";
                String firmEmail = firmUsername + "@mail.com";

                String adminUsername = "wf_admin_review_" + suffix;
                String adminPassword = "admin123";
                createAdminUser(adminUsername, adminUsername + "@mail.com", adminPassword);
                String adminToken = loginAndGetToken(adminUsername, adminPassword);

                MvcResult createReq = mockMvc.perform(post("/api/firm-registrations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username":"%s",
                                                  "email":"%s",
                                                  "password":"%s"
                                                }
                                                """.formatted(firmUsername, firmEmail, firmPassword)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andReturn();

                long requestId = objectMapper.readTree(createReq.getResponse().getContentAsString()).get("requestId")
                                .asLong();

                mockMvc.perform(patch("/api/admin/firm-registrations/{id}/review", requestId)
                                .param("decidedBy", adminUsername)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + adminToken)
                                .content("""
                                                {
                                                  "decision":"%s",
                                                  "note":"approved in workflow test"
                                                }
                                                """.formatted(FirmRegistrationStatus.APPROVED.name())))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));

                MvcResult login = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username": "%s",
                                                  "password": "%s"
                                                }
                                                """.formatted(firmUsername, firmPassword)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.requiresProfileCompletion").value(true))
                                .andExpect(jsonPath("$.nextStep").value("/profile"))
                                .andReturn();

                String firmToken = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken")
                                .asText();

                mockMvc.perform(put("/api/profile/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + firmToken)
                                .content("""
                                                {
                                                  "applicantType":"SME",
                                                  "companyAgeMonths":24,
                                                  "employees":40,
                                                  "countryCode":"TR",
                                                  "cofundingAvailable":true,
                                                  "cofundingRate":35,
                                                  "sector":"Manufacturing",
                                                  "activityArea":"Automation",
                                                  "turnover":2500000,
                                                  "naceCodes":"26.11,26.12"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.profileCompleted").value(true))
                                .andExpect(jsonPath("$.applicantType").value("SME"));

                Grant matchingGrant = new Grant();
                matchingGrant.setTitle("Manufacturing Automation Matching Grant " + suffix);
                matchingGrant.setProgramName("Automation");
                matchingGrant.setProviderName("Manufacturing Authority");
                matchingGrant.setNaceCode("26.11");
                matchingGrant.setStatus(GrantStatus.PUBLISHED);
                matchingGrant = grantRepository.save(matchingGrant);

                EligibilityRule matchingRule = new EligibilityRule();
                matchingRule.setGrant(matchingGrant);
                matchingRule.setApplicantTypes("SME,STARTUP");
                matchingRule.setMinCompanyAgeMonths(12);
                matchingRule.setMinEmployees(5);
                matchingRule.setMaxEmployees(250);
                matchingRule.setRequiredCountryCodes("TR");
                matchingRule.setCofundingRequired(true);
                matchingRule.setCofundingRate(30);
                eligibilityRuleRepository.save(matchingRule);

                Grant nonMatchingGrant = new Grant();
                nonMatchingGrant.setTitle("Non Matching Grant " + suffix);
                nonMatchingGrant.setNaceCode("72.19");
                nonMatchingGrant.setStatus(GrantStatus.PUBLISHED);
                nonMatchingGrant = grantRepository.save(nonMatchingGrant);

                EligibilityRule nonMatchingRule = new EligibilityRule();
                nonMatchingRule.setGrant(nonMatchingGrant);
                nonMatchingRule.setApplicantTypes("UNIVERSITY");
                nonMatchingRule.setRequiredCountryCodes("DE");
                eligibilityRuleRepository.save(nonMatchingRule);

                MvcResult matchesResult = mockMvc.perform(get("/api/grants/matches/me")
                                .param("page", "0")
                                .param("size", "2000")
                                .header("Authorization", "Bearer " + firmToken))
                                .andExpect(status().isOk())
                                .andReturn();

                JsonNode matchesBody = objectMapper.readTree(matchesResult.getResponse().getContentAsString());
                boolean containsMatchingGrant = false;
                boolean containsScore = false;
                for (JsonNode item : matchesBody.get("content")) {
                        if (item.get("id").asLong() == matchingGrant.getId()) {
                                containsMatchingGrant = true;
                                containsScore = item.has("matchScore") && item.get("matchScore").asInt() > 0;
                                break;
                        }
                }
                org.junit.jupiter.api.Assertions.assertTrue(containsMatchingGrant);
                org.junit.jupiter.api.Assertions.assertTrue(containsScore);
        }

        private void registerUser(String username, String email, String password) throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username": "%s",
                                                  "email": "%s",
                                                  "password": "%s"
                                                }
                                                """.formatted(username, email, password)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value(username));
        }

        private void createAdminUser(String username, String email, String rawPassword) {
                AppUser admin = new AppUser();
                admin.setUsername(username);
                admin.setEmail(email);
                admin.setPasswordHash(passwordEncoder.encode(rawPassword));
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
        }

        private long createApplicationAndReturnId(String userToken, Long grantId) throws Exception {
                MvcResult created = mockMvc.perform(post("/api/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + userToken)
                                .content("{\"grantId\":" + grantId + "}"))
                                .andExpect(status().isOk())
                                .andReturn();
                return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        }

        private void requestMeetingAndExpectOk(String userToken, long applicationId, String requestedMeetingAt)
                        throws Exception {
                mockMvc.perform(patch("/api/applications/{id}/meeting", applicationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + userToken)
                                .content("""
                                                {
                                                  "requestedMeetingAt":"%s",
                                                  "note":"test talebi"
                                                }
                                                """.formatted(requestedMeetingAt)))
                                .andExpect(status().isOk());
        }

        private String loginAndGetToken(String username, String password) throws Exception {
                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "username": "%s",
                                                  "password": "%s"
                                                }
                                                """.formatted(username, password)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andReturn();

                JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
                return body.get("accessToken").asText();
        }
}
