/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.p2.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.sonatype.nexus.repository.browse.BrowsePaths;
import org.sonatype.nexus.repository.browse.BrowseTestSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;

import org.junit.Assert;
import org.junit.Test;

import static org.sonatype.nexus.repository.p2.internal.util.P2PathUtils.DIVIDER;

public class P2BrowseNodeGeneratorTest
    extends BrowseTestSupport
{
  private static final List<String> COMPONENT_NAME = Arrays.asList("org", "tigris", "subversion", "clientadapter", "svnkit");

  private static final String COMPONENT_VERSION = "1.7.5";

  private static final String BASE_URL = "www.dummy-url.com";

  private static final String REMOTE_PREFIX = "http/" + BASE_URL;

  private static final String FEATURES = "features";

  private static final String ASSET_NAME = "assetName";

  private P2BrowseNodeGenerator generator = new P2BrowseNodeGenerator();

  @Test
  public void testCompositeRepository() {
    Component component = createComponent(String.join(".", COMPONENT_NAME), null, COMPONENT_VERSION);
    Asset asset = createAsset(REMOTE_PREFIX + DIVIDER + FEATURES + DIVIDER + ASSET_NAME);

    List<String> paths = generator.computeAssetPaths(asset, component).stream().map(BrowsePaths::getBrowsePath).collect(
        Collectors.toList());
    List<String> expectedResult = new ArrayList<>();
    expectedResult.add(BASE_URL);
    expectedResult.addAll(COMPONENT_NAME);
    expectedResult.addAll(Arrays.asList(COMPONENT_VERSION, FEATURES, ASSET_NAME));
    Assert.assertEquals(expectedResult, paths);
  }

  @Test
  public void testCompositeRepositoryWithoutComponent() {
    Asset asset = createAsset(REMOTE_PREFIX + DIVIDER + ASSET_NAME);

    List<String> paths = generator.computeAssetPaths(asset, null).stream().map(BrowsePaths::getBrowsePath).collect(
        Collectors.toList());
    List<String> expectedResult = new ArrayList<>();
    expectedResult.add(BASE_URL);
    expectedResult.add(ASSET_NAME);
    Assert.assertEquals(expectedResult, paths);
  }

  @Test
  public void testSimpleRepository() {
    Component component = createComponent(String.join(".", COMPONENT_NAME), null, COMPONENT_VERSION);
    Asset asset = createAsset(FEATURES + DIVIDER + ASSET_NAME);

    List<String> paths = generator.computeAssetPaths(asset, component).stream().map(BrowsePaths::getBrowsePath).collect(
        Collectors.toList());
    List<String> expectedResult = new ArrayList<>();
    expectedResult.addAll(COMPONENT_NAME);
    expectedResult.addAll(Arrays.asList(COMPONENT_VERSION, FEATURES, ASSET_NAME));
    Assert.assertEquals(expectedResult, paths);
  }

  @Test
  public void testSimpleRepositoryWithoutComponent() {
    Asset asset = createAsset(ASSET_NAME);

    List<String> paths = generator.computeAssetPaths(asset, null).stream().map(BrowsePaths::getBrowsePath).collect(
        Collectors.toList());
    List<String> expectedResult = new ArrayList<>();
    expectedResult.add(ASSET_NAME);
    Assert.assertEquals(expectedResult, paths);
  }
}
