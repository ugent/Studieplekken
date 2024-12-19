package blok2.security.filter;

import blok2.model.users.User;
import blok2.security.token.CustomAuthenticationToken;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private AuthenticationManager authManager;

    public CustomAuthenticationFilter(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Filters incoming HTTP requests and processes custom authentication.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during the filtering process
     * @throws IOException if an I/O error occurs during the filtering process
     *
     * This method checks for a custom header "AS-USER" in the request. If the header is present and the current
     * authentication is not null, it verifies if the authenticated user is an admin. If the user is an admin,
     * it creates a new custom authentication token and sets it in the security context.
     */
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String username = request.getHeader("AS-USER");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (username != null && auth != null) {
            Object principal = auth.getPrincipal();

            if (principal instanceof User) {
                User user = (User) principal;

                if (user.isAdmin()) {
                    Authentication token = new CustomAuthenticationToken(username);
                    Authentication authToken = authManager.authenticate(token);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
