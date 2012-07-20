package org.zanata.maven;

import java.util.Arrays;

import org.zanata.client.commands.push.PushCommand;
import org.zanata.client.commands.push.PushPullType;

public class PushMojoTest extends ZanataMojoTest<PushMojo, PushCommand>
{
   PushCommand mockCommand = control.createMock(PushCommand.class);
   PushMojo pushMojo = new PushMojo()
   {
      @Override
      public PushCommand initCommand()
      {
         return mockCommand;
      }
   };

   public PushMojoTest() throws Exception
   {
   }

   @Override
   protected PushMojo getMojo()
   {
      return pushMojo;
   }

   @Override
   protected PushCommand getMockCommand()
   {
      return mockCommand;
   }

   @Override
   protected void setUp() throws Exception
   {
      // required for mojo lookups to work
      super.setUp();
   }

   @Override
   protected void tearDown() throws Exception
   {
      // required
      super.tearDown();
   }

   /**
    * Test that the pom.xml settings are applied as expected
    * 
    * @throws Exception
    */
   public void testPomConfig() throws Exception
   {
      applyPomParams("pom-config.xml");
      assertEquals("srcDir", pushMojo.getSrcDir().toString());
      assertEquals("transDir", pushMojo.getTransDir().toString());
      assertEquals("es", pushMojo.getSourceLang());
      assertEquals(PushPullType.Both, pushMojo.getPushType());
      assertEquals(false, pushMojo.getCopyTrans());
      assertEquals("import", pushMojo.getMergeType());
      assertEquals(Arrays.asList("includes"), pushMojo.getIncludes());
      assertEquals(Arrays.asList("excludes"), pushMojo.getExcludes());
      assertEquals(false, pushMojo.getDefaultExcludes());
   }

   /**
    * Test that the pom.xml settings are applied as expected using the pushType
    * mojo parameter,
    *
    * @throws Exception
    */
   public void testPomConfigWithPushType() throws Exception
   {
      applyPomParams("pom-config-pushType.xml");
      assertEquals("srcDir", pushMojo.getSrcDir().toString());
      assertEquals("transDir", pushMojo.getTransDir().toString());
      assertEquals("es", pushMojo.getSourceLang());
      assertEquals(PushPullType.Trans, pushMojo.getPushType());
      assertEquals(false, pushMojo.getCopyTrans());
      assertEquals("import", pushMojo.getMergeType());
      assertEquals(Arrays.asList("includes"), pushMojo.getIncludes());
      assertEquals(Arrays.asList("excludes"), pushMojo.getExcludes());
      assertEquals(false, pushMojo.getDefaultExcludes());
   }


}
