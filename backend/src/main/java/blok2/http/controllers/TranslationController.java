package blok2.http.controllers;

import blok2.http.security.authorization.AuthorizedController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("translations")
public class TranslationController extends AuthorizedController {
}
