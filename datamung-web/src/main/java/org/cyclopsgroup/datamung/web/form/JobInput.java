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
    extends CredentialsAndAction
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

    private SourceAndDestination sourceAndDestination;

    private WorkerInstanceOptions workerInstanceOptions;

    public SourceAndDestination getSourceAndDestination()
    {
        return sourceAndDestination;
    }

    public WorkerInstanceOptions getWorkerInstanceOptions()
    {
        return workerInstanceOptions;
    }

    public String serializeTo()
        throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream( out );
        try
        {
            zip.write( CONVERTER.toData( this ).getBytes() );
            zip.flush();
            zip.close();
            return new String( Base64.encodeBase64( out.toByteArray() ) );
        }
        finally
        {
            IOUtils.closeQuietly( zip );
        }
    }

    public void setSourceAndDestination( SourceAndDestination sourceAndDestination )
    {
        this.sourceAndDestination = sourceAndDestination;
    }

    public void setWorkerInstanceOptions( WorkerInstanceOptions workerInstanceOptions )
    {
        this.workerInstanceOptions = workerInstanceOptions;
    }
}
