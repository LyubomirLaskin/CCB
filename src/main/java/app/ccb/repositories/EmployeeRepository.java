package app.ccb.repositories;

import app.ccb.domain.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    @Query("" +
            "SELECT e " +
            "FROM app.ccb.domain.entities.Employee e " +
            "WHERE concat(e.firstName,' ', e.lastName) = :fullName")
    Optional<Employee> findByFullName(@Param("fullName") String name);

    @Query("" +
            "SELECT e " +
            "FROM app.ccb.domain.entities.Employee e " +
            "WHERE size(e.clients) > 0" +
            "ORDER BY size(e.clients) DESC, e.id")
    List<Employee> exportTopEmployees();
}
