/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.helpers.tests;

import static com.android.helpers.MetricUtility.constructKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.runner.AndroidJUnit4;
import com.android.helpers.RssSnapshotHelper;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Android Unit tests for {@link RssSnapshotHelper}.
 *
 * To run:
 * atest CollectorsHelperTest:com.android.helpers.tests.RssSnapshotHelperTest
 */
@RunWith(AndroidJUnit4.class)
public class RssSnapshotHelperTest {
  private static final String TAG = RssSnapshotHelperTest.class.getSimpleName();

  // Valid output file
  private static final String VALID_OUTPUT_DIR = "/sdcard/test_results";
  // Invalid output file (no permissions to write)
  private static final String INVALID_OUTPUT_DIR = "/data/local/tmp";

  // Lists of process names
  private static final String[] EMPTY_PROCESS_LIST = {};
  private static final String[] ONE_PROCESS_LIST = {"com.android.systemui"};
  private static final String[] TWO_PROCESS_LIST = {"com.android.systemui", "system_server"};
  private static final String[] NO_PROCESS_LIST = {null};

  private RssSnapshotHelper mRssSnapshotHelper;

  @Before
  public void setUp() {
    mRssSnapshotHelper = new RssSnapshotHelper();
  }

  /**
   * Test start collecting returns false if the helper has not been properly set up.
   */
  @Test
  public void testSetUpNotCalled() {
    assertFalse(mRssSnapshotHelper.startCollecting());
  }

  /**
   * Test invalid options for drop cache flag.
   */
  @Test
  public void testInvalidDropCacheOptions() {
    assertFalse(mRssSnapshotHelper.setDropCacheOption(-1));
    assertFalse(mRssSnapshotHelper.setDropCacheOption(0));
    assertFalse(mRssSnapshotHelper.setDropCacheOption(4));
  }

  /**
   * Test invalid options for drop cache flag.
   */
  @Test
  public void testValidDropCacheOptions() {
    assertTrue(mRssSnapshotHelper.setDropCacheOption(1));
    assertTrue(mRssSnapshotHelper.setDropCacheOption(2));
    assertTrue(mRssSnapshotHelper.setDropCacheOption(3));
  }

  /**
   * Test no metrics are sampled if process name is empty.
   */
  @Test
  public void testEmptyProcessName() {
    mRssSnapshotHelper.setUp(VALID_OUTPUT_DIR, EMPTY_PROCESS_LIST);
    Map<String, String> metrics = mRssSnapshotHelper.getMetrics();
    assertTrue(metrics.isEmpty());
  }

  /**
   * Test sampling on a valid and running process.
   */
  @Test
  public void testValidFile() {
    mRssSnapshotHelper.setUp(VALID_OUTPUT_DIR, ONE_PROCESS_LIST);
    assertTrue(mRssSnapshotHelper.startCollecting());
  }

  /**
   * Test sampling on using an invalid output file.
   */
  @Test
  public void testInvalidFile() {
    mRssSnapshotHelper.setUp(INVALID_OUTPUT_DIR, ONE_PROCESS_LIST);
    assertFalse(mRssSnapshotHelper.startCollecting());
  }

  /**
   * Test getting metrics from one process.
   */
  @Test
  public void testGetMetrics_OneProcess() {
    testProcessList(ONE_PROCESS_LIST);
  }

  /**
   * Test getting metrics from multiple processes process.
   */
  @Test
  public void testGetMetrics_MultipleProcesses() {
    testProcessList(TWO_PROCESS_LIST);
  }

  /**
   * Test all process flag return more than 2 processes metrics atleast.
   */
  @Test
  public void testGetMetrics_AllProcess() {
    mRssSnapshotHelper.setUp(VALID_OUTPUT_DIR, NO_PROCESS_LIST);
    mRssSnapshotHelper.setAllProcesses();
    assertTrue(mRssSnapshotHelper.startCollecting());
    Map<String, String> metrics = mRssSnapshotHelper.getMetrics();
    assertTrue(metrics.size() > 2);
    assertTrue(metrics.containsKey(RssSnapshotHelper.OUTPUT_FILE_PATH_KEY));

  }


  private void testProcessList(String... processNames) {
    mRssSnapshotHelper.setUp(VALID_OUTPUT_DIR, processNames);
    assertTrue(mRssSnapshotHelper.startCollecting());
    Map<String, String> metrics = mRssSnapshotHelper.getMetrics();
    assertFalse(metrics.isEmpty());
    for (String processName : processNames) {
      assertTrue(
          metrics.containsKey(constructKey(RssSnapshotHelper.RSS_METRIC_PREFIX, processName)));
    }
    assertTrue(metrics.containsKey(RssSnapshotHelper.OUTPUT_FILE_PATH_KEY));
  }
}
