package org.yawlfoundation.yawl.digitalSignature;

/**
 *
 * @author seb
 */
import org.bouncycastle.cms.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.*;

public class DigitalSignature extends InterfaceBWebsideController 
{
    private static final String _Document = "Document";
    private static final String _Signature = "Signature";
    private static final String _CheckSignature = "CheckSignature";
    private static final String _Alias = "Name";
    
    private static String _Certificate = null;
    private static String _P12 = null;
    private static String _Password = null;
    private static String _Pathway = System.getenv("CATALINA_HOME") + "/webapps/digitalSignature/files/";
    private static String _Name = null;
    
    private static String _sessionHandle = null;
    
    private static org.jdom.Document Doc = null;
    
    //Function used by the Custom Service.
    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem)
    {
    	 try
         {             
	    	  if(!checkConnection(_sessionHandle))
	          {
	               _sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
	          }
	          if (successful(_sessionHandle)) 
	          {
	                 WorkItemRecord child = checkOut(enabledWorkItem.getID(), _sessionHandle);
	                 if (child != null) 
	                 {
	                    List children = getChildren(enabledWorkItem.getID(),_sessionHandle);
	                    for (int i = 0; i < children.size(); i++)
	                    {
	                        WorkItemRecord itemRecord = (WorkItemRecord) children.get(i);
	                        if(WorkItemRecord.statusFired.equals(itemRecord.getStatus())) 
	                        {
	                            checkOut(itemRecord.getID(),_sessionHandle);
	                        }

	                    }
	                    List executingChildren = getChildren(enabledWorkItem.getID(),_sessionHandle);
	                    for (int i = 0; i < executingChildren.size(); i++) 
	                    {
	                    	WorkItemRecord itemRecord = (WorkItemRecord) executingChildren.get(i);
	                        Element element = (Element) itemRecord.getDataList();
	                    	
	                        String answer = "false";
	                        
	                        //Get the signed document
	                        String Document = element.getChild(_Signature).getText();
	                        Document  = Document.replace(" ","+");
	                       
	                        //Decode the BASE64 signature              
	                        sun.misc.BASE64Decoder deCoder = new sun.misc.BASE64Decoder();
	                        byte[] SignedDocument = deCoder.decodeBuffer(Document);
                        
	                        System.out.println("Beginning of Checking XmlSignature:");
	                        if(checkSignature(SignedDocument))
	                        	answer = "true";
	                        else answer = "false";
	                        System.out.println("end of Checking XmlSignature:");
	                   
	              
	                        //Set the output element
	                        Element Outputelement = prepareReplyRootElement(itemRecord, _sessionHandle);
	                        Element Child = new Element(_CheckSignature);
	                        Child.setText(answer);
	                        Outputelement.addContent(Child);
	                        
	                        Element Child2 = new Element(_Document);               	
	                        Child2.setContent(Doc.cloneContent());
	                        Outputelement.addContent(Child2);
	                        
	                        Element Child3 = new Element(_Alias);
	                        Child3.setText(_Name);
	                        Outputelement.addContent(Child3);
	                        
	                        //Check In the work item and finish the task. 
	                        this.checkInWorkItem(itemRecord.getID(), itemRecord.getDataList(), Outputelement,_sessionHandle);
	                     
	                        }
	                 }
	          }
         } catch (Exception e) {e.printStackTrace();}
    }
    public YParameter[] describeRequiredParams() 
    {
    	 YParameter[] params = new YParameter[4];
         YParameter param;

         param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
         param.setDataTypeAndName(XSD_STRINGTYPE, _Signature, XSD_NAMESPACE);
         param.setDocumentation("This is the document signed");
         params[0] = param;
         
         param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
         param.setDataTypeAndName(XSD_STRINGTYPE, _CheckSignature, XSD_NAMESPACE);
         param.setDocumentation("This say if the signature is valid or not");
         params[1] = param;
         
         param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
         param.setDataTypeAndName("anyType", _Document, XSD_NAMESPACE);
         param.setDocumentation("This is the Document Content");
         params[2] = param;
         
         param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
         param.setDataTypeAndName(XSD_STRINGTYPE, _Alias, XSD_NAMESPACE);
         param.setDocumentation("This is the Document Content");
         params[3] = param;
         
         return params;
    }
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord){
    	
    }
    // Function to check the Signature 
    public boolean checkSignature(byte[] Document)
    {
        try
        {
         // extract the Signed Fingerprint data  
         CMSSignedData signature = new CMSSignedData(Document);
         SignerInformation signer = (SignerInformation) signature.getSignerInfos().getSigners().iterator().next();
      
         // Get from the collection the appropriate registered certificate
         CertStore cs = signature.getCertificatesAndCRLs("Collection", "BC");
         Iterator iter = cs.getCertificates(signer.getSID()).iterator();
         X509Certificate certificate = (X509Certificate) iter.next();
         // get the contents of the document
         CMSProcessable sg = signature.getSignedContent();
    	 byte[] data = (byte[]) sg.getContent();
    	 String content = new String(data);
    	 
    	 //convert the document content to a valid xml document for YAWL
    	 org.w3c.dom.Document XMLNode = ConvertStringToDocument(content);
    	 org.jdom.input.DOMBuilder builder = new org.jdom.input.DOMBuilder();
         Doc = builder.build(XMLNode);
         
         //Check the document
         System.out.println("xml to Sign:");
       	 XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
         sortie.output(Doc, System.out);
    
        

    	// get the name of the signer
    	 _Name = certificate.getSubjectDN().getName().split("(=|, )", -1).toString();
    	 //return the result of the signature checking
     	 return signer.verify(certificate, "BC");
   
        }catch (Exception e) 
        	{
        	System.out.println("Test error");
        		e.printStackTrace();
        		return false;
        	}
        
    }
    //convert a String to a valid w3c Document
	public static org.w3c.dom.Document ConvertStringToDocument(String Doc)	
	{
		
		org.w3c.dom.Document document = null;				
		
		try
		{			
			DocumentBuilderFactory Factory = DocumentBuilderFactory.newInstance();								
			DocumentBuilder DocBuild = Factory.newDocumentBuilder();								
					
			StringBuffer Buffer = new StringBuffer(Doc);
			ByteArrayInputStream DocArray = new ByteArrayInputStream(Buffer.toString().getBytes("UTF-8"));
			document = DocBuild.parse(DocArray);
		
		}
		catch(ParserConfigurationException pce)
		{
			pce.printStackTrace();
			System.exit(0);
		}
		catch(org.xml.sax.SAXException se)
		{
			se.printStackTrace();
			System.exit(0);
		}
		catch(IOException ioe)
		{	ioe.printStackTrace();			
			System.exit(0);
		}
  	return document;
		
	}	
	//Function use to Sign Document (used by the Signature.jsp)
    public X509Certificate getCertificate()
	    {
	  
	      try
	      {
	    	  //Extract the x.509 certificates
	    	  InputStream inStream = new FileInputStream(_Pathway + _Certificate);
	          CertificateFactory cf = CertificateFactory.getInstance("X.509");
	          X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
	          inStream.close();
	         
	          _Certificate = null;
	          return cert;
	      }catch (Exception e) {
	          e.printStackTrace();
	          return null;
	        }
	                  
	    } 
    //Extract the PrivateKey from the keystore  
    private PrivateKey getPrivateKey()
	    {
	        KeyStore keystore = null;

	        try
	        {
	           char[] password = _Password.toCharArray();
	           String _alias= "";
	           _Password = null; 
	           keystore = KeyStore.getInstance("PKCS12");
	  
	           keystore.load(new FileInputStream(_Pathway + _P12), password);
	           
	           Enumeration enumeration = keystore.aliases();
	           Vector vectaliases = new Vector();
	           while (enumeration.hasMoreElements())vectaliases.add(enumeration.nextElement());
	       		
	           String[] aliases = (String []) (vectaliases.toArray(new String[0]));
	       		for (int i = 0; i < aliases.length; i++)
	       		if (keystore.isKeyEntry(aliases[i]))
	       		{
	       			_alias = aliases[i];
	       			break;
	       		}
	       		PrivateKey pk = (PrivateKey)keystore.getKey(_alias, password);
	       		password = null;
	       		return pk;
	       	
	                      
	        }catch (Exception e) {
	            System.out.println("Error: " + "Invalid pkcs#12 Certificate");
	            return null;
	            }
	            
	    }
    public String PrepareDocumentToBeSign(Element element)
	    {

	    	try{
	    	     //extract the Document to sign and transform it in a valid XML DOM 
	    	   	Element rootElement = new Element(element.getName());
	    	 	rootElement.setContent(element.cloneContent());
	    	 	//convert the Element in a JDOM Document
	    	 	Document xdoc = new Document( rootElement );
	    	 	//create a DOMOutputter to write the content of the JDOM document in a DOM document
	    	 	DOMOutputter outputter = new DOMOutputter();
	    	 	org.w3c.dom.Document Doctosign = outputter.output(xdoc);
	    	 	
	    	 	// Show the document before being sign 
	    	 	System.out.println("xml to Sign:");
	    	   	XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
	    	    sortie.output(xdoc, System.out);
	    	     
	    	 	//Transform the XML DOM in a String using the xml transformer
	    	     DOMSource domSource = new DOMSource(Doctosign);
	    	     StringWriter writer = new StringWriter();
	    	     StreamResult result = new StreamResult(writer);
	    	     TransformerFactory tf = TransformerFactory.newInstance();
	    	     Transformer transformer = tf.newTransformer();
	    	     transformer.transform(domSource, result);
	    	     String StringTobeSign = writer.toString();
	    	     
	    	     return StringTobeSign;
	    	     
	    	}   catch (Exception e) {
	    	    e.printStackTrace();  
	    	    return null;
	    	    }
	    		
	    	}
    public CMSSignedData SignedData(Element InputDocument)
    {
           	        
        try {
            X509Certificate cert = getCertificate();
            PrivateKey privatekey = getPrivateKey();
            String Document = PrepareDocumentToBeSign(InputDocument);
            System.out.println(Document);
            System.out.println("Certificate loaded");
        	// define the provider Bouncy castle  
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            	}
            
            //register the user certificate in the collection 
     		ArrayList certList = new ArrayList();
      		certList.add(cert);
      		CertStore certs = CertStore.getInstance("Collection",new CollectionCertStoreParameters(certList), "BC");
      		 
        	System.out.println("provider loaded");
            // create the CMSSignedData
            CMSSignedDataGenerator signGen = new CMSSignedDataGenerator();
        	System.out.println("CMS created");
            signGen.addSigner(privatekey, cert, CMSSignedDataGenerator.DIGEST_SHA1);
            signGen.addCertificatesAndCRLs(certs);
            System.out.println("Signer loaded");
            
            CMSProcessable content = new CMSProcessableByteArray(Document.getBytes());
            System.out.println("BytesArray loaded");
            // the second variable "true" means that the content will be wrap with the signature
            return signGen.generate(content, true, "BC");
            } 
        catch (Exception e) {
            e.printStackTrace();  
            return null;
            }
    }
	public void setP12AndPassword(String P12, String password, String Certificate) {
        _P12 = P12;
        _Password = password;
        _Certificate = Certificate;
        
    }
    public String ProgMain(Element Document){
    	try{
             
                 System.out.println("Beginning of XmlSignature:");
                 //Call the function to sign the document
                 byte[] signeddata = SignedData(Document).getEncoded();
                 System.out.println("End of Xml Signature");
    	  	  	
                 // Convert the signed data in a BASE64 string to make it a valid content 
                 // for Yawl
                 sun.misc.BASE64Encoder enCoder = new sun.misc.BASE64Encoder();
                 String base64OfSignatureValue = enCoder.encode(signeddata);
                 return base64OfSignatureValue;
                          
                
    		
    	} catch (Exception e) {e.printStackTrace();
    	return null;
    	}
    }


}