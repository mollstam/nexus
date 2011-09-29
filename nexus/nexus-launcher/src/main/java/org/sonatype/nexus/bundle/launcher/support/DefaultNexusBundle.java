/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bundle.launcher.support;

import org.sonatype.nexus.bundle.launcher.NexusBundle;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.sisu.bl.support.DefaultWebBundle;
import org.sonatype.sisu.bl.support.jsw.JSWExec;
import org.sonatype.sisu.bl.support.jsw.JSWExecFactory;
import org.sonatype.sisu.filetasks.FileTaskBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.sisu.filetasks.FileTaskRunner.onDirectory;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

/**
 * Default Nexus bundle implementation.
 *
 * @since 1.10.0
 */
@Named
public class DefaultNexusBundle
    extends DefaultWebBundle<NexusBundle, NexusBundleConfiguration>
    implements NexusBundle
{

    /**
     * JSW utility factory.
     * Cannot be null.
     */
    private JSWExecFactory jswExecFactory;

    /**
     * JSW utility to star/stop Nexus.
     * Lazy created.
     */
    private JSWExec jswExec;

    /**
     * File task builder.
     * Cannot be null.
     */
    private FileTaskBuilder fileTaskBuilder;

    /**
     * Constructor.
     *
     * @param jswExecFactory JSW executor factory.
     * @since 1.10.0
     */
    @Inject
    public DefaultNexusBundle( JSWExecFactory jswExecFactory,
                               FileTaskBuilder fileTaskBuilder )
    {
        super( "nexus" );
        this.fileTaskBuilder = fileTaskBuilder;
        this.jswExecFactory = checkNotNull( jswExecFactory );
    }

    /**
     * Aditionaly configures Nexus/Jetty port and installs plugins.
     * <p/>
     * {@inheritDoc}
     *
     * @since 1.10.0
     */
    @Override
    public void doPrepare()
    {
        super.doPrepare();
        installPlugins();
        configureNexusPort();
    }

    /**
     * Starts Nexus using JSW.
     * <p/>
     * {@inheritDoc}
     *
     * @since 1.10.0
     */
    @Override
    protected void startApplication()
    {
        jswExec().start();
    }

    /**
     * Stops Nexus using JSW.
     * <p/>
     * {@inheritDoc}
     *
     * @since 1.10.0
     */
    @Override
    protected void stopApplication()
    {
        jswExec().stop();
    }

    /**
     * Checks if Nexus is alive by using REST status service.
     *
     * @return true if Nexus is alive
     * @since 1.10.0
     */
    @Override
    protected boolean applicationAlive()
    {
        try
        {
            return RequestUtils.isNexusRESTStarted( getUrl().toExternalForm() );
        }
        catch ( IOException ignore )
        {
            return false;
        }
    }

    /**
     * Lazy creates and returns JSW utility.
     *
     * @return JSW utility (never null)
     */
    private JSWExec jswExec()
    {
        if ( jswExec == null )
        {
            jswExec = jswExecFactory.create( getConfiguration().getTargetDirectory(), "nexus" );
        }
        return jswExec;
    }

    /**
     * Install Nexus plugins in {@code sonatype-work/nexus/plugin-repository}.
     */
    private void installPlugins()
    {
        NexusBundleConfiguration config = getConfiguration();
        List<File> plugins = config.getPlugins();
        for ( File plugin : plugins )
        {
            if ( plugin.isDirectory() )
            {
                onDirectory( config.getTargetDirectory() ).apply(
                    fileTaskBuilder.copy()
                        .directory( file( plugin ) )
                        .to().directory( path( "sonatype-work/nexus/plugin-repository" ) )
                );
            }
            else
            {
                onDirectory( config.getTargetDirectory() ).apply(
                    fileTaskBuilder.expand( file( plugin ) )
                        .to().directory( path( "sonatype-work/nexus/plugin-repository" ) )
                );
            }
        }
    }

    /**
     * Configures "application-port" nexus property.
     */
    private void configureNexusPort()
    {
        onDirectory( getConfiguration().getTargetDirectory() ).apply(
            fileTaskBuilder.properties( path( "nexus/conf/nexus.properties" ) )
                .property( "application-port", String.valueOf( getPort() ) )
        );
    }

}