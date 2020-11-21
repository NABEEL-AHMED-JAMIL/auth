package com.barco.auth.service.Impl;

import com.barco.auth.repository.AccessServiceRepository;
import com.barco.auth.service.AccessServiceService;
import com.barco.common.utility.ApplicationConstants;
import com.barco.model.dto.AccessServiceDto;
import com.barco.model.dto.ResponseDTO;
import com.barco.model.enums.ApiCode;
import com.barco.model.enums.Status;
import com.barco.model.pojo.AccessService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nabeel Ahmed
 */
@Service
@Transactional
@Scope("prototype")
public class AccessServiceServiceImpl implements AccessServiceService {

    public Logger logger = LogManager.getLogger(AccessServiceServiceImpl.class);

    @Autowired
    private AccessServiceRepository accessServiceRepository;

    @Override
    public ResponseDTO createAccessService(AccessServiceDto accessServiceDto) {
        AccessService accessService = new AccessService();
        accessService.setServiceName(accessServiceDto.getServiceName());
        accessService.setServiceName(accessServiceDto.getInternalServiceName());
        accessService.setStatus(Status.Active);
        accessService.setCreatedBy(01L);
        this.accessServiceRepository.saveAndFlush(accessService);
        accessServiceDto.setId(accessService.getId());
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, accessServiceDto);
    }

    @Override
    public ResponseDTO getAllAccessService() {
        List<AccessService> accessServices = this.accessServiceRepository.findAllByStatus(Status.Active);
        List<AccessServiceDto> accessServiceList = accessServices.stream().map(accessService -> {
            AccessServiceDto accessServiceDto = new AccessServiceDto();
            if (accessService.getId() != null) { accessServiceDto.setId(accessService.getId()); }
            if (accessService.getServiceName() != null) { accessServiceDto.setServiceName(accessService.getServiceName()); }
            if (accessService.getInternalServiceName() != null) { accessServiceDto.setInternalServiceName(accessService.getInternalServiceName()); }
            return accessServiceDto;
        }).collect(Collectors.toList());
        return new ResponseDTO(ApiCode.SUCCESS, ApplicationConstants.SUCCESS_MSG, accessServiceList);
    }
}
