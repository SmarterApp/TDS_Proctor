/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Web.presentation.taglib;

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import AIR.Common.Utilities.TDSStringUtils;
import TDS.Proctor.Presentation.PresenterBase;
import TDS.Proctor.Sql.Data.AppConfig;

@FacesComponent (value = "CSSLink")
public class CSSLink extends UIComponentBase
{
  private String        _href     = null;
  private String        _media    = null;
  private String        _type     = null;
  private String        _rel      = null;

  private PresenterBase presenter = null;

  public String getRel () {
    return this._rel;
  }

  public void setRel (String rel) {
    this._rel = rel;
  }

  public String getType () {
    return _type;
  }

  public void setType (String type) {
    this._type = type;
  }

  public String getMedia () {
    return _media;
  }

  public void setMedia (String media) {
    this._media = media;
  }

  public String getHref () {
    return _href;
  }

  public void setHref (String href) {
    this._href = href;
  }

  public PresenterBase getPresenter () {
    return presenter;
  }

  public void setPresenter (PresenterBase presenter) {
    this.presenter = presenter;
  }

  @Override
  public String getFamily () {
    return "CSSLink";
  }

  @Override
  public void encodeAll (FacesContext context) throws IOException {
    ResponseWriter output = context.getResponseWriter ();
    // resolve url
    String url = null;
    String manifestKey = this._href; // This is how the file name would be
                                     // listed in our MD5 manifest

    if (this._href.indexOf ("{0}") > 0) {
      // This is some client specific CSS. Get client path from externs
      // from P_GetConfigs
      String cssPath = "";
      if (presenter != null) {
        AppConfig pAppConfig = presenter.getAppConfigDB ();
        if (pAppConfig != null) {
          cssPath = pAppConfig.getClientPath ();
        }
      }
      url = TDSStringUtils.format (this._href, cssPath);
    } else {
      url = this._href;
    }

    output.startElement ("link", null);

    output.writeAttribute ("href", url, null);

    // check if there is a media defined
    if (_media == null)
      _media = "screen";
    output.writeAttribute ("media", _media, null);

    if (_type == null)
      _type = "text/css";
    output.writeAttribute ("type", _type, null);

    if (_rel == null)
      _rel = "stylesheet";
    output.writeAttribute ("rel", "stylesheet", null);

    // TODO Shiva should we put the lines below? Something similar was in .NET
    // code.
    // for (Map.Entry<String, Object> pair : this.getAttributes().entrySet ())
    // {
    // output.WriteAttribute(pair.getKey (), pair.getValue ().toString ());
    // }
    output.endElement ("link");
    output.write ("\r\n");
  }
}
