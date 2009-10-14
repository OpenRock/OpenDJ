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
 *      Copyright 2009 Sun Microsystems, Inc.
 */

package org.opends.sdk.examples;



import org.opends.sdk.DN;
import org.opends.sdk.schema.Schema;



/**
 * Schema test.
 */
public class SchemaTest
{
  public static final void main(String[] args)
  {
    Schema defaultSchema = Schema.getCoreSchema();

    DN dn =
        DN
            .valueOf(
                "  attributetypes=\\0dLu\\C4\\8Di\\C4\\87                    ,nameforms=\"Before\\0d\\\"After\\\" , +\"+  objectclasses= test  ,nameforms=com  ",
                defaultSchema);
    DN dn2 = DN.valueOf(dn.toString(), defaultSchema);
    System.out.println(dn);
    System.out.println(dn2);
    System.out.println(dn.toString().equals(dn2.toString()));
  }
}
