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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final static String EMPLOYEE_JSON_FILE_PATH = "E:\\SoftUni\\JavaDatabase\\Projects\\CCB\\src\\main\\resources\\files\\json\\employees.json";
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final BranchRepository branchRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository, FileUtil fileUtil, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper, BranchRepository branchRepository) {
        this.employeeRepository = employeeRepository;
        this.fileUtil = fileUtil;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.branchRepository = branchRepository;
    }

    @Override
    public Boolean employeesAreImported() {
        return this.employeeRepository.count() != 0;
    }

    @Override
    public String readEmployeesJsonFile() throws IOException {
        return fileUtil.readFile(EMPLOYEE_JSON_FILE_PATH);
    }

    @Override
    public String importEmployees(String employees) {
        StringBuilder importer = new StringBuilder();

        EmployeeImportDto[] employeeImportDtos = gson.fromJson(employees, EmployeeImportDto[].class);

        for (EmployeeImportDto employeeImportDto : employeeImportDtos) {
            if(!validationUtil.isValid(employeeImportDto)){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Branch branchEntity = this.branchRepository.findByName(employeeImportDto.getBranchName()).orElse(null);

            if (branchEntity == null){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Employee employeeEntity = this.modelMapper.map(employeeImportDto, Employee.class);
            employeeEntity.setFirstName(employeeImportDto.getFullName().split("\\s+")[0]);
            employeeEntity.setLastName(employeeImportDto.getFullName().split("\\s+")[1]);
            employeeEntity.setStartedOn(LocalDate.parse(employeeImportDto.getStartedOn()));
            employeeEntity.setBranch(branchEntity);

            this.employeeRepository.saveAndFlush(employeeEntity);

            importer.append(String.format("Successfully imported Employee - %s %s",employeeEntity.getFirstName(),employeeEntity.getLastName())).append(System.lineSeparator());

        }
        return importer.toString().trim();
    }

    @Override
    public String exportTopEmployees() {
        List<Employee> employees = this.employeeRepository.exportTopEmployees();
        StringBuilder exporter = new StringBuilder();

        for (Employee employee : employees) {
            exporter.append(String.format("Full Name: %s %s",employee.getFirstName(),employee.getLastName())).append(System.lineSeparator());
            exporter.append(String.format("Salary: %f",employee.getSalary())).append(System.lineSeparator());
            exporter.append(String.format("Started On: %s",employee.getStartedOn())).append(System.lineSeparator());
            exporter.append("Clients").append(System.lineSeparator());
            List<Client> clients = employee.getClients();
            for (Client client : clients) {
                exporter.append(client.getFullName()).append(System.lineSeparator());
            }
        }
        return exporter.toString();
    }
}
