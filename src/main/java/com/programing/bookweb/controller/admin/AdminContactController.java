package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.dto.Email;
import com.programing.bookweb.entity.Contact;
import com.programing.bookweb.enums.ContactStatus;
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
                                            @RequestParam(name = "status", required = false) String statusParam,
                                            Model model){
        Pageable pageable = PageRequest.of(page - 1, 10);
//        Page<Contact> contacts = contactService.getAllContactPage(pageable);
        Page<Contact> contacts;

        String statusTemp = (statusParam != null && !statusParam.trim().isEmpty()) ? statusParam.trim() : null;
        ContactStatus status = null;
        if (statusTemp != null) {
            status = ContactStatus.valueOf(statusTemp);
        }

        try {
            if (status != null) {
                contacts = contactService.getContactsByStatus(status, pageable);
            } else {
                contacts = contactService.getAllContactPage(pageable);
            }

        } catch (Exception e) {
            e.printStackTrace();
            contacts = contactService.getAllContactPage(pageable);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách đơn hàng: " + e.getMessage());
        }

        model.addAttribute("contacts", contacts);
        model.addAttribute("totalPages", contacts.getTotalPages());
        model.addAttribute("pageNumber", page);
        model.addAttribute("selectedStatus", status != null ? status.toString() : null);
        model.addAttribute("contactStatuses", ContactStatus.values());

        return "admin/contacts";
    }


    @GetMapping("/delete/{id}")
    public String deleteContact(@PathVariable Long id){
        contactService.deleteContact(id);
        return "redirect:/dashboard/contact_management";
    }


    @GetMapping("/response/{id}")
    public String response(@PathVariable Long id, Model model){
        //String userEmail = contactService.getContactById(id).getEmail();
        Contact contact = contactService.getContactById(id);

        // Update status to PROCESSING when responding
        if (contact.getStatus() == null || contact.getStatus() == ContactStatus.PENDING) {
            contact.setStatus(ContactStatus.PROCESSING);
            contactService.saveContact(contact);
        }

        String userEmail = contact.getEmail();
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

    @PostMapping("/update-status/{id}")
    public String updateContactStatus(@PathVariable Long id,
                                      @RequestParam("status") ContactStatus status,
                                      RedirectAttributes redirectAttributes) {
        try {
            Contact contact = contactService.getContactById(id);
            if (contact == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy liên hệ");
                return "redirect:/dashboard/contact_management";
            }

            contact.setStatus(status);
            contactService.saveContact(contact);

            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái liên hệ thành " + status.getLabel());
            return "redirect:/dashboard/contact_management";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return "redirect:/dashboard/contact_management";
        }
    }

}
