package com.barco.auth.service.impl;

import com.barco.auth.service.LookupDataCacheService;
import com.barco.auth.service.RPPService;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.excel.BulkExcel;
import com.barco.common.utility.excel.ExcelUtil;
import com.barco.common.utility.excel.SheetFiled;
import com.barco.common.utility.validation.RPPValidation;
import com.barco.model.dto.request.*;
import com.barco.model.dto.response.*;
import com.barco.model.pojo.*;
import com.barco.model.repository.*;
import com.barco.model.util.MessageUtil;
import com.barco.model.util.lookup.APPLICATION_STATUS;
import com.barco.model.util.lookup.GLookup;
import com.barco.model.util.lookup.LookupUtil;
import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Nabeel Ahmed
 * TemplateReg can be email and etc
 */
@Service
public class RPPServiceImpl implements RPPService {

    private Logger logger = LoggerFactory.getLogger(RPPServiceImpl.class);

    @Value("${storage.efsFileDire}")
    private String tempStoreDirectory;
    @Autowired
    private BulkExcel bulkExcel;
    @Autowired
    private QueryService queryService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private ProfilePermissionRepository profilePermissionRepository;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private LookupDataCacheService lookupDataCacheService;
    @Autowired
    private AppUserRoleAccessRepository appUserRoleAccessRepository;
    @Autowired
    private AppUserProfileAccessRepository appUserProfileAccessRepository;

    public RPPServiceImpl() {}

