/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.client.commands;

import java.io.Console;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public abstract class PushPullCommand<O extends PushPullOptions> extends ConfigurableProjectCommand<O>
{
   private static final Logger log = LoggerFactory.getLogger(PushPullCommand.class);

   protected final ISourceDocResource sourceDocResource;
   protected final ITranslatedDocResource translationResources;
   protected URI uri;
   private Marshaller marshaller;
   private String modulePrefix;

   public PushPullCommand(O opts, ZanataProxyFactory factory, ISourceDocResource sourceDocResource, ITranslatedDocResource translationResources, URI uri)
   {
      super(opts, factory);
      this.sourceDocResource = sourceDocResource;
      this.translationResources = translationResources;
      this.uri = uri;
      this.modulePrefix = opts.getEnableModules() ? getOpts().getCurrentModule() + opts.getModuleSuffix() : "";
   }

   private PushPullCommand(O opts, ZanataProxyFactory factory)
   {
      this(opts, factory,
            factory.getSourceDocResource(opts.getProj(), opts.getProjectVersion()),
            factory.getTranslatedDocResource(opts.getProj(), opts.getProjectVersion()),
            factory.getResourceURI(opts.getProj(), opts.getProjectVersion()));
   }

   public PushPullCommand(O opts)
   {
      this(opts, OptionsUtil.createRequestFactory(opts));
   }

   protected void confirmWithUser(String message) throws IOException
   {
      if (getOpts().isInteractiveMode())
      {
         Console console = System.console();
         if (console == null)
            throw new RuntimeException("console not available: please run Maven from a console, or use batch mode (mvn -B)");
         console.printf(message + "\nAre you sure (y/n)? ");
         expectYes(console);
      }
   }

   protected static void expectYes(Console console) throws IOException
   {
      String line = console.readLine();
      if (line == null)
         throw new IOException("console stream closed");
      if (!line.toLowerCase().equals("y") && !line.toLowerCase().equals("yes"))
         throw new RuntimeException("operation aborted by user");
   }

   protected void debug(Object jaxbElement)
   {
      try
      {
         if (getOpts().isDebugSet())
         {
            StringWriter writer = new StringWriter();
            getMarshaller().marshal(jaxbElement, writer);
            log.debug("{}", writer);
         }
      }
      catch (JAXBException e)
      {
         log.debug(e.toString(), e);
      }
   }

   /**
    * @return
    * @throws JAXBException 
    */
   private Marshaller getMarshaller() throws JAXBException
   {
      if (marshaller == null)
      {
         JAXBContext jc = JAXBContext.newInstance(Resource.class, TranslationsResource.class);
         marshaller = jc.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      }
      return marshaller;
   }

   protected String qualifiedDocName(String localDocName)
   {
      String qualifiedDocName = modulePrefix + localDocName;
      return qualifiedDocName;
   }

   protected String unqualifiedDocName(String qualifiedDocName)
   {
      assert qualifiedDocName.startsWith(modulePrefix);
      return qualifiedDocName.substring(modulePrefix.length());
   }

   protected boolean belongsToCurrentModule(String qualifiedDocName)
   {
      return qualifiedDocName.startsWith(modulePrefix);
   }

   protected List<String> getQualifiedDocNamesForCurrentModuleFromServer()
   {
      List<ResourceMeta> remoteDocList = getDocListForProjectIterationFromServer();
      List<String> docNames = new ArrayList<String>();
      for (ResourceMeta doc : remoteDocList)
      {
         // NB ResourceMeta.name = HDocument.docId
         String qualifiedDocName = doc.getName();
         if (getOpts().getEnableModules())
         {
            if (belongsToCurrentModule(qualifiedDocName))
            {
               docNames.add(qualifiedDocName);
            }
            else
            {
               log.debug("found extra-modular document: {}", qualifiedDocName);
            }
         }
         else
         {
            docNames.add(qualifiedDocName);
         }
      }
      return docNames;
   }

   // TODO use a cache which will be accessible to all invocations
   protected List<ResourceMeta> getDocListForProjectIterationFromServer()
   {
      ClientResponse<List<ResourceMeta>> getResponse = sourceDocResource.get(null);
      ClientUtility.checkResult(getResponse, uri);
      List<ResourceMeta> remoteDocList = getResponse.getEntity();
      return remoteDocList;
   }

}
