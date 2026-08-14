package com.remateclub.clubimage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.remateclub.common.exception.BadRequestException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ClubImageStorageTest {

  @TempDir
  Path tempDirectory;

  @Test
  void storesValidatedPngUsingGeneratedStorageKey() throws Exception {
    ClubImageStorage storage = new ClubImageStorage(tempDirectory.toString());
    byte[] png = png(800, 600);

    StoredImage stored = storage.store(new MockMultipartFile(
      "file",
      "../../../court.png",
      "application/octet-stream",
      png
    ));

    assertThat(stored.contentType()).isEqualTo("image/png");
    assertThat(stored.dimensions()).isEqualTo(new ImageDimensions(800, 600));
    assertThat(stored.storageKey()).endsWith(".png").doesNotContain("court").doesNotContain("..");
    assertThat(Files.size(tempDirectory.resolve(stored.storageKey()))).isEqualTo(png.length);
  }

  @Test
  void rejectsUnsupportedContentEvenWhenFilenameLooksLikeAnImage() {
    ClubImageStorage storage = new ClubImageStorage(tempDirectory.toString());
    MockMultipartFile fake = new MockMultipartFile("file", "court.jpg", "image/jpeg", "not-an-image".getBytes());

    assertThatThrownBy(() -> storage.store(fake))
      .isInstanceOf(BadRequestException.class)
      .hasMessageContaining("JPEG, PNG i WebP");
  }

  @Test
  void rejectsImagesBelowMinimumResolution() throws Exception {
    ClubImageStorage storage = new ClubImageStorage(tempDirectory.toString());
    MockMultipartFile small = new MockMultipartFile("file", "small.png", "image/png", png(599, 399));

    assertThatThrownBy(() -> storage.store(small))
      .isInstanceOf(BadRequestException.class)
      .hasMessageContaining("600 × 400");
  }

  private byte[] png(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ImageIO.write(image, "png", output);
    return output.toByteArray();
  }
}
