/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package TDS.Proctor.Sql.Data.Abstractions;

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;

import tds.accommodation.Accommodation;

/**
 * Repository to interact with Assessment data
 */
public interface AssessmentRepository {
    /**
     * @param clientName    the current envrionment's client name
     * @param assessmentKey the key of the {@link tds.assessment.Assessment}
     * @return the list of all assessment {@link tds.accommodation.Accommodation}
     * @throws ReturnStatusException
     */
    List<Accommodation> findAccommodations(final String clientName, final String assessmentKey) throws ReturnStatusException;
}
