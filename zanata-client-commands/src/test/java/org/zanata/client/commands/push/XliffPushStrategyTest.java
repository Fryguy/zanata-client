package org.zanata.client.commands.push;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

public class XliffPushStrategyTest
{
   @Mock
   private PushOptions mockPushOption;

   private LocaleList locales = new LocaleList();

   private XliffStrategy xliffStrategy;
   private List<String> include;
   private List<String> exclude;

   private final File sourceDir = new File("src/test/resources/xliffDir");
   private final File sourceDir2 = new File("src/test/resources/xliffDir2");

   private final String sourceLocale = "en-US";

   @Before
   public void prepare()
   {
      locales.add(new LocaleMapping("de"));
      locales.add(new LocaleMapping("fr"));
   }

   @Before
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      xliffStrategy = new XliffStrategy();
      include = new ArrayList<String>();
      exclude = new ArrayList<String>();
   }

   @Test
   public void findDocNamesTest() throws IOException
   {
      include.add("**/**StringResource_en_US*");

      when(mockPushOption.getLocaleMapList()).thenReturn(locales);
      when(mockPushOption.getSourceLang()).thenReturn(sourceLocale);
      when(mockPushOption.getDefaultExcludes()).thenReturn(true);
      when(mockPushOption.getCaseSensitive()).thenReturn(true);
      when(mockPushOption.getExcludeLocaleFilenames()).thenReturn(true);
      when(mockPushOption.getValidate()).thenReturn("xsd");

      xliffStrategy.setPushOptions(mockPushOption);

      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir, include, exclude, mockPushOption.getDefaultExcludes(), mockPushOption.getCaseSensitive(), mockPushOption.getExcludeLocaleFilenames());
      Assert.assertEquals(3, localDocNames.size());
   }

   @Test
   public void loadSrcDocTest() throws IOException
   {
      include.add("**/**StringResource_en_US*");

      when(mockPushOption.getTransDir()).thenReturn(sourceDir);
      when(mockPushOption.getLocaleMapList()).thenReturn(locales);
      when(mockPushOption.getSourceLang()).thenReturn(sourceLocale);
      when(mockPushOption.getDefaultExcludes()).thenReturn(true);
      when(mockPushOption.getCaseSensitive()).thenReturn(true);
      when(mockPushOption.getExcludeLocaleFilenames()).thenReturn(true);
      when(mockPushOption.getValidate()).thenReturn("xsd");

      xliffStrategy.setPushOptions(mockPushOption);

      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir, include, exclude, mockPushOption.getDefaultExcludes(), mockPushOption.getCaseSensitive(), mockPushOption.getExcludeLocaleFilenames());
      List<Resource> resourceList = new ArrayList<Resource>();
      for (String docName : localDocNames)
      {
         Resource srcDoc = xliffStrategy.loadSrcDoc(sourceDir, docName);
         resourceList.add(srcDoc);

         TranslationResourcesVisitor visitor = mock(TranslationResourcesVisitor.class);

         LocaleMapping loc;
         // each src file in test has one trans file ('de' or 'fr'):
         if (srcDoc.getName().equals("dir1/StringResource"))
         {
            loc = new LocaleMapping("de");
         }
         else
         {
            loc = new LocaleMapping("fr");
         }
         xliffStrategy.visitTranslationResources(docName, srcDoc, visitor);
         verify(visitor).visit(eq(loc), isA(TranslationsResource.class));
      }
      Assert.assertEquals(3, resourceList.size());
   }

   @Test
   public void loadSrcDocTestWithCaseSensitive() throws IOException
   {
      include.add("StringResource_en*");

      when(mockPushOption.getSourceLang()).thenReturn(sourceLocale);
      when(mockPushOption.getLocaleMapList()).thenReturn(locales);
      when(mockPushOption.getCaseSensitive()).thenReturn(true);
      when(mockPushOption.getExcludeLocaleFilenames()).thenReturn(true);
      when(mockPushOption.getDefaultExcludes()).thenReturn(false);
      when(mockPushOption.getValidate()).thenReturn("xsd");

      xliffStrategy.setPushOptions(mockPushOption);
      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir2, include, exclude, mockPushOption.getDefaultExcludes(), mockPushOption.getCaseSensitive(), mockPushOption.getExcludeLocaleFilenames());
      Assert.assertEquals(5, localDocNames.size());
   }

   @Test
   public void loadSrcDocTestWithoutCaseSensitive() throws IOException
   {
      include.add("STRINGRESOURCE_en*");

      when(mockPushOption.getSourceLang()).thenReturn(sourceLocale);
      when(mockPushOption.getLocaleMapList()).thenReturn(locales);
      when(mockPushOption.getCaseSensitive()).thenReturn(false);
      when(mockPushOption.getExcludeLocaleFilenames()).thenReturn(true);
      when(mockPushOption.getDefaultExcludes()).thenReturn(false);
      when(mockPushOption.getValidate()).thenReturn("xsd");

      xliffStrategy.setPushOptions(mockPushOption);
      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir2, include, exclude, mockPushOption.getDefaultExcludes(), mockPushOption.getCaseSensitive(), mockPushOption.getExcludeLocaleFilenames());
      Assert.assertEquals(6, localDocNames.size());
   }

   @Test
   public void loadSrcDocTestWithExcludeFileOption() throws IOException
   {
      include.add("**/**StringResource_en_US*");
      exclude.add("**/*StringResource*");

      when(mockPushOption.getTransDir()).thenReturn(sourceDir);
      when(mockPushOption.getLocaleMapList()).thenReturn(locales);
      when(mockPushOption.getSourceLang()).thenReturn(sourceLocale);
      when(mockPushOption.getDefaultExcludes()).thenReturn(true);
      when(mockPushOption.getCaseSensitive()).thenReturn(true);
      when(mockPushOption.getExcludeLocaleFilenames()).thenReturn(true);
      when(mockPushOption.getValidate()).thenReturn("xsd");

      xliffStrategy.setPushOptions(mockPushOption);

      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir, include, exclude, mockPushOption.getDefaultExcludes(), mockPushOption.getCaseSensitive(), mockPushOption.getExcludeLocaleFilenames());
      List<Resource> resourceList = new ArrayList<Resource>();
      for (String docName : localDocNames)
      {
         Resource srcDoc = xliffStrategy.loadSrcDoc(sourceDir, docName);
         resourceList.add(srcDoc);

         TranslationResourcesVisitor visitor = mock(TranslationResourcesVisitor.class);
         LocaleMapping loc;
         // each src file in test has one trans file ('de' or 'fr'):
         if (srcDoc.getName().equals("dir1/StringResource"))
         {
            loc = new LocaleMapping("de");
         }
         else
         {
            loc = new LocaleMapping("fr");
         }
         xliffStrategy.visitTranslationResources(docName, srcDoc, visitor);
         verify(visitor).visit(eq(loc), isA(TranslationsResource.class));
      }
      Assert.assertEquals(0, resourceList.size());
   }

   @Test
   public void loadSrcDocTestWithExcludeOption() throws IOException
   {
      include.add("**/**StringResource_en_US*");
      exclude.add("**/dir2/*");

      when(mockPushOption.getTransDir()).thenReturn(sourceDir);
      when(mockPushOption.getLocaleMapList()).thenReturn(locales);
      when(mockPushOption.getSourceLang()).thenReturn(sourceLocale);
      when(mockPushOption.getDefaultExcludes()).thenReturn(true);
      when(mockPushOption.getCaseSensitive()).thenReturn(true);
      when(mockPushOption.getExcludeLocaleFilenames()).thenReturn(true);
      when(mockPushOption.getValidate()).thenReturn("xsd");

      xliffStrategy.setPushOptions(mockPushOption);

      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir, include, exclude, mockPushOption.getDefaultExcludes(), mockPushOption.getCaseSensitive(), mockPushOption.getExcludeLocaleFilenames());
      List<Resource> resourceList = new ArrayList<Resource>();
      for (String docName : localDocNames)
      {
         Resource srcDoc = xliffStrategy.loadSrcDoc(sourceDir, docName);
         resourceList.add(srcDoc);

         TranslationResourcesVisitor visitor = mock(TranslationResourcesVisitor.class);
         LocaleMapping loc;
         // each src file in test has one trans file ('de' or 'fr'):
         if (srcDoc.getName().equals("dir1/StringResource"))
         {
            loc = new LocaleMapping("de");
         }
         else
         {
            loc = new LocaleMapping("fr");
         }
         xliffStrategy.visitTranslationResources(docName, srcDoc, visitor);
         verify(visitor).visit(eq(loc), isA(TranslationsResource.class));
      }
      Assert.assertEquals(1, resourceList.size());
   }
}
