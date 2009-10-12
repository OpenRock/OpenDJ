package org.opends.sdk.schema.syntaxes;

import org.opends.sdk.schema.Schema;
import static org.opends.sdk.schema.StringPrepProfile.prepareUnicode;
import static org.opends.sdk.schema.StringPrepProfile.TRIM;
import static org.opends.sdk.schema.StringPrepProfile.CASE_FOLD;
import static org.opends.sdk.schema.SchemaConstants.*;
import org.opends.sdk.util.Validator;
import org.opends.server.types.ByteSequence;
import org.opends.server.types.ByteString;
import org.opends.messages.MessageBuilder;
import org.opends.messages.Message;
import static org.opends.messages.SchemaMessages.WARN_ATTR_SYNTAX_LDAPSYNTAX_ENUM_INVALID_VALUE;

import java.util.*;

/**
 * This class provides an enumeration-based mechanism where a new syntax
 * and its corresponding matching rules can be created on-the-fly. An enum
 * syntax is an LDAPSyntaxDescriptionSyntax with X-ENUM extension.
 */
public final class EnumSyntax extends AbstractSyntaxImplementation
{
  private final String oid;
  //Set of read-only enum entries.
  private final List<String> entries;

  public EnumSyntax(String oid, List<String> entries) {
    Validator.ensureNotNull(oid, entries);
    this.oid = oid;
    List<String> entryStrings = new ArrayList<String>(entries.size());

    for(String entry : entries)
    {
      String normalized = normalize(ByteString.valueOf(entry));
      if(!entryStrings.contains(normalized))
      {
        entryStrings.add(normalized);
      }
    }
    this.entries = Collections.unmodifiableList(entryStrings);
  }

  public String getName() {
    return oid;
  }

  public boolean isHumanReadable() {
    return true;
  }

  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    //The value is acceptable if it belongs to the set.
    boolean isAllowed = entries.contains(normalize(value));

    if(!isAllowed)
    {
      Message message = WARN_ATTR_SYNTAX_LDAPSYNTAX_ENUM_INVALID_VALUE.get(
          value.toString(), oid);
      invalidReason.append(message);
    }

    return isAllowed;
  }

  private String normalize(ByteSequence value)
  {
    StringBuilder buffer = new StringBuilder();
    prepareUnicode(buffer, value, TRIM, CASE_FOLD);

    int bufferLength = buffer.length();
    if (bufferLength == 0)
    {
      if (value.length() > 0)
      {
        // This should only happen if the value is composed entirely of spaces.
        // In that case, the normalized value is a single space.
        return " ";
      }
      else
      {
        // The value is empty, so it is already normalized.
        return "";
      }
    }


    // Replace any consecutive spaces with a single space.
    for (int pos = bufferLength-1; pos > 0; pos--)
    {
      if (buffer.charAt(pos) == ' ')
      {
        if (buffer.charAt(pos-1) == ' ')
        {
          buffer.delete(pos, pos+1);
        }
      }
    }

    return buffer.toString();
  }

  public Iterable<String> getEntries()
  {
    return entries;
  }

  public int indexOf(ByteSequence value)
  {
    return entries.indexOf(normalize(value));
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_CASE_IGNORE_OID;
  }

  @Override
  public String getOrderingMatchingRule() {
    return OMR_OID_GENERIC_ENUM + "." + oid;
  }

  @Override
  public String getSubstringMatchingRule() {
    return SMR_CASE_IGNORE_OID;
  }

  @Override
  public String getApproximateMatchingRule() {
    return AMR_DOUBLE_METAPHONE_OID;
  }
}
