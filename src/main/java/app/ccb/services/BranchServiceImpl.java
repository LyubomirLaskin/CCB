package app.ccb.services;

import app.ccb.domain.dtos.BranchImportDto;
import app.ccb.domain.entities.Branch;
import app.ccb.repositories.BranchRepository;
import app.ccb.util.FileUtil;
import app.ccb.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final static String BRANCH_JSON_FILE_PATH = "E:\\SoftUni\\JavaDatabase\\Projects\\CCB\\src\\main\\resources\\files\\json\\branches.json";
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final Gson gson;
    private final ModelMapper modelMapper;

    @Autowired
    public BranchServiceImpl(BranchRepository branchRepository, FileUtil fileUtil, ValidationUtil validationUtil, Gson gson, ModelMapper modelMapper) {
        this.branchRepository = branchRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.gson = gson;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean branchesAreImported() {
        return this.branchRepository.count() != 0;
    }

    @Override
    public String readBranchesJsonFile() throws IOException {
        return fileUtil.readFile(BRANCH_JSON_FILE_PATH);
    }

    @Override
    public String importBranches(String branchesJson) {
        StringBuilder importer = new StringBuilder();
        BranchImportDto[] branchImportDtos = this.gson.fromJson(branchesJson, BranchImportDto[].class);

        for (BranchImportDto Dto : branchImportDtos) {
            if (!validationUtil.isValid(Dto)){
                importer.append("Error: Incorrect Data!");

                continue;
            }

            Branch branchEntity = this.modelMapper.map(Dto, Branch.class);
            this.branchRepository.saveAndFlush(branchEntity);

            importer.append(String.format("Successfully imported Branch – %s.", branchEntity.getName())).append(System.lineSeparator());
        }
        return importer.toString().trim();
    }
}
