package test;

import java.net.URL;
import java.util.Vector;

import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.StringDeserializer;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.util.xml.QName;
import org.apache.soap.Constants;

public class SOAPTestClient {
  public static void main(String [] args) {
    try {
      // URI des aufzurufenden Web-Service
      String locationUrlString = "https://www.amberdms.com/products/billing_system/online/api/authenticate/authenticate.php";
      // Name der SOAPAction im HTTP-Header
      // klappt auch mit beliebigen Angaben
      String soapActionString = "urn:authenticate#login";
      // URI für den Namensraum des Web-Service
      // klappt auch mit beliebigen Angaben
      String namespaceUriString = "urn:authenticate";
      // Name der aufzurufenden Methode
      // wird benötigt
      String methodNameString = "login";

      // Erzeugen eines Call-Objekts für den RPC
      Call call = new Call();
      call.setTargetObjectURI(namespaceUriString);
      call.setMethodName(methodNameString);
      call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
      
      SOAPMappingRegistry soapMappingRegistry = new SOAPMappingRegistry();
      soapMappingRegistry.mapTypes(Constants.NS_URI_SOAP_ENC, new QName("", "sessionid"),
                                   null, null, new StringDeserializer());
      call.setSOAPMappingRegistry(soapMappingRegistry);

      // Erzeugen der Parameterliste
      Vector<Parameter> params = new Vector<Parameter>();
      params.addElement(new Parameter("username", String.class, "setup" ,null));
      params.addElement(new Parameter("password", String.class, "vvw6ag" ,null));
      params.addElement(new Parameter("account", String.class, "demo86" ,null));

      // Zuweisen der Parameterliste an das Call-Objekt
      call.setParams(params);

      // Aufrufen des Web-Service
      Response res = call.invoke(new URL(locationUrlString), soapActionString);

      // Ausgabe des Ergebnisses
      if( res.generatedFault() ==false)
      {
        Parameter retValue = res.getReturnValue();
        Object value = retValue.getValue();
        System.out.print(retValue.getName() + ":\040");
        System.out.println(value);
      } else {
        System.out.println("Fehler: "+res.getFault().getFaultString());
      }
    } 
    catch (Exception e) {
     System.err.println(e.toString());
    }
	}
}
