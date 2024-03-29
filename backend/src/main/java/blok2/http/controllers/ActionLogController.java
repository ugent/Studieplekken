package blok2.http.controllers;

import blok2.database.dao.IActionLogDao;
import blok2.model.ActionLogEntry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("actions")
public class ActionLogController {

    private final IActionLogDao actionLogDao;

    public ActionLogController(IActionLogDao actionLogDao) {
        this.actionLogDao = actionLogDao;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<ActionLogEntry> getAllActions() {
        return actionLogDao.getAllActions();
    }

}
