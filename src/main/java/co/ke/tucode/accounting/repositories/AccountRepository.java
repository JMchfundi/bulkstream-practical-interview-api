package co.ke.tucode.accounting.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.ke.finsis.entity.LoanType;
import co.ke.finsis.inventory.entity.Category;
import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.entities.AccountType;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Used for generating the next account code (optional in your current logic)
    @Query("SELECT a FROM Account a WHERE a.type = :type ORDER BY a.code DESC")
    List<Account> findTopByTypeOrderByCodeDesc(@Param("type") AccountType type);

    Optional<Account> findByName(String name);

    Optional<Account> findByNameAndAccountCategory(String accountName, AccountType receivable);

    boolean existsByNameAndAccountCategory(String name, AccountType accountCategory);

    Optional<Account> findByCode(String string);
}
