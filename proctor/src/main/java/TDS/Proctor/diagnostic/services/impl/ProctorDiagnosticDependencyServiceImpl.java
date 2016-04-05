/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 * <p/>
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 * <p/>
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/

package TDS.Proctor.diagnostic.services.impl;

import org.opentestsystem.shared.progman.client.ProgManClient;
import org.opentestsystem.shared.progman.client.domain.TenantType;
import org.opentestsystem.shared.security.domain.permission.UserRole;
import org.opentestsystem.shared.security.integration.PermissionClient;
import org.opentestsystem.shared.trapi.ITrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tds.dll.common.diagnostic.domain.Level;
import tds.dll.common.diagnostic.domain.Providers;
import tds.dll.common.diagnostic.domain.Rating;
import tds.dll.common.diagnostic.domain.Status;
import tds.dll.common.diagnostic.services.DiagnosticDependencyService;
import tds.dll.common.diagnostic.services.impl.AbstractDiagnosticDependencyService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class ProctorDiagnosticDependencyServiceImpl extends AbstractDiagnosticDependencyService {

    private static final Logger logger = LoggerFactory.getLogger(ProctorDiagnosticDependencyServiceImpl.class);

    @Autowired
    private ProgManClient progManClient;

    @Autowired
    private PermissionClient permissionClient;

    @Autowired
    private ITrClient _trClient;

    @Value("${proctor.security.idp}")
    private String ssoPingUrl;


    public Providers getProviders() {

        List<Status> statusList = new ArrayList<>();

        statusList.add(getArt());
        statusList.add(getPermissions());
        statusList.add(getProgman());
        statusList.add(getSSO());

        return new Providers(statusList);
    }

    protected Status getArt() {

        final String unit = "ART";
        try {
            String clients = _trClient.getForObject("clients");
            logger.debug("Client from ART {}", clients);
            return new Status(unit, Level.LEVEL_0, new Date());

        } catch (Exception e) {
            logger.error("Diagnostic error with dependency ART ", e);
            Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.FAILED);
            errorStatus.setError("Diagnostic error with dependency ART");
            return errorStatus;
        }
    }

    protected Status getPermissions() {
        final String unit = "Permission";
        try {
            List<UserRole> roles = permissionClient.getRoles();
            logger.debug("Roles from Permission {}", roles);

            if (roles.size() == 0) {
                Status warningStatus = new Status(unit, Level.LEVEL_0, new Date());
                warningStatus.setRating(Rating.WARNING);
                warningStatus.setWarning("There are no roles configured in Permission for Proctor");
                return warningStatus;
            }

            return new Status(unit, Level.LEVEL_0, new Date());

        } catch (Exception e) {
            logger.error("Diagnostic error with dependency Permission ", e);
            Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.FAILED);
            errorStatus.setError("Diagnostic error with dependency Permission");
            return errorStatus;
        }
    }

    protected Status getProgman() {
        final String unit = "Progman";
        try {
            String progmanClassName = progManClient.getClass().getSimpleName();

            if (progmanClassName.equalsIgnoreCase("ProgramManagementNullClient")) {
                logger.info("Using null client {} ", progmanClassName);
            }

            List<TenantType> tenantTypes = progManClient.getTenantTypes();
            logger.debug("Tenant Types from progman: {}", tenantTypes);

            return new Status(unit, Level.LEVEL_0, new Date());

        } catch (Exception e) {
            logger.error("Diagnostic error with dependency Progman ", e);
            Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.FAILED);
            errorStatus.setError("Diagnostic error with dependency Progman");
            return errorStatus;
        }

    }

    protected Status getSSO() {
        final String unit = "SSO";
        if (!pingURL(ssoPingUrl, 2000)) {
            Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.FAILED);
            errorStatus.setError("Diagnostic error with dependency SSO.  Could not successfully ping URL");
            return errorStatus;
        }

        return new Status(unit, Level.LEVEL_0, new Date());
    }



}
