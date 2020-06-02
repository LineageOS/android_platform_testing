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

package android.device.collectors;

import android.device.collectors.annotations.OptionClass;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import com.android.helpers.RssSnapshotHelper;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link RssSnapshotListener} that takes a snapshot of Rss sizes for the list of
 * specified processes.
 *
 * Options:
 * -e process-names [processNames] : a comma-separated list of processes
 * -e drop-cache [pagecache | slab | all] : drop cache flag
 * -e test-output-dir [path] : path to the output directory
 */
@OptionClass(alias = "rsssnapshot-collector")
public class RssSnapshotListener extends BaseCollectionListener<String> {
  private static final String TAG = RssSnapshotListener.class.getSimpleName();
  private static final String DEFAULT_OUTPUT_DIR = "/sdcard/test_results";

  @VisibleForTesting static final String PROCESS_SEPARATOR = ",";
  @VisibleForTesting static final String PROCESS_NAMES_KEY = "process-names";
  @VisibleForTesting static final String DROP_CACHE_KEY = "drop-cache";
  @VisibleForTesting static final String OUTPUT_DIR_KEY = "test-output-dir";

  private RssSnapshotHelper mRssSnapshotHelper = new RssSnapshotHelper();
  private final Map<String, Integer> dropCacheValues = new HashMap<String, Integer>() {
    {
      put("pagecache", 1);
      put("slab", 2);
      put("all", 3);
    }
  };

  public RssSnapshotListener() {
    createHelperInstance(mRssSnapshotHelper);
  }

  /**
   * Constructor to simulate receiving the instrumentation arguments. Should not be used except
   * for testing.
   */
  @VisibleForTesting
  public RssSnapshotListener(Bundle args, RssSnapshotHelper helper) {
    super(args, helper);
    mRssSnapshotHelper = helper;
    createHelperInstance(mRssSnapshotHelper);
  }

  /**
   * Adds the options for rss snapshot collector.
   */
  @Override
  public void setupAdditionalArgs() {
    Bundle args = getArgsBundle();
    String testOutputDir = args.getString(OUTPUT_DIR_KEY, DEFAULT_OUTPUT_DIR);
    // Collect for all processes if process list is empty or null.
    String procsString = args.getString(PROCESS_NAMES_KEY);

    String[] procs = null;
    if (procsString == null || procsString.isEmpty()) {
      mRssSnapshotHelper.setAllProcesses();
    } else {
      procs = procsString.split(PROCESS_SEPARATOR);
    }

    mRssSnapshotHelper.setUp(testOutputDir, procs);

    String dropCacheValue = args.getString(DROP_CACHE_KEY);
    if (dropCacheValue != null) {
      if (dropCacheValues.containsKey(dropCacheValue)) {
        mRssSnapshotHelper.setDropCacheOption(dropCacheValues.get(dropCacheValue));
      } else {
        Log.e(TAG, "Value for \"" + DROP_CACHE_KEY + "\" parameter is invalid");
        return;
      }
    }
  }
}
