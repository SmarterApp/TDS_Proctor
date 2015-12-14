/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IProctorDLL;
import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Utilities.Dates;
import TDS.Proctor.Sql.Data.AlertMessage;
import TDS.Proctor.Sql.Data.AlertMessages;
import TDS.Proctor.Sql.Data.Abstractions.IAlertMessageRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

public class AlertMessageRepository extends AbstractDAO implements IAlertMessageRepository
{
	private static final Logger _logger = LoggerFactory.getLogger(AlertMessageRepository.class);
    @Autowired
	IProctorDLL  dll  = null;

	public AlertMessages loadCurrentMessages (String clientName, int timezoneOffset) throws ReturnStatusException {
    AlertMessages alerts = new AlertMessages (timezoneOffset);
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_GetCurrentAlertMessages_SP (connection, clientName);
      ReturnStatusException.getInstanceIfAvailable (result);
      Iterator<DbResultRecord> records = result.getRecords ();
      if (records != null) {
        loadMessages (records, alerts);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return alerts;
  }

  private void loadMessages (Iterator<DbResultRecord> records, AlertMessages altMessages) throws SQLException {

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        Date date = record.<Date> get ("dateStarted");
        Date currentdate = new Date ();
        boolean bStarted = (date.compareTo (currentdate) < 0);// already
        // started
        // messages
        if (!bStarted)
          continue;
        Date convertedDate = Dates.convertXST_EST (date, altMessages.getTimeZoneOffset ());
        AlertMessage alertmsg = new AlertMessage (record.<UUID> get ("_key").toString (), record.<String> get ("title"), record.<String> get ("message"), convertedDate, bStarted);
        altMessages.add (alertmsg);
      }
  }

  public AlertMessages loadUnAcknowledgedMessages (String clientName, long proctorKey, int timezoneOffset) throws ReturnStatusException {
    AlertMessages alerts = new AlertMessages (timezoneOffset);
    try (SQLConnection connection = getSQLConnection ()) {
      SingleDataResultSet result = dll.P_GetUnAcknowledgedAlertMessages_SP (connection, clientName, proctorKey);
      ReturnStatusException.getInstanceIfAvailable (result);
      Iterator<DbResultRecord> records = result.getRecords ();
      if (records != null) {
        loadMessages (records, alerts);
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return alerts;
  }

}
