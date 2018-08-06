/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@weareavp.com)
 * Author: Rimsha Khalid (rimsha@weareavp.com)
 * Version: 0.1.6
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@weareavp.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class SimpleErrorHandler implements ErrorHandler {
	private String GACOM = "com.UKExactly";

	@Override
	public void warning(SAXParseException e) throws SAXException {
		Logger.getLogger(GACOM).log(Level.WARNING, "SAXParseException: ", e);
        System.out.println(e.getMessage());
    }

	@Override
    public void error(SAXParseException e) throws SAXException {
		Logger.getLogger(GACOM).log(Level.SEVERE, "SAXParseException: ", e);
        System.out.println(e.getMessage());
    }

	@Override
    public void fatalError(SAXParseException e) throws SAXException {
		Logger.getLogger(GACOM).log(Level.SEVERE, "SAXParseException: ", e);
        System.out.println(e.getMessage());
    }
}
