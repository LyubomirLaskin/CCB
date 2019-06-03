package app.ccb.services;

import app.ccb.domain.dtos.ClientImportDto;
import app.ccb.domain.entities.Card;
import app.ccb.domain.entities.Client;
import app.ccb.domain.entities.Employee;
import app.ccb.repositories.ClientRepository;
import app.ccb.repositories.EmployeeRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final static String CLIENT_JSON_FILE_PATH = "E:\\SoftUni\\JavaDatabase\\Projects\\CCB\\src\\main\\resources\\files\\json\\clients.json";
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, EmployeeRepository employeeRepository, FileUtil fileUtil, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.fileUtil = fileUtil;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean clientsAreImported() {
        return this.clientRepository.count() != 0;
    }

    @Override
    public String readClientsJsonFile() throws IOException {
        return fileUtil.readFile(CLIENT_JSON_FILE_PATH);
    }

    @Override
    public String importClients(String clients) {
        StringBuilder importer = new StringBuilder();

        ClientImportDto[] clientImportDtos = this.gson.fromJson(clients, ClientImportDto[].class);

        for (ClientImportDto clientImportDto : clientImportDtos) {
            if (!validationUtil.isValid(clientImportDto)){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }
            Employee employeeEntity = this.employeeRepository.findByFullName(clientImportDto.getAppointedEmployee()).orElse(null);

            if (employeeEntity == null){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }
            Client client = this.clientRepository.findByFullName(clientImportDto.getFirstName() + " " + clientImportDto.getLastName()).orElse(null);

            if (client != null){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }
            client = this.modelMapper.map(clientImportDto,Client.class);
            client.setFullName(clientImportDto.getFirstName() + " " + clientImportDto.getLastName());
            client.getEmployees().add(employeeEntity);

            clientRepository.saveAndFlush(client);

            importer.append(String.format("Successfully imported Client - %s.",client.getFullName())).append(System.lineSeparator());
        }
        return importer.toString().trim();
    }

    @Override
    public String exportFamilyGuy() {
        Client clientEntity = this.clientRepository.exportFamilyGuy().stream().findFirst().orElse(null);
        StringBuilder exporter = new StringBuilder();

        exporter.append(String.format("Full Name: %s",clientEntity.getFullName())).append(System.lineSeparator());
        exporter.append(String.format("Age: %d", clientEntity.getAge())).append(System.lineSeparator());
        exporter.append(String.format("Bank Account: %s",clientEntity.getBankAccount().getAccountNumber())).append(System.lineSeparator());

        for (Card card : clientEntity.getBankAccount().getCards()) {
            exporter.append(String.format("Card Number: %s",card.getCardNumber())).append(System.lineSeparator());
            exporter.append(String.format("Card Status: %s",card.getCardStatus())).append(System.lineSeparator());
        }

        return exporter.toString().trim();
    }
}
