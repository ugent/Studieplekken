package blok2.http.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import blok2.http.controllers.authorization.AuthorizedController;

@RestController
@RequestMapping("translations")
public class TranslationController extends AuthorizedController {
}
