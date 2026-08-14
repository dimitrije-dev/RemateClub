package com.remateclub.clubimage;

record StoredImage(
  String storageKey,
  String contentType,
  long fileSize,
  ImageDimensions dimensions
) {
}
