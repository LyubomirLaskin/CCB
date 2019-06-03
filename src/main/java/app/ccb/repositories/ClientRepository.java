package app.ccb.repositories;

import app.ccb.domain.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByFullName(String name);

    @Query("" +
            "SELECT c " +
            "FROM app.ccb.domain.entities.Client c " +
            "JOIN c.bankAccount b " +
            "JOIN b.cards cd " +
            "GROUP BY c.id " +
            "ORDER BY size(b.cards) DESC")
    List<Client> exportFamilyGuy();
}
