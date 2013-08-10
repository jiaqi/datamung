package org.cyclopsgroup.datamung.service.core;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.cyclopsgroup.datamung.swf.interfaces.Description;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.Activity;

class ActivityInvocationDescriber
{
    private final Map<String, Description> invocationMap;

    private final VelocityEngine velocity = new VelocityEngine();

    ActivityInvocationDescriber( Class<?>... activityTypes )
    {
        Map<String, Description> map = new HashMap<String, Description>();
        for ( Class<?> activityType : activityTypes )
        {
            Activities activities =
                activityType.getAnnotation( Activities.class );
            if ( activities == null )
            {
                throw new IllegalArgumentException( "Type " + activityType
                    + " is not annotated with " + Activities.class );
            }
            String prefix =
                StringUtils.isBlank( activities.activityNamePrefix() ) ? ( activityType.getSimpleName() + "." )
                                : activities.activityNamePrefix();
            for ( Method method : activityType.getMethods() )
            {
                Description desc = method.getAnnotation( Description.class );
                if ( desc == null )
                {
                    continue;
                }
                String name = method.getName();
                Activity activity = method.getAnnotation( Activity.class );
                if ( activity != null
                    && StringUtils.isNotBlank( activity.name() ) )
                {
                    name = activity.name();
                }
                map.put( prefix + name, desc );
            }
        }
        this.invocationMap = Collections.unmodifiableMap( map );
    }

    String describeInvocation( String activityName, List<Object> parameters )
    {
        Description i = invocationMap.get( activityName );
        if ( i == null )
        {
            return null;
        }
        Map<String, Object> context = new HashMap<String, Object>();
        context.put( "params", parameters );
        StringWriter out = new StringWriter();
        velocity.evaluate( new VelocityContext( context ), out, "internal",
                           i.value() );
        return out.toString();
    }

    String describeResult( String activityName, Object result )
    {
        Description i = invocationMap.get( activityName );
        if ( i == null || StringUtils.isBlank( i.result() ) )
        {
            return null;
        }
        Map<String, Object> context = new HashMap<String, Object>();
        context.put( "result", result );
        StringWriter out = new StringWriter();
        velocity.evaluate( new VelocityContext( context ), out, "internal",
                           i.value() );
        return out.toString();
    }

    boolean hasActivity( String activityName )
    {
        return invocationMap.containsKey( activityName );
    }
}
