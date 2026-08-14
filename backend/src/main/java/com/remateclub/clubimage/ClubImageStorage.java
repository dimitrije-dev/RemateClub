package com.remateclub.clubimage;

import com.remateclub.common.exception.BadRequestException;
import com.remateclub.common.exception.ResourceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClubImageStorage {

  static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
  static final int MIN_WIDTH = 600;
  static final int MIN_HEIGHT = 400;
  static final int MAX_DIMENSION = 6000;

  private final Path root;

  public ClubImageStorage(@Value("${remate-club.storage.images-directory:./data/uploads}") String directory) {
    this.root = Path.of(directory).toAbsolutePath().normalize();
  }

  public StoredImage store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("Izaberi sliku za upload");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new BadRequestException("Slika može imati najviše 5 MB");
    }

    try {
      byte[] bytes = file.getBytes();
      String contentType = detectContentType(bytes);
      ImageDimensions dimensions = readDimensions(bytes, contentType);
      validateDimensions(dimensions);
      String storageKey = UUID.randomUUID() + extension(contentType);
      Files.createDirectories(root);
      Files.write(resolve(storageKey), bytes, StandardOpenOption.CREATE_NEW);
      return new StoredImage(storageKey, contentType, bytes.length, dimensions);
    } catch (IOException exception) {
      throw new IllegalStateException("Slika nije mogla da se sačuva", exception);
    }
  }

  public Resource load(String storageKey) {
    try {
      Path path = resolve(storageKey);
      Resource resource = new UrlResource(path.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new ResourceNotFoundException("Image file not found");
      }
      return resource;
    } catch (IOException exception) {
      throw new ResourceNotFoundException("Image file not found");
    }
  }

  public void delete(String storageKey) {
    try {
      Files.deleteIfExists(resolve(storageKey));
    } catch (IOException exception) {
      throw new IllegalStateException("Slika nije mogla da se obriše", exception);
    }
  }

  private Path resolve(String storageKey) {
    Path path = root.resolve(storageKey).normalize();
    if (!path.startsWith(root)) {
      throw new BadRequestException("Invalid image storage key");
    }
    return path;
  }

  private String detectContentType(byte[] bytes) {
    if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
      return "image/jpeg";
    }
    if (bytes.length >= 8
      && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
      && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) {
      return "image/png";
    }
    if (bytes.length >= 16
      && ascii(bytes, 0, 4).equals("RIFF")
      && ascii(bytes, 8, 4).equals("WEBP")) {
      return "image/webp";
    }
    throw new BadRequestException("Dozvoljeni formati su JPEG, PNG i WebP");
  }

  private ImageDimensions readDimensions(byte[] bytes, String contentType) throws IOException {
    if (contentType.equals("image/webp")) {
      return readWebpDimensions(bytes);
    }
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
    if (image == null) {
      throw new BadRequestException("Fajl nije validna slika");
    }
    return new ImageDimensions(image.getWidth(), image.getHeight());
  }

  private ImageDimensions readWebpDimensions(byte[] bytes) {
    String chunk = ascii(bytes, 12, 4);
    if (chunk.equals("VP8X") && bytes.length >= 30) {
      return new ImageDimensions(1 + littleEndian24(bytes, 24), 1 + littleEndian24(bytes, 27));
    }
    if (chunk.equals("VP8 ") && bytes.length >= 30
      && (bytes[23] & 0xff) == 0x9d && (bytes[24] & 0xff) == 0x01 && (bytes[25] & 0xff) == 0x2a) {
      int width = littleEndian16(bytes, 26) & 0x3fff;
      int height = littleEndian16(bytes, 28) & 0x3fff;
      return new ImageDimensions(width, height);
    }
    if (chunk.equals("VP8L") && bytes.length >= 25 && (bytes[20] & 0xff) == 0x2f) {
      int b1 = bytes[21] & 0xff;
      int b2 = bytes[22] & 0xff;
      int b3 = bytes[23] & 0xff;
      int b4 = bytes[24] & 0xff;
      int width = 1 + b1 + ((b2 & 0x3f) << 8);
      int height = 1 + ((b2 & 0xc0) >> 6) + (b3 << 2) + ((b4 & 0x0f) << 10);
      return new ImageDimensions(width, height);
    }
    throw new BadRequestException("WebP slika nema podržano zaglavlje");
  }

  private void validateDimensions(ImageDimensions dimensions) {
    if (dimensions.width() < MIN_WIDTH || dimensions.height() < MIN_HEIGHT) {
      throw new BadRequestException("Slika mora biti najmanje 600 × 400 px");
    }
    if (dimensions.width() > MAX_DIMENSION || dimensions.height() > MAX_DIMENSION) {
      throw new BadRequestException("Slika može biti najviše 6000 × 6000 px");
    }
  }

  private String extension(String contentType) {
    return switch (contentType.toLowerCase(Locale.ROOT)) {
      case "image/jpeg" -> ".jpg";
      case "image/png" -> ".png";
      case "image/webp" -> ".webp";
      default -> throw new BadRequestException("Unsupported image type");
    };
  }

  private String ascii(byte[] bytes, int offset, int length) {
    if (offset + length > bytes.length) return "";
    return new String(bytes, offset, length, java.nio.charset.StandardCharsets.US_ASCII);
  }

  private int littleEndian16(byte[] bytes, int offset) {
    return ByteBuffer.wrap(bytes, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xffff;
  }

  private int littleEndian24(byte[] bytes, int offset) {
    return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8) | ((bytes[offset + 2] & 0xff) << 16);
  }
}