    /**
     * Method use to add new role
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse addRole(RoleRequest payload) throws Exception {
        logger.info("Request addRole :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (BarcoUtil.isNull(payload.getName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_NAME_MISSING, payload);
        } else if (BarcoUtil.isNull(payload.getDescription())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_DESCRIPTION_MISSING, payload);
        } else if (this.roleRepository.findRoleByName(payload.getName()).isPresent()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_ALREADY_EXIST, payload);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        Role role = new Role();
        role.setName(payload.getName());
        role.setDescription(payload.getDescription());
        role.setCreatedBy(appUser.get());
        role.setUpdatedBy(appUser.get());
        role.setStatus(APPLICATION_STATUS.ACTIVE);
        this.roleRepository.save(role);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, role.getId().toString()), payload);
    }

    /**
     * Method use to update new role
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse updateRole(RoleRequest payload) throws Exception {
        logger.info("Request updateRole :-  {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_ID_MISSING, payload);
        } else if (BarcoUtil.isNull(payload.getName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_NAME_MISSING, payload);
        }
        Optional<Role> role = this.roleRepository.findByUuid(payload.getUuid());
        if (role.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.ROLE_NOT_FOUND_WITH_ID, payload.getId()), payload);
        } else if (!role.get().getName().equals(payload.getName()) && this.roleRepository.findRoleByName(payload.getName()).isPresent()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_ALREADY_EXIST, payload);
        }
        role.get().setName(payload.getName());
        if (!BarcoUtil.isNull(payload.getDescription())) {
            role.get().setDescription(payload.getDescription());
        }
        // active and in-active
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (!BarcoUtil.isNull(payload.getStatus())) {
            // if status is in-active & delete then we have filter the role and show only those role in user detail
            role.get().setStatus(APPLICATION_STATUS.getByLookupCode(payload.getStatus()));
            role.get().getRoleAccesses().stream()
                .map(appUserRoleAccess -> {
                    appUserRoleAccess.setStatus(role.get().getStatus());
                    appUserRoleAccess.setUpdatedBy(appUser.get());
                    return appUserRoleAccess;
                });
        }
        role.get().setUpdatedBy(appUser.get());
        this.roleRepository.save(role.get());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, role.get().getId().toString()), payload);
    }

    /**
     * Method use to fetch all role
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchAllRole(RoleRequest payload) throws Exception {
        logger.info("Request fetchAllRole :- {}.", payload);
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY,
            RoleRepository.asStream(this.roleRepository.findAll().iterator()).map(this::gateRoleResponse).collect(Collectors.toList()));
    }

    /**
     * Method use to fetch role by id
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse findRoleById(RoleRequest payload) throws Exception {
        logger.info("Request findRoleById :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_ID_MISSING, payload);
        }
        return this.roleRepository.findByUuid(payload.getUuid())
            .map(value -> new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, this.gateRoleResponse(value)))
            .orElseGet(() -> new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.ROLE_NOT_FOUND_WITH_ID, payload.getId()), payload));
    }

    /**
     * Method use to delete role by id
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteRoleById(RoleRequest payload) throws Exception {
        logger.info("Request deleteRoleById :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_ID_MISSING, payload);
        }
        Optional<Role> role = this.roleRepository.findByUuid(payload.getUuid());
        if (role.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.ROLE_NOT_FOUND_WITH_ID, payload.getUuid()), payload);
        }
        this.roleRepository.delete(role.get());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_DELETED, payload.getUuid()));
    }

    /**
     * Method use to delete all role
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteAllRole(RoleRequest payload) throws Exception {
        logger.info("Request deleteAllRole :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.IDS_MISSING);
        }
        this.roleRepository.deleteAll(this.roleRepository.findAllByUuidIn(payload.getUuids()));
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_DELETED_ALL);
    }

    /**
     * Method use to download role template
     * @return ByteArrayOutputStream
     * @throws Exception
     * */
    @Override
    public ByteArrayOutputStream downloadRoleTemplateFile() throws Exception {
        logger.info("Request downloadRoleTemplateFile");
        return downloadTemplateFile(this.tempStoreDirectory, this.bulkExcel,
            this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.ROLE));
    }

    /**
     * Method use to download role data
     * @return ByteArrayOutputStream
     * @throws Exception
     * */
    @Override
    public ByteArrayOutputStream downloadRole(RoleRequest payload) throws Exception {
        logger.info("Request downloadRole :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.APPUSER_NOT_FOUND);
        }
        SheetFiled sheetFiled = this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.ROLE);
        XSSFWorkbook workbook = new XSSFWorkbook();
        this.bulkExcel.setWb(workbook);
        XSSFSheet xssfSheet = workbook.createSheet(sheetFiled.getSheetName());
        this.bulkExcel.setSheet(xssfSheet);
        AtomicInteger rowCount = new AtomicInteger();
        this.bulkExcel.fillBulkHeader(rowCount.get(), sheetFiled.getColTitle());
        Iterator<Role> roleList;
        if (!BarcoUtil.isNull(payload.getIds()) && !payload.getIds().isEmpty()) {
            roleList = this.roleRepository.findAllByIdIn(payload.getIds()).iterator();
        } else {
            roleList = this.roleRepository.findAll().iterator();
        }
        while (roleList.hasNext()) {
            Role role = roleList.next();
            rowCount.getAndIncrement();
            List<String> dataCellValue = new ArrayList<>();
            dataCellValue.add(role.getName());
            dataCellValue.add(role.getDescription());
            this.bulkExcel.fillBulkBody(dataCellValue, rowCount.get());
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        workbook.write(outStream);
        return outStream;
    }

    /**
     * Method use to upload role data
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse uploadRole(FileUploadRequest payload) throws Exception {
        logger.info("Request for bulk uploading file for role!");
        LookupDataRequest lookupDataRequest = new Gson().fromJson((String) payload.getData(), LookupDataRequest.class);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        }
        if (!payload.getFile().getContentType().equalsIgnoreCase(ExcelUtil.SHEET_TYPE)) {
            logger.info("File Type {}.", payload.getFile().getContentType());
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.XLSX_FILE_ONLY);
        }
        // fill the stream with file into work-book
        LookupDataResponse uploadLimit = this.lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.UPLOAD_LIMIT);
        XSSFWorkbook workbook = new XSSFWorkbook(payload.getFile().getInputStream());
        if (BarcoUtil.isNull(workbook) || workbook.getNumberOfSheets() == 0) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.YOU_UPLOAD_EMPTY_FILE);
        }
        SheetFiled sheetFiled = this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.ROLE);
        XSSFSheet sheet = workbook.getSheet(sheetFiled.getSheetName());
        if (BarcoUtil.isNull(sheet)) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.SHEET_NOT_FOUND, sheetFiled.getSheetName()));
        } else if (sheet.getLastRowNum() < 1) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.YOU_CANT_UPLOAD_EMPTY_FILE);
        } else if (sheet.getLastRowNum() > Long.parseLong(uploadLimit.getLookupValue())) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.FILE_SUPPORT_ROW_AT_TIME, uploadLimit.getLookupValue()));
        }
        List<RPPValidation> rppValidationsList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (Row currentRow : sheet) {
            if (currentRow.getRowNum() == 0) {
                for (int i = 0; i < sheetFiled.getColTitle().size(); i++) {
                    if (!currentRow.getCell(i).getStringCellValue().equals(sheetFiled.getColTitle().get(i))) {
                        return new AppResponse(BarcoUtil.ERROR, "File at row " + (currentRow.getRowNum() + 1)
                                + " " + sheetFiled.getColTitle().get(i) + " heading missing.");
                    }
                }
            } else if (currentRow.getRowNum() > 0) {
                RPPValidation rppValidation = new RPPValidation();
                rppValidation.setRowCounter(currentRow.getRowNum() + 1);
                for (int i = 0; i < sheetFiled.getColTitle().size(); i++) {
                    int index = 0;
                    if (i == index) {
                        rppValidation.setName(this.bulkExcel.getCellDetail(currentRow, i));
                    } else if (i == ++index) {
                        rppValidation.setDescription(this.bulkExcel.getCellDetail(currentRow, i));
                    }
                }
                rppValidation.isValidBatch();
                Optional<Role> isAlreadyExistRole = this.roleRepository.findRoleByName(rppValidation.getName());
                if (isAlreadyExistRole.isPresent()) {
                    rppValidation.setErrorMsg(String.format(MessageUtil.ROLE_TYPE_ALREADY_USE_AT_ROW, rppValidation.getName(), rppValidation.getRowCounter()));
                }
                if (!BarcoUtil.isNull(rppValidation.getErrorMsg())) {
                    errors.add(rppValidation.getErrorMsg());
                    continue;
                }
                rppValidationsList.add(rppValidation);
            }
        }
        if (!errors.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.TOTAL_INVALID, errors.size()), errors);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(lookupDataRequest.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        rppValidationsList.forEach(rppValidation -> {
            Role role = new Role();
            role.setName(rppValidation.getName());
            role.setDescription(rppValidation.getDescription());
            role.setCreatedBy(appUser.get());
            role.setUpdatedBy(appUser.get());
            role.setStatus(APPLICATION_STATUS.ACTIVE);
            this.roleRepository.save(role);
        });
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, ""));
    }

    /**
     * Method use to add the new profile
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse addProfile(ProfileRequest payload) throws Exception {
        logger.info("Request addProfile :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (BarcoUtil.isNull(payload.getProfileName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_NAME_MISSING, payload);
        } else if (BarcoUtil.isNull(payload.getDescription())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_DESCRIPTION_MISSING, payload);
        } else if (this.profileRepository.findProfileByProfileName(payload.getProfileName()).isPresent()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ALREADY_EXIST, payload);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        Profile profile = new Profile();
        profile.setProfileName(payload.getProfileName());
        profile.setDescription(payload.getDescription());
        profile.setCreatedBy(appUser.get());
        profile.setUpdatedBy(appUser.get());
        profile.setStatus(APPLICATION_STATUS.ACTIVE);
        this.profileRepository.save(profile);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED,profile.getId().toString()), payload);
    }

    /**
     * Method use to edit the new profile
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse updateProfile(ProfileRequest payload) throws Exception {
        logger.info("Request updateProfile :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ID_MISSING, payload);
        } else if (BarcoUtil.isNull(payload.getProfileName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_NAME_MISSING, payload);
        } else if (BarcoUtil.isNull(payload.getDescription())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_DESCRIPTION_MISSING, payload);
        }
        Optional<Profile> profile = this.profileRepository.findByUuid(payload.getUuid());
        if (profile.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.PROFILE_NOT_FOUND_WITH_ID, payload.getUuid()), payload);
        } else if (!profile.get().getProfileName().equals(payload.getProfileName()) && this.profileRepository.findProfileByProfileName(payload.getProfileName()).isPresent()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ALREADY_EXIST, payload);
        }
        profile.get().setProfileName(payload.getProfileName());
        if (!BarcoUtil.isNull(payload.getDescription())) {
            profile.get().setDescription(payload.getDescription());
        }
        // active and in-active
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (!BarcoUtil.isNull(payload.getStatus())) {
            // profile status
            profile.get().setStatus(APPLICATION_STATUS.getByLookupCode(payload.getStatus()));
            // profile-permission
            profile.get().getProfilePermissions().stream()
                .map(profilePermission -> {
                    profilePermission.setStatus(APPLICATION_STATUS.getByLookupCode(payload.getStatus()));
                    profilePermission.setUpdatedBy(appUser.get());
                    return profilePermission;
                });
            // profile-access
            profile.get().getProfileAccesses().stream()
                .map(appUserProfileAccess -> {
                    appUserProfileAccess.setStatus(APPLICATION_STATUS.getByLookupCode(payload.getStatus()));
                    appUserProfileAccess.setUpdatedBy(appUser.get());
                    return appUserProfileAccess;
                });
        }
        profile.get().setUpdatedBy(appUser.get());
        this.profileRepository.save(profile.get());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, profile.get().getId().toString()), payload);
    }

    /**
     * Method use to fetch all profile
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchAllProfile(ProfileRequest payload) throws Exception {
        logger.info("Request fetchAllProfile :- {}", payload);
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY,
            RoleRepository.asStream(this.profileRepository.findAll().iterator()).map(this::gateProfileResponse).collect(Collectors.toList()));
    }

    /**
     * Method use to fetch profile by id
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchProfileById(ProfileRequest payload) throws Exception {
        logger.info("Request fetchProfileById :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ID_MISSING, payload);
        }
        return this.profileRepository.findByUuid(payload.getUuid())
            .map(value -> new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, this.gateProfileResponse(value)))
            .orElseGet(() -> new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.PROFILE_NOT_FOUND_WITH_ID, payload.getUuid()), payload));
    }

    /**
     * Method use to delete profile by id
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteProfileById(ProfileRequest payload) throws Exception {
        logger.info("Request deleteProfileById :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ID_MISSING, payload);
        }
        Optional<Profile> profile = this.profileRepository.findByUuid(payload.getUuid());
        if (profile.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.PROFILE_NOT_FOUND_WITH_ID, payload.getUuid()), payload);
        }
        this.profileRepository.delete(profile.get());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_DELETED, payload.getUuid().toString()));
    }

    /**
     * Method use to delete all profile
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteAllProfile(ProfileRequest payload) throws Exception {
        logger.info("Request deleteAllProfile :- {}.", payload);
        if (BarcoUtil.isNull(payload.getIds())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.IDS_MISSING);
        }
        this.profileRepository.deleteAll(this.profileRepository.findAllByUuidIn(payload.getUuids()));
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_DELETED_ALL);
    }

    /**
     * Method use to download the profile template
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public ByteArrayOutputStream downloadProfileTemplateFile() throws Exception {
        logger.info("Request downloadProfileTemplateFile");
        return downloadTemplateFile(this.tempStoreDirectory, this.bulkExcel,
            this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.PROFILE));
    }

    /**
     * Method use to download profile
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public ByteArrayOutputStream downloadProfile(ProfileRequest payload) throws Exception {
        logger.info("Request downloadProfile :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        }
        SheetFiled sheetFiled = this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.PROFILE);
        XSSFWorkbook workbook = new XSSFWorkbook();
        this.bulkExcel.setWb(workbook);
        XSSFSheet xssfSheet = workbook.createSheet(sheetFiled.getSheetName());
        this.bulkExcel.setSheet(xssfSheet);
        AtomicInteger rowCount = new AtomicInteger();
        this.bulkExcel.fillBulkHeader(rowCount.get(), sheetFiled.getColTitle());
        Iterator<Profile> profileList;
        if (!BarcoUtil.isNull(payload.getIds()) && !payload.getIds().isEmpty()) {
            profileList = this.profileRepository.findAllByIdIn(payload.getIds()).iterator();
        } else {
            profileList = this.profileRepository.findAll().iterator();
        }
        while (profileList.hasNext()) {
            Profile profile = profileList.next();
            rowCount.getAndIncrement();
            List<String> dataCellValue = new ArrayList<>();
            dataCellValue.add(profile.getProfileName());
            dataCellValue.add(profile.getDescription());
            this.bulkExcel.fillBulkBody(dataCellValue, rowCount.get());
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream;
    }

    /**
     * Method use to upload profile
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse uploadProfile(FileUploadRequest payload) throws Exception {
        logger.info("Request for bulk uploading file for profile!");
        LookupDataRequest lookupDataRequest = new Gson().fromJson((String) payload.getData(), LookupDataRequest.class);
        AppResponse validationResponse = this.validateUsername(lookupDataRequest);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        }
        if (!payload.getFile().getContentType().equalsIgnoreCase(ExcelUtil.SHEET_TYPE)) {
            logger.info("File Type {}", payload.getFile().getContentType());
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.XLSX_FILE_ONLY);
        }
        // fill the stream with file into work-book
        LookupDataResponse uploadLimit = this.lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.UPLOAD_LIMIT);
        XSSFWorkbook workbook = new XSSFWorkbook(payload.getFile().getInputStream());
        if (BarcoUtil.isNull(workbook) || workbook.getNumberOfSheets() == 0) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.YOU_UPLOAD_EMPTY_FILE);
        }
        SheetFiled sheetFiled = this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.PROFILE);
        XSSFSheet sheet = workbook.getSheet(sheetFiled.getSheetName());
        if (BarcoUtil.isNull(sheet)) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.SHEET_NOT_FOUND, sheetFiled.getSheetName()));
        } else if (sheet.getLastRowNum() < 1) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.YOU_CANT_UPLOAD_EMPTY_FILE);
        } else if (sheet.getLastRowNum() > Long.valueOf(uploadLimit.getLookupValue())) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.FILE_SUPPORT_ROW_AT_TIME, uploadLimit.getLookupValue()));
        }
        List<RPPValidation> rppValidationsList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (Row currentRow : sheet) {
            if (currentRow.getRowNum() == 0) {
                for (int i = 0; i < sheetFiled.getColTitle().size(); i++) {
                    if (!currentRow.getCell(i).getStringCellValue().equals(sheetFiled.getColTitle().get(i))) {
                        return new AppResponse(BarcoUtil.ERROR, "File at row " + (currentRow.getRowNum() + 1)
                                + " " + sheetFiled.getColTitle().get(i) + " heading missing.");
                    }
                }
            } else if (currentRow.getRowNum() > 0) {
                RPPValidation rppValidation = new RPPValidation();
                rppValidation.setRowCounter(currentRow.getRowNum() + 1);
                for (int i = 0; i < sheetFiled.getColTitle().size(); i++) {
                    int index = 0;
                    if (i == index) {
                        rppValidation.setName(this.bulkExcel.getCellDetail(currentRow, i));
                    } else if (i == ++index) {
                        rppValidation.setDescription(this.bulkExcel.getCellDetail(currentRow, i));
                    }
                }
                rppValidation.isValidBatch();
                Optional<Profile> isAlreadyExistProfile = this.profileRepository.findProfileByProfileName(rppValidation.getName());
                if (isAlreadyExistProfile.isPresent()) {
                    rppValidation.setErrorMsg(String.format(MessageUtil.PROFILE_TYPE_ALREADY_USE_AT_ROW, rppValidation.getName(), rppValidation.getRowCounter()));
                }
                if (!BarcoUtil.isNull(rppValidation.getErrorMsg())) {
                    errors.add(rppValidation.getErrorMsg());
                    continue;
                }
                rppValidationsList.add(rppValidation);
            }
        }
        if (!errors.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.TOTAL_INVALID, errors.size()), errors);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(lookupDataRequest.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        rppValidationsList.forEach(rppValidation -> {
            Profile profile = new Profile();
            profile.setProfileName(rppValidation.getName());
            profile.setDescription(rppValidation.getDescription());
            profile.setCreatedBy(appUser.get());
            profile.setUpdatedBy(appUser.get());
            profile.setStatus(APPLICATION_STATUS.ACTIVE);
            this.profileRepository.save(profile);
        });
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, ""));
    }

    /**
     * Method use to add permission
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse addPermission(PermissionRequest payload) throws Exception {
        logger.info("Request addPermission :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (BarcoUtil.isNull(payload.getPermissionName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_NAME_MISSING, payload);
        } else if (BarcoUtil.isNull(payload.getDescription())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_DESCRIPTION_MISSING, payload);
        } else if (this.permissionRepository.findPermissionByPermissionName(payload.getPermissionName()).isPresent()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_ALREADY_EXIST, payload);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        Permission permission = new Permission();
        permission.setPermissionName(payload.getPermissionName());
        permission.setDescription(payload.getDescription());
        permission.setCreatedBy(appUser.get());
        permission.setUpdatedBy(appUser.get());
        permission.setStatus(APPLICATION_STATUS.ACTIVE);
        this.permissionRepository.save(permission);
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, permission.getId().toString()), payload);
    }

    /**
     * Method use to edit permission
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse updatePermission(PermissionRequest payload) throws Exception {
        logger.info("Request updatePermission :- {}.",payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            return validationResponse;
        } else if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_ID_MISSING, payload);
        } else if (BarcoUtil.isNull(payload.getPermissionName())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_NAME_MISSING, payload);
        } else if (BarcoUtil.isNull(payload.getDescription())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_DESCRIPTION_MISSING, payload);
        }
        Optional<Permission> permission = this.permissionRepository.findByUuid(payload.getUuid());
        if (permission.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.PERMISSION_NOT_FOUND_WITH_ID, payload.getUuid()), payload);
        } else if (!permission.get().getPermissionName().equals(payload.getPermissionName()) &&
            this.permissionRepository.findPermissionByPermissionName(payload.getPermissionName()).isPresent()) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_ALREADY_EXIST, payload);
        }
        permission.get().setPermissionName(payload.getPermissionName());
        if (!BarcoUtil.isNull(payload.getDescription())) {
            permission.get().setDescription(payload.getDescription());
        }
        // active and in-active
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (!BarcoUtil.isNull(payload.getStatus())) {
            permission.get().setStatus(APPLICATION_STATUS.getByLookupCode(payload.getStatus()));
            permission.get().getProfilePermissions().stream()
                .map(profilePermission -> {
                    profilePermission.setStatus(APPLICATION_STATUS.getByLookupCode(payload.getStatus()));
                    profilePermission.setUpdatedBy(appUser.get());
                    return profilePermission;
                });
        }
        permission.get().setUpdatedBy(appUser.get());
        this.permissionRepository.save(permission.get());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, permission.get().getId().toString()), payload);
    }

    /**
     * Method use to fetch all permission
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchAllPermission(PermissionRequest payload) throws Exception {
        logger.info("Request fetchAllPermission :- {}.", payload);
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY,
            RoleRepository.asStream(this.permissionRepository.findAll().iterator()).map(this::gatePermissionResponse).collect(Collectors.toList()));
    }

    /**
     * Method use to fetch permission by id
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchPermissionById(PermissionRequest payload) throws Exception {
        logger.info("Request fetchPermissionById :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_ID_MISSING, payload);
        }
        return this.permissionRepository.findByUuid(payload.getUuid())
            .map(value -> new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, this.gatePermissionResponse(value)))
            .orElseGet(() -> new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.PERMISSION_NOT_FOUND_WITH_ID, payload.getUuid()), payload));
    }

    /**
     * Method use to delete permission by id
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deletePermissionById(PermissionRequest payload) throws Exception {
        logger.info("Request deletePermissionById :- {}.", payload);
        if (BarcoUtil.isNull(payload.getUuid())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_ID_MISSING, payload);
        }
        Optional<Permission> permission = this.permissionRepository.findByUuid(payload.getUuid());
        if (permission.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.PERMISSION_NOT_FOUND_WITH_ID, payload.getUuid()), payload);
        }
        // delete with permission user
        this.permissionRepository.delete(permission.get());
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_DELETED, payload.getUuid()));
    }

    /**
     * Method use to delete all permission
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse deleteAllPermission(PermissionRequest payload) throws Exception {
        logger.info("Request deleteAllPermission :- {}.",payload);
        if (BarcoUtil.isNull(payload.getUuids())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.IDS_MISSING);
        }
        this.permissionRepository.deleteAll(this.permissionRepository.findAllByUuidIn(payload.getUuids()));
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_DELETED_ALL);
    }

    /**
     * Method use to download permission template
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public ByteArrayOutputStream downloadPermissionTemplateFile() throws Exception {
        logger.info("Request downloadPermissionTemplateFile");
        return downloadTemplateFile(this.tempStoreDirectory, this.bulkExcel,
            this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.PERMISSION));
    }

    /**
     * Method use to download permission
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public ByteArrayOutputStream downloadPermission(PermissionRequest payload) throws Exception {
        logger.info("Request downloadPermission :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        }
        SheetFiled sheetFiled = this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.PERMISSION);
        XSSFWorkbook workbook = new XSSFWorkbook();
        this.bulkExcel.setWb(workbook);
        XSSFSheet xssfSheet = workbook.createSheet(sheetFiled.getSheetName());
        this.bulkExcel.setSheet(xssfSheet);
        AtomicInteger rowCount = new AtomicInteger();
        this.bulkExcel.fillBulkHeader(rowCount.get(), sheetFiled.getColTitle());
        Iterator<Permission> permissions;
        if (!BarcoUtil.isNull(payload.getIds()) && !payload.getIds().isEmpty()) {
            permissions = this.permissionRepository.findAllByIdIn(payload.getIds()).iterator();
        } else {
            permissions = this.permissionRepository.findAll().iterator();
        }
        while (permissions.hasNext()) {
            rowCount.getAndIncrement();
            Permission permission = permissions.next();
            List<String> dataCellValue = new ArrayList<>();
            dataCellValue.add(permission.getPermissionName());
            dataCellValue.add(permission.getDescription());
            this.bulkExcel.fillBulkBody(dataCellValue, rowCount.get());
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream;
    }

    /**
     * Method use to upload permission
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse uploadPermission(FileUploadRequest payload) throws Exception {
        logger.info("Request for bulk uploading file!");
        LookupDataRequest lookupDataRequest = new Gson().fromJson((String) payload.getData(), LookupDataRequest.class);
        AppResponse validationResponse = this.validateUsername(lookupDataRequest);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        } else if (!payload.getFile().getContentType().equalsIgnoreCase(ExcelUtil.SHEET_TYPE)) {
            logger.info("File Type {}.", payload.getFile().getContentType());
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.XLSX_FILE_ONLY);
        }
        // fill the stream with file into work-book
        LookupDataResponse uploadLimit = this.lookupDataCacheService.getParentLookupDataByParentLookupType(LookupUtil.UPLOAD_LIMIT);
        XSSFWorkbook workbook = new XSSFWorkbook(payload.getFile().getInputStream());
        if (BarcoUtil.isNull(workbook) || workbook.getNumberOfSheets() == 0) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.YOU_UPLOAD_EMPTY_FILE);
        }
        SheetFiled sheetFiled = this.lookupDataCacheService.getSheetFiledMap().get(ExcelUtil.PERMISSION);
        XSSFSheet sheet = workbook.getSheet(sheetFiled.getSheetName());
        if (BarcoUtil.isNull(sheet)) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.SHEET_NOT_FOUND, sheetFiled.getSheetName()));
        } else if (sheet.getLastRowNum() < 1) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.YOU_CANT_UPLOAD_EMPTY_FILE);
        } else if (sheet.getLastRowNum() > Long.valueOf(uploadLimit.getLookupValue())) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.FILE_SUPPORT_ROW_AT_TIME, uploadLimit.getLookupValue()));
        }
        List<RPPValidation> rppValidationsList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (Row currentRow : sheet) {
            if (currentRow.getRowNum() == 0) {
                for (int i = 0; i < sheetFiled.getColTitle().size(); i++) {
                    if (!currentRow.getCell(i).getStringCellValue().equals(sheetFiled.getColTitle().get(i))) {
                        return new AppResponse(BarcoUtil.ERROR, "File at row " + (currentRow.getRowNum() + 1)
                                + " " + sheetFiled.getColTitle().get(i) + " heading missing.");
                    }
                }
            } else if (currentRow.getRowNum() > 0) {
                RPPValidation rppValidation = new RPPValidation();
                rppValidation.setRowCounter(currentRow.getRowNum() + 1);
                for (int i = 0; i < sheetFiled.getColTitle().size(); i++) {
                    int index = 0;
                    if (i == index) {
                        rppValidation.setName(this.bulkExcel.getCellDetail(currentRow, i));
                    } else if (i == ++index) {
                        rppValidation.setDescription(this.bulkExcel.getCellDetail(currentRow, i));
                    }
                }
                rppValidation.isValidBatch();
                Optional<Permission> isAlreadyExistPermission = this.permissionRepository.findPermissionByPermissionName(rppValidation.getName());
                if (isAlreadyExistPermission.isPresent()) {
                    rppValidation.setErrorMsg(String.format(MessageUtil.PERMISSION_TYPE_ALREADY_USE_AT_ROW, rppValidation.getName(), rppValidation.getRowCounter()));
                }
                if (!BarcoUtil.isNull(rppValidation.getErrorMsg())) {
                    errors.add(rppValidation.getErrorMsg());
                    continue;
                }
                rppValidationsList.add(rppValidation);
            }
        }
        if (!errors.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.TOTAL_INVALID, errors.size()), errors);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(lookupDataRequest.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        rppValidationsList.forEach(rppValidation -> {
            Permission permission = new Permission();
            permission.setPermissionName(rppValidation.getName());
            permission.setDescription(rppValidation.getDescription());
            permission.setCreatedBy(appUser.get());
            permission.setUpdatedBy(appUser.get());
            permission.setStatus(APPLICATION_STATUS.ACTIVE);
            this.permissionRepository.save(permission);
        });
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_SAVED, ""));
    }

    /**
     * Method use to upload permission
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchLinkProfilePermission(LinkPPRequest payload) throws Exception {
        logger.info("Request fetchLinkProfilePermission :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        }
        // link with permission user
        CrossTabResponse crossTabResponse = new CrossTabResponse();
        // profiles
        List<ProfileResponse> profileResponses = new ArrayList<>();
        this.profileRepository.findAll().forEach(profile -> {
            profileResponses.add(getProfileResponse(profile));
        });
        crossTabResponse.setRow(profileResponses);
        // permission
        List<PermissionResponse> permissionResponses = new ArrayList<>();
        this.permissionRepository.findAll().forEach(permission -> {
            permissionResponses.add(getPermissionResponse(permission));
        });
        crossTabResponse.setCol(permissionResponses);
        // existing cross
        QueryResponse queryResponse = this.queryService.executeQueryResponse(QueryService.FETCH_PROFILE_PERMISSION);
        if (!BarcoUtil.isNull(queryResponse.getData())) {
            Hashtable<String, Object> profilePermissionCrossTabs = new Hashtable<>();
            for (HashMap<String, Object> data : (List<HashMap<String, Object>>) queryResponse.getData()) {
                if (data.containsKey(QueryService.LINK_PP) && data.containsKey(QueryService.STATUS)) {
                    GLookup status = APPLICATION_STATUS.getStatusByLookupCode(Long.valueOf(data.get(QueryService.STATUS).toString()));
                    profilePermissionCrossTabs.put(String.valueOf(data.get(QueryService.LINK_PP)), new KeyValue<>(true, status));
                }
            }
            // iterate over profile and permission make combination for all => profile.1=>permission.*
            for (ProfileResponse profileResponse: (List<ProfileResponse>)crossTabResponse.getRow()) {
                for (PermissionResponse permissionResponse: (List<PermissionResponse>)crossTabResponse.getCol()) {
                    String key = profileResponse.getUuid()+"||"+permissionResponse.getUuid();
                    // checking the link cross table and adding those value into hash-table
                    if (!profilePermissionCrossTabs.containsKey(key)) {
                        GLookup status;
                        // if any of status in active its in-active
                        if (profileResponse.getStatus().getLookupType().equals(APPLICATION_STATUS.INACTIVE.getLookupType()) ||
                            permissionResponse.getStatus().getLookupType().equals(APPLICATION_STATUS.INACTIVE.getLookupType())) {
                            status = APPLICATION_STATUS.getStatusByLookupCode(APPLICATION_STATUS.INACTIVE.getLookupCode());
                        } else {
                            status = APPLICATION_STATUS.getStatusByLookupCode(APPLICATION_STATUS.ACTIVE.getLookupCode());
                        }
                        profilePermissionCrossTabs.put(key, new KeyValue<>(false, status));
                    }
                }
            }
            crossTabResponse.setCrossTab(profilePermissionCrossTabs);
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, crossTabResponse);
    }

    /**
     * Method use to upload permission
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse updateLinkProfilePermission(LinkPPRequest payload) throws Exception {
        logger.info("Request updateLinkProfilePermission :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getProfileId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getPermissionId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PERMISSION_ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getLinked())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.LINKED_MISSING);
        }
        // add operation de-link
        if (payload.getLinked()) {
            Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
            ProfilePermission profilePermission = new ProfilePermission();
            profilePermission.setCreatedBy(appUser.get());
            profilePermission.setUpdatedBy(appUser.get());
            profilePermission.setProfile(this.profileRepository.findById(payload.getProfileId())
                .orElseThrow(() -> new NullPointerException(String.format(MessageUtil.PROFILE_NOT_FOUND_WITH_ID, payload.getProfileId()))));
            profilePermission.setPermission(this.permissionRepository.findById(payload.getPermissionId())
                .orElseThrow(() -> new NullPointerException(String.format(MessageUtil.PERMISSION_NOT_FOUND_WITH_ID, payload.getPermissionId()))));
            profilePermission.setStatus(APPLICATION_STATUS.ACTIVE);
            if (profilePermission.getProfile().getStatus().equals(APPLICATION_STATUS.INACTIVE) ||
                profilePermission.getPermission().getStatus().equals(APPLICATION_STATUS.INACTIVE)) {
                profilePermission.setStatus(APPLICATION_STATUS.INACTIVE);
            }
            this.profilePermissionRepository.save(profilePermission);
        } else {
            // delete operation de-link
            this.queryService.deleteQuery(String.format(QueryService.DELETE_PROFILE_PERMISSION_BY_PROFILE_ID_AND_PERMISSION_ID, payload.getProfileId(), payload.getPermissionId()));
        }
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, ""), payload);
    }

    /**
     * Method use to fetch role with user
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchLinkRoleWithUser(LinkRURequest payload) throws Exception {
        logger.info("Request fetchLinkRoleWithUser :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getRoleId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_ID_MISSING);
        }
        Optional<Role> role = this.roleRepository.findById(payload.getRoleId());
        if (role.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.ROLE_NOT_FOUND_WITH_ID, payload.getRoleId()), payload);
        }
        QueryResponse queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.FETCH_LINK_ROLE_WITH_USER_SUPER_ADMIN_PROFILE_AND_USER_ADMIN_PROFILE,
            role.get().getId(), APPLICATION_STATUS.DELETE.getLookupCode(), payload.getStartDate().concat(BarcoUtil.START_DATE), payload.getEndDate().concat(BarcoUtil.END_DATE)));
        List<LinkRPUResponse> linkRPUResponses = new ArrayList<>();
        if (!BarcoUtil.isNull(queryResponse.getData())) {
            for (HashMap<String, Object> data : (List<HashMap<String, Object>>) queryResponse.getData()) {
                linkRPUResponses.add(getLinkRPUResponse(data, role.get().getStatus()));
            }
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, linkRPUResponses);
    }

    /**
     * Method use to link role with user
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse linkRoleWithUser(LinkRURequest payload) throws Exception {
        logger.info("Request linkRoleWithUser :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getRoleId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.ROLE_ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getAppUserId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APP_USER_ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getLinked())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.LINKED_MISSING);
        }
        Optional<Role> role = this.roleRepository.findById(payload.getRoleId());
        if (role.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.ROLE_NOT_FOUND_WITH_ID, payload.getRoleId()), payload);
        }
        Optional<AppUser> appUser = this.appUserRepository.findById(payload.getAppUserId());
        if (appUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.APPUSER_NOT_FOUND, payload.getAppUserId()), payload);
        }
        Optional<AppUser> superAdmin = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        // add operation de-link
        if (payload.getLinked()) {
            this.appUserRoleAccessRepository.save(this.getAppUserRoleAccess(superAdmin.get(), role.get(), appUser.get()));
        } else {
            // delete operation de-link
            this.queryService.deleteQuery(String.format(QueryService.DELETE_APP_USER_ROLE_ACCESS_BY_ROLE_ID_AND_APP_USER_ID, role.get().getId(), appUser.get().getId()));
        }
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, ""), payload);
    }

    /**
     * Method use to fetch role with user
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchLinkProfileWithUser(LinkPURequest payload) throws Exception {
        logger.info("Request fetchLinkProfileWithUser :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getProfileId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ID_MISSING);
        }
        Optional<Profile> profile = this.profileRepository.findById(payload.getProfileId());
        if (profile.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.PROFILE_NOT_FOUND_WITH_ID, payload.getProfileId()), payload);
        }
        QueryResponse queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.FETCH_LINK_PROFILE_WITH_USER_SUPER_ADMIN_PROFILE_AND_USER_ADMIN_PROFILE,
            profile.get().getId(), APPLICATION_STATUS.DELETE.getLookupCode(), payload.getStartDate().concat(BarcoUtil.START_DATE), payload.getEndDate().concat(BarcoUtil.END_DATE)));
        List<LinkRPUResponse> linkRPUResponses = new ArrayList<>();
        if (!BarcoUtil.isNull(queryResponse.getData())) {
            for (HashMap<String, Object> data : (List<HashMap<String, Object>>) queryResponse.getData()) {
                linkRPUResponses.add(this.getLinkRPUResponse(data, profile.get().getStatus()));
            }
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, linkRPUResponses);
    }

    /**
     * Method use to fetch profile with user
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse linkProfileWithUser(LinkPURequest payload) throws Exception {
        logger.info("Request linkProfileWithUser :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        } else if (BarcoUtil.isNull(payload.getProfileId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.PROFILE_ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getAppUserId())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APP_USER_ID_MISSING);
        } else if (BarcoUtil.isNull(payload.getLinked())) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.LINKED_MISSING);
        }
        Optional<Profile> profile = this.profileRepository.findById(payload.getProfileId());
        if (profile.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.PROFILE_NOT_FOUND_WITH_ID, payload.getProfileId()), payload);
        }
        Optional<AppUser> appUser = this.appUserRepository.findById(payload.getAppUserId());
        if (appUser.isEmpty()) {
            return new AppResponse(BarcoUtil.ERROR, String.format(MessageUtil.APPUSER_NOT_FOUND, payload.getAppUserId()), payload);
        }
        Optional<AppUser> superAdmin = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        if (payload.getLinked()) {
            // add operation de-link
            this.appUserProfileAccessRepository.save(getAppUserProfileAccess(superAdmin.get(), profile.get(), appUser.get()));
        } else {
            // delete operation de-link
            this.queryService.deleteQuery(String.format(QueryService.DELETE_APP_USER_PROFILE_ACCESS_BY_ROLE_ID_AND_APP_USER_ID, profile.get().getId(), appUser.get().getId()));
        }
        return new AppResponse(BarcoUtil.SUCCESS, String.format(MessageUtil.DATA_UPDATE, ""), payload);
    }

    /**
     * Method use to fetch profile with user
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchProfileWithUser(ProfileRequest payload) throws Exception {
        logger.info("Request fetchProfileWithUser :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        QueryResponse queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.FETCH_PROFILE_WITH_USER, appUser.get().getId(),
            APPLICATION_STATUS.ACTIVE.getLookupCode(), APPLICATION_STATUS.ACTIVE.getLookupCode()));
        List<ProfileResponse> profileResponses = new ArrayList<>();
        if (!BarcoUtil.isNull(queryResponse.getData())) {
            for (HashMap<String, Object> data : (List<HashMap<String, Object>>) queryResponse.getData()) {
                profileResponses.add(new ProfileResponse(Long.valueOf(data.get(QueryService.ID).toString()),
                data.get(QueryService.PROFILE_NAME).toString(), data.get(QueryService.DESCRIPTION).toString()));
            }
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, profileResponses);
    }

    /**
     * Method use to fetch role with user
     * @param payload
     * @return AppResponse
     * @throws Exception
     * */
    @Override
    public AppResponse fetchRoleWithUser(RoleRequest payload) throws Exception {
        logger.info("Request fetchRoleWithUser :- {}.", payload);
        AppResponse validationResponse = this.validateUsername(payload);
        if (!BarcoUtil.isNull(validationResponse)) {
            throw new Exception(MessageUtil.USERNAME_MISSING);
        }
        Optional<AppUser> appUser = this.appUserRepository.findByUsernameAndStatus(payload.getSessionUser().getUsername(), APPLICATION_STATUS.ACTIVE);
        QueryResponse queryResponse = this.queryService.executeQueryResponse(String.format(QueryService.FETCH_ROLE_WITH_USER, appUser.get().getId(),
            APPLICATION_STATUS.ACTIVE.getLookupCode(), APPLICATION_STATUS.ACTIVE.getLookupCode()));
        List<RoleResponse> roleResponses = new ArrayList<>();
        if (!BarcoUtil.isNull(queryResponse.getData())) {
            for (HashMap<String, Object> data : (List<HashMap<String, Object>>) queryResponse.getData()) {
                roleResponses.add(new RoleResponse(Long.valueOf(data.get(QueryService.ID).toString()),
                 data.get(QueryService.ROLE_NAME).toString(), data.get(QueryService.DESCRIPTION).toString()));
            }
        }
        return new AppResponse(BarcoUtil.SUCCESS, MessageUtil.DATA_FETCH_SUCCESSFULLY, roleResponses);
    }

    /**
     * Method use to convert the profile to profile response
     * @param profile
     * @return ProfileResponse
     * */
    private ProfileResponse getProfileResponse(Profile profile) {
        ProfileResponse profileResponse = new ProfileResponse();
        profileResponse.setUuid(profile.getUuid());
        profileResponse.setProfileName(profile.getProfileName());
        profileResponse.setDescription(profile.getDescription());
        profileResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(profile.getStatus().getLookupType()));
        return profileResponse;
    }

    /**
     * Method use to convert the permission to permission response
     * @param permission
     * @return ProfileResponse
     * */
    private PermissionResponse getPermissionResponse(Permission permission) {
        PermissionResponse permissionResponse = new PermissionResponse();
        permissionResponse.setUuid(permission.getUuid());
        permissionResponse.setPermissionName(permission.getPermissionName());
        permissionResponse.setDescription(permission.getDescription());
        permissionResponse.setStatus(APPLICATION_STATUS.getStatusByLookupType(permission.getStatus().getLookupType()));
        return permissionResponse;
    }

    /**
     * Method used to validate the username.
     * @param payload
     * @return AppResponse
     */
    private AppResponse validateUsername(Object payload) {
        SessionUser sessionUser = null;
        // Check if the payload is an instance of RoleRequest or other types
        if (payload instanceof RoleRequest) {
            RoleRequest roleRequest = (RoleRequest) payload;
            sessionUser = roleRequest.getSessionUser();
        } else if (payload instanceof ProfileRequest) {
            ProfileRequest profileRequest = (ProfileRequest) payload;
            sessionUser = profileRequest.getSessionUser();
        } else if (payload instanceof PermissionRequest) {
            PermissionRequest permissionRequest = (PermissionRequest) payload;
            sessionUser = permissionRequest.getSessionUser();
        } else if (payload instanceof LinkPPRequest) {
            LinkPPRequest linkPPRequest = (LinkPPRequest) payload;
            sessionUser = linkPPRequest.getSessionUser();
        } else if (payload instanceof LinkRURequest) {
            LinkRURequest linkRURequest = (LinkRURequest) payload;
            sessionUser = linkRURequest.getSessionUser();
        } else if (payload instanceof LinkPURequest) {
            LinkPURequest linkPURequest = (LinkPURequest) payload;
            sessionUser = linkPURequest.getSessionUser();
        } else if (payload instanceof LookupDataRequest) {
            LookupDataRequest lookupDataRequest = (LookupDataRequest) payload;
            sessionUser = lookupDataRequest.getSessionUser();
        } else {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.INVALID_PAYLOAD_TYPE);
        }
        // Ensure sessionUser is not null
        if (BarcoUtil.isNull(sessionUser)) {
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.SESSION_USER_MISSING);
        } else if (BarcoUtil.isNull(sessionUser.getUsername())) {
            // Check if the username is null or empty
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.USERNAME_MISSING);
        } else if (this.appUserRepository.findByUsernameAndStatus(sessionUser.getUsername(), APPLICATION_STATUS.ACTIVE).isEmpty()) {
            // Check if the username exists and has an active status
            return new AppResponse(BarcoUtil.ERROR, MessageUtil.APPUSER_NOT_FOUND);
        }
        // Username is valid
        return (AppResponse) BarcoUtil.NULL;
    }

}
