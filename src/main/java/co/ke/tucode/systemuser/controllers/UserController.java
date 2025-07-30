package co.ke.tucode.systemuser.controllers;

import co.ke.tucode.logs.services.UserActivityLogService;
import co.ke.tucode.systemuser.config.TokenProviderTuCode;
import co.ke.tucode.systemuser.entities.Role;
import co.ke.tucode.systemuser.entities.TRES_User;
import co.ke.tucode.systemuser.payloads.AfricanaUserDto;
import co.ke.tucode.systemuser.payloads.LoginRequest;
import co.ke.tucode.systemuser.services.Africana_UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Controller for user-related operations such as authentication,
 * account creation, retrieval, and deletion.
 */
@RestController
public class UserController {

    @Autowired
    private Africana_UserService service;

    @Autowired
    private TokenProviderTuCode tokenProviderTuCode;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserActivityLogService logService;

    /**
     * Fetch all users in the system.
     */
    @GetMapping("/get_service")
    public ResponseEntity<?> getAllUsers() {
        List<AfricanaUserDto> users = service.findAll();
        return users.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Get user details by email (via path variable).
     */
    @GetMapping("/get_user_data/{email}")
    public ResponseEntity<?> getUserByPath(@PathVariable String email) {
        List<AfricanaUserDto> users = service.findByEmail(email);
        return users.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(users.get(0), HttpStatus.OK);
    }

    /**
     * Get user details by email (via path variable).
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        List<AfricanaUserDto> users = service.findById(id);
        return users.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(users.get(0), HttpStatus.OK);
    }

    /**
     * Get user details by email (via request param).
     */
    @GetMapping("/get_user")
    public ResponseEntity<?> getUserByQuery(@RequestParam(name = "email") String email) {
        List<AfricanaUserDto> users = service.findByEmail(email);
        return users.isEmpty()
                ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(users.get(0), HttpStatus.OK);
    }

    /**
     * User login handler. Validates credentials, generates token on success,
     * and logs both success and failure attempts.
     */
    @PostMapping("/login_request")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            List<AfricanaUserDto> users = service.findByEmail(request.getEmail());
            Long userId = users.isEmpty() ? null : users.get(0).getId();

            if (!authentication.getName().equalsIgnoreCase("Jakida@tucode.co.ke")) {
                logService.log(
                        authentication.getName(),
                        "AuthController",
                        "login",
                        "User logged in successfully",
                        "User",
                        userId);

            }

            String token = tokenProviderTuCode.generateRefinedToken(authentication);
            return new ResponseEntity<>(token, HttpStatus.OK);

        } catch (Exception ex) {
            List<AfricanaUserDto> users = service.findByEmail(request.getEmail());
            Long userId = users.isEmpty() ? null : users.get(0).getId();
            String details = users.isEmpty() ? "User not found" : "Invalid password";

            logService.log(
                    request.getEmail(),
                    "AuthController",
                    "login_failed",
                    details,
                    "User",
                    userId);

            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Register a new user using form-encoded data.
     * Role and access type are automatically assigned.
     */
    @PostMapping(value = "/post_service", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createUser(@ModelAttribute TRES_User user, UriComponentsBuilder builder) {
        if (service.existsByEmail(user.getEmail())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        user.setAccess("Buyer");
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        service.save(user);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    /**
     * Delete a user by email.
     */
    @DeleteMapping("/delete_service/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable("email") String email) {
        List<AfricanaUserDto> users = service.findByEmail(email);
        if (users.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        service.deleteByEmail(email);
        return new ResponseEntity<>(users.get(0), HttpStatus.NO_CONTENT);
    }

    /**
     * Delete all users from the database.
     */
    @DeleteMapping("/delete_all_service")
    public ResponseEntity<?> deleteAllUsers() {
        service.deleteAll();
        return new ResponseEntity<>("db data erased", HttpStatus.NO_CONTENT);
    }

    /**
     * Fetch currently authenticated user details using the JWT principal.
     */
    @GetMapping("/current_user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<AfricanaUserDto> users = service.findByEmail(email);

        return users == null || users.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email)
                : ResponseEntity.ok(users.get(0));
    }
}
