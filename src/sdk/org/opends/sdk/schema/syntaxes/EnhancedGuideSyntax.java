package org.opends.sdk.schema.syntaxes;

import static org.opends.messages.SchemaMessages.*;
import static org.opends.sdk.util.StaticUtils.toLowerCase;
import org.opends.sdk.util.SubstringReader;

import org.opends.messages.MessageBuilder;
import org.opends.sdk.schema.Schema;
import org.opends.sdk.schema.SchemaUtils;
import org.opends.sdk.DecodeException;
import org.opends.server.types.ByteSequence;
import static org.opends.sdk.schema.SchemaConstants.*;

/**
 * This class implements the enhanced guide attribute syntax, which may be used
 * to provide criteria for generating search filters for entries of a given
 * objectclass.
 */
public class EnhancedGuideSyntax extends AbstractSyntaxImplementation
{
  public String getName() {
    return SYNTAX_ENHANCED_GUIDE_NAME;
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
    // Get a lowercase string version of the provided value.
    String valueStr = toLowerCase(value.toString());


    // Find the position of the first octothorpe.  It should denote the end of
    // the objectclass.
    int sharpPos = valueStr.indexOf('#');
    if (sharpPos < 0)
    {

      invalidReason.append(
              ERR_ATTR_SYNTAX_ENHANCEDGUIDE_NO_SHARP.get(valueStr));
      return false;
    }


    // Get the objectclass and see if it is a valid name or OID.
    String ocName   = valueStr.substring(0, sharpPos).trim();
    int    ocLength = ocName.length();
    if (ocLength == 0)
    {

      invalidReason.append(ERR_ATTR_SYNTAX_ENHANCEDGUIDE_NO_OC.get(valueStr));
      return false;
    }

    try
    {
      SchemaUtils.readOID(new SubstringReader(ocName.substring(ocLength)));
    }
    catch(DecodeException de)
    {
      invalidReason.append(de.getMessageObject());
      return false;
    }

    // Find the last octothorpe and make sure it is followed by a valid scope.
    int lastSharpPos = valueStr.lastIndexOf('#');
    if (lastSharpPos == sharpPos)
    {

      invalidReason.append(
              ERR_ATTR_SYNTAX_ENHANCEDGUIDE_NO_FINAL_SHARP.get(valueStr));
      return false;
    }

    String scopeStr = valueStr.substring(lastSharpPos+1).trim();
    if (! (scopeStr.equals("baseobject") || scopeStr.equals("onelevel") ||
           scopeStr.equals("wholesubtree") ||
           scopeStr.equals("subordinatesubtree")))
    {
      if (scopeStr.length() == 0)
      {

        invalidReason.append(
                ERR_ATTR_SYNTAX_ENHANCEDGUIDE_NO_SCOPE.get(valueStr));
      }
      else
      {

        invalidReason.append(
                ERR_ATTR_SYNTAX_ENHANCEDGUIDE_INVALID_SCOPE.get(
                        valueStr, scopeStr));
      }

      return false;
    }


    // Everything between the two octothorpes must be the criteria.  Make sure
    // it is valid.
    String criteria       = valueStr.substring(sharpPos+1, lastSharpPos).trim();
    int    criteriaLength = criteria.length();
    if (criteriaLength == 0)
    {

      invalidReason.append(
              ERR_ATTR_SYNTAX_ENHANCEDGUIDE_NO_CRITERIA.get(valueStr));
      return false;
    }

    return GuideSyntax.criteriaIsValid(criteria, valueStr, invalidReason);
  }

  public boolean isHumanReadable() {
    return true;
  }

  @Override
  public String getEqualityMatchingRule() {
    return EMR_OCTET_STRING_OID;
  }

  @Override
  public String getOrderingMatchingRule() {
    return OMR_OCTET_STRING_OID;
  }
}
