package org.zanata.adapter.properties;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.*;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PropReaderTests
{
   private static final Logger log = LoggerFactory.getLogger(PropReaderTests.class);

   @SuppressWarnings("deprecation")
   PropReader propReader;

   @BeforeMethod
   public void resetReader()
   {
      propReader = new PropReader();
   }

   @Test
   public void roundtripSrcPropsToDocXmlToProps() throws Exception
   {
      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream("test.properties");

      propReader.extractTemplate(srcDoc, testStream);
      JAXBContext jc = JAXBContext.newInstance(Resource.class);
      Marshaller marshal = jc.createMarshaller();
      StringWriter sw = new StringWriter();
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshal.marshal(srcDoc, sw);
      log.debug("{}", sw);

      Unmarshaller unmarshal = jc.createUnmarshaller();
      Resource docIn = (Resource) unmarshal.unmarshal(new StringReader(sw.toString()));

      PropWriter.write(docIn, new File("target/test-output"));

      // TODO check output files against input
   }

   @Test
   public void roundtripTransPropsToDocXmlToProps() throws Exception
   {
      String locale = "fr";
      InputStream targetStream = getResourceAsStream("test_fr.properties");
      TranslationsResource transDoc = new TranslationsResource();
      propReader.extractTarget(transDoc, targetStream, new LocaleId(locale), ContentState.New);

      JAXBContext jc = JAXBContext.newInstance(TranslationsResource.class);
      Marshaller marshal = jc.createMarshaller();
      StringWriter sw = new StringWriter();
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshal.marshal(transDoc, sw);
      log.debug("{}", sw);

      Unmarshaller unmarshal = jc.createUnmarshaller();
      TranslationsResource docIn = (TranslationsResource) unmarshal.unmarshal(new StringReader(sw.toString()));

      PropWriter.write(docIn, new File("target/test-output"), "test", locale);

      // TODO check output files against input
   }

   private InputStream getResourceAsStream(String relativeResourceName) throws FileNotFoundException
   {
      InputStream stream = PropReaderTests.class.getResourceAsStream(relativeResourceName);
      if (stream == null)
         throw new FileNotFoundException(relativeResourceName);
      return stream;
   }
   
   @Test
   public void extractTemplateRemovesNonTranslateableRegions() throws IOException
   {
      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream("test_non_trans.properties");
      propReader.extractTemplate(srcDoc, testStream);

      List<TextFlow> textFlows = srcDoc.getTextFlows();

      assertEquals(textFlows.size(), 2, "Unexpected number of textflows");
      assertEquals(textFlows.get(0).getId(), "HELLO", "Unexpected textflow id");
      assertEquals(textFlows.get(1).getId(), "GOODBYE", "Unexpected textflow id");
      // TODO also check comments?
   }

   @Test
   public void extractTemplateNestedNonTranslatableRegions() throws Exception
   {
      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream("test_non_trans_nested.properties");
      propReader.extractTemplate(srcDoc, testStream);

      List<TextFlow> textFlows = srcDoc.getTextFlows();

      assertEquals(textFlows.size(), 2, "Unexpected number of textflows");
      assertEquals(textFlows.get(0).getId(), "HELLO", "Unexpected textflow id");
      assertEquals(textFlows.get(1).getId(), "GOODBYE", "Unexpected textflow id");
      // TODO also check comments?
   }

   @Test(expectedExceptions = InvalidPropertiesFormatException.class)
   public void extractTemplateNonTranslatableMismatchException() throws IOException, InvalidPropertiesFormatException
   {
      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream("test_non_trans_mismatch.properties");
      propReader.extractTemplate(srcDoc, testStream);
   }
}
