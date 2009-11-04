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

/**
 * OpenDS SDK.
 *
 * <h1>TODO</h1>
 * <ul>
 * <li>LDIF support <b>[Matt]</b>
 * <ul>
 * <li>LDIFReader
 * <ul>
 * <li>filtered reader (this should wrap an entry enumeration)
 * <li>should implement generic entry enumeration API.
 * </ul>
 * <li>LDIFWriter
 * <ul>
 * <li>add comments for DNs
 * <li>comments in native charset
 * <li>rest of output must be in ASCII
 * </ul>
 * </ul>
 * <li>Messages
 * <li>Logging?
 * <li>Single entry search, blocking search <b>[Bo]</b>
 * <li>Exceptions sub-types for ErrorResultException (e.g. referrals, assertion failures, client side errors).
 * <li>Refactor non-schema aware request / response APIs - how should they handle duplicate attribute descriptions and values? I.e. what matching should be performed: do they have List or Set semantics? [Matt]
 * <ul>
 * <li>AttributeValueSequence -> AttributeValueCollection?
 * <li>AttributeSequence -> AttributeCollection?
 * <li>SearchResultEntry must be cheap to decode in non schema case.
 * <li>Schema aware versions of these should provide set semantics w.r.t. attribute descriptions and attribute values.
 * </ul>
 * <li>How should non-default Grizzly transport be specified by the application?
 * <li>Unmodifiable requests and responses
 * <li>Check that it is possible to create SearchResultEntry objects with empty attributes.
 * <li>Nameable? All objects that have a getName() method
 * <li>DN, RDN - check APIs. <b>[Matt]</b>
 * <li>Schema - clean up abstract stuff. Ensure exception handling is correct. <b>[Matt]</b>
 * <li>Enum / GeneralizedTime parsing function
 * <li>LDAP connection request timeouts configured using LDAPConnectionOptions.
 * <li>Re-instate Connection.isValid()
 * <li>Support parameters in result handlers.
 * <li>Javadoc
 * <li>Unit tests
 * <li>Move to standalone source tree
 * <li>LDAP URL support and referral support
 * </ul>
 *
 */
package org.opends.sdk;



