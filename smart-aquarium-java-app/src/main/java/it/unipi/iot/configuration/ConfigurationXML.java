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
    private final String pathXML = "./config.xml";
    private final String pathXSD = "./config.xsd";
    public ConfigurationParameters configurationParameters;
    
    public ConfigurationXML(){
       configurationParameters = new ConfigurationParameters();
       validateXML();
       deserializeXML();
    }
    
    private void deserializeXML(){
        
    XStream xs = new XStream();
    xs.addPermission(AnyTypePermission.ANY);
    
    String x = new String(); 
    try {   
        x = new String(Files.readAllBytes(Paths.get(pathXML)));       
     }  catch (Exception e) {}    
    
        configurationParameters = (ConfigurationParameters)xs.fromXML((x));
    }
    
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