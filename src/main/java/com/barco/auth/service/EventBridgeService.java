package com.barco.auth.service;

import com.barco.model.dto.request.LinkEBURequest;
import com.barco.model.dto.response.AppResponse;

/**
 * @author Nabeel Ahmed
 */
public interface EventBridgeService extends RootService {

    public AppResponse linkEventBridgeWithUser(LinkEBURequest payload) throws Exception;

}
