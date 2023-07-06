package blok2.http.security.handlers;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        httpServletResponse.addHeader("Set-Cookie", "JSESSIONID=; Max-Age=0; Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        httpServletResponse.addHeader("Set-Cookie", "XSRF-TOKEN=; Max-Age=0; Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    }
}
