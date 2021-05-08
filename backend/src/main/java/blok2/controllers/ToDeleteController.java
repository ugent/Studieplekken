package blok2.controllers;

import blok2.mail.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@RestController
@RequestMapping("Yljsd4QMFN")
public class ToDeleteController {

    @Autowired
    private MailService mailService;

    @GetMapping("{mail}")
    @PreAuthorize("permitAll()")
    public void sendMail(@PathVariable("mail") String mail) throws MessagingException {
        mailService.sendTestMail(mail);
    }

}
