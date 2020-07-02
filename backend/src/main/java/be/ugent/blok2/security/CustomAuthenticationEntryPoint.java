package be.ugent.blok2.security;

import be.ugent.blok2.helpers.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    public static final String AUTHENTICATION_TYPE_HEADER = "Authentication-Type";
    public static final String AUTHENTICATION_TYPE_AUGENT = "AUGent-Email-Based-Authentication";

    private final CasAuthenticationEntryPoint casAuthenticationEntryPoint;
    private final HttpStatusEntryPoint httpStatusEntryPoint;

    @Autowired
    public CustomAuthenticationEntryPoint(ServiceProperties serviceProperties) {
        // AuthenticationEntryPoint when the login method is via CAS
        casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties);
        casAuthenticationEntryPoint.setLoginUrl(Resources.applicationProperties.getString("casLoginUrl"));

        // AuthenticationEntryPoint when the login method is not via CAS, i.e. via our own email based login mechanism
        httpStatusEntryPoint = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        // When the user is not a UGhent student, he/she needs to login using our email based login mechanism. The
        // frontend will add a custom HTTP header 'Authentication-Type' which will have the value 'AUGent-Email-Based-Authentication'
        // if set, otherwise the CAS authentication entry point should be used
        if (AUTHENTICATION_TYPE_AUGENT.equals(httpServletRequest.getHeader(AUTHENTICATION_TYPE_HEADER)))
            httpStatusEntryPoint.commence(httpServletRequest, httpServletResponse, e);
        else
            casAuthenticationEntryPoint.commence(httpServletRequest, httpServletResponse, e);
    }
}
