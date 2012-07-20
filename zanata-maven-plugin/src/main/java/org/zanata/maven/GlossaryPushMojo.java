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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zanata.client.commands.glossary.push.GlossaryPushCommand;
import org.zanata.client.commands.glossary.push.GlossaryPushOptions;

/**
 * Pushes glossary file into Zanata.
 * 
 * @goal glossary-push
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class GlossaryPushMojo extends ConfigurableProjectMojo<GlossaryPushOptions> implements GlossaryPushOptions
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
    * Treat source comments and references as target comments in glossary file
    * as translation comment
    * 
    * @parameter expression="${zanata.treatSourceCommentsAsTarget}"
    *            default-value="false"
    */
   private boolean treatSourceCommentsAsTarget = false;

   /**
    * Customized comment column headers for csv file format. Format of CSV:
    * {source locale},{locale},{locale}...,pos,description OR {source
    * locale},{locale},{locale}...,description1,description2...
    * 
    * @parameter expression="${zanata.commentCols}"
    *            default-value="pos,description"
    */
   private String commentCols = "pos,description";

   /**
    * Batch size for large glossary file
    * 
    * @parameter expression="${zanata.batchSize}" default-value=50
    */
   private int batchSize = 50;


   public GlossaryPushMojo() throws Exception
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
   public GlossaryPushCommand initCommand()
   {
      return new GlossaryPushCommand(this);
   }

   @Override
   public String getTransLang()
   {
      return transLang;
   }

   @Override
   public boolean getTreatSourceCommentsAsTarget()
   {
      return treatSourceCommentsAsTarget;
   }

   @Override
   public List<String> getCommentCols()
   {
      String[] commentHeadersList = StringUtils.split(commentCols, ",");
      List<String> list = new ArrayList<String>();
      if (commentHeadersList != null && commentHeadersList.length > 0)
      {
         Collections.addAll(list, commentHeadersList);
      }
      return list;
   }

   @Override
   public int getBatchSize()
   {
      return batchSize;
   }

   @Override
   public String getCommandName()
   {
      return "glossary-push";
   }
}


 