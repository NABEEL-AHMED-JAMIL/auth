package com.barco.auth.controller;

import com.barco.auth.service.RPPService;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.ExceptionUtil;
import com.barco.common.utility.excel.ExcelUtil;
import com.barco.model.dto.request.*;
import com.barco.model.dto.response.AppResponse;
import com.barco.model.util.MessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Api use to perform the rpp
 * @author Nabeel Ahmed
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/rpp.json")
@Api(value = "RPP Rest Api",
    description = "RPP Service : Service related to the [Role&Permission] for profile management. ")
public class RPPRestApi extends RootRestApi {

    private Logger logger = LoggerFactory.getLogger(RPPRestApi.class);

    @Autowired
    private RPPService rppService;

    /**
     * @apiName :- addRole
     * @apiNote :- Api use to create the role
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to add new role in the system.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/addRole", method=RequestMethod.POST)
    public ResponseEntity<?> addRole(@RequestBody RoleRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.addRole(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while addRole ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- updateRole
     * @apiNote :- Api use to update the role
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to update role in the system.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/updateRole", method=RequestMethod.POST)
    public ResponseEntity<?> updateRole(@RequestBody RoleRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.updateRole(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while updateRole ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchAllRole
     * @apiNote :- Api use to find all role
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch all roles.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/fetchAllRole", method=RequestMethod.POST)
    public ResponseEntity<?> fetchAllRole(@RequestBody RoleRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.fetchAllRole(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchAllRole ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- findRoleById
     * @apiNote :- Api use to find the role py id
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to find role by id.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/findRoleById", method=RequestMethod.POST)
    public ResponseEntity<?> findRoleById(@RequestBody RoleRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.findRoleById(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while findRoleById ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteRoleById
     * @apiNote :- Api use to find the role py id
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to delete role by id.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/deleteRoleById", method=RequestMethod.POST)
    public ResponseEntity<?> deleteRoleById(@RequestBody RoleRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.deleteRoleById(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteRoleById ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteAllRole
     * @apiNote :- Api use to find the role py id
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to delete all roles by ids.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/deleteAllRole", method=RequestMethod.POST)
    public ResponseEntity<?> deleteAllRole(@RequestBody RoleRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.deleteAllRole(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteAllRole ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- downloadRoleTemplateFile
     * @apiNote :- Api use to download role template
     * @return ResponseEntity<?> downloadRoleTemplateFile
     * */
    @ApiOperation(value = "Api use to download role template file.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/downloadRoleTemplateFile", method = RequestMethod.GET)
    public ResponseEntity<?> downloadRoleTemplateFile() {
        try {
            HttpHeaders headers = new HttpHeaders();
            DateFormat dateFormat = new SimpleDateFormat(BarcoUtil.SIMPLE_DATE_PATTERN);
            String fileName = "BatchRoleDownload-"+dateFormat.format(new Date())+"-"+ UUID.randomUUID() + ExcelUtil.XLSX_EXTENSION;
            headers.add(BarcoUtil.CONTENT_DISPOSITION,BarcoUtil.FILE_NAME_HEADER + fileName);
            return ResponseEntity.ok().headers(headers).body(this.rppService.downloadRoleTemplateFile().toByteArray());
        } catch (Exception ex) {
            logger.error("An error occurred while downloadRoleTemplateFile xlsx file", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- downloadRole
     * @apiNote :- Api use to download the role data
     * @return ResponseEntity<?> downloadRole
     * */
    @ApiOperation(value = "Api use to download role file.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/downloadRole", method = RequestMethod.POST)
    public ResponseEntity<?> downloadRole(@RequestBody RoleRequest payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            DateFormat dateFormat = new SimpleDateFormat(BarcoUtil.SIMPLE_DATE_PATTERN);
            String fileName = "BatchRoleDownload-"+dateFormat.format(new Date())+"-"+ UUID.randomUUID() + ExcelUtil.XLSX_EXTENSION;
            headers.add(BarcoUtil.CONTENT_DISPOSITION,BarcoUtil.FILE_NAME_HEADER + fileName);
            return ResponseEntity.ok().headers(headers).body(this.rppService.downloadRole(payload).toByteArray());
        } catch (Exception ex) {
            logger.error("An error occurred while downloadRole ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- uploadRole
     * @apiNote :- Api use to upload the role
     * @return ResponseEntity<?> uploadRole
     * */
    @ApiOperation(value = "Api use to upload role file.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/uploadRole", method = RequestMethod.POST)
    public ResponseEntity<?> uploadRole(FileUploadRequest payload) {
        try {
            payload.setData(this.getSessionUser());
            if (!BarcoUtil.isNull(payload.getFile())) {
                return new ResponseEntity<>(this.rppService.uploadRole(payload), HttpStatus.OK);
            }
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, MessageUtil.DATA_NOT_FOUND), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            logger.error("An error occurred while uploadRole ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- addProfile
     * @apiNote :- Api use to add the profile
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to add new profile in system.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/addProfile", method=RequestMethod.POST)
    public ResponseEntity<?> addProfile(@RequestBody ProfileRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.addProfile(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while addProfile ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- updateProfile
     * @apiNote :- Api use to update the profile
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to update profile in system.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/updateProfile", method=RequestMethod.POST)
    public ResponseEntity<?> updateProfile(@RequestBody ProfileRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.updateProfile(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while updateProfile ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchAllProfile
     * @apiNote :- Api use to fetch all profile
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch all profiles.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/fetchAllProfile", method=RequestMethod.POST)
    public ResponseEntity<?> fetchAllProfile(@RequestBody ProfileRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.fetchAllProfile(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchAllProfile ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchProfileById
     * @apiNote :- Api use to fetch profile by id
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch profile by id.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/fetchProfileById",  method=RequestMethod.POST)
    public ResponseEntity<?> fetchProfileById(@RequestBody ProfileRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.fetchProfileById(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchProfileById ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteProfileById
     * @apiNote :- Api use to delete profile by id
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to delete profile by id.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/deleteProfileById",  method=RequestMethod.POST)
    public ResponseEntity<?> deleteProfileById(@RequestBody ProfileRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.deleteProfileById(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteProfileById ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteAllProfile
     * @apiNote :- Api use to delete profile by ids
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to delete all profile by ids.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/deleteAllProfile",  method=RequestMethod.POST)
    public ResponseEntity<?> deleteAllProfile(@RequestBody ProfileRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.deleteAllProfile(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteAllProfile ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- downloadProfileTemplateFile
     * @apiNote :- Api use to download profile template
     * @return ResponseEntity<?> downloadProfileTemplateFile
     * */
    @ApiOperation(value = "Api use to download template file for profile upload.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/downloadProfileTemplateFile", method = RequestMethod.GET)
    public ResponseEntity<?> downloadProfileTemplateFile() {
        try {
            HttpHeaders headers = new HttpHeaders();
            DateFormat dateFormat = new SimpleDateFormat(BarcoUtil.SIMPLE_DATE_PATTERN);
            String fileName = "BatchProfileDownload-"+dateFormat.format(new Date())+"-"+ UUID.randomUUID() + ExcelUtil.XLSX_EXTENSION;
            headers.add(BarcoUtil.CONTENT_DISPOSITION,BarcoUtil.FILE_NAME_HEADER + fileName);
            return ResponseEntity.ok().headers(headers).body(this.rppService.downloadProfileTemplateFile().toByteArray());
        } catch (Exception ex) {
            logger.error("An error occurred while downloadProfileTemplateFile xlsx file", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- downloadProfile
     * @apiNote :- Api use to download the profile
     * @return ResponseEntity<?> downloadProfile
     * */
    @ApiOperation(value = "Api use to download profiles file.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/downloadProfile", method = RequestMethod.POST)
    public ResponseEntity<?> downloadProfile(@RequestBody ProfileRequest payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            DateFormat dateFormat = new SimpleDateFormat(BarcoUtil.SIMPLE_DATE_PATTERN);
            String fileName = "BatchProfileDownload-"+dateFormat.format(new Date())+"-"+ UUID.randomUUID() + ExcelUtil.XLSX_EXTENSION;
            headers.add(BarcoUtil.CONTENT_DISPOSITION,BarcoUtil.FILE_NAME_HEADER + fileName);
            return ResponseEntity.ok().headers(headers).body(this.rppService.downloadProfile(payload).toByteArray());
        } catch (Exception ex) {
            logger.error("An error occurred while downloadProfile ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- uploadProfile
     * @apiNote :- Api use to upload the profile
     * @return ResponseEntity<?> uploadProfile
     * */
    @ApiOperation(value = "Api use to upload profiles file.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/uploadProfile", method = RequestMethod.POST)
    public ResponseEntity<?> uploadProfile(FileUploadRequest payload) {
        try {
            payload.setData(this.getSessionUser());
            if (!BarcoUtil.isNull(payload.getFile())) {
                return new ResponseEntity<>(this.rppService.uploadProfile(payload), HttpStatus.OK);
            }
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, MessageUtil.DATA_NOT_FOUND), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            logger.error("An error occurred while uploadProfile ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- addPermission
     * @apiNote :- Api use to fetch add the permission
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to add new permission in the system.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/addPermission", method=RequestMethod.POST)
    public ResponseEntity<?> addPermission(@RequestBody PermissionRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.addPermission(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while addPermission ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- updatePermission
     * @apiNote :- Api use to fetch update the permission
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to update permission in the system.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/updatePermission", method=RequestMethod.POST)
    public ResponseEntity<?> updatePermission(@RequestBody PermissionRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.updatePermission(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while updatePermission ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchAllPermission
     * @apiNote :- Api use to fetch all the permission
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch all permission.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/fetchAllPermission", method=RequestMethod.POST)
    public ResponseEntity<?> fetchAllPermission(@RequestBody PermissionRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.fetchAllPermission(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchAllPermission ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchPermissionById
     * @apiNote :- Api use to fetch permission by id
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch permission by id.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/fetchPermissionById", method=RequestMethod.POST)
    public ResponseEntity<?> fetchPermissionById(@RequestBody PermissionRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.fetchPermissionById(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchPermissionById ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deletePermissionById
     * @apiNote :- Api use to delete permission by id
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to delete permission by id.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/deletePermissionById", method=RequestMethod.POST)
    public ResponseEntity<?> deletePermissionById(@RequestBody PermissionRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.deletePermissionById(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deletePermissionById ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- deleteAllPermission
     * @apiNote :- Api use to delete permission by ids
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to delete all permission by ids.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/deleteAllPermission", method=RequestMethod.POST)
    public ResponseEntity<?> deleteAllPermission(@RequestBody PermissionRequest payload) {
        try {
            return new ResponseEntity<>(this.rppService.deleteAllPermission(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while deleteAllPermission ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- downloadPermissionTemplateFile
     * @apiNote :- Api use to download permission template
     * @return ResponseEntity<?> downloadPermissionTemplateFile
     * */
    @ApiOperation(value = "Api use to download permission template for permission upload.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/downloadPermissionTemplateFile", method = RequestMethod.GET)
    public ResponseEntity<?> downloadPermissionTemplateFile() {
        try {
            HttpHeaders headers = new HttpHeaders();
            DateFormat dateFormat = new SimpleDateFormat(BarcoUtil.SIMPLE_DATE_PATTERN);
            String fileName = "BatchPermissionDownload-"+dateFormat.format(new Date())+"-"+ UUID.randomUUID() + ExcelUtil.XLSX_EXTENSION;
            headers.add(BarcoUtil.CONTENT_DISPOSITION,BarcoUtil.FILE_NAME_HEADER + fileName);
            return ResponseEntity.ok().headers(headers).body(this.rppService.downloadPermissionTemplateFile().toByteArray());
        } catch (Exception ex) {
            logger.error("An error occurred while downloadPermissionTemplateFile xlsx file", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- downloadPermission
     * @apiNote :- Api use to download the permission
     * @return ResponseEntity<?> downloadPermission
     * */
    @ApiOperation(value = "Api use to download permissions.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/downloadPermission", method = RequestMethod.POST)
    public ResponseEntity<?> downloadPermission(@RequestBody PermissionRequest payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            DateFormat dateFormat = new SimpleDateFormat(BarcoUtil.SIMPLE_DATE_PATTERN);
            String fileName = "BatchPermissionDownload-"+dateFormat.format(new Date())+"-"+ UUID.randomUUID() + ExcelUtil.XLSX_EXTENSION;
            headers.add(BarcoUtil.CONTENT_DISPOSITION,BarcoUtil.FILE_NAME_HEADER + fileName);
            return ResponseEntity.ok().headers(headers).body(this.rppService.downloadPermission(payload).toByteArray());
        } catch (Exception ex) {
            logger.error("An error occurred while downloadPermission ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- uploadPermission
     * @apiNote :- Api use to upload the permission
     * @return ResponseEntity<?> uploadPermission
     * */
    @ApiOperation(value = "Api use to upload new permissions.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/uploadPermission", method = RequestMethod.POST)
    public ResponseEntity<?> uploadPermission(FileUploadRequest payload) {
        try {
            payload.setData(this.getSessionUser());
            if (!BarcoUtil.isNull(payload.getFile())) {
                return new ResponseEntity<>(this.rppService.uploadPermission(payload), HttpStatus.OK);
            }
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, MessageUtil.DATA_NOT_FOUND), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            logger.error("An error occurred while uploadPermission ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchLinkProfilePermission
     * @apiNote :- Api use to fetch link-> profile & permission
     * @return ResponseEntity<?> fetchLinkProfilePermission
     * */
    @ApiOperation(value = "Api use to fetch linked profile with permission.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/fetchLinkProfilePermission", method = RequestMethod.POST)
    public ResponseEntity<?> fetchLinkProfilePermission(@RequestBody LinkPPRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.fetchLinkProfilePermission(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchLinkProfilePermission ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- updateLinkProfilePermission
     * @apiNote :- Api use to update link-> profile & permission
     * @return ResponseEntity<?> updateLinkProfilePermission
     * */
    @ApiOperation(value = "Api use to update linked profile with permission.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(value = "/updateLinkProfilePermission", method = RequestMethod.POST)
    public ResponseEntity<?> updateLinkProfilePermission(@RequestBody LinkPPRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.updateLinkProfilePermission(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while updateLinkProfilePermission ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchLinkRoleWithUser
     * @apiNote :- Api use to fetch the role with user
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch linked role with user.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/fetchLinkRoleWithUser", method=RequestMethod.POST)
    public ResponseEntity<?> fetchLinkRoleWithUser(@RequestBody LinkRURequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.fetchLinkRoleWithUser(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchLinkRoleWithUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- linkRoleWithUser
     * @apiNote :- Api use to link the role with root user
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to linked role with user.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/linkRoleWithUser", method=RequestMethod.POST)
    public ResponseEntity<?> linkRoleWithUser(@RequestBody LinkRURequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.linkRoleWithUser(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while linkRoleWithUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchLinkProfileWithUser
     * @apiNote :- Api use to fetch the profile with root user
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch linked profile with user.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/fetchLinkProfileWithUser", method=RequestMethod.POST)
    public ResponseEntity<?> fetchLinkProfileWithUser(@RequestBody LinkPURequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.fetchLinkProfileWithUser(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchLinkProfileWithUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- linkProfileWithUser
     * @apiNote :- Api use to link the profile with user
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to linked profile with user.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('DB') or hasRole('DEV')")
    @RequestMapping(path="/linkProfileWithUser", method=RequestMethod.POST)
    public ResponseEntity<?> linkProfileWithUser(@RequestBody LinkPURequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.linkProfileWithUser(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while linkProfileWithUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchProfileWithUser
     * @apiNote :- Api use to link the profile with root user
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch profile with user.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(path="/fetchProfileWithUser", method=RequestMethod.POST)
    public ResponseEntity<?> fetchProfileWithUser(@RequestBody ProfileRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.fetchProfileWithUser(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchProfileWithUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ExceptionUtil.getRootCauseMessage(ex)), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @apiName :- fetchRoleWithUser
     * @apiNote :- Api use to link the role with root user
     * @param payload
     * @return ResponseEntity<?>
     * */
    @ApiOperation(value = "Api use to fetch role with user.", response = ResponseEntity.class)
    @PreAuthorize("hasRole('MASTER_ADMIN') or hasRole('ADMIN') or hasRole('DEV')")
    @RequestMapping(path="/fetchRoleWithUser", method=RequestMethod.POST)
    public ResponseEntity<?> fetchRoleWithUser(@RequestBody RoleRequest payload) {
        try {
            payload.setSessionUser(this.getSessionUser());
            return new ResponseEntity<>(this.rppService.fetchRoleWithUser(payload), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("An error occurred while fetchRoleWithUser ", ExceptionUtil.getRootCause(ex));
            return new ResponseEntity<>(new AppResponse(BarcoUtil.ERROR, ExceptionUtil.getRootCauseMessage(ex)), HttpStatus.BAD_REQUEST);
        }
    }

}
