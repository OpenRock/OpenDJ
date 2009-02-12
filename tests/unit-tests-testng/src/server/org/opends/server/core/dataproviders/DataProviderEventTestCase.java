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
 *      Copyright 2008 Sun Microsystems, Inc.
 */
package org.opends.server.core.dataproviders;



import static org.testng.Assert.*;

import java.util.EnumSet;

import org.opends.messages.Message;
import org.testng.annotations.Test;



/**
 * This class tests {@link DataProviderEvent}.
 */
public final class DataProviderEventTestCase extends DataProviderTestCase
{

  /**
   * Verify that null data provider is not permitted.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testConstructor1()
  {
    new DataProviderEvent(Message.EMPTY, EnumSet
        .of(DataProviderEvent.Type.ACCESS_MODE));
  }



  /**
   * Verify that null reason is not permitted.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testConstructor2()
  {
    new DataProviderEvent(null, EnumSet.of(DataProviderEvent.Type.ACCESS_MODE));
  }



  /**
   * Verify that null types is not permitted.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testConstructor3()
  {
    new DataProviderEvent(Message.EMPTY, null);
  }



  /**
   * Verify that empty types is not permitted.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void testConstructor4()
  {
    new DataProviderEvent(Message.EMPTY, EnumSet
        .noneOf(DataProviderEvent.Type.class));
  }



  /**
   * Verify that reason getter works.
   */
  @Test
  public void testGetReason()
  {
    DataProviderEvent event = new DataProviderEvent(Message.EMPTY, EnumSet
        .of(DataProviderEvent.Type.ACCESS_MODE));
    assertSame(event.getReason(), Message.EMPTY);
  }



  /**
   * Verify that event types getter works.
   */
  @Test
  public void testGetEventTypes1()
  {
    DataProviderEvent event = new DataProviderEvent(Message.EMPTY, EnumSet
        .of(DataProviderEvent.Type.ACCESS_MODE));
    assertEquals(event.getEventTypes(), EnumSet
        .of(DataProviderEvent.Type.ACCESS_MODE));
  }



  /**
   * Verify that event types getter works.
   */
  @Test
  public void testGetEventTypes2()
  {
    DataProviderEvent event = new DataProviderEvent(Message.EMPTY, EnumSet.of(
        DataProviderEvent.Type.ACCESS_MODE,
        DataProviderEvent.Type.SUPPORTED_CONTROLS));
    assertEquals(event.getEventTypes(), EnumSet.of(
        DataProviderEvent.Type.ACCESS_MODE,
        DataProviderEvent.Type.SUPPORTED_CONTROLS));
  }

}
