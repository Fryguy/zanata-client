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
package org.zanata.maven;

import java.io.File;

import org.zanata.client.commands.pushGlossary.PushGlossaryCommand;
import org.zanata.client.commands.pushGlossary.PushGlossaryOptions;

/**
 * Pushes glossary file into Zanata.
 * 
 * @goal push-glossary
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class PushGlossaryMojo extends ConfigurableProjectMojo implements PushGlossaryOptions
{

   /**
    * Source language of document
    * 
    * @parameter expression="${zanata.sourceLang}" default-value="en-US"
    */
   private String sourceLang = "en-US";


   /**
    * Translation language of document. Not required for csv file
    * 
    * @parameter expression="${zanata.transLang}"
    */
   private String transLang;

   /**
    * Location path for the glossary file
    * 
    * @parameter expression="${zanata.glossaryFile}"
    * @required
    */
   private File glossaryFile;

   /**
    * Treat all comments and source reference in glossary file as translation
    * comment
    * 
    * @parameter expression="${zanata.allTransComments}" default-value="false"
    */
   private boolean allTransComments = false;


   public PushGlossaryMojo() throws Exception
   {
      super();
   }

   @Override
   public File getGlossaryFile()
   {
      return glossaryFile;
   }


   @Override
   public String getSourceLang()
   {
      return sourceLang;
   }

   @Override
   public PushGlossaryCommand initCommand()
   {
      return new PushGlossaryCommand(this);
   }

   @Override
   public String getTransLang()
   {
      return transLang;
   }

   @Override
   public boolean getAllTransComments()
   {
      return allTransComments;
   }

}


 