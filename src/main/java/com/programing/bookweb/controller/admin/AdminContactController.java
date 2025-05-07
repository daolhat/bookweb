package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.dto.Email;
import com.programing.bookweb.entity.Contact;
import com.programing.bookweb.service.IContactService;
import com.programing.bookweb.service.IEmailService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/contact_management")
public class AdminContactController extends BaseController {

    private final IContactService contactService;
    private final IEmailService emailService;


    @GetMapping
    public String showContactPageManagement(@RequestParam(name = "page", defaultValue = "1") int page,
                                            Model model){
        Pageable pageable = PageRequest.of(page - 1, 15);
        Page<Contact> contacts = contactService.getAllContactPage(pageable);
        //List<Contact> contacts = contactService.getAllContacts();
        model.addAttribute("contacts", contacts);
        model.addAttribute("totalPages", contacts.getTotalPages());
        model.addAttribute("pageNumber", page);

        return "admin/contacts";
    }


    @GetMapping("/delete/{id}")
    public String deleteContact(@PathVariable Long id){
        contactService.deleteContact(id);
        return "redirect:/dashboard/contact_management";
    }


    @GetMapping("/response/{id}")
    public String response(@PathVariable Long id, Model model){
        String userEmail = contactService.getContactById(id).getEmail();
        Email email = new Email();
        email.setTo(userEmail);
        model.addAttribute("newEmail", email);
        model.addAttribute("uid", id);
        return "admin/contacts-response";
    }


    @PostMapping("/submit_email")
    public String responseTo(@ModelAttribute Email email,
                             @RequestParam Long uid,
                             RedirectAttributes redirectAttributes){
        emailService.sendEmail(email.getTo(),email.getSubject(),email.getMessage());
        redirectAttributes.addFlashAttribute("message","Gửi mail thành công!");
        return "redirect:/dashboard/contact_management/response/" + uid + "?success=true";
    }

}
