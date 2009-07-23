package org.opends.ldap.controls;



import static org.opends.messages.ProtocolMessages.*;
import static org.opends.server.util.ServerConstants.*;
import static org.opends.server.util.StaticUtils.*;

import java.io.IOException;

import org.opends.asn1.ASN1;
import org.opends.asn1.ASN1Reader;
import org.opends.asn1.ASN1Writer;
import org.opends.ldap.DecodeException;
import org.opends.messages.Message;
import org.opends.server.types.ByteString;
import org.opends.server.types.ByteStringBuilder;
import org.opends.server.util.Validator;
import org.opends.spi.ControlDecoder;
import org.opends.types.filter.Filter;



/**
 * Created by IntelliJ IDEA. User: boli Date: Jun 29, 2009 Time: 4:01:44
 * PM To change this template use File | Settings | File Templates.
 */
public class AssertionControl extends Control
{
  /**
   * ControlDecoder implentation to decode this control from a
   * ByteString.
   */
  private final static class Decoder implements
      ControlDecoder<AssertionControl>
  {
    /**
     * {@inheritDoc}
     */
    public AssertionControl decode(boolean isCritical, ByteString value)
        throws DecodeException
    {
      if (value == null)
      {
        Message message = ERR_LDAPASSERT_NO_CONTROL_VALUE.get();
        throw new DecodeException(message);
      }

      ASN1Reader reader = ASN1.getReader(value);
      Filter filter;
      try
      {
        filter = Filter.decode(reader);
      }
      catch (IOException e)
      {
        throw new DecodeException(ERR_LDAPASSERT_INVALID_CONTROL_VALUE
            .get(getExceptionMessage(e)), e);
      }

      return new AssertionControl(isCritical, filter);
    }



    public String getOID()
    {
      return OID_LDAP_ASSERTION;
    }

  }



  /**
   * The Control Decoder that can be used to decode this control.
   */
  public static final ControlDecoder<AssertionControl> DECODER =
      new Decoder();

  // The unparsed LDAP search filter contained in the request from the
  // client.
  private final Filter rawFilter;



  /**
   * Creates a new instance of this LDAP assertion request control with
   * the provided information.
   * 
   * @param isCritical
   *          Indicates whether support for this control should be
   *          considered a critical part of the server processing.
   * @param rawFilter
   *          The unparsed LDAP search filter contained in the request
   *          from the client.
   */
  public AssertionControl(boolean isCritical, Filter rawFilter)
  {
    super(OID_LDAP_ASSERTION, isCritical);

    Validator.ensureNotNull(rawFilter);
    this.rawFilter = rawFilter;
  }



  /**
   * Retrieves the raw, unparsed filter from the request control.
   * 
   * @return The raw, unparsed filter from the request control.
   */
  public Filter getRawFilter()
  {
    return rawFilter;
  }



  @Override
  public ByteString getValue()
  {
    ByteStringBuilder buffer = new ByteStringBuilder();
    ASN1Writer writer = ASN1.getWriter(buffer);
    try
    {
      rawFilter.encode(writer);
      return buffer.toByteString();
    }
    catch (IOException ioe)
    {
      // This should never happen unless there is a bug somewhere.
      throw new RuntimeException(ioe);
    }
  }



  @Override
  public boolean hasValue()
  {
    return true;
  }



  /**
   * Appends a string representation of this LDAP assertion request
   * control to the provided buffer.
   * 
   * @param buffer
   *          The buffer to which the information should be appended.
   */
  @Override
  public void toString(StringBuilder buffer)
  {
    buffer.append("AssertionControl(oid=");
    buffer.append(getOID());
    buffer.append(", criticality=");
    buffer.append(isCritical());
    buffer.append(", filter=\"");
    rawFilter.toString(buffer);
    buffer.append("\")");
  }
}
