package blok2.http.security.handlers;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
@Configuration
@ConfigurationProperties(prefix = "redirect")
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private String successLoginRedirectUrl;

    /**
     * When the user has successfully logged in, we need to redirect the user to the
     * frontends home page.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        httpServletResponse.sendRedirect(successLoginRedirectUrl);
    }

    public void setSuccessLoginRedirectUrl(String successLoginRedirectUrl) {
        this.successLoginRedirectUrl = successLoginRedirectUrl;
    }

}
