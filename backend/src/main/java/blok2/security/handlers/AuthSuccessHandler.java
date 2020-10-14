package blok2.security.handlers;

import blok2.helpers.Resources;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Service
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * When the user has successfully logged in, we need to redirect the user to the
     * frontends home page.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        httpServletResponse.sendRedirect(Resources.blokatugentConf.getString("successfulLoginRedirectUrl"));
    }

}
