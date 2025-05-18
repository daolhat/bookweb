package com.programing.bookweb.controller;

import com.programing.bookweb.entity.Contact;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.enums.ContactStatus;
import com.programing.bookweb.service.IContactService;
import com.programing.bookweb.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/contact")
public class ContactController extends BaseController{

    private final IContactService contactService;
    private final IUserService userService;

    @GetMapping
    public String getContactPage(Model model){
        User user = userService.getUserById(getCurrentUser().getId());
        model.addAttribute("user", user);
        model.addAttribute("contact", new Contact());
        return "user/contact";
    }

    @PostMapping("/submit")
    public String submitContactForm(@ModelAttribute Contact contact,
                                    RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        contact.setStatus(ContactStatus.PENDING);
        Contact savedContact = contactService.saveContact(contact, user);
        if(savedContact != null){
            redirectAttributes.addFlashAttribute("thankForContacting","Cảm ơn bạn đã liên hệ!");
            return "redirect:/contact?success=true";
        } else {
            redirectAttributes.addFlashAttribute("error","Lỗi khi gửi liên hệ!");
            return "redirect:/contact?error=error_sending_contact";
        }

    }
}
