package com.remateclub.clubimage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.remateclub.club.Club;
import com.remateclub.club.ClubRepository;
import com.remateclub.common.exception.BadRequestException;
import com.remateclub.user.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ClubImageServiceTest {

  @Mock ClubRepository clubRepository;
  @Mock ClubImageRepository imageRepository;
  @Mock ClubImageStorage storage;
  @Mock MultipartFile file;

  private ClubImageService service;

  @BeforeEach
  void setUp() {
    service = new ClubImageService(clubRepository, imageRepository, storage);
  }

  @Test
  void ownerCannotUploadToAnotherOwnersClub() {
    UUID ownerId = UUID.randomUUID();
    UUID clubId = UUID.randomUUID();
    Club club = club(clubId, UUID.randomUUID());
    when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));

    assertThatThrownBy(() -> service.upload(ownerId, clubId, file, "Teren", false))
      .isInstanceOf(AccessDeniedException.class);
    verify(storage, never()).store(any());
  }

  @Test
  void firstUploadedImageAutomaticallyBecomesCover() {
    UUID ownerId = UUID.randomUUID();
    UUID clubId = UUID.randomUUID();
    Club club = club(clubId, ownerId);
    when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    when(imageRepository.countByClubId(clubId)).thenReturn(0L);
    when(file.getOriginalFilename()).thenReturn("court.png");
    when(storage.store(file)).thenReturn(new StoredImage("safe.png", "image/png", 1234, new ImageDimensions(1600, 1200)));
    when(imageRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ClubImageResponse response = service.upload(ownerId, clubId, file, "Centralni teren", false);

    assertThat(response.cover()).isTrue();
    assertThat(response.width()).isEqualTo(1600);
    verify(imageRepository).clearCover(clubId);
  }

  @Test
  void refusesMoreThanEightImagesWithoutWritingAFile() {
    UUID ownerId = UUID.randomUUID();
    UUID clubId = UUID.randomUUID();
    Club club = club(clubId, ownerId);
    when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
    when(imageRepository.countByClubId(clubId)).thenReturn(8L);

    assertThatThrownBy(() -> service.upload(ownerId, clubId, file, "Još jedna slika", false))
      .isInstanceOf(BadRequestException.class)
      .hasMessageContaining("najviše 8");
    verify(storage, never()).store(any());
  }

  private Club club(UUID clubId, UUID ownerId) {
    Club club = mock(Club.class);
    User owner = mock(User.class);
    lenient().when(club.getId()).thenReturn(clubId);
    when(club.getOwner()).thenReturn(owner);
    when(owner.getId()).thenReturn(ownerId);
    return club;
  }
}
