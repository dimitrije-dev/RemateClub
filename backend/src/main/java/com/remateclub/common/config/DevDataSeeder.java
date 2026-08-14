package com.remateclub.common.config;

import com.remateclub.club.Club;
import com.remateclub.club.ClubRepository;
import com.remateclub.court.Court;
import com.remateclub.court.CourtRepository;
import com.remateclub.court.CourtType;
import com.remateclub.user.User;
import com.remateclub.user.UserRepository;
import com.remateclub.user.UserRole;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

  static final String DEV_PASSWORD = "Remate123!";

  private final UserRepository userRepository;
  private final ClubRepository clubRepository;
  private final CourtRepository courtRepository;
  private final PasswordEncoder passwordEncoder;

  public DevDataSeeder(
    UserRepository userRepository,
    ClubRepository clubRepository,
    CourtRepository courtRepository,
    PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.clubRepository = clubRepository;
    this.courtRepository = courtRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments arguments) {
    createUser("admin@remate.local", "Admin", "Remate", UserRole.ADMIN);
    User owner = createUser("owner@remate.local", "Mila", "Vlasnik", UserRole.OWNER);
    createUser("player@remate.local", "Nikola", "Igrač", UserRole.PLAYER);

    if (!clubRepository.findAllByOwnerIdOrderByCreatedAtDesc(owner.getId()).isEmpty()) {
      return;
    }

    Club approvedClub = new Club(owner, "Remate Arena Dorćol", "Beograd");
    approvedClub.approve();
    clubRepository.saveAndFlush(approvedClub);
    clubRepository.saveAndFlush(new Club(owner, "Remate Padel Novi Beograd", "Beograd"));

    courtRepository.saveAllAndFlush(List.of(
      new Court(approvedClub, "Centralni panoramski", CourtType.PANORAMIC, true, new BigDecimal("3600.00")),
      new Court(approvedClub, "Teren 2", CourtType.STANDARD, true, new BigDecimal("3000.00"))
    ));
  }

  private User createUser(String email, String firstName, String lastName, UserRole role) {
    return userRepository.findByEmail(email).orElseGet(() -> userRepository.saveAndFlush(new User(
      email,
      passwordEncoder.encode(DEV_PASSWORD),
      firstName,
      lastName,
      role
    )));
  }
}
