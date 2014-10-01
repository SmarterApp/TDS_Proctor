/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Sql.RepositorySP;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Utilities.Dates;
import TDS.Proctor.Sql.Data.AlertMessage;
import TDS.Proctor.Sql.Data.AlertMessages;
import TDS.Proctor.Sql.Data.Abstractions.IAlertMessageRepository;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.LoggerFactory;

public class AlertMessageRepository extends AbstractDAO implements IAlertMessageRepository
{
private static final Logger _logger = LoggerFactory.getLogger(AlertMessageRepository.class);
  public final String CMD_GET_CURRENT_MESSAGES = "{call P_GetCurrentAlertMessages(?) }";
  public final String CMD_GET_UNACKOWLEDGE     = "{call P_GetUnAcknowledgedAlertMessages(?, ?) }";

  public AlertMessages loadCurrentMessages (String clientName, int timezoneOffset) throws ReturnStatusException {
    AlertMessages alerts = new AlertMessages (timezoneOffset);
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_CURRENT_MESSAGES)) {
        callstatement.setString (1, clientName);
        if (callstatement.execute ()) {
          ResultSet rs = callstatement.getResultSet ();
          loadMessages (rs, alerts);
        }

      }

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return alerts;
  }

  public AlertMessages loadUnAcknowledgedMessages (String clientName, long proctorKey, int timezoneOffset) throws ReturnStatusException {
    AlertMessages alerts = new AlertMessages (timezoneOffset);
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_UNACKOWLEDGE)) {
        callstatement.setString (1, clientName);
        callstatement.setLong (2, proctorKey);
        if (callstatement.execute ()) {
          ResultSet rs = callstatement.getResultSet ();
          loadMessages (rs, alerts);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }

    return alerts;
  }

  private void loadMessages (ResultSet resultset, AlertMessages altMessages) throws SQLException {
    try {
      while (resultset.next ()) {
        Date date = resultset.getTimestamp ("dateStarted");
        Date currentdate = new Date ();
        boolean bStarted = (date.compareTo (currentdate) < 0);// already started
                                                              // messages
        if (!bStarted)
          continue;
        Date convertedDate = Dates.convertXST_EST (date, altMessages.getTimeZoneOffset ());
        // String key = resultset.getString ("_key");
        // System.out.println ("key value is =" + key);
        AlertMessage alertmsg = new AlertMessage (resultset.getString ("_key"), resultset.getString ("title"), resultset.getString ("message"), convertedDate, bStarted);
        altMessages.add (alertmsg);
      }

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw e;
    }
  }
}
