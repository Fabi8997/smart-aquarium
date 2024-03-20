package it.unipi.iot.configuration;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.security.AnyTypePermission;
import java.io.*;
import java.nio.file.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class ConfigurationXML {
	
	//Path of the configuration file and its validation file
    private final String pathXML = "./config.xml";
    private final String pathXSD = "./config.xsd";
    
    //Object that will contain the configuration parameters read from the configuration file
    public ConfigurationParameters configurationParameters;
    
    /**
     * Constructor of the class.<br>
     * It will validate the XML configuration file using its XSD schema, then the XML configuration file is deserialized 
     * into a ConfigurationParameters class. 
     */
    public ConfigurationXML(){
       configurationParameters = new ConfigurationParameters();
       validateXML();
       deserializeXML();
    }
    
    /**
     * Deserialize the XML Configuration file into a ConfigurationParameters class.
     */
    private void deserializeXML(){
        
    XStream xs = new XStream();
    xs.addPermission(AnyTypePermission.ANY);
    
    String x = new String(); 
    try {   
        x = new String(Files.readAllBytes(Paths.get(pathXML)));       
     }  catch (Exception e) {}    
    
        configurationParameters = (ConfigurationParameters)xs.fromXML((x));
    }
    
    /**
     * Validate the XML configuration file using its XSD schema.
     */
    private void validateXML(){
         
        try {  
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder(); 
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Document d = db.parse(new File(pathXML)); 
            Schema s = sf.newSchema(new StreamSource(new File(pathXSD)));
            s.newValidator().validate(new DOMSource(d));
        } catch (Exception e) {
            if (e instanceof SAXException) 
                System.out.println("Validation XML error: " + e.getMessage());
            else
                System.out.println("Other error: " + e.getMessage());    
        }  
    }
}