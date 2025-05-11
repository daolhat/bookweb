package com.programing.bookweb.service.impl;

import com.programing.bookweb.entity.Contact;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.enums.ContactStatus;
import com.programing.bookweb.repository.ContactRepository;
import com.programing.bookweb.service.IContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContactServiceImpl implements IContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Override
    public Contact saveContact(Contact contact, User user) {
        contact.setUser(user);
        return contactRepository.save(contact);
    }

    @Override
    public void deleteContact(Long contactId) {
        Optional<Contact> contact = contactRepository.findById(contactId);
        if (contact.isPresent()){
            contactRepository.deleteById(contactId);
        }
    }

    @Override
    public Contact updateContact(Contact contact) {
        return contactRepository.save(contact);
    }

    @Override
    public Contact getContactById(Long contactId) {
        return contactRepository.findById(contactId).orElse(null);
    }

    @Override
    public Page<Contact> getAllContactPage(Pageable pageable) {
        return contactRepository.findByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<Contact> getContactsByStatus(ContactStatus status, Pageable pageable) {
        if (status == null){
            return contactRepository.findAll(pageable);
        }
        return contactRepository.findByStatus(status, pageable);
    }

}
