/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE
 * or https://OpenDS.dev.java.net/OpenDS.LICENSE.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at
 * trunk/opends/resource/legal-notices/OpenDS.LICENSE.  If applicable,
 * add the following below this CDDL HEADER, with the fields enclosed
 * by brackets "[]" replaced with your own identifying information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2015 ForgeRock AS
 */
package org.opends.server.loggers;

import static org.forgerock.json.fluent.JsonValue.*;

import java.util.List;
import java.util.Set;

import org.forgerock.audit.event.AccessAuditEventBuilder;
import org.forgerock.i18n.LocalizableMessageBuilder;
import org.forgerock.json.fluent.JsonValue;
import org.opends.server.core.SearchOperation;
import org.opends.server.types.AdditionalLogItem;
import org.opends.server.types.Control;
import org.opends.server.types.Operation;

/**
 * Builder for /audit/access events specific to OpenDJ.
 *
 * This builder add LDAP specific fields to the common fields defined in AccessAuditEventBuilder.
 */
class OpenDJAccessAuditEventBuilder<T extends OpenDJAccessAuditEventBuilder<T>> extends AccessAuditEventBuilder<T>
{

  private OpenDJAccessAuditEventBuilder()
  {
    super();
  }

  @SuppressWarnings("rawtypes")
  public static <T> OpenDJAccessAuditEventBuilder<?> openDJAccessEvent()
  {
    return new OpenDJAccessAuditEventBuilder();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected T self()
  {
    return (T) this;
  }

  public T ldapAdditionalItems(Operation op) {
    String items = getAdditionalItemsAsString(op);
    if (!items.isEmpty()) {
      JsonValue ldapValue = getLdapValue();
      ldapValue.put("additionalItems", items);
    }
    return self();

  }

  public T ldapConnectionId(long id)
  {
    JsonValue ldapValue = getLdapValue();
    ldapValue.put("connId", id);
    return self();
  }

  public T ldapControls(Operation operation)
  {
    JsonValue ldapValue = getLdapValue();
    List<Control> requestControls = operation.getRequestControls();
    if (!requestControls.isEmpty()) {
      ldapValue.put("reqControls", getControlsAsString(requestControls));
    }
    List<Control> responseControls = operation.getResponseControls();
    if (!responseControls.isEmpty()) {
      ldapValue.put("respControls", getControlsAsString(responseControls));
    }
    return self();
  }

  public T ldapDn(String dn) {
    JsonValue ldapValue = getLdapValue();
    ldapValue.put("dn", dn);
    return self();
  }

  public T ldapFailureMessage(String msg) {
    JsonValue ldapValue = getLdapValue();
    ldapValue.put("failureReason", msg);
    return self();
  }

  public T ldapIds(Operation op)
  {
    JsonValue ldapValue = getLdapValue();
    ldapValue.put("connId", op.getConnectionID());
    ldapValue.put("opId", op.getOperationID());
    ldapValue.put("msgId", op.getMessageID());
    return self();
  }

  public T ldapIdToAbandon(int id) {
    JsonValue ldapValue = getLdapValue();
    ldapValue.put("idToAbandon", id);
    return self();
  }

  public T ldapMaskedResultAndMessage(Operation operation) {
    JsonValue ldapValue = getLdapValue();
    if (operation.getMaskedResultCode() != null)
    {
      ldapValue.put("maskedResult", operation.getMaskedResultCode().intValue());
    }
    final LocalizableMessageBuilder maskedMsg = operation.getMaskedErrorMessage();
    if (maskedMsg != null && maskedMsg.length() > 0)
    {
      ldapValue.put("maskedMessage", maskedMsg.toString());
    }
    return self();
  }

  public T ldapMessage(String msg) {
    JsonValue ldapValue = getLdapValue();
    ldapValue.put("message", msg);
    return self();
  }

  public T ldapNEntries(int nbEntries) {
    JsonValue ldapValue = getLdapValue();
    ldapValue.put("nentries", nbEntries);
    return self();
  }

  public T ldapReason(String msg) {
    JsonValue ldapValue = getLdapValue();
    ldapValue.put("reason", msg);
    return self();
  }

  public T ldapSearch(SearchOperation searchOperation) {
    JsonValue ldapValue = getLdapValue();
    JsonValue searchValue = json(object());
    ldapValue.put("search", searchValue);

    searchValue.put("base", searchOperation.getRawBaseDN().toString());
    searchValue.put("scope", searchOperation.getScope().toString());
    searchValue.put("filter", searchOperation.getRawFilter().toString());

    final Set<String> attrs = searchOperation.getAttributes();
    final StringBuilder attrsValue = new StringBuilder();
    if ((attrs == null) || attrs.isEmpty())
    {
      attrsValue.append("ALL");
    }
    else
    {
      for (String attr : attrs)
      {
        if (attrsValue.length() > 0) {
          attrsValue.append(" ");
        }
        attrsValue.append(attr);
      }
    }
    searchValue.put("attrs", attrsValue.toString());
    return self();
  }

  public T ldapSync(Operation operation) {
    if (operation.isSynchronizationOperation())
    {
      JsonValue ldapValue = getLdapValue();
      ldapValue.put("type", "synchronized");
    }
    return self();
  }

  private String getControlsAsString(List<Control> controls)
  {
    StringBuilder buffer = new StringBuilder();
    boolean isFirst = true;
    for (final Control control : controls)
    {
      if (!isFirst)
      {
        buffer.append(",");
      }
      buffer.append(control.getOID());
      isFirst = false;
    }
    return buffer.toString();
  }

  private String getAdditionalItemsAsString(Operation operation) {
    StringBuilder items = new StringBuilder();
    for (final AdditionalLogItem item : operation.getAdditionalLogItems())
    {
      items.append(' ');
      item.toString(items);
    }
    return items.toString();
  }

  private JsonValue getLdapValue()
  {
    JsonValue ldapValue = null;
    if (jsonValue.isDefined("ldap"))
    {
      ldapValue = jsonValue.get("ldap");
    }
    else
    {
      ldapValue = json(object());
      jsonValue.put("ldap", ldapValue);
    }
    return ldapValue;
  }



}
