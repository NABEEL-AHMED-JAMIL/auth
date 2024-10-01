package com.barco.auth.service.impl;


import com.barco.model.dto.response.QueryResponse;
import com.google.gson.Gson;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

/**
 * @author Nabeel Ahmed
 */
@Service
@Transactional
public class QueryService {

    private Logger logger = LoggerFactory.getLogger(QueryService.class);

    @PersistenceContext
    private EntityManager _em;

    // Filed
    public static String ID = "id";
    public static String STATUS = "status";
    public static String PROFILE_NAME = "profile_name";
    public static String ROLE_NAME = "role_name";
    public static String DESCRIPTION = "description";
    public static String LINK_PP = "link_pp";
    public static String EMAIL = "email";
    public static String USERNAME = "username";
    public static String FULL_NAME = "full_name";
    public static String PROFILE_IMG = "profile_img";
    public static String LINK_ID = "link_id";
    public static String LINK_DATA = "link_data";
    public static String LINKED = "linked";
    public static String LINK_STATUS = "link_status";
    public static String PROFILE_ID = "profile_id";
    public static String ENV_VALUE = "env_value";
    public static String ACCESS_TOKEN = "access_token";
    public static String EXPIRE_TIME = "expire_time";
    public static String TOKEN_ID = "token_id";

