package be.ugent.blok2.security;

import be.ugent.blok2.model.users.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Service
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
    private final UsersCache usersCache = UsersCache.getInstance();

    /**
     * When a user can succesfully login, he gets a mapping cookie from the
     * userscache and gets redirected to the dashboard page.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        HttpSession session = httpServletRequest.getSession();
        if (session != null) {
            String mapping = this.usersCache.login(session.getId(), (User) authentication.getPrincipal());

            Cookie cookie = new Cookie("mapping", mapping);
            cookie.setHttpOnly(false);
            httpServletResponse.addCookie(cookie);
        }
        httpServletResponse.sendRedirect("/dashboard");
    }
}
