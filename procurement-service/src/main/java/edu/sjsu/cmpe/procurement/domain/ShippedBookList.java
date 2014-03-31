package edu.sjsu.cmpe.procurement.domain;

import java.util.List;
import java.util.ArrayList;

public class ShippedBookList {
  private List<Book> shippedBookList = new ArrayList<Book>();
  
  public List<Book> getShippedBookList() {
      return shippedBookList;
  }

  public void setShippedBookList (List<Book> shippedBookList) {
      this.shippedBookList = shippedBookList;
  }

}
