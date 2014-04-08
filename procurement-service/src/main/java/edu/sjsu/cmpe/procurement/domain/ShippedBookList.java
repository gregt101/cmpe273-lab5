package edu.sjsu.cmpe.procurement.domain;

import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShippedBookList {

  @JsonProperty("shipped_books")
  private List<Book> shippedBookList = new ArrayList<Book>();

  public List<Book> getShippedBookList() {
      return shippedBookList;
  }

  public void setShippedBookList (List<Book> shippedBookList) {
      this.shippedBookList = shippedBookList;
  }

}
