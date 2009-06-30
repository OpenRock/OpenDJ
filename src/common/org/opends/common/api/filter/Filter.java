package org.opends.common.api.filter;

import org.opends.common.protocols.asn1.ASN1Writer;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: digitalperk Date: Jun 10, 2009 Time: 11:34:45
 * AM To change this template use File | Settings | File Templates.
 */
public abstract class Filter
{
  public abstract void encodeLDAP(ASN1Writer writer) throws IOException;

  /**
   * Returns a string representation of this request.
   *
   * @return A string representation of this request.
   */
  @Override
  public final String toString()
  {
    StringBuilder builder = new StringBuilder();
    toString(builder);
    return builder.toString();
  }



  /**
   * Appends a string representation of this request to the provided
   * buffer.
   *
   * @param buffer
   *          The buffer into which a string representation of this
   *          request should be appended.
   */
  public abstract void toString(StringBuilder buffer);
}
