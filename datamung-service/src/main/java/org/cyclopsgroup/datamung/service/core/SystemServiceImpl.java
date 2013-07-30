package org.cyclopsgroup.datamung.service.core;

import org.springframework.stereotype.Component;

@Component( "systemService" )
public class SystemServiceImpl
    implements SystemService
{
    /**
     * @inheritDoc
     */
    @Override
    public String ping()
    {
        return "shazoooooo!";
    }
}
