/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Data.Abstractions;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import AIR.Common.Helpers._Ref;
import TDS.Proctor.Sql.Data.TestSession;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

public interface ITestSessionRepository
{

  List<TestSession> getCurrentSessions (String clientname, long proctorKey, int sessionType) throws ReturnStatusException;

  ReturnStatus pauseSession (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  TestSession createSession (String clientName, long proctorKey, UUID browserKey, String sessionName, String proctorID, String proctorName, Date dateBegin, Date dateEnd)
      throws ReturnStatusException;

  ReturnStatus insertSessionTest (UUID sessionKey, long proctorKey, UUID browserKey, String testKey, String testID) throws ReturnStatusException;

  List<String> getSessionTests (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  ReturnStatus setSessionDateVisited (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  boolean hasActiveOpps (UUID sessionKey, long proctorKey, UUID browserKey) throws ReturnStatusException;

  ReturnStatus handoffSession (String clientname, long proctorKey, UUID browserKey, String sessionID, _Ref<UUID> sessionKey) throws ReturnStatusException;

}
