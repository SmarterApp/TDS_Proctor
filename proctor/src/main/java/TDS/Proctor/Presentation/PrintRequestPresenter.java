/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Presentation;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.omg.CORBA.SystemException;

import tds.itemrenderer.ITSDocumentFactory;
import tds.itemrenderer.data.IITSDocument;
import tds.itemrenderer.data.IItemRender;
import tds.itemrenderer.data.ItemRender;
import tds.itemrenderer.data.ItemRenderGroup;
import AIR.Common.Json.JsonHelper;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Sql.Data.TesteeRequest;
import TDS.Shared.Exceptions.ReturnStatusException;

public class PrintRequestPresenter extends PresenterBase
{
  private/* readonly */IPrintRequestPresenterView _view;

  TesteeRequest                                   _testeeRequest;

  public PrintRequestPresenter (IPrintRequestPresenterView view)// :base(view)
  {
    super (view);
    this._view = view;
    // init
    InitView ();
  }

  public PrintRequestPresenter (IPrintRequestPresenterView view, boolean isEmbossRequest)// :base(view)
  {
    super (view);
    this._view = view;
    // get testee request values
    try
    {
      _testeeRequest = getProctorTasks ().getRequestTasks ().getTesteeRequestValues
          (this.getThisUser ().getSessionKey (),
              getThisUser ().getKey (),
              getThisUser ().getBrowserKey (),
              _view.getRequestKey (),
              true);
      Date date = new Date ();
      _testeeRequest.setDatePrinted (date); // StrDatePrinted
                                                           // =
                                                           // DateTime.Now.ToShortDateString
                                                           // ();
    } catch (ReturnStatusException rex)
    {
      _view.displayMessage (rex.getReturnStatus ().getReason ());
      return;
    } catch (Exception ex)
    {
      _view.displayMessage (ex.getMessage ());
      return;
    }
  }

  public TesteeRequest GetTesteeRequest ()
  {
    return _testeeRequest;
  }

  private void InitView ()
  {
    // get testee request values
    try
    {
      _testeeRequest = getProctorTasks ().getRequestTasks ().getTesteeRequestValues (getThisUser ().getSessionKey (), getThisUser ().getKey (), getThisUser ().getBrowserKey (),
      _view.getRequestKey (), true);
      Date date = new Date ();
      _testeeRequest.setDatePrinted (date);
    } catch (ReturnStatusException rex)
    {
      _view.displayMessage (rex.getReturnStatus ().getReason ());
      return;
    } catch (Exception ex)
    {
      _view.displayMessage (ex.getMessage ());
      return;
    }

    /*
     * All we need is the testeerequest values. This information is parsed and
     * sent to blackbox via javascript.
     * 
     * RequestType requestType =
     * _testeeRequest.RequestType.ConvertToEnum<RequestType>();
     * 
     * if (requestType == RequestType.PRINTITEM)
     * this.LoadItem(_testeeRequest.RequestValue, _testeeRequest.AccCode,
     * _testeeRequest.ItemResponse); else
     * this.LoadPassage(_testeeRequest.RequestValue, _testeeRequest.AccCode);
     */
  }

  public String GetTesteeRequestJSON ()
  {
    StringBuilder testeeRequest = new StringBuilder ();
    if (_testeeRequest == null)
      testeeRequest.append ("var gTesteeRequest = null;");
    else
      try {
        testeeRequest.append (TDSStringUtils.format ("var gTesteeRequest = {0};", JsonHelper.serialize (_testeeRequest)));
      } catch (IOException e) {
        e.printStackTrace ();
      }
    return testeeRequest.toString ();
  }

  private void LoadItem (String filePath, String languageCode, String response)
  {
    try
    {
      // filePath = @"C:\projects\AIR\FTP\Item-104-563\item-104-563.xml";
      // create a item group structure
      String id = "";
      String segmentID = "";
      ItemRenderGroup itemRenderGroup = new ItemRenderGroup (id, segmentID, languageCode);

      // load item into ITS document object
      IITSDocument itemDoc = ITSDocumentFactory.load (filePath, languageCode, true);

      // add item to control
      IItemRender itemRender = new ItemRender (itemDoc, (int) itemDoc.getItemKey ());
      itemRender.setResponse (response);
      itemRenderGroup.add (itemRender);

      /*
       * We are no longer using the PageLayout control therefore, commented out
       * 
       * // figure out the right response type for printing if (itemDoc.Format
       * == "MC") { _view.PageLayout.ResponseTypeOverride = "MCPrint"; } else if
       * (itemDoc.Format != "GI") { _view.PageLayout.ResponseTypeOverride =
       * "CRPrint"; }
       * 
       * _view.PageLayout.SetItemGroup(itemRenderGroup, languageCode);
       * _view.PageLayout.Layout = "Layout_item.ascx";
       * 
       * 
       * // show the layout in the browser title for debug purposes
       * _view.PageLayout.OnRendered += new EventHandler(delegate {
       * _view.DisplayMessage(_view.PageLayout.ErrorDescription); });
       */
    } catch (SystemException ex)
    {
      _view.displayMessage ("UnableToProcessRequest");
      // TDSLogger.Application.Error(ex);
      // TODO Shajib: we have no clearError method in Server, so commented now
      // HttpContext.getCurrentContext ().getServer().ClearError();
    }
  }

  // / <summary>
  // / This is a helper method for loading data into the page layout control on
  // this page
  // / </summary>
  // / <param name="filePath">The full fule path to the passage</param>
  // / <param name="languageCode">The language code from the students
  // accommodation (it is probably either ENG or ESN)</param>
  private void LoadPassage (String filePath, String languageCode)
  {
    try
    {
      // for debug
      // filePath = @"C:\projects\AIR\FTP\stim-104-4239.xml"; //testing

      // load passage into ITS document object
      IITSDocument stimulusDoc = ITSDocumentFactory.load (filePath, languageCode, true);

      // add passage to item group (no items though)
      String id = "";
      String segmentID = "";
      ItemRenderGroup itemGroup = new ItemRenderGroup (id, segmentID, languageCode);
      itemGroup.setPassage (stimulusDoc);

      // add data to the page layout control
      // _view.PageLayout.SetItemGroup(itemGroup, languageCode);
      // _view.PageLayout.Layout = "Layout_PassagePrint.ascx";

    } catch (SystemException ex)
    {
      _view.displayMessage ("UnableToProcessRequest");
      // TDSLogger.Application.Error(ex);

      // TODO Shajib: we have no clearError method in Server, so commented now
      // HttpContext.getCurrentContext ().getServer().ClearError();
    }
  }

}
