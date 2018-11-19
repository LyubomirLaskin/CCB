package app.ccb.services;

import app.ccb.domain.dtos.EmployeeImportDto;
import app.ccb.domain.entities.Branch;
import app.ccb.domain.entities.Client;
import app.ccb.domain.entities.Employee;
import app.ccb.repositories.BranchRepository;
import app.ccb.repositories.EmployeeRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.ValidationUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String EMPLOYEES_JSON_FILE_PATH = "D:\\Repositories\\BitBucket\\ColonialCouncilBank\\src\\main\\resources\\files\\json\\employees.json";

    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, BranchRepository branchRepository, FileUtil fileUtil, ValidationUtil validationUtil) {
        this.employeeRepository = employeeRepository;
        this.branchRepository = branchRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
    }

    @Override
    public String readEmployeesJsonFile() throws IOException {
        return this.fileUtil.readFile(EMPLOYEES_JSON_FILE_PATH);
    }

    @Override
    public String importEmployees(String employees) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        EmployeeImportDto[] employeeImportDtos = gson.fromJson(employees, EmployeeImportDto[].class);

        StringBuilder sb = new StringBuilder();
        for (EmployeeImportDto employeeImportDto : employeeImportDtos) {
            Employee employee = new Employee();
            try {
                String firstName = employeeImportDto.getFullName().split("\\s+")[0];
                String lastName = employeeImportDto.getFullName().split("\\s+")[1];
                employee.setFirstName(firstName);
                employee.setLastName(lastName);
            } catch (Exception e) {
                sb.append("Error: Incorrect Data!").append(System.lineSeparator());
                continue;
            }
            Branch branch = this.branchRepository.findByName(employeeImportDto.getBranchName()).orElse(null);

            employee.setSalary(employeeImportDto.getSalary());
            employee.setStartedOn(LocalDate.parse(employeeImportDto.getStartedOn()));
            employee.setBranch(branch);

            if (this.validationUtil.isValid(employee)) {
                this.employeeRepository.saveAndFlush(employee);
                sb.append(String.format("Successfully imported Employee - %s %s.", employee.getFirstName(), employee.getLastName()))
                        .append(System.lineSeparator());
            } else {
                sb.append("Error: Incorrect Data!").append(System.lineSeparator());
            }
        }

        return sb.toString().trim();
    }

    @Override
    public String exportTopEmployees() {
        List<Employee> employees = this.employeeRepository.getAllTopEmployees();

        StringBuilder sb = new StringBuilder();
        for (Employee employee : employees) {
            sb.append(String.format("Full Name: %s %s", employee.getFirstName(), employee.getLastName())).append(System.lineSeparator());
            sb.append(String.format("Salary: %.2f", employee.getSalary())).append(System.lineSeparator());
            sb.append(String.format("Started On: %s", String.valueOf(employee.getStartedOn()))).append(System.lineSeparator());
            sb.append("Clients: ").append(System.lineSeparator());

            for (Client client : employee.getClients()) {
                sb.append(String.format("   %s", client.getFullName())).append(System.lineSeparator());
            }

            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}
