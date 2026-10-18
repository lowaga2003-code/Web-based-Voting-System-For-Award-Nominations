package com.pation.awardnomination.policy;

import com.pation.awardnomination.admin.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MODULE 6 - consent on the registration form (Module 1's POST /api/auth/register).
 *
 * (සිංහලෙන්: Register page එකේ "I agree" checkbox එක tick කරලා නැත්නම් account එක හදන්න දෙන්නේ නැහැ.
 *  Account එක හැදුණාම REGISTRATION consent record එකක් consent_records table එකට save කරනවා.
 *  Module 1 (admin) member ගේ AuthController එක වෙනස් නොකර, ඒ request එක වටේට මේ filter එක වැඩ කරනවා.)
 *
 *  - Public sign-up must send ?consent=accepted (register.jsp does) - otherwise 400 and no account is created.
 *  - After the account is created (HTTP 200 and the e-mail did not exist before), a REGISTRATION consent is saved.
 *  - "Add User" by a logged-in System Administrator passes through: that person accepts at their first sign-in.
 */
@Component
public class RegistrationConsentFilter extends OncePerRequestFilter {

    static final String REGISTER_PATH = "/api/auth/register";
    static final String CONSENT_PARAM = "consent";
    static final String CONSENT_ACCEPTED = "accepted";
    private static final int MAX_BODY_BYTES = 16 * 1024;
    private static final Pattern EMAIL_FIELD = Pattern.compile("\"email\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationConsentFilter.class);

    private final ConsentService consentService;
    private final PolicyAccounts accounts;
    private final UserRepository users;

    public RegistrationConsentFilter(ConsentService consentService, PolicyAccounts accounts, UserRepository users) {
        this.consentService = consentService;
        this.accounts = accounts;
        this.users = users;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !("POST".equalsIgnoreCase(request.getMethod()) && REGISTER_PATH.equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (accounts.isAdmin()) {
            chain.doFilter(request, response); // "Add User" in User Management
            return;
        }
        if (!CONSENT_ACCEPTED.equals(request.getParameter(CONSENT_PARAM))) {
            reject(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Error: Please agree to the Privacy Policy and the Terms & Conditions to create an account.");
            return;
        }
        byte[] body = request.getInputStream().readNBytes(MAX_BODY_BYTES + 1);
        if (body.length > MAX_BODY_BYTES) {
            reject(response, 413, "Error: The request is too large.");
            return;
        }
        Charset charset = request.getCharacterEncoding() == null
                ? StandardCharsets.UTF_8 : Charset.forName(request.getCharacterEncoding());
        String email = emailFrom(new String(body, charset));
        boolean newAccount = email != null && !Boolean.TRUE.equals(users.existsByEmail(email));

        chain.doFilter(new CachedBodyRequest(request, body), response); // Module 1's AuthController runs here

        if (newAccount && response.getStatus() == HttpServletResponse.SC_OK) {
            users.findByEmail(email).ifPresent(user -> {
                try {
                    consentService.recordConsent(user.getId(), ConsentType.REGISTRATION, null,
                            PolicyAccounts.clientIp(request), PolicyAccounts.userAgent(request));
                } catch (RuntimeException e) {
                    // the account exists anyway - the user will simply be asked to accept at the first sign-in
                    LOG.warn("Could not save the registration consent for user #{}: {}", user.getId(), e.getMessage());
                }
            });
        }
    }

    /** The "email" value from the JSON body that register.jsp sends. */
    static String emailFrom(String json) {
        Matcher m = EMAIL_FIELD.matcher(json == null ? "" : json);
        if (!m.find()) {
            return null;
        }
        String email = m.group(1).replace("\\\"", "\"").replace("\\/", "/").replace("\\\\", "\\").trim();
        return email.isEmpty() ? null : email;
    }

    private static void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
    }

    /** Gives the already-read JSON body to the controller again. */
    static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            final InputStream in = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    try {
                        return in.available() == 0;
                    } catch (IOException e) {
                        return true;
                    }
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException("Asynchronous reading is not supported here.");
                }

                @Override
                public int read() throws IOException {
                    return in.read();
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return in.read(b, off, len);
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            Charset charset = getCharacterEncoding() == null ? StandardCharsets.UTF_8 : Charset.forName(getCharacterEncoding());
            return new BufferedReader(new InputStreamReader(getInputStream(), charset));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }
    }
}
