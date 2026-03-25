package stud.ntnu.no.fullstack_project.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import stud.ntnu.no.fullstack_project.entity.AlcoholGroup;
import stud.ntnu.no.fullstack_project.entity.AppUser;
import stud.ntnu.no.fullstack_project.entity.Bevilling;
import stud.ntnu.no.fullstack_project.entity.BevillingCondition;
import stud.ntnu.no.fullstack_project.entity.BevillingServingHours;
import stud.ntnu.no.fullstack_project.entity.BevillingStatus;
import stud.ntnu.no.fullstack_project.entity.BevillingType;
import stud.ntnu.no.fullstack_project.entity.ConditionType;
import stud.ntnu.no.fullstack_project.entity.Organization;
import stud.ntnu.no.fullstack_project.entity.OrganizationType;
import stud.ntnu.no.fullstack_project.entity.Role;
import stud.ntnu.no.fullstack_project.entity.Weekday;
import stud.ntnu.no.fullstack_project.repository.AppUserRepository;
import stud.ntnu.no.fullstack_project.repository.BevillingConditionRepository;
import stud.ntnu.no.fullstack_project.repository.BevillingRepository;
import stud.ntnu.no.fullstack_project.repository.BevillingServingHoursRepository;
import stud.ntnu.no.fullstack_project.repository.OrganizationRepository;

/**
 * Seeds the database with a default organization, sample users, and a demo bevilling on first run.
 *
 * <p>Only executes when the user table is empty, ensuring idempotent behaviour.
 * Creates one organization, three users (admin, manager, staff), and a bevilling
 * with conditions and serving hours for development and testing purposes.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

  private final AppUserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;
  private final BevillingRepository bevillingRepository;
  private final BevillingConditionRepository bevillingConditionRepository;
  private final BevillingServingHoursRepository bevillingServingHoursRepository;

  /**
   * Runs the seed logic on application startup if no users exist.
   *
   * @param args command-line arguments (ignored)
   */
  @Override
  public void run(String... args) {
    if (userRepository.count() > 0) {
      return;
    }

    log.info("Initializing seed data...");

    Organization org = new Organization();
    org.setName("Everest Sushi & Fusion");
    org.setOrganizationNumber("937219997");
    org.setAddress("Trondheim, Norway");
    org.setPhone("+47 123 45 678");
    org.setType(OrganizationType.RESTAURANT);
    org = organizationRepository.save(org);

    AppUser admin = new AppUser();
    admin.setUsername("admin");
    admin.setPassword(passwordEncoder.encode("admin123"));
    admin.setFirstName("System");
    admin.setLastName("Administrator");
    admin.setEmail("admin@everest.no");
    admin.setEmailVerified(true);
    admin.setOrganization(org);
    admin.setRoles(Set.of(Role.ROLE_ADMIN));
    userRepository.save(admin);

    AppUser manager = new AppUser();
    manager.setUsername("manager");
    manager.setPassword(passwordEncoder.encode("manager123"));
    manager.setFirstName("Restaurant");
    manager.setLastName("Manager");
    manager.setEmail("manager@everest.no");
    manager.setEmailVerified(true);
    manager.setOrganization(org);
    manager.setRoles(Set.of(Role.ROLE_MANAGER));
    userRepository.save(manager);

    AppUser staff = new AppUser();
    staff.setUsername("staff");
    staff.setPassword(passwordEncoder.encode("staff123"));
    staff.setFirstName("Kitchen");
    staff.setLastName("Staff");
    staff.setEmail("staff@everest.no");
    staff.setEmailVerified(true);
    staff.setOrganization(org);
    staff.setRoles(Set.of(Role.ROLE_STAFF));
    userRepository.save(staff);

    // --- Seed demo bevilling ---
    Bevilling bevilling = new Bevilling();
    bevilling.setOrganization(org);
    bevilling.setMunicipality("Trondheim");
    bevilling.setBevillingType(BevillingType.SKJENKING);
    bevilling.setValidFrom(LocalDate.of(2025, 1, 1));
    bevilling.setValidTo(LocalDate.of(2027, 12, 31));
    bevilling.setLicenseNumber("SK-2025-001");
    bevilling.setStatus(BevillingStatus.ACTIVE);
    bevilling.setAlcoholGroupsAllowed(
        Set.of(AlcoholGroup.GROUP_1, AlcoholGroup.GROUP_2, AlcoholGroup.GROUP_3));
    bevilling.setServingAreaDescription("Main dining room, bar area, and outdoor terrace");
    bevilling.setIndoorAllowed(true);
    bevilling.setOutdoorAllowed(true);
    bevilling.setStyrerName("Restaurant Manager");
    bevilling.setStedfortrederName("System Administrator");
    bevilling.setNotes("Renewed annually by Trondheim kommune");
    bevilling = bevillingRepository.save(bevilling);

    // Conditions
    BevillingCondition cond1 = new BevillingCondition();
    cond1.setBevilling(bevilling);
    cond1.setConditionType(ConditionType.FOOD_REQUIREMENT);
    cond1.setTitle("Food must be available");
    cond1.setDescription("Hot food must be available during all serving hours.");
    cond1.setActive(true);
    bevillingConditionRepository.save(cond1);

    BevillingCondition cond2 = new BevillingCondition();
    cond2.setBevilling(bevilling);
    cond2.setConditionType(ConditionType.TRAINING_REQUIREMENT);
    cond2.setTitle("Staff training required");
    cond2.setDescription("All serving staff must complete responsible alcohol service training.");
    cond2.setActive(true);
    bevillingConditionRepository.save(cond2);

    // Serving hours Mon-Sun
    Weekday[] weekdays = Weekday.values();
    for (Weekday day : weekdays) {
      BevillingServingHours hours = new BevillingServingHours();
      hours.setBevilling(bevilling);
      hours.setWeekday(day);
      if (day == Weekday.FRI || day == Weekday.SAT) {
        hours.setStartTime(LocalTime.of(11, 0));
        hours.setEndTime(LocalTime.of(2, 0));
      } else if (day == Weekday.SUN) {
        hours.setStartTime(LocalTime.of(12, 0));
        hours.setEndTime(LocalTime.of(22, 0));
      } else {
        hours.setStartTime(LocalTime.of(11, 0));
        hours.setEndTime(LocalTime.of(0, 0));
      }
      hours.setConsumptionDeadlineMinutesAfterEnd(30);
      bevillingServingHoursRepository.save(hours);
    }

    log.info("Seed data initialized: 1 organization, 3 users, 1 bevilling with conditions and serving hours");
  }
}
