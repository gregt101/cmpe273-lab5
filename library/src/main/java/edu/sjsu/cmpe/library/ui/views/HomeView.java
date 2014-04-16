package edu.sjsu.cmpe.library.ui.views;

import com.yammer.dropwizard.views.View;

import edu.sjsu.cmpe.library.domain.Book;

import java.util.List;

/*
public class HomeView extends View {
    private final Book book;

    public HomeView(Book book) {
	super("home.mustache");
	this.book = book;
    }

    public Book getBook() {
	return book;
    }
}
*/
public class HomeView extends View {
        private final List<Book> books;
        public HomeView(List<Book> books) {
                  super("home.mustache");
                  this.books = books;
        }
        public List<Book> getBooks() {
                  return books;
        }
}
