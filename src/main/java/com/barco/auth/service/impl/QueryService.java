package com.barco.auth.service.impl;


import com.barco.model.dto.response.QueryResponse;
import com.google.gson.Gson;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static String SESSION_STATISTICS = "SELECT * FROM ( " +
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

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }


}