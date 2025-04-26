package com.programing.bookweb.service;

import com.programing.bookweb.entity.Contact;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface IContactService {

    Contact saveContact(Contact contact);

    List<Contact> getAllContacts();

    void deleteContact(Long contactId);

    Contact getContactById(Long contactId);

}
