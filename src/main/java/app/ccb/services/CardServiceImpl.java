package app.ccb.services;

import app.ccb.domain.dtos.cards.CardImportDto;
import app.ccb.domain.dtos.cards.CardImportRootDto;
import app.ccb.domain.entities.BankAccount;
import app.ccb.domain.entities.Card;
import app.ccb.repositories.BankAccountRepository;
import app.ccb.repositories.CardRepository;
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
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final BankAccountRepository bankAccountRepository;
    private final static String CARDS_XML_FILE_PATH= "E:\\SoftUni\\JavaDatabase\\Projects\\CCB\\src\\main\\resources\\files\\xml\\cards.xml";
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;


    @Autowired
    public CardServiceImpl(CardRepository cardRepository, BankAccountRepository bankAccountRepository, FileUtil fileUtil, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean cardsAreImported() {
        return this.cardRepository.count() != 0;
    }

    @Override
    public String readCardsXmlFile() throws IOException {
        return fileUtil.readFile(CARDS_XML_FILE_PATH);
    }

    @Override
    public String importCards() throws JAXBException {
        StringBuilder importer = new StringBuilder();

        JAXBContext context = JAXBContext.newInstance(CardImportRootDto.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        CardImportRootDto cardImportRootDto = (CardImportRootDto) unmarshaller.unmarshal(new File(CARDS_XML_FILE_PATH));

        for (CardImportDto cardImportDto : cardImportRootDto.getCardImportDtos()) {
            if (!this.validationUtil.isValid(cardImportDto)){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }
            BankAccount bankAccountEntity = this.bankAccountRepository.findBankAccountByAccountNumber(cardImportDto.getAccountNumber()).orElse(null);

            if (bankAccountEntity == null){
                importer.append("Error: Incorrect Data!").append(System.lineSeparator());

                continue;
            }

            Card card = this.modelMapper.map(cardImportDto,Card.class);
            card.setBankAccount(bankAccountEntity);
            this.cardRepository.saveAndFlush(card);

            importer.append(String.format("Successfully imported Card - %s", card.getCardNumber()));
        }

        return importer.toString().trim();
    }
}
