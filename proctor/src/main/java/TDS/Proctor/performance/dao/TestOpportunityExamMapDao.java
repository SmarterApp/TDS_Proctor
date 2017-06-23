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

package TDS.Proctor.performance.dao;

import java.util.UUID;

/**
 * Interacts with the table that stores mapping from test opportunity ID to exam ID
 * This will eventually be removed as it won't be needed.
 */
public interface TestOpportunityExamMapDao {

    /**
     * Maps from the examID used in Proctor to the legacy test opportunity ID when running in dual mode
     * @param examId examID form the exam.exam table
     * @return the test opportunity ID mapped that corresponds to the examID
     */
    UUID getTestOpportunityId(UUID examId);

    /**
     * Maps from the legacy test opportunity id used in Proctor to the exam ID when running in dual mode
     * @param testOpportunityId testOpportunity key from the session.testopportunity table
     * @return the examId mapped that corresponds to the test opportunity ID
     */
    UUID getExamId(UUID testOpportunityId);
}
