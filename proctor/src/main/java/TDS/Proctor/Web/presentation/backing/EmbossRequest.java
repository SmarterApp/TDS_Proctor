/*************************************************************************
 * Educational Online Test Delivery System Copyright (c) 2015 American
 * Institutes for Research
 * 
 * Distributed under the AIR Open Source License, Version 1.0 See accompanying
 * file AIR-License-1_0.txt or at
 * https://bitbucket.org/sbacoss/eotds/wiki/AIR_Open_Source_License
 *************************************************************************/

package TDS.Proctor.Web.presentation.backing;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import TDS.Proctor.Services.EmbossFileService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tds.itemrenderer.webcontrols.PageLayout;
import AIR.Common.Utilities.Path;
import AIR.Common.Utilities.TDSStringUtils;
import AIR.Common.Web.WebHelper;
import AIR.Common.Web.Session.HttpContext;
import AIR.Common.Web.Session.Server;
import TDS.Proctor.Presentation.IPrintRequestPresenterView;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Presentation.PrintRequestPresenter;
import TDS.Proctor.Sql.Data.TesteeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author mskhan
 * 
 */
@Component
@Scope("request")
public class EmbossRequest extends BasePage implements IPrintRequestPresenterView
{

  private static final Logger   _logger = LoggerFactory.getLogger (EmbossRequest.class);

  private PrintRequestPresenter _presenter;
  private UUID                  _requestKey;
  private String                _lblName;
  private String                _lblSSID;
  private String                _lblDate;
  private PageLayout            _pageLayout;
  private String                _title;
  @Autowired
  private EmbossFileService     _embossFileService;

  public void download (String filepath, String contentType, String contentDisposition) throws IOException {
    String[] files = filepath.split(";");
    String filename = _embossFileService.getCombinedFileName(files[0], files.length > 1 ? "_combined" : "");
    try {
      byte[] fileContents = _embossFileService.combineFiles(files);
      
      FacesContext facesContext = FacesContext.getCurrentInstance ();
      ExternalContext externalContext = facesContext.getExternalContext ();
      externalContext.responseReset ();
      externalContext.setResponseContentType (contentType);
      externalContext.setResponseContentLength (fileContents.length);
      externalContext.setResponseHeader ("Content-Disposition", contentDisposition + "; filename=" + filename);

      OutputStream outStream = externalContext.getResponseOutputStream ();
      outStream.write(fileContents);

      facesContext.responseComplete ();
    } catch (Exception ex) {
      writeError("Unable to download the content file: " + filename);
      _logger.error(ex.getMessage(), ex);
      // handle the message first
      // TDSLogger.Application.Fatal (ex);
    }
  }

  @PostConstruct
  protected void Page_Load ()
  {
    // The file path to download.
    try
    {
      // Fetches the print request from the database and displays the resluts
      String sampleFilePath = WebHelper.getQueryString ("sampleFilePath");
      boolean isSampleFile = !StringUtils.isEmpty (sampleFilePath);
      String filepath = null;
      if (isSampleFile) // for sample files download
      {
        filepath = Server.mapPath (sampleFilePath);
      }
      else
      {
        setRequestKey (UUID.fromString (WebHelper.getQueryString ("requestKey")));
        _presenter = new PrintRequestPresenter (this);
        TesteeRequest testeeRequest = _presenter.GetTesteeRequest ();
        if (testeeRequest == null)
          return;
        filepath = testeeRequest.getRequestValue ();
      }
      String contentType;
      String contentDisposition = "attachment";
      String fileext = Path.getExtension (filepath).toLowerCase ();
      switch (fileext)
      {
      case "brf":
        contentType = "application/x-brf";
        break;
      case "prn":
        contentType = "application/x-VPPrintFile";
        break;
      default:
        writeError (TDSStringUtils.format ("Unknown file type: {0}", fileext));
        return;
      }

      download (filepath, contentType, contentDisposition);
    } catch (Exception ex)
    {
      writeError (ex.getMessage ());
      // handle the message first
      // TDSLogger.Application.Fatal (ex);
      return;
    }
  }

  public void writeError (String msg)
  {
    try {
      HttpContext.getCurrentContext ().getResponse ().getWriter ().write (TDSStringUtils.format ("<html><body><h1>{0}</h1></body></html>", msg));
    } catch (IOException e) {
      _logger.error (e.getMessage (), e);
    }
  }

  public UUID getRequestKey () {
    return _requestKey;
  }

  public void setRequestKey (UUID value) {
    this._requestKey = value;
  }

  public String getLblName () {
    return _lblName;
  }

  public void setLblName (String value) {
    this._lblName = value;
  }

  public String getLblSSID () {
    return _lblSSID;
  }

  public void setLblSSID (String value) {
    this._lblSSID = value;
  }

  public String getLblDate () {
    return _lblDate;
  }

  public void setLblDate (String value) {
    this._lblDate = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * TDS.Proctor.Presentation.IPresenterBase#displayMessage(java.lang.String)
   */
  @Override
  public void displayMessage (String msg) {
    writeError (msg);
  }

  /*
   * (non-Javadoc)
   * 
   * @see TDS.Proctor.Presentation.IPresenterBase#setPresenterBase(TDS.Proctor.
   * Presentation.PresenterBase)
   */
  @Override
  public void setPresenterBase (PresenterBase value) {

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * TDS.Proctor.Presentation.IPrintRequestPresenterView#setPageTitle(java.lang
   * .String)
   */
  @Override
  public void setPageTitle (String value) {
    _title = value;
  }

  public String getPageTitle ()
  {
    return _title;
  }

  private PageLayout getPageLayout () {
    return _pageLayout;
  }

  private void setPageLayout (PageLayout value) {
    this._pageLayout = value;
  }

}