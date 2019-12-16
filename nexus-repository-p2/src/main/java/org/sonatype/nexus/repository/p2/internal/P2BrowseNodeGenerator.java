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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.browse.BrowseNodeGenerator;
import org.sonatype.nexus.repository.browse.BrowsePaths;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;

import com.google.common.base.Splitter;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import static org.sonatype.nexus.repository.p2.internal.util.P2PathUtils.DIVIDER;
import static org.sonatype.nexus.repository.p2.internal.util.P2PathUtils.HTTPS_NXRM_PREFIX;
import static org.sonatype.nexus.repository.p2.internal.util.P2PathUtils.HTTP_NXRM_PREFIX;

/**
 * @since 0.next
 */
@Singleton
@Named(P2Format.NAME)
public class P2BrowseNodeGenerator
    implements BrowseNodeGenerator
{
  private static final String REMOTE_URL_PREFIX_REGEX = "(" + HTTP_NXRM_PREFIX + "|" + HTTPS_NXRM_PREFIX + ")";

  @Override
  public List<BrowsePaths> computeAssetPaths(final Asset asset, @Nullable final Component component) {
    int shift = component == null ? 1 : 2;
    Pair<String, String> assetName = splitToRemoteAndAssetName(asset.name(), shift);
    List<BrowsePaths> paths = Lists.newArrayList(computeComponentPaths(asset, component).iterator());
    paths.addAll(BrowsePaths
        .fromPaths(Lists.newArrayList(Splitter.on(DIVIDER).omitEmptyStrings().split(assetName.getRight()).iterator()),
            true));
    return paths;
  }

  @Override
  public List<BrowsePaths> computeComponentPaths(final Asset asset, @Nullable final Component component) {
    List<String> pathParts = new ArrayList<>();

    int shift = component == null ? 1 : 2;
    String remoteFromAssetName = splitToRemoteAndAssetName(asset.name(), shift).getLeft();
    if (!remoteFromAssetName.isEmpty()) {
      pathParts.add(remoteFromAssetName);
    }
    if (component != null) {
      pathParts.addAll(Lists.newArrayList(Splitter.on('.').omitEmptyStrings().split(component.name()).iterator()));
      pathParts.add(component.version());
    }

    return BrowsePaths.fromPaths(pathParts, true);
  }

  private Pair<String, String> splitToRemoteAndAssetName(final String assetName, final int shift) {
    int indexOfRemoteEnd = StringUtils.lastOrdinalIndexOf(assetName, DIVIDER, shift);
    return indexOfRemoteEnd != -1 ? Pair
        .of(getRemoteWithoutPrefix(assetName.substring(0, indexOfRemoteEnd)),
            assetName.substring(indexOfRemoteEnd + 1)) : Pair
        .of(StringUtils.EMPTY, assetName);
  }

  private String getRemoteWithoutPrefix(final String remote) {
    Matcher matcher = Pattern.compile(REMOTE_URL_PREFIX_REGEX).matcher(remote);
    return matcher.find() ? remote.substring(matcher.end()) : remote;
  }
}
