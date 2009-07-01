package org.opends.common.api.request;

import org.opends.server.types.ByteString;

/**
 * Created by IntelliJ IDEA.
 * User: boli
 * Date: Jul 1, 2009
 * Time: 12:52:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SASLBindRequest extends Request
{
  ByteString getSASLCredentials();

  String getSASLMechanism();
}
