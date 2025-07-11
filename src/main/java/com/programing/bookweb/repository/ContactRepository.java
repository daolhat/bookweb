package com.programing.bookweb.repository;

import com.programing.bookweb.entity.Contact;
import com.programing.bookweb.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findByOrderByCreatedAtDesc(Pageable pageable);

    Page<Contact> findByStatus(ContactStatus status, Pageable pageable);

}
