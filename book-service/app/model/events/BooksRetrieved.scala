package model.events

import model.Book

case class BooksRetrieved ( books: Seq [Book] )