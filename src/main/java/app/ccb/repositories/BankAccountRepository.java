package app.ccb.repositories;

import app.ccb.domain.entities.BankAccount;
import app.ccb.domain.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {

    Optional<BankAccount> findBankAccountByAccountNumber(String accountNumber);
}
