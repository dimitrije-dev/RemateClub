package com.remateclub.booking;

import com.remateclub.common.exception.ConflictException;

public class BookingTimeUnavailableException extends ConflictException {

  public BookingTimeUnavailableException() {
    super("Requested booking time is unavailable");
  }
}
