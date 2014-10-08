/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package TDS.Proctor.Services.UnitTests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils
{
  private static Logger logger = LoggerFactory.getLogger (Utils.class);

  public static void writeInfo (String formatPattern, Object... values) {

    logger.info (formatPattern, values);
    // Console.ForegroundColor = ConsoleColor.White;
    // Console.WriteLine(format, args);
    // Console.ResetColor();
  }

  public static void writeSuccess (String formatPattern, Object... values) {
    logger.info (formatPattern, values);
    // Console.ForegroundColor = ConsoleColor.Green;
    // Console.WriteLine(format, args);
    // Console.ResetColor();
  }

  public static void writeWarning (String formatPattern, Object... values) {
    System.out.printf (formatPattern, values);
    // Console.ForegroundColor = ConsoleColor.Yellow;
    // Console.WriteLine(format, args);
    // Console.ResetColor();
  }

  public static void writeValidation (String name, String message, Object... values) {
    logger.info (name);
    // Console.ForegroundColor = ConsoleColor.Yellow;
    // Console.Write(name);
    // Console.ResetColor();

    logger.info (" : ");
    // Console.ForegroundColor = ConsoleColor.White;
    // Console.Write(" : ");
    // Console.ResetColor();

    logger.info (message, values);
    // Console.ForegroundColor = ConsoleColor.Gray;
    // Console.WriteLine(message, args);
    // Console.ResetColor();
  }

  public static void writeError (String formatPattern, Object... values) {
    logger.error (formatPattern, values);
    // Console.ForegroundColor = ConsoleColor.Red;
    // Console.WriteLine(format, args);
    // Console.ResetColor();
  }

}
