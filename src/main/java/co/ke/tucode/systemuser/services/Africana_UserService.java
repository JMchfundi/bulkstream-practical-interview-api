package co.ke.tucode.systemuser.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.ke.tucode.systemuser.entities.TRES_User;
import co.ke.tucode.systemuser.payloads.AfricanaUserDto;
import co.ke.tucode.systemuser.repositories.Africana_UserRepository;
import jakarta.transaction.Transactional;

import java.util.List;

@Service
@Transactional
public class Africana_UserService implements UserDetailsService {

    @Autowired
    private Africana_UserRepository repository;

    public void save(TRES_User certificate) {
        repository.save(certificate);
    }

    public List<AfricanaUserDto> findAll() {
        return repository.findAll().stream()
                .map(user -> new AfricanaUserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getAccess(),
                        user.getRole() != null ? user.getRole().name() : null))
                .toList();
    }

    public void update(TRES_User certificate) {
        repository.save(certificate);
    }

    public void deleteByEmail(String email) {
        repository.deleteByEmail(email);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

        public List<AfricanaUserDto> findById(Long id) {
        return repository.findById(id).stream()
                .map(user -> new AfricanaUserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getAccess(),
                        user.getRole() != null ? user.getRole().name() : null))
                .toList();
    }


    public List<AfricanaUserDto> findByEmail(String email) {
        return repository.findByEmail(email).stream()
                .map(user -> new AfricanaUserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getAccess(),
                        user.getRole() != null ? user.getRole().name() : null))
                .toList();
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public Long count() {
        return repository.count();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO Auto-generated method stub
        List<TRES_User> users = repository.findByEmail(username);

        if (users.isEmpty()) {
            throw new UsernameNotFoundException(username + " doesn't exist");
        }

        return new User(users.get(0).getEmail(), users.get(0).getPassword(), users.get(0).getAuthorities());
    }

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // TODO Auto-generated method stub
                List<TRES_User> users = repository.findByEmail(username);

                if (users.isEmpty()) {
                    throw new UsernameNotFoundException(username + " doesn't exist");
                }

                return new User(users.get(0).getEmail(), users.get(0).getPassword(), users.get(0).getAuthorities());
            }
        };
    }
}