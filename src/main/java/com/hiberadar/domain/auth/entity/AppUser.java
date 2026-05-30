package com.hiberadar.domain.auth.entity;

import com.hiberadar.domain.user.entity.enums.UserRole;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_app_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_app_users_email", columnNames = "email")
        }
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "first_name", length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    @Column(name = "phone", length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(name = "profile_completed")
    private Boolean profileCompleted = false;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "applicant_type", length = 40)
    private String applicantType;

        @Column(name = "company_name", length = 180)
        private String companyName;

    @Column(name = "company_age_months")
    private Integer companyAgeMonths;

    @Column(name = "employees")
    private Integer employees;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "cofunding_available")
    private Boolean cofundingAvailable;

    @Column(name = "cofunding_rate")
    private Integer cofundingRate;

    @Column(name = "sector", length = 120)
    private String sector;

    @Column(name = "activity_area", length = 255)
    private String activityArea;

    @Column(name = "turnover", precision = 18, scale = 2)
    private BigDecimal turnover;

    @Column(name = "nace_codes", length = 500)
    private String naceCodes;

        @Column(name = "company_logo_url", length = 500)
        private String companyLogoUrl;

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isProfileCompleted() { return Boolean.TRUE.equals(profileCompleted); }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }

    public boolean isActive() { return !Boolean.FALSE.equals(active); }
    public void setActive(boolean active) { this.active = active; }

    public String getApplicantType() { return applicantType; }
    public void setApplicantType(String applicantType) { this.applicantType = applicantType; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Integer getCompanyAgeMonths() { return companyAgeMonths; }
    public void setCompanyAgeMonths(Integer companyAgeMonths) { this.companyAgeMonths = companyAgeMonths; }

    public Integer getEmployees() { return employees; }
    public void setEmployees(Integer employees) { this.employees = employees; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public Boolean getCofundingAvailable() { return cofundingAvailable; }
    public void setCofundingAvailable(Boolean cofundingAvailable) { this.cofundingAvailable = cofundingAvailable; }

    public Integer getCofundingRate() { return cofundingRate; }
    public void setCofundingRate(Integer cofundingRate) { this.cofundingRate = cofundingRate; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getActivityArea() { return activityArea; }
    public void setActivityArea(String activityArea) { this.activityArea = activityArea; }

    public BigDecimal getTurnover() { return turnover; }
    public void setTurnover(BigDecimal turnover) { this.turnover = turnover; }

    public String getNaceCodes() { return naceCodes; }
    public void setNaceCodes(String naceCodes) { this.naceCodes = naceCodes; }

        public String getCompanyLogoUrl() { return companyLogoUrl; }
        public void setCompanyLogoUrl(String companyLogoUrl) { this.companyLogoUrl = companyLogoUrl; }
}