    public static String FETCH_PROFILE_PERMISSION = "SELECT PRO.UUID || '||' || PM.UUID AS LINK_PP, PP.STATUS AS STATUS FROM PROFILE_PERMISSION PP " +
        "INNER JOIN PROFILE PRO ON PRO.ID = PP.PROFILE_ID " +
        "INNER JOIN PERMISSION PM ON PM.ID = PP.PERMISSION_ID";
    public static String DELETE_PROFILE_PERMISSION_BY_PROFILE_ID_AND_PERMISSION_ID = "DELETE FROM PROFILE_PERMISSION pp WHERE pp.PROFILE_ID = %d AND pp.PERMISSION_ID = %d ";
    public static String DELETE_APP_USER_ROLE_ACCESS_BY_ROLE_ID_AND_APP_USER_ID = "DELETE FROM APP_USER_ROLE_ACCESS aura WHERE aura.ROLE_ID = %d AND aura.APP_USER_ID = %d ";
    public static String DELETE_APP_USER_EVENT_BRIDGE_BY_EVENT_BRIDGE_ID_AND_APP_USER_ID = "DELETE FROM APP_USER_EVENT_BRIDGE AUEB WHERE EVENT_BRIDGE_ID = %d AND APP_USER_ID = %d  ";
    public static String DELETE_APP_USER_PROFILE_ACCESS_BY_ROLE_ID_AND_APP_USER_ID = "DELETE FROM APP_USER_PROFILE_ACCESS aupa WHERE aupa.PROFILE_ID = %d AND aupa.APP_USER_ID = %d ";
    public static String FETCH_LINK_ROLE_WITH_USER_SUPER_ADMIN_PROFILE_AND_USER_ADMIN_PROFILE = "SELECT DISTINCT AU.ID, AU.EMAIL, AU.USERNAME, AU.FIRST_NAME || ' ' || AU.LAST_NAME AS FULL_NAME, " +
        "AU.IMG AS PROFIlE_IMG, PRO.ID AS PROFILE_ID, PRO.PROFILE_NAME, PRO.DESCRIPTION, AURA.DATE_CREATED AS LINK_DATA, " +
        "CASE WHEN AURA.DATE_CREATED IS NULL THEN FALSE ELSE TRUE END LINKED, " +
        "CASE WHEN AURA.STATUS IS NOT NULL THEN AURA.STATUS WHEN AU.STATUS = 0 OR RL.STATUS = 0 THEN 0 ELSE 1 END AS LINK_STATUS " +
        "FROM APP_USER AU " +
        "INNER JOIN PROFILE PRO ON PRO.ID = AU.PROFILE_ID " +
        "LEFT JOIN APP_USER_ROLE_ACCESS AURA ON AURA.APP_USER_ID = AU.ID AND AURA.ROLE_ID = %d " +
        "LEFT JOIN ROLE RL ON RL.ID = AURA.ROLE_ID " +
        "WHERE AU.STATUS != %d AND AU.DATE_CREATED between '%s' and '%s' AND PRO.PROFILE_NAME IN ('ADMIN_PROFILE', 'SUPER_ADMIN_PROFILE') " + // super admin and admin user fetch only
        "ORDER BY AU.ID DESC";
    public static String FETCH_LINK_PROFILE_WITH_USER_SUPER_ADMIN_PROFILE_AND_USER_ADMIN_PROFILE = "SELECT DISTINCT AU.ID, AU.EMAIL, AU.USERNAME, AU.FIRST_NAME || ' ' || AU.LAST_NAME AS FULL_NAME, " +
        "AU.IMG AS PROFIlE_IMG, PRO.ID AS PROFILE_ID, PRO.PROFILE_NAME, PRO.DESCRIPTION, AUPA.DATE_CREATED AS LINK_DATA, " +
        "CASE WHEN AUPA.DATE_CREATED IS NULL THEN FALSE ELSE TRUE END LINKED, " +
        "CASE WHEN AUPA.STATUS IS NOT NULL THEN AUPA.STATUS WHEN AU.STATUS = 0 OR PRO.STATUS = 0 THEN 0 ELSE 1 END AS LINK_STATUS " +
        "FROM APP_USER AU " +
        "INNER JOIN PROFILE PRO ON PRO.ID = AU.PROFILE_ID " +
        "LEFT JOIN APP_USER_PROFILE_ACCESS AUPA ON AUPA.APP_USER_ID = AU.ID AND AUPA.PROFILE_ID = %d " +
        "WHERE AU.STATUS != %d AND AU.DATE_CREATED between '%s' and '%s' AND PRO.PROFILE_NAME IN ('ADMIN_PROFILE', 'SUPER_ADMIN_PROFILE') " + // super admin and admin user fetch only
        "ORDER BY AU.ID DESC";
    public static String FETCH_ROLE_WITH_USER = "SELECT DISTINCT ROLE.ID, ROLE.NAME AS ROLE_NAME, ROLE.DESCRIPTION AS DESCRIPTION " +
        "FROM ROLE " +
        "INNER JOIN APP_USER_ROLE_ACCESS AURA ON AURA.ROLE_ID = ROLE.ID AND AURA.APP_USER_ID = %d AND AURA.STATUS = %d AND ROLE.STATUS = %d ";
    public static String FETCH_PROFILE_WITH_USER = "SELECT DISTINCT PRO.ID, PRO.PROFILE_NAME, PRO.DESCRIPTION AS DESCRIPTION " +
        "FROM PROFILE PRO " +
        "INNER JOIN APP_USER_PROFILE_ACCESS AUPA ON AUPA.PROFILE_ID = PRO.ID AND AUPA.APP_USER_ID = %d AND AUPA.STATUS = %d AND PRO.STATUS = %d ";
    public static String TOKEN_SESSION_STATISTICS = "SELECT * FROM ( " +
        "SELECT '1' AS ORDER, 'DAILY' AS NAME, COUNT(1) AS TOTALCOUNT, COALESCE(SUM(CASE WHEN RT.STATUS = 1 THEN 1 ELSE 0 END), 0) AS ACTIVECOUNT, COALESCE(SUM(CASE WHEN RT.STATUS = 2 THEN 1 ELSE 0 END), 0) AS OFFCOUNT " +
        "FROM REFRESH_TOKEN RT " +
        "WHERE CAST(DATE_CREATED AS DATE) = CURRENT_DATE " +
        "UNION " +
        "SELECT '2' AS ORDER, 'WEEK' AS NAME, COUNT(1) AS TOTALCOUNT, COALESCE(SUM(CASE WHEN RT.STATUS = 1 THEN 1 ELSE 0 END), 0) AS ACTIVECOUNT, COALESCE(SUM(CASE WHEN RT.STATUS = 2 THEN 1 ELSE 0 END), 0) AS OFFCOUNT " +
        "FROM REFRESH_TOKEN RT " +
        "WHERE DATE_CREATED >= DATE_TRUNC('WEEK', CURRENT_DATE) AND DATE_CREATED < DATE_TRUNC('WEEK', CURRENT_DATE) + INTERVAL '1 WEEK' " +
        "UNION " +
        "SELECT '3' AS ORDER, 'MONTH' AS NAME, COUNT(1) AS TOTALCOUNT, COALESCE(SUM(CASE WHEN RT.STATUS = 1 THEN 1 ELSE 0 END), 0) AS ACTIVECOUNT, COALESCE(SUM(CASE WHEN RT.STATUS = 2 THEN 1 ELSE 0 END), 0) AS OFFCOUNT " +
        "FROM REFRESH_TOKEN RT " +
        "WHERE DATE_CREATED >= DATE_TRUNC('MONTH', CURRENT_DATE) AND DATE_CREATED < DATE_TRUNC('MONTH', CURRENT_DATE) + INTERVAL '1 MONTH' " +
        "UNION " +
        "SELECT '4' AS ORDER, 'YEAR' AS NAME, COUNT(1) AS TOTALCOUNT, COALESCE(SUM(CASE WHEN RT.STATUS = 1 THEN 1 ELSE 0 END), 0) AS ACTIVECOUNT, COALESCE(SUM(CASE WHEN RT.STATUS = 2 THEN 1 ELSE 0 END), 0) AS OFFCOUNT\n" +
        "FROM REFRESH_TOKEN RT " +
        "WHERE DATE_CREATED >= CURRENT_DATE - INTERVAL '1 YEAR') TOKEN_DATA " +
        "ORDER BY TOKEN_DATA.ORDER ASC;";
    // org setting statistics
    public static String APP_SETTING_STATISTICS = "SELECT CASE WHEN LD.STATUS = 0 THEN 'INACTIVE-LOOKUP' WHEN LD.STATUS = 1 THEN 'ACTIVE-LOOKUP' END AS NAME, COUNT(*) AS VALUE " +
        "FROM LOOKUP_DATA LD " +
        "INNER JOIN APP_USER AU ON LD.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE LD.PARENT_LOOKUP_ID IS NULL AND ORG.ID = %1$d AND LD.STATUS != 2 " +
        "GROUP BY LD.STATUS " +
        "UNION " +
        "SELECT CASE WHEN EV.STATUS = 0 THEN 'INACTIVE-E-VARIABLE' WHEN EV.STATUS = 1 THEN 'ACTIVE-E-VARIABLE' END AS NAME, COUNT(*) AS VALUE " +
        "FROM ENV_VARIABLES EV  " +
        "INNER JOIN APP_USER AU ON EV.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND EV.STATUS != 2 " +
        "GROUP BY EV.STATUS " +
        "UNION " +
        "SELECT CASE WHEN EB.STATUS = 0 THEN 'INACTIVE-EVENT-BRIDGE' WHEN EB.STATUS = 1 THEN 'ACTIVE-EVENT-BRIDGE' END AS NAME, COUNT(*) AS VALUE " +
        "FROM EVENT_BRIDGE EB " +
        "INNER JOIN APP_USER AU ON EB.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND EB.STATUS != 2 " +
        "GROUP BY EB.STATUS " +
        "UNION " +
        "SELECT CASE WHEN TR.STATUS = 0 THEN 'INACTIVE-TEMPLATE' WHEN TR.STATUS = 1 THEN 'ACTIVE-TEMPLATE' END AS NAME, COUNT(*) AS VALUE " +
        "FROM TEMPLATE_REG TR " +
        "INNER JOIN APP_USER AU ON TR.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND TR.STATUS != 2 " +
        "GROUP BY TR.STATUS " +
        "UNION " +
        "SELECT CASE WHEN CRD.STATUS = 0 THEN 'INACTIVE-CREDENTIAL' WHEN CRD.STATUS = 1 THEN 'ACTIVE-CREDENTIAL' END AS NAME, COUNT(*) AS VALUE " +
        "FROM CREDENTIAL CRD " +
        "INNER JOIN APP_USER AU ON CRD.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND CRD.STATUS != 2 " +
        "GROUP BY CRD.STATUS ";
    public static String PROFILE_SETTING_STATISTICS = "SELECT CASE WHEN ROL.STATUS = 0 THEN 'INACTIVE-ROLE' WHEN ROL.STATUS = 1 THEN 'ACTIVE-ROLE' END AS NAME, COUNT(*) AS VALUE " +
        "FROM ROLE ROL  " +
        "INNER JOIN APP_USER AU ON ROL.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND ROL.STATUS != 2 " +
        "GROUP BY ROL.STATUS " +
        "UNION " +
        "SELECT CASE WHEN PRO.STATUS = 0 THEN 'INACTIVE-PROFILE' WHEN PRO.STATUS = 1 THEN 'ACTIVE-PROFILE' END AS NAME, COUNT(*) AS VALUE " +
        "FROM PROFILE PRO " +
        "INNER JOIN APP_USER AU ON PRO.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND PRO.STATUS != 2 " +
        "GROUP BY PRO.STATUS " +
        "UNION " +
        "SELECT CASE WHEN PER.STATUS = 0 THEN 'INACTIVE-PERMISSION' WHEN PER.STATUS = 1 THEN 'ACTIVE-PERMISSION' END AS NAME, COUNT(*) AS VALUE " +
        "FROM PERMISSION PER " +
        "INNER JOIN APP_USER AU ON PER.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND PER.STATUS != 2 " +
        "GROUP BY PER.STATUS " +
        "UNION " +
        "SELECT CASE WHEN AU.STATUS = 0 AND AU.ORG_ACCOUNT = FALSE THEN 'INACTIVE-USER' WHEN AU.STATUS = 1 AND AU.ORG_ACCOUNT = FALSE THEN 'ACTIVE-USER' END AS NAME, COUNT(*) AS VALUE " +
        "FROM APP_USER AU " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND AU.STATUS != 2 " +
        "GROUP BY AU.STATUS, AU.ORG_ACCOUNT";
    public static String FORM_SETTING_STATISTICS = "SELECT CASE WHEN GF.STATUS = 0 THEN 'INACTIVE-FORM' WHEN GF.STATUS = 1 THEN 'ACTIVE-FORM' END AS NAME, COUNT(*) AS VALUE " +
        "FROM GEN_FORM GF " +
        "INNER JOIN APP_USER AU ON GF.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND GF.STATUS != 2 " +
        "GROUP BY GF.STATUS " +
        "UNION " +
        "SELECT CASE WHEN GC.STATUS = 0 THEN 'INACTIVE-CONTROL' WHEN GC.STATUS = 1 THEN 'ACTIVE-CONTROL' END AS NAME, COUNT(*) AS VALUE " +
        "FROM GEN_CONTROL GC " +
        "INNER JOIN APP_USER AU ON GC.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND GC.STATUS != 2 " +
        "GROUP BY GC.STATUS " +
        "UNION " +
        "SELECT CASE WHEN GS.STATUS = 0 THEN 'INACTIVE-SECTION' WHEN GS.STATUS = 1 THEN 'ACTIVE-SECTION' END AS NAME, COUNT(*) AS VALUE " +
        "FROM GEN_SECTION GS " +
        "INNER JOIN APP_USER AU ON GS.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND GS.STATUS != 2 " +
        "GROUP BY GS.STATUS";
    public static String DASHBOARD_AND_REPORT_SETTING_STATISTICS = "SELECT CASE WHEN DS.STATUS = 0 THEN 'INACTIVE-DASHBOARD' WHEN DS.STATUS = 1 THEN 'ACTIVE-DASHBOARD' END AS NAME, COUNT(*) AS VALUE " +
        "FROM DASHBOARD_SETTING DS " +
        "INNER JOIN APP_USER AU ON DS.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND DS.STATUS != 2 " +
        "GROUP BY DS.STATUS " +
        "UNION " +
        "SELECT CASE WHEN RS.STATUS = 0 THEN 'INACTIVE-REPORT' WHEN RS.STATUS = 1 THEN 'ACTIVE-REPORT' END AS NAME, COUNT(*) AS VALUE " +
        "FROM REPORT_SETTING RS " +
        "INNER JOIN APP_USER AU ON RS.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND RS.STATUS != 2 " +
        "GROUP BY RS.STATUS";
    public static String SERVICE_SETTING_STATISTICS = "SELECT CASE WHEN STT.STATUS = 0 THEN 'INACTIVE-STT' WHEN STT.STATUS = 1 THEN 'ACTIVE-STT' END AS NAME, COUNT(*) AS VALUE " +
        "FROM SOURCE_TASK_TYPE STT " +
        "INNER JOIN APP_USER AU ON STT.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d AND STT.STATUS != 2 " +
        "GROUP BY STT.STATUS " +
        "UNION " +
        "SELECT CASE WHEN ST.STATUS = 0 THEN 'INACTIVE-ST' WHEN ST.STATUS = 1 THEN 'ACTIVE-ST' END AS NAME, COUNT(*) AS VALUE " +
        "FROM SOURCE_TASK ST " +
        "INNER JOIN APP_USER AU ON ST.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d " +
        "GROUP BY ST.STATUS";
    public static String SESSION_COUNT_STATISTICS = "SELECT DATE(RT.DATE_CREATED) AS KEY, COUNT(*) AS VALUE " +
        "FROM REFRESH_TOKEN RT " +
        "INNER JOIN APP_USER AU ON RT.CREATED_BY_ID = AU.ID " +
        "INNER JOIN ORGANIZATION ORG ON AU.ORG_ID = ORG.ID " +
        "WHERE ORG.ID = %1$d " +
        "GROUP BY DATE(RT.DATE_CREATED) " +
        "ORDER BY DATE(RT.DATE_CREATED) ASC";

