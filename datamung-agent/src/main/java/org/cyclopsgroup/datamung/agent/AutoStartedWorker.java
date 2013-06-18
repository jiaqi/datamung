package org.cyclopsgroup.datamung.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.SmartLifecycle;

public abstract class AutoStartedWorker
    implements SmartLifecycle
{
    private static final Log LOG = LogFactory.getLog( AutoStartedWorker.class );

    private volatile boolean running;

    abstract void doStart();

    abstract void doStop();

    /**
     * @inheritDoc
     */
    @Override
    public int getPhase()
    {
        return 0;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isAutoStartup()
    {
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isRunning()
    {
        return running;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void start()
    {
        doStart();
        running = true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stop()
    {
        doStop();
        stop( null );
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stop( Runnable callback )
    {
        if ( callback != null )
        {
            try
            {
                callback.run();
            }
            catch ( Throwable e )
            {
                LOG.warn( "Termination callback failed, " + e.getMessage(), e );
            }
        }
        running = false;
    }
}
