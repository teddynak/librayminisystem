package com.library.interfaces;

import java.util.List;

// OOP: Interface - a reusable contract implemented by the book service.
public interface Searchable<T> {
  List<T> search(String keyword);
}
