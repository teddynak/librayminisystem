package com.library.model;

import com.library.exception.LibraryException;

public final class BookCopy {
  private final long copyId, bookId;
  private CopyStatus status;

  public BookCopy(long copyId, long bookId, CopyStatus status) {
    this.copyId = Validation.id(copyId);
    this.bookId = Validation.id(bookId);
    if (status == null) throw new LibraryException("Copy status is required.");
    this.status = status;
  }

  public long getCopyId() {
    return copyId;
  }

  public long getBookId() {
    return bookId;
  }

  public CopyStatus getStatus() {
    return status;
  }

  public boolean isAvailable() {
    return status == CopyStatus.AVAILABLE;
  }

  // OOP: Encapsulation - state changes must follow the circulation rules.
  public void markBorrowed() {
    if (!isAvailable()) throw new LibraryException("This copy is unavailable.");
    status = CopyStatus.BORROWED;
  }

  public void markAvailable() {
    if (status != CopyStatus.BORROWED) throw new LibraryException("This copy is not borrowed.");
    status = CopyStatus.AVAILABLE;
  }

  public void deactivate() {
    if (status == CopyStatus.BORROWED)
      throw new LibraryException("Return this copy before deactivating it.");
    status = CopyStatus.INACTIVE;
  }

  @Override
  public String toString() {
    return "Copy " + copyId + " / book " + bookId + " / " + status;
  }
}
