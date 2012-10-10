package org.zanata.rest.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.RestConstant;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.rest.service.CopyTransResource;
import org.zanata.rest.service.StatisticsResource;

import javax.ws.rs.core.Response;

public class ZanataProxyFactory implements ITranslationResourcesFactory
{
   private String clientVersion;
   private String serverVersion;

   static
   {
      ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(instance);
   }

   private static final Logger log = LoggerFactory.getLogger(ZanataProxyFactory.class);
   private final ClientRequestFactory crf;
   private static final String RESOURCE_PREFIX = "rest";

   public ZanataProxyFactory(String username, String apiKey, VersionInfo clientApiVersion)
   {
      this(null, username, apiKey, clientApiVersion);
   }

   public ZanataProxyFactory(URI base, String username, String apiKey, VersionInfo clientApiVersion)
   {
      this(base, username, apiKey, null, clientApiVersion, false);
   }
   
   public ZanataProxyFactory(URI base, String username, String apiKey, VersionInfo clientApiVersion, boolean logHttp)
   {
      this(base, username, apiKey, null, clientApiVersion, logHttp);
   }

   public ZanataProxyFactory(URI base, String username, String apiKey, ClientExecutor executor, VersionInfo clientApiVersion,
                             boolean logHttp)
   {
      crf = new ClientRequestFactory(executor, null, fixBase(base));
      registerPrefixInterceptor(new TraceDebugInterceptor(logHttp));
      registerPrefixInterceptor(new ApiKeyHeaderDecorator(username, apiKey, clientApiVersion.getVersionNo()));
      
      clientVersion = clientApiVersion.getVersionNo();
      String clientTimestamp = clientApiVersion.getBuildTimeStamp();
      IVersionResource iversion = createIVersionResource();
      ClientResponse<VersionInfo> versionResp = iversion.get();
      VersionInfo serverVersionInfo = null;
      // unauthorized
      if( versionResp.getResponseStatus() == Response.Status.UNAUTHORIZED )
      {
         throw new RuntimeException("Incorrect username/password");
      }
      else
      {
         serverVersionInfo = versionResp.getEntity();
      }
      serverVersion = serverVersionInfo.getVersionNo();
      String serverTimestamp = serverVersionInfo.getBuildTimeStamp();

      log.info("client API version: {}, server API version: {}", clientVersion, serverVersion);
      if (!serverVersion.equals(clientVersion))
      {
         log.warn("client API version is {}, but server API version is {}", clientVersion, serverVersion);
      }
      else if (serverVersion.contains(RestConstant.SNAPSHOT_VERSION) && !serverTimestamp.equalsIgnoreCase(clientTimestamp))
      {
         log.warn("client API timestamp is {}, but server API timestamp is {}", clientTimestamp, serverTimestamp);
      }
   }

   public <T> T createProxy(Class<T> clazz, URI baseUri)
   {
      log.debug("{} proxy uri: {}", clazz.getSimpleName(), baseUri);
      T proxy = crf.createProxy(clazz, baseUri);
      //      CacheFactory.makeCacheable(proxy);
      return proxy;
   }

   /**
    * Returns a client proxy, provided all information is on the proxied interface.
    * (i.e. The interface is marked with a {@link javax.ws.rs.Path} annotation.)
    *
    * @param clazz Client interface to proxy.
    * @return Client proxy for the class.
    * @see {@link ZanataProxyFactory#createProxy(Class, java.net.URI)}
    */
   public <T> T createProxy(Class<T> clazz)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX );
         return createProxy(clazz, url.toURI());
      }
      catch(MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   private static URI fixBase(URI base)
   {
      if (base != null)
      {
         String baseString = base.toString();
         if (!baseString.endsWith("/"))
         {
            try
            {
               URI result = new URI(baseString + "/");
               log.warn("Appending '/' to base URL '{}': using '{}'", baseString, result);
               return result;
            }
            catch (URISyntaxException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
      return base;
   }

   public IGlossaryResource getGlossaryResource()
   {
      return createProxy(IGlossaryResource.class, getGlossaryResourceURI());
   }

   public URI getGlossaryResourceURI()
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/glossary");
         return url.toURI();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   public IAccountResource getAccount(String username)
   {
      return createProxy(IAccountResource.class, getAccountURI(username));
   }

   public URI getAccountURI(String username)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/accounts/u/" + username);
         return url.toURI();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   public IProjectResource getProject(String proj)
   {
      return createProxy(IProjectResource.class, getProjectURI(proj));
   }


   public URI getProjectURI(String proj)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/projects/p/" + proj);
         return url.toURI();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   public IProjectIterationResource getProjectIteration(String proj, String iter)
   {
      return createProxy(IProjectIterationResource.class, getProjectIterationURI(proj, iter));
   }

   public URI getProjectIterationURI(String proj, String iter)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/projects/p/" + proj + "/iterations/i/" + iter);
         return url.toURI();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   // NB IProjectsResource is not currently used in Java
   public IProjectsResource getProjects(final URI uri)
   {
      return createProxy(IProjectsResource.class, uri);
   }

   @Override
   public ITranslatedDocResource getTranslatedDocResource(String projectSlug, String versionSlug)
   {
      return createProxy(ITranslatedDocResource.class, getResourceURI(projectSlug, versionSlug));
   }

   public ISourceDocResource getSourceDocResource(String projectSlug, String versionSlug)
   {
      return createProxy(ISourceDocResource.class, getResourceURI(projectSlug, versionSlug));
   }

   public IFileResource getFileResource()
   {
      return createProxy(IFileResource.class, getFileURI() );
   }

   private URI getFileURI()
   {
      String spec = RESOURCE_PREFIX + "/file/";
      try
      {
         return new URL(crf.getBase().toURL(), spec).toURI();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         log.error("URI Syntax error. Part of url: {}", spec);
         throw new RuntimeException(e);
      }
   }

   public StatisticsResource getStatisticsResource()
   {
      // NB: No specific client interface (not needed)
      return createProxy(StatisticsResource.class);
   }

   public CopyTransResource getCopyTransResource()
   {
      // NB: No specific client interface (not needed)
      return createProxy(CopyTransResource.class);
   }


   @Override
   public URI getResourceURI(String projectSlug, String versionSlug)
   {
      String spec = RESOURCE_PREFIX + "/projects/p/" + projectSlug + "/iterations/i/" + versionSlug + "/r";
      try
      {
         return new URL(crf.getBase().toURL(), spec).toURI();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         String msg = "URI Syntax error. Please make sure your project (project ID) and version are correct.";
         log.error(msg);
         log.error("part of your url: {}", spec);
         throw new RuntimeException(msg);
      }
   }

   /**
    * @see org.jboss.resteasy.client.core.ClientInterceptorRepositoryImpl#registerInterceptor(Object)
    * @param interceptor
    */
   public void registerPrefixInterceptor(Object interceptor)
   {
      crf.getPrefixInterceptors().registerInterceptor(interceptor);
   }
   
   protected IVersionResource createIVersionResource()
   {
      URL url;
      try
      {
         url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/version");
         return createProxy(IVersionResource.class, url.toURI());
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Compares a given version identifier with the server version.
    *
    * @param version The version to against which to compare the server version.
    * @return A positive integer if the server version is greater than the given version.
    * A negative integer if the server version is less than the given version.
    * 0 if both versions are the same.
    */
   public int compareToServerVersion( String version )
   {
      DefaultArtifactVersion srvVersion = new DefaultArtifactVersion(serverVersion);
      DefaultArtifactVersion providedVersion = new DefaultArtifactVersion(version);

      return srvVersion.compareTo( providedVersion );
   }


}

