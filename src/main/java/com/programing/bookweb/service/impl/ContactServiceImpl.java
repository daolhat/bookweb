package com.programing.bookweb.service.impl;

import com.programing.bookweb.entity.Contact;
import com.programing.bookweb.repository.ContactRepository;
import com.programing.bookweb.service.IContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactServiceImpl implements IContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Override
    public Contact saveContact(Contact contact) {
        return contactRepository.save(contact);
    }

    @Override
    public List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void deleteContact(Long contactId) {
        Optional<Contact> contact = contactRepository.findById(contactId);
        if (contact.isPresent()){
            contactRepository.deleteById(contactId);
        }
    }

    @Override
    public Contact getContactById(Long contactId) {
        return contactRepository.findById(contactId).orElse(null);
    }

    @Override
    public Page<Contact> getAllContactPage(Pageable pageable) {
        return contactRepository.findByOrderByCreatedAtDesc(pageable);
    }

}
