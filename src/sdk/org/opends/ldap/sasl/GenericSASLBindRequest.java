package org.opends.ldap.sasl;



import org.opends.server.types.ByteString;
import org.opends.server.util.Validator;
import org.opends.spi.AbstractBindRequest;



/**
 * Created by IntelliJ IDEA. User: digitalperk Date: May 26, 2009 Time:
 * 7:46:51 PM To change this template use File | Settings | File
 * Templates.
 */
public class GenericSASLBindRequest extends SASLBindRequest
{
  // The SASL credentials.
  private ByteString saslCredentials;

  // The SASL mechanism.
  private String saslMechanism;



  public GenericSASLBindRequest(String saslMechanism,
      ByteString saslCredentials)
  {
    Validator.ensureNotNull(saslMechanism);
    this.saslCredentials = saslCredentials;
    this.saslMechanism = saslMechanism;
  }



  /**
   * Returns the SASL credentials for this bind request.
   *
   * @return The SASL credentials for this bind request, or {@code null}
   *         if there are none or if the bind does not use SASL
   *         authentication.
   */
  @Override
  public ByteString getSASLCredentials()
  {
    return saslCredentials;
  }



  /**
   * Returns the SASL mechanism for this bind request.
   *
   * @return The SASL mechanism for this bind request, or {@code null}
   *         if there are none or if the bind does not use SASL
   *         authentication.
   */
  @Override
  public String getSASLMechanism()
  {
    return saslMechanism;
  }



  /**
   * Sets the SASL credentials for this bind request.
   *
   * @param saslCredentials
   *          The SASL credentials for this bind request, or {@code
   *          null} if there are none or if the bind does not use SASL
   *          authentication.
   * @return This raw bind request.
   */
  public AbstractBindRequest setSASLCredentials(ByteString saslCredentials)
  {
    this.saslCredentials = saslCredentials;
    return this;
  }



  /**
   * Sets The SASL mechanism for this bind request.
   *
   * @param saslMechanism
   *          The SASL mechanism for this bind request, or {@code null}
   *          if there are none or if the bind does not use SASL
   *          authentication.
   * @return This raw bind request.
   */
  public AbstractBindRequest setSASLMechanism(String saslMechanism)
  {
    Validator.ensureNotNull(saslMechanism);
    this.saslMechanism = saslMechanism;
    return this;
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public StringBuilder toString(StringBuilder buffer)
  {
    buffer.append("SASLBindRequest(bindDN=");
    buffer.append(getBindDN());
    buffer.append(", authentication=SASL");
    buffer.append(", saslMechanism=");
    buffer.append(saslMechanism);
    buffer.append(", saslCredentials=");
    buffer.append(saslCredentials);
    buffer.append(", controls=");
    buffer.append(getControls());
    buffer.append(")");
    return buffer;
  }
}
