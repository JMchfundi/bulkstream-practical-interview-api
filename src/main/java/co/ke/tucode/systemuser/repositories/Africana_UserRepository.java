package co.ke.tucode.systemuser.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.ke.tucode.systemuser.entities.TRES_User;

public interface Africana_UserRepository extends JpaRepository<TRES_User, Long> {
    public List<TRES_User> findByEmail(String email);
    public boolean existsByEmail(String email); 
    public void deleteByEmail(String email);  
    // public List<Africana_User> findByUsername(String username);
}
