package org.zanata.maven;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.zanata.client.commands.PushPullCommand;
import org.zanata.client.commands.PushPullOptions;
import org.zanata.client.config.LocaleList;

/**
 * @requiresOnline true
 * @author Sean Flanigan <sflaniga@redhat.com>
 */
public abstract class AbstractPushPullMojo<O extends PushPullOptions> extends ConfigurableProjectMojo<O> implements PushPullOptions
{
   protected static final String PROJECT_TYPE_FILE = "file";

   /**
    * Separator used between components of the module ID
    */
   private static final char MODULE_SEPARATOR = '/';

   private static final char MODULE_SUFFIX = '/';

   @Override
   protected void runCommand() throws Exception
   {
      if (skip)
      {
         getLog().info("skipping");
         return;
      }
      super.runCommand();
   }

   @Override
   public boolean isRootModule()
   {
      return project.isExecutionRoot();
   }

   @Override
   public String getCurrentModule()
   {
      if (project == null || !getEnableModules())
      {
         return "";
      }
      else
      {
         return toModuleID(project);
      }
   }

   @Override
   public String getModuleSuffix()
   {
      return "" + MODULE_SUFFIX;
   }

   @Override
   public String getDocNameRegex()
   {
      return "^([^/]+/[^" + MODULE_SUFFIX + "]+)" + MODULE_SUFFIX + "(.+)";
   }

   //   @Override
   public Set<String> getAllModules()
   {
      Set<String> localModules = new LinkedHashSet<String>();
      for (MavenProject module : reactorProjects)
      {
         String modID = toModuleID(module);
         localModules.add(modID);
      }
      return localModules;
   }

   private String toModuleID(MavenProject module)
   {
      return module.getGroupId() + MODULE_SEPARATOR + module.getArtifactId();
   }

   /**
    * @parameter expression="${project}"
    * @readonly
    */
   private MavenProject project;

   /**
    * Dry run: don't change any data, on the server or on the filesystem.
    * @parameter expression="${dryRun}"
    */
   private boolean dryRun = false;

   /**
    * @parameter expression="${zanata.skip}"
    */
   private boolean skip;

   /**
    * The projects in the reactor.
    *
    * @parameter expression="${reactorProjects}"
    * @readonly
    */
   private List<MavenProject> reactorProjects;

   /**
    * Base directory for source-language files
    * 
    * @parameter expression="${zanata.srcDir}" default-value="."
    */
   private File srcDir;

   /**
    * Base directory for target-language files (translations)
    * 
    * @parameter expression="${zanata.transDir}" default-value="."
    */
   private File transDir;

   /**
    * Specifies a document from which to begin the push operation.
    * Documents before this document (sorted alphabetically) will not be pushed.
    * Use this option to resume a failed push operation.

    * @parameter expression="${zanata.fromDoc}"
    */
   private String fromDoc;

   /**
    * Locales to push to/pull from the server.
    * By default all locales in zanata.xml will be pushed/pulled.
    * Usage: -Dzanata.locales=locale1,locale2,locale3
    *
    * @parameter expression="${zanata.locales}"
    */
   private String[] locales;

   private LocaleList effectiveLocales;

   public AbstractPushPullMojo()
   {
      super();
   }

   /**
    * @return the dryRun
    */
   @Override
   public boolean isDryRun()
   {
      return dryRun;
   }

   @Override
   public File getSrcDir()
   {
      return srcDir;
   }

   @Override
   public String getSrcDirParameterName()
   {
      return "srcDir";
   }

   @Override
   public File getTransDir()
   {
      return transDir;
   }

   @Override
   public String getFromDoc()
   {
      return fromDoc;
   }

   @Override
   public String buildFromDocArgument(String argValue)
   {
      return "-Dzanata.fromDoc=\"" + argValue + "\"";
   }

   /**
    * Override the default {@link org.zanata.maven.ConfigurableProjectMojo#getLocaleMapList()} method as the push
    * command can have locales specified via command line.
    *
    * @return The locale map list taking into account the global locales in zanata.xml as well as the command line
    * argument ones.
    */
   @Override
   public LocaleList getLocaleMapList()
   {
      if( effectiveLocales == null )
      {
         effectiveLocales = PushPullCommand.getLocaleMapList(super.getLocaleMapList(), locales);
      }

      return effectiveLocales;
   }

}
