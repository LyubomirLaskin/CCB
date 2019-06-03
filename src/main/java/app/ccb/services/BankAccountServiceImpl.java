package app.ccb.services;

import app.ccb.domain.dtos.bankAccount.BankAccountImportDto;
import app.ccb.domain.dtos.bankAccount.BankAccountImportRootDto;
import app.ccb.domain.entities.BankAccount;
import app.ccb.domain.entities.Client;
import app.ccb.repositories.BankAccountRepository;
import app.ccb.repositories.ClientRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final ClientRepository clientRepository;
    private final static String BANKACCOUNT_XML_FILE_PATH = "E:\\SoftUni\\JavaDatabase\\Projects\\CCB\\src\\main\\resources\\files\\xml\\bank-accounts.xml";
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository, ClientRepository clientRepository, FileUtil fileUtil, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.bankAccountRepository = bankAccountRepository;
        this.clientRepository = clientRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean bankAccountsAreImported() {
        return this.bankAccountRepository.count() != 0;
    }

    @Override
    public String readBankAccountsXmlFile() throws IOException {
        return this.fileUtil.readFile(BANKACCOUNT_XML_FILE_PATH);
    }

    @Override
    public String importBankAccounts() throws JAXBException {
        StringBuilder importer = new StringBuilder();
        JAXBContext context = JAXBContext.newInstance(BankAccountImportRootDto.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        BankAccountImportRootDto bankAccountImportRootDto = (BankAccountImportRootDto) unmarshaller.unmarshal(new File(BANKACCOUNT_XML_FILE_PATH));

        for (BankAccountImportDto bankAccountImportDto : bankAccountImportRootDto.getBankAccountImportDtos()) {
            if (!validationUtil.isValid(bankAccountImportDto)){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Client client = this.clientRepository.findByFullName(bankAccountImportDto.getClient()).orElse(null);

            if (client == null){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            BankAccount bankAccountEntity = this.modelMapper.map(bankAccountImportDto,BankAccount.class);
            bankAccountEntity.setClient(client);
            this.bankAccountRepository.saveAndFlush(bankAccountEntity);

            importer.append(String.format("Successfully imported Bank Account - %s.", bankAccountEntity.getAccountNumber())).append(System.lineSeparator());

        }

        return importer.toString().trim();
    }
}