    public QueryService() {}

    /**
     * Method use to perform the delete query
     * @param queryStr
     * @return Object
     * */
    public Object deleteQuery(String queryStr) {
        logger.info("Execute Query :- {}.", queryStr);
        Query query = this._em.createNativeQuery(queryStr);
        int rowsDeleted = query.executeUpdate();
        logger.info("Execute deleted :- {}.", rowsDeleted);
        return rowsDeleted;
    }

    /**
     * Method use to execute query for dynamic result
     * @param queryStr
     * @return QueryResponse
     * */
    public QueryResponse executeQueryResponse(String queryStr) {
        logger.info("Execute Query :- {}.", queryStr);
        Query query = this._em.createNativeQuery(queryStr);
        NativeQueryImpl nativeQuery = (NativeQueryImpl) query;
        nativeQuery.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
        List<Map<String,Object>> result = nativeQuery.getResultList();
        QueryResponse itemResponse = new QueryResponse();
        if (!result.isEmpty()) {
            itemResponse.setQuery(queryStr);
            itemResponse.setData(result);
            itemResponse.setColumn(result.get(0).keySet());
        }
        return itemResponse;
    }

    /**
     * Method use to execute query for dynamic result
     * @param queryStr
     * @return QueryResponse
     * */
    public <T> Page<T> fetchResultWithPagination(String queryStr, Map<String, Object> parameters,
        Pageable pageable, Class<T> type) {
        logger.info("Execute Query :- {}.", queryStr);
        // Bind dynamic parameters
        Query query = this._em.createQuery(queryStr, type);
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        int totalRows = query.getResultList().size();
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<T> resultList = query.getResultList();
        return new PageImpl<>(resultList, pageable, totalRows);
    }

    /**
     * Method use to execute query for dynamic result
     * @param queryStr
     * @return QueryResponse
     * */
    public <T> List<T> getResultList(String queryStr, Map<String, Object> parameters, Class<T> type) {
        logger.info("Execute Query :- {}.", queryStr);
        // Bind dynamic parameters
        Query query = this._em.createQuery(queryStr, type);
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        return query.getResultList();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }


}