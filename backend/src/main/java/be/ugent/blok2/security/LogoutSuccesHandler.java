package be.ugent.blok2.security;

import be.ugent.blok2.helpers.exceptions.UserNotLoggedInException;
import be.ugent.blok2.model.users.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Service
public class LogoutSuccesHandler implements LogoutSuccessHandler {
    private final UsersCache usersCache = UsersCache.getInstance();

    /**
     * When a user logs out, remove the mapping cookie and notify the userscache to
     * do the same.
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        HttpSession session = httpServletRequest.getSession();
        if(session!=null){
            try {
                if(authentication !=null && authentication.getPrincipal() != null){
                    this.usersCache.logout((User) authentication.getPrincipal());
                }
            } catch (UserNotLoggedInException e) {
            }
            // remove cookie
            Cookie cookie = new Cookie("mapping", "");
            cookie.setHttpOnly(false);
            cookie.setMaxAge(0);
            httpServletResponse.addCookie(cookie);

            session.invalidate();
        }
        httpServletResponse.sendRedirect("/login");
    }
}
