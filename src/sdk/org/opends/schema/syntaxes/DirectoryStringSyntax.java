package org.opends.schema.syntaxes;

import static org.opends.messages.SchemaMessages.ERR_ATTR_SYNTAX_DIRECTORYSTRING_INVALID_ZEROLENGTH_VALUE;
import static org.opends.server.schema.SchemaConstants.SYNTAX_DIRECTORY_STRING_NAME;

import org.opends.messages.MessageBuilder;
import org.opends.schema.Schema;
import org.opends.server.types.ByteSequence;

/**
 * This class defines the directory string attribute syntax, which is simply a
 * set of UTF-8 characters.  By default, they will be treated in a
 * case-insensitive manner, and equality, ordering, substring, and approximate
 * matching will be allowed.
 */
public class DirectoryStringSyntax extends AbstractSyntaxImplementation
{
  // Indicates whether we will allow zero-length values.
  private boolean allowZeroLengthValues;

  public DirectoryStringSyntax(boolean allowZeroLengthValues)
  {
    this.allowZeroLengthValues = allowZeroLengthValues;
  }

  public String getName() {
    return SYNTAX_DIRECTORY_STRING_NAME;
  }

  /**
   * Indicates whether the provided value is acceptable for use in an attribute
   * with this syntax.  If it is not, then the reason may be appended to the
   * provided buffer.
   *
   * @param schema
   *@param  value          The value for which to make the determination.
   * @param  invalidReason  The buffer to which the invalid reason should be
 *                        appended.
 * @return  <CODE>true</CODE> if the provided value is acceptable for use with
   *          this syntax, or <CODE>false</CODE> if not.
   */
  public boolean valueIsAcceptable(Schema schema, ByteSequence value,
                                   MessageBuilder invalidReason)
  {
    if (allowZeroLengthValues || (value.length() > 0))
    {
      return true;
    }
    else
    {
      invalidReason.append(
          ERR_ATTR_SYNTAX_DIRECTORYSTRING_INVALID_ZEROLENGTH_VALUE.get());
      return false;
    }
  }



  /**
   * Indicates whether zero-length values will be allowed.  This is technically
   * forbidden by the LDAP specification, but it was allowed in earlier versions
   * of the server, and the discussion of the directory string syntax in RFC
   * 2252 does not explicitly state that they are not allowed.
   *
   * @return  <CODE>true</CODE> if zero-length values should be allowed for
   *          attributes with a directory string syntax, or <CODE>false</CODE>
   *          if not.
   */
  public boolean allowZeroLengthValues()
  {
    return allowZeroLengthValues;
  }

  public boolean isHumanReadable() {
    return true;
  }
}
