package com.barco.auth.service.impl;

import com.barco.auth.service.LookupDataCacheService;
import com.barco.common.utility.BarcoUtil;
import com.barco.common.utility.excel.BulkExcel;
import com.barco.common.utility.excel.SheetFiled;
import com.barco.model.dto.response.LookupDataResponse;
import com.barco.model.pojo.LookupData;
import com.barco.model.repository.AppUserRepository;
import com.barco.model.repository.LookupDataRepository;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Nabeel Ahmed
 */
@Service
public class LookupDataCacheServiceImpl implements LookupDataCacheService {

    private Logger logger = LoggerFactory.getLogger(LookupDataCacheServiceImpl.class);

    @Value("${storage.efsFileDire}")
    private String tempStoreDirectory;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private Map<String, LookupDataResponse> lookupCacheMap = new HashMap<>();
    private Map<String, SheetFiled> sheetFiledMap = new HashMap<>();

    @Autowired
    private BulkExcel bulkExcel;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private LookupDataRepository lookupDataRepository;

    public LookupDataCacheServiceImpl() {}

    /**
     * Method use to cache the data
     * */
    @PostConstruct
    public void initialize() {
        this.writeLock.lock();
        try {
            logger.info("****************Cache-Lookup-Start***************************");
            this.lookupCacheMap = new HashMap<>();
            Iterable<LookupData> lookupDataList = this.lookupDataRepository.findByParentLookupIsNull();
            lookupDataList.forEach(lookupData -> {
                if (this.lookupCacheMap.containsKey(lookupData.getLookupType())) {
                    this.lookupCacheMap.put(lookupData.getLookupType(), this.getLookupDataDetail(lookupData));
                } else {
                    this.lookupCacheMap.put(lookupData.getLookupType(), this.getLookupDataDetail(lookupData));
                }
            });
            logger.info("***************Cache-Lookup-End********************************");
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Method use to cache the data
     * */
    @PostConstruct
    private void initSheetData() throws IOException {
        logger.info("****************Sheet-Start***************************");
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream inputStream = cl.getResourceAsStream(this.bulkExcel.SHEET_COL);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8);
        String result = CharStreams.toString(inputStreamReader);
        Type type = new TypeToken<Map<String, SheetFiled>>(){}.getType();
        this.sheetFiledMap = new Gson().fromJson(result, type);
        logger.info("Sheet Map {}.", this.sheetFiledMap.size());
        logger.info("****************Sheet-End***************************");
    }

    /**
     * Method use to get the parent lookup by parent lookup type
     * @param parentLookupType
     * @return LookupDataResponse
     * */
    @Override
    public LookupDataResponse getParentLookupDataByParentLookupType(String parentLookupType) {
        return this.lookupCacheMap.get(parentLookupType);
    }

    /**
     * Method use to get the child lookup by parent lookup type and child lookup code
     * @param parentLookupType
     * @param childLookupCode
     * @return LookupDataResponse
     * */
    @Override
    public LookupDataResponse getChildLookupDataByParentLookupTypeAndChildLookupCode(String parentLookupType, Long childLookupCode) {
        return this.getParentLookupDataByParentLookupType(parentLookupType).getLookupChildren().stream()
            .filter(childLookup -> childLookupCode.equals(childLookup.getLookupCode())).findAny()
            .orElse((LookupDataResponse) BarcoUtil.NULL);
    }


    public Map<String, LookupDataResponse> getLookupCacheMap() {
        return lookupCacheMap;
    }

    public Map<String, SheetFiled> getSheetFiledMap() {
        return sheetFiledMap;
    }

}
