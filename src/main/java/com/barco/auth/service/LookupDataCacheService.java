package com.barco.auth.service;

import com.barco.common.utility.excel.SheetFiled;
import com.barco.model.dto.response.LookupDataResponse;
import java.util.Map;

/**
 * @author Nabeel Ahmed
 */
public interface LookupDataCacheService extends RootService {

    public LookupDataResponse getParentLookupDataByParentLookupType(String parentLookupType);

    public LookupDataResponse getChildLookupDataByParentLookupTypeAndChildLookupCode(String parentLookupType, Long lookupCode);

    public Map<String, SheetFiled> getSheetFiledMap();

}
