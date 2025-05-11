package com.programing.bookweb.service;

import com.programing.bookweb.entity.Contact;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IContactService {

    Contact saveContact(Contact contact, User user);

    void deleteContact(Long contactId);

    Contact updateContact(Contact contact);

    Contact getContactById(Long contactId);

    Page<Contact> getAllContactPage(Pageable pageable);

    Page<Contact> getContactsByStatus(ContactStatus status, Pageable pageable);
}
