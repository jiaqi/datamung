package org.cyclopsgroup.datamung.web.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;

public class JobInput
{
    private static final JsonDataConverter CONVERTER = new JsonDataConverter();

    public static JobInput deserializeFrom( String input )
        throws IOException
    {
        InputStream in =
            new ByteArrayInputStream( Base64.decodeBase64( input.getBytes() ) );
        GZIPInputStream zip = new GZIPInputStream( in );
        try
        {
            return CONVERTER.fromData( IOUtils.toString( zip ), JobInput.class );
        }
        finally
        {
            IOUtils.closeQuietly( zip );
        }
    }

    private ActionType actionType;

    private CredentialsAndAction credsAndAction;

    private SourceAndDestination sourceAndDestination;

    public ActionType getActionType()
    {
        return actionType;
    }

    public CredentialsAndAction getCredsAndAction()
    {
        return credsAndAction;
    }

    public SourceAndDestination getSourceAndDestination()
    {
        return sourceAndDestination;
    }

    public String serializeTo()
        throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream( out );
        try
        {
            out.write( CONVERTER.toData( this ).getBytes() );
            zip.flush();
            zip.close();
            return new String( Base64.encodeBase64( out.toByteArray() ) );
        }
        finally
        {
            IOUtils.closeQuietly( zip );
        }
    }

    public void setActionType( ActionType actionType )
    {
        this.actionType = actionType;
    }

    public void setCredsAndAction( CredentialsAndAction credsAndAction )
    {
        this.credsAndAction = credsAndAction;
    }

    public void setSourceAndDestination( SourceAndDestination sourceAndDestination )
    {
        this.sourceAndDestination = sourceAndDestination;
    }
}
