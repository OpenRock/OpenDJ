package org.opends.client.protocol.sasl;

import org.opends.common.protocols.sasl.SASLFilter;
import org.opends.common.protocols.sasl.SASLStreamReader;
import org.opends.common.protocols.sasl.SASLStreamWriter;
import org.opends.common.utils.ByteArrayWrapper;
import com.sun.grizzly.attributes.Attribute;
import com.sun.grizzly.attributes.AttributeBuilder;
import com.sun.grizzly.streams.StreamReader;
import com.sun.grizzly.streams.StreamWriter;
import com.sun.grizzly.Buffer;
import com.sun.grizzly.Connection;
import com.sun.grizzly.filterchain.NextAction;
import com.sun.grizzly.filterchain.FilterChainContext;

import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 3, 2009 Time: 2:14:23
 * PM To change this template use File | Settings | File Templates.
 */
public final class SASLClientFilter extends SASLFilter
{
  private static final String SASL_CLIENT_ATTR_NAME = "SASLClientAttr";
  private static SASLClientFilter SINGLETON = new SASLClientFilter();

  private final Attribute<SaslClient> saslClientAttribute;

  private SASLClientFilter()
  {
    AttributeBuilder attrBuilder = getAttributeBuilder();
    saslClientAttribute =
        attrBuilder.createAttribute(SASL_CLIENT_ATTR_NAME);
  }

  public static SASLClientFilter getInstance(SaslClient saslClient,
                                             Connection connection)
  {
    SINGLETON.saslClientAttribute.set(connection, saslClient);
    return SINGLETON;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public NextAction postClose(FilterChainContext ctx, NextAction nextAction)
          throws IOException {
      saslClientAttribute.remove(ctx.getConnection());
      return super.postClose(ctx, nextAction);
  }


  public StreamReader getStreamReader(StreamReader parentStreamReader)
  {
    return new SASLStreamReader(parentStreamReader, this);
  }

  public StreamWriter getStreamWriter(StreamWriter parentStreamWriter)
  {
    return new SASLStreamWriter(parentStreamWriter, this);
  }

  public Buffer unwrap(Buffer incoming, Connection connection)
      throws SaslException
  {
    SaslClient saslClient = saslClientAttribute.get(connection);
    byte[] incomingBuffer =
        obtainIncomingBuffer(incoming.capacity(), connection);
    int remaining = incoming.remaining();

    incoming.get(incomingBuffer, 0, remaining);
    byte[] appBuffer = saslClient.unwrap(incomingBuffer, 0, remaining);
    return ByteArrayWrapper.wrap(appBuffer);
  }

  public Buffer wrap(Buffer outgoing, Connection connection)
      throws SaslException
  {
    SaslClient saslClient = saslClientAttribute.get(connection);
    byte[] outgoingBuffer =
        obtainOutgoingBuffer(outgoing.capacity(), connection);
    int remaining = outgoing.remaining();

    outgoing.get(outgoingBuffer, 0, remaining);
    byte[] netBuffer = saslClient.wrap(outgoingBuffer, 0, remaining);
    return ByteArrayWrapper.wrap(netBuffer);
  }
}
